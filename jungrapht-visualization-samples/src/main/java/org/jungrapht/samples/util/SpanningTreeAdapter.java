package org.jungrapht.samples.util;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;

public class SpanningTreeAdapter {

  /**
   * @param graph input graph, directed or undirected
   * @param <V> vertex type
   * @param <E> edge type
   * @return a directed acyclic graph that contains all vertices and edges from the minimum spanning
   *     tree of the input graph
   */
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
