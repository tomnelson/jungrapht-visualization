package org.jungrapht.visualization.control;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

public interface MultiSelectionStrategy {

  Shape getInitialShape(Point2D location);

  void closeShape();

  Shape updateShape(Point2D down, Point2D out);

  class Rectangular implements MultiSelectionStrategy {

    RectangularShape shape;

    @Override
    public Shape getInitialShape(Point2D location) {
      this.shape = new Rectangle2D.Double(location.getX(), location.getY(), 0, 0);
      return this.shape;
    }

    public void closeShape() {}

    public Shape updateShape(Point2D down, Point2D out) {
      shape.setFrameFromDiagonal(down, out);
      return this.shape;
    }
  }

  class Arbitrary implements MultiSelectionStrategy {

    Path2D shape;

    @Override
    public Shape getInitialShape(Point2D location) {
      this.shape = new Path2D.Double();
      this.shape.moveTo(location.getX(), location.getY());
      return this.shape;
    }

    public void closeShape() {
      this.shape.closePath();
    }

    public Shape updateShape(Point2D down, Point2D out) {
      Path2D path = shape;
      path.lineTo(out.getX(), out.getY());
      return this.shape;
    }
  }
}
