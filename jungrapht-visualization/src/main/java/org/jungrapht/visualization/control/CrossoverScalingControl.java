/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 * Created on Mar 8, 2005
 *
 */
package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A scaling control that has a crossover point. When the overall scale of the view and model is
 * less than the crossover point, the scaling is applied to the view's transform and the graph
 * vertices, labels, etc grow smaller. This preserves the overall shape of the graph. When the scale
 * is larger than the crossover, the scaling is applied to the graph layout. The graph spreads out,
 * but the vertices and labels grow no larger than their original layoutSize.
 *
 * @author Tom Nelson
 */
public class CrossoverScalingControl implements ScalingControl {

  private static final Logger log = LoggerFactory.getLogger(CrossoverScalingControl.class);
  private static final double RESET_THRESHOLD = 0.002;

  public static class Builder {
    double minScale = Double.parseDouble(System.getProperty(MIN_SCALE, "0.0001"));
    double maxScale = Double.parseDouble(System.getProperty(MAX_SCALE, "20000.0"));
    double crossover = Double.parseDouble(System.getProperty(CROSSOVER, "1.0"));
    boolean enableSingleAxisScaling =
        Boolean.parseBoolean(System.getProperty(ENABLE_SINGLE_AXIS_SCALING, "true"));

    public Builder minScale(double minScale) {
      this.minScale = minScale;
      return this;
    }

    public Builder maxScale(double maxScale) {
      this.maxScale = maxScale;
      return this;
    }

    public Builder crossover(double crossover) {
      this.crossover = crossover;
      return this;
    }

    public Builder enableSingleAxisScaling(boolean enableSingleAxisScaling) {
      this.enableSingleAxisScaling = enableSingleAxisScaling;
      return this;
    }

    public ScalingControl build() {
      return new CrossoverScalingControl(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  CrossoverScalingControl(Builder builder) {
    this.minScale = builder.minScale;
    this.maxScale = builder.maxScale;
    this.singleAxisMin = minScale / 200;
    this.singleAxisMax = maxScale * 200;
    this.crossover = builder.crossover;
    this.enableSingleAxisScaling = builder.enableSingleAxisScaling;
  }

  public CrossoverScalingControl() {
    this(CrossoverScalingControl.builder());
  }

  protected double minScale;

  protected double maxScale;

  double singleAxisMin;

  double singleAxisMax;

  /** Point where scale crosses over from view to layout. */
  protected double crossover;

  protected boolean enableSingleAxisScaling;

  /**
   * Sets the crossover point to the specified value.
   *
   * @param crossover the crossover point to use (defaults to 1.0)
   */
  public void setCrossover(double crossover) {
    this.crossover = crossover;
  }

  /** @return the current crossover value */
  public double getCrossover() {
    return crossover;
  }

  public void setEnableSingleAxisScaling(boolean enableSingleAxisScaling) {
    this.enableSingleAxisScaling = enableSingleAxisScaling;
  }

  @Override
  public void scale(
      VisualizationServer<?, ?> vv, double horizontalAmount, double verticalAmount, Point2D at) {
    Axis axis =
        !enableSingleAxisScaling || horizontalAmount == verticalAmount
            ? Axis.XY
            : verticalAmount == 1.0 ? Axis.X : horizontalAmount == 1.0 ? Axis.Y : Axis.XY;

    MutableTransformer layoutTransformer =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.LAYOUT);
    MutableTransformer viewTransformer =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.VIEW);
    double modelScaleX = layoutTransformer.getScaleX();
    double modelScaleY = layoutTransformer.getScaleY();
    double viewScaleX = viewTransformer.getScaleX();
    double viewScaleY = viewTransformer.getScaleY();
    double inverseModelScaleX = Math.sqrt(crossover) / modelScaleX;
    double inverseModelScaleY = Math.sqrt(crossover) / modelScaleY;
    double inverseViewScaleX = Math.sqrt(crossover) / viewScaleX;
    double inverseViewScaleY = Math.sqrt(crossover) / viewScaleY;
    double scaleX = modelScaleX * viewScaleX;
    double scaleY = modelScaleY * viewScaleY;

    if (!withinScaleBounds(
        scaleX,
        scaleY,
        axis == Axis.XY ? minScale : singleAxisMin,
        axis == Axis.XY ? maxScale : singleAxisMax,
        horizontalAmount,
        verticalAmount)) {
      log.trace("out of scale bounds: {} {}", horizontalAmount, verticalAmount);
      return;
    }

    Point2D transformedAt =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .inverseTransform(MultiLayerTransformer.Layer.VIEW, at);

    double newX = scaleX * horizontalAmount;
    double newY = scaleY * verticalAmount;
    double minX = newX - crossover;
    double minY = newY - crossover;

    log.trace("scaleX: {}, scaleY: {}", scaleX, scaleY);
    boolean reset = false;

    if (scaleX == scaleY && minX * minY < RESET_THRESHOLD) {
      log.trace("will reset");
      reset = true;
    }

    switch (axis) {
      case X:
        adjustTransformers(
            horizontalAmount,
            verticalAmount,
            at,
            layoutTransformer,
            viewTransformer,
            inverseModelScaleX,
            inverseModelScaleY,
            inverseViewScaleX,
            inverseViewScaleY,
            transformedAt,
            reset,
            newX < crossover);
        break;
      case Y:
        adjustTransformers(
            horizontalAmount,
            verticalAmount,
            at,
            layoutTransformer,
            viewTransformer,
            inverseModelScaleX,
            inverseModelScaleY,
            inverseViewScaleX,
            inverseViewScaleY,
            transformedAt,
            reset,
            newY < crossover);
        break;
      case XY:
      default:
        adjustTransformers(
            horizontalAmount,
            verticalAmount,
            at,
            layoutTransformer,
            viewTransformer,
            inverseModelScaleX,
            inverseModelScaleY,
            inverseViewScaleX,
            inverseViewScaleY,
            transformedAt,
            reset,
            newX < crossover || newY < crossover);
    }
    vv.repaint();
  }

  protected void adjustTransformers(
      double horizontalAmount,
      double verticalAmount,
      Point2D at,
      MutableTransformer layoutTransformer,
      MutableTransformer viewTransformer,
      double inverseModelScaleX,
      double inverseModelScaleY,
      double inverseViewScaleX,
      double inverseViewScaleY,
      Point2D transformedAt,
      boolean reset,
      boolean adjustViewTransform) {
    if (reset) {
      log.trace(
          "close to control point horizontalAmount:{}, verticalAmount: {}",
          horizontalAmount,
          verticalAmount);
      // close to the control point, return both Functions to a scale of sqrt crossover value
      layoutTransformer.scale(inverseModelScaleX, inverseModelScaleY, transformedAt);
      viewTransformer.scale(inverseViewScaleX, inverseViewScaleY, at);
    } else if (adjustViewTransform) {
      log.trace("adjust view {}, {}", horizontalAmount, verticalAmount);
      viewTransformer.scale(horizontalAmount, verticalAmount, at);
    } else {
      log.trace("adjust layout {}, {}", horizontalAmount, verticalAmount);
      layoutTransformer.scale(horizontalAmount, verticalAmount, transformedAt);
    }
  }

  public void reset(VisualizationServer<?, ?> vv, Point2D at) {
    MutableTransformer layoutTransformer =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.LAYOUT);
    MutableTransformer viewTransformer =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.VIEW);
    double modelScaleX = layoutTransformer.getScaleX();
    double modelScaleY = layoutTransformer.getScaleY();
    double viewScaleX = viewTransformer.getScaleX();
    double viewScaleY = viewTransformer.getScaleY();
    double inverseModelScaleX = Math.sqrt(crossover) / modelScaleX;
    double inverseModelScaleY = Math.sqrt(crossover) / modelScaleY;
    double inverseViewScaleX = Math.sqrt(crossover) / viewScaleX;
    double inverseViewScaleY = Math.sqrt(crossover) / viewScaleY;
    Point2D transformedAt =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .inverseTransform(MultiLayerTransformer.Layer.VIEW, at);
    layoutTransformer.scale(inverseModelScaleX, inverseModelScaleY, transformedAt);
    viewTransformer.scale(inverseViewScaleX, inverseViewScaleY, at);
  }

  protected boolean withinScaleBounds(
      double scaleX,
      double scaleY,
      double minScale,
      double maxScale,
      double horizontalAmount,
      double verticalAmount) {
    if (scaleX > maxScale && horizontalAmount > 1.0) {
      return false;
    }
    if (scaleX < minScale && horizontalAmount < 1.0) {
      return false;
    }
    if (scaleY > maxScale && verticalAmount > 1.0) {
      return false;
    }
    if (scaleY < minScale && verticalAmount < 1.0) {
      return false;
    }
    return true;
  }
}
