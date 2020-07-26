package org.jungrapht.visualization.layout.algorithms.util;

import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNetworkSimplexFive {

  private static final Logger log = LoggerFactory.getLogger(TestNetworkSimplexFive.class);

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
    dag =
        GraphTypeBuilder.<String, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    dag.addVertex("a");
    dag.addVertex("b");
    dag.addVertex("c");
    dag.addVertex("d");
    dag.addVertex("e");
    dag.addVertex("f");
    dag.addVertex("g");
    dag.addVertex("h");

    int ae = dag.addEdge("a", "e");
    int af = dag.addEdge("a", "f");
    int ab = dag.addEdge("a", "b");
    int bc = dag.addEdge("b", "c");
    int eg = dag.addEdge("e", "g");
    int fg = dag.addEdge("f", "g");
    int cd = dag.addEdge("c", "d");
    int gh = dag.addEdge("g", "h");
    int dh = dag.addEdge("d", "h");

    //        spanningTree = GraphTypeBuilder.<String, Integer>undirected()
    //                .buildGraph();
    //        spanningTree.addVertex("a");
    //        spanningTree.addVertex("b");
    //        spanningTree.addVertex("c");
    //        spanningTree.addVertex("d");
    //        spanningTree.addVertex("e");
    //        spanningTree.addVertex("f");
    //        spanningTree.addVertex("g");
    //        spanningTree.addVertex("h");
    //
    //        spanningTree.addEdge("a", "b", ab);
    //        spanningTree.addEdge("b", "c", bc);
    //        spanningTree.addEdge("e", "g", eg);
    //        spanningTree.addEdge("f", "g", fg);
    //        spanningTree.addEdge("c", "d", cd);
    //        spanningTree.addEdge("g", "h", gh);
    //        spanningTree.addEdge("d", "h", dh);
  }
}
