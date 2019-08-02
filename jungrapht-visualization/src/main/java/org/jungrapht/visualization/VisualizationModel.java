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
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.event.LayoutChange;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

/** */
public interface VisualizationModel<V, E>
    extends LayoutChange.Listener, // can tell the view to repaint
        LayoutChange.Producer {

  class Builder<V, E, T extends VisualizationModel, B extends Builder<V, E, T, B>> {
    protected Graph<V, E> graph;
    protected LayoutAlgorithm<V> layoutAlgorithm;
    protected LayoutModel<V> layoutModel;
    protected Dimension layoutSize;
    protected Function<V, Point> initializer;

    protected B self() {
      return (B) this;
    }

    protected Builder(Graph<V, E> graph) {
      this.graph = graph;
    }

    protected Builder(LayoutModel<V> layoutModel) {
      this.layoutModel = layoutModel;
    }

    public B layoutAlgorithm(LayoutAlgorithm<V> layoutAlgorithm) {
      this.layoutAlgorithm = layoutAlgorithm;
      return self();
    }

    public B layoutModel(LayoutModel<V> layoutModel) {
      this.layoutModel = layoutModel;
      return self();
    }

    public B layoutSize(Dimension layoutSize) {
      this.layoutSize = layoutSize;
      return self();
    }

    public B initializer(Function<V, Point> initializer) {
      this.initializer = initializer;
      return self();
    }

    public T build() {
      return (T) new DefaultVisualizationModel<>(this);
    }
  }

  static <V, E> Builder<V, E, ?, ?> builder(Graph<V, E> graph) {
    return new Builder(graph);
  }

  static <V, E> Builder<V, E, ?, ?> builder(LayoutModel<V> layoutModel) {
    return new Builder(layoutModel);
  }

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

  void setGraph(Graph<V, E> graph, boolean forceUpdate);
}
