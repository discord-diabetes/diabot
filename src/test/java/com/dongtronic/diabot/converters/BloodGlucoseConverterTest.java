package com.dongtronic.diabot.converters;

import com.dongtronic.diabot.data.ConversionDTO;
import org.junit.Assert;
import org.junit.Test;

public class BloodGlucoseConverterTest {

  public BloodGlucoseConverterTest() {}

  @Test
  public void convert() throws Exception {

    ConversionDTO actual = BloodGlucoseConverter.convert("5.5", "mmol");

    ConversionDTO expected = new ConversionDTO(5.5, 99, GlucoseUnit.MMOL);

    Assert.assertEquals(expected, actual);
  }

  @Test(expected = IllegalArgumentException.class)
  public void noInput() throws Exception {
    ConversionDTO actual = BloodGlucoseConverter.convert("", "");
  }
}