/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.layout.model;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.jungrapht.visualization.layout.event.LayoutSizeChange;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.layout.event.LayoutVertexPositionChange;
import org.jungrapht.visualization.layout.event.ModelChange;
import org.jungrapht.visualization.layout.event.ViewChange;

/**
 * two dimensional layout model. Acts as a Mediator between the Graph vertices and their locations
 * in the Cartesian coordinate system.
 *
 * @author Tom Nelson
 */
public interface LayoutModel<V>
    extends Function<V, Point>,
        ModelChange.Producer,
        ViewChange.Producer,
        LayoutVertexPositionChange.Producer<V>,
        LayoutStateChange.Producer,
        LayoutVertexPositionChange.Listener<V>,
        LayoutSizeChange.Producer<V> {

  String PREFIX = "jungrapht.";

  /**
   * a builder for LayoutModel instances
   *
   * @param <V> the vertex type
   * @param <T> the type of the superclass of the LayoutModel to be built
   */
  class Builder<V, T extends DefaultLayoutModel<V>, B extends Builder<V, T, B>>
      extends AbstractLayoutModel.Builder<V, T, B> {

    Function<V, Point> initializer = v -> Point.ORIGIN;

    /**
     * set the LayoutModel to copy with this builder
     *
     * @param layoutModel
     * @return this builder for further use
     */
    public B layoutModel(LayoutModel<V> layoutModel) {
      this.width = layoutModel.getWidth();
      this.height = layoutModel.getHeight();
      return (B) this;
    }

    /**
     * sets the initializer to use for new vertices
     *
     * @param initializer
     * @return the builder
     */
    public B initializer(Function<V, Point> initializer) {
      this.initializer = initializer;
      return (B) this;
    }

    public B createVisRunnable(boolean createVisRunnable) {
      this.createVisRunnable = createVisRunnable;
      return (B) this;
    }

    /**
     * build an instance of the requested LayoutModel of type T
     *
     * @return
     */
    public T build() {
      return (T) new DefaultLayoutModel<>(this);
    }
  }

  static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  /** @return the width of the layout area */
  int getWidth();

  /** @return the height of the layout area */
  int getHeight();

  int getPreferredWidth();

  int getPreferredHeight();

  default Point getCenter() {
    return Point.of(getWidth() / 2, getHeight() / 2);
  }

  /**
   * allow the passed LayoutAlgorithm to operate on this LayoutModel
   *
   * @param layoutAlgorithm the algorithm to apply to this model's Points
   */
  void accept(LayoutAlgorithm<V> layoutAlgorithm);

  /** @return a mapping of Vertices to Point locations */
  default Map<V, Point> getLocations() {
    return Collections.unmodifiableMap(
        getGraph().vertexSet().stream().collect(Collectors.toMap(v -> v, this::apply)));
  }

  /**
   * The initialDimensionFunction sets the layout area bounds by evaluating the {@link Graph}
   *
   * @param initialDimensionFunction
   */
  void setInitialDimensionFunction(Function<Graph<V, ?>, Pair<Integer>> initialDimensionFunction);

  /**
   * @param width to set
   * @param helght to set
   */
  void setSize(int width, int helght);

  /**
   * @param width to set
   * @param helght to set
   */
  void setPreferredSize(int width, int helght);

  /** stop a relaxer Thread from continuing to operate */
  default void stop() {
    // noop
  }

  /**
   * indicates that there is a relaxer thread operating on this LayoutModel
   *
   * @param relaxing whether there is a relaxer thread
   */
  void setRelaxing(boolean relaxing);

  /**
   * indicates that there is a relaxer thread operating on this LayoutModel
   *
   * @return relaxing
   */
  boolean isRelaxing();

  //  /**
  //   * a handle to the relaxer thread; may be used to attach a process to run after relax is complete
  //   *
  //   * @return the Future
  //   */
  //  Future getTheFuture();

  /**
   * @param vertex the vertex whose locked state is being queried
   * @return <code>true</code> if the position of vertex <code>v</code> is locked
   */
  boolean isLocked(V vertex);

  /**
   * Changes the layout coordinates of {@code vertex} to {@code location}.
   *
   * @param vertex the vertex whose location is to be specified
   * @param location the coordinates of the specified location
   */
  void set(V vertex, Point location);

  /**
   * Changes the layout coordinates of {@code vertex} to {@code x, y}.
   *
   * @param vertex the vertex to set location for
   * @param x coordinate to set
   * @param y coordinate to set
   */
  void set(V vertex, double x, double y);

  /**
   * @param vertex the vertex of interest
   * @return the Point location for vertex
   */
  Point get(V vertex);

  /** @return the {@code Graph} that this model is mediating */
  <E> Graph<V, E> getGraph();

  /** @param graph the {@code Graph} to set */
  void setGraph(Graph<V, ?> graph);

  void lock(V vertex, boolean locked);

  void lock(boolean locked);

  boolean isLocked();

  void setInitializer(Function<V, Point> initializer);

  void resizeToSurroundingRectangle();

  void appendLayoutModel(LayoutModel<V> layoutModel);

  default void setFireEvents(boolean fireEvents) {
    if (getModelChangeSupport() != null) getModelChangeSupport().setFireEvents(fireEvents);
    if (getViewChangeSupport() != null) getViewChangeSupport().setFireEvents(fireEvents);
    if (getLayoutStateChangeSupport() != null)
      getLayoutStateChangeSupport().setFireEvents(fireEvents);
    if (getLayoutSizeChangeSupport() != null)
      getLayoutSizeChangeSupport().setFireEvents(fireEvents);
  }
}
