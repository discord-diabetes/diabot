package com.dongtronic.diabot.converters;

import com.dongtronic.diabot.data.A1cDTO;
import com.dongtronic.diabot.data.ConversionDTO;
import com.dongtronic.diabot.exceptions.UnknownUnitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A1c conversion logic
 */
public class A1cConverter {

  private static final Logger logger = LoggerFactory.getLogger(A1cConverter.class);

  public static A1cDTO estimateA1c(String originalValue, String unit) throws UnknownUnitException {
    logger.info("Estimating A1c for BG " + originalValue);

    ConversionDTO glucoseConversionResult = BloodGlucoseConverter.convert(originalValue, unit);

    return estimateA1c(glucoseConversionResult);
  }

  public static A1cDTO estimateAverage(String originalValue) {
    double a1c = Double.valueOf(originalValue);

    if (a1c < 0 || a1c > 375) {
      throw new IllegalArgumentException();
    }

    A1cDTO result = null;

    try {
      if (a1c < 25) {
        result = estimateAverageDcct(a1c);
      } else {
        result = estimateAverageIfcc(a1c);
      }
    } catch (UnknownUnitException e) {
      // Ignored on purpose
    }

    return result;
  }

  private static A1cDTO estimateAverageDcct(double dcct) throws UnknownUnitException {
    double mgdl = convertDcctToMgdl(dcct);
    double ifcc = convertDcctToIfcc(dcct);

    ConversionDTO conversion = BloodGlucoseConverter.convert(String.valueOf(mgdl), "mgdl");

    return new A1cDTO(conversion, dcct, ifcc, 0, 0);
  }

  private static A1cDTO estimateAverageIfcc(double ifcc) throws UnknownUnitException {
    double mgdl = convertIfccToMgdl(ifcc);
    double dcct = convertIfccToDcct(ifcc);

    ConversionDTO conversion = BloodGlucoseConverter.convert(String.valueOf(mgdl), "mgdl");

    return new A1cDTO(conversion, dcct, ifcc, 0, 0);
  }


  private static A1cDTO estimateA1c(ConversionDTO glucose) {
    double ifcc_mgdl = 0;
    double dcct_mgdl = 0;
    double ifcc_mmol = 0;
    double dcct_mmol = 0;

    if (glucose.getInputUnit() == GlucoseUnit.MGDL) {
      ifcc_mgdl = convertMgdlToIfcc(glucose.getOriginal());
      dcct_mgdl = convertMgdlToDcct(glucose.getOriginal());
    } else if (glucose.getInputUnit() == GlucoseUnit.MMOL) {
      ifcc_mmol = convertMgdlToIfcc(glucose.getConverted());
      dcct_mmol = convertMgdlToDcct(glucose.getConverted());
    } else {
      return estimateA1cAmbiguous(glucose);
    }

    return new A1cDTO(glucose, dcct_mgdl, ifcc_mgdl, dcct_mmol, ifcc_mmol);
  }

  private static A1cDTO estimateA1cAmbiguous(ConversionDTO glucose) {

    double ifcc_mgdl = convertMgdlToIfcc(glucose.getMgdl());
    double dcct_mgdl = convertMgdlToDcct(glucose.getMgdl());
    double ifcc_mmol = convertMmolToIfcc(glucose.getMmol());
    double dcct_mmol = convertMmolToDcct(glucose.getMmol());

    return new A1cDTO(glucose, dcct_mgdl, ifcc_mgdl, dcct_mmol, ifcc_mmol);
  }

  private static double convertMmolToDcct(double glucose) {
    return convertMgdlToDcct(glucose * 18.016);
  }

  private static double convertMmolToIfcc(double glucose) {
    return convertMgdlToIfcc(glucose * 18.016);
  }

  private static double convertMgdlToDcct(double glucose) {
    return (glucose + 46.7) / 28.7;
  }

  private static double convertMgdlToIfcc(double glucose) {
    return (convertMgdlToDcct(glucose) - 2.15) * 10.929;
  }

  private static double convertDcctToMgdl(double dcct) {
    return (dcct * 28.7) - 46.7;
  }

  private static double convertIfccToMgdl(double ifcc) {
    return convertDcctToMgdl(convertIfccToDcct(ifcc));
  }

  private static double convertIfccToDcct(double ifcc) {
    return (ifcc / 10.929) + 2.15;
  }

  private static double convertDcctToIfcc(double dcct) {
    return (dcct - 2.15) * 10.929;
  }
}
