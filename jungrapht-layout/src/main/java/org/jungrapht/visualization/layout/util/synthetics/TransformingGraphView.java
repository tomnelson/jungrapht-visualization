package org.jungrapht.visualization.layout.util.synthetics;

import java.util.Objects;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a transformed view of the input Graph&lt;V, E&gt; The transformed graph will be of type
 * Graph&lt;W, F&gt;
 *
 * <p>The transformation from vertex type V to W is achieved via a vertexTransformFunction
 * Function&lt;V, W&gt; and the transformation from edge type E to F is achieved via an
 * edgeTransformFunction Function&lt;E, F&gt;
 *
 * @param <V> incoming vertex type
 * @param <W> outgoing vertex type
 * @param <E> incoming edge type
 * @param <F> outgoing edge type
 */
public class TransformingGraphView<V, W, E, F> {

  /**
   * builds the TransformGraphView instance with supplied Functions
   *
   * @param <V> incoming vertex type
   * @param <W> outgoing vertex type
   * @param <E> incoming edge type
   * @param <F> outgoing edge type
   * @param <T> the TransformingGraphView type
   * @param <B> the Builder type
   */
  public static class Builder<
      V,
      W,
      E,
      F,
      T extends TransformingGraphView<V, W, E, F>,
      B extends Builder<V, W, E, F, T, B>> {

    private Graph<V, E> graph;
    private Function<V, W> vertexTransformFunction;
    private Function<E, F> edgeTransformFunction;

    private Builder(Graph<V, E> graph) {
      this.graph = graph;
      Objects.requireNonNull(this.graph);
    }

    /** {@inheritDoc} */
    protected B self() {
      return (B) this;
    }

    /**
     * @param vertexTransformFunction {@link Function} to apply to vertices
     * @return this Builder
     */
    public B vertexTransformFunction(Function<V, W> vertexTransformFunction) {
      this.vertexTransformFunction = vertexTransformFunction;
      return self();
    }

    /**
     * @param edgeTransformFunction {@link Function} to apply to edges
     * @return this Builder
     */
    public B edgeTransformFunction(Function<E, F> edgeTransformFunction) {
      this.edgeTransformFunction = edgeTransformFunction;
      return self();
    }

    /** {@inheritDoc} */
    public T build() {
      return (T) new TransformingGraphView<>(this);
    }
  }
  /**
   * @param <V> vertex type
   * @param <E> edge type
   * @return a Builder ready to configure
   */
  public static <V, W, E, F> Builder<V, W, E, F, ?, ?> builder(Graph<V, E> graph) {
    return new Builder<>(graph);
  }

  private static final Logger log = LoggerFactory.getLogger(TransformingGraphView.class);
  private Graph<V, E> graph;
  private Function<V, ? extends W> vertexTransformFunction;
  private Function<E, ? extends F> edgeTransformFunction;

  TransformingGraphView(Builder<V, W, E, F, ?, ?> builder) {
    this.graph = builder.graph;
    this.vertexTransformFunction = builder.vertexTransformFunction;
    Objects.requireNonNull(this.vertexTransformFunction);
    this.edgeTransformFunction = builder.edgeTransformFunction;
    Objects.requireNonNull(this.edgeTransformFunction);
  }

  public Graph<W, F> build() {
    Graph<W, F> transformedGraph =
        GraphTypeBuilder.<W, F>forGraphType(graph.getType()).buildGraph();
    for (V v : graph.vertexSet()) {
      W w = vertexTransformFunction.apply(v);
      transformedGraph.addVertex(w);
    }
    for (E e : graph.edgeSet()) {
      W source = vertexTransformFunction.apply(graph.getEdgeSource(e));
      W target = vertexTransformFunction.apply(graph.getEdgeTarget(e));
      if (source.equals(target)) {
        // no loop edges allowed in the transformed graph
        continue;
      }
      F edge = edgeTransformFunction.apply(e);
      transformedGraph.addEdge(source, target, edge);
    }
    return transformedGraph;
  }
}
