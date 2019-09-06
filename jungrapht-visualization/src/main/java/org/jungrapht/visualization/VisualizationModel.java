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
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.event.ModelChange;
import org.jungrapht.visualization.layout.event.ViewChange;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

/**
 * Interface for the visualization model to hold state information for a graph visualization
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public interface VisualizationModel<V, E>
    extends ViewChange.Listener, ViewChange.Producer, ModelChange.Listener, ModelChange.Producer {

  /**
   * A builder for creating instances of a {@code }VisualizationModel} with user defined properties
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  class Builder<V, E, T extends VisualizationModel, B extends Builder<V, E, T, B>> {
    /** a {@code Graph} to visualize */
    protected Graph<V, E> graph = GraphTypeBuilder.<V, E>directed().buildGraph();
    /** a {@code LayoutAlgorithm} to position the graph vertices */
    protected LayoutAlgorithm<V> layoutAlgorithm;
    /** a {@code LayoutModel} to hold the positions of the vertices */
    protected LayoutModel<V> layoutModel;
    /** the bounds of the layout area */
    protected Dimension layoutSize = new Dimension(600, 600);
    /** a {@code Function} to set initial vertex locations */
    protected Function<V, Point> initializer;

    /** @return this builder cast to type B */
    protected B self() {
      return (B) this;
    }

    /** create a builder with no arguments */
    protected Builder() {}
    /**
     * create a builder with the passed graph
     *
     * @param graph the graph to visualize
     */
    protected Builder(Graph<V, E> graph) {
      this.graph = graph;
    }

    /**
     * create a builder with the passed layoutModel
     *
     * @param layoutModel the layoutModel for the visualization
     */
    protected Builder(LayoutModel<V> layoutModel) {
      this.layoutModel = layoutModel;
    }

    /**
     * @param layoutAlgorithm algorithm to apply for vertex placement
     * @return this Builder
     */
    public B layoutAlgorithm(LayoutAlgorithm<V> layoutAlgorithm) {
      this.layoutAlgorithm = layoutAlgorithm;
      return self();
    }

    /**
     * @param layoutModel the layoutModel to hold visualization state
     * @return this Builder
     */
    public B layoutModel(LayoutModel<V> layoutModel) {
      this.layoutModel = layoutModel;
      return self();
    }

    /**
     * @param layoutSize the bounds (width and height) of the visualization model
     * @return this Builder
     */
    public B layoutSize(Dimension layoutSize) {
      this.layoutSize = layoutSize;
      return self();
    }

    /**
     * @param initializer a {@code Function} to set initial vertex locations
     * @return the Builder
     */
    public B initializer(Function<V, Point> initializer) {
      this.initializer = initializer;
      return self();
    }

    /** @return the new @{link VisualizationModel} instance */
    public T build() {
      return (T) new DefaultVisualizationModel<>(this);
    }
  }

  /**
   * @param <V> vertex type
   * @param <E> edge type
   * @return a Builder to create a VisualizationModel instance
   */
  static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder();
  }
  /**
   * @param graph the graph to visualize
   * @param <V> the vertex type
   * @param <E> the edge type
   * @return a Builder to create a VisualizationModel instance
   */
  static <V, E> Builder<V, E, ?, ?> builder(Graph<V, E> graph) {
    return new Builder(graph);
  }

  /**
   * @param layoutModel the layout model for the visualization model
   * @param <V> the vertex type
   * @param <E> the edge type
   * @return a Builder to create a VisualizationModel instance
   */
  static <V, E> Builder<V, E, ?, ?> builder(LayoutModel<V> layoutModel) {
    return new Builder(layoutModel);
  }

  /** the types of spatial data strucure to use with the visualization */
  enum SpatialSupport {
    RTREE,
    QUADTREE,
    GRID,
    NONE
  }
  /** @return the current layoutSize of the visualization's space */
  Dimension getLayoutSize();

  /** @param layoutAlgorithm the algorithm to apply to the vertex locations */
  void setLayoutAlgorithm(LayoutAlgorithm<V> layoutAlgorithm);

  /** @return the layout algorithm that was used to place the vertices */
  LayoutAlgorithm<V> getLayoutAlgorithm();

  /** @return the layout model that holds vertex location state */
  LayoutModel<V> getLayoutModel();

  /** @param layoutModel the layout model to hold vertex locations */
  void setLayoutModel(LayoutModel<V> layoutModel);

  /** @return the graph being visualized */
  Graph<V, E> getGraph();

  /** @param graph the graph to visualize */
  void setGraph(Graph<V, E> graph);

  /**
   * @param graph the graph to visualize
   * @param forceUpdate whether to force an update
   */
  void setGraph(Graph<V, E> graph, boolean forceUpdate);
}
