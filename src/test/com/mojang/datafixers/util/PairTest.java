// Copyright (c) Diffblue Limited. All rights reserved.
// Licensed under the MIT license.

package com.mojang.datafixers.util;

import com.mojang.datafixers.util.Pair;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PairTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void toStringOutputNotNull() {
    // Arrange
    final Unit objectUnderTest = Unit.INSTANCE;
    // Act
    final String retval = objectUnderTest.toString();
    // Assert result
    Assert.assertEquals("Unit", retval);
  }
}
