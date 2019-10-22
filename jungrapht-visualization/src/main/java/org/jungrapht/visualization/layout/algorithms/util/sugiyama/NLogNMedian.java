package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.Arrays;

public final class NLogNMedian {

  private NLogNMedian() {}

  public static int median(int[] list) {
    int[] copy = list;
    //                Arrays.copyOf(list, list.length);
    Arrays.sort(copy);
    return copy[copy.length / 2];
  }
}
