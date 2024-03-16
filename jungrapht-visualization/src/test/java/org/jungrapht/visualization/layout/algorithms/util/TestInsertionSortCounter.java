package org.jungrapht.visualization.layout.algorithms.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestInsertionSortCounter {

  private static final Logger log = LoggerFactory.getLogger(TestInsertionSortCounter.class);

  int[] array = new int[] {8, 2, 8, 5, 3, 7, 9, 2, 5, 4, 8, 4, 9};

  List<Integer> list = Arrays.asList(8, 2, 8, 5, 3, 7, 9, 2, 5, 4, 8, 4, 9);

  @Before
  public void setup() {
    int size = 1000;
    array = new int[size];
    list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      int val = (int) (Math.random() * 1000);
      array[i] = val;
      list.add(val);
    }
  }

  @Test
  public void testOne() {
    long start = System.currentTimeMillis();
    int count = InsertionSortCounter.insertionSortCounter(array);
    log.info("array took {}", (System.currentTimeMillis() - start));
    log.info("count: {}, array: {}", count, array);
  }

  @Test
  public void testTwo() {
    long start = System.currentTimeMillis();
    int count = InsertionSortCounter.insertionSortCounter(list);
    log.info("list took {}", (System.currentTimeMillis() - start));
    log.info("count: {}, array: {}", count, list);
  }

  @Test
  public void testhree() {
    long start = System.currentTimeMillis();
    int count =
        InsertionSortCounter.insertionSortCounter(
            list.stream().mapToInt(Integer::intValue).toArray());
    log.info("list to array took {}", (System.currentTimeMillis() - start));
    log.info("count: {}, array: {}", count, list);
  }
}
