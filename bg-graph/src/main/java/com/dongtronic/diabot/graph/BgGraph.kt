package com.dongtronic.diabot.graph

import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import com.dongtronic.nightscout.data.BgEntry
import com.dongtronic.nightscout.data.NightscoutDTO
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.XYSeries
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.Circle
import java.awt.BasicStroke
import java.awt.Color
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

// todo:
// - documentation
// - implement customisation options for graph settings
// - light theme
// - handle errors with fetching data/generating graph
object BgGraph {
    fun buildInitialChart(graphSettings: GraphSettings): XYChart {
        val chart = XYChartBuilder()
                .xAxisTitle("Time")
                .height(500)
                .width(833)
                .build()
        chart.styler.theme = DiabotTheme()
        chart.styler.defaultSeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Scatter
        chart.styler.isXAxisTitleVisible = false
        chart.styler.xAxisMaxLabelCount = 4
        chart.styler.xAxisDecimalPattern = "0.#h"
        chart.styler.xAxisTickLabelsColor = Color(88, 88, 88)
        chart.styler.isLegendVisible = false
        chart.styler.isPlotGridVerticalLinesVisible = false
        chart.styler.plotGridLinesStroke = BasicStroke()
        return chart
    }

    fun addEntries(nightscout: NightscoutDTO, settings: GraphSettings, chart: XYChart) {
        setupChartAxes(nightscout, chart)
        val readings = nightscout.entries.toList()

        val ranges = mutableMapOf<Color, List<BgEntry>>()

        readings.forEach {
            val mgdl = it.glucose.mgdl
            val color = when {
                // custom colours are not supported in line mode
                settings.plotMode != PlottingStyle.SCATTER -> settings.inRangeColour

                mgdl > nightscout.top -> settings.highColour
                mgdl < nightscout.bottom -> settings.lowColour
                else -> settings.inRangeColour
            }

            ranges.merge(color, listOf(it)) { oldList: List<BgEntry>, newList: List<BgEntry> ->
                oldList.plus(newList)
            }
        }

        GlucoseUnit.values().forEach { unit ->
            if (unit == GlucoseUnit.AMBIGUOUS)
                return@forEach

            // check if these bg values are in the units which are in use by the nightscout instance.
            //
            // if the nightscout instance does not have default units (either the settings for this instance have not
            // been fetched or something went horribly wrong): default to preferred if there's no data in the chart.
            val preferredUnits = GlucoseUnit.byName(nightscout.units)?.let { it == unit }
                    ?: chart.seriesMap.isEmpty()

            // don't display mmol/l series on the graph since they're less precise compared to mg/dl
            val hidden = unit == GlucoseUnit.MMOL

            val series = ranges.map {
                val data = getSeriesData(it.value, unit)
                addSeries(it.key, data, chart)
            }

            series.forEach { xySeries ->
                if (hidden) {
                    xySeries.markerColor = Color(0, 0, 0, 0)
                    xySeries.lineColor = Color(0, 0, 0, 0)
                }

                // axis group 0 will be used for creating lines on the graph.
                // the series which use the preferred glucose unit will then base line creation off the tick labels for this unit
                xySeries.yAxisGroup = if (preferredUnits) 0 else 1
                xySeries.xySeriesRenderStyle = settings.plotMode.renderStyle
                val scale = ScalingUtil.findMinMax(nightscout.entries.toList(), unit)
                chart.styler.setYAxisMin(xySeries.yAxisGroup, scale.first)
                chart.styler.setYAxisMax(xySeries.yAxisGroup, scale.second)

                when (settings.plotMode) {
                    PlottingStyle.SCATTER -> {

                    }
                    PlottingStyle.LINE -> {
                        // set the line colour if using line graph
                        xySeries.lineColor = xySeries.markerColor
                    }
                }
            }
        }
    }

    fun addSeries(color: Color, readings: Map<Double, Number>, chart: XYChart): XYSeries {
        val series = chart.addSeries(UUID.randomUUID().toString(), readings.keys.toList(), readings.values.toList())

        series.marker = Circle()
        series.markerColor = color
        return series
    }

    fun setupChartAxes(nightscout: NightscoutDTO, chart: XYChart) {
        val preferredUnit = GlucoseUnit.byName(nightscout.units) ?: GlucoseUnit.MMOL

        // axis group 0 will be used for creating lines on the graph.
        val mmolGroup = if (preferredUnit == GlucoseUnit.MMOL) 0 else 1
        val mgdlGroup = if (preferredUnit == GlucoseUnit.MGDL) 0 else 1

        chart.setYAxisGroupTitle(mgdlGroup, "MG/DL")
        chart.setYAxisGroupTitle(mmolGroup, "MMOL/L")
        chart.styler.setYAxisGroupPosition(mmolGroup, Styler.YAxisPosition.Right)
    }

    fun getSeriesData(readings: List<BgEntry>, unit: GlucoseUnit): Map<Double, Number> {
        if (unit == GlucoseUnit.AMBIGUOUS)
            throw IllegalArgumentException("Glucose unit cannot be ambiguous")

        return readings.associate { bgEntry ->
            val glucose: Number = when(unit) {
                GlucoseUnit.MGDL -> bgEntry.glucose.mgdl
                GlucoseUnit.MMOL -> bgEntry.glucose.mmol
                else -> 0
            }

            val relativeSeconds = bgEntry.dateTime.until(Instant.now(), ChronoUnit.SECONDS)
            // hours are used (instead of seconds) because of the decimal formatter.
            // this allows for only the whole number of the hour to be displayed on the chart, while also sorting each reading:
            // 2.47 hours (with a decimal format pattern of "0") -> 2h
            val relativeHours = relativeSeconds.toDouble()/3600
//            logger().info("$relativeHours - $glucose")
            -relativeHours to glucose
        }
    }
}