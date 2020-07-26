package org.jungrapht.visualization.layout.algorithms.util;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNetworkSimplexSix {

  private static final Logger log = LoggerFactory.getLogger(TestNetworkSimplexSix.class);

  private Graph<String, Integer> dag;

  private Graph<String, Integer> spanningTree;

  @Test
  public void computeCutValues() {
    // need a dag like in the Ganser paper

    buildGraphAndSpanningTree();

    NetworkSimplexDevelopment<String, Integer> networkSimplexDevelopment =
        new NetworkSimplexDevelopment<>(dag);
    Graph<String, Integer> best = networkSimplexDevelopment.getTheBestSpanningTree();
    log.info("bestSpanningTree: {}", best);
  }

  private void buildGraphAndSpanningTree() {

    dag = generateProgramGraph2();
  }

  public static Graph<String, Integer> generateProgramGraph2() {
    Integer edgeCounter = 100;
    Integer zeroCounter = 0;
    GraphBuilder<String, Integer, Graph<String, Integer>> builder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag()).buildGraphBuilder();
    builder.addEdge("A0", "A1", zeroCounter++);
    builder.addEdge("A1", "A2", zeroCounter++);
    builder.addEdge("A2", "A3", zeroCounter++);
    builder.addEdge("A3", "A4", zeroCounter++);
    builder.addEdge("A3", "A5", edgeCounter++);
    builder.addEdge("A1", "A6", edgeCounter++);
    builder.addEdge("A0", "A6", edgeCounter++);
    builder.addEdge("A6", "A7", edgeCounter++);
    builder.addEdge("A6", "A8", edgeCounter++);
    builder.addEdge("A0", "A9", edgeCounter++);
    builder.addEdge("A9", "A10", edgeCounter++);
    builder.addEdge("A9", "A11", edgeCounter++);
    builder.addEdge("A10", "A12", edgeCounter++);
    builder.addEdge("A7", "A1", edgeCounter++);
    builder.addEdge("A12", "A2", edgeCounter++);
    builder.addEdge("A11", "A13", edgeCounter++);
    builder.addEdge("A11", "A14", edgeCounter++);

    builder.addEdge("B0", "B1", zeroCounter++);
    builder.addEdge("B1", "B2", zeroCounter++);
    builder.addEdge("B2", "B3", zeroCounter++);
    builder.addEdge("B3", "B4", zeroCounter++);
    builder.addEdge("B3", "B5", edgeCounter++);
    builder.addEdge("B1", "B6", edgeCounter++);
    builder.addEdge("B6", "B7", edgeCounter++);
    builder.addEdge("B6", "B8", edgeCounter++);
    builder.addEdge("B0", "B8", edgeCounter++);
    builder.addEdge("B0", "B9", edgeCounter++);
    builder.addEdge("B9", "B10", edgeCounter++);
    builder.addEdge("B9", "B11", edgeCounter++);
    builder.addEdge("B10", "B12", edgeCounter++);
    builder.addEdge("B7", "B1", edgeCounter++);
    builder.addEdge("B12", "B2", edgeCounter++);
    builder.addEdge("B11", "B13", edgeCounter++);
    builder.addEdge("B11", "B14", edgeCounter++);

    // edge to prior tree
    builder.addEdge("B14", "A1", edgeCounter++);
    //     edge to next tree
    builder.addEdge("A14", "B1", edgeCounter++);
    return builder.build();
  }
}
