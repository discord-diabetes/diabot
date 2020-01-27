package com.dongtronic.diabot.data

import com.dongtronic.diabot.logic.diabetes.GlucoseUnit

class A1cDTO(//region properties
        val original: ConversionDTO, dcct_mgdl: Double, ifcc_mgdl: Double, dcct_mmol: Double, ifcc_mmol: Double) {
    private val dcctMgdl: Double
    private val ifccMgdl: Double
    private val dcctMmol: Double
    private val ifccMmol: Double

    val dcct: Double
        get() = when {
            original.inputUnit == GlucoseUnit.MMOL -> dcctMmol
            original.inputUnit == GlucoseUnit.MGDL -> dcctMgdl
            else -> throw IllegalStateException()
        }

    val ifcc: Double
        get() = when {
            original.inputUnit == GlucoseUnit.MMOL -> ifccMmol
            original.inputUnit == GlucoseUnit.MGDL -> ifccMgdl
            else -> throw IllegalStateException()
        }


    init {

        this.dcctMgdl = round(dcct_mgdl, 1)
        this.ifccMgdl = round(ifcc_mgdl, 1)
        this.dcctMmol = round(dcct_mmol, 1)
        this.ifccMmol = round(ifcc_mmol, 1)
    }

    fun getDcct(unit: GlucoseUnit): Double {
        return when (unit) {
            GlucoseUnit.MMOL -> dcctMmol
            GlucoseUnit.MGDL -> dcctMgdl
            else -> throw IllegalArgumentException()
        }
    }

    fun getIfcc(unit: GlucoseUnit): Double {
        return when (unit) {
            GlucoseUnit.MMOL -> ifccMmol
            GlucoseUnit.MGDL -> ifccMgdl
            else -> throw IllegalArgumentException()
        }
    }


    //endregion

    private fun round(value: Double, precision: Int): Double {
        val scale = Math.pow(10.0, precision.toDouble()).toInt()
        return Math.round(value * scale).toDouble() / scale
    }
}
