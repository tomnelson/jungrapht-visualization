package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.util.TransformingGraphView;

public class SVTransformedGraphSupplier<V, E> implements Supplier<Graph<SV<V>, SE<V, E>>> {

  /**
   * a Builder to create a configured instance
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  public static class Builder<
      V, E, T extends SVTransformedGraphSupplier<V, E>, B extends Builder<V, E, T, B>> {
    protected Graph<V, E> graph;
    protected Predicate<V> vertexPredicate = v -> true;
    protected Predicate<E> edgePredicate = e -> true;
    protected Comparator<V> vertexComparator = (v1, v2) -> 0;
    protected Comparator<E> edgeComparator = (e1, e2) -> 0;

    /** {@inheritDoc} */
    protected B self() {
      return (B) this;
    }

    /**
     * @param vertexPredicate {@link Predicate} to apply to vertices
     * @return this Builder
     */
    public B vertexPredicate(Predicate<V> vertexPredicate) {
      this.vertexPredicate = vertexPredicate;
      return self();
    }

    /**
     * @param edgePredicate {@link Predicate} to apply to edges
     * @return this Builder
     */
    public B edgePredicate(Predicate<E> edgePredicate) {
      this.edgePredicate = edgePredicate;
      return self();
    }

    /**
     * @param vertexComparator {@link Comparator} to sort vertices
     * @return this Builder
     */
    public B vertexComparator(Comparator<V> vertexComparator) {
      this.vertexComparator = vertexComparator;
      return self();
    }

    /**
     * @param edgeComparator {@link Comparator} to sort edges
     * @return this Builder
     */
    public B edgeComparator(Comparator<E> edgeComparator) {
      this.edgeComparator = edgeComparator;
      return self();
    }

    public B graph(Graph<V, E> graph) {
      this.graph = graph;
      return self();
    }

    /** {@inheritDoc} */
    public T build() {
      return (T) new SVTransformedGraphSupplier<>(this);
    }
  }

  /**
   * @param <V> vertex type
   * @param <E> edge type
   * @return a Builder ready to configure
   */
  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  private Graph<V, E> graph;
  protected Predicate<V> vertexPredicate;
  protected Predicate<E> edgePredicate;
  protected Comparator<V> vertexComparator;
  protected Comparator<E> edgeComparator;

  public static class SingletonTransformer<S, T> implements Function<S, T> {

    Map<S, T> transformedMap = new HashMap<>();
    Function<S, T> transformFunction;

    public SingletonTransformer(Function<S, T> transformFunction) {
      this.transformFunction = transformFunction;
    }

    @Override
    public T apply(S s) {
      if (!transformedMap.containsKey(s)) {
        transformedMap.put(s, transformFunction.apply(s));
      }
      return transformedMap.get(s);
    }

    public Map<S, T> getTransformedMap() {
      return transformedMap;
    }
  }

  private SVTransformedGraphSupplier(Builder<V, E, ?, ?> builder) {
    this(
        builder.graph,
        builder.vertexPredicate,
        builder.edgePredicate,
        builder.vertexComparator,
        builder.edgeComparator);
  }

  private SVTransformedGraphSupplier(
      Graph<V, E> graph,
      Predicate<V> vertexPredicate,
      Predicate<E> edgePredicate,
      Comparator<V> vertexComparator,
      Comparator<E> edgeComparator) {
    this.graph = graph;
    this.vertexComparator = vertexComparator;
    this.vertexPredicate = vertexPredicate;
    this.edgeComparator = edgeComparator;
    this.edgePredicate = edgePredicate;
  }

  SingletonTransformer<V, SV<V>> vertexTransformer;

  public SingletonTransformer<V, SV<V>> getVertexTransformer() {
    return vertexTransformer;
  }

  @Override
  public Graph<SV<V>, SE<V, E>> get() {
    Function<V, SV<V>> vertexTransformFunction = SV::of;
    vertexTransformer = new SingletonTransformer<>(vertexTransformFunction);

    Function<E, SE<V, E>> edgeTransformFunction =
        e ->
            SE.of(
                e,
                vertexTransformer.apply(graph.getEdgeSource(e)),
                vertexTransformer.apply(graph.getEdgeTarget(e)));
    SingletonTransformer<E, SE<V, E>> edgeTransformer =
        new SingletonTransformer<>(edgeTransformFunction);

    TransformingGraphView.Builder<V, SV<V>, E, SE<V, E>, ?, ?> builder =
        TransformingGraphView.<V, SV<V>, E, SE<V, E>>builder(graph)
            .vertexTransformFunction(vertexTransformer)
            .edgeTransformFunction(edgeTransformer)
            .edgePredicate(edgePredicate)
            .edgeComparator(edgeComparator)
            .vertexPredicate(vertexPredicate)
            .vertexComparator(vertexComparator);

    TransformingGraphView<V, SV<V>, E, SE<V, E>> graphView = builder.build();
    return graphView.build();
  }
}
