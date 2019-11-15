package org.jungrapht.visualization.layout.model;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class IntersectionsTest {

  Rectangle2D rectangle = new Rectangle2D.Double(10, 10, 80, 80);

  @Test
  public void testVectors() {
    Line2D vector = new Line2D.Double(50, 50, 60, 60);
    Optional<Point2D> intersectionPoint = Intersections.getIntersectionPoint(vector, rectangle);
    Assert.assertTrue(intersectionPoint.isEmpty());

    vector = new Line2D.Double(50, 50, 200, 50);
    intersectionPoint = Intersections.getIntersectionPoint(vector, rectangle);
    Assert.assertTrue(intersectionPoint.isPresent());
    Assert.assertEquals(new Point2D.Double(90, 50), intersectionPoint.get());

    vector = new Line2D.Double(50, 50, 110, -10);
    intersectionPoint = Intersections.getIntersectionPoint(vector, rectangle);
    Assert.assertTrue(intersectionPoint.isPresent());
    Assert.assertEquals(new Point2D.Double(90, 10), intersectionPoint.get());

    vector = new Line2D.Double(50, 50, -10, -10);
    intersectionPoint = Intersections.getIntersectionPoint(vector, rectangle);
    Assert.assertTrue(intersectionPoint.isPresent());
    Assert.assertEquals(new Point2D.Double(10, 10), intersectionPoint.get());

    vector = new Line2D.Double(50, 50, 50, 100);
    intersectionPoint = Intersections.getIntersectionPoint(vector, rectangle);
    Assert.assertTrue(intersectionPoint.isPresent());
    Assert.assertEquals(new Point2D.Double(50, 90), intersectionPoint.get());
  }
}
