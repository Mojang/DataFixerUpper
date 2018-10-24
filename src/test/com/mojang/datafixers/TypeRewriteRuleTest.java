// Copyright (c) Diffblue Limited. All rights reserved.
// Licensed under the MIT license.

package com.mojang.datafixers;

import com.mojang.datafixers.TypeRewriteRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TypeRewriteRuleTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void seqInputNull0OutputNull() {

    // Arrange
    final TypeRewriteRule firstRule = null;
    final TypeRewriteRule[] rules = {};

    // Act
    final TypeRewriteRule retval = TypeRewriteRule.seq(firstRule, rules);

    // Assert result
    Assert.assertNull(retval);
  }
}
