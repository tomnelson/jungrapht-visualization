package org.jungrapht.visualization.layout.algorithms.util;

import java.util.function.Function;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <V> incoming vertex type
 * @param <W> outgoing vertex type
 * @param <E> incoming edge type
 * @param <F> outgoing edge type
 */
public class TransformingGraphView<V, W, E, F> {

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
  private Function<V, W> vertexTransformFunction;
  private Function<E, F> edgeTransformFunction;

  TransformingGraphView(Builder<V, W, E, F, ?, ?> builder) {
    this.graph = builder.graph;
    this.vertexTransformFunction = builder.vertexTransformFunction;
    this.edgeTransformFunction = builder.edgeTransformFunction;
  }

  public Graph<W, F> build() {
    Graph<W, F> transformedGraph = GraphTypeBuilder.<W, F>directed().buildGraph();
    for (V v : graph.vertexSet()) {
      W w = vertexTransformFunction.apply(v);
      transformedGraph.addVertex(w);
    }
    for (E e : graph.edgeSet()) {
      W source = vertexTransformFunction.apply(graph.getEdgeSource(e));
      W target = vertexTransformFunction.apply(graph.getEdgeTarget(e));
      if (source.equals(target)) {
        // no cycles allowed in transformed graph
        continue;
      }
      F edge = edgeTransformFunction.apply(e);
      transformedGraph.addEdge(source, target, edge);
    }
    return transformedGraph;
  }
}
