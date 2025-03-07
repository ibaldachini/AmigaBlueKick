package com.example.amigabluekick

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var bleManager: BLEManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // (1) Controlla/richiedi i permessi di BLUETOOTH_SCAN, BLUETOOTH_CONNECT, LOCATION, ecc.
        requestBlePermissionsIfNeeded()

        // Inizializza BLEManager
        bleManager = BLEManager(this)

        findViewById<Button>(R.id.btnkick1).setOnClickListener() {
            bleManager.send("0")
        }

        findViewById<Button>(R.id.btnkick2).setOnClickListener() {
            bleManager.send("1")
        }

        findViewById<Button>(R.id.btnkick3).setOnClickListener() {
            bleManager.send("2")
        }

        findViewById<Button>(R.id.btnkick4).setOnClickListener() {
            bleManager.send("3")
        }

        // Quando vuoi inviare un messaggio...
        bleManager.listener = {
            this.runOnUiThread {
                findViewById<LinearLayout>(R.id.waitlayout).visibility = View.GONE;
                findViewById<LinearLayout>(R.id.connectedlayout).visibility = View.VISIBLE;
            }
        }

        // (2) Avvia la scansione
        bleManager.startScan()
    }

    private fun requestBlePermissionsIfNeeded() {
        // Esempio di richiesta permessi semplificata, dipende dalle versioni di Android
        val neededPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            neededPermissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            neededPermissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // Sotto Android 12, spesso bastano i permessi di localizzazione
            neededPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // PERMISSION GRANTED
            } else {
                // PERMISSION NOT GRANTED
            }
        }

        ActivityCompat.requestPermissions(this, neededPermissions.toTypedArray(), 1010)
    }
}