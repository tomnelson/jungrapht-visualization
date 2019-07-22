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

import java.util.function.Predicate;
import org.jgrapht.Graph;

/**
 * A class which creates and maintains indices for parallel edges. Edges are evaluated by a
 * Predicate function and those that evaluate to true are excluded from computing a parallel offset.
 *
 * @author Tom Nelson
 */
public class PredicatedParallelEdgeIndexFunction<N, E> extends ParallelEdgeIndexFunction<N, E> {
  protected Predicate<E> predicate;

  public PredicatedParallelEdgeIndexFunction(Predicate<E> predicate) {
    this.predicate = predicate;
  }

  /**
   * Returns the index for the specified edge, or 0 if {@code edge} is accepted by the Predicate.
   *
   * @param context the network and the edge whose index is to be calculated
   */
  @Override
  public int getIndex(Context<Graph<N, E>, E> context) {
    return predicate.test(context.element) ? 0 : super.getIndex(context);
  }

  public Predicate<E> getPredicate() {
    return predicate;
  }

  public void setPredicate(Predicate<E> predicate) {
    this.predicate = predicate;
  }
}
