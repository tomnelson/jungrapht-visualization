package org.jungrapht.visualization.layout.algorithms.util;

import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialDimensionFunction<V> implements Function<Graph<V, ?>, Pair<Integer>> {

  private static final Logger log = LoggerFactory.getLogger(InitialDimensionFunction.class);

  private static final Shape IDENTITY_SHAPE = new Ellipse2D.Double(-5, -5, 10, 10);

  Function<V, Shape> vertexShapeFunction = v -> IDENTITY_SHAPE;

  public InitialDimensionFunction() {}

  public InitialDimensionFunction(Function<V, Shape> vertexShapeFunction) {
    this.vertexShapeFunction = vertexShapeFunction;
  }

  public void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
    this.vertexShapeFunction = vertexShapeFunction;
  }

  /**
   * Applies this function to the given argument.
   *
   * @param graph the function argument
   * @return the function result
   */
  @Override
  public Pair<Integer> apply(Graph<V, ?> graph) {
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
    larger *= 10;
    log.info("returning w, h {} for graph with {} vertices", Pair.of(larger, larger), graph.vertexSet().size());
    return Pair.of(larger, larger);
  }
}
