package org.jungrapht.visualization;

import java.awt.*;
import java.util.Collection;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.graph.Pseudograph;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.RadiusVertexAccessor;
import org.jungrapht.visualization.layout.util.VertexAccessor;
import org.jungrapht.visualization.spatial.Spatial;
import org.jungrapht.visualization.spatial.rtree.TreeNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * test to make sure that a search for a node returns the same leaf that you get when you search for
 * the point location of that node
 *
 * @author Tom Nelson
 */
public class SpatialRTreeTest {

  private static final Logger log = LoggerFactory.getLogger(SpatialRTreeTest.class);

  Graph<String, Number> graph;
  LayoutModel<String> layoutModel;
  Spatial<String> tree;

  @Before
  public void setup() {
    // generate 100 random nodes in a graph at random locations in the layoutModel
    this.graph = Pseudograph.<String, Number>createBuilder(Number.class).build();
    IntStream.range(0, 10).mapToObj(i -> "N" + i).forEach(graph::addVertex);

    VisualizationServer<String, Number> vv =
        BasicVisualizationServer.builder(graph)
            .layoutAlgorithm(new StaticLayoutAlgorithm())
            .viewSize(new Dimension(600, 600))
            .build();

    tree = vv.getVertexSpatial();

    layoutModel = vv.getModel().getLayoutModel();
  }

  /**
   * confirm that the quadtree cell for a node is the same as the quadtree cell for the node's
   * location
   */
  @Test
  public void testRandomPointsAndLocations() {
    for (String node : graph.vertexSet()) {
      Point location = layoutModel.apply(node);
      Collection<? extends TreeNode> pointQuadTrees =
          tree.getContainingLeafs(location.x, location.y);
      TreeNode nodeQuadTree = tree.getContainingLeaf(node);
      Assert.assertTrue(pointQuadTrees.contains(nodeQuadTree));
    }
  }

  /**
   * test that the closest node for a random point is the same one returned for the
   * RadiusVertexAccessor and for the SpatialQuadTree Test with 1000 randomly generated points
   */
  @Test
  public void testClosestVertices() {
    final int COUNT = 100;
    VertexAccessor<String> slowWay = new RadiusVertexAccessor<>(Double.MAX_VALUE);

    // look for nodes closest to COUNT random locations
    for (int i = 0; i < COUNT; i++) {
      double x = Math.random() * layoutModel.getWidth();
      double y = Math.random() * layoutModel.getHeight();
      // use the slowWay
      String winnerOne = slowWay.getVertex(layoutModel, x, y);
      // use the quadtree
      String winnerTwo = tree.getClosestElement(x, y);

      log.trace("{} and {} should be the same...", winnerOne, winnerTwo);

      if (!winnerOne.equals(winnerTwo)) {
        log.warn(
            "the radius distanceSq from winnerOne {} at {} to {},{} is {}",
            winnerOne,
            layoutModel.apply(winnerOne),
            x,
            y,
            layoutModel.apply(winnerOne).distanceSquared(x, y));
        log.warn(
            "the radius distanceSq from winnerTwo {} at {} to {},{} is {}",
            winnerTwo,
            layoutModel.apply(winnerTwo),
            x,
            y,
            layoutModel.apply(winnerTwo).distanceSquared(x, y));

        log.warn("the cell for winnerOne {} is {}", winnerOne, tree.getContainingLeaf(winnerOne));
        log.warn("the cell for winnerTwo {} is {}", winnerTwo, tree.getContainingLeaf(winnerTwo));
        log.warn("the cell for the search point {},{} is {}", x, y, tree.getContainingLeafs(x, y));
      }
      Assert.assertEquals(winnerOne, winnerTwo);
    }
  }

  /**
   * a simple performance measure to compare using the RadiusVertexAccessor and the SpatialQuadTree.
   * Not really a test, it just outputs elapsed time
   */
  @Test
  public void comparePerformance() {
    final int COUNT = 1000;
    VertexAccessor<String> slowWay = new RadiusVertexAccessor<>(Double.MAX_VALUE);

    // generate the points first so both tests use the same points
    double[] xs = new double[COUNT];
    double[] ys = new double[COUNT];
    for (int i = 0; i < COUNT; i++) {
      xs[i] = Math.random() * layoutModel.getWidth();
      ys[i] = Math.random() * layoutModel.getHeight();
    }
    long start = System.currentTimeMillis();
    // look for nodes closest to 10000 random locations
    for (int i = 0; i < COUNT; i++) {
      // use the RadiusVertexAccessor
      String winnerOne = slowWay.getVertex(layoutModel, xs[i], ys[i]);
    }
    long end = System.currentTimeMillis();
    log.info("radius way took {}", end - start);
    start = System.currentTimeMillis();
    for (int i = 0; i < COUNT; i++) {
      // use the rtree
      String winnerTwo = tree.getClosestElement(xs[i], ys[i]);
    }
    end = System.currentTimeMillis();
    log.info("spatial way took {}", end - start);
  }
}
