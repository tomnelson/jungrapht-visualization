package org.jungrapht.visualization.util;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.layout.model.Point;

public final class PointUtils {

  private PointUtils() {}

  public static Point2D convert(Point p) {
    return new Point2D.Double(p.x, p.y);
  }

  public static Point convert(Point2D p2d) {
    return Point.of(p2d.getX(), p2d.getY());
  }
}
