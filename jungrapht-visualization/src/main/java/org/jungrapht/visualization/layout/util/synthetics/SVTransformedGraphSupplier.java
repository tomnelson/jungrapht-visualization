package org.jungrapht.visualization.layout.util.synthetics;

import java.util.function.Function;
import java.util.function.Supplier;
import org.jgrapht.Graph;

/**
 * Serves as a {@code Supplier} for a Graph&lt;V, E&gt; transformed to a Graph&lt;SV&lt;V&gt;,
 * SE&lt;E&gt;&gt;
 *
 * @param <V>
 * @param <E>
 */
public class SVTransformedGraphSupplier<V, E> implements Supplier<Graph<SV<V>, SE<E>>> {

  protected Graph<V, E> graph;

  public SVTransformedGraphSupplier(Graph<V, E> graph) {
    this.graph = graph;
  }

  protected SingletonTransformer<V, SV<V>> vertexTransformer;

  public SingletonTransformer<V, SV<V>> getVertexTransformer() {
    return vertexTransformer;
  }

  /**
   * Transform and return the graph
   *
   * @return a Graph&lt;SV&lt;V&gt;, SE&lt;E&gt;&gt;
   */
  @Override
  public Graph<SV<V>, SE<E>> get() {
    Function<V, SV<V>> vertexTransformFunction = SV::of;
    vertexTransformer = new SingletonTransformer<>(vertexTransformFunction);

    Function<E, SE<E>> edgeTransformFunction = e -> SE.of(e);
    SingletonTransformer<E, SE<E>> edgeTransformer =
        new SingletonTransformer<>(edgeTransformFunction);

    TransformingGraphView.Builder<V, SV<V>, E, SE<E>, ?, ?> builder =
        TransformingGraphView.<V, SV<V>, E, SE<E>>builder(graph)
            .vertexTransformFunction(vertexTransformer)
            .edgeTransformFunction(edgeTransformer);

    TransformingGraphView<V, SV<V>, E, SE<E>> graphView = builder.build();
    return graphView.build();
  }
}
