package com.dongtronic.diabot.graph

import java.awt.Color

data class GraphSettings (
        val plotMode: PlottingStyle = PlottingStyle.SCATTER,
        val theme: GraphTheme = GraphTheme.DARK,
        // for dark theme
        val highDarkColour: Color = Color(255, 140, 0),
        val inRangeDarkColour: Color = Color(0, 203, 255),
        val lowDarkColour: Color = Color(255, 0, 0),
        // for light theme
        val highLightColour: Color = Color(232, 97, 36),
        val inRangeLightColour: Color = Color(0, 123, 255),
        val lowLightColour: Color = Color(182, 0, 0)
)