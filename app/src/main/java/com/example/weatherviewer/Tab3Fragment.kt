package com.example.weatherviewer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.weatherviewer.Tab1Fragment.Companion
import com.highsoft.highcharts.common.HIColor
import com.highsoft.highcharts.common.hichartsclasses.HIBackground
import com.highsoft.highcharts.common.hichartsclasses.HICSSObject
import com.highsoft.highcharts.common.hichartsclasses.HIChart
import com.highsoft.highcharts.common.hichartsclasses.HIData
import com.highsoft.highcharts.common.hichartsclasses.HIDataLabels
import com.highsoft.highcharts.common.hichartsclasses.HIEvents
import com.highsoft.highcharts.common.hichartsclasses.HIGauge
import com.highsoft.highcharts.common.hichartsclasses.HIOptions
import com.highsoft.highcharts.common.hichartsclasses.HIPane
import com.highsoft.highcharts.common.hichartsclasses.HIPlotOptions
import com.highsoft.highcharts.common.hichartsclasses.HISolidgauge
import com.highsoft.highcharts.common.hichartsclasses.HITitle
import com.highsoft.highcharts.common.hichartsclasses.HITooltip
import com.highsoft.highcharts.common.hichartsclasses.HIYAxis
import com.highsoft.highcharts.core.HIChartView
import com.highsoft.highcharts.core.HIFunction

class Tab3Fragment : Fragment(R.layout.fragment_tab3) {
    companion object {
        private const val ARG_PARAM = "details"

        // Function to create a new instance of the fragment with arguments
        fun newInstance(param: Array<String>): Tab3Fragment {
            val fragment = Tab3Fragment()
            val args = Bundle()
            args.putStringArray(ARG_PARAM, param)  // Store the parameter in the Bundle
            fragment.arguments = args  // Set the arguments to the fragment
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the argument (parameter) passed to the fragment
        val details: Array<out String>? = arguments?.getStringArray(ARG_PARAM)
        if (details != null) {
            val precip = details[2]
            val hum = details[6]
            val cc = details[8]

            val highChartView: HIChartView = view.findViewById(R.id.highChartView2)
            val chart = HIChart()
            chart.type = "solidgauge"
            chart.events = HIEvents()
            val renderIconsString = "function renderIcons() {" +
                    "                            if(!this.series[0].icon) {" +
                    "                               this.series[0].icon = this.renderer.path(['M', -8, 0, 'L', 8, 0, 'M', 0, -8, 'L', 8, 0, 0, 8]).attr({'stroke': '#303030','stroke-linecap': 'round','stroke-linejoin': 'round','stroke-width': 2,'zIndex': 10}).add(this.series[2].group);}this.series[0].icon.translate(this.chartWidth / 2 - 10,this.plotHeight / 2 - this.series[0].points[0].shapeArgs.innerR -(this.series[0].points[0].shapeArgs.r - this.series[0].points[0].shapeArgs.innerR) / 2); if(!this.series[1].icon) {this.series[1].icon = this.renderer.path(['M', -8, 0, 'L', 8, 0, 'M', 0, -8, 'L', 8, 0, 0, 8,'M', 8, -8, 'L', 16, 0, 8, 8]).attr({'stroke': '#ffffff','stroke-linecap': 'round','stroke-linejoin': 'round','stroke-width': 2,'zIndex': 10}).add(this.series[2].group);}this.series[1].icon.translate(this.chartWidth / 2 - 10,this.plotHeight / 2 - this.series[1].points[0].shapeArgs.innerR -(this.series[1].points[0].shapeArgs.r - this.series[1].points[0].shapeArgs.innerR) / 2); if(!this.series[2].icon) {this.series[2].icon = this.renderer.path(['M', 0, 8, 'L', 0, -8, 'M', -8, 0, 'L', 0, -8, 8, 0]).attr({'stroke': '#303030','stroke-linecap': 'round','stroke-linejoin': 'round','stroke-width': 2,'zIndex': 10}).add(this.series[2].group);}this.series[2].icon.translate(this.chartWidth / 2 - 10,this.plotHeight / 2 - this.series[2].points[0].shapeArgs.innerR -(this.series[2].points[0].shapeArgs.r - this.series[2].points[0].shapeArgs.innerR) / 2);}";

            chart.events.render = HIFunction(renderIconsString)
            val options = HIOptions()
            options.chart = chart

            val title = HITitle()
            title.text = "Stat Summary"
            title.style = HICSSObject()
            title.style.fontSize = "24px"
            options.title = title

            val tooltip = HITooltip()
            tooltip.borderWidth = 0
            if (tooltip.style != null) {
                tooltip.style.fontSize = "16px"
            }

            tooltip.pointFormat = "{series.name}<br><span style=\"font-size:2em; color: {point.color}; font-weight: bold\">{point.y}%</span>"
            tooltip.positioner.apply {
                HIFunction(
                    "function (labelWidth) {" +
                            "   return {" +
                            "       x: (this.chart.chartWidth - labelWidth) /2," +
                            "       y: (this.chart.plotHeight / 2) + 15" +
                            "   };" +
                            "}"
                )
            }
            options.tooltip = tooltip

            val pane = HIPane()
            pane.startAngle = 0
            pane.endAngle = 360

            val paneBackground1 = HIBackground()
            paneBackground1.outerRadius = "112%"
            paneBackground1.innerRadius = "88%"
            paneBackground1.backgroundColor = HIColor.initWithRGBA(130, 238, 106, 0.35)
            paneBackground1.borderWidth = 0

            val paneBackground2 = HIBackground()
            paneBackground2.outerRadius = "87%"
            paneBackground2.innerRadius = "63%"
            paneBackground2.backgroundColor = HIColor.initWithRGBA(106, 165, 231, 0.35)
            paneBackground2.borderWidth = 0

            val paneBackground3 = HIBackground()
            paneBackground3.outerRadius = "62%"
            paneBackground3.innerRadius = "38%"
            paneBackground3.backgroundColor = HIColor.initWithRGBA(255, 129, 93, 0.35)
            paneBackground3.borderWidth = 0

            pane.background = ArrayList(listOf(paneBackground1, paneBackground2, paneBackground3))
            var panes = ArrayList<HIPane>()
            panes.add(pane)
            options.pane = panes

            val yAxis = HIYAxis()
            yAxis.min = 0
            yAxis.max = 100
            yAxis.lineWidth = 0
            yAxis.tickPositions = ArrayList<Number>()
            var ys = ArrayList<HIYAxis>()
            ys.add(yAxis)
            options.yAxis = ys

            val plotOptions = HIPlotOptions()
            plotOptions.solidgauge = HISolidgauge()
            plotOptions.solidgauge.dataLabels = ArrayList<HIDataLabels>()
            var dataL = HIDataLabels()
            dataL.enabled = false
            plotOptions.solidgauge.dataLabels.add((dataL))
            plotOptions.solidgauge.linecap = "round"
            plotOptions.solidgauge.stickyTracking = false
            plotOptions.solidgauge.rounded = true
            options.plotOptions = plotOptions

            var solidGauge1 = HISolidgauge()
            solidGauge1.name = "Cloud Cover"
            var data1 = HIData()
            data1.color = HIColor.initWithRGB(130, 238, 106)
            data1.radius = "112%"
            data1.innerRadius = "88%"
            data1.y = cc.split('%')[0].toInt()
            var datas1 = ArrayList<HIData>()
            datas1.add(data1)
            solidGauge1.data = datas1

            var solidGauge2 = HISolidgauge()
            solidGauge2.name = "Precipitation"
            var data2 = HIData()
            data2.color = HIColor.initWithRGB(106, 165, 231)
            data2.radius = "87%"
            data2.innerRadius = "63%"
            data2.y = precip.split('%')[0].toInt()
            var datas2 = ArrayList<HIData>()
            datas2.add(data2)
            solidGauge2.data = datas2

            var solidGauge3 = HISolidgauge()
            solidGauge3.name = "Humidity"
            var data3 = HIData()
            data3.color = HIColor.initWithRGB(255, 129, 93)
            data3.radius = "62%"
            data3.innerRadius = "38%"
            data3.y = hum.split('%')[0].toInt()
            var datas3 = ArrayList<HIData>()
            datas3.add(data3)
            solidGauge3.data = datas3

            options.series = ArrayList(listOf(solidGauge1, solidGauge2, solidGauge3))
            highChartView.options = options

            highChartView.layoutParams.height = 1400 // Set the height in pixels
            highChartView.requestLayout()
        }

    }
}