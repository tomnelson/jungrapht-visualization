package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.*;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphLayers<V, E> {

  private static final Logger log = LoggerFactory.getLogger(GraphLayers.class);

  private GraphLayers() {}

  public static <V, E> List<List<SugiyamaVertex<V>>> assign(
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> dag) {
    int rank = 0;
    List<List<SugiyamaVertex<V>>> sorted = new ArrayList<>();
    List<SugiyamaEdge<V, E>> edges =
        dag.edgeSet().stream().collect(Collectors.toCollection(LinkedList::new));
    List<SugiyamaVertex<V>> vertices =
        dag.vertexSet().stream().collect(Collectors.toCollection(LinkedList::new));
    List<SugiyamaVertex<V>> start =
        getVerticesWithoutIncomingEdges(dag, edges, vertices); // should be the roots

    while (start.size() > 0) {
      for (int i = 0; i < start.size(); i++) {
        SugiyamaVertex<V> v = start.get(i);
        v.rank = rank;
        v.index = i;
      }
      sorted.add(start); // add a row
      Set<SugiyamaVertex<V>> fstart = new HashSet<>(start);
      // remove any edges that start in the new row
      edges.removeIf(e -> fstart.contains(dag.getEdgeSource(e)));
      // remove any vertices that have been added to the row
      vertices.removeIf(fstart::contains);
      start = getVerticesWithoutIncomingEdges(dag, edges, vertices);
      rank++;
    }
    return sorted;
  }

  private static <V, E> List<SugiyamaVertex<V>> getVerticesWithoutIncomingEdges(
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> dag,
      Collection<SugiyamaEdge<V, E>> edges,
      Collection<SugiyamaVertex<V>> vertices) {
    // get targets of all edges
    Set<SugiyamaVertex<V>> targets =
        edges.stream().map(e -> dag.getEdgeTarget(e)).collect(Collectors.toSet());
    // from vertices, filter out any that are an edge target
    return vertices.stream().filter(v -> !targets.contains(v)).collect(Collectors.toList());
  }

  public static <V> void checkLayers(List<List<SugiyamaVertex<V>>> layers) {
    for (int i = 0; i < layers.size(); i++) {
      List<SugiyamaVertex<V>> layer = layers.get(i);
      for (int j = 0; j < layer.size(); j++) {
        SugiyamaVertex<V> sugiyamaVertex = layer.get(j);
        assert i == sugiyamaVertex.getRank();
        assert j == sugiyamaVertex.getIndex();
      }
    }
  }
}
