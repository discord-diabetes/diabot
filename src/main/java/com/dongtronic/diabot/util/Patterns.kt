package com.dongtronic.diabot.util

import java.util.regex.Pattern

object Patterns {
    val inlineBgPattern = Pattern.compile("^.*_([0-9]{1,3}\\.?[0-9]?)_.*$")!!
    val separateBgPattern = Pattern.compile("^([0-9]{1,3}\\.?[0-9]?)$")!!
    val unitBgPattern = Pattern.compile("^((.*\\s-?)|(-?))?([0-9.]+)\\s?((mmol)|(mg)).*$")!!

}
