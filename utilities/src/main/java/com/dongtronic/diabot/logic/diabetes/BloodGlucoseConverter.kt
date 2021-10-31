package com.dongtronic.diabot.logic.diabetes

import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import java.util.*

/**
 * BG conversion logic
 */
object BloodGlucoseConverter {

    @Throws(UnknownUnitException::class)
    fun convert(value: String, unit: String?): ConversionDTO {
        val input = value.toDoubleOrNull()
                ?: throw IllegalArgumentException("value must be numeric")

        if (input < 0 || input > 999) {
            throw IllegalArgumentException("value must be between 0 and 999")
        }

        return if (unit != null && unit.length > 1) {
            convert(input, unit)
        } else {
            convert(input)
        }
    }

    private fun convert(originalValue: Double): ConversionDTO {
        return when {
            originalValue < 25 -> convert(originalValue, GlucoseUnit.MMOL)
            originalValue > 50 -> convert(originalValue, GlucoseUnit.MGDL)
            else -> convertAmbiguous(originalValue)
        }
    }

    @Throws(UnknownUnitException::class)
    private fun convert(originalValue: Double, unit: String): ConversionDTO {

        return when {
            unit.uppercase().contains("MMOL") -> {
                val result = originalValue * 18.016
                ConversionDTO(originalValue, result, GlucoseUnit.MMOL)
            }
            unit.uppercase().contains("MG") -> {
                val result = originalValue / 18.016
                ConversionDTO(originalValue, result, GlucoseUnit.MGDL)
            }
            else -> throw UnknownUnitException()
        }
    }

    private fun convert(originalValue: Double, unit: GlucoseUnit): ConversionDTO {

        return when (unit) {
            GlucoseUnit.MMOL -> {
                val result = originalValue * 18.016
                ConversionDTO(originalValue, result, GlucoseUnit.MMOL)
            }
            GlucoseUnit.MGDL -> {
                val result = originalValue / 18.016
                ConversionDTO(originalValue, result, GlucoseUnit.MGDL)
            }
            GlucoseUnit.AMBIGUOUS -> {
                return convertAmbiguous(originalValue)
            }
        }
    }

    private fun convertAmbiguous(originalValue: Double): ConversionDTO {
        val toMgdl = originalValue * 18.016
        val toMmol = originalValue / 18.016

        return ConversionDTO(originalValue, toMmol, toMgdl)
    }
}
