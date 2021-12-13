package com.dongtronic.diabot.converters

import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.logic.diabetes.BloodGlucoseConverter
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BloodGlucoseConverterTest {

    @Test
    @Throws(Exception::class)
    fun mmolWithUnit() {

        val actual = BloodGlucoseConverter.convert("5.5", "mmol")

        val expected = ConversionDTO(5.5, 99.0, GlucoseUnit.MMOL)

        Assertions.assertEquals(expected, actual)
    }

    @Test
    @Throws(Exception::class)
    fun mgdlWithUnit() {
        val actual = BloodGlucoseConverter.convert("100", "mgdl")

        val expected = ConversionDTO(100.0, 5.6, GlucoseUnit.MGDL)

        Assertions.assertEquals(expected, actual)
    }

    @Test
    @Throws(Exception::class)
    fun mmolWithoutUnit() {
        val actual = BloodGlucoseConverter.convert("5.5", "")

        val expected = ConversionDTO(5.5, 99.0, GlucoseUnit.MMOL)

        Assertions.assertEquals(expected, actual)
    }

    @Test
    @Throws(Exception::class)
    fun mgdlWithoutUnit() {
        val actual = BloodGlucoseConverter.convert("100", "")

        val expected = ConversionDTO(100.0, 5.6, GlucoseUnit.MGDL)

        Assertions.assertEquals(expected, actual)
    }

    @Test
    @Throws(Exception::class)
    fun negative() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            BloodGlucoseConverter.convert("-5.5", "mmol")
        }
    }

    @Test
    @Throws(Exception::class)
    fun tooHigh() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            BloodGlucoseConverter.convert("1000", "mmol")
        }
    }


    @Test
    @Throws(Exception::class)
    fun invalidUnit() {
        Assertions.assertThrows(UnknownUnitException::class.java) {
            BloodGlucoseConverter.convert("5.5", "what")
        }
    }

    @Test
    @Throws(Exception::class)
    fun ambiguous() {
        val actual = BloodGlucoseConverter.convert("27", "")

        val expected = ConversionDTO(27.0, 1.5, 486.0)

        Assertions.assertEquals(actual, expected)
    }

    @Test
    @Throws(Exception::class)
    fun noInput() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            BloodGlucoseConverter.convert("", "")
        }
    }
}
