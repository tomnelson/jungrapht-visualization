package org.jungrapht.visualization.layout.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class ThingTest {

  static class Thing {
    static int counter;

    public boolean consider() {
      counter++;
      return true;
    }

    public int getCounter() {
      return counter;
    }
  }

  @Test
  public void testIt() {
    List<Thing> list = new ArrayList();
    for (int i = 0; i < 100; i++) {
      list.add(new Thing());
    }

    int got =
        list.stream()
            .filter(Thing::consider)
            .map(Thing::getCounter)
            .collect(Collectors.toList())
            .get(20);

    System.err.println("got " + got);
  }
}
