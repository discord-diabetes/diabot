package com.dongtronic.diabot.converters;

import com.dongtronic.diabot.data.ConversionDTO;
import com.dongtronic.diabot.exceptions.UnknownUnitException;
import org.junit.Assert;
import org.junit.Test;

public class BloodGlucoseConverterTest {

  public BloodGlucoseConverterTest() {
  }

  @Test
  public void convertMmolWithUnit() throws Exception {

    ConversionDTO actual = BloodGlucoseConverter.convert("5.5", "mmol");

    ConversionDTO expected = new ConversionDTO(5.5, 99, GlucoseUnit.MMOL);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void mgdlWithUnit() throws Exception {
    ConversionDTO actual = BloodGlucoseConverter.convert("100", "mgdl");

    ConversionDTO expected = new ConversionDTO(100, 5.6, GlucoseUnit.MGDL);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void mmolWithoutUnit() throws Exception {
    ConversionDTO actual = BloodGlucoseConverter.convert("5.5", "");

    ConversionDTO expected = new ConversionDTO(5.5, 99, GlucoseUnit.MMOL);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void mgdlWithoutUnit() throws Exception {
    ConversionDTO actual = BloodGlucoseConverter.convert("100", "");

    ConversionDTO expected = new ConversionDTO(100, 5.6, GlucoseUnit.MGDL);

    Assert.assertEquals(expected, actual);
  }

  @Test(expected = IllegalArgumentException.class)
  public void negative() throws Exception {
    BloodGlucoseConverter.convert("-5.5", "mmol");
  }

  @Test(expected = IllegalArgumentException.class)
  public void tooHigh() throws Exception {
    BloodGlucoseConverter.convert("1000", "mmol");
  }


  @Test(expected = UnknownUnitException.class)
  public void invalidUnit() throws Exception {
    BloodGlucoseConverter.convert("5.5", "what");
  }

  @Test
  public void ambiguous() throws Exception {
    ConversionDTO actual = BloodGlucoseConverter.convert("27", "");

    ConversionDTO expected = new ConversionDTO(27, 1.5, 486);

    Assert.assertEquals(actual, expected);
  }

  @Test(expected = IllegalArgumentException.class)

  public void noInput() throws Exception {
    BloodGlucoseConverter.convert("", "");
  }
}