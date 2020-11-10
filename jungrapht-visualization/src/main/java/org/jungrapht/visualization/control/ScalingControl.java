/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.visualization.control;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.VisualizationServer;

public interface ScalingControl {

  String MIN_SCALE = PREFIX + "minScale";

  String MAX_SCALE = PREFIX + "maxScale";

  String CROSSOVER = PREFIX + "crossover";

  String ENABLE_SINGLE_AXIS_SCALING = PREFIX + "enableSingleAxisScaling";

  enum Axis {
    XY,
    X,
    Y
  }
  /**
   * zoom the display in or out
   *
   * @param vv the VisualizationViewer
   * @param amount how much to adjust scale by
   * @param at where to adjust scale from
   */
  @Deprecated
  default void scale(VisualizationServer<?, ?> vv, double amount, Point2D at) {
    scale(vv, amount, amount, at);
  }

  /**
   * zoom the display in or out
   *
   * @param vv the VisualizationViewer
   * @param horizontalAmount how much to adjust horizontal scale by
   * @param verticalAmount how much to adjust vertical scale by
   * @param at where to adjust scale from
   */
  void scale(
      VisualizationServer<?, ?> vv, double horizontalAmount, double verticalAmount, Point2D at);
}
