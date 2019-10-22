/*
 * Created on Sep 24, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.util;

import java.util.HashMap;
import java.util.Map;
import org.jgrapht.Graph;

/**
 * A class which creates and maintains indices for parallel edges. Parallel edges are defined here
 * to be the collection of edges that are returned by <code>graph.edgesConnecting(v, w)</code> for
 * some <code>v</code> and <code>w</code>.
 *
 * <p>At this time, users are responsible for resetting the indices (by calling <code>reset()</code>
 * ) if changes to the graph make it appropriate.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 */
public class ParallelEdgeIndexFunction<V, E> implements EdgeIndexFunction<V, E> {

  protected Map<E, Integer> edgeIndex = new HashMap<>();

  @Override
  public Integer apply(Context<Graph<V, E>, E> context) {
    Graph<V, E> graph = context.graph;
    E edge = context.element;
    Integer index = edgeIndex.get(edge);
    if (index == null) {
      V u = graph.getEdgeSource(edge);
      V v = graph.getEdgeTarget(edge);
      int count = 0;
      for (E connectingEdge : graph.getAllEdges(u, v)) {
        edgeIndex.put(connectingEdge, count++);
      }
      return edgeIndex.get(edge);
    }
    return index;
  }

  public void reset(Context<Graph<V, E>, E> context) {
    edgeIndex.remove(context.element);
  }

  public void reset() {
    edgeIndex.clear();
  }
}
