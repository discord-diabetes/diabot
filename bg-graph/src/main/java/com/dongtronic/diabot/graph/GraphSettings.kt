package com.dongtronic.diabot.graph

import java.awt.Color

data class GraphSettings (
        val plotMode: PlottingStyle,
        val highColour: Color = Color(255, 140, 0),
        val inRangeColour: Color = Color(0, 203, 255),
        val lowColour: Color = Color(255, 0, 0)
)