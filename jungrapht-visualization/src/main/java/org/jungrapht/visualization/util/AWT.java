package org.jungrapht.visualization.util;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jungrapht.visualization.layout.model.Circle;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;

/** Functions to convert between awt geometry objects and jungrapht-layout geometry objects */
public final class AWT {

  private AWT() {}

  public static Point2D convert(Point p) {
    return PointUtils.convert(p);
  }

  public static Point convert(Point2D p2d) {
    return PointUtils.convert(p2d);
  }

  public static Rectangle2D convert(Rectangle r) {
    return RectangleUtils.convert(r);
  }

  public static Rectangle convert(java.awt.geom.Rectangle2D r2d) {
    return RectangleUtils.convert(r2d);
  }

  public static Ellipse2D convert(Circle circle) {
    return new Ellipse2D.Double(
        circle.center.x - circle.radius,
        circle.center.y - circle.radius,
        2 * circle.radius,
        2 * circle.radius);
  }

  public static Circle convert(Ellipse2D ellipse) {
    return Circle.of(
        Point.of(ellipse.getCenterX(), ellipse.getCenterY()),
        Math.max(ellipse.getWidth(), ellipse.getHeight()));
  }
}
