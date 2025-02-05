package com.example.weatherviewer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.weatherviewer.databinding.MainLayoutBinding
import com.example.weatherviewer.databinding.MainPageBinding
import com.example.weatherviewer.databinding.SearchPageBinding
import com.example.weatherviewer.ui.theme.WeatherViewerTheme
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherviewer.databinding.WeekContentBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class SearchActivity : ComponentActivity() {
    private lateinit var binding: MainLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        binding.searchPageUi.backButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.searchResult.backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.mainWeekContent.root.visibility = View.GONE
        binding.searchResult.root.visibility = View.VISIBLE
        binding.mainPageUi.root.visibility = View.GONE
        binding.searchPageUi.root.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.loadingText.visibility = View.VISIBLE

        // get the data passed from the main activity
        val city = intent.getStringExtra("city").toString()
        val state = intent.getStringExtra("state").toString()
        val latlng = intent.getStringExtra("latlng").toString()
        val lat = latlng.split(",").get(0)
        val lng = latlng.split(",")[1]

        // set up the volley request queue
        val requestQueue = Volley.newRequestQueue(this)
        binding.mainWeekContent.searchfavtabMain.setImageResource(this.resources.getIdentifier("add_fav", "drawable", this.packageName))
        binding.mainWeekContent.searchfavtabMain.setOnClickListener {
            addFav(binding.mainWeekContent, this, city, state, lat, lng, requestQueue)
            Toast.makeText(this, "$city was added to favorites!",
                Toast.LENGTH_LONG).show()
        }
        // set the city and state name at the top of the page
        binding.searchResult.cityStateName.setText("$city, $state")
        binding.mainWeekContent.mainPageAddress.setText("$city, $state")
        setTheme(R.style.Theme_WeatherViewer)
        setContentView(view)

        mainPageValueFitting(lat, lng, city, state, requestQueue, binding,this)
    }
}

fun removeFav(binding: WeekContentBinding, activity: ComponentActivity, city: String, state: String, lat: String, lng: String, requestQueue: RequestQueue) {
    val removeFavUrl = "https://multiweatherview1234.wl.r.appspot.com/api/removeFavorite/$lat/$lng"
    val stringRequest = StringRequest(Request.Method.GET, removeFavUrl,
        // Response Listener
        { response ->
            Log.d("favRemove", response)
        },
        // Error Listener
        { error ->
            // Handle the error if the request fails
            Log.e("VolleyError", "Error: ${error.message}")
        }
    )
    requestQueue.add(stringRequest)
    binding.searchfavtabMain.setImageResource(activity.resources.getIdentifier("add_fav", "drawable", activity.packageName))
    binding.searchfavtabMain.setOnClickListener {
        addFav(binding, activity, city, state, lat, lng, requestQueue)
        Toast.makeText(activity, "$city was added to favorites!",
            Toast.LENGTH_LONG).show()
    }
}

fun addFav(binding: WeekContentBinding, activity: ComponentActivity, city: String, state: String, lat: String, lng: String, requestQueue: RequestQueue) {
    val addFavUrl = "https://multiweatherview1234.wl.r.appspot.com/api/addFavorite/$city/$state/$lat/$lng"
    val stringRequest = StringRequest(Request.Method.GET, addFavUrl,
        // Response Listener
        { response ->
            Log.d("favAdd", response)
        },
        // Error Listener
        { error ->
            // Handle the error if the request fails
            Log.e("VolleyError", "Error: ${error.message}")
        }
    )
    requestQueue.add(stringRequest)
    binding.searchfavtabMain.setImageResource(activity.resources.getIdentifier("rem_fav", "drawable", activity.packageName))
    binding.searchfavtabMain.setOnClickListener {
        removeFav(binding, activity, city, state, lat, lng, requestQueue)
        Toast.makeText(activity, "$city was removed from favorites!",
            Toast.LENGTH_LONG).show()
    }
}

fun mainPageValueFitting(lat: String, lng: String, city: String, state: String, requestQueue: RequestQueue, binding: MainLayoutBinding, activity: SearchActivity): Array<String> {
    val weekDataUrl = "https://multiweatherview1234.wl.r.appspot.com/api/tomorrowIO/$lat/$lng"

    var windSpeed = ""
    var pressure = ""
    var precipitation = ""
    var temperature = ""
    var weather = ""
    var weatherSrc = ""
    var humidity = ""
    var visibility = ""
    var cloudCover = ""
    var uv = ""

    val jsonRequest = JsonObjectRequest(Request.Method.GET, weekDataUrl, null,
        // Response Listener
        { response ->

            val data = response.getJSONObject("data")
            val timelines = data.getJSONArray("timelines")
            val timeline = timelines.getJSONObject(0)
            val intervals = timeline.getJSONArray("intervals")

            val temps = ArrayList<String>()
            for (i in 0 until intervals.length()) {
                val interval = intervals.getJSONObject(i)
                val startTime = interval.getString("startTime")
                val values = interval.getJSONObject("values")

                val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                // Parse the date string to a Date object
                val dateTime = timeFormat.parse(startTime)

                if (i == 0) {
                    // set the main temperature on top
                    var temp = values.getString("temperatureApparent")
                    // round the current temperature
                    val tempFloat = temp.toFloat().roundToInt().toString()
                    binding.mainWeekContent.mainPageTemp.setText("$tempFloat°F")
                    temperature = tempFloat

                    // set the weather code and the description
                    val weatherCode = values.getString("weatherCode")
                    val descriptionFull: String = weatherCodes[weatherCode].toString()
                    val description = descriptionFull.split(",")[0]
                    weather = description
                    val src = descriptionFull.split(",")[1]
                    val resId = activity.resources.getIdentifier(src, "drawable", activity.packageName)
                    binding.mainWeekContent.mainPageWeatherIcon.setImageResource(resId)
                    binding.mainWeekContent.mainPageWeather.setText(description)
                    weatherSrc = resId.toString()

                    // set the humidity
                    humidity = values.getString("humidity").toFloat().roundToInt().toString()  + "%"
                    binding.mainWeekContent.mainPageHumidty.setText(humidity)

                    // set the wind speed
                    windSpeed = values.getString("windSpeed") + "mph"
                    binding.mainWeekContent.mainPageWindSpeed.setText(windSpeed)

                    // set the visibility
                    visibility = values.getString("visibility") + "mi"
                    binding.mainWeekContent.mainPageVisibility.setText(visibility)

                    // set the pressure
                    //val pressure = values.getString("pressureSeaLevel") + "inHg"
                    binding.mainWeekContent.mainPagePressure.setText("30.0inHg")
                    pressure = "30.0inHg"

                    // get the precipitation
                    precipitation = values.getString("precipitationProbability").toFloat().roundToInt().toString()  + "%"

                    // get the cloud cover
                    cloudCover = values.getString("cloudCover").toFloat().roundToInt().toString()  + "%"

                    // get the uv level
                    uv = "0"

                    // store the high and low temp to temp list
                    val currentTime = dateTime.time.toString()
                    val highTemp = values.getString("temperatureMax").toFloat().roundToInt().toString()
                    val lowTemp = values.getString("temperatureMin").toFloat().roundToInt().toString()
                    temps += "$currentTime,$highTemp,$lowTemp"

                } else {
                    // get the date and set to header
                    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    val date = LocalDate.parse(startTime, dateFormat)
                    val formatedDate = date.year.toString() + "-" + date.monthValue.toString() + "-" + date.dayOfMonth.toString()

                    // get the weather code to set the small weather icon
                    val weatherCode = values.getString("weatherCode")
                    val descriptionFull: String = weatherCodes[weatherCode].toString()
                    val src = descriptionFull.split(",")[1]
                    val resId = activity.resources.getIdentifier(src, "drawable", activity.packageName)

                    // get the low and high temp
                    val highTemp = values.getString("temperatureMax").toFloat().roundToInt().toString()
                    val lowTemp = values.getString("temperatureMin").toFloat().roundToInt().toString()
                    val currentTime = dateTime.time.toString()
                    temps += "$currentTime,$highTemp,$lowTemp"
                    if (i == 1) {
                        binding.mainWeekContent.mainPageDate1.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall1.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp1.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp1.setText(lowTemp)
                    }
                    if (i == 2) {
                        binding.mainWeekContent.mainPageDate2.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall2.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp2.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp2.setText(lowTemp)
                    }
                    if (i == 3) {
                        binding.mainWeekContent.mainPageDate3.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall3.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp3.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp3.setText(lowTemp)
                    }
                    if (i == 4) {
                        binding.mainWeekContent.mainPageDate4.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall4.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp4.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp4.setText(lowTemp)
                    }
                    if (i == 5) {
                        binding.mainWeekContent.mainPageDate5.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall5.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp5.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp5.setText(lowTemp)
                    }
                }
            }
            binding.progressBar.visibility = View.GONE;
            binding.loadingText.visibility = View.GONE;
            binding.mainWeekContent.root.visibility = View.VISIBLE;
            //binding.invisible_row.visibility = View.INVISIBLE;
            // This is the response you will get as a string
            Log.d("VolleyResponse", "Response: $response")
            val currentWeather = arrayOf(windSpeed, pressure, precipitation, temperature, weather, weatherSrc, humidity, visibility, cloudCover, uv)
            // set the ripple effect to the card
            binding.mainWeekContent.card.setOnClickListener {
                val intent = Intent(activity, DetailsActivity::class.java)
                intent.putExtra("latlng", "$lat,$lng")
                intent.putExtra("city", city)
                intent.putExtra("state", state)
                intent.putExtra("details", currentWeather)
                intent.putExtra("temps", temps)
                activity.startActivity(intent)
            }
        },
        // Error Listener
        { error ->
            // Handle the error if the request fails
            Log.e("VolleyError", "Error: ${error.message}")
        }
    )

    // Add the request to the queue
    requestQueue.add(jsonRequest)

    return arrayOf(windSpeed, pressure, precipitation, temperature, weather, weatherSrc, humidity, visibility, cloudCover, uv)
}

/*
fun mainPageValueFitting(lat: String, lng: String, city: String, state: String, requestQueue: RequestQueue, binding: MainLayoutBinding, activity: SearchActivity): Array<String> {
    val weekDataUrl = "https://multiweatherview1234.wl.r.appspot.com/api/tomorrowIO/$lat/$lng"

    var windSpeed = ""
    var pressure = ""
    var precipitation = ""
    var temperature = ""
    var weather = ""
    var weatherSrc = ""
    var humidity = ""
    var visibility = ""
    var cloudCover = ""
    var uv = ""

    val jsonRequest = JsonObjectRequest(Request.Method.GET, weekDataUrl, null,
        // Response Listener
        { response ->

            val data = response.getJSONObject("data")
            val timelines = data.getJSONArray("timelines")
            val timeline = timelines.getJSONObject(0)
            val intervals = timeline.getJSONArray("intervals")
            for (i in 0 until intervals.length()) {
                val interval = intervals.getJSONObject(i)
                val startTime = interval.getString("startTime")
                val values = interval.getJSONObject("values")
                if (i == 0) {
                    // set the main temperature on top
                    val temp = values.getString("temperatureApparent")
                    // round the current temperature
                    val tempFloat = temp.toFloat().roundToInt().toString()
                    binding.mainWeekContent.mainPageTemp.setText("$tempFloat°F")
                    temperature = tempFloat

                    // set the weather code and the description
                    val weatherCode = values.getString("weatherCode")
                    val descriptionFull: String = weatherCodes[weatherCode].toString()
                    val description = descriptionFull.split(",")[0]
                    weather = description
                    val src = descriptionFull.split(",")[1]
                    val resId = activity.resources.getIdentifier(src, "drawable", activity.packageName)
                    binding.mainWeekContent.mainPageWeatherIcon.setImageResource(resId)
                    binding.mainWeekContent.mainPageWeather.setText(description)
                    weatherSrc = resId.toString()

                    // set the humidity
                    humidity = values.getString("humidity") + "%"
                    binding.mainWeekContent.mainPageHumidty.setText(humidity)

                    // set the wind speed
                    windSpeed = values.getString("windSpeed") + "mph"
                    binding.mainWeekContent.mainPageWindSpeed.setText(windSpeed)

                    // set the visibility
                    visibility = values.getString("visibility") + "mi"
                    binding.mainWeekContent.mainPageVisibility.setText(visibility)

                    // set the pressure
                    //val pressure = values.getString("pressureSeaLevel") + "inHg"
                    binding.mainWeekContent.mainPagePressure.setText("30.0inHg")
                    pressure = "30.0inHg"

                    // get the precipitation
                    precipitation = values.getString("precipitationProbability") + "%"

                    // get the cloud cover
                    cloudCover = values.getString("cloudCover") + "%"

                    // get the uv level
                    uv = "0"

                } else {
                    // get the date and set to header
                    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    val date = LocalDate.parse(startTime, dateFormat)
                    val formatedDate = date.year.toString() + "-" + date.monthValue.toString() + "-" + date.dayOfMonth.toString()

                    // get the weather code to set the small weather icon
                    val weatherCode = values.getString("weatherCode")
                    val descriptionFull: String = weatherCodes[weatherCode].toString()
                    val src = descriptionFull.split(",")[1]
                    val resId = activity.resources.getIdentifier(src, "drawable", activity.packageName)

                    // get the low and high temp
                    val highTemp = values.getString("temperatureMax").toFloat().roundToInt().toString()
                    val lowTemp = values.getString("temperatureMin").toFloat().roundToInt().toString()

                    if (i == 1) {
                        binding.mainWeekContent.mainPageDate1.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall1.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp1.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp1.setText(lowTemp)
                    }
                    if (i == 2) {
                        binding.mainWeekContent.mainPageDate2.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall2.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp2.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp2.setText(lowTemp)
                    }
                    if (i == 3) {
                        binding.mainWeekContent.mainPageDate3.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall3.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp3.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp3.setText(lowTemp)
                    }
                    if (i == 4) {
                        binding.mainWeekContent.mainPageDate4.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall4.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp4.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp4.setText(lowTemp)
                    }
                    if (i == 5) {
                        binding.mainWeekContent.mainPageDate5.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall5.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp5.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp5.setText(lowTemp)
                    }
                }
            }
            binding.progressBar.visibility = View.GONE;
            binding.loadingText.visibility = View.GONE;
            binding.mainWeekContent.root.visibility = View.VISIBLE;
            //binding.invisible_row.visibility = View.INVISIBLE;
            // This is the response you will get as a string
            Log.d("VolleyResponse", "Response: $response")
            val currentWeather = arrayOf(windSpeed, pressure, precipitation, temperature, weather, weatherSrc, humidity, visibility, cloudCover, uv)
            // set the ripple effect to the card
            binding.mainWeekContent.card.setOnClickListener {
                val intent = Intent(activity, DetailsActivity::class.java)
                intent.putExtra("latlng", "$lat,$lng")
                intent.putExtra("city", city)
                intent.putExtra("state", state)
                intent.putExtra("details", currentWeather)
                intent.putExtra("temps", temps)
                activity.startActivity(intent)
            }
        },
        // Error Listener
        { error ->
            // Handle the error if the request fails
            Log.e("VolleyError", "Error: ${error.message}")
        }
    )

    // Add the request to the queue
    requestQueue.add(jsonRequest)

    return arrayOf(windSpeed, pressure, precipitation, temperature, weather, weatherSrc, humidity, visibility, cloudCover, uv)
}

/*
fun mainPageValueFitting(lat: String, lng: String, requestQueue: RequestQueue, binding: MainLayoutBinding, activity: SearchActivity) {
    val weekDataUrl = "https://multiweatherview1234.wl.r.appspot.com/api/tomorrowIO/$lat/$lng"

    val jsonRequest = JsonObjectRequest(
        Request.Method.GET, weekDataUrl, null,
        // Response Listener
        { response ->

            val data = response.getJSONObject("data")
            val timelines = data.getJSONArray("timelines")
            val timeline = timelines.getJSONObject(0)
            val intervals = timeline.getJSONArray("intervals")
            for (i in 0 until intervals.length()) {
                val interval = intervals.getJSONObject(i)
                val startTime = interval.getString("startTime")
                val values = interval.getJSONObject("values")
                if (i == 0) {
                    // set the main temperature on top
                    val temp = values.getString("temperatureApparent")
                    // round the current temperature
                    val tempFloat = temp.toFloat().roundToInt().toString()
                    binding.mainWeekContent.mainPageTemp.setText("$tempFloat°F")

                    // set the weather code and the description
                    val weatherCode = values.getString("weatherCode")
                    val descriptionFull: String = weatherCodes[weatherCode].toString()
                    val description = descriptionFull.split(",")[0]
                    val src = descriptionFull.split(",")[1]
                    val resId = activity.resources.getIdentifier(src, "drawable", activity.packageName)
                    binding.mainWeekContent.mainPageWeatherIcon.setImageResource(resId)
                    binding.mainWeekContent.mainPageWeather.setText(description)

                    // set the humidity
                    val humidity = values.getString("humidity") + "%"
                    binding.mainWeekContent.mainPageHumidty.setText(humidity)

                    // set the wind speed
                    val windSpeed = values.getString("windSpeed") + "mph"
                    binding.mainWeekContent.mainPageWindSpeed.setText(windSpeed)

                    // set the visibility
                    val visibility = values.getString("visibility") + "mi"
                    binding.mainWeekContent.mainPageVisibility.setText(visibility)

                    // set the pressure
                    //val pressure = values.getString("pressureSeaLevel") + "inHg"
                    binding.mainWeekContent.mainPagePressure.setText("30.0inHg")
                } else {
                    // get the date and set to header
                    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    val date = LocalDate.parse(startTime, dateFormat)
                    val formatedDate = date.year.toString() + "-" + date.monthValue.toString() + "-" + date.dayOfMonth.toString()

                    // get the weather code to set the small weather icon
                    val weatherCode = values.getString("weatherCode")
                    val descriptionFull: String = weatherCodes[weatherCode].toString()
                    val src = descriptionFull.split(",")[1]
                    val resId = activity.resources.getIdentifier(src, "drawable", activity.packageName)

                    // get the low and high temp
                    val highTemp = values.getString("temperatureMax").toFloat().roundToInt().toString()
                    val lowTemp = values.getString("temperatureMin").toFloat().roundToInt().toString()

                    if (i == 1) {
                        binding.mainWeekContent.mainPageDate1.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall1.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp1.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp1.setText(lowTemp)
                    }
                    if (i == 2) {
                        binding.mainWeekContent.mainPageDate2.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall2.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp2.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp2.setText(lowTemp)
                    }
                    if (i == 3) {
                        binding.mainWeekContent.mainPageDate3.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall3.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp3.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp3.setText(lowTemp)
                    }
                    if (i == 4) {
                        binding.mainWeekContent.mainPageDate4.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall4.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp4.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp4.setText(lowTemp)
                    }
                    if (i == 5) {
                        binding.mainWeekContent.mainPageDate5.setText(formatedDate)
                        binding.mainWeekContent.mainPageWeatherSmall5.setImageResource(resId)
                        binding.mainWeekContent.mainPageHighTemp5.setText(highTemp)
                        binding.mainWeekContent.mainPageLowTemp5.setText(lowTemp)
                    }
                }
            }
            binding.progressBar.visibility = View.GONE;
            binding.loadingText.visibility = View.GONE;
            binding.mainWeekContent.root.visibility = View.VISIBLE;
            //binding.invisible_row.visibility = View.INVISIBLE;
            // This is the response you will get as a string
            Log.d("VolleyResponse", "Response: $response")
        },
        // Error Listener
        { error ->
            // Handle the error if the request fails
            Log.e("VolleyError", "Error: ${error.message}")
        }
    )

    // Add the request to the queue
    requestQueue.add(jsonRequest)
}
 */