package com.dongtronic.diabot.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class PatternsTest {


    @ParameterizedTest
    @MethodSource("unitBgProvider")
    fun unitBgPatternTest(data: BgParseData) {
        val input = data.input
        val expected = data.expected
        val result = Patterns.unitBgPattern.matcher(input)

        Assertions.assertTrue(result.matches())

        val actualValue = result.group(4)
        val actualUnit = result.group(5)

        Assertions.assertEquals(expected.value, actualValue)
        Assertions.assertEquals(expected.unit, actualUnit)
    }

    private fun unitBgProvider() = Stream.of(
            BgParseData(input = "100.5mg", expected = BgData("100.5", "mg")),
            BgParseData(input = "100mg", expected = BgData("100", "mg")),
            BgParseData(input = "5.5mmol", expected = BgData("5.5", "mmol")),
            BgParseData(input = "5mmol", expected = BgData("5", "mmol")),

            // spaces
            BgParseData(input = "100.5 mg", expected = BgData("100.5", "mg")),
            BgParseData(input = "100 mg", expected = BgData("100", "mg")),
            BgParseData(input = "5.5 mmol", expected = BgData("5.5", "mmol")),
            BgParseData(input = "5 mmol", expected = BgData("5", "mmol")),

            // text
            BgParseData(input = "bla bla 100.5mg", expected = BgData("100.5", "mg")),
            BgParseData(input = "bla bla 100mg", expected = BgData("100", "mg")),
            BgParseData(input = "bla bla 5.5mmol", expected = BgData("5.5", "mmol")),
            BgParseData(input = "bla bla 5mmol", expected = BgData("5", "mmol")),

            // text + spaces
            BgParseData(input = "bla bla 100.5 mg", expected = BgData("100.5", "mg")),
            BgParseData(input = "bla bla 100 mg", expected = BgData("100", "mg")),
            BgParseData(input = "bla bla 5.5 mmol", expected = BgData("5.5", "mmol")),
            BgParseData(input = "bla bla 5 mmol", expected = BgData("5", "mmol")),

            // negative
            BgParseData(input = "-100.5mg", expected = BgData("100.5", "mg")),
            BgParseData(input = "-100mg", expected = BgData("100", "mg")),
            BgParseData(input = "-5.5mmol", expected = BgData("5.5", "mmol")),
            BgParseData(input = "-5mmol", expected = BgData("5", "mmol")),

            // negative + spaces
            BgParseData(input = "-100.5 mg", expected = BgData("100.5", "mg")),
            BgParseData(input = "-100 mg", expected = BgData("100", "mg")),
            BgParseData(input = "-5.5 mmol", expected = BgData("5.5", "mmol")),
            BgParseData(input = "-5 mmol", expected = BgData("5", "mmol")),

            // negative + text
            BgParseData(input = "bla bla -100.5mg", expected = BgData("100.5", "mg")),
            BgParseData(input = "bla bla -100mg", expected = BgData("100", "mg")),
            BgParseData(input = "bla bla -5.5mmol", expected = BgData("5.5", "mmol")),
            BgParseData(input = "bla bla -5mmol", expected = BgData("5", "mmol")),

            // negative + text + spaces
            BgParseData(input = "bla bla -100.5 mg", expected = BgData("100.5", "mg")),
            BgParseData(input = "bla bla -100 mg", expected = BgData("100", "mg")),
            BgParseData(input = "bla bla -5.5 mmol", expected = BgData("5.5", "mmol")),
            BgParseData(input = "bla bla -5 mmol", expected = BgData("5", "mmol")),
    )

    data class BgData(
            val value: String,
            val unit: String
    )

    data class BgParseData(
            val input: String,
            val expected: BgData
    )
}
