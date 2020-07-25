package org.jungrapht.visualization.util;

import org.jungrapht.visualization.layout.model.Rectangle;

public final class RectangleUtils {

  private RectangleUtils() {}

  public static java.awt.geom.Rectangle2D convert(Rectangle r) {
    return new java.awt.geom.Rectangle2D.Double(r.x, r.y, r.width, r.height);
  }

  public static Rectangle convert(java.awt.geom.Rectangle2D r2d) {
    return Rectangle.of(r2d.getX(), r2d.getY(), r2d.getWidth(), r2d.getHeight());
  }
}
