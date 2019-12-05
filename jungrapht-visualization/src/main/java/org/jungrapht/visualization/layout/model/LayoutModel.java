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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
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
        LayoutVertexPositionChange.Listener<V> {

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
    Map<V, Point> map = new HashMap<>();
    getGraph().vertexSet().forEach(v -> map.put(v, apply(v)));
    return Collections.unmodifiableMap(map);
  }

  /**
   * @param width to set
   * @param helght to set
   */
  void setSize(int width, int helght);

  /** stop a relaxer Thread from continuing to operate */
  void stopRelaxer();

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

  /**
   * a handle to the relaxer thread; may be used to attach a process to run after relax is complete
   *
   * @return the Future
   */
  Future getTheFuture();

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
}
