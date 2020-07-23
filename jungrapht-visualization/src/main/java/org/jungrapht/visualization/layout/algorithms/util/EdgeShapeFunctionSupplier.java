package org.jungrapht.visualization.layout.algorithms.util;

import java.awt.Shape;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.jgrapht.Graph;

public interface EdgeShapeFunctionSupplier<V, E> {

  void setEdgeShapeFunctionConsumer(Consumer<BiFunction<Graph<V, E>, E, Shape>> edgeShapeConsumer);
}
