package com.dongtronic.diabot.logic.diabetes

import com.dongtronic.diabot.data.A1cDTO
import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException

/**
 * A1c conversion logic
 */
object A1cConverter {
    private const val MMOL_TO_MGDL_CONVERSION_FACTOR = 18.016

    @Throws(UnknownUnitException::class)
    fun estimateA1c(originalValue: String, unit: String?): A1cDTO {
        val glucoseConversionResult = BloodGlucoseConverter.convert(originalValue, unit)

        return estimateA1c(glucoseConversionResult)
    }

    fun estimateAverage(originalValue: String): A1cDTO {
        val a1c = java.lang.Double.valueOf(originalValue)

        if (a1c < 0 || a1c > 375) {
            throw IllegalArgumentException("Given A1c must be between 0 and 375. Is $a1c")
        }

        return if (a1c < 25) {
            estimateAverageDcct(a1c)
        } else {
            estimateAverageIfcc(a1c)
        }
    }

    private fun estimateAverageDcct(dcct: Double): A1cDTO {
        val mgdl = convertDcctToMgdl(dcct)
        val ifcc = convertDcctToIfcc(dcct)

        val conversion = BloodGlucoseConverter.convert(mgdl.toString(), "mgdl")

        return A1cDTO(conversion, dcct, ifcc, 0.0, 0.0)
    }

    private fun estimateAverageIfcc(ifcc: Double): A1cDTO {
        val mgdl = convertIfccToMgdl(ifcc)
        val dcct = convertIfccToDcct(ifcc)

        val conversion = BloodGlucoseConverter.convert(mgdl.toString(), "mgdl")

        return A1cDTO(conversion, dcct, ifcc, 0.0, 0.0)
    }

    private fun estimateA1c(glucose: ConversionDTO): A1cDTO {
        var ifccMgdl = 0.0
        var dcctMgdl = 0.0
        var ifccMmol = 0.0
        var dcctMmol = 0.0

        when (glucose.inputUnit) {
            GlucoseUnit.MGDL -> {
                ifccMgdl = convertMgdlToIfcc(glucose.original)
                dcctMgdl = convertMgdlToDcct(glucose.original)
            }
            GlucoseUnit.MMOL -> {
                ifccMmol = convertMgdlToIfcc(glucose.converted)
                dcctMmol = convertMgdlToDcct(glucose.converted)
            }
            else -> return estimateA1cAmbiguous(glucose)
        }

        return A1cDTO(glucose, dcctMgdl, ifccMgdl, dcctMmol, ifccMmol)
    }

    private fun estimateA1cAmbiguous(glucose: ConversionDTO): A1cDTO {

        val ifccMgdl = convertMgdlToIfcc(glucose.mgdl.toDouble())
        val dcctMgdl = convertMgdlToDcct(glucose.mgdl.toDouble())
        val ifccMmol = convertMmolToIfcc(glucose.mmol)
        val dcctMmol = convertMmolToDcct(glucose.mmol)

        return A1cDTO(glucose, dcctMgdl, ifccMgdl, dcctMmol, ifccMmol)
    }

    private fun convertMmolToDcct(glucose: Double): Double {
        return convertMgdlToDcct(glucose * MMOL_TO_MGDL_CONVERSION_FACTOR)
    }

    private fun convertMmolToIfcc(glucose: Double): Double {
        return convertMgdlToIfcc(glucose * MMOL_TO_MGDL_CONVERSION_FACTOR)
    }

    private fun convertMgdlToDcct(glucose: Double): Double {
        return (glucose + 46.7) / 28.7
    }

    private fun convertMgdlToIfcc(glucose: Double): Double {
        return (convertMgdlToDcct(glucose) - 2.15) * 10.929
    }

    private fun convertDcctToMgdl(dcct: Double): Double {
        return dcct * 28.7 - 46.7
    }

    private fun convertIfccToMgdl(ifcc: Double): Double {
        return convertDcctToMgdl(convertIfccToDcct(ifcc))
    }

    private fun convertIfccToDcct(ifcc: Double): Double {
        return ifcc / 10.929 + 2.15
    }

    private fun convertDcctToIfcc(dcct: Double): Double {
        return (dcct - 2.15) * 10.929
    }
}
