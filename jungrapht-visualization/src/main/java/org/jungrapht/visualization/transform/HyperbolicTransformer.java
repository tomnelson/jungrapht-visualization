/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.visualization.transform;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HyperbolicTransformer wraps a MutableAffineTransformer and modifies the transform and
 * inverseTransform methods so that they create a fisheye projection of the graph points, with
 * points near the center spread out and points near the edges collapsed onto the circumference of
 * an ellipse or the boundaries of a rectangle
 *
 * <p>HyperbolicTransformer is not an affine transform, but it uses an affine transform to cause
 * translation, scaling, rotation, and shearing while applying a non-affine hyperbolic filter in its
 * transform and inverseTransform methods.
 *
 * @author Tom Nelson
 */
public class HyperbolicTransformer extends LensTransformer implements MutableTransformer {

  public static class Builder<T extends HyperbolicTransformer, B extends Builder<T, B>>
      extends LensTransformer.Builder<T, B> {
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
      return (T) new HyperbolicTransformer(this);
    }
  }

  public static <T extends HyperbolicTransformer> Builder<T, ?> builder(Lens lens) {
    return new Builder<>(lens);
  }

  public static <T extends HyperbolicTransformer> Builder<T, ?> builder(Dimension dimension) {
    return new Builder<>(dimension);
  }

  protected HyperbolicTransformer(Builder builder) {
    this(builder.lens, builder.delegate);
  }

  private static final Logger log = LoggerFactory.getLogger(HyperbolicTransformer.class);

  /**
   * Create an instance, setting values from the passed component and registering to listen for
   * layoutSize changes on the component.
   *
   * @param d the size used for the lens
   */
  protected HyperbolicTransformer(Dimension d) {
    this(d, new MutableAffineTransformer());
  }

  /**
   * create an instance, setting values from the passed component and registering to listen for
   * layoutSize changes on the component
   *
   * @param d the size used for the lens
   */
  protected HyperbolicTransformer(Dimension d, MutableTransformer delegate) {
    super(d, delegate);
  }

  /**
   * Create an instance with a possibly shared transform.
   *
   * @param lens a lens created elsewhere, but on the same component
   */
  protected HyperbolicTransformer(Lens lens, MutableTransformer delegate) {
    super(lens, delegate);
  }

  /** override base class transform to project the fisheye effect */
  public Point2D transform(Point2D graphPoint) {
    if (graphPoint == null) {
      return null;
    }
    Point2D lensCenterInLayoutCoordinates = lens.getCenter();
    Shape lensShape = lens.getLensShape();

    double centerToCorner = lens.getCenterToCorner();
    double ratio = lens.getRatio();
    // transform the point from the graph to the view
    Point2D viewPoint = delegate.transform(graphPoint);

    // calculate point from center
    double dx = viewPoint.getX() - lensCenterInLayoutCoordinates.getX();
    double dy = viewPoint.getY() - lensCenterInLayoutCoordinates.getY();
    // factor out ellipse
    dx *= ratio;
    org.jungrapht.visualization.layout.model.Point pointFromCenter =
        org.jungrapht.visualization.layout.model.Point.of(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);
    double polarPointAngle = polar.theta;
    double polarPointRadius = polar.radius;
    if (!lensShape.contains(viewPoint)) {
      return viewPoint;
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
  public Point2D inverseTransform(Point2D viewPoint) {

    Point2D lensCenterInLayoutCoords = lens.getCenter();
    double viewRadius = lens.getRadius();
    double ratio = lens.getRatio();
    double dx = viewPoint.getX() - lensCenterInLayoutCoords.getX();
    double dy = viewPoint.getY() - lensCenterInLayoutCoords.getY();
    // factor out ellipse
    dx *= ratio;

    org.jungrapht.visualization.layout.model.Point pointFromCenter =
        org.jungrapht.visualization.layout.model.Point.of(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);

    double radius = polar.radius;
    Shape lensShape = lens.getLensShape();

    if (!lensShape.contains(viewPoint)) {
      return delegate.inverseTransform(viewPoint);
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
            projectedPoint.x + lensCenterInLayoutCoords.getX(),
            projectedPoint.y + lensCenterInLayoutCoords.getY());
    return delegate.inverseTransform(translatedBack);
  }
}
