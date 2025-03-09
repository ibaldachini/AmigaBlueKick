package com.example.amigabluekick

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {
    private lateinit var bleManager: BLEManager
    private lateinit var userPreferencesManager: UserPreferencesManager

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

        userPreferencesManager = UserPreferencesManager(this)

        var prefs = userPreferencesManager.getPreferences()

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
        bleManager.listener = { actual ->
            this.runOnUiThread {
                if (actual == null) {
                    findViewById<LinearLayout>(R.id.waitlayout).visibility = View.VISIBLE;
                    findViewById<LinearLayout>(R.id.connectedlayout).visibility = View.GONE;
                } else {
                    findViewById<LinearLayout>(R.id.waitlayout).visibility = View.GONE;
                    findViewById<LinearLayout>(R.id.connectedlayout).visibility = View.VISIBLE;
                    findViewById<Button>(R.id.btnkick1).backgroundTintList = ColorStateList.valueOf(
                        Color.parseColor("#808080")
                    )
                    findViewById<Button>(R.id.btnkick1).setTextColor(Color.parseColor("#000000"))
                    findViewById<Button>(R.id.btnkick1).setText(prefs.btn1Text)
                    findViewById<Button>(R.id.btnkick2).backgroundTintList = ColorStateList.valueOf(
                        Color.parseColor("#808080")
                    )
                    findViewById<Button>(R.id.btnkick2).setTextColor(Color.parseColor("#000000"))
                    findViewById<Button>(R.id.btnkick2).setText(prefs.btn2Text)
                    findViewById<Button>(R.id.btnkick3).backgroundTintList = ColorStateList.valueOf(
                        Color.parseColor("#808080")
                    )
                    findViewById<Button>(R.id.btnkick3).setTextColor(Color.parseColor("#000000"))
                    findViewById<Button>(R.id.btnkick3).setText(prefs.btn3Text)
                    findViewById<Button>(R.id.btnkick4).backgroundTintList = ColorStateList.valueOf(
                        Color.parseColor("#808080")
                    )
                    findViewById<Button>(R.id.btnkick4).setTextColor(Color.parseColor("#000000"))
                    findViewById<Button>(R.id.btnkick4).setText(prefs.btn4Text)
                    if (actual == "0") {
                        findViewById<Button>(R.id.btnkick1).backgroundTintList =
                            ColorStateList.valueOf(
                                Color.parseColor("#0000FF")
                            )
                        findViewById<Button>(R.id.btnkick1).setTextColor(Color.parseColor("#FFFFFF"))
                    }
                    if (actual == "1") {
                        findViewById<Button>(R.id.btnkick2).backgroundTintList =
                            ColorStateList.valueOf(
                                Color.parseColor("#0000FF")
                            )
                        findViewById<Button>(R.id.btnkick2).setTextColor(Color.parseColor("#FFFFFF"))
                    }
                    if (actual == "2") {
                        findViewById<Button>(R.id.btnkick3).backgroundTintList =
                            ColorStateList.valueOf(
                                Color.parseColor("#0000FF")
                            )
                        findViewById<Button>(R.id.btnkick3).setTextColor(Color.parseColor("#FFFFFF"))
                    }
                    if (actual == "3") {
                        findViewById<Button>(R.id.btnkick4).backgroundTintList =
                            ColorStateList.valueOf(
                                Color.parseColor("#0000FF")
                            )
                        findViewById<Button>(R.id.btnkick4).setTextColor(Color.parseColor("#FFFFFF"))
                    }
                }
            }
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener() {
            val intent = Intent(
                this,
                UserPreferencesActivity::class.java
            )
            startActivity(intent)
        }

        // (2) Avvia la scansione
        bleManager.startScan()
    }

    override fun onResume() {
        super.onResume()
        var prefs = userPreferencesManager.getPreferences()
        findViewById<Button>(R.id.btnkick1).setText(prefs.btn1Text)
        findViewById<Button>(R.id.btnkick2).setText(prefs.btn2Text)
        findViewById<Button>(R.id.btnkick3).setText(prefs.btn3Text)
        findViewById<Button>(R.id.btnkick4).setText(prefs.btn4Text)
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