package com.dongtronic.diabot.converters;

public enum GlucoseUnit {
  MMOL("mmol/L"), MGDL("mg/dL"), AMBIGUOUS("Ambiguous");

  String unit;

  GlucoseUnit(String unit) {
    this.unit = unit;
  }
}
