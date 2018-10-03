package com.dongtronic.diabot.converters;

public enum A1cUnit {
  IFCC("mmol/mol"), DCCT("%"), AMBIGUOUS("Ambiguous");

  String unit;

  A1cUnit(String unit) {
    this.unit = unit;
  }
}
