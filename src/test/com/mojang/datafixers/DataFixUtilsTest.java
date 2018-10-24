// Copyright (c) Diffblue Limited. All rights reserved.
// Licensed under the MIT license.

package com.mojang.datafixers;

import com.mojang.datafixers.DataFixUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DataFixUtilsTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void ceillog2InputPositiveOutputPositive() {
    // Arrange
    final int input = 193;
    // Act
    final int retval = DataFixUtils.ceillog2(input);
    // Assert result
    Assert.assertEquals(8, retval);
  }

  @Test
  public void ceillog2InputPositiveOutputPositive2() {
    // Arrange
    final int input = 256;
    // Act
    final int retval = DataFixUtils.ceillog2(input);
    // Assert result
    Assert.assertEquals(8, retval);
  }

  @Test
  public void makeKeyInputZeroOutputZero() {
    // Arrange
    final int version = 0;
    // Act
    final int retval = DataFixUtils.makeKey(version);
    // Assert result
    Assert.assertEquals(0, retval);
  }

  @Test
  public void makeKeyInputZeroZeroOutputZero() {
    // Arrange
    final int version = 0;
    final int subVersion = 0;
    // Act
    final int retval = DataFixUtils.makeKey(version, subVersion);
    // Assert result
    Assert.assertEquals(0, retval);
  }

  @Test
  public void smallestEncompassingPowerOfTwoInputZeroOutputZero() {
    // Arrange
    final int input = 0;
    // Act
    final int retval = DataFixUtils.smallestEncompassingPowerOfTwo(input);
    // Assert result
    Assert.assertEquals(0, retval);
  }
}
