package com.example.feasibility_study_development_artefact

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //check permissions xdxd
        checkPermissions()

        //get password and name from editext
        var WiFi_name = findViewById(R.id.WiFi_network) as EditText
        var WiFi_password = findViewById(R.id.WiFi_passowrd) as EditText


        //val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager

        //when connect button is pressed
        findViewById<Button>(R.id.ConnectButton).setOnClickListener {

            //save name and password in this class
            val suggestWifi = WifiNetworkSuggestion.Builder()
                .setSsid(WiFi_name.text.toString())
                .setWpa2Passphrase(WiFi_password.text.toString())
                .build()

            //create wifi manager
            //val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager

            //add wifi to suggestion list
            //var suggestionsList = ArrayList<WifiNetworkSuggestion>()
            //suggestionsList.add(suggestWifi)

            //suggest that android connects to this wifi, yes SUGGEST kernel will decide where to connect, it has to be done this way as previous api was removed in android 10
            //wifiManager.addNetworkSuggestions(suggestionsList);



            val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val wifiScanReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    Log.d("test", success.toString())
                    if (success) {
                        scanSuccess()
                        val results = wifiManager.scanResults
                        Log.d("test", results.toString())
                    } else {
                        scanFailure()
                    }
                }
            }

            Log.d("test", "3")
            val intentFilter = IntentFilter()
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            this.registerReceiver(wifiScanReceiver, intentFilter)

            val success = wifiManager.startScan()
            if (!success) {
                // scan failure handling
                scanFailure()
            }

            //WifiManager.startScan()





            //get current battery level using battery manager and a sticky intent
            //val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
           // val batteryStatus = registerReceiver(null, ifilter)
           // val level = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
           // val scale = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
           // val batteryPct = level / scale.toFloat()
           // val x = (batteryPct * 100).toInt()
           // Toast.makeText(this, x.toString(), Toast.LENGTH_SHORT).show()
        }
    }







    private fun scanSuccess() {
      //  val results = wifiManager.scanResults
        Log.d("test", "1")

    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
       // val results = wifiManager.scanResults
        Log.d("test", "2")

    }







    private fun checkPermissions(){//checks permissions
        val checkPermission1 = checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        val checkPermission2 = checkSelfPermission(Manifest.permission.INTERNET) //the internet permission technically does not need to be checked here but I still do it to be safe.
        val checkPermission3 = checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE)
        val checkPermission4 = checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE)
        val checkPermission5 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val checkPermission6 = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val needToAsk =
            !((checkPermission1 == PackageManager.PERMISSION_GRANTED) and (checkPermission2 == PackageManager.PERMISSION_GRANTED) and (checkPermission3 == PackageManager.PERMISSION_GRANTED) and (checkPermission4 == PackageManager.PERMISSION_GRANTED)and (checkPermission5 == PackageManager.PERMISSION_GRANTED)and (checkPermission6 == PackageManager.PERMISSION_GRANTED))
        if (needToAsk) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 0
            )
        }

    }

}
