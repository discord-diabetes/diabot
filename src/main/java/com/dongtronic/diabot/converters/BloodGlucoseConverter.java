package com.dongtronic.diabot.converters;

import com.dongtronic.diabot.exceptions.AmbiguousUnitException;
import com.dongtronic.diabot.exceptions.UnknownUnitException;

/**
 * BG conversion logic
 */
public class BloodGlucoseConverter {
  public static double convert(String value) throws AmbiguousUnitException {
    double input = Double.valueOf(value);

    if (input < 0 || input > 999) {
      throw new IllegalArgumentException();
    }

    if (input < 25) { //Convert to mg/dL
      double result = input * 18.016;
      return (int) result;
    } else if (input > 50) { //Convert to mmol/L
      double result = input / 1.8016;
      return ((int) result) / 10;
    } else {
      throw new AmbiguousUnitException();
    }
  }

  @SuppressWarnings("IntegerDivisionInFloatingPointContext")
  public static double convert(String value, String unit) throws UnknownUnitException {
    double input = Double.valueOf(value);

    if (input < 0 || input > 999) {
      throw new IllegalArgumentException();
    }

    if (unit.toUpperCase().contains("MMOL")) { //Convert to mg/dL
      double result = input * 18.016;
      return (int) result;
    } else if (unit.toUpperCase().contains("MG")) { //Convert to mmol/L
      double result = input / 1.8016;
      return ((int) result) / 10;
    } else {
      throw new UnknownUnitException();
    }
  }

  public static Double[] convertAmbiguous(String value) {
    double input = Double.valueOf(value);

    if (input < 0 || input > 999) {
      throw new IllegalArgumentException();
    }

    Double[] returned = new Double[2];


    returned[0] = (double) Math.round(input * 18.016);
    returned[1] = (double) Math.round(input / 18.016);

    return returned;
  }

  public static GlucoseUnit detectUnit(String value){
    double input = Double.valueOf(value);

    if (input < 0 || input > 999) {
      throw new IllegalArgumentException();
    }

    if (input < 25) {
      return GlucoseUnit.MMOL;
    } else if (input > 50) {
      return GlucoseUnit.MGDL;
    } else {
      return GlucoseUnit.AMBIGUOUS;
    }
  }
}
