package com.dongtronic.diabot.data

import com.dongtronic.diabot.logic.diabetes.GlucoseUnit

class A1cDTO(
        val original: ConversionDTO,
        dcctMgdl: Double,
        ifccMgdl: Double,
        dcctMmol: Double,
        ifccMmol: Double) {
    private val dcctMgdl: Double
    private val ifccMgdl: Double
    private val dcctMmol: Double
    private val ifccMmol: Double

    val dcct: Double
        get() = when (original.inputUnit) {
            GlucoseUnit.MMOL -> dcctMmol
            GlucoseUnit.MGDL -> dcctMgdl
            else -> throw IllegalStateException("Unknown input unit: ${original.inputUnit}")
        }

    val ifcc: Double
        get() = when (original.inputUnit) {
            GlucoseUnit.MMOL -> ifccMmol
            GlucoseUnit.MGDL -> ifccMgdl
            else -> throw IllegalStateException("Unknown input unit: ${original.inputUnit}")
        }


    init {
        this.dcctMgdl = round(dcctMgdl, 1)
        this.ifccMgdl = round(ifccMgdl, 1)
        this.dcctMmol = round(dcctMmol, 1)
        this.ifccMmol = round(ifccMmol, 1)
    }

    fun getDcct(unit: GlucoseUnit): Double {
        return when (unit) {
            GlucoseUnit.MMOL -> dcctMmol
            GlucoseUnit.MGDL -> dcctMgdl
            else -> throw IllegalStateException("Unknown glucose unit: $unit")
        }
    }

    fun getIfcc(unit: GlucoseUnit): Double {
        return when (unit) {
            GlucoseUnit.MMOL -> ifccMmol
            GlucoseUnit.MGDL -> ifccMgdl
            else -> throw IllegalStateException("Unknown glucose unit: $unit")
        }
    }

    private fun round(value: Double, precision: Int): Double {
        val scale = Math.pow(10.0, precision.toDouble()).toInt()
        return Math.round(value * scale).toDouble() / scale
    }
}
