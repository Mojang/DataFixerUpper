package com.mojang.datafixers.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PairTest {
  private static final String FIRST_ELEMENT = "First";
  private static final String SECOND_ELEMENT = "Second";

  private Pair<String, String> pair;

  @BeforeEach
  public void setUp() {
    pair = new Pair<>(FIRST_ELEMENT, SECOND_ELEMENT);
  }

  @Test
  public void getFirstTest() {
    assertEquals(FIRST_ELEMENT, pair.getFirst());
  }

  @Test
  public void getSecondTest() {
    assertEquals(SECOND_ELEMENT, pair.getSecond());
  }

}
