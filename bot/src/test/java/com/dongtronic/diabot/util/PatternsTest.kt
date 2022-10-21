package com.dongtronic.diabot.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class PatternsTest {

    @ParameterizedTest
    @MethodSource("inlineBgProvider")
    fun inlineBgPatternTest(data: BgParseData) {
        val input = data.input
        val expected = data.expected
        val result = Patterns.inlineBgPattern.find(input)

        if (expected == null) {
            Assertions.assertEquals(null, result)
            return
        }

        Assertions.assertNotNull(result)

        // result won't be null here because of the above assertion
        result!!

        val actualValue = result.groups["value"]!!.value

        Assertions.assertEquals(expected.value, actualValue)
    }

    @ParameterizedTest
    @MethodSource("unitBgProvider")
    fun unitBgPatternTest(data: BgParseData) {
        val input = data.input
        val expected = data.expected
        val result = Patterns.unitBgPattern.find(input)

        if (expected == null) {
            Assertions.assertEquals(null, result)
            return
        }

        Assertions.assertNotNull(result)

        // result won't be null here because of the above assertion
        result!!

        val actualValue = result.groups["value"]!!.value
        val actualUnit = result.groups["unit"]!!.value

        Assertions.assertEquals(expected.value, actualValue)
        Assertions.assertEquals(expected.unit, actualUnit)
    }

    data class BgData(
            val value: String,
            val unit: String? = null
    )

    data class BgParseData(
            val input: String,
            val expected: BgData?
    )

    companion object {
        @JvmStatic
        private fun inlineBgProvider() = Stream.of(
                BgParseData(input = "test _100.5_", expected = BgData("100.5")),
                BgParseData(input = "test _100_", expected = BgData("100")),
                BgParseData(input = "test _5.5_", expected = BgData("5.5")),
                BgParseData(input = "test _5_", expected = BgData("5")),

                // invalid
                BgParseData(input = "test _100.5 mg_", expected = null),
                BgParseData(input = "test _100 mg_", expected = null),
                BgParseData(input = "test _5.5 mmol_", expected = null),
                BgParseData(input = "test _5 mmol_", expected = null),

                // invalid + spaces
                BgParseData(input = "test _100.5mg_", expected = null),
                BgParseData(input = "test _100mg_", expected = null),
                BgParseData(input = "test _5.5mmol_", expected = null),
                BgParseData(input = "test _5mmol_", expected = null),
        )

        @JvmStatic
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
    }
}
