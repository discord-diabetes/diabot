package com.dongtronic.diabot.data;

import com.dongtronic.diabot.converters.GlucoseUnit;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("Duplicates")
public class A1cDTO {
  private ConversionDTO original;
  private double dcct_mgdl;
  private double ifcc_mgdl;
  private double dcct_mmol;
  private double ifcc_mmol;


  public A1cDTO(ConversionDTO original, double dcct_mgdl, double ifcc_mgdl, @Nullable double dcct_mmol, @Nullable double ifcc_mmol) {
    this.original = original;

    this.dcct_mgdl = round(dcct_mgdl,1);
    this.ifcc_mgdl = round(ifcc_mgdl,1);
    this.dcct_mmol = round(dcct_mmol, 1);
    this.ifcc_mmol = round(ifcc_mmol, 1);
  }

  //region properties
  public ConversionDTO getOriginal() {
    return original;
  }

  public double getDcct() {
    if(original.getInputUnit() == GlucoseUnit.MMOL) {
      return dcct_mmol;
    } else if(original.getInputUnit() == GlucoseUnit.MGDL) {
      return dcct_mgdl;
    } else {
      throw new IllegalStateException();
    }
  }

  public double getIfcc() {
    if(original.getInputUnit() == GlucoseUnit.MMOL) {
      return ifcc_mmol;
    } else if(original.getInputUnit() == GlucoseUnit.MGDL) {
      return ifcc_mgdl;
    } else {
      throw new IllegalStateException();
    }
  }

  public double getDcct(GlucoseUnit unit) {
    if(unit == GlucoseUnit.MMOL) {
      return dcct_mmol;
    } else if (unit == GlucoseUnit.MGDL) {
      return dcct_mgdl;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public double getIfcc(GlucoseUnit unit) {
    if(unit == GlucoseUnit.MMOL) {
      return ifcc_mmol;
    } else if (unit == GlucoseUnit.MGDL) {
      return ifcc_mgdl;
    } else {
      throw new IllegalArgumentException();
    }
  }


  //endregion

  private static double round(double value, int precision) {
    int scale = (int) Math.pow(10, precision);
    return (double) Math.round(value * scale) / scale;
  }
}
