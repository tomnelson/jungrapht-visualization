package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Arrays;

public final class NLogNMedian {

  private NLogNMedian() {}

  public static int median(int[] list) {
    int[] copy = list;
    Arrays.sort(copy);
    return copy[copy.length / 2];
  }
}
