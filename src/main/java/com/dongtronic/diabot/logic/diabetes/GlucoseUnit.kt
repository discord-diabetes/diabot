package com.dongtronic.diabot.logic.diabetes

enum class GlucoseUnit(internal var unit: String) {
    MMOL("mmol/L"), MGDL("mg/dL"), AMBIGUOUS("Ambiguous")
}
