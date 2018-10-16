package com.dongtronic.diabot.util;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

public class DateTest {

  public DateTest(){}

  @Test
  public void stringToDate() {
    long timestamp = 1539672587884L;
    Timestamp date = new Timestamp(timestamp);

    Assert.assertNotNull(date);
  }
}
