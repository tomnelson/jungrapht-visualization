package org.jungrapht.visualization.layout.algorithms.util;

import java.util.List;

/**
 * InsertionSort counter. Both List and Array versions are provided. For larger sizes (&gt;100),
 * array is faster
 */
public class InsertionSortCounter {

  public static int insertionSortCounter(List<Integer> list) {
    int counter = 0;
    for (int i = 1; i < list.size(); i++) {
      int value = list.get(i);
      int j = i - 1;
      while (j >= 0 && list.get(j) > value) {
        list.set(j + 1, list.get(j));
        counter++;
        j--;
      }
      list.set(j + 1, value);
    }
    return counter;
  }

  public static int insertionSortCounter(int[] array) {
    int counter = 0;
    for (int i = 1; i < array.length; i++) {
      int value = array[i];
      int j = i - 1;
      while (j >= 0 && array[j] > value) {
        array[j + 1] = array[j];
        counter++;
        j--;
      }
      array[j + 1] = value;
    }
    return counter;
  }
}
