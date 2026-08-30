package com.example.zerogrid.mesh.transport

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import com.example.zerogrid.debug.DebugLevel
import com.example.zerogrid.debug.DebugLogger
import com.example.zerogrid.mesh.engine.MeshNode
import com.example.zerogrid.mesh.engine.MeshPacket
import com.example.zerogrid.mesh.engine.PacketType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

/**
 * Bluetooth Low Energy Mesh Driver — ZeroGrid
 *
 * FIX SUMMARY:
 * 1. LOGICAL NODE-ID ADDRESSING: Maps logical NodeIDs (e.g. NODE-d48923d7) to fresh BluetoothDevice
 *    objects updated on every advertisement scan, eliminating stale BLE Resolvable Private Addresses (RPAs).
 * 2. SERVICE DATA ADVERTISING: Advertises Node ID in BLE Service Data so scanners resolve the NodeID immediately.
 * 3. SCAN-PAUSE ON CONNECT (Fix Status 147): Pauses BLE scanning during connectGatt() to prevent radio
 *    contention on Android Bluetooth controller, then resumes after connection/timeout.
 * 4. SEQUENTIAL HANDSHAKE & BIDIRECTIONAL GATT: Discovers services -> requests MTU 512 -> enables CCCD -> READY.
 * 5. AUTOMATIC PEER_DISCOVERY HANDSHAKE: Exchanges NodeID and Alias immediately upon GATT ready state.
 */
class BleMeshDriver(
    private val context: Context,
    private val localNodeId: String,
    private var localDisplayName: String = "ZeroGrid Node"
) : MeshTransport {

    companion object {
        private const val TAG = "BleMeshDriver"

        /** Shared service UUID — identical on all ZeroGrid devices. */
        val SERVICE_UUID: UUID = UUID.fromString("0000a701-0000-1000-8000-00805f9b34fb")

        /** Client writes TO this characteristic on the server (client->server direction). */
        val WRITE_CHAR_UUID: UUID = UUID.fromString("0000a702-0000-1000-8000-00805f9b34fb")

        /** Server notifies clients via this characteristic (server->client direction). */
        val NOTIFY_CHAR_UUID: UUID = UUID.fromString("0000a703-0000-1000-8000-00805f9b34fb")

        /** Standard BLE Client Characteristic Configuration Descriptor UUID. */
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        val CHARACTERISTIC_UUID: UUID = WRITE_CHAR_UUID

        private const val REASSEMBLY_TIMEOUT_MS = 30_000L
        private const val MAX_REASSEMBLY_SESSIONS = 10
        private const val MAX_PACKET_SIZE = 65536
        private const val READY_WAIT_RETRIES = 60    // 6 seconds max
        private const val CONNECT_WAIT_RETRIES = 40  // 4 seconds max
    }

    private data class BleFrame(
        val transmissionId: UUID,
        val sequence: Int,
        val flags: Byte,
        val payload: ByteArray,
    ) {
        companion object {
            const val HEADER_SIZE = 16 + 4 + 1  // UUID(16) + seq(4) + flags(1) = 21 bytes
            const val FLAG_SINGLE = 0x00.toByte()
            const val FLAG_START  = 0x01.toByte()
            const val FLAG_MIDDLE = 0x02.toByte()
            const val FLAG_END    = 0x03.toByte()

            fun fromBytes(bytes: ByteArray): BleFrame? {
                if (bytes.size < HEADER_SIZE) return null
                return try {
                    val buffer = ByteBuffer.wrap(bytes)
                    val mostSig  = buffer.long
                    val leastSig = buffer.long
                    val seq      = buffer.int
                    val flag     = buffer.get()
                    val payload  = ByteArray(buffer.remaining())
                    buffer.get(payload)
                    BleFrame(UUID(mostSig, leastSig), seq, flag, payload)
                } catch (_: Exception) { null }
            }
        }

        fun toBytes(): ByteArray {
            val buffer = ByteBuffer.allocate(HEADER_SIZE + payload.size)
            buffer.putLong(transmissionId.mostSignificantBits)
            buffer.putLong(transmissionId.leastSignificantBits)
            buffer.putInt(sequence)
            buffer.put(flags)
            buffer.put(payload)
            return buffer.array()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is BleFrame) return false
            return transmissionId == other.transmissionId &&
                   sequence == other.sequence &&
                   flags == other.flags &&
                   payload.contentEquals(other.payload)
        }

        override fun hashCode(): Int {
            var r = transmissionId.hashCode()
            r = 31 * r + sequence
            r = 31 * r + flags.toInt()
            r = 31 * r + payload.contentHashCode()
            return r
        }
    }

    private class ReassemblySession(transmissionId: UUID) {
        val chunks = ConcurrentHashMap<Int, ByteArray>()
        var lastUpdate = System.currentTimeMillis()
        var isComplete = false
        var expectedChunks = -1

        init { DebugLogger.log(TAG, "Reassembly session started: $transmissionId", DebugLevel.VERBOSE) }

        fun addChunk(frame: BleFrame): Boolean {
            lastUpdate = System.currentTimeMillis()
            chunks[frame.sequence] = frame.payload
            if (frame.flags == BleFrame.FLAG_END || frame.flags == BleFrame.FLAG_SINGLE) {
                expectedChunks = frame.sequence + 1
            }
            if (expectedChunks != -1 && chunks.size == expectedChunks) {
                isComplete = true
            }
            return isComplete
        }

        fun getFullData(): ByteArray? {
            if (!isComplete) return null
            val totalSize = chunks.values.sumOf { it.size }
            if (totalSize > MAX_PACKET_SIZE) return null
            val buffer = ByteBuffer.allocate(totalSize)
            for (i in 0 until expectedChunks) {
                val chunk = chunks[i] ?: return null
                buffer.put(chunk)
            }
            return buffer.array()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MeshTransport interface
    // ─────────────────────────────────────────────────────────────────────────
    override val transportName: String = MeshNode.TRANSPORT_BLE
    override var isRunning: Boolean = false
        private set

    private val _packetFlow = MutableSharedFlow<MeshPacket>(extraBufferCapacity = 64)
    override val packetFlow: SharedFlow<MeshPacket> = _packetFlow.asSharedFlow()

    private val _peerDiscoveryFlow = MutableSharedFlow<MeshNode>(extraBufferCapacity = 64)
    override val peerDiscoveryFlow: SharedFlow<MeshNode> = _peerDiscoveryFlow.asSharedFlow()

    // ─────────────────────────────────────────────────────────────────────────
    // Dynamic NodeID & Device Addressing
    // ─────────────────────────────────────────────────────────────────────────

    /** Maps logical NodeID (e.g. "NODE-d48923d7") -> freshest BluetoothDevice object */
    private val nodeToDeviceMap = ConcurrentHashMap<String, BluetoothDevice>()

    /** Maps Bluetooth MAC address -> logical NodeID */
    private val addressToNodeMap = ConcurrentHashMap<String, String>()

    /** Devices discovered by scan, keyed by MAC address and NodeID */
    private val discoveredDevices = ConcurrentHashMap<String, BluetoothDevice>()

    /** GATT clients WE opened (client mode). Key = BLE MAC address */
    private val activeGatts = ConcurrentHashMap<String, BluetoothGatt>()

    /** Devices connected TO OUR server (server mode). Key = BLE MAC address */
    private val serverConnectedDevices = ConcurrentHashMap<String, BluetoothDevice>()

    /** Negotiated MTU per peer (keyed by address and NodeID) */
    private val peerMtus = ConcurrentHashMap<String, Int>()

    /** Handshake completion flag per peer address */
    private val peerReady = ConcurrentHashMap<String, Boolean>()

    /** Tracks which server-connected peers enabled CCCD for notifications */
    private val notifyEnabledPeers = ConcurrentHashMap<String, Boolean>()

    private val writeMutex = Mutex()
    private val reassemblySessions = ConcurrentHashMap<UUID, ReassemblySession>()

    private val bluetoothManager: BluetoothManager? by lazy {
        try { context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager } catch (_: Exception) { null }
    }
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        try { bluetoothManager?.adapter } catch (_: Exception) { null }
    }

    private var bleScanner: BluetoothLeScanner? = null
    private var bleAdvertiser: BluetoothLeAdvertiser? = null
    private var gattServer: BluetoothGattServer? = null
    private var isScanning = false

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        DebugLogger.log(TAG, "BleMeshDriver initialized for node $localNodeId", DebugLevel.INFO)
    }

    fun updateDisplayName(name: String) {
        localDisplayName = name
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        if (isRunning) { DebugLogger.log(TAG, "Already running", DebugLevel.VERBOSE); return }
        if (!hasRequiredPermissions()) {
            DebugLogger.log(TAG, "Missing BLE permissions — cannot start", DebugLevel.ERROR)
            return
        }
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            DebugLogger.log(TAG, "Bluetooth adapter unavailable or disabled", DebugLevel.WARN)
            return
        }
        isRunning = true
        setupGattServer()
        startAdvertising()
        startScanning()
        DebugLogger.log(TAG, "BLE discovery started — node $localNodeId", DebugLevel.INFO)
    }

    @SuppressLint("MissingPermission")
    override fun stopDiscovery() {
        isRunning = false
        try { stopAdvertising() } catch (_: Exception) {}
        try { stopScanning() }    catch (_: Exception) {}
        try { gattServer?.close(); gattServer = null } catch (_: Exception) {}
        activeGatts.values.forEach { try { it.close() } catch (_: Exception) {} }
        activeGatts.clear()
        peerMtus.clear()
        peerReady.clear()
        notifyEnabledPeers.clear()
        serverConnectedDevices.clear()
        discoveredDevices.clear()
        nodeToDeviceMap.clear()
        addressToNodeMap.clear()
        reassemblySessions.clear()
        DebugLogger.log(TAG, "BLE discovery stopped", DebugLevel.INFO)
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GATT CLIENT CALLBACKS
    // ─────────────────────────────────────────────────────────────────────────

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val address = gatt.device.address
            // Resume scanning if it was paused during connect
            resumeScanningAfterConnect()

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    DebugLogger.log(TAG, "CLIENT connected to $address — discovering services", DebugLevel.INFO)
                    DebugLogger.updatePeerState(address) { copy(connectionStage = "DISCOVERING", direction = if (direction == "SERVER") "BOTH" else "CLIENT") }
                    activeGatts[address] = gatt
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    DebugLogger.log(TAG, "CLIENT disconnected from $address", DebugLevel.WARN)
                    DebugLogger.updatePeerState(address) { copy(connectionStage = "DISCONNECTED", isReady = false) }
                    activeGatts.remove(address)
                    peerMtus.remove(address)
                    peerReady.remove(address)
                    try { gatt.close() } catch (_: Exception) {}
                }
            } else {
                DebugLogger.log(TAG, "CLIENT GATT error for $address: status=$status", DebugLevel.ERROR)
                DebugLogger.updatePeerState(address) { copy(connectionStage = "ERROR", lastError = "GATT status $status", isReady = false) }
                activeGatts.remove(address)
                peerMtus.remove(address)
                peerReady.remove(address)
                try { gatt.close() } catch (_: Exception) {}
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val address = gatt.device.address
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                if (service == null) {
                    DebugLogger.log(TAG, "ZeroGrid service NOT found on $address", DebugLevel.ERROR)
                    DebugLogger.updatePeerState(address) { copy(connectionStage = "NO_SERVICE", lastError = "Service UUID mismatch") }
                    return
                }
                DebugLogger.log(TAG, "Services discovered on $address — requesting MTU 512", DebugLevel.INFO)
                DebugLogger.updatePeerState(address) { copy(connectionStage = "MTU_REQUEST") }
                gatt.requestMtu(512)
            } else {
                DebugLogger.log(TAG, "Service discovery failed on $address: status=$status", DebugLevel.ERROR)
                DebugLogger.updatePeerState(address) { copy(connectionStage = "DISCOVER_FAIL", lastError = "status $status") }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            val address = gatt.device.address
            val finalMtu = if (status == BluetoothGatt.GATT_SUCCESS) mtu else 185
            peerMtus[address] = finalMtu
            DebugLogger.log(TAG, "MTU negotiated with $address: $finalMtu bytes (payload=${finalMtu - 3 - BleFrame.HEADER_SIZE})", DebugLevel.INFO)
            DebugLogger.updatePeerState(address) { copy(mtu = finalMtu, connectionStage = "CCCD_ENABLE") }
            enableNotifications(gatt, address)
        }

        @SuppressLint("MissingPermission")
        private fun enableNotifications(gatt: BluetoothGatt, address: String) {
            val service = gatt.getService(SERVICE_UUID)
            val notifyChar = service?.getCharacteristic(NOTIFY_CHAR_UUID)
            if (notifyChar == null) {
                DebugLogger.log(TAG, "NOTIFY_CHAR not found on $address — write-only mode", DebugLevel.WARN)
                markPeerReady(address, notifyEnabled = false)
                return
            }
            gatt.setCharacteristicNotification(notifyChar, true)
            val cccd = notifyChar.getDescriptor(CCCD_UUID)
            if (cccd == null) {
                DebugLogger.log(TAG, "CCCD descriptor not found on $address — write-only mode", DebugLevel.WARN)
                markPeerReady(address, notifyEnabled = false)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            } else {
                @Suppress("DEPRECATION")
                cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(cccd)
            }
            DebugLogger.log(TAG, "CCCD write sent to $address — waiting for confirm", DebugLevel.DEBUG)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            val address = gatt.device.address
            if (descriptor.uuid == CCCD_UUID) {
                val success = (status == BluetoothGatt.GATT_SUCCESS)
                DebugLogger.log(TAG, "✅ CCCD ${if (success) "enabled" else "failed"} on $address — marking READY", DebugLevel.INFO)
                markPeerReady(address, notifyEnabled = success)
            }
        }

        private fun markPeerReady(address: String, notifyEnabled: Boolean) {
            peerReady[address] = true
            DebugLogger.updatePeerState(address) { copy(notifyEnabled = notifyEnabled, isReady = true, connectionStage = "READY") }
            // Exchange Node Announce immediately so remote peer knows our NodeID
            sendPeerAnnounce(address)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            val address = gatt.device.address
            if (status != BluetoothGatt.GATT_SUCCESS) {
                DebugLogger.log(TAG, "Write failed to $address: status=$status", DebugLevel.ERROR)
                DebugLogger.updatePeerState(address) { copy(lastError = "Write failed status=$status") }
            }
            try { writeMutex.unlock() } catch (_: Exception) {}
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val address = gatt.device.address
            val value = characteristic.value ?: return
            DebugLogger.log(TAG, "📩 NOTIFY received from $address: ${value.size} bytes", DebugLevel.DEBUG)
            DebugLogger.updatePeerState(address) { copy(packetsReceived = packetsReceived + 1) }
            handleIncomingBytes(value, address)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            val address = gatt.device.address
            DebugLogger.log(TAG, "📩 NOTIFY received from $address: ${value.size} bytes", DebugLevel.DEBUG)
            DebugLogger.updatePeerState(address) { copy(packetsReceived = packetsReceived + 1) }
            handleIncomingBytes(value, address)
        }
    }

    private fun sendPeerAnnounce(address: String) {
        scope.launch {
            val announcePacket = MeshPacket(
                senderId = localNodeId,
                recipientId = MeshPacket.BROADCAST_ADDRESS,
                type = PacketType.PEER_DISCOVERY,
                payload = localDisplayName
            )
            val gatt = activeGatts[address]
            if (gatt != null) {
                transmitViaWrite(gatt, address, UUID.randomUUID(), announcePacket.toByteArray())
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SEND PACKET
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    override fun sendPacket(packet: MeshPacket, targetPeerId: String?): Boolean {
        if (!isRunning || bluetoothAdapter == null) return false

        scope.launch {
            val payloadBytes   = packet.toByteArray()
            val transmissionId = UUID.randomUUID()

            // Resolve target devices by logical NodeID first, then MAC address
            val targets: List<BluetoothDevice> = when {
                targetPeerId != null -> {
                    val device = resolveDeviceForPeerId(targetPeerId)
                    listOfNotNull(device)
                }
                packet.recipientId != MeshPacket.BROADCAST_ADDRESS && packet.recipientId != "*" -> {
                    val device = resolveDeviceForPeerId(packet.recipientId)
                    if (device != null) listOf(device) else getAllAvailableDevices()
                }
                else -> getAllAvailableDevices()
            }

            if (targets.isEmpty()) {
                DebugLogger.log(TAG, "No BLE peers available for transmission (${packet.type} to ${packet.recipientId})", DebugLevel.WARN)
                return@launch
            }

            DebugLogger.log(TAG, "📤 Sending ${packet.type} packet [${payloadBytes.size}B] to ${targets.size} peer(s)", DebugLevel.INFO)
            targets.forEach { device -> transmitToDevice(device, transmissionId, payloadBytes) }
        }
        return true
    }

    private fun resolveDeviceForPeerId(peerId: String): BluetoothDevice? {
        return nodeToDeviceMap[peerId]
            ?: nodeToDeviceMap[peerId.removePrefix("NODE-")]
            ?: discoveredDevices[peerId]
            ?: addressToNodeMap.entries.firstOrNull { it.value.equals(peerId, ignoreCase = true) }?.let { discoveredDevices[it.key] }
    }

    private fun getAllAvailableDevices(): List<BluetoothDevice> {
        val all = mutableMapOf<String, BluetoothDevice>()
        discoveredDevices.forEach { (k, v) -> all[v.address] = v }
        serverConnectedDevices.forEach { (k, v) -> all[v.address] = v }
        return all.values.toList()
    }

    @SuppressLint("MissingPermission")
    private suspend fun transmitToDevice(device: BluetoothDevice, transmissionId: UUID, data: ByteArray) {
        val address = device.address

        // ── PATH A: We are already connected as CLIENT ────────────────────────
        val clientGatt = activeGatts[address]
        if (clientGatt != null && peerReady[address] == true) {
            transmitViaWrite(clientGatt, address, transmissionId, data)
            return
        }

        // ── PATH B: Stale handle cleanup & Connect ─────────────────────────────
        activeGatts.remove(address)?.let { stale ->
            try { stale.close() } catch (_: Exception) {}
        }
        peerReady.remove(address)

        // Pause scanning momentarily to prevent radio controller contention (Status 147 fix)
        pauseScanningForConnect()

        DebugLogger.log(TAG, "Connecting to $address as GATT client", DebugLevel.INFO)
        DebugLogger.updatePeerState(address) { copy(connectionStage = "CONNECTING") }

        val newGatt = try {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } catch (e: Exception) {
            DebugLogger.log(TAG, "connectGatt exception for $address: ${e.message}", DebugLevel.ERROR)
            resumeScanningAfterConnect()
            return
        }

        // Wait for connection
        var retries = 0
        while (activeGatts[address] == null && retries < CONNECT_WAIT_RETRIES) {
            delay(100.milliseconds)
            retries++
        }

        if (activeGatts[address] == null) {
            DebugLogger.log(TAG, "Connection timeout to $address (${retries * 100}ms)", DebugLevel.ERROR)
            DebugLogger.updatePeerState(address) { copy(connectionStage = "CONNECT_TIMEOUT", lastError = "timeout") }
            resumeScanningAfterConnect()
            return
        }

        // Wait for services + MTU + CCCD handshake
        retries = 0
        while (peerReady[address] != true && retries < READY_WAIT_RETRIES) {
            delay(100.milliseconds)
            retries++
        }

        if (peerReady[address] != true) {
            DebugLogger.log(TAG, "Handshake timeout for $address — skipping send", DebugLevel.ERROR)
            DebugLogger.updatePeerState(address) { copy(lastError = "Handshake timeout") }
            return
        }

        val connectedGatt = activeGatts[address] ?: newGatt
        transmitViaWrite(connectedGatt, address, transmissionId, data)
    }

    @SuppressLint("MissingPermission")
    private suspend fun transmitViaWrite(gatt: BluetoothGatt, address: String, transmissionId: UUID, data: ByteArray) {
        val mtu = peerMtus[address] ?: 185
        val maxPayload = mtu - 3 - BleFrame.HEADER_SIZE
        if (maxPayload <= 0) {
            DebugLogger.log(TAG, "MTU $mtu too small for ZeroGrid framing on $address", DebugLevel.ERROR)
            return
        }

        val service = gatt.getService(SERVICE_UUID)
        val writeChar = service?.getCharacteristic(WRITE_CHAR_UUID)
        if (writeChar == null) {
            DebugLogger.log(TAG, "WRITE_CHAR not found on $address", DebugLevel.ERROR)
            return
        }

        val totalFrames = (data.size + maxPayload - 1) / maxPayload
        DebugLogger.log(TAG, "📦 Sending to $address: ${data.size}B -> $totalFrames frame(s) @ MTU $mtu", DebugLevel.DEBUG)

        var offset = 0
        var sequence = 0

        while (offset < data.size) {
            val chunkSize = minOf(maxPayload, data.size - offset)
            val chunk = data.copyOfRange(offset, offset + chunkSize)
            val flags = when {
                totalFrames == 1 -> BleFrame.FLAG_SINGLE
                offset == 0 -> BleFrame.FLAG_START
                offset + chunkSize >= data.size -> BleFrame.FLAG_END
                else -> BleFrame.FLAG_MIDDLE
            }
            val frameBytes = BleFrame(transmissionId, sequence++, flags, chunk).toBytes()

            writeMutex.lock()
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val result = gatt.writeCharacteristic(writeChar, frameBytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                    if (result != BluetoothGatt.GATT_SUCCESS) {
                        DebugLogger.log(TAG, "writeCharacteristic error on $address: code=$result", DebugLevel.ERROR)
                        try { writeMutex.unlock() } catch (_: Exception) {}
                        return
                    }
                } else {
                    @Suppress("DEPRECATION")
                    writeChar.value = frameBytes
                    writeChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    @Suppress("DEPRECATION")
                    if (!gatt.writeCharacteristic(writeChar)) {
                        DebugLogger.log(TAG, "writeCharacteristic returned false on $address", DebugLevel.ERROR)
                        try { writeMutex.unlock() } catch (_: Exception) {}
                        return
                    }
                }
            } catch (e: Exception) {
                DebugLogger.log(TAG, "Exception during write to $address: ${e.message}", DebugLevel.ERROR)
                try { writeMutex.unlock() } catch (_: Exception) {}
                return
            }
            offset += chunkSize
        }
        DebugLogger.log(TAG, "✅ Sent ${data.size}B to $address in $totalFrames frame(s)", DebugLevel.INFO)
        DebugLogger.updatePeerState(address) { copy(packetsSent = packetsSent + 1) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GATT SERVER
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    private fun setupGattServer() {
        try {
            gattServer = bluetoothManager?.openGattServer(context, gattServerCallback)

            val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

            val writeChar = BluetoothGattCharacteristic(
                WRITE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            service.addCharacteristic(writeChar)

            val notifyChar = BluetoothGattCharacteristic(
                NOTIFY_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
            )
            val cccd = BluetoothGattDescriptor(
                CCCD_UUID,
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
            )
            notifyChar.addDescriptor(cccd)
            service.addCharacteristic(notifyChar)

            gattServer?.addService(service)
            DebugLogger.log(TAG, "GATT server started — service $SERVICE_UUID", DebugLevel.INFO)
        } catch (e: Exception) {
            DebugLogger.log(TAG, "Failed to setup GATT server: ${e.message}", DebugLevel.ERROR)
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            val address = device.address
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                serverConnectedDevices[address] = device
                DebugLogger.log(TAG, "SERVER: peer $address connected to OUR server", DebugLevel.INFO)
                DebugLogger.updatePeerState(address) { copy(direction = if (direction == "CLIENT") "BOTH" else "SERVER", connectionStage = "SERVER_CONNECTED") }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                serverConnectedDevices.remove(address)
                notifyEnabledPeers.remove(address)
                DebugLogger.log(TAG, "SERVER: peer $address disconnected from OUR server", DebugLevel.WARN)
                DebugLogger.updatePeerState(address) { copy(connectionStage = "SERVER_DISCONNECTED", notifyEnabled = false) }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
            }
            val address = device?.address ?: "unknown"
            value?.let { bytes ->
                DebugLogger.log(TAG, "📩 WRITE received from $address: ${bytes.size} bytes", DebugLevel.DEBUG)
                DebugLogger.updatePeerState(address) { copy(packetsReceived = packetsReceived + 1) }
                device?.let { discoveredDevices[address] = it }
                handleIncomingBytes(bytes, address)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
            }
            val address = device?.address ?: return
            if (descriptor?.uuid == CCCD_UUID && value != null) {
                val enabled = value.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                notifyEnabledPeers[address] = enabled
                DebugLogger.log(TAG, "CCCD from $address: notifications ${if (enabled) "ENABLED ✅" else "DISABLED"}", DebugLevel.INFO)
                DebugLogger.updatePeerState(address) { copy(notifyEnabled = enabled) }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REASSEMBLY & INCOMING PACKET DISPATCH
    // ─────────────────────────────────────────────────────────────────────────

    private fun handleIncomingBytes(bytes: ByteArray, sourceAddress: String) {
        val frame = BleFrame.fromBytes(bytes)
        if (frame == null) {
            try {
                val packet = MeshPacket.fromJson(String(bytes, Charsets.UTF_8))
                packet?.let { onPacketReceived(it, sourceAddress) }
            } catch (_: Exception) {}
            return
        }

        pruneReassemblySessions()
        val session = reassemblySessions.computeIfAbsent(frame.transmissionId) {
            if (reassemblySessions.size >= MAX_REASSEMBLY_SESSIONS) {
                reassemblySessions.minByOrNull { it.value.lastUpdate }
                    ?.let { reassemblySessions.remove(it.key) }
            }
            ReassemblySession(frame.transmissionId)
        }

        if (session.addChunk(frame)) {
            reassemblySessions.remove(frame.transmissionId)
            val fullData = session.getFullData()
            if (fullData != null) {
                try {
                    val packet = MeshPacket.fromJson(String(fullData, Charsets.UTF_8))
                    if (packet != null) {
                        onPacketReceived(packet, sourceAddress)
                    }
                } catch (e: Exception) {
                    DebugLogger.log(TAG, "Reassembly parse error: ${e.message}", DebugLevel.ERROR)
                }
            }
        }
    }

    private fun onPacketReceived(packet: MeshPacket, sourceAddress: String) {
        DebugLogger.log(TAG, "📬 Packet reassembled: type=${packet.type} from=${packet.senderId}", DebugLevel.INFO)

        // Dynamic routing update: map sender's logical NodeID to the fresh BluetoothDevice handle
        val device = discoveredDevices[sourceAddress] ?: serverConnectedDevices[sourceAddress]
        if (device != null) {
            nodeToDeviceMap[packet.senderId] = device
            addressToNodeMap[sourceAddress] = packet.senderId
            discoveredDevices[packet.senderId] = device
        }

        // Handle PEER_DISCOVERY announcement
        if (packet.type == PacketType.PEER_DISCOVERY) {
            val peerAlias = if (packet.payload.isNotBlank()) packet.payload else "Peer ${packet.senderId.takeLast(4)}"
            val peerNode = MeshNode(
                nodeId = packet.senderId,
                alias = peerAlias,
                rssi = -30,
                transportType = MeshNode.TRANSPORT_BLE,
                lastSeenTimestamp = System.currentTimeMillis(),
                hopDistance = 1,
                isDirectNeighbor = true
            )
            scope.launch { _peerDiscoveryFlow.emit(peerNode) }
        }

        scope.launch { _packetFlow.emit(packet) }
    }

    private fun pruneReassemblySessions() {
        val now = System.currentTimeMillis()
        reassemblySessions.entries.removeIf { now - it.value.lastUpdate > REASSEMBLY_TIMEOUT_MS }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADVERTISING & SCANNING
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    private fun startAdvertising() {
        try {
            bleAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser ?: run {
                DebugLogger.log(TAG, "BLE advertiser not available", DebugLevel.WARN)
                return
            }
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build()

            // Include 8-character NodeID suffix in ServiceData (fits in standard 31-byte legacy PDU)
            val nodeSuffixBytes = localNodeId.removePrefix("NODE-").take(8).toByteArray(Charsets.UTF_8)
            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(ParcelUuid(SERVICE_UUID))
                .addServiceData(ParcelUuid(SERVICE_UUID), nodeSuffixBytes)
                .build()

            bleAdvertiser?.startAdvertising(settings, data, advertiseCallback)
        } catch (e: Exception) {
            DebugLogger.log(TAG, "Error starting BLE advertising: ${e.message}", DebugLevel.ERROR)
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopAdvertising() {
        try { bleAdvertiser?.stopAdvertising(advertiseCallback) } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        if (isScanning) return
        try {
            bleScanner = bluetoothAdapter?.bluetoothLeScanner ?: return
            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SERVICE_UUID))
                .build()
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build()
            bleScanner?.startScan(listOf(filter), settings, scanCallback)
            isScanning = true
            DebugLogger.log(TAG, "BLE scan started — filtering for SERVICE_UUID $SERVICE_UUID", DebugLevel.INFO)
        } catch (e: Exception) {
            DebugLogger.log(TAG, "Error starting BLE scan: ${e.message}", DebugLevel.ERROR)
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScanning() {
        if (!isScanning) return
        try {
            bleScanner?.stopScan(scanCallback)
            isScanning = false
        } catch (_: Exception) {}
    }

    private fun pauseScanningForConnect() {
        if (isScanning) {
            stopScanning()
        }
    }

    private fun resumeScanningAfterConnect() {
        if (isRunning && !isScanning) {
            startScanning()
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            DebugLogger.log(TAG, "BLE advertising started — Node $localNodeId", DebugLevel.INFO)
        }
        override fun onStartFailure(errorCode: Int) {
            DebugLogger.log(TAG, "BLE advertising failed: code=$errorCode", DebugLevel.ERROR)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { scanResult ->
                try {
                    val device = scanResult.device
                    val rssi = scanResult.rssi
                    val deviceName = try { device.name } catch (_: SecurityException) { null } ?: "Peer"
                    val deviceAddress = try { device.address } catch (_: SecurityException) { null } ?: return

                    // Extract advertised NodeID from ServiceData
                    val sData = scanResult.scanRecord?.getServiceData(ParcelUuid(SERVICE_UUID))
                    val nodeSuffix = if (sData != null && sData.isNotEmpty()) String(sData, Charsets.UTF_8) else ""
                    val logicalNodeId = if (nodeSuffix.isNotEmpty()) "NODE-$nodeSuffix" else deviceAddress

                    // Always update mapping to the freshest BluetoothDevice handle (RPA rotation fix)
                    nodeToDeviceMap[logicalNodeId] = device
                    addressToNodeMap[deviceAddress] = logicalNodeId
                    discoveredDevices[deviceAddress] = device
                    discoveredDevices[logicalNodeId] = device

                    val alias = if (deviceName.isNotBlank() && deviceName != "Peer")
                        deviceName else "Peer ${logicalNodeId.takeLast(4)}"

                    DebugLogger.log(TAG, "🔭 BLE peer found: $logicalNodeId ($alias) RSSI=$rssi", DebugLevel.DEBUG)

                    val peerNode = MeshNode(
                        nodeId = logicalNodeId,
                        alias = alias,
                        rssi = rssi,
                        transportType = MeshNode.TRANSPORT_BLE,
                        lastSeenTimestamp = System.currentTimeMillis(),
                        hopDistance = 1,
                        isDirectNeighbor = true
                    )
                    scope.launch { _peerDiscoveryFlow.emit(peerNode) }
                } catch (e: Exception) {
                    DebugLogger.log(TAG, "Scan result error: ${e.message}", DebugLevel.ERROR)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            DebugLogger.log(TAG, "BLE scan failed: code=$errorCode", DebugLevel.ERROR)
            isScanning = false
        }
    }
}
