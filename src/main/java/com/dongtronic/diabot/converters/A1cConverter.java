package com.dongtronic.diabot.converters;

import com.dongtronic.diabot.data.A1cDTO;
import com.dongtronic.diabot.data.ConversionDTO;
import com.dongtronic.diabot.exceptions.UnknownUnitException;

/**
 * A1c conversion logic
 */
public class A1cConverter{

  private static A1cDTO convert(double originalValue) {
    A1cDTO result = null;
    try {
      if (originalValue < 25) {
        //Convert to mg/dL
        result = convert(originalValue, "mmol");
      } else if (originalValue > 50) {
        //Convert to mmol/L
        result = convert(originalValue, "mgdl");
      } else {
        result = convertAmbiguous(originalValue);
      }
    } catch (UnknownUnitException ex) {
      // Ignored on purpose
    }

    return result;
  }

  private static A1cDTO convert(double originalValue, String unit) throws UnknownUnitException {

    if (unit.toUpperCase().contains("MMOL")) { //Convert to mg/dL
      double result = originalValue * 18.016;
      return new A1cDTO(originalValue, result, GlucoseUnit.MMOL);
    } else if (unit.toUpperCase().contains("MG")) { //Convert to mmol/L
      double result = originalValue / 1.8016;
      return new A1cDTO(originalValue, result, GlucoseUnit.MGDL);
    } else {
      throw new UnknownUnitException();
    }
  }

  private static A1cDTO convertAmbiguous(double originalValue) {

    double toMgdl = originalValue * 18.016;
    double toMmol = originalValue / 18.016;

    return new A1cDTO(originalValue, toMmol, toMgdl);

  }
}
