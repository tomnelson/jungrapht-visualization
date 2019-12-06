package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.function.Function;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.util.synthetics.SingletonTransformer;
import org.jungrapht.visualization.layout.util.synthetics.TransformingGraphView;

public class SugiyamaTransformedGraphSupplier<V, E>
    implements Supplier<Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>>> {

  private Graph<V, E> graph;

  public SugiyamaTransformedGraphSupplier(Graph<V, E> graph) {
    this.graph = graph;
  }

  SingletonTransformer<V, SugiyamaVertex<V>> vertexTransformer;

  public SingletonTransformer<V, SugiyamaVertex<V>> getVertexTransformer() {
    return vertexTransformer;
  }

  @Override
  public Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> get() {
    Function<V, SugiyamaVertex<V>> vertexTransformFunction = SugiyamaVertex::of;
    vertexTransformer = new SingletonTransformer<>(vertexTransformFunction);

    Function<E, SugiyamaEdge<V, E>> edgeTransformFunction =
        e ->
            SugiyamaEdge.of(
                e,
                vertexTransformer.apply(graph.getEdgeSource(e)),
                vertexTransformer.apply(graph.getEdgeTarget(e)));
    SingletonTransformer<E, SugiyamaEdge<V, E>> edgeTransformer =
        new SingletonTransformer<>(edgeTransformFunction);

    TransformingGraphView.Builder<V, SugiyamaVertex<V>, E, SugiyamaEdge<V, E>, ?, ?> builder =
        TransformingGraphView.<V, SugiyamaVertex<V>, E, SugiyamaEdge<V, E>>builder(graph)
            .vertexTransformFunction(vertexTransformer)
            .edgeTransformFunction(edgeTransformer);

    TransformingGraphView<V, SugiyamaVertex<V>, E, SugiyamaEdge<V, E>> graphView = builder.build();
    return graphView.build();
  }
}
