package org.jungrapht.visualization.layout.algorithms.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.builder.GraphTypeBuilder;

public class SubGraphFunction<V, E> implements Function<Graph<V, E>, List<Graph<V, E>>> {

  @Override
  public List<Graph<V, E>> apply(Graph<V, E> graph) {
    List<Graph<V, E>> subGraphList = new ArrayList<>();

    ConnectivityInspector<V, ?> connectivityInspector = new ConnectivityInspector<>(graph);
    List<Set<V>> componentVertices = connectivityInspector.connectedSets();
    if (componentVertices.size() > 1) {
      for (Set<V> vertexSet : componentVertices) {
        // get the graph for these vertices
        Graph<V, E> subGraph = GraphTypeBuilder.forGraph(graph).buildGraph();
        vertexSet.forEach(subGraph::addVertex);
        for (V v : vertexSet) {
          // get neighbors
          Graphs.successorListOf(graph, v)
              .forEach(s -> subGraph.addEdge(v, s, graph.getEdge(v, s)));
          Graphs.predecessorListOf(graph, v)
              .forEach(p -> subGraph.addEdge(p, v, graph.getEdge(p, v)));
        }
        subGraphList.add(subGraph);
      }
    }
    return subGraphList;
  }
}
