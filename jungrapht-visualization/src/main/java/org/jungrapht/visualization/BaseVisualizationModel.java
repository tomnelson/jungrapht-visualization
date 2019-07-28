/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Jul 7, 2003
 *
 */
package org.jungrapht.visualization;

import com.google.common.base.Preconditions;
import java.awt.Dimension;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.event.LayoutChange;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.LoadingCacheLayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.RandomLocationTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class BaseVisualizationModel<V, E> implements VisualizationModel<V, E> {

  private static final Logger log = LoggerFactory.getLogger(BaseVisualizationModel.class);

  //  public class Builder<V, E, T extends VisualizationModel, B extends Builder<V, E, T, B>> {
  //    private Graph<V,E> graph;
  //    private LayoutAlgorithm<V> layoutAlgorithm;
  //    private LayoutModel<V> layoutModel;
  //    private Dimension layoutSize;
  //    private Function<V, Point> initializer;
  //
  //
  //    public B builder() {
  //      return new Builder<V,E,T,B>();
  //
  //    }
  //    public T build() {
  //      return (T) new BaseVisualizationModel<>(this);
  //    }
  //
  //  }

  protected LayoutModel<V> layoutModel;

  protected LayoutAlgorithm<V> layoutAlgorithm;

  protected LayoutChange.Support changeSupport = LayoutChange.Support.create();

  public BaseVisualizationModel(VisualizationModel<V, E> other) {
    this(other.getGraph(), other.getLayoutAlgorithm(), null, other.getLayoutSize());
  }

  //  private BaseVisualizationModel(Builder<V, E, ?, ?> builder) {
  //    return new BaseVisualizationModel<>(builder.graph, builder.layoutAlgorithm, builder.layoutSize, builder)
  //
  //  }
  public BaseVisualizationModel(VisualizationModel<V, E> other, Dimension layoutSize) {
    this(other.getGraph(), other.getLayoutAlgorithm(), null, layoutSize);
  }

  /**
   * @param graph the graph to visualize
   * @param layoutAlgorithm the algorithm to apply
   * @param layoutSize the size of the layout area
   */
  public BaseVisualizationModel(
      Graph<V, E> graph, LayoutAlgorithm<V> layoutAlgorithm, Dimension layoutSize) {
    this(graph, layoutAlgorithm, null, layoutSize);
  }

  /**
   * Creates an instance for {@code graph} which initializes the vertex locations using {@code
   * initializer} and sets the layoutSize of the layout to {@code layoutSize}.
   *
   * @param graph the graph on which the layout algorithm is to operate
   * @param initializer specifies the starting positions of the vertices
   * @param layoutSize the dimensions of the region in which the layout algorithm will place
   *     vertices
   */
  public BaseVisualizationModel(
      Graph<V, E> graph,
      LayoutAlgorithm<V> layoutAlgorithm,
      Function<V, Point> initializer,
      Dimension layoutSize) {
    Preconditions.checkNotNull(graph);
    Preconditions.checkNotNull(layoutSize);
    Preconditions.checkArgument(layoutSize.width > 0, "width must be > 0");
    Preconditions.checkArgument(layoutSize.height > 0, "height must be > 0");
    this.layoutAlgorithm = layoutAlgorithm;
    this.layoutModel =
        LoadingCacheLayoutModel.<V>builder()
            .graph(graph)
            .size(layoutSize.width, layoutSize.height)
            .initializer(
                new RandomLocationTransformer<>(
                    layoutSize.width, layoutSize.height, System.currentTimeMillis()))
            .build();

    if (initializer != null) {
      this.layoutModel.setInitializer(initializer);
    }

    this.layoutModel.accept(layoutAlgorithm);
  }

  public BaseVisualizationModel(
      Graph<V, E> graph, LayoutModel<V> layoutModel, LayoutAlgorithm<V> layoutAlgorithm) {
    Preconditions.checkNotNull(graph);
    Preconditions.checkNotNull(layoutModel);
    this.layoutModel = layoutModel;
    if (this.layoutModel instanceof LayoutChange.Support) {
      ((LayoutChange.Support) layoutModel).addLayoutChangeListener(this);
    }
    this.layoutModel.accept(layoutAlgorithm);
    this.layoutAlgorithm = layoutAlgorithm;
  }

  private BaseVisualizationModel(
      Graph<V, E> graph,
      LayoutModel<V> layoutModel,
      Dimension layoutSize,
      LayoutAlgorithm<V> layoutAlgorithm,
      Function<V, Point> initializer) {}

  public LayoutModel<V> getLayoutModel() {
    log.trace("getting a layourModel " + layoutModel);
    return layoutModel;
  }

  public void setLayoutModel(LayoutModel<V> layoutModel) {
    // stop any Relaxer threads before abandoning the previous LayoutModel
    if (this.layoutModel != null) {
      this.layoutModel.stopRelaxer();
    }
    this.layoutModel = layoutModel;
    if (layoutAlgorithm != null) {
      layoutModel.accept(layoutAlgorithm);
    }
  }

  public void setLayoutAlgorithm(LayoutAlgorithm<V> layoutAlgorithm) {
    this.layoutAlgorithm = layoutAlgorithm;
    log.trace("setLayoutAlgorithm to " + layoutAlgorithm);
    layoutModel.accept(layoutAlgorithm);
  }

  /**
   * Returns the current layoutSize of the visualization space, accoring to the last call to
   * resize().
   *
   * @return the current layoutSize of the screen
   */
  public Dimension getLayoutSize() {
    return new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
  }

  public void setGraph(Graph<V, E> graph) {
    this.setGraph(graph, true);
  }

  public void setGraph(Graph<V, E> graph, boolean forceUpdate) {
    log.trace("setGraph to n:{} e:{}", graph.vertexSet(), graph.edgeSet());
    //    this.graph = graph;
    this.layoutModel.setGraph(graph);
    if (forceUpdate && this.layoutAlgorithm != null) {
      log.trace("will accept {}", layoutAlgorithm);
      layoutModel.accept(this.layoutAlgorithm);
      log.trace("will fire stateChanged");
      changeSupport.fireLayoutChanged();
      log.trace("fired stateChanged");
    }
  }

  public LayoutAlgorithm<V> getLayoutAlgorithm() {
    return layoutAlgorithm;
  }

  public Graph<V, E> getGraph() {
    return this.layoutModel.getGraph();
  }

  @Override
  public LayoutChange.Support getLayoutChangeSupport() {
    return this.changeSupport;
  }

  @Override
  public void layoutChanged() {
    getLayoutChangeSupport().fireLayoutChanged();
  }
}
