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
     *
     * @param conversionDTO BG conversion DTO
     * @return [Set] of unicode emojis reacting to the given BG
     * @see [getReactions]
     */
    fun getReactions(conversionDTO: ConversionDTO) = getReactions(conversionDTO.mmol, conversionDTO.mgdl)

    /**
     * Get a set of appropriate unicode emojis for reacting to a blood glucose value.
     *
     * Currently, the two emojis are:
     * :smirk: - 69 mg/dL or 6.9 mmol/L
     * :100:   - 100 mg/dL, 5.5 mmol/L, or 10.0 mmol/L
     *
     * @param mmol BG number in mmol/L
     * @param mgdl BG number in mg/dL
     * @return [Set] of unicode emojis reacting to the given BG
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

    /**
     * Convert a string blood glucose input into a [ConversionDTO].
     *
     * @param value BG number
     * @param unit Optional: Measurement unit of [value]
     * @return [ConversionDTO] if successful
     * @throws IllegalArgumentException if [value] is not numeric
     * @throws IllegalArgumentException if [value] is not between 0 and 999
     * @throws UnknownUnitException if [unit] is an unknown BG measurement unit
     */
    fun convert(value: String, unit: String?): Result<ConversionDTO> {
        val input = value.toDoubleOrNull()
                ?: return Result.failure(IllegalArgumentException("value must be numeric"))

        if (input < -999 || input > 999) {
            return Result.failure(IllegalArgumentException("value must be between -999 and 999"))
        }

        return if (unit != null && unit.length > 1) {
            convert(input, unit)
        } else {
            convert(input)
        }
    }

    private fun convert(originalValue: Double): Result<ConversionDTO> {
        return when {
            originalValue < 25 -> convert(originalValue, GlucoseUnit.MMOL)
            originalValue > 50 -> convert(originalValue, GlucoseUnit.MGDL)
            else -> convertAmbiguous(originalValue)
        }
    }

    private fun convert(originalValue: Double, unit: String): Result<ConversionDTO> {
        return when {
            unit.contains("MMOL", true) -> {
                val result = originalValue * GlucoseUnit.CONVERSION_FACTOR
                Result.success(ConversionDTO(originalValue, result, GlucoseUnit.MMOL))
            }
            unit.contains("MG", true) -> {
                val result = originalValue / GlucoseUnit.CONVERSION_FACTOR
                Result.success(ConversionDTO(originalValue, result, GlucoseUnit.MGDL))
            }
            else -> Result.failure(UnknownUnitException())
        }
    }

    private fun convert(originalValue: Double, unit: GlucoseUnit): Result<ConversionDTO> {
        return when (unit) {
            GlucoseUnit.MMOL -> {
                val result = originalValue * GlucoseUnit.CONVERSION_FACTOR
                Result.success(ConversionDTO(originalValue, result, GlucoseUnit.MMOL))
            }
            GlucoseUnit.MGDL -> {
                val result = originalValue / GlucoseUnit.CONVERSION_FACTOR
                Result.success(ConversionDTO(originalValue, result, GlucoseUnit.MGDL))
            }
            GlucoseUnit.AMBIGUOUS -> convertAmbiguous(originalValue)
        }
    }

    private fun convertAmbiguous(originalValue: Double): Result<ConversionDTO> {
        val toMgdl = originalValue * GlucoseUnit.CONVERSION_FACTOR
        val toMmol = originalValue / GlucoseUnit.CONVERSION_FACTOR

        return Result.success(ConversionDTO(originalValue, toMmol, toMgdl))
    }
}
