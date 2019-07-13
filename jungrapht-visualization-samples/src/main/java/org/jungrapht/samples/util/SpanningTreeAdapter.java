package org.jungrapht.samples.util;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;

public class SpanningTreeAdapter {

  public static <N, E> Graph<N, E> getSpanningTree(Graph<N, E> network) {

    if (network.getType().isDirected()) {
      // make a non-directed version
      network = new AsUndirectedGraph(network);
    }
    SpanningTreeAlgorithm<E> prim = new PrimMinimumSpanningTree<>(network);
    SpanningTreeAlgorithm.SpanningTree<E> tree = prim.getSpanningTree();
    Graph<N, E> graph = GraphTypeBuilder.<N, E>forGraphType(DefaultGraphType.dag()).buildGraph();

    for (E edge : tree.getEdges()) {
      graph.addVertex(network.getEdgeSource(edge));
      if (!graph.addVertex(network.getEdgeTarget(edge))) continue;
      graph.addEdge(network.getEdgeSource(edge), network.getEdgeTarget(edge), edge);
    }
    return graph;
  }
}
