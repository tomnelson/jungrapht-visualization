package org.jungrapht.visualization.layout.algorithms.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkSimplexDevelopment<V, E> {

  private static final Logger log = LoggerFactory.getLogger(NetworkSimplexDevelopment.class);
  Graph<V, E> dag;
  Graph<V, E> spanningTree;

  public NetworkSimplexDevelopment(Graph<V, E> dag, Graph<V, E> spanningTree) {
    this.dag = dag;
    this.spanningTree = spanningTree;
  }

  public NetworkSimplexDevelopment(Graph<V, E> dag) {
    this(dag, getSpanningTree(dag));
  }

  public Graph<V, E> getTheBestSpanningTree() {
    // get a spanning tree for graph.
    // for each edge in the spanning tree
    Map<E, Integer> cutValueMap = getEdgeCutValues(spanningTree);

    E edgeToCut =
        cutValueMap
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() < 0)
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(null);

    while (edgeToCut != null) {
      Pair<Set<V>> headAndTail = getHeadAndTailComponents(spanningTree, edgeToCut);
      spanningTree.removeEdge(edgeToCut);
      Set<E> crossComponentEdges = getCrossComponentEdges(spanningTree, headAndTail);
      final E loser = edgeToCut;
      if (crossComponentEdges.size() > 0) {
        E edgeToAdd = crossComponentEdges.stream().filter(e -> e != loser).findFirst().get();
        V source = dag.getEdgeSource(edgeToAdd);
        V target = dag.getEdgeTarget(edgeToAdd);
        spanningTree.addEdge(source, target, edgeToAdd);
      }

      cutValueMap = getEdgeCutValues(spanningTree);
      log.info("cutValueMap: {}", cutValueMap);
      edgeToCut =
          cutValueMap
              .entrySet()
              .stream()
              .filter(entry -> entry.getValue() < 0)
              .findFirst()
              .map(Map.Entry::getKey)
              .orElse(null);
      log.info("edgeToCut: {}", edgeToCut);
    }

    // for all the cut values
    // find the lowest
    // remove that edge from the spanning tree copy
    // add in an edge that is not in the spanning tree and is not fully contained in either component

    // do it again until there are no negative value cut edges
    // make the spanning tree the one that has the removed and added edge

    return spanningTree;
  }

  static <V, E> Graph<V, E> getDirectedGraphFromSpanningTree(
      Graph<V, E> spanningTree, Graph<V, E> dag) {
    Graph<V, E> newGraph = GraphTypeBuilder.<V, E>forGraphType(DefaultGraphType.dag()).buildGraph();

    for (E edge : spanningTree.edgeSet()) {
      newGraph.addVertex(dag.getEdgeSource(edge));
      newGraph.addVertex(dag.getEdgeTarget(edge));
      newGraph.addEdge(dag.getEdgeSource(edge), dag.getEdgeTarget(edge), edge);
    }
    return newGraph;
  }

  public Set<E> getCrossComponentEdges(Graph<V, E> spanningTree, Pair<Set<V>> headAndTail) {
    return dag.edgeSet()
        .stream()
        .filter(e -> !(spanningTree.containsEdge(e)))
        .filter(
            e -> {
              V source = dag.getEdgeSource(e);
              V target = dag.getEdgeTarget(e);
              List<V> endpoints = List.of(source, target);
              return !headAndTail.first.containsAll(endpoints)
                  && !headAndTail.second.containsAll(endpoints);
            })
        .collect(Collectors.toSet());
  }

  public Map<E, Integer> getEdgeCutValues(Graph<V, E> spanningTree) {
    log.info("cutValues");
    Map<E, Integer> map = new HashMap<>();
    for (E edge : new ArrayList<>(spanningTree.edgeSet())) {
      spanningTree.removeEdge(edge);
      Pair<Set<V>> headAndTail = getHeadAndTailComponents(spanningTree, edge);
      int cutValue = cutValue(spanningTree, headAndTail);
      log.info(
          "cut value for edge {} is {}",
          edge + "{" + dag.getEdgeSource(edge) + "," + dag.getEdgeTarget(edge) + "}",
          cutValue);
      map.put(edge, cutValue);
      // put the edge back
      spanningTree.addEdge(dag.getEdgeSource(edge), dag.getEdgeTarget(edge), edge);
    }
    return map;
  }

  Pair<Set<V>> getHeadAndTailComponents(Graph<V, E> spanningTree, E edge) {
    V dagSource = dag.getEdgeSource(edge);
    V dagTarget = dag.getEdgeTarget(edge);
    spanningTree.removeEdge(edge);

    ConnectivityInspector<V, ?> connectivityInspector = new ConnectivityInspector<>(spanningTree);
    List<Set<V>> componentVertices = connectivityInspector.connectedSets();

    // should be 2 items in the list
    // headComponent is the one with dagTarget in it
    // tailComponent is the one with dagSource in it
    Set<V> headComponent = new HashSet<>();
    Set<V> tailComponent = new HashSet<>();
    for (Set<V> set : componentVertices) {
      if (set.contains(dagTarget)) {
        headComponent.addAll(set);
      } else if (set.contains(dagSource)) {
        tailComponent.addAll(set);
      }
    }
    return Pair.of(headComponent, tailComponent);
  }

  private int cutValue(Graph<V, E> spanningTree, Pair<Set<V>> headAndTail) {
    Set<V> headComponent = headAndTail.first;
    Set<V> tailComponent = headAndTail.second;
    // cut value is the count of all dag edges (including edge) from tail to head
    // minus the sum of all dag edges from head to tail

    // get all the edges that are in dag but not in spanningTree
    List<E> nonTreeEdges =
        dag.edgeSet()
            .stream()
            .filter(e -> !(spanningTree.containsEdge(e)))
            .collect(Collectors.toList());
    int tailToHead =
        (int)
            nonTreeEdges
                .stream()
                .filter(
                    e -> {
                      V source = dag.getEdgeSource(e);
                      V target = dag.getEdgeTarget(e);
                      return tailComponent.contains(source) && headComponent.contains(target);
                    })
                .count();
    int headToTail =
        (int)
            nonTreeEdges
                .stream()
                .filter(
                    e -> {
                      V source = dag.getEdgeSource(e);
                      V target = dag.getEdgeTarget(e);
                      return tailComponent.contains(target) && headComponent.contains(source);
                    })
                .count();
    return tailToHead - headToTail;
  }

  public static <V, E> Graph<V, E> getSpanningTree(Graph<V, E> graph) {

    if (graph.getType().isDirected()) {
      // make a non-directed version
      graph = new AsUndirectedGraph(graph);
    }
    SpanningTreeAlgorithm<E> prim = new PrimMinimumSpanningTree<>(graph);
    SpanningTreeAlgorithm.SpanningTree<E> tree = prim.getSpanningTree();
    Graph<V, E> newGraph = GraphTypeBuilder.<V, E>forGraphType(DefaultGraphType.dag()).buildGraph();

    for (E edge : tree.getEdges()) {
      newGraph.addVertex(graph.getEdgeSource(edge));
      newGraph.addVertex(graph.getEdgeTarget(edge));
      newGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge), edge);
    }
    return newGraph;
  }
}
