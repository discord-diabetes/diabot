package com.dongtronic.diabot.graph

import com.fasterxml.jackson.annotation.JsonInclude
import java.awt.Color

@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
data class GraphSettings(
    val plotMode: PlottingStyle = PlottingStyle.SCATTER,
    val theme: GraphTheme = GraphTheme.DARK,
    val hours: Long = 4,
    // for dark theme
    val highDarkColor: Color = Color(255, 140, 0),
    val inRangeDarkColor: Color = Color(0, 203, 255),
    val lowDarkColor: Color = Color(255, 0, 0),
    // for light theme
    val highLightColor: Color = Color(232, 97, 36),
    val inRangeLightColor: Color = Color(0, 123, 255),
    val lowLightColor: Color = Color(182, 0, 0)
)
