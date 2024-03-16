package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Arrays;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvgMedianTests {

  private static Logger log = LoggerFactory.getLogger(AvgMedianTests.class);

  @Test
  public void testOne() {

    test(new int[] {0, 5, 10});
    test(new int[] {5});
    test(new int[0]);
    test(new int[] {0, 4});
    test(new int[] {0, 5, 6, 10});
    test(new int[] {0, 2, 3, 4, 5, 6, 9, 10});
  }

  private void test(int[] P) {
    log.info("avg median of {} is {}", Arrays.toString(P), avgMedianValue(P));
  }

  private double avgMedianValue(int[] P) {
    int m = (P.length) / 2;
    log.info("for {}, m = {}", Arrays.toString(P), m);
    if (P.length == 0) {
      return -1;
    } else if (P.length % 2 == 1) {
      return P[m];
    } else if (P.length == 2) {
      return (P[0] + P[1]) / 2;
    } else {
      double left = P[m - 1] - P[0];
      log.info("left is {}", left);
      double right = P[P.length - 1] - P[m];
      log.info("right is {}", right);
      return (P[m - 1] * right + P[m] * left) / (left + right);
    }
  }
}
