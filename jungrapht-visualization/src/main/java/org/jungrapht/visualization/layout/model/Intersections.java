package org.jungrapht.visualization.layout.model;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Intersections {

  private static final Logger log = LoggerFactory.getLogger(Intersections.class);

  /**
   * for log messages
   *
   * @param line
   * @return
   */
  private static String from(Line2D line) {
    return line.getX1() + "," + line.getY1() + " -> " + line.getX2() + "," + line.getY2();
  }

  public static Optional<Point2D> getIntersectionPoint(Line2D vector, Rectangle2D r) {
    Line2D top = new Line2D.Double(r.getMinX(), r.getMinY(), r.getMaxX(), r.getMinY());
    if (vector.intersectsLine(top)) {
      return Optional.of(getIntersectionPoint(vector, top));
    }
    Line2D bottom = new Line2D.Double(r.getMinX(), r.getMaxY(), r.getMaxX(), r.getMaxY());
    if (vector.intersectsLine(bottom)) {
      return Optional.of(getIntersectionPoint(vector, bottom));
    }
    Line2D left = new Line2D.Double(r.getMinX(), r.getMinY(), r.getMinX(), r.getMaxY());
    if (vector.intersectsLine(left)) {
      return Optional.of(getIntersectionPoint(vector, left));
    }
    Line2D right = new Line2D.Double(r.getMaxX(), r.getMinY(), r.getMaxX(), r.getMaxY());
    if (vector.intersectsLine(right)) {
      return Optional.of(getIntersectionPoint(vector, right));
    }
    return Optional.empty();
  }

  private static Point2D getIntersectionPoint(Line2D line1, Line2D line2) {

    double line1x1 = line1.getX1();
    double line1y1 = line1.getY1();
    double line1x2 = line1.getX2();
    double line1y2 = line1.getY2();

    double line2x1 = line2.getX1();
    double line2y1 = line2.getY1();
    double line2x2 = line2.getX2();
    double line2y2 = line2.getY2();

    double denomintator =
        ((line1x1 - line1x2) * (line2y1 - line2y2) - (line1y1 - line1y2) * (line2x1 - line2x2));

    double x =
        ((line1x2 - line1x1) * (line2x1 * line2y2 - line2x2 * line2y1)
                - (line2x2 - line2x1) * (line1x1 * line1y2 - line1x2 * line1y1))
            / denomintator;
    double y =
        ((line2y1 - line2y2) * (line1x1 * line1y2 - line1x2 * line1y1)
                - (line1y1 - line1y2) * (line2x1 * line2y2 - line2x2 * line2y1))
            / denomintator;

    return new Point2D.Double(x, y);
  }
}
