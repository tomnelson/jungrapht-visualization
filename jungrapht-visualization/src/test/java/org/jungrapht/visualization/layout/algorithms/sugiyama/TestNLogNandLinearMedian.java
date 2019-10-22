package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jungrapht.visualization.layout.algorithms.util.sugiyama.LinearTimeMedian;
import org.jungrapht.visualization.layout.algorithms.util.sugiyama.NLogNMedian;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNLogNandLinearMedian {

  private static final Logger log = LoggerFactory.getLogger(TestNLogNandLinearMedian.class);

  int[] array;

  /** create a randomized array of 20 ints */
  @Before
  public void setup() {
    int max = 1000;
    List<Integer> list = IntStream.range(0, max).boxed().collect(Collectors.toList());
    Collections.shuffle(list);
    array = list.stream().mapToInt(i -> i).toArray();
    //    array = new int[] {0,4,8,1,8,9,5,4,7,6,1,9,0};
  }

  /**
   * show the original array and the original array sorted by Arrays.sort method
   *
   * @param array
   */
  private void showArrayAndSortedArray(int[] array) {
    log.info("array is {}", array);
    int[] sorted = new int[array.length];
    System.arraycopy(array, 0, sorted, 0, sorted.length);
    Arrays.sort(sorted);
    log.info("sorted is {}", sorted);
  }

  /** use quickSelectMedian to find the median of the array. assert that the value is 9 */
  @Test
  public void testFindMedianNLogN() {
    showArrayAndSortedArray(array);
    long time = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      int medianValue = NLogNMedian.median(array);
    }
    System.err.println("nlogn took " + (System.currentTimeMillis() - time));
    //    log.info("median value of {} is {}", Arrays.toString(array), medianValue);
    //    Assert.assertEquals(9, medianValue);
  }

  @Test
  public void testFindMedianLinear() {
    showArrayAndSortedArray(array);
    long time = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      int medianValue = LinearTimeMedian.select(array);
    }
    System.err.println("linear took " + (System.currentTimeMillis() - time));
    //    log.info("median value of {} is {}", Arrays.toString(array), medianValue);
    //    Assert.assertEquals(9, medianValue);
  }
}
