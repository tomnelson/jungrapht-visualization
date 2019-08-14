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

import org.jgrapht.Graph;

/**
 * An interface for a service to access the index of a given edge (in a given {@code Graph}) into
 * the set formed by the given edge and all the other edges it is parallel to.
 *
 * <p>This index is assumed to be an integer value in the interval [0,n-1], where n-1 is the number
 * of edges parallel to <code>e</code>.
 *
 * @author Tom Nelson
 */
public interface EdgeIndexFunction<V, E> {

  /**
   * The index of <code>e</code> is defined as its position in some consistent ordering of <code>e
   * </code> and all edges parallel to <code>e</code>.
   *
   * @param context the graph and the edge whose index is to be queried
   * @return {@code edge}'s index in this instance's <code>Graph</code>.
   */
  int getIndex(Context<Graph<V, E>, E> context);

  /**
   * Resets the indices for <code>edge</code> and its parallel edges. Should be invoked when an edge
   * parallel to <code>edge</code> has been added or removed.
   *
   * @param context the graph and the edge whose index is to be reset
   */
  void reset(Context<Graph<V, E>, E> context);

  /** Clears all edge indices for all edges. Does not recalculate the indices. */
  void reset();
}