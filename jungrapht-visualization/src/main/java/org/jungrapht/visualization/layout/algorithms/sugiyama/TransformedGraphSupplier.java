package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.function.Function;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.util.synthetics.SingletonTransformer;
import org.jungrapht.visualization.layout.util.synthetics.TransformingGraphView;

public class TransformedGraphSupplier<V, E> implements Supplier<Graph<LV<V>, LE<V, E>>> {

  protected Graph<V, E> graph;

  public TransformedGraphSupplier(Graph<V, E> graph) {
    this.graph = graph;
  }

  SingletonTransformer<V, LV<V>> vertexTransformer;

  public SingletonTransformer<V, LV<V>> getVertexTransformer() {
    return vertexTransformer;
  }

  @Override
  public Graph<LV<V>, LE<V, E>> get() {
    Function<V, LV<V>> vertexTransformFunction = LV::of;
    vertexTransformer = new SingletonTransformer<>(vertexTransformFunction);

    Function<E, LE<V, E>> edgeTransformFunction =
        e ->
            LE.of(
                e,
                vertexTransformer.apply(graph.getEdgeSource(e)),
                vertexTransformer.apply(graph.getEdgeTarget(e)));
    SingletonTransformer<E, LE<V, E>> edgeTransformer =
        new SingletonTransformer<>(edgeTransformFunction);

    TransformingGraphView.Builder<V, LV<V>, E, LE<V, E>, ?, ?> builder =
        TransformingGraphView.<V, LV<V>, E, LE<V, E>>builder(graph)
            .vertexTransformFunction(vertexTransformer)
            .edgeTransformFunction(edgeTransformer);

    TransformingGraphView<V, LV<V>, E, LE<V, E>> graphView = builder.build();
    return graphView.build();
  }
}
