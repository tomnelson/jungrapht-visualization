package org.jungrapht.visualization.layout.algorithms.util;

import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNetworkSimplexThree {

  private static final Logger log = LoggerFactory.getLogger(TestNetworkSimplexThree.class);

  private Graph<String, Integer> dag;

  private Graph<String, Integer> spanningTree;

  @Test
  public void computeCutValues() {
    // need a dag like in the Ganser paper

    buildGraphAndSpanningTree();

    NetworkSimplexDevelopment<String, Integer> networkSimplexDevelopment =
        new NetworkSimplexDevelopment<>(dag, spanningTree);
    Graph<String, Integer> best = networkSimplexDevelopment.getTheBestSpanningTree();
    log.info("bestSpanningTree: {}", best);
  }

  private void buildGraphAndSpanningTree() {
    this.dag =
        GraphTypeBuilder.<String, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    dag.addVertex("0");
    dag.addVertex("1");
    dag.addVertex("2");
    dag.addVertex("3");
    dag.addVertex("4");

    dag.addEdge("0", "1");
    dag.addEdge("1", "2");
    dag.addEdge("2", "3");
    dag.addEdge("3", "4");
    dag.addEdge("0", "4");
    dag.addEdge("2", "4");
    dag.addEdge("1", "3");
    dag.addEdge("1", "4");

    log.info("dag: {}", dag);
    spanningTree = NetworkSimplexDevelopment.getSpanningTree(dag);
    log.info("spanningTree: {}", spanningTree);
  }
}
