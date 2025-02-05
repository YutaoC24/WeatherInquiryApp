package com.example.weatherviewer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TabCurrentFrag.newInstance] factory method to
 * create an instance of this fragment.
 */
class TabCurrentFrag : Fragment() {
    // TODO: Rename and change types of parameters
    private var currentInfo: String? = null
    private var isFav: Boolean = true

    private var _binding: WeekContentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentInfo = it.getString(ARG_PARAM1)
            isFav = it.getBoolean(ARG_PARAM2)
        }
    }

    fun removeFav(binding: WeekContentBinding, activity: FragmentActivity, city: String, state: String, lat: String, lng: String, requestQueue: RequestQueue) {
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
        binding.searchfavtabMain.setImageResource(requireActivity().resources.getIdentifier("add_fav", "drawable", requireActivity().packageName))
        binding.searchfavtabMain.setOnClickListener {
            addFav(binding, activity, city, state, lat, lng, requestQueue)
            Toast.makeText(activity, "$city was added to favorites!",
                Toast.LENGTH_LONG).show()
        }
    }

    fun addFav(binding: WeekContentBinding, activity: FragmentActivity, city: String, state: String, lat: String, lng: String, requestQueue: RequestQueue) {
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
        binding.searchfavtabMain.setImageResource(requireActivity().resources.getIdentifier("rem_fav", "drawable", requireActivity().packageName))
        binding.searchfavtabMain.setOnClickListener {
            removeFav(binding, activity, city, state, lat, lng, requestQueue)
            Toast.makeText(activity, "$city was removed from favorites!",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = WeekContentBinding.inflate(inflater, container, false)
        binding.progressBar3.visibility = View.VISIBLE;
        binding.loadingText3.visibility = View.VISIBLE;
        binding.mainPageData.visibility = View.GONE;
        //return inflater.inflate(R.layout.week_content, container, false)

        // set up the volley request queue
        val requestQueue = Volley.newRequestQueue(requireActivity())

        if (currentInfo != null) {
            val city = currentInfo!!.split(",")[0]
            val state = currentInfo!!.split(",")[1]
            val lat = currentInfo!!.split(",")[2]
            val lng = currentInfo!!.split(",")[3]

            // check if current tab is favorite tab
            if (isFav) {
                binding.searchfavtabMain.visibility = View.VISIBLE;
                // start with the remove icon
                binding.searchfavtabMain.setOnClickListener {
                    removeFav(binding, requireActivity(), city, state, lat, lng, requestQueue)
                    Toast.makeText(activity, "$city was removed from favorites!",
                        Toast.LENGTH_LONG).show()
                }
            } else {
                binding.searchfavtabMain.visibility = View.GONE;
            }

            // set city and state
            binding.mainPageAddress.setText("$city, $state")

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

            val jsonRequest = JsonObjectRequest(
                Request.Method.GET, weekDataUrl, null,
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
                            binding.mainPageTemp.setText("$tempFloat°F")
                            temperature = tempFloat

                            // set the weather code and the description
                            val weatherCode = values.getString("weatherCode")
                            val descriptionFull: String = weatherCodes[weatherCode].toString()
                            val description = descriptionFull.split(",")[0]
                            weather = description
                            val src = descriptionFull.split(",")[1]
                            val resId = requireActivity().resources.getIdentifier(src, "drawable", requireActivity().packageName)
                            binding.mainPageWeatherIcon.setImageResource(resId)
                            binding.mainPageWeather.setText(description)
                            weatherSrc = resId.toString()

                            // set the humidity
                            humidity = values.getString("humidity").toFloat().roundToInt().toString() + "%"
                            binding.mainPageHumidty.setText(humidity)

                            // set the wind speed
                            windSpeed = values.getString("windSpeed") + "mph"
                            binding.mainPageWindSpeed.setText(windSpeed)

                            // set the visibility
                            visibility = values.getString("visibility") + "mi"
                            binding.mainPageVisibility.setText(visibility)

                            // set the pressure
                            //val pressure = values.getString("pressureSeaLevel") + "inHg"
                            binding.mainPagePressure.setText("30.0inHg")
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
                            val resId = requireActivity().resources.getIdentifier(src, "drawable", requireActivity().packageName)

                            // get the low and high temp
                            val highTemp = values.getString("temperatureMax").toFloat().roundToInt().toString()
                            val lowTemp = values.getString("temperatureMin").toFloat().roundToInt().toString()
                            val currentTime = dateTime.time.toString()
                            temps += "$currentTime,$highTemp,$lowTemp"
                            if (i == 1) {
                                binding.mainPageDate1.setText(formatedDate)
                                binding.mainPageWeatherSmall1.setImageResource(resId)
                                binding.mainPageHighTemp1.setText(highTemp)
                                binding.mainPageLowTemp1.setText(lowTemp)
                            }
                            if (i == 2) {
                                binding.mainPageDate2.setText(formatedDate)
                                binding.mainPageWeatherSmall2.setImageResource(resId)
                                binding.mainPageHighTemp2.setText(highTemp)
                                binding.mainPageLowTemp2.setText(lowTemp)
                            }
                            if (i == 3) {
                                binding.mainPageDate3.setText(formatedDate)
                                binding.mainPageWeatherSmall3.setImageResource(resId)
                                binding.mainPageHighTemp3.setText(highTemp)
                                binding.mainPageLowTemp3.setText(lowTemp)
                            }
                            if (i == 4) {
                                binding.mainPageDate4.setText(formatedDate)
                                binding.mainPageWeatherSmall4.setImageResource(resId)
                                binding.mainPageHighTemp4.setText(highTemp)
                                binding.mainPageLowTemp4.setText(lowTemp)
                            }
                            if (i == 5) {
                                binding.mainPageDate5.setText(formatedDate)
                                binding.mainPageWeatherSmall5.setImageResource(resId)
                                binding.mainPageHighTemp5.setText(highTemp)
                                binding.mainPageLowTemp5.setText(lowTemp)
                            }
                        }
                    }
                    binding.progressBar3.visibility = View.GONE;
                    binding.loadingText3.visibility = View.GONE;
                    binding.mainPageData.visibility = View.VISIBLE;
                    //binding.invisible_row.visibility = View.INVISIBLE;
                    // This is the response you will get as a string
                    Log.d("VolleyResponse", "Response: $response")
                    val currentWeather = arrayOf(windSpeed, pressure, precipitation, temperature, weather, weatherSrc, humidity, visibility, cloudCover, uv)
                    // set the ripple effect to the card
                    binding.card.setOnClickListener {
                        val intent = Intent(activity, DetailsActivity::class.java)
                        intent.putExtra("latlng", "$lat,$lng")
                        intent.putExtra("city", city)
                        intent.putExtra("state", state)
                        intent.putExtra("details", currentWeather)
                        intent.putExtra("temps", temps)
                        requireActivity().startActivity(intent)
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
        }

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment TabCurrentFrag.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: Boolean) =
            TabCurrentFrag().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putBoolean(ARG_PARAM2, param2)
                }
            }
    }
}