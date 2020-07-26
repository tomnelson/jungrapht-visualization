/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICEVSE for a description.
 *
 *
 */
package org.jungrapht.visualization.layout.util;

import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

/**
 * Interface for coordinate-based selection of graph edges.
 *
 * @author Tom Nelson
 */
public interface EdgeAccessor<V, E> {

  /**
   * @param layoutModel the source of Edge positions
   * @param p the pick point
   * @return the vertex associated with the pick point
   */
  E getEdge(LayoutModel<V> layoutModel, Point p);

  /**
   * Returns the edge, if any, associated with (x, y).
   *
   * @param x the x coordinate of the pick point
   * @param y the y coordinate of the pick point
   * @return the edge associated with (x, y)
   */
  E getEdge(LayoutModel<V> layoutModel, double x, double y);
}
