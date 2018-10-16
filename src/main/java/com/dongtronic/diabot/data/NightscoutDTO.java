package com.dongtronic.diabot.data;

import java.time.ZonedDateTime;

public class NightscoutDTO {
  private ConversionDTO glucose;
  private ConversionDTO delta;
  private ZonedDateTime dateTime;
  private boolean deltaIsNegative;
  private int low;
  private int bottom;
  private int top;
  private int high;

  public NightscoutDTO(ConversionDTO glucose, ConversionDTO delta, boolean deltaIsNegative, ZonedDateTime dateTime) {
    this.glucose = glucose;
    this.delta = delta;
    this.deltaIsNegative = deltaIsNegative;
    this.dateTime = dateTime;
  }

  public NightscoutDTO() {}


  //region properties
  public ConversionDTO getGlucose() {
    return glucose;
  }

  public void setGlucose(ConversionDTO glucose) {
    this.glucose = glucose;
  }

  public ConversionDTO getDelta() {
    return delta;
  }

  public boolean getDeltaIsNegative() {
    return deltaIsNegative;
  }

  public void setDelta(ConversionDTO delta) {
    this.delta = delta;
  }

  public void setDeltaIsNegative(boolean deltaIsNegative) {
    this.deltaIsNegative = deltaIsNegative;
  }

  public ZonedDateTime getDateTime() {
    return dateTime;
  }

  public void setDateTime(ZonedDateTime dateTime) {
    this.dateTime = dateTime;
  }

  public int getLow() {
    return low;
  }

  public void setLow(int low) {
    this.low = low;
  }

  public int getBottom() {
    return bottom;
  }

  public void setBottom(int bottom) {
    this.bottom = bottom;
  }

  public int getTop() {
    return top;
  }

  public void setTop(int top) {
    this.top = top;
  }

  public int getHigh() {
    return high;
  }

  public void setHigh(int high) {
    this.high = high;
  }
  //endregion
}
