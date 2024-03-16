package org.jungrapht.visualization.layout.model;

import org.junit.Test;

public class ModelTest {

  @Test
  public void testIntersection() {
    Rectangle one = Rectangle.of(0, 0, 10, 10);
    Rectangle two = Rectangle.of(5, 5, 10, 10);

    Rectangle intersection = one.intersect(two);
    System.err.println("intersection is " + intersection);

    Rectangle three = Rectangle.of(20, 20, 10, 10);
    intersection = one.intersect(three);
    System.err.println("intersection is " + intersection);
  }
}
