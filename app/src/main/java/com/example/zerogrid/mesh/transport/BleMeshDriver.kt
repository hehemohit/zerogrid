package com.example.zerogrid.mesh.transport

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Bluetooth Low Energy (BLE) Mesh Driver for ZeroGrid.
 * Manages BLE Advertising, Scanning, GATT Server, and GATT Client transmission.
 */
class BleMeshDriver(
    private val context: Context,
    private val localNodeId: String
) : MeshTransport {

    companion object {
        private const val TAG = "BleMeshDriver"
        val SERVICE_UUID: UUID = UUID.fromString("0000ZG01-0000-1000-8000-00805F9B34FB")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("0000ZG02-0000-1000-8000-00805F9B34FB")
    }

    override val transportName: String = MeshNode.TRANSPORT_BLE
    override var isRunning: Boolean = false
        private set

    private val _packetFlow = MutableSharedFlow<MeshPacket>(extraBufferCapacity = 64)
    override val packetFlow: SharedFlow<MeshPacket> = _packetFlow.asSharedFlow()

    private val _peerDiscoveryFlow = MutableSharedFlow<MeshNode>(extraBufferCapacity = 64)
    override val peerDiscoveryFlow: SharedFlow<MeshNode> = _peerDiscoveryFlow.asSharedFlow()

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
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping BLE discovery", e)
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendPacket(packet: MeshPacket, targetPeerId: String?): Boolean {
        if (!isRunning || bluetoothAdapter == null) return false

        try {
            val payloadBytes = packet.toByteArray()
            Log.d(TAG, "Queueing BLE packet dispatch: ${packet.packetId} (${payloadBytes.size} bytes)")

            scope.launch {
                _packetFlow.emit(packet)
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending BLE packet", e)
            return false
        }
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
                    val deviceName = try { device.name } catch (e: SecurityException) { null } ?: "Peer"
                    val deviceAddress = try { device.address } catch (e: SecurityException) { null } ?: "00:00:00:00:00:00"

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
            value?.let { bytes ->
                try {
                    val jsonStr = String(bytes, Charsets.UTF_8)
                    val packet = MeshPacket.fromJson(jsonStr)
                    packet?.let {
                        scope.launch {
                            _packetFlow.emit(it)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling GATT write request", e)
                }
            }
        }
    }
}
