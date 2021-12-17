package com.dongtronic.diabot.converters

import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.logic.diabetes.BloodGlucoseConverter
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BloodGlucoseConverterTest {

    @Test
    fun mmolWithUnit() {

        val actual = BloodGlucoseConverter.convert("5.5", "mmol")

        val expected = ConversionDTO(5.5, 99.0, GlucoseUnit.MMOL)

        Assertions.assertEquals(expected, actual.getOrNull())
    }

    @Test
    fun mgdlWithUnit() {
        val actual = BloodGlucoseConverter.convert("100", "mgdl")

        val expected = ConversionDTO(100.0, 5.6, GlucoseUnit.MGDL)

        Assertions.assertEquals(expected, actual.getOrNull())
    }

    @Test
    fun mmolWithoutUnit() {
        val actual = BloodGlucoseConverter.convert("5.5", "")

        val expected = ConversionDTO(5.5, 99.0, GlucoseUnit.MMOL)

        Assertions.assertEquals(expected, actual.getOrNull())
    }

    @Test
    fun mgdlWithoutUnit() {
        val actual = BloodGlucoseConverter.convert("100", "")

        val expected = ConversionDTO(100.0, 5.6, GlucoseUnit.MGDL)

        Assertions.assertEquals(expected, actual.getOrNull())
    }

    @Test
    fun negative() {
        val actual = BloodGlucoseConverter.convert("-5.5", "mmol")

        Assertions.assertTrue(actual.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun tooHigh() {
        val actual = BloodGlucoseConverter.convert("1000", "mmol")

        Assertions.assertTrue(actual.exceptionOrNull() is IllegalArgumentException)
    }


    @Test
    fun invalidUnit() {
        val actual = BloodGlucoseConverter.convert("5.5", "what")

        Assertions.assertTrue(actual.exceptionOrNull() is UnknownUnitException)
    }

    @Test
    fun ambiguous() {
        val actual = BloodGlucoseConverter.convert("27", "")

        val expected = ConversionDTO(27.0, 1.5, 486.0)

        Assertions.assertEquals(expected, actual.getOrNull())
    }

    @Test
    fun noInput() {
        val actual = BloodGlucoseConverter.convert("", "")

        Assertions.assertTrue(actual.exceptionOrNull() is IllegalArgumentException)
    }
}
