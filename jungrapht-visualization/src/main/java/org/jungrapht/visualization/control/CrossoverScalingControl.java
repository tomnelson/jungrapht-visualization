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

  /** Point where scale crosses over from view to layout. */
  protected double crossover = 1.0;

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

  public void scale(
      VisualizationServer<?, ?> vv, double horizontalAmount, double verticalAmount, Point2D at) {
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
    //Math.min(viewTransformer.getScaleX(), viewTransformer.getScaleY());
    double viewScaleY =
        //            viewScaleX;
        viewTransformer.getScaleY();
    double inverseModelScaleX = Math.sqrt(crossover) / modelScaleX;
    double inverseModelScaleY = Math.sqrt(crossover) / modelScaleY;
    double inverseViewScaleX = Math.sqrt(crossover) / viewScaleX;
    double inverseViewScaleY = Math.sqrt(crossover) / viewScaleY;
    double scaleX = modelScaleX * viewScaleX;
    double scaleY = modelScaleY * viewScaleY;

    Point2D transformedAt =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .inverseTransform(MultiLayerTransformer.Layer.VIEW, at);

    if ((scaleX * horizontalAmount - crossover) * (scaleX * horizontalAmount - crossover) < 0.001
        || (scaleY * verticalAmount - crossover) * (scaleY * verticalAmount - crossover) < 0.001) {
      // close to the control point, return both Functions to a scale of sqrt crossover value
      layoutTransformer.scale(inverseModelScaleX, inverseModelScaleY, transformedAt);
      viewTransformer.scale(inverseViewScaleX, inverseViewScaleY, at);
    } else if (scaleX * horizontalAmount < crossover || scaleY * verticalAmount < crossover) {
      // scale the viewTransformer, return the layoutTransformer to sqrt crossover value
      double amount = Math.min(horizontalAmount, verticalAmount);
      viewTransformer.scale(amount, amount, at);
      layoutTransformer.scale(inverseModelScaleX, inverseModelScaleY, transformedAt);
    } else {
      // scale the layoutTransformer, return the viewTransformer to crossover value
      layoutTransformer.scale(horizontalAmount, verticalAmount, transformedAt);
      viewTransformer.scale(inverseViewScaleX, inverseViewScaleY, at);
    }
    vv.repaint();
  }

  public void scale(VisualizationServer<?, ?> vv, double amount, Point2D at) {

    MutableTransformer layoutTransformer =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.LAYOUT);
    MutableTransformer viewTransformer =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.VIEW);
    double modelScale = layoutTransformer.getScale();
    double viewScale = viewTransformer.getScale();
    double inverseModelScale = Math.sqrt(crossover) / modelScale;
    double inverseViewScale = Math.sqrt(crossover) / viewScale;
    double scale = modelScale * viewScale;

    Point2D transformedAt =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .inverseTransform(MultiLayerTransformer.Layer.VIEW, at);

    if ((scale * amount - crossover) * (scale * amount - crossover) < 0.001) {
      // close to the control point, return both Functions to a scale of sqrt crossover value
      layoutTransformer.scale(inverseModelScale, inverseModelScale, transformedAt);
      viewTransformer.scale(inverseViewScale, inverseViewScale, at);
    } else if (scale * amount < crossover) {
      // scale the viewTransformer, return the layoutTransformer to sqrt crossover value
      viewTransformer.scale(amount, amount, at);
      layoutTransformer.scale(inverseModelScale, inverseModelScale, transformedAt);
    } else {
      // scale the layoutTransformer, return the viewTransformer to crossover value
      layoutTransformer.scale(amount, amount, transformedAt);
      viewTransformer.scale(inverseViewScale, inverseViewScale, at);
    }
    vv.repaint();
  }
}
