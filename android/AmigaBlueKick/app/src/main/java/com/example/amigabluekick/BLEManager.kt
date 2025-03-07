package com.example.amigabluekick

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.*

class BLEManager(private val context: Context) {

    var listener: (()->Unit)? = null

    // Nome del dispositivo BLE da cercare (uguale al tuo "sensorName" in iOS)
    private val deviceName = "AmigaKickstartControl"

    // UUID del servizio e della caratteristica (ricavati rispettivamente da "A500" e "1234")
    private val serviceUUID: UUID = UUID.fromString("0000A500-0000-1000-8000-00805f9b34fb")
    private val characteristicUUID: UUID = UUID.fromString("00001234-0000-1000-8000-00805f9b34fb")

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var scanning = false
    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()


    // Callback di GATT: simile a CBCentralManagerDelegate + CBPeripheralDelegate in iOS
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Dispositivo connesso, avvio discoverServices()")
                // Scopriamo i servizi sul dispositivo
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Dispositivo disconnesso")
                bluetoothGatt = null
                // Eventualmente riavvia la scansione, se vuoi riconnetterti automaticamente
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Servizi scoperti con successo")
                val service = gatt.getService(serviceUUID)
                if (service != null) {
                    // Trova la caratteristica
                    val characteristic = service.getCharacteristic(characteristicUUID)
                    if (characteristic != null) {
                        writeCharacteristic = characteristic
                        Log.d(TAG, "Caratteristica individuata: $characteristicUUID")
                        listener?.invoke()
                    } else {
                        Log.e(TAG, "Caratteristica non trovata!")
                    }
                } else {
                    Log.e(TAG, "Servizio non trovato!")
                }
            } else {
                Log.e(TAG, "onServicesDiscovered fallito con status $status")
            }
        }

        // Se vuoi leggere i dati inviati dal dispositivo, puoi gestire onCharacteristicChanged
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == characteristicUUID) {
                val data = characteristic.value
                val stringValue = data?.let { String(it) }
                Log.d(TAG, "Valore aggiornato dalla caratteristica: $stringValue")
            }
        }
    }

    init {
        // Inizializza BluetoothAdapter
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    }

    // Avvia la scansione BLE
    @SuppressLint("MissingPermission")
    fun startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.e(TAG, "Bluetooth disabilitato o non disponibile")
            return
        }

        // Controllo permessi runtime (Android 12 e successivi)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Permessi di scansione BLE non concessi!")
            // Gestisci richiesta permessi. Se necessario, fermati qui.
            return
        }

        if (!scanning) {
            scanning = true
            Log.d(TAG, "Inizio scansione per $deviceName")

            // Scansione base per dispositivi BLE
            bluetoothLeScanner?.startScan(null, scanSettings, leScanCallback)
/*
            // Facoltativo: fermare la scansione dopo un timeout (ad es. 10 secondi)
            Handler(Looper.getMainLooper()).postDelayed({
                stopScan()
            }, SCAN_PERIOD)
*/
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (scanning) {
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
            Log.d(TAG, "Scansione fermata")
        }
    }

    // Callback della scansione BLE, analogo a didDiscoverPeripheral su iOS
    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val name = result.scanRecord?.deviceName?: "Sconosciuto"
            if (name == deviceName) {
                Log.d(TAG, "Dispositivo trovato: $name, fermo la scansione e mi connetto")
                stopScan()
                connectToDevice(device)
            }
        }
    }

    // Connessione al dispositivo selezionato
    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Permessi di connessione BLE non concessi!")
            return
        }
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    // Invia un messaggio di testo al dispositivo BLE, analogo a send(message:) in Swift
    @SuppressLint("MissingPermission")
    fun send(message: String) {
        if (bluetoothGatt == null || writeCharacteristic == null) {
            Log.e(TAG, "Gatt o caratteristica non inizializzati, impossibile inviare dati")
            return
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Permessi di connessione BLE non concessi!")
            return
        }

        // Converte la stringa in byte e scrive nella caratteristica
        writeCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        writeCharacteristic?.value = message.toByteArray()
        val success = bluetoothGatt?.writeCharacteristic(writeCharacteristic)
        Log.d(TAG, "Tentativo di invio dati (success=$success)")
    }

    companion object {
        private const val TAG = "BLEManager"
        private const val SCAN_PERIOD = 10000L // 10 secondi di scansione
    }
}

