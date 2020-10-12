package com.dongtronic.diabot.data

import com.dongtronic.diabot.logic.diabetes.GlucoseUnit

class ConversionDTO {
    var original: Double = 0.toDouble()
        private set(original) {
            field = round(original, 1)
        }
    /**
     * Get the conversion result in mmol/L
     *
     * @return conversion result in mmol/L
     */
    var mmol: Double = 0.toDouble()
        private set(input) {
            field = round(input, 1)
        }
    /**
     * Get the conversion result in mg/dL
     *
     * @return conversion result in mg/dL
     */
    var mgdl: Int = 0
        private set
    /**
     * Get the unit of the original BG value
     *
     * @return unit of the original BG value
     */
    var inputUnit: GlucoseUnit? = null
        private set

    /**
     * Get the converted BG value
     *
     * @return double converted value
     * @throws IllegalStateException when the DTO contains an ambiguous conversion
     */
    val converted: Double
        get() {
            if (inputUnit == GlucoseUnit.AMBIGUOUS) {
                throw IllegalStateException("cannot retrieve specific unit result for ambiguous conversion")
            }

            return if (inputUnit == GlucoseUnit.MGDL) {
                this.mmol
            } else {
                mgdl.toDouble()
            }
        }


    /**
     * Create a ConversionDTO object with explicit unit
     *
     * @param original   original BG value
     * @param conversion converted BG value
     * @param inputUnit  original unit
     */
    constructor(original: Double, conversion: Double, inputUnit: GlucoseUnit) {
        if (inputUnit == GlucoseUnit.AMBIGUOUS) {
            throw IllegalArgumentException("single conversion constructor must contain explicit input unit")
        }

        this.original = original
        this.inputUnit = inputUnit

        if (inputUnit == GlucoseUnit.MMOL) {
            setMgdl(conversion)
            mmol = original
        } else if (inputUnit == GlucoseUnit.MGDL) {
            mmol = conversion
            setMgdl(original)
        }
    }

    /**
     * Create a ConversionDTO object with ambiguous unit
     *
     * @param original       original BG value
     * @param mmolConversion BG value converted to mmol/L
     * @param mgdlConversion BG value converted to mg/dL
     */
    constructor(original: Double, mmolConversion: Double, mgdlConversion: Double) {
        this.original = original
        mmol = mmolConversion
        setMgdl(mgdlConversion)
        this.inputUnit = GlucoseUnit.AMBIGUOUS
    }

    private fun setMgdl(input: Double) {
        this.mgdl = round(input, 0).toInt()
    }

    private fun round(value: Double, precision: Int): Double {
        val scale = Math.pow(10.0, precision.toDouble()).toInt()
        return Math.round(value * scale).toDouble() / scale
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConversionDTO

        if (original != other.original) return false
        if (mmol != other.mmol) return false
        if (mgdl != other.mgdl) return false
        if (inputUnit != other.inputUnit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = original.hashCode()
        result = 31 * result + mmol.hashCode()
        result = 31 * result + mgdl
        result = 31 * result + (inputUnit?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ConversionDTO(original=$original, mmol=$mmol, mgdl=$mgdl, inputUnit=$inputUnit)"
    }
}