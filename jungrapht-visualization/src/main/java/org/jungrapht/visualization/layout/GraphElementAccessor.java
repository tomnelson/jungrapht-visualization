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
import java.util.Collection;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.util.EdgeAccessor;
import org.jungrapht.visualization.layout.util.VertexAccessor;

/**
 * Interface for coordinate-based selection of graph components.
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public interface GraphElementAccessor<V, E> extends VertexAccessor<V>, EdgeAccessor<V, E> {

  /**
   * @param shape the region in which the returned vertices are located
   * @return the vertices whose locations given by {@code layoutModel} are contained within {@code
   *     shape}
   */
  Collection<V> getVertices(LayoutModel<V> layoutModel, Shape shape);
}
