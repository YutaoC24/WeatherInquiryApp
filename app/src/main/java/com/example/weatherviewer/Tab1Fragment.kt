package com.example.weatherviewer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView

class Tab1Fragment : Fragment(R.layout.fragment_tab1) {
    companion object {
        private const val ARG_PARAM = "details"

        // Function to create a new instance of the fragment with arguments
        fun newInstance(param: Array<String>): Tab1Fragment {
            val fragment = Tab1Fragment()
            val args = Bundle()
            args.putStringArray(ARG_PARAM, param)  // Store the parameter in the Bundle
            fragment.arguments = args  // Set the arguments to the fragment
            return fragment
        }
    }
    /*
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inf = inflater.inflate(R.layout.fragment_tab1, container, false)
        inf.findViewById<EditText>(R.id.windSpeedToday).setText("wowow")
        return inf
    }
    */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val details: Array<out String>? = arguments?.getStringArray(ARG_PARAM)
        if (details != null) {
            // Retrieve the data from arguments
            val windSpeed = details[0]
            val pressure = details[1]
            val precip = details[2]
            val temp = details[3]
            val weather = details[4]
            val weatherSrc = details[5]
            val hum = details[6]
            val vis = details[7]
            val cc = details[8]
            val ozone = details[9]
            // Use the data (e.g., display it in a TextView)
            view.findViewById<EditText>(R.id.windSpeedToday).setText(windSpeed)
            view.findViewById<EditText>(R.id.pressureToday).setText(pressure)
            view.findViewById<EditText>(R.id.precepToday).setText(precip)
            view.findViewById<EditText>(R.id.tempToday).setText(temp)
            view.findViewById<EditText>(R.id.weatherToday).setText(weather)
            view.findViewById<ImageView>(R.id.imageView7).setImageResource(weatherSrc.toInt())
            view.findViewById<EditText>(R.id.humToday).setText(hum)
            view.findViewById<EditText>(R.id.visToday).setText(vis)
            view.findViewById<EditText>(R.id.ccToday).setText(cc)
            view.findViewById<EditText>(R.id.ozoneToday).setText(ozone)
        }
    }
}