package org.jungrapht.visualization.layout.algorithms.util.synthetics;

import static org.junit.jupiter.api.Assertions.*;

import org.jungrapht.visualization.layout.util.synthetics.SingletonTransformer;
import org.junit.jupiter.api.Test;

/**
 * Test to ensure that keys return expected values, that a second transformation of the same key
 * will yield a result that is == to the first time, and that the underlying map does not grow when
 * a duplicate key is queried
 */
public class SingletonTransformerTest {

  @Test
  public void test() {
    SingletonTransformer<Integer, Integer> singletonTransformer =
        new SingletonTransformer<>(i -> i + 1);

    int got2 = singletonTransformer.apply(1);
    int got3 = singletonTransformer.apply(2);

    assertEquals(2, got2);
    assertEquals(3, got3);

    int gotDupe = singletonTransformer.apply(1);
    assertTrue(gotDupe == got2);

    assertEquals(2, singletonTransformer.getTransformedMap().size());
  }
}
