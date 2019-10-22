package org.jungrapht.visualization.layout.algorithms.util;

import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Before;
import org.junit.Test;

public class TestGreedyCycleRemoval {

  Graph<String, Integer> graph;

  @Before
  public void setup() {
    graph =
        GraphTypeBuilder.<String, Integer>directed()
            .vertexSupplier(SupplierUtil.createSupplier(String.class))
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addVertex("E");
    graph.addVertex("F");
    graph.addVertex("G");
    graph.addEdge("A", "B");
    graph.addEdge("A", "C");
    graph.addEdge("C", "D");
    graph.addEdge("A", "D");
    graph.addEdge("B", "E");
    graph.addEdge("D", "E");
    graph.addEdge("E", "F");
    graph.addEdge("F", "B");
    graph.addEdge("D", "G");
  }

  @Test
  public void testCycleRemoval() {
    GreedyCycleRemoval<String, Integer> gcr = new GreedyCycleRemoval<>(graph);
  }
}
