package org.jungrapht.visualization.subLayout;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationServer;

public interface Collapser<V, E> {

  V collapse(Collection<V> vertices);

  void expand(Collection<V> vertices);

  void expand(V vertex);

  Function<V, Graph<V, E>> collapsedGraphFunction();

  Map<V, Graph<V, E>> getCollapsedGraphMap();

  V findOwnerOf(V vertex);

  static <V, E> Collapser<V, E> forGraph(Graph<V, E> graph, Supplier<V> vertexFactory) {
    return new GraphCollapser<>(graph, vertexFactory);
  }

  static <V, E> Collapser<V, E> forVisualization(
      VisualizationServer<V, E> vv, Supplier<V> vertexFactory) {
    return new VisualGraphCollapser<>(vv, vertexFactory);
  }
}
