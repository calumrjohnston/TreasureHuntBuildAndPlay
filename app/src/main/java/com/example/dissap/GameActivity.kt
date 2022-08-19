package com.example.dissap

import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import android.app.Activity
import android.os.*
import androidx.annotation.RequiresApi
import androidx.room.Room.databaseBuilder
import com.example.dissap.Global.Variables.loggedUser

class GameActivity : AppCompatActivity(), OnMapReadyCallback {

    //Initialise map variables and set empty variables to be set by intent laater
    private var huntDB: HuntDB? = null
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    var lastLocation = Location("")
    private var locationUpdateState = false
    private lateinit var geofencingClient: GeofencingClient
    private var distance: Float = 0.0f
    private var names = ""
    private var hints = ""
    private var infos = ""
    private var lats = ""
    private var longs = ""
    private var huntId = ""
    private var title= ""
    private var victory = ""
    var currentPosition: Int = 0
    var gameEnd = 0
    val LAUNCH_SECOND_ACTIVITY: Int = 1
    val startTime:Long = SystemClock.elapsedRealtime()
    var latLngList: ArrayList<LatLng> = ArrayList()
    var imgList: MutableList<Img> = mutableListOf()
    var markerList: ArrayList<Marker> = ArrayList()
    var gameWon = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        val PERMISSION_ID = 42
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            fusedLocationClient.lastLocation
        }
        geofencingClient = LocationServices.getGeofencingClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
            }
        }
        getLastLocation()
        requestNewLocationData()
        //Set database and fetch values for images and deleting the hunt later, set global
        //huntId for use in pop up intent
        huntId = intent.getStringExtra("huntId")
        Global.huntId = huntId
        Log.d("GameActivity", huntId)
        Global.imgDB = databaseBuilder(
            this,
            ImgDB::class.java, "dissap.img.db"
        ).build()
        Global.huntDB = databaseBuilder(
            this,
            HuntDB::class.java, "dissap.hunt.db"
        ).build()
        var imgServices = ImageServices()
        imgList= imgServices.getAllImages()
        Log.d("GameActivity", imgList.size.toString())
        title = intent.getStringExtra("title")
        victory = intent.getStringExtra("victory")
    }

    class ImageServices {

        //Public function to call the async thread and return the images for the hunt
        fun getAllImages(): MutableList<Img> {
            return GetImgsAsync().execute().get() as MutableList<Img>
        }

        //Create an async thread so multiple users can manipulate the thread without collisions
        private class GetImgsAsync : AsyncTask<Void, Void, MutableList<Img>>() {
            override fun doInBackground(vararg url: Void): MutableList<Img> {
                return Global.imgDB!!.imgDao().searchByHuntId(Global.huntId)
            }
        }
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        fusedLocationClient.lastLocation

        //Retrieve hunt data from intent on launch
        names = intent.getStringExtra("names")
        hints = intent.getStringExtra("hints")
        infos = intent.getStringExtra("infos")
        lats = intent.getStringExtra("lats")
        longs = intent.getStringExtra("longs")

        //Unpack the joined string into a list for use by splitting at the # delimiter
        val latList = lats.split("#").map{ it.trim() }
        val longList = longs.split("#").map{ it.trim() }
        gameEnd = latList.size - 1

        //Turn lat and long variabled into LatLng data type and add them into a list
        for (x in 0..latList.size-1) {
            val markLoc = LatLng(latList[x].toDouble(),longList[x].toDouble())
            latLngList.add(x, markLoc)

        }

        //User the latLng list to create s list of markers but don't add them to the map
        for (x in 0..latList.size-1) {
            val markerToAdd = mMap.addMarker(MarkerOptions().position(latLngList[x]))
                markerList.add(x, markerToAdd)
            markerToAdd.remove()
        }

        //Set up the map and add the first waypoint location to the map
        val realMarker = latLngList[0]
        val markerReal = mMap.addMarker( MarkerOptions().position(realMarker))
        markerReal.isVisible = false
        setUpMap()
        fusedLocationClient.lastLocation
    }

    //Refresh and get location, handle for location turned off
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        var lat = location.latitude
                        var long = location.longitude
                        var accuracy = location.accuracy
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            //Ask user for location permission
            requestPermissions()
        }
    }

    //Request location data and ping every set interval
    private fun requestNewLocationData() {
        Log.i("myLocation", "request")
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 2000
        mLocationRequest.fastestInterval = 1000

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    //Set current location and move the camera focus to the location
    private val mLocationCallback = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onLocationResult(locationResult: LocationResult) {
            Log.i("myLocation", "Callback")
            var mLastLocation: Location = locationResult.lastLocation
            var lat = mLastLocation.latitude
            var long = mLastLocation.longitude
            val lastLoc = LatLng(lat, long)
            Log.i("myLocation", lastLoc.toString())
            fusedLocationClient.lastLocation
            checkDistanceReal(lastLoc)
            //checkDistanceSelected(lastLoc)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLoc,17f))

        }
    }

    //Return if the location permission is set to true
    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    //Return if the location permission for this app is set to true
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    //Launch location permission manager
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    //If location permission is set to true then run the code which fetches the user data
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    //
    fun showCurrentHint(view: View){
        val hintList = hints.split("#").map{ it.trim() }
        Toast.makeText(this, hintList[currentPosition] , Toast.LENGTH_LONG).show()
    }

    //Functions used to generate lat and long values within the bay campus boundary
    fun showCurrentLocations(view: View){
        val showWayPointIntent = Intent(this, WaypointActivity::class.java)
        val nameList = names.split("#").map{ it.trim() }
        val infoList = infos.split("#").map{ it.trim() }
        val arrayListToPass = arrayListOf<Img>()
        arrayListToPass.addAll(imgList)
        showWayPointIntent.putExtra("huntId",Global.huntId)
        showWayPointIntent.putExtra("names",nameList.toTypedArray())
        showWayPointIntent.putExtra("infos",infoList.toTypedArray())
        showWayPointIntent.putParcelableArrayListExtra("latLongs", latLngList)
        startActivityForResult(showWayPointIntent, LAUNCH_SECOND_ACTIVITY)
    }

    //As long as the result is ok then return latlng string variable, split and format it into the
    //appropriate data type, clear the map, add the real marker and displaythe selected marker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check that it is the SecondActivity with an OK result
        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                var returnString = data!!.getStringExtra("result")
                val latLngSeperate:List<String> = returnString.split(",")
                val returnLat:Double = latLngSeperate[0].replace("(","")
                    .replace("lat/lng: ","").toDouble()
                val returnLong:Double = latLngSeperate[1].replace(")","").toDouble()
                val selectedMarker = LatLng(returnLat, returnLong)
                mMap.clear()
                val realMarker = latLngList[currentPosition]
                val markerReal = mMap.addMarker(MarkerOptions().position(realMarker))
                markerReal.isVisible = false
                val markerSelected = mMap.addMarker(MarkerOptions().position(selectedMarker))
                markerSelected.isVisible = true
                markerSelected.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.x))
            }
        }
    }

    //Check distance between marker and user
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun checkDistanceReal(lastLoc: LatLng) {
        val markerLoc = Location(LocationManager.GPS_PROVIDER)
        val userLoc = Location(LocationManager.GPS_PROVIDER)
        markerLoc.latitude = markerList[currentPosition].position.latitude
        markerLoc.longitude = markerList[currentPosition].position.longitude
        userLoc.latitude = lastLoc.latitude
        userLoc.longitude = lastLoc.longitude
        distance = userLoc.distanceTo(markerLoc)
        if (distance < 12) {
            //If the user is at the end of the list of waypoints, delete the current hunt to make
            //it unjoinable and launch the victory activity
            if (currentPosition == gameEnd){
                DeleteHunt(this, Global.huntId).execute()
                val endTime:Long = SystemClock.elapsedRealtime()
                val elapsedMilliSeconds = endTime - startTime
                val elapsedSeconds = elapsedMilliSeconds / 1000.0
                val victoryIntent = Intent(this, VictoryActivity::class.java)
                victoryIntent.putExtra("huntId", huntId)
                victoryIntent.putExtra("userId", loggedUser?.username)
                victoryIntent.putExtra("elapsedSeconds", elapsedSeconds)
                victoryIntent.putExtra("victory", victory)
                //For some reason the background activity of the map doesn't end and repetitively
                //launched the victory activity, this prevents this from happening
                startActivity(victoryIntent)
                stopLockTask()
                stopService(this.intent)
                this.finish()
                finish()
                currentPosition = 0
                gameEnd = 999999999
                gameWon = true
            } else {
                //If game is still in progress then set up the next marker in the game and launch
                //the waypoint information table
                if (gameWon == false) {
                    mMap.clear()
                    val realMarker = latLngList[currentPosition + 1]
                    val markerReal = mMap.addMarker(MarkerOptions().position(realMarker))
                    markerReal.isVisible = false
                    val popUpIntent = Intent(this, PopUpActivity::class.java)
                    //huntId = intent.getLongExtra("huntId", 0)
                    val nameList = names.split("#").map { it.trim() }
                    val infoList = infos.split("#").map { it.trim() }
                    popUpIntent.putExtra("name", nameList[currentPosition])
                    popUpIntent.putExtra("info", infoList[currentPosition])
                    popUpIntent.putExtra("currentPosition", currentPosition)
                    currentPosition = currentPosition + 1
                    startActivity(popUpIntent)
                }
            }
        }
    }

    //Search for the hunt with the id and delete it in a singular thread to prevent collisions
    private class DeleteHunt(var context: GameActivity, var huntId: String) :
        AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void?): Boolean {
            Global.huntDB!!.huntDao().deleteHunt(Global.huntId)
            return true
        }
    }
}
