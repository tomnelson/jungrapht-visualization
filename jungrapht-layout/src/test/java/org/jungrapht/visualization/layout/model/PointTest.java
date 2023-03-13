package org.jungrapht.visualization.layout.model;

import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointTest {

  private static final Logger log = LoggerFactory.getLogger(PointTest.class);

  @Test
  public void testGeometricMedian() {
    Collection<Point> points = List.of(Point.of(0, 0), Point.of(0, 0), Point.of(0, 12));
    Point median = Point.geometricMedian(points);
    log.info("median of {} is {}", points, median);

    points = List.of(Point.of(1, 1), Point.of(3, 3));
    median = Point.geometricMedian(points);
    log.info("median of {} is {}", points, median);
  }
}
