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
import org.jungrapht.visualization.layout.model.Intersections;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MagnifyTransformer wraps a MutableAffineTransformer and modifies the transform and
 * inverseTransform methods so that they create an enlarging projection of the graph points.
 *
 * <p>MagnifyTransformer uses an affine transform to cause translation, scaling, rotation, and
 * shearing while applying a separate magnification filter in its transform and inverseTransform
 * methods.
 *
 * @author Tom Nelson
 */
public class MagnifyTransformer extends LensTransformer implements MutableTransformer {

  public static class Builder<T extends MagnifyTransformer, B extends Builder<T, B>>
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
      return (T) new MagnifyTransformer(this);
    }
  }

  public static Builder<?, ?> builder(Lens lens) {
    return new Builder<>(lens);
  }

  public static Builder<?, ?> builder(Dimension dimension) {
    return new Builder<>(dimension);
  }

  protected MagnifyTransformer(Builder builder) {
    this(builder.lens, builder.delegate);
  }

  private static final Logger log = LoggerFactory.getLogger(MagnifyTransformer.class);

  /**
   * Create an instance, setting values from the passed component and registering to listen for
   * layoutSize changes on the component.
   *
   * @param d the size used for the lens
   */
  protected MagnifyTransformer(Dimension d) {
    this(d, new MutableAffineTransformer());
  }

  protected MagnifyTransformer(Lens lens) {
    this(lens, new MutableAffineTransformer());
  }

  /**
   * Create an instance with a possibly shared transform.
   *
   * @param d the size used for the lens
   * @param delegate the layoutTransformer to use
   */
  public MagnifyTransformer(Dimension d, MutableTransformer delegate) {
    super(d, delegate);
  }

  public MagnifyTransformer(Lens lens, MutableTransformer delegate) {
    super(lens, delegate);
  }

  /** override base class transform to project the fisheye effect */
  /**
   * @param graphPoint is a location of something in the graph in the layout coordinate system
   * @return that location transformed to the view coordinate system and possibly further
   *     transformed by the lens magnification
   */
  public Point2D transform(Point2D graphPoint) {
    if (graphPoint == null) {
      return null;
    }
    Point2D lensCenterInLayoutCoordinates = lens.getCenter();
    Shape lensShape = lens.getLensShape();
    // use the layoutTransform to transform the point from the graph
    // with the delegate layout transform to accommodate any translation/etc
    Point2D viewPoint = delegate.transform(graphPoint);

    // calculate point from center
    double dx = viewPoint.getX() - lensCenterInLayoutCoordinates.getX();
    double dy = viewPoint.getY() - lensCenterInLayoutCoordinates.getY();
    // pointFromCenter is in layout coordinates
    org.jungrapht.visualization.layout.model.Point pointFromCenter =
        org.jungrapht.visualization.layout.model.Point.of(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);
    double polarPointAngle = polar.theta;
    double polarPointRadius = polar.radius;
    if (!lensShape.contains(viewPoint)) {
      return viewPoint;
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
  public Point2D inverseTransform(Point2D viewPoint) {

    Point2D lensCenterInLayoutCoords = lens.getCenter();
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

    double mag = lens.getMagnification();
    radius /= mag;
    polar = polar.newRadius(radius);
    org.jungrapht.visualization.layout.model.Point projectedPoint =
        PolarPoint.polarToCartesian(polar);
    projectedPoint =
        org.jungrapht.visualization.layout.model.Point.of(
            projectedPoint.x / ratio, projectedPoint.y);
    Point2D translatedBack =
        new Point2D.Double(
            projectedPoint.x + lensCenterInLayoutCoords.getX(),
            projectedPoint.y + lensCenterInLayoutCoords.getY());
    return delegate.inverseTransform(translatedBack);
  }

  /**
   * Magnifies the point, without considering the Lens.
   *
   * @param graphPoint the point to transform via magnification
   * @return the transformed point
   */
  public Point2D magnify(Point2D graphPoint) {
    if (graphPoint == null) {
      return null;
    }
    Point2D viewCenter = lens.getCenter();
    double ratio = lens.getRatio();
    // calculate point from center
    double dx = graphPoint.getX() - viewCenter.getX();
    double dy = graphPoint.getY() - viewCenter.getY();
    // factor out ellipse
    dx *= ratio;
    org.jungrapht.visualization.layout.model.Point pointFromCenter =
        org.jungrapht.visualization.layout.model.Point.of(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);
    double theta = polar.theta;
    double radius = polar.radius;

    double mag = lens.getMagnification();
    radius *= mag;

    //        radius = Math.min(radius, viewRadius);
    org.jungrapht.visualization.layout.model.Point projectedPoint =
        PolarPoint.polarToCartesian(theta, radius);
    projectedPoint = Point.of(projectedPoint.x / ratio, projectedPoint.y);
    Point2D translatedBack =
        new Point2D.Double(
            projectedPoint.x + viewCenter.getX(), projectedPoint.y + viewCenter.getY());
    return translatedBack;
  }
}
