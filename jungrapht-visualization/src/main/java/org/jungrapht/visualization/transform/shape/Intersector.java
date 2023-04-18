/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.visualization.transform.shape;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

public class Intersector {

  protected Rectangle rectangle;
  Line2D line;
  Set<Point2D> points = new HashSet<>();

  public Intersector(Rectangle rectangle) {
    this.rectangle = rectangle;
  }

  public Intersector(Rectangle rectangle, Line2D line) {
    this.rectangle = rectangle;
    intersectLine(line);
  }

  public void intersectLine(Line2D line) {
    this.line = line;
    points.clear();
    double rx0 = rectangle.getMinX();
    double ry0 = rectangle.getMinY();
    double rx1 = rectangle.getMaxX();
    double ry1 = rectangle.getMaxY();

    double x1 = line.getX1();
    double y1 = line.getY1();
    double x2 = line.getX2();
    double y2 = line.getY2();

    double dy = y2 - y1;
    double dx = x2 - x1;

    if (dx != 0) {
      double m = dy / dx;
      double b = y1 - m * x1;

      // base of rect where y == ry0
      double x = (ry0 - b) / m;

      if (rx0 <= x && x <= rx1) {
        points.add(new Point2D.Double(x, ry0));
      }

      // top where y == ry1
      x = (ry1 - b) / m;
      if (rx0 <= x && x <= rx1) {
        points.add(new Point2D.Double(x, ry1));
      }

      // left side, where x == rx0
      double y = m * rx0 + b;
      if (ry0 <= y && y <= ry1) {
        points.add(new Point2D.Double(rx0, y));
      }

      // right side, where x == rx1
      y = m * rx1 + b;
      if (ry0 <= y && y <= ry1) {
        points.add(new Point2D.Double(rx1, y));
      }

    } else {

      // base, where y == ry0
      double x = x1;
      if (rx0 <= x && x <= rx1) {
        points.add(new Point2D.Double(x, ry0));
      }

      // top, where y == ry1
      x = x2;
      if (rx0 <= x && x <= rx1) {
        points.add(new Point2D.Double(x, ry1));
      }
    }
  }

  public Line2D getLine() {
    return line;
  }

  public Set<Point2D> getPoints() {
    return points;
  }

  public Rectangle getRectangle() {
    return rectangle;
  }

  public String toString() {
    return "Rectangle: " + rectangle + ", points:" + points;
  }
}
