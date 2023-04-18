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
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.jungrapht.visualization.transform.HyperbolicTransformer;
import org.jungrapht.visualization.transform.Intersections;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HyperbolicShapeTransformer extends HyperbolicTransformer and adds implementations for methods in
 * ShapeFlatnessTransformer. It modifies the shapes (Vertex, Edge, and Arrowheads) so that they are
 * distorted by the hyperbolic transformation
 *
 * @author Tom Nelson
 */
public class HyperbolicShapeTransformer extends HyperbolicTransformer
    implements ShapeFlatnessTransformer {

  public static class Builder<T extends HyperbolicShapeTransformer, B extends Builder<T, B>>
      extends HyperbolicTransformer.Builder<T, B> {
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
      return (T) new HyperbolicShapeTransformer(this);
    }
  }

  public static Builder<?, ?> builder(Lens lens) {
    return new Builder<>(lens);
  }

  public static Builder<?, ?> builder(Dimension dimension) {
    return new Builder<>(dimension);
  }

  protected HyperbolicShapeTransformer(Builder builder) {
    super(builder);
  }

  private static final Logger log = LoggerFactory.getLogger(HyperbolicShapeTransformer.class);

  /**
   * Create an instance, setting values from the passed component and registering to listen for
   * layoutSize changes on the component.
   *
   * @param lens the {@link Lens} to consider for transform
   * @param delegate transformer to use
   */
  protected HyperbolicShapeTransformer(Lens lens, MutableTransformer delegate) {
    super(lens, delegate);
  }

  /**
   * Create an instance, setting values from the passed component and registering to listen for
   * layoutSize changes on the component, with a possibly shared transform <code>delegate</code>.
   *
   * @param d the size for the lens
   * @param delegate the viewTransformer to use
   */
  protected HyperbolicShapeTransformer(Dimension d, MutableTransformer delegate) {
    super(d, delegate);
  }

  /**
   * Transform the supplied shape with the overridden transform method so that the shape is
   * distorted by the hyperbolic transform.
   *
   * @param shape a shape to transform
   * @return a Path2Dfor the transformed shape
   */
  public Shape transform(Shape shape) {
    return transform(shape, 0);
  }

  public Shape transform(Shape shape, double flatness) {
    //    shape = super.transform(shape);
    if (log.isTraceEnabled()) {
      log.trace("transforming {}", shape);
    }
    Path2D newPath = new Path2D.Double();
    double[] coords = new double[6];
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
          Point2D p = _transform(new Point2D.Double(coords[0], coords[1]));
          newPath.moveTo(p.getX(), p.getY());
          break;

        case PathIterator.SEG_LINETO:
          p = _transform(new Point2D.Double(coords[0], coords[1]));
          newPath.lineTo(p.getX(), p.getY());
          break;

        case PathIterator.SEG_QUADTO:
          p = _transform(new Point2D.Double(coords[0], coords[1]));
          Point2D q = _transform(new Point2D.Double(coords[2], coords[3]));
          newPath.quadTo(p.getX(), p.getY(), q.getX(), q.getY());
          break;

        case PathIterator.SEG_CUBICTO:
          p = _transform(new Point2D.Double(coords[0], coords[1]));
          q = _transform(new Point2D.Double(coords[2], coords[3]));
          Point2D r = _transform(new Point2D.Double(coords[4], coords[5]));
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

  public Shape inverseTransform(Shape shape) {
    Path2D newPath = new Path2D.Double();
    double[] coords = new double[6];
    for (PathIterator iterator = shape.getPathIterator(null); !iterator.isDone(); iterator.next()) {
      int type = iterator.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          Point2D p = _inverseTransform(new Point2D.Double(coords[0], coords[1]));
          newPath.moveTo(p.getX(), p.getY());
          break;

        case PathIterator.SEG_LINETO:
          p = _inverseTransform(new Point2D.Double(coords[0], coords[1]));
          newPath.lineTo(p.getX(), p.getY());
          break;

        case PathIterator.SEG_QUADTO:
          p = _inverseTransform(new Point2D.Double(coords[0], coords[1]));
          Point2D q = _inverseTransform(new Point2D.Double(coords[2], coords[3]));
          newPath.quadTo(p.getX(), p.getY(), q.getX(), q.getY());
          break;

        case PathIterator.SEG_CUBICTO:
          p = _inverseTransform(new Point2D.Double(coords[0], coords[1]));
          q = _inverseTransform(new Point2D.Double(coords[2], coords[3]));
          Point2D r = _inverseTransform(new Point2D.Double(coords[4], coords[5]));
          newPath.curveTo(p.getX(), p.getY(), q.getX(), q.getY(), r.getX(), r.getY());
          break;

        case PathIterator.SEG_CLOSE:
          newPath.closePath();
          break;

        default:
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("hyperbolic shape bounds: {}", newPath.getBounds());
    }

    return newPath;
  }
  /** override base class transform to project the fisheye effect */
  private Point2D _transform(Point2D graphPoint) {
    if (graphPoint == null) {
      return null;
    }
    Point2D lensCenterInLayoutCoordinates = lens.getCenter();

    Shape lensShape = lens.getLensShape();

    double centerToCorner = lens.getCenterToCorner();
    double ratio = lens.getRatio();
    // calculate point from center
    double dx = graphPoint.getX() - lensCenterInLayoutCoordinates.getX();
    double dy = graphPoint.getY() - lensCenterInLayoutCoordinates.getY();
    // factor out ellipse
    dx *= ratio;
    org.jungrapht.visualization.layout.model.Point pointFromCenter =
        org.jungrapht.visualization.layout.model.Point.of(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);
    double polarPointAngle = polar.theta;
    double polarPointRadius = polar.radius;
    if (!lensShape.contains(graphPoint)) {
      return graphPoint;
    }

    double mag = Math.tan(Math.PI / 2 * lens.getMagnification());
    polarPointRadius *= mag;

    polarPointRadius = Math.min(polarPointRadius, centerToCorner);
    polarPointRadius /= centerToCorner;
    polarPointRadius *= Math.PI / 2;
    polarPointRadius = Math.abs(Math.atan(polarPointRadius));
    polarPointRadius *= centerToCorner;
    polarPointRadius = Math.min(polarPointRadius, centerToCorner);

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
    projectedPoint =
        org.jungrapht.visualization.layout.model.Point.of(
            projectedPoint.x / ratio, projectedPoint.y);
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

    radius /= viewRadius;
    radius = Math.abs(Math.tan(radius));
    radius /= Math.PI / 2;
    radius *= viewRadius;
    double mag = Math.tan(Math.PI / 2 * lens.getMagnification());
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
}
