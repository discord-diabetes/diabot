package com.dongtronic.diabot.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;

public class PatternsTest {

  public PatternsTest() {
  }


  @Test
  public void unitStartNoSpacesMg() {
    String message = "100mg";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("100", matcher.group(4));
    Assert.assertEquals("mg", matcher.group(5));
  }

  @Test
  public void unitStartNoSpacesMmol() {
    String message = "5.5mmol";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("5.5", matcher.group(4));
    Assert.assertEquals("mmol", matcher.group(5));
  }

  @Test
  public void unitStartNoSpacesMgDecimal() {
    String message = "100.5mg";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("100.5", matcher.group(4));
    Assert.assertEquals("mg", matcher.group(5));
  }

  @Test
  public void unitStartNoSpacesMmolWhole() {
    String message = "5mmol";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("5", matcher.group(4));
    Assert.assertEquals("mmol", matcher.group(5));
  }

  @Test
  public void unitStartSpacesMg() {
    String message = "100 mg";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("100", matcher.group(4));
    Assert.assertEquals("mg", matcher.group(5));
  }

  @Test
  public void unitStartSpacesMmol() {
    String message = "5.5 mmol";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("5.5", matcher.group(4));
    Assert.assertEquals("mmol", matcher.group(5));
  }

  @Test
  public void unitStartSpacesMgDecimal() {
    String message = "100.5 mg";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("100.5", matcher.group(4));
    Assert.assertEquals("mg", matcher.group(5));
  }

  @Test
  public void unitStartSpacesMmolWhole() {
    String message = "5 mmol";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("5", matcher.group(4));
    Assert.assertEquals("mmol", matcher.group(5));
  }
  
  /////

  @Test
  public void textStartNoSpacesMg() {
    String message = "bla bla 100mg";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("100", matcher.group(4));
    Assert.assertEquals("mg", matcher.group(5));
  }

  @Test
  public void textStartNoSpacesMmol() {
    String message = "bla bla 5.5mmol";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("5.5", matcher.group(4));
    Assert.assertEquals("mmol", matcher.group(5));
  }

  @Test
  public void textStartNoSpacesMgDecimal() {
    String message = "bla bla 100.5mg";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("100.5", matcher.group(4));
    Assert.assertEquals("mg", matcher.group(5));
  }

  @Test
  public void textStartNoSpacesMmolWhole() {
    String message = "bla bla 5mmol";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("5", matcher.group(4));
    Assert.assertEquals("mmol", matcher.group(5));
  }

  @Test
  public void textStartSpacesMg() {
    String message = "bla bla 100 mg";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("100", matcher.group(4));
    Assert.assertEquals("mg", matcher.group(5));
  }

  @Test
  public void textStartSpacesMmol() {
    String message = "bla bla 5.5 mmol";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("5.5", matcher.group(4));
    Assert.assertEquals("mmol", matcher.group(5));
  }

  @Test
  public void textStartSpacesMgDecimal() {
    String message = "bla bla 100.5 mg";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("100.5", matcher.group(4));
    Assert.assertEquals("mg", matcher.group(5));
  }

  @Test
  public void textStartSpacesMmolWhole() {
    String message = "bla bla 5 mmol";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("5", matcher.group(4));
    Assert.assertEquals("mmol", matcher.group(5));
  }

  @Test
  public void negativeNoSpacesMmol() {
    String message = "-5.5mmol";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("5.5", matcher.group(4));
    Assert.assertEquals("mmol", matcher.group(5));
  }

  @Test
  public void negativeNoSpacesMmolWhole() {
    String message = "-5mmol";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("5", matcher.group(4));
    Assert.assertEquals("mmol", matcher.group(5));
  }

  @Test
  public void negativeNoSpacesMg() {
    String message = "-100mg";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("100", matcher.group(4));
    Assert.assertEquals("mg", matcher.group(5));
  }

  @Test
  public void negativeNoSpacesMgDecimal() {
    String message = "-100.5mg";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("100.5", matcher.group(4));
    Assert.assertEquals("mg", matcher.group(5));
  }

  @Test
  public void textStartNegativeMmol() {
    String message = "bla bla -5.5mmol";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("5.5", matcher.group(4));
    Assert.assertEquals("mmol", matcher.group(5));
  }

  @Test
  public void textStartNegativeMmolWhole() {
    String message = "bla bla -5mmol";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("5", matcher.group(4));
    Assert.assertEquals("mmol", matcher.group(5));
  }

  @Test
  public void textStartNegativeMg() {
    String message = "bla bla -100mg";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("100", matcher.group(4));
    Assert.assertEquals("mg", matcher.group(5));
  }

  @Test
  public void textStartNegativeMgDecimal() {
    String message = "bla bla -100.5mg";

    Matcher matcher = Patterns.unitBgPattern.matcher(message);

    Assert.assertTrue(matcher.matches());

    Assert.assertEquals("100.5", matcher.group(4));
    Assert.assertEquals("mg", matcher.group(5));
  }
}
