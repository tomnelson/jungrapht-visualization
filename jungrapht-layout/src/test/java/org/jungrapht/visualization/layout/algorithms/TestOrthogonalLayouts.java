package org.jungrapht.visualization.layout.algorithms;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.algorithms.orthogonal.OrthogonalLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

public class TestOrthogonalLayouts {

  private static Logger log = LoggerFactory.getLogger(TestOrthogonalLayouts.class);
  Graph<String, Integer> graph;

  @Before
  public void setup() {
    graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .allowingSelfLoops(true)
            .buildGraph();

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

  }

//  @Test
//  public void testTreeAndEdgeAwareTree() {
//    int edge = 0;
//    IntStream.range(0, 10)
//        .forEach(
//            i -> {
//              graph.addVertex("A" + i);
//              graph.addVertex("B" + i);
//              graph.addVertex("C" + i);
//              graph.addEdge("A" + i, "B" + i);
//              graph.addEdge("A" + i, "C" + i);
//            });
//
//    TreeLayoutAlgorithm<String> treeLayoutAlgorithm =
//        TreeLayoutAlgorithm.<String>builder().expandLayout(false).build();
//
//    EdgeAwareTreeLayoutAlgorithm<String, Integer> multiRowEdgeAwareTreeLayoutAlgorithm =
//        EdgeAwareTreeLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
//            .expandLayout(false)
//            .build();
//
//    testEdgeAwareTrees(graph, treeLayoutAlgorithm, multiRowEdgeAwareTreeLayoutAlgorithm);
//  }
//
//  @Test
//  public void testMultiRowTreeAndEdgeAwareMultiRowTree() {
//    int edge = 0;
//    IntStream.range(0, 10)
//        .forEach(
//            i -> {
//              graph.addVertex("A" + i);
//              graph.addVertex("B" + i);
//              graph.addVertex("C" + i);
//              graph.addEdge("A" + i, "B" + i);
//              graph.addEdge("A" + i, "C" + i);
//            });
//
//    MultiRowTreeLayoutAlgorithm<String> treeLayoutAlgorithm =
//        MultiRowTreeLayoutAlgorithm.<String>builder().expandLayout(false).build();
//
//    MultiRowEdgeAwareTreeLayoutAlgorithm<String, Integer> multiRowEdgeAwareTreeLayoutAlgorithm =
//        MultiRowEdgeAwareTreeLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
//            .expandLayout(false)
//            .build();
//
//    testEdgeAwareTrees(graph, treeLayoutAlgorithm, multiRowEdgeAwareTreeLayoutAlgorithm);
//  }

//  private <V, E> void testEdgeAwareTrees(
//      Graph<V, E> graph,
//      TreeLayoutAlgorithm<V> layoutAlgorithmOne,
//      TreeLayout<V> layoutAlgorithmTwo) {
//    LayoutModel<V> layoutModelOne = LayoutModel.<V>builder().size(100, 100).graph(graph).build();
//    LayoutModel<V> layoutModelTwo = LayoutModel.<V>builder().size(100, 100).graph(graph).build();
//    layoutAlgorithmOne.visit(layoutModelOne);
//    layoutAlgorithmTwo.visit(layoutModelTwo);
//
//    Assert.assertEquals(layoutAlgorithmOne.getBaseBounds(), layoutAlgorithmTwo.getBaseBounds());
//    Assert.assertEquals(layoutModelOne.getLocations(), layoutModelTwo.getLocations());
//
//    Assert.assertEquals(layoutModelOne.getWidth(), layoutModelTwo.getWidth());
//    Assert.assertEquals(layoutModelOne.getHeight(), layoutModelTwo.getHeight());
//    log.info("treeLayout bounds: {}", layoutAlgorithmOne.getBaseBounds());
//    log.info("edgeSortingTreeLayoutBounds: {}", layoutAlgorithmTwo.getBaseBounds());
//    log.info("positions: {}", layoutModelOne.getLocations());
//    log.info("positions: {}", layoutModelTwo.getLocations());
//  }

//  private <V> void testTrees(
//      Graph<V, ?> graph, TreeLayout<V> layoutAlgorithmOne, TreeLayout<V> layoutAlgorithmTwo) {
//
//    LayoutModel<V> layoutModelOne = LayoutModel.<V>builder().size(100, 100).graph(graph).build();
//
//    LayoutModel<V> layoutModelTwo = LayoutModel.<V>builder().size(100, 100).graph(graph).build();
//
//    layoutAlgorithmOne.visit(layoutModelOne);
//    layoutAlgorithmTwo.visit(layoutModelTwo);
//
//    Assert.assertEquals(layoutAlgorithmOne.getBaseBounds(), layoutAlgorithmTwo.getBaseBounds());
//    Assert.assertEquals(layoutModelOne.getLocations(), layoutModelTwo.getLocations());
//
//    Assert.assertEquals(layoutModelOne.getWidth(), layoutModelTwo.getWidth());
//    Assert.assertEquals(layoutModelOne.getHeight(), layoutModelTwo.getHeight());
//    log.info("treeLayout bounds: {}", layoutAlgorithmOne.getBaseBounds());
//    log.info("edgeSortingTreeLayoutBounds: {}", layoutAlgorithmTwo.getBaseBounds());
//    log.info("positions: {}", layoutModelOne.getLocations());
//    log.info("positions: {}", layoutModelTwo.getLocations());
//  }

  @Test
  public void testThis() {

    LayoutModel<String> layoutModel =
        LayoutModel.<String>builder().size(100, 100).graph(graph).build();

    OrthogonalLayoutAlgorithm<String, Integer> layoutAlgorithm =
            OrthogonalLayoutAlgorithm.<String, Integer>builder().build();
    layoutAlgorithm.visit(layoutModel);
//    testPositions(layoutModel);
//
//    TidierTreeLayoutAlgorithm<String, Integer> tidierTreeLayoutAlgorithm =
//        TidierTreeLayoutAlgorithm.<String, Integer>edgeAwareBuilder().build();
//    tidierTreeLayoutAlgorithm.visit(layoutModel);
//    testPositions(layoutModel);
  }

//  private void testPositions(LayoutModel<String> layoutModel) {
//    // there should be 5 vertices in the top row, all with equal y values
//    log.info("Positions: {}", layoutModel.getLocations());
//    Assert.assertEquals(
//        "Y values should match", layoutModel.get("I1").y, layoutModel.get("I2").y, .1);
//    Assert.assertEquals(
//        "Y values should match", layoutModel.get("I2").y, layoutModel.get("L1").y, .1);
//    Assert.assertEquals(
//        "Y values should match", layoutModel.get("L1").y, layoutModel.get("L2").y, .1);
//    Assert.assertEquals(
//        "Y values should match", layoutModel.get("L2").y, layoutModel.get("R").y, .1);
//    Assert.assertNotEquals(
//        "Y values should not match", layoutModel.get("R").y, layoutModel.get("C1").y, .1);
//
//    // there should be one vertex in row 2 with x value the same as "P"
//    Assert.assertEquals(
//        "X values should match", layoutModel.get("R").x, layoutModel.get("C1").x, .1);
//  }
}
