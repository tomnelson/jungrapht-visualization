/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.VisualizationServer;

public interface ScalingControl {

  /**
   * zoom the display in or out
   *
   * @param vv the VisualizationViewer
   * @param amount how much to adjust scale by
   * @param at where to adjust scale from
   */
  void scale(VisualizationServer<?, ?> vv, double amount, Point2D at);

  void scale(
      VisualizationServer<?, ?> vv, double horizontalAmount, double verticalAmount, Point2D at);
}
