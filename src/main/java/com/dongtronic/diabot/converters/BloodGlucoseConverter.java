package com.dongtronic.diabot.converters;

import com.dongtronic.diabot.data.ConversionDTO;
import com.dongtronic.diabot.exceptions.UnknownUnitException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * BG conversion logic
 */
public class BloodGlucoseConverter {

  public static ConversionDTO convert(String value, String unit) throws UnknownUnitException {

    if(!NumberUtils.isCreatable(value)) {
      throw new IllegalArgumentException("value must be numeric");
    }

    double input = Double.valueOf(value);

    if (input < 0 || input > 999) {
      throw new IllegalArgumentException();
    }

    if (unit != null && unit.length() > 1) {
      return convert(input, unit);
    } else {
      return convert(input);
    }
  }

  private static ConversionDTO convert(double originalValue) {
    ConversionDTO result = null;
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

  private static ConversionDTO convert(double originalValue, String unit) throws UnknownUnitException {

    if (unit.toUpperCase().contains("MMOL")) { //Convert to mg/dL
      double result = originalValue * 18.016;
      return new ConversionDTO(originalValue, result, GlucoseUnit.MMOL);
    } else if (unit.toUpperCase().contains("MG")) { //Convert to mmol/L
      double result = originalValue / 18.016;
      return new ConversionDTO(originalValue, result, GlucoseUnit.MGDL);
    } else {
      throw new UnknownUnitException();
    }
  }

  private static ConversionDTO convertAmbiguous(double originalValue) {

    double toMgdl = originalValue * 18.016;
    double toMmol = originalValue / 18.016;

    return new ConversionDTO(originalValue, toMmol, toMgdl);

  }
}
