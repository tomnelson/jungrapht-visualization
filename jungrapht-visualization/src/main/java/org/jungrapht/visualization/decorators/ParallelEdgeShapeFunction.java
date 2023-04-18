/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on March 10, 2005
 */
package org.jungrapht.visualization.decorators;

import java.awt.Shape;
import java.util.function.BiFunction;
import org.jgrapht.Graph;
import org.jungrapht.visualization.util.EdgeIndexFunction;

/**
 * An abstract class for edge-to-Shape functions that work with parallel edges.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public abstract class ParallelEdgeShapeFunction<V, E> implements BiFunction<Graph<V, E>, E, Shape> {
  /** Specifies the distance between control points for edges being drawn in parallel. */
  protected double controlOffsetIncrement = 20.f;

  protected EdgeIndexFunction<V, E> edgeIndexFunction = new EdgeIndexFunction<>() {};

  public void setControlOffsetIncrement(double y) {
    controlOffsetIncrement = y;
  }

  public void setEdgeIndexFunction(EdgeIndexFunction<V, E> edgeIndexFunction) {
    this.edgeIndexFunction = edgeIndexFunction;
  }

  public EdgeIndexFunction<V, E> getEdgeIndexFunction() {
    return edgeIndexFunction;
  }
}
