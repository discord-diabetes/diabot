package com.dongtronic.diabot.converters

enum class GlucoseUnit(internal var unit: String) {
    MMOL("mmol/L"), MGDL("mg/dL"), AMBIGUOUS("Ambiguous")
}
