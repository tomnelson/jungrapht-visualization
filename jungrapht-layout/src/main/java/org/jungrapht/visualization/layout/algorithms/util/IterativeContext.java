/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.layout.algorithms.util;

/**
 * An interface for algorithms that proceed iteratively. Each call to <code>step()</code> will
 * advance the algorithm by one cycle thru the affected members.
 */
public interface IterativeContext {

  /** Advances one step. */
  void step();

  /** @return {@code true} if this iterative process is finished, and {@code false} otherwise. */
  boolean done();
}
