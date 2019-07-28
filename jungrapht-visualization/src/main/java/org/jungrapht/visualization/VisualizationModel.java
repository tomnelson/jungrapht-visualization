/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization;

import java.awt.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.event.LayoutChange;
import org.jungrapht.visualization.layout.model.LayoutModel;

/** */
public interface VisualizationModel<V, E>
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

  void setLayoutAlgorithm(LayoutAlgorithm<V> layoutAlgorithm);

  LayoutAlgorithm<V> getLayoutAlgorithm();

  LayoutModel<V> getLayoutModel();

  void setLayoutModel(LayoutModel<V> layoutModel);

  Graph<V, E> getGraph();

  void setGraph(Graph<V, E> graph);

  void setGraph(Graph<V, E> network, boolean forceUpdate);
}
