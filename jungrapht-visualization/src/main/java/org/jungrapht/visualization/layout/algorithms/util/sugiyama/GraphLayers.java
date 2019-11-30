package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.*;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphLayers<V, E> {

  private static final Logger log = LoggerFactory.getLogger(GraphLayers.class);

  private GraphLayers() {}

  public static <V, E> List<List<SV<V>>> assign(Graph<SV<V>, SE<V, E>> dag) {
    int rank = 0;
    List<List<SV<V>>> sorted = new ArrayList<>();
    List<SE<V, E>> edges = dag.edgeSet().stream().collect(Collectors.toCollection(LinkedList::new));
    List<SV<V>> vertices =
        dag.vertexSet().stream().collect(Collectors.toCollection(LinkedList::new));
    List<SV<V>> start =
        getVerticesWithoutIncomingEdges(dag, edges, vertices); // should be the roots

    while (start.size() > 0) {
      for (int i = 0; i < start.size(); i++) {
        SV<V> v = start.get(i);
        v.rank = rank;
        v.index = i;
      }
      sorted.add(start); // add a row
      Set<SV<V>> fstart = new HashSet<>(start);
      // remove any edges that start in the new row
      edges.removeIf(e -> fstart.contains(dag.getEdgeSource(e)));
      // remove any vertices that have been added to the row
      vertices.removeIf(fstart::contains);
      start = getVerticesWithoutIncomingEdges(dag, edges, vertices);
      rank++;
    }
    return sorted;
  }

  private static <V, E> List<SV<V>> getVerticesWithoutIncomingEdges(
      Graph<SV<V>, SE<V, E>> dag, Collection<SE<V, E>> edges, Collection<SV<V>> vertices) {
    // get targets of all edges
    Set<SV<V>> targets = edges.stream().map(e -> dag.getEdgeTarget(e)).collect(Collectors.toSet());
    // from vertices, filter out any that are an edge target
    return vertices.stream().filter(v -> !targets.contains(v)).collect(Collectors.toList());
  }

  public static <V> void checkLayers(List<List<SV<V>>> layers) {
    for (int i = 0; i < layers.size(); i++) {
      List<SV<V>> layer = layers.get(i);
      for (int j = 0; j < layer.size(); j++) {
        SV<V> sv = layer.get(j);
        assert i == sv.getRank();
        assert j == sv.getIndex();
      }
    }
  }
}
