package com.example.zerogrid.mesh.transport

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
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
import android.os.ParcelUuid
import android.util.Log
import com.example.zerogrid.mesh.engine.MeshNode
import com.example.zerogrid.mesh.engine.MeshPacket
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
 * Bluetooth Low Energy (BLE) Mesh Driver for ZeroGrid.
 * Manages BLE Advertising, Scanning, GATT Server, and GATT Client transmission.
 */
class BleMeshDriver(
    private val context: Context,
    localNodeId: String,
) : MeshTransport {

    init {
        // Use localNodeId to avoid "unused parameter" warning
        Log.d(TAG, "Initializing BleMeshDriver for $localNodeId")
    }

    companion object {
        private const val TAG = "BleMeshDriver"
        val SERVICE_UUID: UUID = UUID.fromString("0000a701-0000-1000-8000-00805f9b34fb")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("0000a702-0000-1000-8000-00805f9b34fb")

        private const val REASSEMBLY_TIMEOUT_MS = 30_000L
        private const val MAX_REASSEMBLY_SESSIONS = 10
        private const val MAX_PACKET_SIZE = 65536
    }

    private data class BleFrame(
        val transmissionId: UUID,
        val sequence: Int,
        val flags: Byte,
        val payload: ByteArray,
    ) {
        companion object {
            const val HEADER_SIZE = 16 + 4 + 1
            const val FLAG_SINGLE = 0x00.toByte()
            const val FLAG_START = 0x01.toByte()
            const val FLAG_MIDDLE = 0x02.toByte()
            const val FLAG_END = 0x03.toByte()

            fun fromBytes(bytes: ByteArray): BleFrame? {
                if (bytes.size < HEADER_SIZE) return null
                return try {
                    val buffer = ByteBuffer.wrap(bytes)
                    val mostSig = buffer.long
                    val leastSig = buffer.long
                    val seq = buffer.int
                    val flag = buffer.get()
                    val payload = ByteArray(size = buffer.remaining())
                    buffer.get(payload)
                    BleFrame(UUID(mostSig, leastSig), seq, flag, payload)
                } catch (_: Exception) {
                    null
                }
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
            if (transmissionId != other.transmissionId) return false
            if (sequence != other.sequence) return false
            if (flags != other.flags) return false
            return payload.contentEquals(other.payload)
        }

        override fun hashCode(): Int {
            var result = transmissionId.hashCode()
            result = (31 * result) + sequence
            result = (31 * result) + flags.toInt()
            result = (31 * result) + payload.contentHashCode()
            return result
        }
    }

    private class ReassemblySession(transmissionId: UUID) {
        val chunks = ConcurrentHashMap<Int, ByteArray>()
        var lastUpdate = System.currentTimeMillis()
        var isComplete = false
        var expectedChunks = -1

        init {
            Log.d(TAG, "Started reassembly session for $transmissionId")
        }

        fun addChunk(frame: BleFrame): Boolean {
            lastUpdate = System.currentTimeMillis()
            chunks[frame.sequence] = frame.payload
            if ((frame.flags == BleFrame.FLAG_END) || (frame.flags == BleFrame.FLAG_SINGLE)) {
                expectedChunks = frame.sequence + 1
            }
            if ((expectedChunks != -1) && (chunks.size == expectedChunks)) {
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

    override val transportName: String = MeshNode.TRANSPORT_BLE
    override var isRunning: Boolean = false
        private set

    private val _packetFlow = MutableSharedFlow<MeshPacket>(extraBufferCapacity = 64)
    override val packetFlow: SharedFlow<MeshPacket> = _packetFlow.asSharedFlow()

    private val _peerDiscoveryFlow = MutableSharedFlow<MeshNode>(extraBufferCapacity = 64)
    override val peerDiscoveryFlow: SharedFlow<MeshNode> = _peerDiscoveryFlow.asSharedFlow()

    private val discoveredDevices = ConcurrentHashMap<String, BluetoothDevice>()
    private val activeGatts = ConcurrentHashMap<String, BluetoothGatt>()
    private val peerMtus = ConcurrentHashMap<String, Int>()
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

    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        try {
            val adapter = bluetoothAdapter
            if (adapter == null || !adapter.isEnabled) {
                Log.w(TAG, "Bluetooth adapter not available or disabled")
                return
            }

            isRunning = true
            setupGattServer()
            startAdvertising()
            startScanning()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting BLE discovery", e)
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopDiscovery() {
        try {
            isRunning = false
            stopAdvertising()
            stopScanning()
            gattServer?.close()
            gattServer = null
            
            activeGatts.values.forEach { it.close() }
            activeGatts.clear()
            peerMtus.clear()
            discoveredDevices.clear()
            reassemblySessions.clear()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping BLE discovery", e)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val address = gatt.device.address
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to GATT: $address, discovering services...")
                    activeGatts[address] = gatt
                    gatt.discoverServices()
                    gatt.requestMtu(512)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "Disconnected from GATT: $address")
                    activeGatts.remove(address)
                    peerMtus.remove(address)
                    gatt.close()
                }
            } else {
                Log.e(TAG, "GATT error for $address: status $status")
                activeGatts.remove(address)
                peerMtus.remove(address)
                gatt.close()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "MTU changed for ${gatt.device.address}: $mtu")
                peerMtus[gatt.device.address] = mtu
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered for ${gatt.device.address}")
            }
        }
        
        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Characteristic write failed for ${gatt.device.address}: status $status")
            }
            writeMutex.unlock()
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendPacket(packet: MeshPacket, targetPeerId: String?): Boolean {
        if (!isRunning || bluetoothAdapter == null) return false

        scope.launch {
            val payloadBytes = packet.toByteArray()
            val transmissionId = UUID.randomUUID()
            
            val targets = if (targetPeerId != null) {
                listOfNotNull(discoveredDevices[targetPeerId])
            } else {
                discoveredDevices.values.toList()
            }

            if (targets.isEmpty()) {
                Log.w(TAG, "No BLE peers available for transmission")
                return@launch
            }

            targets.forEach { device ->
                transmitToDevice(device, transmissionId, payloadBytes)
            }
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private suspend fun transmitToDevice(device: BluetoothDevice, transmissionId: UUID, data: ByteArray) {
        val address = device.address
        var gatt = activeGatts[address]
        
        if (gatt == null) {
            Log.d(TAG, "Establishing new GATT connection to $address")
            device.connectGatt(context, false, gattCallback)
            // Wait for connection and MTU
            var retries = 0
            while (activeGatts[address] == null && retries < 50) {
                delay(100.milliseconds)
                retries++
            }
            gatt = activeGatts[address] ?: return
        }

        val mtu = peerMtus[address] ?: 23
        val maxPayload = mtu - 3 - BleFrame.HEADER_SIZE
        if (maxPayload <= 0) {
            Log.e(TAG, "Negotiated MTU ($mtu) too small for ZeroGrid framing")
            return
        }

        val service = gatt.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
        if (characteristic == null) {
            Log.e(TAG, "ZeroGrid characteristic not found on $address")
            return
        }

        var offset = 0
        var sequence = 0
        val totalFrames = (data.size + maxPayload - 1) / maxPayload

        while (offset < data.size) {
            val chunkSize = minOf(maxPayload, data.size - offset)
            val chunk = data.copyOfRange(offset, offset + chunkSize)
            
            val flags = when {
                totalFrames == 1 -> BleFrame.FLAG_SINGLE
                offset == 0 -> BleFrame.FLAG_START
                offset + chunkSize == data.size -> BleFrame.FLAG_END
                else -> BleFrame.FLAG_MIDDLE
            }
            
            val frame = BleFrame(transmissionId, sequence++, flags, chunk)
            val frameBytes = frame.toBytes()
            
            writeMutex.lock() // Lock until onCharacteristicWrite is called
            try {
                @Suppress("DEPRECATION")
                characteristic.value = frameBytes
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                @Suppress("DEPRECATION")
                if (!gatt.writeCharacteristic(characteristic)) {
                    Log.e(TAG, "Failed to initiate characteristic write to $address")
                    writeMutex.unlock()
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during GATT write to $address", e)
                writeMutex.unlock()
                return
            }
            offset += chunkSize
        }
        Log.d(TAG, "Successfully sent packet to $address via BLE")
    }

    @SuppressLint("MissingPermission")
    private fun setupGattServer() {
        try {
            gattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
            val service = BluetoothGattService(
                SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
            )
            val characteristic = BluetoothGattCharacteristic(
                CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ
            )
            service.addCharacteristic(characteristic)
            gattServer?.addService(service)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup GATT server", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertising() {
        try {
            bleAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
            if (bleAdvertiser == null) return

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(ParcelUuid(SERVICE_UUID))
                .build()

            bleAdvertiser?.startAdvertising(settings, data, advertiseCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting BLE advertising", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopAdvertising() {
        try {
            bleAdvertiser?.stopAdvertising(advertiseCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping BLE advertising", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        try {
            bleScanner = bluetoothAdapter?.bluetoothLeScanner
            if (bleScanner == null) return

            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SERVICE_UUID))
                .build()

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build()

            bleScanner?.startScan(listOf(filter), settings, scanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting BLE scanning", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScanning() {
        try {
            bleScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping BLE scan", e)
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "BLE Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "BLE Advertising failed with code: $errorCode")
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { scanResult ->
                try {
                    val device = scanResult.device
                    val rssi = scanResult.rssi
                    val deviceName = try { device.name } catch (_: SecurityException) { null } ?: "Peer"
                    val deviceAddress = try { device.address } catch (_: SecurityException) { null } ?: "00:00:00:00:00:00"

                    discoveredDevices[deviceAddress] = device

                    val peerNode = MeshNode(
                        nodeId = deviceAddress,
                        alias = if (deviceName.isNotBlank() && deviceName != "Peer") deviceName else "Peer ${deviceAddress.takeLast(4)}",
                        rssi = rssi,
                        transportType = MeshNode.TRANSPORT_BLE,
                        lastSeenTimestamp = System.currentTimeMillis(),
                        hopDistance = 1,
                        isDirectNeighbor = true
                    )
                    scope.launch {
                        _peerDiscoveryFlow.emit(peerNode)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing scan result", e)
                }
            }
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
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
            super.onCharacteristicWriteRequest(
                device, requestId, characteristic, preparedWrite, responseNeeded, offset, value
            )
            
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
            }

            value?.let { bytes ->
                handleIncomingBytes(bytes)
            }
        }
    }

    private fun handleIncomingBytes(bytes: ByteArray) {
        val frame = BleFrame.fromBytes(bytes)
        if (frame == null) {
            // Fallback for non-framed packets if any
            try {
                val jsonStr = String(bytes, Charsets.UTF_8)
                val packet = MeshPacket.fromJson(jsonStr)
                packet?.let { scope.launch { _packetFlow.emit(it) } }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling direct GATT write", e)
            }
            return
        }

        pruneReassemblySessions()
        val session = reassemblySessions.computeIfAbsent(frame.transmissionId) {
            if (reassemblySessions.size >= MAX_REASSEMBLY_SESSIONS) {
                val oldest = reassemblySessions.minByOrNull { it.value.lastUpdate }
                oldest?.let { reassemblySessions.remove(it.key) }
            }
            ReassemblySession(frame.transmissionId)
        }

        if (session.addChunk(frame)) {
            reassemblySessions.remove(frame.transmissionId)
            val fullData = session.getFullData()
            if (fullData != null) {
                try {
                    val jsonStr = String(fullData, Charsets.UTF_8)
                    val packet = MeshPacket.fromJson(jsonStr)
                    packet?.let { scope.launch { _packetFlow.emit(it) } }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reassembling MeshPacket", e)
                }
            }
        }
    }

    private fun pruneReassemblySessions() {
        val now = System.currentTimeMillis()
        reassemblySessions.entries.removeIf { now - it.value.lastUpdate > REASSEMBLY_TIMEOUT_MS }
    }
}
