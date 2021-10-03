package com.dongtronic.diabot.util

import java.util.regex.Pattern

object Patterns {
    val inlineBgPattern = Pattern.compile("^.*\\s_([0-9]{1,3}[.,]?[0-9]?)_.*$")!!
    val separateBgPattern = Pattern.compile("^([0-9]{1,3}[.,]?[0-9]?)$")!!
    val unitBgPattern = Pattern.compile("^((.*\\s-?)|(-?))?([0-9.,]+)\\s?((mmol)|(mg)).*$")!!
    val feelPattern = Pattern.compile(".*feel it.*")!!
    val ohNoPattern = Pattern.compile("oh no")!!
}
