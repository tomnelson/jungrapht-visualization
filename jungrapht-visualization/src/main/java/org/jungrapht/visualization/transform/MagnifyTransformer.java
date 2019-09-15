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
import java.awt.geom.Point2D;
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
        lens = new Lens(dimension);
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
  public Point2D transform(Point2D graphPoint) {
    if (graphPoint == null) {
      return null;
    }
    if (log.isTraceEnabled()) {
      Ellipse2D lensEllipse = (Ellipse2D) lens.getLensShape();
      if (lensEllipse.contains(graphPoint)) {
        log.trace("lens {} contains graphPoint{}", lensEllipse, graphPoint);
      } else {
        log.trace("lens {} does not contain graphPoint {}", lensEllipse, graphPoint);
      }
    }
    Point2D viewCenter = lens.getCenter();
    double viewRadius = lens.getRadius();
    double ratio = lens.getRatio();
    // transform the point from the graph to the view
    Point2D viewPoint = delegate.transform(graphPoint);
    if (log.isTraceEnabled()) {
      Ellipse2D lensEllipse = (Ellipse2D) lens.getLensShape();
      if (lensEllipse.contains(viewPoint)) {
        log.trace("lens {} contains viewPoint {}", lensEllipse, viewPoint);
      } else {
        log.trace("lens {} does not contain viewPoint {}", lensEllipse, viewPoint);
      }
    }

    // calculate point from center
    double dx = viewPoint.getX() - viewCenter.getX();
    double dy = viewPoint.getY() - viewCenter.getY();
    // factor out ellipse
    dx *= ratio;
    org.jungrapht.visualization.layout.model.Point pointFromCenter =
        org.jungrapht.visualization.layout.model.Point.of(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);
    double theta = polar.theta;
    double radius = polar.radius;
    if (radius > viewRadius) {
      return viewPoint;
    }

    double mag = lens.getMagnification();
    radius *= mag;

    radius = Math.min(radius, viewRadius);
    org.jungrapht.visualization.layout.model.Point projectedPoint =
        PolarPoint.polarToCartesian(theta, radius);
    projectedPoint =
        org.jungrapht.visualization.layout.model.Point.of(
            projectedPoint.x / ratio, projectedPoint.y);
    Point2D translatedBack =
        new Point2D.Double(
            projectedPoint.x + viewCenter.getX(), projectedPoint.y + viewCenter.getY());
    return translatedBack;
  }

  /** override base class to un-project the fisheye effect */
  public Point2D inverseTransform(Point2D viewPoint) {

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
            projectedPoint.x + viewCenter.getX(), projectedPoint.y + viewCenter.getY());
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
