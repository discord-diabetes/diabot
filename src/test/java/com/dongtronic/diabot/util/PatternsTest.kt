package com.dongtronic.diabot.util

import org.junit.Assert
import org.junit.Test

import java.util.regex.Matcher

class PatternsTest {


    @Test
    fun unitStartNoSpacesMg() {
        val message = "100mg"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("100", matcher.group(4))
        Assert.assertEquals("mg", matcher.group(5))
    }

    @Test
    fun unitStartNoSpacesMmol() {
        val message = "5.5mmol"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("5.5", matcher.group(4))
        Assert.assertEquals("mmol", matcher.group(5))
    }

    @Test
    fun unitStartNoSpacesMgDecimal() {
        val message = "100.5mg"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("100.5", matcher.group(4))
        Assert.assertEquals("mg", matcher.group(5))
    }

    @Test
    fun unitStartNoSpacesMmolWhole() {
        val message = "5mmol"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("5", matcher.group(4))
        Assert.assertEquals("mmol", matcher.group(5))
    }

    @Test
    fun unitStartSpacesMg() {
        val message = "100 mg"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("100", matcher.group(4))
        Assert.assertEquals("mg", matcher.group(5))
    }

    @Test
    fun unitStartSpacesMmol() {
        val message = "5.5 mmol"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("5.5", matcher.group(4))
        Assert.assertEquals("mmol", matcher.group(5))
    }

    @Test
    fun unitStartSpacesMgDecimal() {
        val message = "100.5 mg"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("100.5", matcher.group(4))
        Assert.assertEquals("mg", matcher.group(5))
    }

    @Test
    fun unitStartSpacesMmolWhole() {
        val message = "5 mmol"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("5", matcher.group(4))
        Assert.assertEquals("mmol", matcher.group(5))
    }

    /////

    @Test
    fun textStartNoSpacesMg() {
        val message = "bla bla 100mg"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("100", matcher.group(4))
        Assert.assertEquals("mg", matcher.group(5))
    }

    @Test
    fun textStartNoSpacesMmol() {
        val message = "bla bla 5.5mmol"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("5.5", matcher.group(4))
        Assert.assertEquals("mmol", matcher.group(5))
    }

    @Test
    fun textStartNoSpacesMgDecimal() {
        val message = "bla bla 100.5mg"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("100.5", matcher.group(4))
        Assert.assertEquals("mg", matcher.group(5))
    }

    @Test
    fun textStartNoSpacesMmolWhole() {
        val message = "bla bla 5mmol"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("5", matcher.group(4))
        Assert.assertEquals("mmol", matcher.group(5))
    }

    @Test
    fun textStartSpacesMg() {
        val message = "bla bla 100 mg"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("100", matcher.group(4))
        Assert.assertEquals("mg", matcher.group(5))
    }

    @Test
    fun textStartSpacesMmol() {
        val message = "bla bla 5.5 mmol"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("5.5", matcher.group(4))
        Assert.assertEquals("mmol", matcher.group(5))
    }

    @Test
    fun textStartSpacesMgDecimal() {
        val message = "bla bla 100.5 mg"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("100.5", matcher.group(4))
        Assert.assertEquals("mg", matcher.group(5))
    }

    @Test
    fun textStartSpacesMmolWhole() {
        val message = "bla bla 5 mmol"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("5", matcher.group(4))
        Assert.assertEquals("mmol", matcher.group(5))
    }

    @Test
    fun negativeNoSpacesMmol() {
        val message = "-5.5mmol"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("5.5", matcher.group(4))
        Assert.assertEquals("mmol", matcher.group(5))
    }

    @Test
    fun negativeNoSpacesMmolWhole() {
        val message = "-5mmol"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("5", matcher.group(4))
        Assert.assertEquals("mmol", matcher.group(5))
    }

    @Test
    fun negativeNoSpacesMg() {
        val message = "-100mg"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("100", matcher.group(4))
        Assert.assertEquals("mg", matcher.group(5))
    }

    @Test
    fun negativeNoSpacesMgDecimal() {
        val message = "-100.5mg"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("100.5", matcher.group(4))
        Assert.assertEquals("mg", matcher.group(5))
    }

    @Test
    fun textStartNegativeMmol() {
        val message = "bla bla -5.5mmol"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("5.5", matcher.group(4))
        Assert.assertEquals("mmol", matcher.group(5))
    }

    @Test
    fun textStartNegativeMmolWhole() {
        val message = "bla bla -5mmol"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("5", matcher.group(4))
        Assert.assertEquals("mmol", matcher.group(5))
    }

    @Test
    fun textStartNegativeMg() {
        val message = "bla bla -100mg"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("100", matcher.group(4))
        Assert.assertEquals("mg", matcher.group(5))
    }

    @Test
    fun textStartNegativeMgDecimal() {
        val message = "bla bla -100.5mg"

        val matcher = Patterns.unitBgPattern.matcher(message)

        Assert.assertTrue(matcher.matches())

        Assert.assertEquals("100.5", matcher.group(4))
        Assert.assertEquals("mg", matcher.group(5))
    }
}
