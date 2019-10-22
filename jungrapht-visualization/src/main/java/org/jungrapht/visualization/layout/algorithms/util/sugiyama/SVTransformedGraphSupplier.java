package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.util.TransformingGraphView;

public class SVTransformedGraphSupplier<V, E> implements Supplier<Graph<SV<V>, SE<V, E>>> {
  private Graph<V, E> graph;

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

  public SVTransformedGraphSupplier(Graph<V, E> graph) {
    this.graph = graph;
  }

  SingletonTransformer<V, SV<V>> vertexTransformer;

  public SingletonTransformer<V, SV<V>> getVertexTransformer() {
    return vertexTransformer;
  }

  @Override
  public Graph<SV<V>, SE<V, E>> get() {
    Function<V, SV<V>> vertexTransformFunction = s -> SV.of(s);
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
            .edgeTransformFunction(edgeTransformer);

    TransformingGraphView<V, SV<V>, E, SE<V, E>> graphView = builder.build();
    return graphView.build();
  }
}
