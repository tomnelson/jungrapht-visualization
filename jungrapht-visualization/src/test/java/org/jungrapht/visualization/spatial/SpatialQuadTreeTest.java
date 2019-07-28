package org.jungrapht.visualization.spatial;

import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.graph.Pseudograph;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.LoadingCacheLayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.RadiusVertexAccessor;
import org.jungrapht.visualization.layout.util.RandomLocationTransformer;
import org.jungrapht.visualization.layout.util.VertexAccessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * test to make sure that a search for a vertex returns the same leaf that you get when you search
 * for the point location of that vertex
 *
 * @author Tom Nelson
 */
public class SpatialQuadTreeTest {

  private static final Logger log = LoggerFactory.getLogger(SpatialQuadTreeTest.class);

  int width = 600;
  int height = 600;
  Graph<String, Object> graph;
  LayoutModel<String> layoutModel;
  SpatialQuadTree<String> tree;

  @Before
  public void setup() {
    // generate 100 random vertices in a graph at random locations in the layoutModel
    Pseudograph<String, Object> network =
        Pseudograph.<String, Object>createBuilder(Object::new).build();

    IntStream.range(0, 100).mapToObj(i -> "N" + i).forEach(network::addVertex);
    this.graph = network;
    layoutModel =
        LoadingCacheLayoutModel.<String>builder()
            .graph(graph)
            .size(width, height)
            .initializer(new RandomLocationTransformer(width, height, System.currentTimeMillis()))
            .build();

    tree = new SpatialQuadTree(layoutModel, width, height);
    for (String vertex : graph.vertexSet()) {
      tree.insert(vertex);
    }
  }

  /**
   * confirm that the quadtree cell for a vertex is the same as the quadtree cell for the vertex's
   * location
   */
  @Test
  public void testRandomPointsAndLocations() {
    for (String vertex : graph.vertexSet()) {
      Point location = layoutModel.apply(vertex);
      SpatialQuadTree pointQuadTree = tree.getContainingQuadTreeLeaf(location.x, location.y);
      SpatialQuadTree vertexQuadTree = (SpatialQuadTree) tree.getContainingQuadTreeLeaf(vertex);
      Assert.assertEquals(pointQuadTree, vertexQuadTree);
      log.debug(
          "pointQuadTree level {} vertexQuadTree level {}",
          pointQuadTree.getLevel(),
          vertexQuadTree.getLevel());
    }
  }

  /**
   * test that the closest vertex for a random point is the same one returned for the
   * RadiusVertexAccessor and for the SpatialQuadTree Test with 1000 randomly generated points
   */
  @Test
  public void testClosestVertices() {
    final int COUNT = 10000;
    VertexAccessor<String> slowWay = new RadiusVertexAccessor<>(Double.MAX_VALUE);

    // look for vertices closest to COUNT random locations
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

        log.warn(
            "the cell for winnerOne {} is {}",
            winnerOne,
            tree.getContainingQuadTreeLeaf(winnerOne));
        log.warn(
            "the cell for winnerTwo {} is {}",
            winnerTwo,
            tree.getContainingQuadTreeLeaf(winnerTwo));
        log.warn(
            "the cell for the search point {},{} is {}",
            x,
            y,
            tree.getContainingQuadTreeLeaf(x, y));
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
    final int COUNT = 100000;
    VertexAccessor<String> slowWay = new RadiusVertexAccessor<>(Double.MAX_VALUE);

    // generate the points first so both tests use the same points
    double[] xs = new double[COUNT];
    double[] ys = new double[COUNT];
    for (int i = 0; i < COUNT; i++) {
      xs[i] = Math.random() * layoutModel.getWidth();
      ys[i] = Math.random() * layoutModel.getHeight();
    }
    long start = System.currentTimeMillis();
    // look for vertices closest to 10000 random locations
    for (int i = 0; i < COUNT; i++) {
      // use the RadiusVertexAccessor
      String winnerOne = slowWay.getVertex(layoutModel, xs[i], ys[i]);
    }
    long end = System.currentTimeMillis();
    log.info("radius way took {}", end - start);
    start = System.currentTimeMillis();
    for (int i = 0; i < COUNT; i++) {
      // use the quadtree
      String winnerTwo = tree.getClosestElement(xs[i], ys[i]);
    }
    end = System.currentTimeMillis();
    log.info("spatial way took {}", end - start);
  }
}
