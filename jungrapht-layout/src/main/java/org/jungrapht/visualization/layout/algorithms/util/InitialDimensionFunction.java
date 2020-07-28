package org.jungrapht.visualization.layout.algorithms.util;

import static org.jungrapht.visualization.layout.model.LayoutModel.PREFIX;

import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.Dimension;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialDimensionFunction<V> implements Function<Graph<V, ?>, Pair<Integer>> {

  private static final Logger log = LoggerFactory.getLogger(InitialDimensionFunction.class);

  private static final Rectangle IDENTITY_SHAPE = Rectangle.of(-5, -5, 10, 10);

  private static final String INITIAL_DIMENSION_VERTEX_DENSITY =
      PREFIX + "initialDimensionVertexDensity";

  public static class Builder<
      V, T extends InitialDimensionFunction<V>, B extends Builder<V, T, B>> {

    Builder(Function<V, Rectangle> vertexShapeFunction) {
      this.vertexShapeFunction = vertexShapeFunction;
    }

    Builder() {}

    protected Function<V, Rectangle> vertexShapeFunction = v -> IDENTITY_SHAPE;

    protected float weight =
        Float.parseFloat(System.getProperty(INITIAL_DIMENSION_VERTEX_DENSITY, "0.1f"));

    public B vertexShapeFunction(Function<V, Rectangle> vertexShapeFunction) {
      this.vertexShapeFunction = vertexShapeFunction;
      return (B) this;
    }

    public B weight(float weight) {
      this.weight = weight;
      return (B) this;
    }

    public T build() {
      return (T) new InitialDimensionFunction<>(this);
    }
  }

  public static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  public static <V> Builder<V, ?, ?> builder(Function<V, Rectangle> vertexShapeFunction) {
    return new Builder<>(vertexShapeFunction);
  }

  protected Function<V, Rectangle> vertexShapeFunction;

  protected float density;

  public InitialDimensionFunction() {
    this(InitialDimensionFunction.builder());
  }

  InitialDimensionFunction(Builder<V, ?, ?> builder) {
    this(builder.vertexShapeFunction, builder.weight);
  }

  InitialDimensionFunction(Function<V, Rectangle> vertexShapeFunction, float density) {
    this.vertexShapeFunction = vertexShapeFunction;
    this.density = density;
  }

  public void setVertexShapeFunction(Function<V, Rectangle> vertexShapeFunction) {
    this.vertexShapeFunction = vertexShapeFunction;
  }

  public void setDensity(float density) {
    this.density = density;
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
        .map(vertex -> vertexShapeFunction.apply(vertex))
        .forEach(dss::accept);
    Dimension average = dss.getAverage();
    int count = graph.vertexSet().size();
    int sqrt = (int) Math.sqrt(count);
    int larger = Math.max(average.width, average.height);
    larger *= sqrt;
    if (density > 0) {
      larger /= density;
    }
    if (log.isTraceEnabled()) {
      log.trace(
          "returning {} for graph with {} vertices",
          Pair.of(larger, larger),
          graph.vertexSet().size());
    }
    return Pair.of(larger, larger);
  }
}
