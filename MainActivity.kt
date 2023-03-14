package com.example.feasibility_study_development_artefact

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.AWSDataStorePlugin
import com.amplifyframework.datastore.generated.model.Todo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.util.*


var stopClicked = false
private lateinit var fusedLocationClient: FusedLocationProviderClient
var ogaboga = "abc"
var recentlyTried: Queue<String> = LinkedList<String>()

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //check and/or ask for permissions
        checkPermissions()


        //AWS Amplify datastore setup
        //the datastore function is linked with dynamo db
        //thanks to Faraday Solutions for allowing me to use their AWS credit
        //remember to set min sdk to 32 otherwise gradle will die
        try {
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSDataStorePlugin())
            Amplify.configure(applicationContext)
            Log.i("Amplify", "Initialized Amplify")
        } catch (e: AmplifyException) {
            Log.e("Amplify", "Could not initialize Amplify", e)
        }








        //create some global values and managers
        //val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager

        //wait at least 10 seconds
        //remember to disable throttling in Developer Options > Networking > Wi-Fi scan throttling (normally only allows 4 scans per 2 minutes)
        var TimeNow = System.currentTimeMillis()
        var TimeFuture = TimeNow + 1

//put it into a corutine cause cannot delay main function



        //scanWifi()


        findViewById<Button>(R.id.amplifyButton).setOnClickListener {

            amplifyCreate()
            amplifyReadAll()
        }




        findViewById<Button>(R.id.buttonStop).setOnClickListener {

            stopClicked = true

        }

        findViewById<Button>(R.id.resetButton).setOnClickListener {

            recentlyTried.clear()

        }



        //get password and name from editext
        //var WiFi_name = findViewById(R.id.WiFi_network) as EditText
        //var WiFi_password = findViewById(R.id.WiFi_passowrd) as EditText

        Log.d("test1", "test1")
        //val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager

        //when connect button is pressed
        findViewById<Button>(R.id.ConnectButton).setOnClickListener {
            Log.d("test1", "test")
            //isDeviceOnline(this)
            stopClicked = false

            GlobalScope.launch  {main_coroutine()}

            //save name and password in this class
          //  val suggestWifi = WifiNetworkSuggestion.Builder()
           //     .setSsid(WiFi_name.text.toString())
           //     .setWpa2Passphrase(WiFi_password.text.toString())
           //     .build()

            //create wifi manager
            //val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager

            //add wifi to suggestion list
            //var suggestionsList = ArrayList<WifiNetworkSuggestion>()
            //suggestionsList.add(suggestWifi)

            //suggest that android connects to this wifi, yes SUGGEST kernel will decide where to connect, it has to be done this way as previous api was removed in android 10
            //wifiManager.addNetworkSuggestions(suggestionsList);



          //  val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager

          //  val wifiScanReceiver = object : BroadcastReceiver() {
           //     override fun onReceive(context: Context, intent: Intent) {
           //         val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
           //         Log.d("test", success.toString())
           //         if (success) {
           //             scanSuccess()
           //             val results = wifiManager.scanResults
            //            Log.d("test", results.toString())
           //         } else {
           //             scanFailure()
           //         }
           //     }
          //  }

           // Log.d("test", "3")
          //  val intentFilter = IntentFilter()
           // intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
          //  this.registerReceiver(wifiScanReceiver, intentFilter)

          //  val success = wifiManager.startScan()
          //  if (!success) {
                // scan failure handling
          //      scanFailure()
         //   }

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


    /////////////////////////////////amplify datastore functions
    private fun amplifyCreate(){//this function saves to-do data model locally and syncs it with dynamodb when possible
        val item: Todo = Todo.builder()//todo if possible add aws lambda  function to also record time at which the data was synced with aws
            .name("testor")
            .description("Lorem ipsum dolor sit amet")
            .build()
        Amplify.DataStore.save(
            item,
            { success -> Log.i("Amplify", "Saved item: " + success.item().name) },
            { error -> Log.e("Amplify", "Could not save item to DataStore", error) }
        )
    }

    private fun amplifyReadAll(){
        Amplify.DataStore.query(
            Todo::class.java,
            { items ->
                while (items.hasNext()) {
                    val item = items.next()
                    Log.i("Amplify", "Queried item: " + item.id + item.name)
                }
            },
            { failure -> Log.e("Tutorial", "Could not query DataStore", failure) }
        )
    }
    /////////////////////////////////no moreamplify datastore functions


    private fun isDeviceOnline(context: Context): Boolean {
        val connManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connManager.getNetworkCapabilities(connManager.activeNetwork)
            if (networkCapabilities == null) {
                Log.d("test1", "Device Offline")
                return false
            } else {
                Log.d("test1", "Device Online")
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
                ) {
                    Log.d("test1", "Connected to Internet")
                    return true
                } else {
                    Log.d("test1", "Not connected to Internet")
                    return false
                }
            }
        }
     else {
        Log.d("test1", "Device Offline")
        return false
    }

}




    private fun scanWifi(): MutableList<ScanResult>? { //scan for all detectable access points
        //creates and intent to scan wifi and a receiver to get the scan results
        //works only on new versions of android, on android oreo it just returns empty list
        val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
        Log.d("test1", "Searching for hotspots")
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        this.registerReceiver(wifiScanReceiver, intentFilter)
        val success = wifiManager.scanResults
        Log.d("test1", success.toString())
        //if empty = fail or your in a desert
        return success
    }


    suspend fun main_coroutine() {//run this function as a coroutine so that delay can be used

        val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var TimeNow = System.currentTimeMillis()
        var TimeFuture = TimeNow + 10000
        var ssids: Queue<String> = LinkedList<String>()
        var scanResults: MutableList<ScanResult>?
        var suggestionsList = ArrayList<WifiNetworkSuggestion>()
        //var recentlyTried: Queue<String> = LinkedList<String>()
        ogaboga = "error"
        wifiManager.removeNetworkSuggestions(suggestionsList)

        while (!stopClicked){

            TimeNow = System.currentTimeMillis()
            TimeFuture = TimeNow + 10000
            ssids.clear()
            suggestionsList.clear()

            //TimeNow = System.currentTimeMillis()//update current time //remember to update future timer later

            //search for a suitable hotspot
            scanResults = scanWifi()
            //var scanResults = scanWifi()

           // Log.d("test1", "test = ${scanResults?.get(0)}")
           // val ssids = scanResults?.map { it.SSID }
            //Log.d("test1", ssids?.get(0).toString())
            //val ssids: Queue<String> = LinkedList<String>()

            if (scanResults != null) {//need this check because android studio cries about it
                for (result in scanResults) {
                    val capabilities = result.capabilities
                    val ssid = result.SSID

                    //val ssids: Queue<String> = LinkedList<String>()

                    val isPasswordProtected = capabilities.contains("WPA") || capabilities.contains("WEP") //if capabilities contain word wpa or wpe then the wifi is password protected
                    //Log.d("test1", ssid.toString() + " " + isPasswordProtected.toString())
                    if (capabilities.contains("WPA") || capabilities.contains("WEP")) {


                        Log.d("test1", ssid.toString() + " is password protected")
                    }
                    else {
                        Log.d("test1", ssid.toString() + " is not password protected")
                        ssids.add(ssid)
                    }
                    }


                if (ssids != null) {
                    Log.d("test1", "found " + ssids.size + " paswordless hotspots")


                   // val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    //var suggestionsList = ArrayList<WifiNetworkSuggestion>()

                    run breaking@ {
                    ssids?.forEach {//a fancy loop for queues
                            x ->

                        if (recentlyTried.size == 0){
                            recentlyTried.add(x)
                            Log.d("test1", "connecting to " + x.toString())
                            wifiManager.addNetworkSuggestions(suggestionsList)
                            val suggestWifi = WifiNetworkSuggestion.Builder()
                                .setSsid(x)
                                .build()
                            ogaboga = x.toString()
                            suggestionsList.add(suggestWifi)
                            wifiManager.addNetworkSuggestions(suggestionsList)
                            return@breaking
                        }
                        else {


                        recentlyTried.forEach { y ->
                            //Log.d("test1", "y- " + y.toString())
                            //Log.d("test1", "x- " + x.toString())
                            if (x == y) {
                                //do nothing
                                Log.d("test1", x.toString() + "was recently tried, next")

                            }
                            else {
                            //add to the list
                            //if the list is more than 3 pop 1
                            //try
                                recentlyTried.add(x)
                                if (recentlyTried.size > 3){
                                    recentlyTried.remove()
                                }


                                Log.d("test1", "connecting to " + x.toString())
                            val suggestWifi = WifiNetworkSuggestion.Builder()
                                .setSsid(x)
                                .build()
                            ogaboga = x.toString()
                            suggestionsList.add(suggestWifi)
                                wifiManager.addNetworkSuggestions(suggestionsList)
                            //ogaboga = x.toString()
                            return@breaking
                            }

                        }
                        }
                    }


                       // val suggestWifi = WifiNetworkSuggestion.Builder()
                          //  .setSsid(x)
                         //   .build()
                      //  suggestionsList.add(suggestWifi)
                    }

                    ////////////////////////////
                    //wifiManager.addNetworkSuggestions(suggestionsList);

                }
                //Log.d("test1", "All hotspots are password protected")



                }
                //Log.d("test1", "no hotspots detected")


            Log.d("test1", "123")
            if (suggestionsList.size > 0){
                Log.d("test1", "waiting")


                //problem ahead
                //basically sometimes android insists on using a certain access point
                //for example at lidl near dundee uni, i know i can log in into lidls wifi but android
                //insists on using some wierd wifi that i cannot log into
                //i can either pick a random access point or have another list to follow ssid of access points
                //that do not work, i dont like both solutions
                //so save tried access point ssids in a small queue and cycle throught recently tried ssids//FIX THIS LATER




               // wifiManager.addNetworkSuggestions(suggestionsList)

                delay(18000L)//wait 8 seconds to allow wifi login application to work
                Log.d("test1", "stop waiting")
                var test1 = false
                test1 = isDeviceOnline(this)

                while (test1){//does not work ?? idk now
                    test1 = isDeviceOnline(this)
                    delay(1000L)

                }



                //check if there is network connection
                //if no then scan again

                //if yes then all is fine
                    //check again in 5 seconds


                }
                    //if some passwordless hotspots are found then use them, if not use mobile data
                    //in android when wifi and mobile data is available then it usually uses wifi
                    //so having mobile data active all the time works fine


                    //pick 3 paswordless wifis at random and add them to the list

                    //wait for it to connect and for wifi web login app to kick in

                    //check if there is a connection to the internet
                    //if no then scan again
                    //if yes then all is fine

            //save data in a csv file here
            //location ,time , battery %, try cpu temp, wifi name connected to, was the connection sucesfull?, list of other available wifis
            //another file for test app
            //another file to save how long the sucesfull conections lasted for

                    //when connection fails scan again


            Log.d("test1", "12345")
            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(folder, "test1234.csv")
            val stream = FileOutputStream(file, true)
            //var location1: Location
            var location1 = Location("dummyprovider")
            location1.setLatitude(20.3);
            location1.setLongitude(52.6);

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

          //  fusedLocationClient.lastLocation
              //  .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                   // Log.d("test1", "1234567")
                   // Log.d("test1", location.toString())

              //  }


            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

                override fun isCancellationRequested() = false
            })
                .addOnSuccessListener { location: Location? ->
                    if (location == null)
                        Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                    else {
                       // val lat = location.latitude
                       // val lon = location.longitude
                        Log.d("test1", location.toString())
                        location1 = location
                        //Log.d("test1", location.toString())
                    }

                }

            delay(4000L)





            //get current battery level using battery manager and a sticky intent
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = registerReceiver(null, ifilter)
            val level = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = level / scale.toFloat()
            val x = (batteryPct * 100).toInt()
            //Toast.makeText(this, x.toString(), Toast.LENGTH_SHORT).show()


                var tester45 = ""
                //tester45 = tester45.replace(",", "testor")

            scanResults?.forEach {
                    d ->

                var igabiga = d.toString().replace(",", ".")

                     tester45 = tester45 + igabiga + ", ," }

//if error its using mobile data, simple as

            var current = LocalDateTime.now()
            var newText = "\n " + current + "," + "\"" + location1.toString() + "\""  + ","  + x.toString() + "," + ogaboga + "," + tester45
            // Write the new text to the file
            stream.write(newText.toByteArray())


            // Close the stream
            stream.close()
            Log.d("test1", "123456")



                    while (TimeNow < TimeFuture) {
                        Log.d("test1", "oga")
                        TimeNow = System.currentTimeMillis()
                        delay(1000L)
                    }

                }

        Log.d("test1", "oga")
        TimeFuture = TimeNow + 1


        delay(1000L)

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
