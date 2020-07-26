/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 *
 */
package org.jungrapht.visualization.layout.util;

import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

/**
 * Interface for coordinate-based selection of graph vertices.
 *
 * @author Tom Nelson
 */
public interface VertexAccessor<V> {

  /**
   * @param layoutModel the source of graph element locations
   * @param p the pick point
   * @return the vertex associated with the pick point
   */
  V getVertex(LayoutModel<V> layoutModel, Point p);

  /**
   * Returns the vertex, if any, associated with (x, y).
   *
   * @param x the x coordinate of the pick point
   * @param y the y coordinate of the pick point
   * @return the vertex associated with (x, y)
   */
  V getVertex(LayoutModel<V> layoutModel, double x, double y);
}
