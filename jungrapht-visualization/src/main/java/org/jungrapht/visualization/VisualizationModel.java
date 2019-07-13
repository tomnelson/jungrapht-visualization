/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization;

import java.awt.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.event.LayoutChange;
import org.jungrapht.visualization.layout.model.LayoutModel;

/** */
public interface VisualizationModel<N, E>
    extends LayoutChange.Listener, // can tell the view to repaint
        LayoutChange.Producer {

  enum SpatialSupport {
    RTREE,
    QUADTREE,
    GRID,
    NONE
  }
  /** @return the current layoutSize of the visualization's space */
  Dimension getLayoutSize();

  void setLayoutAlgorithm(LayoutAlgorithm<N> layoutAlgorithm);

  LayoutAlgorithm<N> getLayoutAlgorithm();

  LayoutModel<N> getLayoutModel();

  void setLayoutModel(LayoutModel<N> layoutModel);

  Graph<N, E> getNetwork();

  void setNetwork(Graph<N, E> network);

  void setNetwork(Graph<N, E> network, boolean forceUpdate);
}
