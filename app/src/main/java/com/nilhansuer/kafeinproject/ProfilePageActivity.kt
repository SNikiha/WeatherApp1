package com.nilhansuer.kafeinproject

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProfilePageActivity : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    private lateinit var weatherTextView: TextView
    private lateinit var locationTextView: TextView

    private lateinit var icon_weather: ImageView

    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var textView: TextView

    private lateinit var descriptionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        textView = findViewById(R.id.textView)
        descriptionTextView = findViewById(R.id.descriptionTextView)

        currentUser?.uid?.let { userId ->
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    textView.text = "Hello $name"
                }
            }
        }

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val backgroundId = if (currentHour < 18) {
            R.drawable.ic_morning
        } else {
            R.drawable.ic_night
        }

        val backgroundImageView = findViewById<ImageView>(R.id.backgroundImageView)
        backgroundImageView.setImageResource(backgroundId)


        val buttonLocation: ImageView = findViewById(R.id.buttonLocation)
        buttonLocation.setOnClickListener {
            getLocation()
        }

        weatherTextView = findViewById(R.id.weatherTextView)
        locationTextView = findViewById(R.id.locationTextView)

        icon_weather = findViewById(R.id.icon_weather)


        val homeFragment = HomeFragment()

        val dateTextView = findViewById<TextView>(R.id.date)
        val timeTextView = findViewById<TextView>(R.id.time)

        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        dateTextView.text = currentDate
        timeTextView.text = currentTime

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, homeFragment)
            .commit()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.menu_home
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    val homeIntent = Intent(this, ProfilePageActivity::class.java)
                    startActivity(homeIntent)
                    true
                }
                R.id.menu_weather -> {
                    val weatherActivityIntent = Intent(this, WeatherActivity::class.java)
                    startActivity(weatherActivityIntent)
                    true
                }
                else -> false
            }
        }

        val exitButton: ImageView = findViewById(R.id.buttonExit)
        exitButton.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun getLocation() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }

    override fun onLocationChanged(location: Location) {
        val lat = location.latitude
        val lon = location.longitude

        val apiKey = "96823da85d3eb034c2dbc7eafc42b79f"
        val apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey"

        val apiUrlAirPol = "https://api.openweathermap.org/data/2.5/air_pollution?lat=$lat&lon=$lon&appid=$apiKey"

        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                makeAPIRequest(apiUrl)
            }
            parseWeatherData(result)
            println("Weather Data result: $result")
        }
    }

    private fun parseWeatherData(jsonString: String) {
        val jsonObject = JSONObject(jsonString)
        if (jsonObject.has("main")) {
            val mainObject = jsonObject.getJSONObject("main")
            if (mainObject.has("temp")) {
                val temperature = mainObject.getDouble("temp")
                val celcius = (temperature.minus(273.15)).toInt()
                val weatherInfo = "$celcius °C"
                weatherTextView.text = weatherInfo

                println("$celcius C")
            }
        }

        if (jsonObject.has("weather")) {
            val weatherArray = jsonObject.getJSONArray("weather")
            if (weatherArray.length() > 0) {
                val weatherObject = weatherArray.getJSONObject(0)
                if ((weatherObject.has("description")) && (weatherObject.has("main"))) {

                    val description = weatherObject.getString("description")
                    descriptionTextView.text = description

                    val main = weatherObject.getString("main")
                    when(main){
                        "Clouds"->{
                            if(description == "few clouds"){
                                setImageResGif(R.drawable.ic_fewclouds)
                            } else if(description == "scattered clouds"){
                                setImageResGif(R.drawable.ic_scatteredclouds)
                            } else {
                                setImageResGif(R.drawable.ic_brokenclouds)
                            }
                        } "Snow"->{
                            setImageResGif(R.drawable.ic_snow)
                        } "Rain"->{
                            if (description == "shower rain") {
                                setImageResGif(R.drawable.ic_showerrain)
                            } else{
                                setImageResGif(R.drawable.ic_rainy)
                            }
                        } "Clear"->{
                            setImageResGif(R.drawable.ic_clearsky)
                        } "Thunderstorm"->{
                            setImageResGif(R.drawable.ic_thunderstorm)
                        } "Mist"->{
                            setImageResGif(R.drawable.ic_mist)
                        }
                    }
                }
            }
        }
        val locationName = jsonObject.getString("name")
        val locationInfo = "$locationName"
        locationTextView.text = locationInfo

        println("locationInfo: $locationInfo")
    }

    private  fun setImageResGif( icon:Int ){
        icon_weather.setImageResource(icon);
    }

    private fun makeAPIRequest(apiUrl: String): String {
        var result = ""
        val url = URL(apiUrl)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

        try {
            val inputStream = connection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?

            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }

            result = stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }

        return result
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
