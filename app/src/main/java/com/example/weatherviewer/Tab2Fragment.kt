package com.example.weatherviewer

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.highsoft.highcharts.common.HIColor
import com.highsoft.highcharts.common.HIGradient
import com.highsoft.highcharts.common.HIStop
import com.highsoft.highcharts.common.hichartsclasses.HIArearange
import com.highsoft.highcharts.common.hichartsclasses.HIChart
import com.highsoft.highcharts.common.hichartsclasses.HILegend
import com.highsoft.highcharts.common.hichartsclasses.HIOptions
import com.highsoft.highcharts.common.hichartsclasses.HITitle
import com.highsoft.highcharts.common.hichartsclasses.HITooltip
import com.highsoft.highcharts.common.hichartsclasses.HIXAxis
import com.highsoft.highcharts.common.hichartsclasses.HIYAxis
import com.highsoft.highcharts.core.HIChartView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.LinkedList
import java.util.Locale


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Tab2Fragment : Fragment(R.layout.fragment_tab2) {
    companion object {
        private const val ARG_PARAM = "temps"

        // Function to create a new instance of the fragment with arguments
        fun newInstance(param: ArrayList<String>): Tab2Fragment {
            val fragment = Tab2Fragment()
            val args = Bundle()
            args.putStringArrayList(ARG_PARAM, param)  // Store the parameter in the Bundle
            fragment.arguments = args  // Set the arguments to the fragment
            return fragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val temps = arguments?.getStringArrayList(ARG_PARAM)

        var seriesData2 = ArrayList<FloatArray>()
        if (temps != null) {
            for (i in temps) {
                val temp = i.split(",")
                seriesData2.add(floatArrayOf(temp[0].toFloat(), temp[2].toFloat(), temp[1].toFloat()))
            }
        }

        // Find the HighChartView in the layout
        val chartView = view.findViewById<HIChartView>(R.id.highChartView1)

        // Create chart options
        val chart = HIChart().apply{
            type = "arearange"
        }
        //chart.zooming.type = "x"

        // Set up the chart options with title and series
        val options = HIOptions()
        options.chart = chart
        if (chart.zooming != null) {
            chart.zooming.apply {
                type = "x"  // Enables zooming along the x-axis (time axis)
            }
        }

        // set up the HITitle
        val title = HITitle()
        title.text = "Temperature variation by day"
        options.title = title

        // set the HixAxis
        val HIXAxis = HIXAxis()
        HIXAxis.type = "datetime"
        val HIXAxisList = ArrayList<HIXAxis>()
        HIXAxisList.add(HIXAxis)
        options.xAxis = HIXAxisList;

        // set the HiyAxis
        val HIYAxis = HIYAxis()
        HIYAxis.title = HITitle()
        val HIYAxisList = ArrayList<HIYAxis>()
        HIYAxisList.add(HIYAxis)
        options.yAxis = HIYAxisList

        val tooltip = HITooltip()
        tooltip.valueSuffix = "°F"
        tooltip.shared = true
        tooltip.xDateFormat = "%A, %b %e"
        options.tooltip = tooltip

        // Create series data
        val legend = HILegend()
        legend.enabled = false
        options.legend = legend

        val series = HIArearange()
        series.name = "Temperatures"
        val seriesData = listOf(
            floatArrayOf(1388624400000F, 1.1F, 4.7F),
            floatArrayOf(1418864400000F, -1.1F, 1.5F)
            //, listOf(1388624400000L, 1.8, 6.4)
        )
        //series.data = ArrayList(seriesData)
        series.data = seriesData2
        options.series = ArrayList(Arrays.asList(series))

        val gradient = HIGradient(0F, 0F, 0F, 1F)
        val color = HIColor.initWithHexValue("FFA500")
        val color2 = HIColor.initWithHexValue("75abf4")
        val stop1 = HIStop(0F, color)
        val stop2 = HIStop(1F, color2)
        val stops = LinkedList<HIStop>()
        stops.add(stop1)
        stops.add(stop2)
        val color3 = HIColor.initWithLinearGradient(gradient, stops)
        series.color = color3


        // Set the options to the chart
        chartView.options = options

        chartView.layoutParams.height = 1400 // Set the height in pixels
        chartView.requestLayout()

        //Thread.sleep(500)
        view.findViewById<ProgressBar>(R.id.progressBar2).visibility = View.GONE
        view.findViewById<EditText>(R.id.loadingText2).visibility = View.GONE
    }
}