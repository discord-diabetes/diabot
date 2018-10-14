package com.dongtronic.diabot.converters;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class A1cConverterTest {

  @Test
  void estimateA1c() {
    String input = "5.5%";
    double number = Double.valueOf(input);

    System.out.println("number = " + number);
  }



  @Test
  void doTheThing() {
    String c= "5.5%";
    String pattern = "[^0-9\\.]";

    c = c.replaceAll(pattern, "");

    System.out.println(c);
  }
}