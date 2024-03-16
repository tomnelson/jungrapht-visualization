package org.jungrapht.visualization.layout.algorithms.util;

import java.util.List;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNetworkSimplexFour {

  private static final Logger log = LoggerFactory.getLogger(TestNetworkSimplexFour.class);

  private Graph<String, Integer> dag;

  private Graph<String, Integer> spanningTree;

  @Test
  public void computeCutValues() {

    buildGraphAndSpanningTree();

    NetworkSimplexDevelopment<String, Integer> networkSimplexDevelopment =
        new NetworkSimplexDevelopment<>(dag, spanningTree);

    Map<Integer, Integer> expectedFirstCutValueMap =
        Map.of(2, 3, 3, 3, 4, 0, 5, 0, 6, 3, 7, -1, 8, 3);
    // preliminary cut value map. check to make sure it matches values in the paper
    Map<Integer, Integer> cutValueMap = networkSimplexDevelopment.getEdgeCutValues(spanningTree);
    Assert.assertEquals(expectedFirstCutValueMap, cutValueMap);
    Graph<String, Integer> best = networkSimplexDevelopment.getTheBestSpanningTree();
    cutValueMap = networkSimplexDevelopment.getEdgeCutValues(spanningTree);

    Map<Integer, Integer> expectedSecondCutValueMap =
        Map.of(0, 1, 2, 2, 3, 2, 4, 1, 5, 0, 6, 2, 8, 2);
    Assert.assertEquals(expectedSecondCutValueMap, cutValueMap);

    log.info("bestSpanningTree: {}", best);
    Assert.assertTrue(best.edgeSet().containsAll(List.of(0, 2, 3, 4, 5, 6, 8)));
    Assert.assertFalse(best.containsEdge(1));
    Assert.assertFalse(best.containsEdge(7));
  }

  /** build the graph and the spanning tree shown in the paper */
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

    spanningTree = GraphTypeBuilder.<String, Integer>undirected().buildGraph();
    spanningTree.addVertex("a");
    spanningTree.addVertex("b");
    spanningTree.addVertex("c");
    spanningTree.addVertex("d");
    spanningTree.addVertex("e");
    spanningTree.addVertex("f");
    spanningTree.addVertex("g");
    spanningTree.addVertex("h");

    spanningTree.addEdge("a", "b", ab);
    spanningTree.addEdge("b", "c", bc);
    spanningTree.addEdge("e", "g", eg);
    spanningTree.addEdge("f", "g", fg);
    spanningTree.addEdge("c", "d", cd);
    spanningTree.addEdge("g", "h", gh);
    spanningTree.addEdge("d", "h", dh);
  }
}
