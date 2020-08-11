package org.jungrapht.visualization.layout.algorithms.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentGrouping {

  private static final Logger log = LoggerFactory.getLogger(ComponentGrouping.class);

  public static <V, E> List<V> groupByComponents(Graph<V, E> graph, List<V> vertices) {
    // if there are multiple components, arrange the first row order to group their roots
    ConnectivityInspector<V, E> connectivityInspector = new ConnectivityInspector<>(graph);
    List<Set<V>> componentVertices = connectivityInspector.connectedSets();
    if (log.isTraceEnabled()) {
      log.trace("there are {} components", componentVertices.size());
    }
    if (componentVertices.size() > 1) {
      vertices = groupByComponentMembership(componentVertices, vertices);
    }

    // sort the first layer so that isolated vertices and loop vertices are grouped together and at
    // one end of the rank
    vertices.sort(Comparator.comparingInt(v -> vertexIsolationScore(graph, v)));
    return vertices;
  }

  public static <V, E> List<Graph<V, E>> getComponentGraphs(Graph<V, E> graph) {
    List<Graph<V, E>> graphList = new ArrayList<>();
    ConnectivityInspector<V, E> connectivityInspector = new ConnectivityInspector<>(graph);
    List<Set<V>> componentVertices = connectivityInspector.connectedSets();
    Set<E> allEdges = new HashSet<>(graph.edgeSet());

    for (Set<V> set : componentVertices) {
      Graph<V, E> subGraph = GraphTypeBuilder.<V, E>forGraphType(graph.getType()).buildGraph();
      set.forEach(subGraph::addVertex);
      for (E edge : graph.edgeSet()) {
        V source = graph.getEdgeSource(edge);
        V target = graph.getEdgeTarget(edge);
        if (set.contains(source) && set.contains(target)) {
          subGraph.addEdge(source, target, edge);
          allEdges.remove(edge);
        }
      }
      graphList.add(subGraph);
    }
    if (!allEdges.isEmpty()) {
      throw new RuntimeException("edges were left out of subgraphs: " + allEdges);
    }
    return graphList;
  }

  private static <V> List<V> groupByComponentMembership(
      List<Set<V>> componentVertices, List<V> list) {
    List<V> groupedRow = new ArrayList<>();
    for (Set<V> set : componentVertices) {
      groupedRow.addAll(list.stream().filter(set::contains).collect(Collectors.toList()));
    }
    return groupedRow;
  }

  /**
   * to set vertex order to normal -> loop -> zeroDegree
   *
   * @param graph
   * @param v
   * @param <V>
   * @param <E>
   * @return
   */
  public static <V, E> int vertexIsolationScore(Graph<V, E> graph, V v) {
    if (isZeroDegreeVertex(graph, v)) return 2;
    if (isLoopVertex(graph, v)) return 1;
    return 0;
  }

  public static <V, E> boolean isLoopVertex(Graph<V, E> graph, V v) {
    return graph.outgoingEdgesOf(v).equals(graph.incomingEdgesOf(v));
  }

  public static <V, E> boolean isZeroDegreeVertex(Graph<V, E> graph, V v) {
    return graph.degreeOf(v) == 0;
  }

  public static <V, E> boolean isIsolatedVertex(Graph<V, E> graph, V v) {
    return isLoopVertex(graph, v) || isZeroDegreeVertex(graph, v);
  }
}
