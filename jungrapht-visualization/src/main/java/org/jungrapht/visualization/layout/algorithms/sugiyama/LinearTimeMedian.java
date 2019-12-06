package org.jungrapht.visualization.layout.algorithms.sugiyama;

public final class LinearTimeMedian {

  private LinearTimeMedian() {}

  public static int select(int[] list) {
    if (list.length < 1) {
      throw new IllegalArgumentException();
    }
    int pos = select(list, 0, list.length, list.length / 2);
    return list[pos];
  }

  public static int select(int[] list, int lo, int hi, int k) {
    int n = hi - lo;
    if (n < 2) {
      return lo;
    }
    double pivot = list[lo + (k * 7919) % n];

    int nLess = 0;
    int nSame = 0;
    int nMore = 0;
    int lo3 = lo;
    int hi3 = hi;
    while (lo3 < hi3) {
      double e = list[lo3];
      int cmp = (int) (e - pivot);
      if (cmp < 0) {
        nLess++;
        lo3++;
      } else if (cmp > 0) {
        swap(list, lo3, --hi3);
        if (nSame > 0) {
          swap(list, hi3, hi3 + nSame);
        }
        nMore++;
      } else {
        nSame++;
        swap(list, lo3, --hi3);
      }
    }
    assert nSame > 0;
    assert nLess + nSame + nMore == n;
    assert list[hi - nMore - 1] == pivot;
    if (k >= n - nMore) {
      return select(list, hi - nMore, hi, k - nLess - nSame);
    } else if (k < nLess) {
      return select(list, lo, lo + nLess, k);
    }
    return lo + k;
  }

  static void swap(int[] array, int i, int j) {
    if (i == j) return;
    int temp = array[i];
    array[i] = array[j];
    array[j] = temp;
    //    log.info("after swap of {} and {}: {}", i, j, Arrays.toString(array));
  }
}
