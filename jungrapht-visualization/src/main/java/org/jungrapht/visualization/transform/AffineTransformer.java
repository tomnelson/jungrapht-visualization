/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Apr 16, 2005
 */

package org.jungrapht.visualization.transform;

import java.awt.Shape;
import java.awt.geom.*;
import org.jungrapht.visualization.transform.shape.ShapeTransformer;

/**
 * Provides methods to map points from one coordinate system to another, by delegating to a wrapped
 * AffineTransform (uniform) and its inverse.
 *
 * @author Tom Nelson
 */
public class AffineTransformer implements BidirectionalTransformer, ShapeTransformer {

  protected AffineTransform inverse;

  /** The AffineTransform to use; initialized to identity. */
  protected AffineTransform transform = new AffineTransform();

  /** Create an instance that does not transform points. */
  public AffineTransformer() {
    // nothing left to do
  }

  /**
   * Create an instance with the supplied transform.
   *
   * @param transform the transform to use
   */
  public AffineTransformer(AffineTransform transform) {
    if (transform != null) {
      this.transform = transform;
    }
  }

  /** @return Returns the transform. */
  public AffineTransform getTransform() {
    return transform;
  }
  /** @param transform The transform to set. */
  public void setTransform(AffineTransform transform) {
    this.transform = transform;
  }

  /**
   * applies the inverse transform to the supplied point
   *
   * @param p the point to transform
   * @return the transformed point
   */
  public Point2D inverseTransform(Point2D p) {

    return getInverse().transform(p, null);
  }

  public Point2D inverseTransform(double x, double y) {
    return inverseTransform(new Point2D.Double(x, y));
  }

  public AffineTransform getInverse() {
    if (inverse == null) {
      try {
        inverse = transform.createInverse();
      } catch (NoninvertibleTransformException e) {
        e.printStackTrace();
      }
    }
    return inverse;
  }

  /** @return the transform's x scale value */
  public double getScaleX() {
    Point2D p = new Point2D.Double(1, 0);
    p = transform.deltaTransform(p, p);
    return p.distance(0, 0);
  }

  /** @return the transform's y scale value */
  public double getScaleY() {
    Point2D p = new Point2D.Double(0, 1);
    p = transform.deltaTransform(p, p);
    return p.distance(0, 0);
  }

  /** @return the transform's overall scale magnitude */
  public double getScale() {
    return Math.sqrt(transform.getDeterminant());
  }

  public double scale() {
    return getScale();
  }

  /** @return the transform's x shear value */
  public double getShearX() {
    return transform.getShearX();
  }

  /** @return the transform's y shear value */
  public double getShearY() {
    return transform.getShearY();
  }

  /** @return the transform's x translate value */
  public double getTranslateX() {
    return transform.getTranslateX();
  }

  /** @return the transform's y translate value */
  public double getTranslateY() {
    return transform.getTranslateY();
  }

  /**
   * Applies the transform to the supplied point.
   *
   * @param p the point to be transformed
   * @return the transformed point
   */
  public Point2D transform(Point2D p) {
    if (p == null) {
      return null;
    }
    return transform.transform(p, null);
  }

  public Point2D transform(double x, double y) {
    return transform(new Point2D.Double(x, y));
  }

  /**
   * Transform the supplied shape from graph (layout) to screen (view) coordinates.
   *
   * @return the Path2Dof the transformed shape
   */
  public Shape transform(Shape shape) {
    Path2D newPath = new Path2D.Double();
    double[] coords = new double[6];
    for (PathIterator iterator = shape.getPathIterator(null); !iterator.isDone(); iterator.next()) {
      int type = iterator.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          Point2D p = transform(coords[0], coords[1]);
          newPath.moveTo(p.getX(), p.getY());
          break;

        case PathIterator.SEG_LINETO:
          p = transform(coords[0], coords[1]);
          newPath.lineTo(p.getX(), p.getY());
          break;

        case PathIterator.SEG_QUADTO:
          p = transform(coords[0], coords[1]);
          Point2D q = transform(coords[2], coords[3]);
          newPath.quadTo(p.getX(), p.getY(), q.getX(), q.getY());
          break;

        case PathIterator.SEG_CUBICTO:
          p = transform(coords[0], coords[1]);
          q = transform(coords[2], coords[3]);
          Point2D r = transform(coords[4], coords[5]);
          newPath.curveTo(p.getX(), p.getY(), q.getX(), q.getY(), r.getX(), r.getY());
          break;

        case PathIterator.SEG_CLOSE:
          newPath.closePath();
          break;

        default:
      }
    }
    return newPath;
  }

  /**
   * Transform the supplied shape from screen (view) to graph (layout) coordinates.
   *
   * @return the Path2Dof the transformed shape
   */
  public Shape inverseTransform(Shape shape) {
    Path2D newPath = new Path2D.Double();
    double[] coords = new double[6];
    for (PathIterator iterator = shape.getPathIterator(null); !iterator.isDone(); iterator.next()) {
      int type = iterator.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          Point2D p = inverseTransform(coords[0], coords[1]);
          newPath.moveTo(p.getX(), p.getY());
          break;

        case PathIterator.SEG_LINETO:
          p = inverseTransform(coords[0], coords[1]);
          newPath.lineTo(p.getX(), p.getY());
          break;

        case PathIterator.SEG_QUADTO:
          p = inverseTransform(coords[0], coords[1]);
          Point2D q = inverseTransform(coords[2], coords[3]);
          newPath.quadTo(p.getX(), p.getY(), q.getX(), q.getY());
          break;

        case PathIterator.SEG_CUBICTO:
          p = inverseTransform(coords[0], coords[1]);
          q = inverseTransform(coords[2], coords[3]);
          Point2D r = inverseTransform(coords[4], coords[5]);
          newPath.curveTo(p.getX(), p.getY(), q.getX(), q.getY(), r.getX(), r.getY());
          break;

        case PathIterator.SEG_CLOSE:
          newPath.closePath();
          break;

        default:
      }
    }
    return newPath;
  }

  public double getRotation() {
    double[] unitVector = new double[] {0, 0, 1, 0};
    double[] result = new double[4];

    transform.transform(unitVector, 0, result, 0, 2);

    double dy = Math.abs(result[3] - result[1]);
    double length = Point2D.distance(result[0], result[1], result[2], result[3]);
    double rotation = Math.asin(dy / length);

    if (result[3] - result[1] > 0) {
      if (result[2] - result[0] < 0) {
        rotation = Math.PI - rotation;
      }
    } else {
      if (result[2] - result[0] > 0) {
        rotation = 2 * Math.PI - rotation;
      } else {
        rotation = rotation + Math.PI;
      }
    }

    return rotation;
  }
}
