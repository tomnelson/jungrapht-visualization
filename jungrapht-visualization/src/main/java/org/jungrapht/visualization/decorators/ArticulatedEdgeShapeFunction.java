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

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.util.Context;

/**
 * An abstract class for edge-to-Shape functions that work with passed articulation points.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public abstract class ArticulatedEdgeShapeFunction<V, E>
    implements Function<Context<Graph<V, E>, E>, Shape> {

  protected Function<E, List<Point>> edgeArticulationFunction = e -> Collections.emptyList();

  public void setEdgeArticulationFunction(Function<E, List<Point>> edgeArticulationFunction) {
    this.edgeArticulationFunction = edgeArticulationFunction;
  }

  public Function<E, List<Point>> getEdgeArticulationFunction() {
    return edgeArticulationFunction;
  }
}
