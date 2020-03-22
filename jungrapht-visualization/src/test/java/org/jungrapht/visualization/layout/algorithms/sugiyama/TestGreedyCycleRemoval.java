package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Collection;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestGreedyCycleRemoval {

  private static final Logger log = LoggerFactory.getLogger(TestGreedyCycleRemoval.class);
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
    //    graph.addEdge("F", "B");
    graph.addEdge("D", "G");
    graph.addEdge("E", "A");
  }

  @Test
  public void testCycleRemoval() {

    GreedyCycleRemoval<String, Integer> gcr = new GreedyCycleRemoval<>(graph);

    Assert.assertTrue(gcr.getFeedbackArcs().containsAll(Set.of(8)));
  }

  @Test
  public void anotherTest() {
    Graph<String, Integer> graph = generateProgramGraph2();

    GreedyCycleRemoval<String, Integer> gcr = new GreedyCycleRemoval<>(graph);

    log.info("reverseArcs: {}", gcr.getFeedbackArcs());
    Assert.assertTrue(gcr.getFeedbackArcs().containsAll(Set.of(122, 109)));
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

  @Test
  public void smallTest() {
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "B");
    graph.addEdge("B", "C");
    graph.addEdge("C", "D");
    graph.addEdge("D", "B"); // introduces a cycle

    log.info("graph: {}", graph);
    Assert.assertEquals(graph.getEdgeSource(3), "D");
    Assert.assertEquals(graph.getEdgeTarget(3), "B");

    TransformedGraphSupplier<String, Integer> transformedGraphSupplier =
        new TransformedGraphSupplier(graph);
    Graph<LV<String>, LE<String, Integer>> svGraph = transformedGraphSupplier.get();

    GreedyCycleRemoval<LV<String>, LE<String, Integer>> gcr = new GreedyCycleRemoval<>(svGraph);

    Collection<LE<String, Integer>> feedbackArcs = gcr.getFeedbackArcs();

    for (LE<String, Integer> se : feedbackArcs) {
      svGraph.removeEdge(se);
      LE<String, Integer> newEdge =
          LE.<String, Integer>of(se.getEdge(), se.getTarget(), se.getSource());
      svGraph.addEdge(newEdge.getSource(), newEdge.getTarget(), newEdge);
    }
    log.info("reverseArcs: {}", gcr.getFeedbackArcs());

    log.info("svGraph edges: {}", svGraph.edgeSet());
    log.info("svGraphs: {}", svGraph);
  }
}
