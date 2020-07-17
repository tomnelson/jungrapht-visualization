package org.jungrapht.visualization.layout.algorithms.util;

import java.awt.Dimension;
import java.awt.Shape;
import java.util.function.Function;
import org.jgrapht.Graph;

public class InitialDimensionFunction<V> implements Function<Graph<V, ?>, Integer> {

  Function<V, Shape> vertexShapeFunction;

  public InitialDimensionFunction(Function<V, Shape> vertexShapeFunction) {
    this.vertexShapeFunction = vertexShapeFunction;
  }

  /**
   * Applies this function to the given argument.
   *
   * @param graph the function argument
   * @return the function result
   */
  @Override
  public Integer apply(Graph<V, ?> graph) {
    DimensionSummaryStatistics dss = new DimensionSummaryStatistics();
    graph
        .vertexSet()
        .stream()
        .map(vertex -> vertexShapeFunction.apply(vertex).getBounds())
        .forEach(dss::accept);
    Dimension average = dss.getAverage();
    int count = graph.vertexSet().size();
    int sqrt = (int) Math.sqrt(count);
    int larger = Math.max(average.width, average.height);
    larger *= sqrt;
    larger *= 3;
    return larger;
  }
}
