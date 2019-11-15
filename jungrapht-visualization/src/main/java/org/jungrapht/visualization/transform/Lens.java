/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.visualization.transform;

import com.google.common.base.Preconditions;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LensTransformer wraps a MutableAffineTransformer and modifies the transform and inverseTransform
 * methods so that they create a projection of the graph points within an elliptical lens.
 *
 * <p>LensTransformer uses an affine transform to cause translation, scaling, rotation, and shearing
 * while applying a possibly non-affine filter in its transform and inverseTransform methods.
 *
 * @author Tom Nelson
 */
public class Lens {

  public enum Shape {
    ELLIPSE,
    RECTANGLE
  }

  private static final Logger log = LoggerFactory.getLogger(Lens.class);

  public static class Builder {
    private RectangularShape lensShape = new Ellipse2D.Double();;
    private Dimension dimension = new Dimension(100, 100);
    private float magnification = 0.7f;

    public Builder lensShape(Lens.Shape shape) {
      switch (shape) {
        case RECTANGLE:
          this.lensShape = new Rectangle2D.Double();
          break;
        case ELLIPSE:
        default:
          this.lensShape = new Ellipse2D.Double();
      }
      return this;
    }

    public Builder dimension(Dimension dimension) {
      this.dimension = dimension;
      return this;
    }

    public Builder magnification(float magnification) {
      this.magnification = magnification;
      return this;
    }

    public Lens build() {
      return new Lens(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private Lens(Builder builder) {
    this.lensShape = builder.lensShape;
    this.magnification = builder.magnification;
    setSize(builder.dimension);
  }
  /** the area affected by the transform */
  protected RectangularShape lensShape;

  protected float magnification;

  /** @param d the size used for the lens */
  public void setSize(Dimension d) {
    Preconditions.checkNotNull(d);
    Preconditions.checkArgument(d.width > 0, "width must be > 0");
    Preconditions.checkArgument(d.height > 0, "height must be > 0");
    float width = d.width / 1.5f;
    float height = d.height / 1.5f;
    lensShape.setFrame((d.width - width) / 2, (d.height - height) / 2, width, height);
  }

  public float getMagnification() {
    return magnification;
  }

  public void setMagnification(float magnification) {
    log.trace("setmagnification to {}", magnification);
    this.magnification = magnification;
  }

  public Point2D getCenter() {

    return new Point2D.Double(lensShape.getCenterX(), lensShape.getCenterY());
  }

  public void setCenter(Point2D viewCenter) {
    double width = lensShape.getWidth();
    double height = lensShape.getHeight();
    lensShape.setFrame(
        viewCenter.getX() - width / 2, viewCenter.getY() - height / 2, width, height);
    log.trace("setCenter of lens {} to {}", lensShape, viewCenter);
  }

  public double getRadius() {
    return lensShape.getHeight() / 2;
  }

  public double getCenterToCorner() {
    double w = lensShape.getWidth();
    double h = lensShape.getHeight();
    double diag = Math.sqrt(w * w + h * h);
    return diag / 2;
  }

  public void setRadius(double viewRadius) {
    double x = lensShape.getCenterX();
    double y = lensShape.getCenterY();
    double viewRatio = getRatio();
    lensShape.setFrame(
        x - viewRadius / viewRatio, y - viewRadius, 2 * viewRadius / viewRatio, 2 * viewRadius);
    log.trace("setRadius of lens {} to {}", this, viewRadius);
  }

  /** @return the ratio between the lens height and lens width */
  public double getRatio() {
    return lensShape.getHeight() / lensShape.getWidth();
  }

  public void setLensShape(RectangularShape ellipse) {
    log.trace("setLensShape to {}", ellipse);
    this.lensShape = ellipse;
  }

  public RectangularShape getLensShape() {
    return lensShape;
  }

  public double getDistanceFromCenter(Point2D p) {
    double dx = lensShape.getCenterX() - p.getX();
    double dy = lensShape.getCenterY() - p.getY();
    dx *= getRatio();
    return Math.sqrt(dx * dx + dy * dy);
  }
}
