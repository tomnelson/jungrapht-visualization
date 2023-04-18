/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.visualization.transform;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides a magnification area (elliptical or rectangular) in a visualization */
public class Lens {

  /** supported lens shapes are Ellipse and Rectangle */
  public enum Shape {
    ELLIPSE,
    RECTANGLE
  }

  private static final Logger log = LoggerFactory.getLogger(Lens.class);

  /** builds a {@Code Lens} with a provided shape and initial magnification */
  public static class Builder {
    private RectangularShape lensShape = new Ellipse2D.Double(0, 0, 1, 1);
    private double magnification = 0.7f;

    private Builder(Lens.Shape lensShape) {
      setShapeFrom(lensShape);
    }

    private void setShapeFrom(Lens.Shape lensShape) {
      switch (lensShape) {
        case RECTANGLE:
          this.lensShape = new Rectangle2D.Double(0, 0, 1, 1);
          break;
        case ELLIPSE:
        default:
          this.lensShape = new Ellipse2D.Double(0, 0, 1, 1);
      }
    }

    public Builder lensShape(Lens.Shape shape) {
      setShapeFrom(shape);
      return this;
    }

    public Builder magnification(double magnification) {
      this.magnification = magnification;
      return this;
    }

    public Lens build() {
      return new Lens(this);
    }
  }

  public static Builder builder(Lens.Shape lensShape) {
    return new Builder(lensShape);
  }

  public static Builder builder() {
    return new Builder(Shape.ELLIPSE);
  }

  public Lens() {
    this(Lens.builder());
  }

  private Lens(Builder builder) {
    this.lensShape = builder.lensShape;
    this.magnification = builder.magnification;
  }
  /** the area affected by the transform */
  protected RectangularShape lensShape;

  protected double magnification;

  public double getMagnification() {
    return magnification;
  }

  public void setMagnification(double magnification) {
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
    if (lensShape.getWidth() == 0) {
      return 1;
    }
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
