package org.jungrapht.visualization;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class ShapeTest {

  Logger log = LoggerFactory.getLogger(ShapeTest.class);

  @Test
  public void testInside() {
    Rectangle2D r = new Rectangle2D.Double(0, 0, 500, 500);
    Point2D p = new Point2D.Double(0, 0);

    log.info("{} is inside {}: {}", p, r, r.contains(p));

    p = new Point2D.Double(500, 500);
    log.info("{} is inside {}: {}", p, r, r.contains(p));
    log.info("{} is whatever {}: {}", p, r, r.intersects(0, 0, 1, 1));
  }
}
