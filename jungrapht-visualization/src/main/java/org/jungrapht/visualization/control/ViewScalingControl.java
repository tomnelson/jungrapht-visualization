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
 * ViewScalingGraphMouse applies a scaling transform to the View of the graph. This causes all
 * elements of the graph to grow larger or smaller. ViewScalingGraphMouse, by default, is activated
 * by the MouseWheel when the control key is pressed. The control key modifier can be overridden in
 * the contstructor.
 *
 * @author Tom Nelson
 */
public class ViewScalingControl implements ScalingControl {

  /** zoom the display in or out, depending on the direction of the mouse wheel motion. */
  public void scale(VisualizationServer<?, ?> vv, float amount, Point2D from) {
    MutableTransformer viewTransformer =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.VIEW);
    viewTransformer.scale(amount, amount, from);
    vv.repaint();
  }
}
