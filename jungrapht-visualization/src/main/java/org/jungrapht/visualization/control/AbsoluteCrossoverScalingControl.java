/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.transform.MutableTransformer;

/**
 * Scales to the absolute value passed as an argument. It first resets the scaling Functions, then
 * uses the relative CrossoverScalingControl to achieve the absolute value.
 *
 * @author Tom Nelson
 */
public class AbsoluteCrossoverScalingControl extends CrossoverScalingControl
    implements ScalingControl {

  /**
   * Scale to the absolute value passed as 'amount'.
   *
   * @param vv the VisualizationServer used for rendering; provides the layout and view
   *     transformers.
   * @param amount the amount by which to scale
   * @param at the point of reference for scaling
   */
  public void scale(VisualizationServer<?, ?> vv, float amount, Point2D at) {
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

    Point2D transformedAt =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .inverseTransform(MultiLayerTransformer.Layer.VIEW, at);

    // return the Functions to 1.0
    layoutTransformer.scale(inverseModelScale, inverseModelScale, transformedAt);
    viewTransformer.scale(inverseViewScale, inverseViewScale, at);

    super.scale(vv, amount, at);
  }
}
