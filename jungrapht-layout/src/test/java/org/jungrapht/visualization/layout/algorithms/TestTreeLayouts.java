package org.jungrapht.visualization.layout.algorithms;

import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestTreeLayouts {

  private static Logger log = LoggerFactory.getLogger(TestTreeLayouts.class);
  Graph<String, Integer> graph;

  @Before
  public void setup() {
    graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraphBuilder()
            .build();
  }

  @Test
  public void testTreeAndEdgeAwareTree() {
    int edge = 0;
    IntStream.range(0, 10)
        .forEach(
            i -> {
              graph.addVertex("A" + i);
              graph.addVertex("B" + i);
              graph.addVertex("C" + i);
              graph.addEdge("A" + i, "B" + i);
              graph.addEdge("A" + i, "C" + i);
            });

    TreeLayoutAlgorithm<String> treeLayoutAlgorithm =
        TreeLayoutAlgorithm.<String>builder().expandLayout(false).build();

    EdgeAwareTreeLayoutAlgorithm<String, Integer> multiRowEdgeAwareTreeLayoutAlgorithm =
        EdgeAwareTreeLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .expandLayout(false)
            .build();

    testEdgeAwareTrees(graph, treeLayoutAlgorithm, multiRowEdgeAwareTreeLayoutAlgorithm);
  }

  @Test
  public void testMultiRowTreeAndEdgeAwareMultiRowTree() {
    int edge = 0;
    IntStream.range(0, 10)
        .forEach(
            i -> {
              graph.addVertex("A" + i);
              graph.addVertex("B" + i);
              graph.addVertex("C" + i);
              graph.addEdge("A" + i, "B" + i);
              graph.addEdge("A" + i, "C" + i);
            });

    MultiRowTreeLayoutAlgorithm<String> treeLayoutAlgorithm =
        MultiRowTreeLayoutAlgorithm.<String>builder().expandLayout(false).build();

    MultiRowEdgeAwareTreeLayoutAlgorithm<String, Integer> multiRowEdgeAwareTreeLayoutAlgorithm =
        MultiRowEdgeAwareTreeLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .expandLayout(false)
            .build();

    testEdgeAwareTrees(graph, treeLayoutAlgorithm, multiRowEdgeAwareTreeLayoutAlgorithm);
  }

  private <V, E> void testEdgeAwareTrees(
      Graph<V, E> graph,
      TreeLayoutAlgorithm<V> layoutAlgorithmOne,
      TreeLayout<V> layoutAlgorithmTwo) {
    LayoutModel<V> layoutModelOne = LayoutModel.<V>builder().size(100, 100).graph(graph).build();
    LayoutModel<V> layoutModelTwo = LayoutModel.<V>builder().size(100, 100).graph(graph).build();
    layoutAlgorithmOne.visit(layoutModelOne);
    layoutAlgorithmTwo.visit(layoutModelTwo);

    Assert.assertEquals(layoutAlgorithmOne.getBaseBounds(), layoutAlgorithmTwo.getBaseBounds());
    Assert.assertEquals(layoutModelOne.getLocations(), layoutModelTwo.getLocations());

    Assert.assertEquals(layoutModelOne.getWidth(), layoutModelTwo.getWidth());
    Assert.assertEquals(layoutModelOne.getHeight(), layoutModelTwo.getHeight());
    log.info("treeLayout bounds: {}", layoutAlgorithmOne.getBaseBounds());
    log.info("edgeSortingTreeLayoutBounds: {}", layoutAlgorithmTwo.getBaseBounds());
    log.info("positions: {}", layoutModelOne.getLocations());
    log.info("positions: {}", layoutModelTwo.getLocations());
  }

  private <V> void testTrees(
      Graph<V, ?> graph, TreeLayout<V> layoutAlgorithmOne, TreeLayout<V> layoutAlgorithmTwo) {

    LayoutModel<V> layoutModelOne = LayoutModel.<V>builder().size(100, 100).graph(graph).build();

    LayoutModel<V> layoutModelTwo = LayoutModel.<V>builder().size(100, 100).graph(graph).build();

    layoutAlgorithmOne.visit(layoutModelOne);
    layoutAlgorithmTwo.visit(layoutModelTwo);

    Assert.assertEquals(layoutAlgorithmOne.getBaseBounds(), layoutAlgorithmTwo.getBaseBounds());
    Assert.assertEquals(layoutModelOne.getLocations(), layoutModelTwo.getLocations());

    Assert.assertEquals(layoutModelOne.getWidth(), layoutModelTwo.getWidth());
    Assert.assertEquals(layoutModelOne.getHeight(), layoutModelTwo.getHeight());
    log.info("treeLayout bounds: {}", layoutAlgorithmOne.getBaseBounds());
    log.info("edgeSortingTreeLayoutBounds: {}", layoutAlgorithmTwo.getBaseBounds());
    log.info("positions: {}", layoutModelOne.getLocations());
    log.info("positions: {}", layoutModelTwo.getLocations());
  }
}
