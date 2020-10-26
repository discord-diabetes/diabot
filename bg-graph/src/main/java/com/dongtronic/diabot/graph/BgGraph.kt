package com.dongtronic.diabot.graph

import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import com.dongtronic.nightscout.data.BgEntry
import com.dongtronic.nightscout.data.NightscoutDTO
import org.knowm.xchart.XYChart
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.Circle
import java.awt.BasicStroke
import java.awt.Color
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

// todo:
// - implement customisation options for graph settings
// - light theme
// - handle errors with fetching data/generating graph
class BgGraph(
        private val settings: GraphSettings,
        width: Int = 833,
        height: Int = 500
) : XYChart(width, height) {
    init {
        styler.theme = settings.theme.clazz
        styler.isLegendVisible = false
    }

    /**
     * Sets up the chart's axes for BG data.
     *
     * @param units The glucose units which are preferred
     */
    fun setupChartAxes(units: String) {
        val preferredUnit = GlucoseUnit.byName(units) ?: GlucoseUnit.MMOL
        setupChartAxes(preferredUnit)
    }

    /**
     * Sets up the chart's axes for BG data.
     *
     * @param preferredUnit The glucose units which are preferred
     */
    fun setupChartAxes(preferredUnit: GlucoseUnit) {
        requireNonAmbiguous(preferredUnit)
        // use the preferred unit's axis as y axis group 0.
        // axis group 0 will be used for creating tick marks on the y-axis for the graph.
        val mmolGroup = if (preferredUnit == GlucoseUnit.MMOL) 0 else 1
        val mgdlGroup = if (preferredUnit == GlucoseUnit.MGDL) 0 else 1

        styler.xAxisDecimalPattern = "0.#h"
        styler.xAxisTickLabelsColor = Color(88, 88, 88)

        styler.isPlotGridVerticalLinesVisible = false
        styler.plotGridLinesStroke = BasicStroke()

        setYAxisGroupTitle(mgdlGroup, "MG/DL")
        setYAxisGroupTitle(mmolGroup, "MMOL/L")
        // always put mmol on the right axis
        styler.setYAxisGroupPosition(mmolGroup, Styler.YAxisPosition.Right)
    }

    /**
     * Adds a [NightscoutDTO]'s BG entries to the chart
     *
     * @param nightscout The Nightscout data to add to the chart
     */
    fun addEntries(nightscout: NightscoutDTO) {
        setupChartAxes(nightscout.units)
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
                    ?: seriesMap.isEmpty()

            // don't display mmol/l series on the graph since they're less precise compared to mg/dl
            val hidden = unit == GlucoseUnit.MMOL

            ranges.forEach { (rangeColour, entries) ->
                val data = getSeriesData(entries, unit)
                val colour = if (hidden) Color(0, 0, 0, 0) else rangeColour

                val xySeries = addSeries(UUID.randomUUID().toString(), data.keys.toList(), data.values.toList())

                if (settings.plotMode == PlottingStyle.LINE) {
                    // set the line colour if using line graph
                    xySeries.lineColor = colour
                }

                xySeries.marker = Circle()
                xySeries.markerColor = colour

                // axis group 0 will be used for creating lines on the graph.
                // the series which use the preferred glucose unit will then base line creation off the tick labels for this unit
                xySeries.yAxisGroup = if (preferredUnits) 0 else 1
                xySeries.xySeriesRenderStyle = settings.plotMode.renderStyle

                val scale = ScalingUtil.findMinMax(readings, unit)
                styler.setYAxisMin(xySeries.yAxisGroup, scale.first)
                styler.setYAxisMax(xySeries.yAxisGroup, scale.second)
            }
        }
    }

    /**
     * Split and convert a list of [BgEntry]s into a [Map] consisting of the relative BG timestamp in hours and
     * the BG value in the specified units.
     *
     * @param readings List of [BgEntry]s to split and convert
     * @param unit The glucose unit to display the SGVs as. This cannot be [GlucoseUnit.AMBIGUOUS].
     * @return [Map] of relative timestamp in hours and glucose reading(s)
     */
    private fun getSeriesData(readings: List<BgEntry>, unit: GlucoseUnit): Map<Double, Number> {
        requireNonAmbiguous(unit)

        return readings.associate { bgEntry ->
            val glucose: Number = when(unit) {
                GlucoseUnit.MGDL -> bgEntry.glucose.mgdl
                GlucoseUnit.MMOL -> bgEntry.glucose.mmol
                else -> 0
            }

            val relativeSeconds = Instant.now().until(bgEntry.dateTime, ChronoUnit.SECONDS)
            // hours are used (instead of seconds) because of the decimal formatter.
            // this allows for only the whole number of the hour to be displayed on the chart, while also sorting each reading:
            // 2.47 hours (with a decimal format pattern of "0") -> 2h
            val relativeHours = relativeSeconds.toDouble()/3600
            relativeHours to glucose
        }
    }

    /**
     * Helper function for requiring a [GlucoseUnit] object to not be [GlucoseUnit.AMBIGUOUS]
     *
     * @param glucoseUnit [GlucoseUnit] to ensure is not ambiguous
     */
    private fun requireNonAmbiguous(glucoseUnit: GlucoseUnit) =
            require(glucoseUnit != GlucoseUnit.AMBIGUOUS) { "Glucose unit cannot be ambiguous" }
}