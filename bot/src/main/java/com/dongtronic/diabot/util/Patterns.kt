package com.dongtronic.diabot.util

import java.util.regex.Pattern

object Patterns {
    /**
     * Matches BG numbers surrounded by underscores
     */
    val inlineBgPattern = Regex("""\b_(?<value>\d{1,3}(?:[.,]\d+)?)_\b(?=([^`]*"[^`]*")*[^`]*$)""")

    /**
     * Matches a message that is just a BG value, without any extra characters.
     * There needs to be:
     * - a BG value in either mg/dL or mmol/L formats (200 OR 200.2)
     * - nothing else before or after the BG value
     *
     * `3.4`
     * `3.4`
     * `100`
     * `100.0`
     */
    val separateBgPattern = Pattern.compile("^([0-9]{1,3}[.,]?[0-9]?)$")!!

    /**
     * Matches suspected BG values with units
     */
    val unitBgPattern = Regex("""(?<value>\d{1,3}(?:[.,]\d+)?) ?(?<unit>mmol|mg)""")
    val feelPattern = Pattern.compile(".*feel it.*")!!
    val ohNoPattern = Pattern.compile("oh no")!!
}
