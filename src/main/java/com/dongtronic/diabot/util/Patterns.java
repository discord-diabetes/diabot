package com.dongtronic.diabot.util;

import java.util.regex.Pattern;

public class Patterns {
  public static Pattern inlineBgPattern = Pattern.compile("^.*_([0-9]{1,3}\\.?[0-9]?)_.*$");
  public static Pattern separateBgPattern = Pattern.compile("^([0-9]{1,3}\\.?[0-9]?)$");
  public static Pattern unitBgPattern = Pattern.compile("^((.*\\s-?)|(-?))?([0-9.]+)\\s?((mmol)|(mg)).*$");

}
