package com.example.feasibility_study_development_artefact

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.media.metrics.NetworkEvent.NETWORK_TYPE_UNKNOWN
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.NETWORK_TYPE_LTE
import android.telephony.TelephonyManager.NETWORK_TYPE_NR
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.temporal.Temporal
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

//some global variables
var stopClicked = false
private lateinit var fusedLocationClient: FusedLocationProviderClient
var recentlyTried: Queue<String> = LinkedList<String>()
var usedSSID = "abc"
var wifiScans = 0
var totalTries = 0
var networkAfterTry = false
var wasonline = false

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //check and/or ask for permissions
        checkPermissions()

        //this will keep screen from going blank, when i lock the device the coroutines die (threats below), the globalscope function was to fix this but it did not.
        //as im low on time then just keep the screen opened to fix this.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //AWS Amplify datastore setup
        //the datastore function is linked with dynamo db
        //remember to set min sdk to 32 otherwise gradle will die, also remember to pull the amplify backend to the project if not remove this part.
        try {
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSDataStorePlugin())
            Amplify.configure(applicationContext)
            Log.i("Amplify", "Initialized Amplify")
        } catch (e: AmplifyException) {
            Log.e("Amplify", "Could not initialize Amplify", e)
        }

        findViewById<Button>(R.id.amplifyButton).setOnClickListener {//amplify button, starts the experiment once the button is clicked
            GlobalScope.launch  {testTask()}
        }

        findViewById<Button>(R.id.amplifyResetButton).setOnClickListener {//this button 'removes' all data from amplify datastore, actually it adds a deleted tag to the data, remove data manually for the experiments
            amplifyDeleteAll()
        }

        findViewById<Button>(R.id.buttonStop).setOnClickListener {//stop button, stops the application and saves some more data
            stopClicked = true

            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)//creates a csv file in downloads directory
            val file = File(folder, "experiment_two_test_three_LTE_2.csv")
            val stream = FileOutputStream(file, true)
            var newText = "\n " + totalTries.toString() + "," + wifiScans.toString()
            stream.write(newText.toByteArray())//write to the file
            stream.close()
        }

        findViewById<Button>(R.id.resetButton).setOnClickListener {//debugging button, clears the recently tried queue. (only for debugging, remove this later)
            recentlyTried.clear()
        }

        findViewById<Button>(R.id.ConnectButton).setOnClickListener {//when connect button is pressed
            stopClicked = false
            //remember to disable throttling in Developer Options > Networking > Wi-Fi scan throttling (normally only allows 4 scans per 2 minutes)
            GlobalScope.launch  {main_coroutine()} //the main offloading function is put into a corutine to be able to use delay function, also it would just lag the gui threat
        }
    }

    /////////////////////////////////amplify datastore functions
    private fun amplifyCreate(name: String, description: String){//this function saves to-do data model locally and syncs it with dynamodb when possible
        val item: Todo = Todo.builder()
            .name(name)
            .description(description)
            .build()
        Amplify.DataStore.save(
            item,
            { success -> Log.i("Amplify", "Saved item: " + success.item().name) },
            { error -> Log.e("Amplify", "Could not save item to DataStore", error) }
        )
    }

    private fun amplifyReadAll(){//returns all items
        Amplify.DataStore.query(
            Todo::class.java,
            { items ->
                while (items.hasNext()) {
                    val item = items.next()
                    Log.i("Amplify", "Queried item: " + item.id + item.name)
                }
            },
            { failure -> Log.e("Amplify", "Could not query DataStore", failure) }
        )
    }

    private fun amplifyDeleteAll() {//deletes everything
        Amplify.DataStore.query(
            Todo::class.java,
            { items ->
                while (items.hasNext()) {
                    val item = items.next()

                    Amplify.DataStore.delete(item,
                        { deleted -> Log.i("Amplify", "Deleted item.") },
                        { failure -> Log.e("Amplify", "Delete failed.", failure) }
                    )
                }
            },
            { failure -> Log.e("Amplify", "Could not query DataStore", failure) }
        )
    }
    ///////////////////////////////////////////

    //this function tries to check if device has access to the internet
    //I tried to implement it by pinging a random server like bbc.co.uk but there is no ping command in kotlin and online implementations of ping are broken or just a spyware.
    fun isDeviceOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork
        if (networkCapabilities != null) {
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities)
            if (actNw != null) {//if those capabilities are found this most likely means that device has internet. This is stupid, fix later.
                if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (actNw.hasTransport(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    return true
                }
            }
        }
        return false
    }

    private fun scanWifi(): MutableList<ScanResult>? { //scan for all detectable access points
        //creates and intent to scan wifi and a receiver to get the scan results
        wifiScans += 1
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
        return success
    }

    suspend fun testTask() {//this is a task for the experiments, it saves a timestamp and string every 10 seconds
        while (!stopClicked) {
            var TimeNow = System.currentTimeMillis()
            amplifyCreate(TimeNow.toString(), "noTempCauseAndroidBad")// I wanted to save temperature reading but getting temp involves more intents, filters and receivers. Maybe fix later.
            Log.d("test1", "Saving data to datastore")
            delay(10000L)
        }
    }

    suspend fun main_coroutine() {//run this function as a coroutine so that delay can be used
        val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager //declare and reset a bunch of variables
        var TimeNow = System.currentTimeMillis()
        var TimeFuture = TimeNow
        var ssids: Queue<String> = LinkedList<String>()
        var scanResults: MutableList<ScanResult>?
        var suggestionsList = ArrayList<WifiNetworkSuggestion>()
        usedSSID = "error"
        wifiManager.removeNetworkSuggestions(suggestionsList)
        var networkTimeStart = LocalDateTime.of(2020, 1, 1, 1, 1, 1) //initial time, if you see this in csv output then something went wrong
        var networkTimeNetOk = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
        var networkTimeEnd = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
        var isOnline = false
        var whatconnectivity  = whatConnectivity(this)
        var whatwasconnectivity = whatConnectivity(this)

        while (!stopClicked){//keep looping until stop button is clicked
            try{
                TimeNow = System.currentTimeMillis()
                TimeFuture = TimeNow + 10000//this function is limited to loop max once every 10 seconds
                ssids.clear()
                suggestionsList.clear()
                networkAfterTry = false
                wasonline = false

                scanResults = scanWifi()  //search for a suitable hotspot
                if (scanResults != null) {//need this check because kotlin cries about it
                    for (result in scanResults) {//loop for each wifi discovered
                        val capabilities = result.capabilities
                        val ssid = result.SSID
                        val isPasswordProtected = capabilities.contains("WPA") || capabilities.contains("WEP") //if capabilities contain word wpa or wpe then the wifi is password protected
                        if (capabilities.contains("WPA") || capabilities.contains("WEP")) {
                            //Log.d("test1", ssid.toString() + " is password protected")
                        }
                        else {
                            Log.d("test1", ssid.toString() + " is not password protected")
                            ssids.add(ssid) //save all not password protected SSIDs in a list
                        }
                    }

                    if (ssids != null) {
                        Log.d("test1", "found " + ssids.size + " paswordless hotspots")
                        totalTries += 1

                        //so, basically sometimes android insists on using a certain access point for example at lidl near dundee uni, i know i can log in into lidls wifi but android
                        //insists on using some wierd wifi that i cannot log into, so save tried access point ssids in a small queue and cycle throught recently tried ssids
                        run breaking@ {//the run breaking is used to stop this loop good ssid is found and device connects to it
                            ssids?.forEach {
                                    x ->
                                if (recentlyTried.size == 0){//when recently tried list is empty aka the first scan
                                    recentlyTried.add(x)
                                    Log.d("test1", "connecting to " + x.toString())//the hotspots ssid is added to the recently tried list and app tries to connect to it
                                    //the list is put into this function, due to androids architecture its the only way to connect to a wifi
                                    //so the android have full control, it sometimes can just refuse to connect to this wifi
                                    val suggestWifi = WifiNetworkSuggestion.Builder().setSsid(x).build()
                                    usedSSID = x.toString()
                                    suggestionsList.add(suggestWifi)
                                    wifiManager.addNetworkSuggestions(suggestionsList)
                                    return@breaking

                                } else { //the recently tried list is not empty, so check if the ssid was tried recently
                                    recentlyTried.forEach { y ->//loop all recently tried ssids
                                        if (x == y) {//if recently tried wifi == wifi found
                                            //do nothing
                                            Log.d("test1", x.toString() + " was recently tried, next")
                                        } else {
                                            recentlyTried.add(x)//add to the list
                                            if (recentlyTried.size > 2){//if the list is more than 3 pop 1
                                                recentlyTried.remove()
                                            }
                                            Log.d("test1", "connecting to " + x.toString())
                                                val suggestWifi = WifiNetworkSuggestion.Builder().setSsid(x).build()
                                                usedSSID = x.toString()
                                                suggestionsList.add(suggestWifi)
                                                wifiManager.addNetworkSuggestions(suggestionsList)//try to connect to a wifi
                                            return@breaking
                                        }
                                    }
                                }
                            }
                        }
                    } else {Log.d("test1", "All hotspots are password protected")}
                } else {Log.d("test1", "no hotspots detected")}

                Log.d("test1", "waiting for wifi web login application")
                delay(8000L)//wait 8 seconds to allow wifi login application to work, why 8 ? from my manual experiments it takes around 6 seconds for that app to log in
                Log.d("test1", "done waiting")

                whatconnectivity  = whatConnectivity(this)//save what type of network is being used
                whatwasconnectivity = whatconnectivity
                isOnline = isDeviceOnline(this) //checks if device can connect to the internet, checks if the captive portal login app worked
                Log.d("test1", isOnline.toString())

                if (isOnline) {
                    Log.d("test1", "Success there is internet access")
                    networkTimeNetOk = LocalDateTime.now()
                    wasonline = true
                }

                while (isOnline && (whatconnectivity == "WIFI")){//loop until there is no longer internet access
                    networkAfterTry = true
                    delay(1000L)//check every second for internet access
                    isOnline = isDeviceOnline(this)
                    whatconnectivity  = whatConnectivity(this)
                }

                Log.d("test1", "Internet access died")
                var networkTimeEnd = LocalDateTime.now()
                //in android when wifi and mobile data is available then it usually uses wifi
                //so having mobile data active all the time works fine, no need to disable and enable it here

                //save some data for dissertation
                Log.d("test1", "Saving analysis data")
                val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)//creates a csv file in downloads directory
                val file = File(folder, "experiment_two_test_three_LTE.csv")
                val stream = FileOutputStream(file, true)

                //get location
                var location1 = Location("error")
                location1.setLatitude(10.0)
                location1.setLongitude(1.0)
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                //get the last known location
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        if (location == null) {
                            Toast.makeText(this, "Cannot get location", Toast.LENGTH_SHORT).show()
                        }
                        else {location1 = location}
                    }
                //try to get current location, this may not work or be very late so use the above last know location instead while this intent works in the background
                fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                    override fun isCancellationRequested() = false
                })
                    .addOnSuccessListener { location: Location? ->
                        if (location == null)
                            Toast.makeText(this, "Cannot get location", Toast.LENGTH_SHORT).show()
                        else {
                            location1 = location
                        }
                    }
                delay(2000L) //give the location client some time

                val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager //get battery %, i tried to get in more reliable units like amper hours or something but failed so this will have to do
                val mobileDataInfo =  telephonyManager.allCellInfo
                val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = registerReceiver(null, ifilter)
                val level = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = level / scale.toFloat()
                val batteryPctStr = (batteryPct * 100).toString()

                var wifiscanCommasRemoved = "" //get data of all scanned wifis
                scanResults?.forEach { //remove commas because they break the formatting in csv file, no adding quotes aroud it like in the location does not work
                        d ->
                    var temp = d.toString().replace(",", "|") //separate data using | because commas break formatting
                    wifiscanCommasRemoved = wifiscanCommasRemoved + temp + ", ," } //lol formatting was broken either way, use python regex to fix this after XD

                var current = LocalDateTime.now()// save all data collected in newtext variable, this will change depending on the experiment.
                var newText = "\n " + current + "," + "\"" + location1.toString() + "\"" + "," + whatwasconnectivity  + "," + wasonline.toString() + ","  + batteryPctStr + "," + usedSSID.toString() + "," + networkTimeStart.toString() +  "," + networkTimeNetOk.toString() + "," + networkTimeEnd.toString() + "," + wifiscanCommasRemoved + "," + "\"" + "," + mobileDataInfo + "\""
                stream.write(newText.toByteArray())//write to the file
                // Close the stream
                stream.close()

                while (TimeNow < TimeFuture) {//throttle the app by only scanning trying this once per 10 seconds
                    Log.d("test1", "Waiting for 10 seconds to pass")
                    TimeNow = System.currentTimeMillis()
                    delay(1000L)
                }

            }catch(e: ArithmeticException){
            }
        }

    }

    fun whatConnectivity(context: Context): String {//determine if the device is using lte, wifi or both
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return "No Connection"
        val capabilities = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return "No Connection"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "WIFI and 4G"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "4G" //yes it could be 5g, thats why i save allCellInfo in the csv file
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            else -> "Unknown"
        }
    }

    private fun checkPermissions(){//checks permissions
        val checkPermission1 = checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        val checkPermission2 = checkSelfPermission(Manifest.permission.INTERNET)
        val checkPermission3 = checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE)
        val checkPermission4 = checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE)
        val checkPermission5 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val checkPermission6 = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val checkPermission7 = checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
        val needToAsk =
            !((checkPermission1 == PackageManager.PERMISSION_GRANTED) and (checkPermission2 == PackageManager.PERMISSION_GRANTED) and (checkPermission3 == PackageManager.PERMISSION_GRANTED) and (checkPermission4 == PackageManager.PERMISSION_GRANTED)and (checkPermission5 == PackageManager.PERMISSION_GRANTED)and (checkPermission6 == PackageManager.PERMISSION_GRANTED)and (checkPermission7 == PackageManager.PERMISSION_GRANTED))
        if (needToAsk) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
                ), 0
            )
        }
    }
}
