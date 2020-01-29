package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssignLayers<V, E> {

  private static final Logger log = LoggerFactory.getLogger(AssignLayers.class);
  protected Graph<LV<V>, LE<V, E>> dag;

  public AssignLayers(Graph<LV<V>, LE<V, E>> dag) {
    this.dag = dag;
  }

  public List<List<LV<V>>> assignLayers() {
    int rank = 0;
    List<List<LV<V>>> sorted = new ArrayList<>();
    List<LE<V, E>> edges = new LinkedList<>(dag.edgeSet());
    List<LV<V>> vertices = new LinkedList<>(dag.vertexSet());
    List<LV<V>> start = getVerticesWithoutIncomingEdges(edges, vertices); // should be the roots

    while (start.size() > 0) {
      for (int i = 0; i < start.size(); i++) {
        LV<V> v = start.get(i);
        v.setRank(rank);
        v.setIndex(i);
      }
      sorted.add(start); // add a row
      Set<LV<V>> fstart = new HashSet<>(start);
      // remove any edges that start in the new row
      edges.removeIf(e -> fstart.contains(dag.getEdgeSource(e)));
      // remove any vertices that have been added to the row
      vertices.removeIf(fstart::contains);
      start = getVerticesWithoutIncomingEdges(edges, vertices);
      rank++;
    }
    return sorted;
  }

  List<LV<V>> getVerticesWithoutIncomingEdges(
      Collection<LE<V, E>> edges, Collection<LV<V>> vertices) {
    // get targets of all edges
    Set<LV<V>> targets = edges.stream().map(e -> dag.getEdgeTarget(e)).collect(Collectors.toSet());
    // from vertices, filter out any that are an edge target
    return vertices.stream().filter(v -> !targets.contains(v)).collect(Collectors.toList());
  }

  public static <V> void checkLayers(List<List<LV<V>>> layers) {
    for (int i = 0; i < layers.size(); i++) {
      List<LV<V>> layer = layers.get(i);
      for (int j = 0; j < layer.size(); j++) {
        LV<V> LV = layer.get(j);
        if (i != LV.getRank())
          throw new IllegalArgumentException("fails rank check: " + i + ", " + LV);
        if (j != LV.getIndex())
          throw new IllegalArgumentException("fails index check: " + j + ", " + LV);
      }
    }
  }
}
