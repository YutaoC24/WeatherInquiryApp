package com.example.weatherviewer

import android.app.SearchManager
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import android.widget.CursorAdapter
import android.widget.SearchView
import android.widget.SimpleCursorAdapter
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherviewer.databinding.MainLayoutBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Locale

val weatherCodes = mapOf(
    "0" to "Unknown,Unknown",
    "1000" to "Clear,clear_day",
    "1100" to "Mostly Clear,mostly_clear_day",
    "1101" to "Partly Cloudy,partly_cloudy_day",
    "1102" to "Mostly Cloudy,mostly_cloudy",
    "1001" to "Cloudy,cloudy",
    "2000" to "Fog, fog",
    "2100" to "Light Fog,fog_light",
    "4000" to "Drizzle,drizzle",
    "4001" to "Rain,rain",
    "4200" to "Light Rain,rain_light",
    "4201" to "Heavy Rain,rain_heavy",
    "5000" to "Snow,snow",
    "5001" to "Flurries,flurries",
    "5100" to "Light Snow,snow_light",
    "5101" to "Heavy Snow,snow_heavy",
    "6000" to "Freezing Drizzle,freezing_drizzle",
    "6001" to "Freezing Rain,freezing_rain",
    "6200" to "Light Freezing Rain,freezing_rain_light",
    "6201" to "Heavy Freezing Rain,freezing_rain_heavy",
    "7000" to "Ice Pellets,ice_pellets",
    "7101" to "Heavy Ice Pellets,ice_pellets_heavy",
    "7102" to "Light Ice Pellets,ice_pellets_light",
    "8000" to "Thunderstorm,tstorm"
)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setTheme(R.style.Theme_WeatherViewer)
        setContentView(view)
        // set the loading UIs visible
        binding.progressBar.visibility = View.VISIBLE;
        binding.loadingText.visibility = View.VISIBLE;
        binding.searchPageUi.root.visibility = View.GONE;
        binding.searchResult.root.visibility = View.GONE;
        binding.mainPageUi.root.visibility = View.VISIBLE;
        binding.mainWeekContent.root.visibility = View.GONE;
        // set the onclick event to the search button
        binding.mainPageUi.imageButton.setOnClickListener{
            binding.mainPageUi.root.visibility = View.GONE
            binding.searchPageUi.root.visibility = View.VISIBLE
        }
        binding.searchPageUi.backButton.setOnClickListener{
            binding.mainPageUi.root.visibility = View.VISIBLE
            binding.searchPageUi.root.visibility = View.GONE
        }

        // set up the volley request queue
        val requestQueue = Volley.newRequestQueue(this)

        // set up the search query
        val from = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
        val to = intArrayOf(R.id.searchItemID)
        val searchView = binding.searchPageUi.searchView
        //val suggestions = listOf("Apple", "Banana", "Cherry", "Dog", "Elephant")
        val cursorAdapter = SimpleCursorAdapter(this, R.layout.layout_item, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
        searchView.suggestionsAdapter = cursorAdapter


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                val cursor =
                    MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1))

                var suggestions: List<String> = emptyList()
                // send the received input to backend to receive suggestions
                val autoCompleteUrl = "https://multiweatherview1234.wl.r.appspot.com/api/autoComplete/$newText"
                val jsonRequest = JsonObjectRequest(Request.Method.GET, autoCompleteUrl, null,
                    // Response Listener
                    { response ->
                        suggestions = emptyList()
                        val predictions = response.getJSONArray("predictions")
                        var iteration_num = 5
                        if (predictions.length() < 5) {
                            iteration_num = predictions.length()
                        }
                        for (i in 0..<iteration_num) {
                            val prediction = predictions.getJSONObject(i)
                            val terms = prediction.getJSONArray("terms")
                            val cityterm = terms.getJSONObject(0).getString("value")
                            val stateterm = terms.getJSONObject(1).getString("value")
                            val newSuggestion = "$cityterm, $stateterm"
                            val newSuggestions = suggestions + newSuggestion
                            suggestions = newSuggestions
                        }
                        newText?.let {
                            suggestions.forEachIndexed { index, suggestion ->
                                Log.d("success", "New suggestions coming")
                                //if (suggestion.contains(newText, true))
                                cursor.addRow(arrayOf(index, suggestion))
                            }
                        }
                        cursorAdapter.changeCursor(cursor)
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
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
                val selection =
                    cursor.getString(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1))
                searchView.setQuery(selection, false)

                // Do whatever you want with selection text
                val city = selection.split(",")[0]
                val state = selection.split(",")[1]
                val geoCodingUrl = "https://multiweatherview1234.wl.r.appspot.com/api/geocoding/a/$city/$state"
                val stringRequest = StringRequest(Request.Method.GET, geoCodingUrl,
                    // Response Listener
                    { response ->
                        // load the city and state into the main page
                        val frags = response.split(",")
                        val lat = frags[0]
                        val lng = frags[1]
                        val intent = Intent(this@MainActivity, SearchActivity::class.java)
                        intent.putExtra("latlng", "$lat,$lng")
                        intent.putExtra("city", city)
                        intent.putExtra("state", state)
                        startActivity(intent)
                        // This is the response you will get as a string
                        Log.d("VolleyResponse", "Response: $response")
                    },
                    // Error Listener
                    { error ->
                        // Handle the error if the request fails
                        Log.e("VolleyError", "Error: ${error.message}")
                    }
                )
                requestQueue.add(stringRequest)
                return true
            }

            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }
        })

        var currentCity = ""
        var currentState = ""
        val adapter = ViewPagerAdapterMain(this)
        val viewPager: ViewPager2 = binding.viewPagerMain
        val tabLayout: TabLayout = binding.tabLayoutMain
        // get all the favorite items from the database
        val favAllUrl: String = "https://multiweatherview1234.wl.r.appspot.com//api/favoriteAll"
        val jsonRequest2 =  JsonArrayRequest(Request.Method.GET, favAllUrl, null,
            // Response Listener
            { response ->
                // get the number of favorites
                val favNum = response.length()
                adapter.setTabNum(favNum + 1)
                val favCityList = ArrayList<String>()
                for (i in 0..<favNum) {
                    val favObject = response.getJSONObject(i)
                    val favCity = favObject.getString("city")
                    val favState = favObject.getString("state")
                    val favLat = favObject.getString("lat")
                    val favLng = favObject.getString("lng")
                    val favString = "$favCity,$favState,$favLat,$favLng"
                    favCityList.add(favString)
                }
                adapter.setFavInfo(favCityList)

                val lat = "34.0549"
                val lng = "118.2426"
                currentCity = "Los Angeles"
                currentState = "CA"
                adapter.setCurrentInfo("$currentCity,$currentState,$lat,$lng")
                //mainPageValueFitting(lat, lng, currentCity, currentState, requestQueue, binding,this)

                // connect the viewpager and the adapter
                viewPager.adapter = adapter

                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    tab.text = when (position) {
                        0 -> ""
                        else -> ""
                    }
                }.attach()

                /*
                // url that returns string in format lat, lng, city, state
                val ipUrl: String = "https://multiweatherview1234.wl.r.appspot.com/api/autoDetect"
                val stringRequest = StringRequest(Request.Method.GET, ipUrl,
                    // Response Listener
                    { response ->
                        val frags = response.split(",")
                        val lat = frags[0]
                        val lng = frags[1]
                        currentCity = frags[2]
                        currentState = frags[3]
                        adapter.setCurrentInfo("$currentCity,$currentState,$lat,$lng")
                        //mainPageValueFitting(lat, lng, currentCity, currentState, requestQueue, binding,this)

                        // connect the viewpager and the adapter
                        viewPager.adapter = adapter

                        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                            tab.text = when (position) {
                                0 -> ""
                                else -> ""
                            }
                        }.attach()

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
                requestQueue.add(stringRequest)
                 */


            },
            // Error Listener
            { error ->
                // Handle the error if the request fails
                Log.e("VolleyError", "Error: ${error.message}")
            }
        )

        // Add the request to the queue
        requestQueue.add(jsonRequest2)
    }
}

fun mainPageValueFitting(lat: String, lng: String, city: String, state: String, requestQueue: RequestQueue, binding: MainLayoutBinding, activity: MainActivity): Array<String> {
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
                    humidity = values.getString("humidity").toFloat().roundToInt().toString() + "%"
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
                    precipitation = values.getString("precipitationProbability").toFloat().roundToInt().toString() + "%"

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
class MainActivity : ComponentActivity() {
    private lateinit var binding: MainLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setTheme(R.style.Theme_WeatherViewer)
        setContentView(view)
        // set the loading UIs visible
        binding.progressBar.visibility = View.VISIBLE;
        binding.loadingText.visibility = View.VISIBLE;
        binding.searchPageUi.root.visibility = View.GONE;
        binding.searchResult.root.visibility = View.GONE;
        binding.mainPageUi.root.visibility = View.VISIBLE;
        binding.mainWeekContent.root.visibility = View.GONE;
        // set the onclick event to the search button
        binding.mainPageUi.imageButton.setOnClickListener{
            binding.mainPageUi.root.visibility = View.GONE
            binding.searchPageUi.root.visibility = View.VISIBLE
        }
        binding.searchPageUi.backButton.setOnClickListener{
            binding.mainPageUi.root.visibility = View.VISIBLE
            binding.searchPageUi.root.visibility = View.GONE
        }

        // set up the volley request queue
        val requestQueue = Volley.newRequestQueue(this)

        // set up the search query
        val from = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
        val to = intArrayOf(R.id.searchItemID)
        val searchView = binding.searchPageUi.searchView
        //val suggestions = listOf("Apple", "Banana", "Cherry", "Dog", "Elephant")
        val cursorAdapter = SimpleCursorAdapter(this, R.layout.layout_item, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
        searchView.suggestionsAdapter = cursorAdapter


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                val cursor =
                    MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1))

                var suggestions: List<String> = emptyList()
                // send the received input to backend to receive suggestions
                val autoCompleteUrl = "https://multiweatherview1234.wl.r.appspot.com/api/autoComplete/$newText"
                val jsonRequest = JsonObjectRequest(Request.Method.GET, autoCompleteUrl, null,
                    // Response Listener
                    { response ->
                        suggestions = emptyList()
                        val predictions = response.getJSONArray("predictions")
                        var iteration_num = 5
                        if (predictions.length() < 5) {
                            iteration_num = predictions.length()
                        }
                        for (i in 0..<iteration_num) {
                            val prediction = predictions.getJSONObject(i)
                            val terms = prediction.getJSONArray("terms")
                            val cityterm = terms.getJSONObject(0).getString("value")
                            val stateterm = terms.getJSONObject(1).getString("value")
                            val newSuggestion = "$cityterm, $stateterm"
                            val newSuggestions = suggestions + newSuggestion
                            suggestions = newSuggestions
                        }
                        newText?.let {
                            suggestions.forEachIndexed { index, suggestion ->
                                Log.d("success", "New suggestions coming")
                                //if (suggestion.contains(newText, true))
                                cursor.addRow(arrayOf(index, suggestion))
                            }
                        }
                        cursorAdapter.changeCursor(cursor)
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
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
                val selection =
                    cursor.getString(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1))
                searchView.setQuery(selection, false)

                // Do whatever you want with selection text
                val city = selection.split(",")[0].replace(" ", "")
                val state = selection.split(",")[1]
                val geoCodingUrl = "https://multiweatherview1234.wl.r.appspot.com/api/geocoding/a/$city/$state"
                val stringRequest = StringRequest(Request.Method.GET, geoCodingUrl,
                    // Response Listener
                    { response ->
                        // load the city and state into the main page
                        val frags = response.split(",")
                        val lat = frags[0]
                        val lng = frags[1]
                        val intent = Intent(this@MainActivity, SearchActivity::class.java)
                        intent.putExtra("latlng", "$lat,$lng")
                        intent.putExtra("city", city)
                        intent.putExtra("state", state)
                        startActivity(intent)
                        // This is the response you will get as a string
                        Log.d("VolleyResponse", "Response: $response")
                    },
                    // Error Listener
                    { error ->
                        // Handle the error if the request fails
                        Log.e("VolleyError", "Error: ${error.message}")
                    }
                )
                requestQueue.add(stringRequest)
                return true
            }

            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }
        })

        var currentCity = ""
        var currentState = ""

        // url that returns string in format lat, lng, city, state
        val ipUrl: String = "https://multiweatherview1234.wl.r.appspot.com/api/autoDetect"
        val stringRequest = StringRequest(Request.Method.GET, ipUrl,
            // Response Listener
            { response ->
                // load the city and state into the main page
                val frags = response.split(",")
                val lat = frags[0]
                val lng = frags[1]
                currentCity = frags[2]
                currentState = frags[3]
                binding.mainWeekContent.mainPageAddress.setText("$currentCity, $currentState")
                mainPageValueFitting(lat, lng, currentCity, currentState, requestQueue, binding,this)

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
        requestQueue.add(stringRequest)
    }
}

fun mainPageValueFitting(lat: String, lng: String, city: String, state: String, requestQueue: RequestQueue, binding: MainLayoutBinding, activity: MainActivity): Array<String> {
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
                    humidity = values.getString("humidity").toFloat().roundToInt().toString() + "%"
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
                    precipitation = values.getString("precipitationProbability").toFloat().roundToInt().toString() + "%"

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
 */