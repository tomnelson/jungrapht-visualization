/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
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
public class BaseVisualizationModel<N, E> implements VisualizationModel<N, E> {

  private static final Logger log = LoggerFactory.getLogger(BaseVisualizationModel.class);

  protected Graph<N, E> network;

  protected LayoutModel<N> layoutModel;

  protected LayoutAlgorithm<N> layoutAlgorithm;

  protected LayoutChange.Support changeSupport = LayoutChange.Support.create();

  public BaseVisualizationModel(VisualizationModel<N, E> other) {
    this(other.getNetwork(), other.getLayoutAlgorithm(), null, other.getLayoutSize());
  }

  public BaseVisualizationModel(VisualizationModel<N, E> other, Dimension layoutSize) {
    this(other.getNetwork(), other.getLayoutAlgorithm(), null, layoutSize);
  }

  /**
   * @param network the network to visualize
   * @param layoutAlgorithm the algorithm to apply
   * @param layoutSize the size of the layout area
   */
  public BaseVisualizationModel(
      Graph<N, E> network, LayoutAlgorithm<N> layoutAlgorithm, Dimension layoutSize) {
    this(network, layoutAlgorithm, null, layoutSize);
  }

  /**
   * Creates an instance for {@code graph} which initializes the node locations using {@code
   * initializer} and sets the layoutSize of the layout to {@code layoutSize}.
   *
   * @param network the graph on which the layout algorithm is to operate
   * @param initializer specifies the starting positions of the nodes
   * @param layoutSize the dimensions of the region in which the layout algorithm will place nodes
   */
  public BaseVisualizationModel(
      Graph<N, E> network,
      LayoutAlgorithm<N> layoutAlgorithm,
      Function<N, Point> initializer,
      Dimension layoutSize) {
    Preconditions.checkNotNull(network);
    Preconditions.checkNotNull(layoutSize);
    Preconditions.checkArgument(layoutSize.width > 0, "width must be > 0");
    Preconditions.checkArgument(layoutSize.height > 0, "height must be > 0");
    this.layoutAlgorithm = layoutAlgorithm;
    this.layoutModel =
        LoadingCacheLayoutModel.<N>builder()
            .graph(network)
            .size(layoutSize.width, layoutSize.height)
            .initializer(
                new RandomLocationTransformer<N>(
                    layoutSize.width, layoutSize.height, System.currentTimeMillis()))
            .build();

    this.network = network;
    if (initializer != null) {
      this.layoutModel.setInitializer(initializer);
    }

    this.layoutModel.accept(layoutAlgorithm);
  }

  public BaseVisualizationModel(
      Graph<N, E> network, LayoutModel<N> layoutModel, LayoutAlgorithm<N> layoutAlgorithm) {
    Preconditions.checkNotNull(network);
    Preconditions.checkNotNull(layoutModel);
    this.layoutModel = layoutModel;
    if (this.layoutModel instanceof LayoutChange.Support) {
      ((LayoutChange.Support) layoutModel).addLayoutChangeListener(this);
    }
    this.network = network;
    this.layoutModel.accept(layoutAlgorithm);
    this.layoutAlgorithm = layoutAlgorithm;
  }

  public LayoutModel<N> getLayoutModel() {
    log.trace("getting a layourModel " + layoutModel);
    return layoutModel;
  }

  public void setLayoutModel(LayoutModel<N> layoutModel) {
    // stop any Relaxer threads before abandoning the previous LayoutModel
    if (this.layoutModel != null) {
      this.layoutModel.stopRelaxer();
    }
    this.layoutModel = layoutModel;
    if (layoutAlgorithm != null) {
      layoutModel.accept(layoutAlgorithm);
    }
  }

  public void setLayoutAlgorithm(LayoutAlgorithm<N> layoutAlgorithm) {
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

  public void setNetwork(Graph<N, E> network) {
    this.setNetwork(network, true);
  }

  public void setNetwork(Graph<N, E> network, boolean forceUpdate) {
    log.trace("setNetwork to n:{} e:{}", network.vertexSet(), network.edgeSet());
    this.network = network;
    this.layoutModel.setGraph(network);
    if (forceUpdate && this.layoutAlgorithm != null) {
      log.trace("will accept {}", layoutAlgorithm);
      layoutModel.accept(this.layoutAlgorithm);
      log.trace("will fire stateChanged");
      changeSupport.fireLayoutChanged();
      log.trace("fired stateChanged");
    }
  }

  public LayoutAlgorithm<N> getLayoutAlgorithm() {
    return layoutAlgorithm;
  }

  public Graph<N, E> getNetwork() {
    return this.network;
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
