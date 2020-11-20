package org.jungrapht.visualization.sublayout;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationServer;

public interface Collapser<V, E> {

  V collapse(Collection<V> vertices, Function<Collection<V>, V> vertexFunction);

  void expand(Collection<V> vertices);

  void expand(V vertex);

  Function<V, Graph<V, E>> collapsedGraphFunction();

  Map<V, Graph<V, E>> getCollapsedGraphMap();

  V findOwnerOf(V vertex);

  static <V, E> Collapser<V, E> forGraph(Graph<V, E> graph) {
    return new GraphCollapser<>(graph);
  }

  static <V, E> Collapser<V, E> forVisualization(VisualizationServer<V, E> vv) {
    return new VisualGraphCollapser<>(vv);
  }
}
