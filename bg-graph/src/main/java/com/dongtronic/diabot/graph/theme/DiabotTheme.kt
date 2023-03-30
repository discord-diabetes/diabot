package com.dongtronic.diabot.graph.theme

import com.dongtronic.diabot.graph.BgGraph
import org.knowm.xchart.internal.chartpart.Plot_
import org.knowm.xchart.style.theme.XChartTheme
import java.awt.Graphics2D

open class DiabotTheme(val overridePaint: Boolean = false) : XChartTheme() {
    open fun customPaint(g: Graphics2D, chart: BgGraph, plot: Plot_<*, *>) { }

    open fun getImageWidth(chart: BgGraph): Int = chart.width

    open fun getImageHeight(chart: BgGraph): Int = chart.height
}
