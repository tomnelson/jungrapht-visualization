package org.jungrapht.visualization.layout.algorithms.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
public class TestNetworkSimplexOne {

  private static final Logger log = LoggerFactory.getLogger(TestNetworkSimplexOne.class);

  @Test
  public void computeCutValues() {
    // need a dag like in the Ganser paper

    Graph<String, Integer> dag =
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

    Graph<String, Integer> spanningTree =
        GraphTypeBuilder.<String, Integer>undirected().buildGraph();
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

    Map<Integer, Integer> cutValues = getEdgeCutValues(dag, spanningTree);
    Assert.assertEquals(-1, (int) cutValues.get(7));
    Assert.assertEquals(3, (int) cutValues.get(2));
    Assert.assertEquals(3, (int) cutValues.get(3));
    Assert.assertEquals(0, (int) cutValues.get(4));
    Assert.assertEquals(3, (int) cutValues.get(6));
    Assert.assertEquals(3, (int) cutValues.get(8));
    // if there is a negative value...
    List<Integer> values = new ArrayList<>(cutValues.values());
    values.sort(Integer::compareTo);
    int edgeToCut = -1;
    for (Map.Entry<Integer, Integer> entry : cutValues.entrySet()) {
      if (entry.getValue() < 0) {
        edgeToCut = entry.getKey();
        break;
      }
    }
    while (edgeToCut >= 0) {
      List<Integer> nonTreeEdges =
          dag.edgeSet()
              .stream()
              .filter(e -> !(spanningTree.containsEdge(e)))
              .collect(Collectors.toList());
      spanningTree.removeEdge(edgeToCut);
      if (nonTreeEdges.size() > 0) {
        int edgeToAdd = nonTreeEdges.get(0);
        spanningTree.addEdge(dag.getEdgeSource(edgeToAdd), dag.getEdgeTarget(edgeToAdd), edgeToAdd);
      }
      cutValues = getEdgeCutValues(dag, spanningTree);

      // if there is a negative value...
      values = new ArrayList<>(cutValues.values());
      values.sort(Integer::compareTo);
      edgeToCut = -1;
      for (Map.Entry<Integer, Integer> entry : cutValues.entrySet()) {
        if (entry.getValue() < 0) {
          edgeToCut = entry.getKey();
          break;
        }
      }
    }
    log.info("bestSpanningTree: {}", spanningTree);
    Assert.assertTrue(spanningTree.edgeSet().containsAll(List.of(0, 2, 3, 4, 5, 6, 8)));
    Assert.assertFalse(spanningTree.edgeSet().contains(1));
    Assert.assertFalse(spanningTree.edgeSet().contains(7));
  }

  Map<Integer, Integer> getEdgeCutValues(
      Graph<String, Integer> dag, Graph<String, Integer> spanningTree) {
    Map<Integer, Integer> map = new HashMap<>();
    for (Integer edge : spanningTree.edgeSet()) {
      Graph<String, Integer> spanningTreeCopy =
          (Graph<String, Integer>) ((AbstractBaseGraph) spanningTree).clone();
      spanningTreeCopy.removeEdge(edge);
      int cutValue = cutValue(dag, spanningTreeCopy, edge);
      log.info(
          "cut value for edge {} is {}",
          edge + "{" + dag.getEdgeSource(edge) + "," + dag.getEdgeTarget(edge) + "}",
          cutValue);
      map.put(edge, cutValue);
    }
    return map;
  }

  Pair<List<String>> getHeadAndTailComponents(
      Graph<String, Integer> dag, Graph<String, Integer> spanningTree, Integer edge) {
    String dagSource = dag.getEdgeSource(edge);
    String dagTarget = dag.getEdgeTarget(edge);
    Graph<String, Integer> spanningTreeCopy =
        (Graph<String, Integer>) ((AbstractBaseGraph) spanningTree).clone();
    spanningTreeCopy.removeEdge(edge);

    ConnectivityInspector<String, ?> connectivityInspector =
        new ConnectivityInspector<>(spanningTreeCopy);
    List<Set<String>> componentVertices = connectivityInspector.connectedSets();

    // should be 2 items in the list
    // headComponent is the one with dagTarget in it
    // tailComponent is the one with dagSource in it
    List<String> headComponent = new ArrayList<>();
    List<String> tailComponent = new ArrayList<>();
    for (Set<String> set : componentVertices) {
      if (set.contains(dagTarget)) {
        headComponent.addAll(set);
      } else if (set.contains(dagSource)) {
        tailComponent.addAll(set);
      }
    }
    return Pair.of(headComponent, tailComponent);
  }

  private int cutValue(
      Graph<String, Integer> dag, Graph<String, Integer> spanningTreeCopy, Integer edge) {
    Pair<List<String>> headAndTail = getHeadAndTailComponents(dag, spanningTreeCopy, edge);
    List<String> headComponent = headAndTail.first;
    List<String> tailComponent = headAndTail.second;

    // cut value is the count of all dag edges (including edge) from tail to head
    // minus the sum of all dag edges from head to tail

    // get all the edges that are in dag but not in spanningTreeCopy
    List<Integer> nonTreeEdges =
        dag.edgeSet()
            .stream()
            .filter(e -> !(spanningTreeCopy.containsEdge(e)))
            .collect(Collectors.toList());
    int tailToHead =
        (int)
            nonTreeEdges
                .stream()
                .filter(
                    e -> {
                      String source = dag.getEdgeSource(e);
                      String target = dag.getEdgeTarget(e);
                      return tailComponent.contains(source) && headComponent.contains(target);
                    })
                .count();
    int headToTail =
        (int)
            nonTreeEdges
                .stream()
                .filter(
                    e -> {
                      String source = dag.getEdgeSource(e);
                      String target = dag.getEdgeTarget(e);
                      return tailComponent.contains(target) && headComponent.contains(source);
                    })
                .count();
    return tailToHead - headToTail;
  }
}
