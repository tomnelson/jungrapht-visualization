package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.*;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphLayers {

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

  /**
   * Find all vertices that have no incoming edges by
   *
   * <p>
   *
   * <ul>
   *   <li>collect all vertices that are edge targets
   *   <li>collect all vertices that are not part of that set of targets
   * </ul>
   *
   * Note that loop edges have already been removed from the graph, so any vertices that have only
   * loop edges will appear in the set of vertices without incoming edges.
   *
   * @param dag the {@code Graph} to examine
   * @param edges all edges from the {@code Graph}. Note that loop edges have already been removed
   *     from this set
   * @param vertices all vertices in the {@code Graph} (including vertices that have only loop edges
   * @param <V> vertex type
   * @param <E> edge type
   * @return vertices in the graph that have no incoming edges (or only loop edges)
   */
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
        if (i != sugiyamaVertex.getRank()) {
          log.error("{} is not the rank of {}", i, sugiyamaVertex);
          throw new RuntimeException("rank is wrong");
        }
        if (j != sugiyamaVertex.getIndex()) {
          log.error("{} is not the index of {}", j, sugiyamaVertex);
          throw new RuntimeException("index is wrong");
        }
      }
    }
  }

  public static <V> void checkLayers(SugiyamaVertex<V>[][] layers) {
    if (log.isTraceEnabled()) {
      for (int i = 0; i < layers.length; i++) {
        for (int j = 0; j < layers[i].length; j++) {
          if (i != layers[i][j].getRank()) {
            log.error("{} is not the rank of {}", i, layers[i][j]);
            throw new RuntimeException(i + " is not the rank of " + layers[i][j]);
          }
          if (j != layers[i][j].getIndex()) {
            log.error("{} is not the index of {}", j, layers[i][j]);
            throw new RuntimeException(j + " is not the index of " + layers[i][j]);
          }
        }
      }
    }
  }
}
