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
 * LayoutScalingControl applies a scaling transformation to the graph layout. The Vertices get
 * closer or farther apart, but do not themselves change layoutSize. ScalingGraphMouse uses
 * MouseWheelEvents to apply the scaling.
 *
 * @author Tom Nelson
 */
public class LayoutScalingControl implements ScalingControl {

  /** zoom the display in or out, depending on the direction of the mouse wheel motion. */
  public void scale(VisualizationServer<?, ?> vv, double amount, Point2D from) {

    Point2D ivtfrom =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .inverseTransform(MultiLayerTransformer.Layer.VIEW, from);
    MutableTransformer modelTransformer =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.LAYOUT);
    modelTransformer.scale(amount, amount, ivtfrom);
    vv.repaint();
  }

  @Override
  public void scale(
      VisualizationServer<?, ?> vv, double horizontalAmount, double verticalAmount, Point2D from) {
    Point2D ivtfrom =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .inverseTransform(MultiLayerTransformer.Layer.VIEW, from);
    MutableTransformer modelTransformer =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.LAYOUT);
    modelTransformer.scale(horizontalAmount, verticalAmount, ivtfrom);
    vv.repaint();
  }
}
