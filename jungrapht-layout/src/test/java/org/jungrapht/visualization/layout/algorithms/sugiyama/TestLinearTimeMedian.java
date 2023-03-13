package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.junit.Assert;
import org.junit.Test;

public class TestLinearTimeMedian {

  @Test
  public void testMedianEven() {
    int[] arr = {2, 8, 5, 1, 10, 7};
    int expectedMedian = 7;
    int actualMedian = LinearTimeMedian.select(arr);
    Assert.assertEquals(expectedMedian, actualMedian);
  }
}
