/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 *
 * Created on Apr 12, 2005
 */
package org.jungrapht.visualization.layout;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Collection;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.util.NetworkNodeAccessor;

/**
 * Interface for coordinate-based selection of graph components.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 * @param <N> the node type
 * @param <E> the edge type
 */
public interface NetworkElementAccessor<N, E> extends NetworkNodeAccessor<N> {

  /**
   * @param rectangle the region in which the returned nodes are located
   * @return the nodes whose locations given by {@code layout} are contained within {@code
   *     rectangle}
   */
  Collection<N> getNodes(LayoutModel<N> layoutModel, Shape rectangle);

  /**
   * @param x the x coordinate of the location
   * @param y the y coordinate of the location
   * @return an edge which is associated with the location {@code (x,y)} as given by {@code layout},
   *     generally by reference to the edge's endpoints
   */
  E getEdge(LayoutModel<N> layoutModel, double x, double y);

  /**
   * @param layoutModel
   * @param p the pick location
   * @return an edge associated with the pick location
   */
  E getEdge(LayoutModel<N> layoutModel, Point2D p);
}
