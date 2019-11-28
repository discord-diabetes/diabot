package com.dongtronic.diabot.logic.diabetes

import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import org.apache.commons.lang3.math.NumberUtils

/**
 * BG conversion logic
 */
object BloodGlucoseConverter {

    @Throws(UnknownUnitException::class)
    fun convert(value: String, unit: String?): ConversionDTO? {

        if (!NumberUtils.isCreatable(value)) {
            throw IllegalArgumentException("value must be numeric")
        }

        val input = java.lang.Double.valueOf(value)

        if (input < 0 || input > 999) {
            throw IllegalArgumentException("value must be between 0 and 999")
        }

        return if (unit != null && unit.length > 1) {
            convert(input, unit)
        } else {
            convert(input)
        }
    }

    private fun convert(originalValue: Double): ConversionDTO? {
        var result: ConversionDTO? = null
        try {
            result = when {
                originalValue < 25 -> convert(originalValue, "mmol")
                originalValue > 50 -> convert(originalValue, "mgdl")
                else -> convertAmbiguous(originalValue)
            }
        } catch (ex: UnknownUnitException) {
            // Ignored on purpose
        }

        return result
    }

    @Throws(UnknownUnitException::class)
    private fun convert(originalValue: Double, unit: String): ConversionDTO {

        return when {
            unit.toUpperCase().contains("MMOL") -> {
                val result = originalValue * 18.016
                ConversionDTO(originalValue, result, GlucoseUnit.MMOL)
            }
            unit.toUpperCase().contains("MG") -> {
                val result = originalValue / 18.016
                ConversionDTO(originalValue, result, GlucoseUnit.MGDL)
            }
            else -> throw UnknownUnitException()
        }
    }

    private fun convertAmbiguous(originalValue: Double): ConversionDTO {

        val toMgdl = originalValue * 18.016
        val toMmol = originalValue / 18.016

        return ConversionDTO(originalValue, toMmol, toMgdl)

    }
}
