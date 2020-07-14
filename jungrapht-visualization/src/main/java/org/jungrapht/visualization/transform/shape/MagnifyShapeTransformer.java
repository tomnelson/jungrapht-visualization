/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.visualization.transform.shape;

import java.awt.*;
import java.awt.geom.*;
import java.util.Optional;
import org.jungrapht.visualization.layout.model.Intersections;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.MagnifyTransformer;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MagnifyShapeTransformer extends MagnifyTransformer and adds implementations for methods in
 * ShapeTransformer. It modifies the shapes (Vertex, Edge, and Arrowheads) so that they are enlarged
 * by the magnify transformation.
 *
 * @author Tom Nelson
 */
public class MagnifyShapeTransformer extends MagnifyTransformer
    implements ShapeFlatnessTransformer {

  public static class Builder<T extends MagnifyShapeTransformer, B extends Builder<T, B>>
      extends MagnifyTransformer.Builder<T, B> {

    public Builder(Lens lens) {
      super(lens);
    }

    public Builder(Dimension dimension) {
      super(dimension);
    }

    public T build() {
      if (lens == null && dimension != null) {
        lens = new Lens();
      }
      return (T) new MagnifyShapeTransformer(this);
    }
  }

  public static Builder<?, ?> builder(Lens lens) {
    return new Builder<>(lens);
  }

  public static Builder<?, ?> builder(Dimension dimension) {
    return new Builder<>(dimension);
  }

  protected MagnifyShapeTransformer(Builder builder) {
    super(builder);
  }

  private static final Logger log = LoggerFactory.getLogger(MagnifyShapeTransformer.class);
  /** @param d the size used for the lens */
  protected MagnifyShapeTransformer(Dimension d) {
    super(d);
  }

  /**
   * @param d the size used for the lens
   * @param delegate the layoutTransformer to use
   */
  protected MagnifyShapeTransformer(Dimension d, MutableTransformer delegate) {
    super(d, delegate);
  }

  protected MagnifyShapeTransformer(Lens lens, MutableTransformer delegate) {
    super(lens, delegate);
  }

  /**
   * Transform the supplied shape with the overridden transform method so that the shape is
   * distorted by the magnify transform.
   *
   * @param shape a shape to transform
   * @return a GeneralPath for the transformed shape
   */
  public Shape transform(Shape shape) {
    return transform(shape, 0);
  }

  public Shape transform(Shape shape, float flatness) {
    if (log.isTraceEnabled()) {
      log.trace("transform {}", shape);
    }
    GeneralPath newPath = new GeneralPath();
    float[] coords = new float[6];
    PathIterator iterator = null;
    if (flatness == 0) {
      iterator = shape.getPathIterator(null);
    } else {
      iterator = shape.getPathIterator(null, flatness);
    }
    for (; !iterator.isDone(); iterator.next()) {
      int type = iterator.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          Point2D p = _transform(new Point2D.Float(coords[0], coords[1]));
          newPath.moveTo((float) p.getX(), (float) p.getY());
          break;

        case PathIterator.SEG_LINETO:
          p = _transform(new Point2D.Float(coords[0], coords[1]));
          newPath.lineTo((float) p.getX(), (float) p.getY());
          break;

        case PathIterator.SEG_QUADTO:
          p = _transform(new Point2D.Float(coords[0], coords[1]));
          Point2D q = _transform(new Point2D.Float(coords[2], coords[3]));
          newPath.quadTo((float) p.getX(), (float) p.getY(), (float) q.getX(), (float) q.getY());
          break;

        case PathIterator.SEG_CUBICTO:
          p = _transform(new Point2D.Float(coords[0], coords[1]));
          q = _transform(new Point2D.Float(coords[2], coords[3]));
          Point2D r = _transform(new Point2D.Float(coords[4], coords[5]));
          newPath.curveTo(
              (float) p.getX(),
              (float) p.getY(),
              (float) q.getX(),
              (float) q.getY(),
              (float) r.getX(),
              (float) r.getY());
          break;

        case PathIterator.SEG_CLOSE:
          newPath.closePath();
          break;
      }
    }
    log.info("magnified shape bounds: {}", newPath.getBounds());
    return newPath;
  }

  public Shape inverseTransform(Shape shape) {
    GeneralPath newPath = new GeneralPath();
    float[] coords = new float[6];
    for (PathIterator iterator = shape.getPathIterator(null); !iterator.isDone(); iterator.next()) {
      int type = iterator.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          Point2D p = _inverseTransform(new Point2D.Float(coords[0], coords[1]));
          newPath.moveTo((float) p.getX(), (float) p.getY());
          break;

        case PathIterator.SEG_LINETO:
          p = _inverseTransform(new Point2D.Float(coords[0], coords[1]));
          newPath.lineTo((float) p.getX(), (float) p.getY());
          break;

        case PathIterator.SEG_QUADTO:
          p = _inverseTransform(new Point2D.Float(coords[0], coords[1]));
          Point2D q = _inverseTransform(new Point2D.Float(coords[2], coords[3]));
          newPath.quadTo((float) p.getX(), (float) p.getY(), (float) q.getX(), (float) q.getY());
          break;

        case PathIterator.SEG_CUBICTO:
          p = _inverseTransform(new Point2D.Float(coords[0], coords[1]));
          q = _inverseTransform(new Point2D.Float(coords[2], coords[3]));
          Point2D r = _inverseTransform(new Point2D.Float(coords[4], coords[5]));
          newPath.curveTo(
              (float) p.getX(),
              (float) p.getY(),
              (float) q.getX(),
              (float) q.getY(),
              (float) r.getX(),
              (float) r.getY());
          break;

        case PathIterator.SEG_CLOSE:
          newPath.closePath();
          break;
      }
    }
    return newPath;
  }

  private Point2D _transform(Point2D graphPoint) {
    if (graphPoint == null) {
      return null;
    }
    Point2D lensCenterInLayoutCoordinates = lens.getCenter();

    Shape lensShape = lens.getLensShape();
    // use the layoutTransform to transform the point from the graph
    // with the delegate layout transform to accommodate any translation/etc
    //    Point2D viewPoint = layoutTransformer.transform(graphPoint);

    // calculate point from center
    double dx = graphPoint.getX() - lensCenterInLayoutCoordinates.getX();
    double dy = graphPoint.getY() - lensCenterInLayoutCoordinates.getY();
    // pointFromCenter is in layout coordinates
    org.jungrapht.visualization.layout.model.Point pointFromCenter =
        org.jungrapht.visualization.layout.model.Point.of(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);
    double polarPointAngle = polar.theta;
    double polarPointRadius = polar.radius;
    if (!lensShape.contains(graphPoint)) {
      return graphPoint;
    }

    double mag = lens.getMagnification();
    // push the point out from the center by a factor of the lens magnification
    polarPointRadius *= mag;

    if (lensShape instanceof Ellipse2D) {
      double lensRadius = lens.getRadius();
      polarPointRadius = Math.min(polarPointRadius, lensRadius);
    } else if (lensShape instanceof Rectangle2D) {
      // projected the point away from the center by a factor of the lens magnification
      org.jungrapht.visualization.layout.model.Point projectedPoint =
          PolarPoint.polarToCartesian(polarPointAngle, polarPointRadius);
      // create a line from the lens center (layout coords) to the projected point (layout coords)
      Line2D vector =
          new Line2D.Double(
              lensCenterInLayoutCoordinates.getX(),
              lensCenterInLayoutCoordinates.getY(),
              lensCenterInLayoutCoordinates.getX() + projectedPoint.x,
              lensCenterInLayoutCoordinates.getY() + projectedPoint.y);

      Rectangle2D lensRectangle = (Rectangle2D) lens.getLensShape();
      // see if the vector intersects an edge of the lens
      Optional<Point2D> intersectionPointOptional =
          Intersections.getIntersectionPoint(vector, lensRectangle);
      if (intersectionPointOptional.isPresent()) {
        // this intersection point is in layout coords
        Point2D intersectionPoint = intersectionPointOptional.get();
        // radius is now the distance from center to the intersection point (shorten it)
        polarPointRadius = lensCenterInLayoutCoordinates.distance(intersectionPoint);
      }
    }
    org.jungrapht.visualization.layout.model.Point projectedPoint =
        PolarPoint.polarToCartesian(polarPointAngle, polarPointRadius);
    Point2D translatedBack =
        new Point2D.Double(
            projectedPoint.x + lensCenterInLayoutCoordinates.getX(),
            projectedPoint.y + lensCenterInLayoutCoordinates.getY());
    return translatedBack;
  }

  /** override base class to un-project the fisheye effect */
  private Point2D _inverseTransform(Point2D viewPoint) {

    viewPoint = delegate.inverseTransform(viewPoint);
    Point2D viewCenter = lens.getCenter();
    double viewRadius = lens.getRadius();
    double ratio = lens.getRatio();
    double dx = viewPoint.getX() - viewCenter.getX();
    double dy = viewPoint.getY() - viewCenter.getY();
    // factor out ellipse
    dx *= ratio;

    org.jungrapht.visualization.layout.model.Point pointFromCenter =
        org.jungrapht.visualization.layout.model.Point.of(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);

    double radius = polar.radius;
    if (radius > viewRadius) {
      return viewPoint;
    }

    double mag = lens.getMagnification();
    radius /= mag;
    polar = polar.newRadius(radius);
    org.jungrapht.visualization.layout.model.Point projectedPoint =
        PolarPoint.polarToCartesian(polar);
    projectedPoint = Point.of(projectedPoint.x / ratio, projectedPoint.y);
    Point2D translatedBack =
        new Point2D.Double(
            projectedPoint.x + viewCenter.getX(), projectedPoint.y + viewCenter.getY());
    return translatedBack;
  }

  /**
   * Magnify the shape, without considering the Lens.
   *
   * @param shape the shape to magnify
   * @return the transformed shape
   */
  public Shape magnify(Shape shape) {
    return magnify(shape, 0);
  }

  public Shape magnify(Shape shape, float flatness) {
    GeneralPath newPath = new GeneralPath();
    float[] coords = new float[6];
    PathIterator iterator = null;
    if (flatness == 0) {
      iterator = shape.getPathIterator(null);
    } else {
      iterator = shape.getPathIterator(null, flatness);
    }
    for (; !iterator.isDone(); iterator.next()) {
      int type = iterator.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          Point2D p = magnify(new Point2D.Float(coords[0], coords[1]));
          newPath.moveTo((float) p.getX(), (float) p.getY());
          break;

        case PathIterator.SEG_LINETO:
          p = magnify(new Point2D.Float(coords[0], coords[1]));
          newPath.lineTo((float) p.getX(), (float) p.getY());
          break;

        case PathIterator.SEG_QUADTO:
          p = magnify(new Point2D.Float(coords[0], coords[1]));
          Point2D q = magnify(new Point2D.Float(coords[2], coords[3]));
          newPath.quadTo((float) p.getX(), (float) p.getY(), (float) q.getX(), (float) q.getY());
          break;

        case PathIterator.SEG_CUBICTO:
          p = magnify(new Point2D.Float(coords[0], coords[1]));
          q = magnify(new Point2D.Float(coords[2], coords[3]));
          Point2D r = magnify(new Point2D.Float(coords[4], coords[5]));
          newPath.curveTo(
              (float) p.getX(),
              (float) p.getY(),
              (float) q.getX(),
              (float) q.getY(),
              (float) r.getX(),
              (float) r.getY());
          break;

        case PathIterator.SEG_CLOSE:
          newPath.closePath();
          break;
      }
    }
    return newPath;
  }
}
