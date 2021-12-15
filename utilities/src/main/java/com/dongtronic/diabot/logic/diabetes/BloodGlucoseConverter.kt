package com.dongtronic.diabot.logic.diabetes

import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException

/**
 * BG conversion logic
 */
object BloodGlucoseConverter {

    /**
     * Get a set of appropriate unicode emojis for reacting to a blood glucose value.
     *
     * Currently, the two emojis are:
     * :smirk: - 69 mg/dL or 6.9 mmol/L
     * :100:   - 100 mg/dL, 5.5 mmol/L, or 10.0 mmol/L
     */
    fun getReactions(conversionDTO: ConversionDTO) = getReactions(conversionDTO.mmol, conversionDTO.mgdl)

    /**
     * Get a set of appropriate unicode emojis for reacting to a blood glucose value.
     *
     * Currently, the two emojis are:
     * :smirk: - 69 mg/dL or 6.9 mmol/L
     * :100:   - 100 mg/dL, 5.5 mmol/L, or 10.0 mmol/L
     */
    fun getReactions(mmol: Double, mgdl: Int): Set<String> {
        val reactions = mutableSetOf<String>()
        // #20: Reply with :smirk: when value is 69 mg/dL or 6.9 mmol/L
        if (mmol == 6.9 || mgdl == 69) {
            reactions.add("\uD83D\uDE0F")
        }

        // #36 and #60: Reply with :100: when value is 100 mg/dL, 5.5 mmol/L, or 10.0 mmol/L
        if (mmol == 5.5
                || mmol == 10.0
                || mgdl == 100) {
            reactions.add("\uD83D\uDCAF")
        }

        return reactions
    }

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
