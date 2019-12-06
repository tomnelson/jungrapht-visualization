package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaEdge;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaVertex;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticVertex;

public class BrandesKopf<V, E> {

  List<List<SugiyamaVertex<V>>> layers;
  Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph;

  void preprocessing() {
    int h = layers.size();
    Set<SugiyamaEdge<V, E>> markedSegments = new HashSet<>();
    for (int i = 2; i < h - 2; i++) {

      int k0 = 0;
      int el = 1;
      List<SugiyamaVertex<V>> thisLayer = layers.get(i); // Li
      List<SugiyamaVertex<V>> nextLayer = layers.get(i + 1); // Li+1
      for (int el1 = 1; el1 <= nextLayer.size(); el1++) {
        // get the vertex at next layer index el1
        SugiyamaVertex<V> vel1nextLayer = nextLayer.get(el1);
        if (el1 == nextLayer.size() || vel1nextLayer instanceof SyntheticVertex) {
          int k1 = thisLayer.size();
          if (vel1nextLayer instanceof SyntheticVertex) {
            Optional<SugiyamaEdge<V, E>> incomingEdgeOpt =
                graph.incomingEdgesOf(vel1nextLayer).stream().findFirst();
            if (incomingEdgeOpt.isPresent()) {
              SugiyamaEdge<V, E> incomingEdge = incomingEdgeOpt.get();
              SugiyamaVertex<V> upperNeighbor = graph.getEdgeSource(incomingEdge);
              k1 = upperNeighbor.getIndex();
            }
          }
          while (el <= el1) {
            for (SugiyamaVertex<V> upperNeighbor : getUpperNeighbors(graph, vel1nextLayer)) {
              int k = upperNeighbor.getIndex();
              if (k < k0 || k > k1) {
                markedSegments.add(graph.getEdge(layers.get(i).get(k), layers.get(el).get(i + 1)));
              }
            }
            el++;
          }
          k0 = k1;
        }
      }
    }
  }

  List<SugiyamaVertex<V>> getUpperNeighbors(
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph, SugiyamaVertex<V> v) {
    return graph
        .incomingEdgesOf(v)
        .stream()
        .map(e -> graph.getEdgeSource(e))
        .collect(Collectors.toList());
  }
}
