package com.nilhansuer.kafeinproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

class WeatherActivity : AppCompatActivity() {

    private lateinit var cityEditText: EditText
    private lateinit var weatherTextView: TextView
    private lateinit var descriptionTextView: TextView
    //private lateinit var iconWeather : ImageView

    private lateinit var icon_weather: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        cityEditText = findViewById(R.id.cityEditText)
        weatherTextView = findViewById(R.id.weatherTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)

        icon_weather = findViewById(R.id.icon_weather)

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val backgroundId = if (currentHour < 18) {
            R.drawable.ic_morning2
        } else {
            R.drawable.ic_night2
        }

        val backgroundImageView = findViewById<ImageView>(R.id.backgroundImageView)
        backgroundImageView.setImageResource(backgroundId)

        val getWeatherButton: Button = findViewById(R.id.getWeatherButton)

        getWeatherButton.setOnClickListener {
            val city = cityEditText.text.toString().trim()
            if (city.isNotEmpty()) {
                getWeatherData(city)
            }
        }

        val homeFragment = HomeFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, homeFragment)
            .commit()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.menu_weather
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
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun getWeatherData(city: String) {
        val apiKey = "96823da85d3eb034c2dbc7eafc42b79f"
        val apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey"

        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                makeAPIRequest(apiUrl)
            }
            parseWeatherData(result)
            println("Weather Data result: $result")
        }
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

    private fun parseWeatherData(jsonString: String) {
        val jsonObject = JSONObject(jsonString)
        if (jsonObject.has("main")) {
            val mainObject = jsonObject.getJSONObject("main")
            if (mainObject.has("temp")) {
                val temperature = mainObject.getDouble("temp")
                val celcius = (temperature.minus(273.15)).toInt()
                val weatherInfo = "Temperature: $celcius C"
                weatherTextView.text = weatherInfo

                println("Temperature: $celcius C")
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

    }

    private fun setImageResGif( icon: Int ) {
        icon_weather.setImageResource(icon);
    }
}