package com.dongtronic.diabot.data;

import com.dongtronic.diabot.converters.GlucoseUnit;
import org.jetbrains.annotations.NotNull;

public class ConversionDTO {
  private double original;
  private double mmol;
  private int mgdl;
  private GlucoseUnit inputUnit;


  /**
   * Create a ConversionDTO object with explicit unit
   *
   * @param original   original BG value
   * @param conversion converted BG value
   * @param inputUnit  original unit
   */
  public ConversionDTO(@NotNull double original, @NotNull double conversion, @NotNull GlucoseUnit inputUnit) {
    if (inputUnit == GlucoseUnit.AMBIGUOUS) {
      throw new IllegalArgumentException("single conversion constructor must contain explicit input unit");
    }

    setOriginal(original);
    this.inputUnit = inputUnit;

    if (inputUnit == GlucoseUnit.MMOL) {
      setMgdl(conversion);
      setMmol(original);
    } else if (inputUnit == GlucoseUnit.MGDL) {
      setMmol(conversion);
      setMgdl(original);
    }
  }

  /**
   * Create a ConversionDTO object with ambiguous unit
   *
   * @param original       original BG value
   * @param mmolConversion BG value converted to mmol/L
   * @param mgdlConversion BG value converted to mg/dL
   */
  public ConversionDTO(@NotNull double original, @NotNull double mmolConversion, @NotNull double mgdlConversion) {
    setOriginal(original);
    setMmol(mmolConversion);
    setMgdl(mgdlConversion);
    this.inputUnit = GlucoseUnit.AMBIGUOUS;
  }

  /**
   * Get the unit of the original BG value
   *
   * @return unit of the original BG value
   */
  public GlucoseUnit getInputUnit() {
    return inputUnit;
  }

  /**
   * Get the converted BG value
   *
   * @return double converted value
   * @throws IllegalStateException when the DTO contains an ambiguous conversion
   */
  public double getConverted() {
    if (inputUnit == GlucoseUnit.AMBIGUOUS) {
      throw new IllegalStateException("cannot retrieve specific unit result for ambiguous conversion");
    }

    if (inputUnit == GlucoseUnit.MGDL) {
      return mmol;
    } else {
      return mgdl;
    }
  }

  /**
   * Get the conversion result in mmol/L
   *
   * @return conversion result in mmol/L
   */
  public double getMmol() {
    return mmol;
  }

  /**
   * Get the conversion result in mg/dL
   *
   * @return conversion result in mg/dL
   */
  public int getMgdl() {
    return mgdl;
  }

  private void setMgdl(double input) {
    this.mgdl = (int) round(input, 0);
  }

  private void setMmol(double input) {
    this.mmol = round(input, 1);
  }

  public double getOriginal() {
    return original;
  }

  private void setOriginal(double original) {
    this.original = round(original, 1);
  }

  private static double round(double value, int precision) {
    int scale = (int) Math.pow(10, precision);
    return (double) Math.round(value * scale) / scale;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj.getClass() != this.getClass()) {
      return false;
    }

    ConversionDTO other = (ConversionDTO) obj;

    if (other.getOriginal() != this.getOriginal()) {
      return false;
    }

    if (other.getConverted() != this.getConverted()) {
      return false;
    }

    if (other.getInputUnit() != this.getInputUnit()) {
      return false;
    }

    return true;

  }
}
