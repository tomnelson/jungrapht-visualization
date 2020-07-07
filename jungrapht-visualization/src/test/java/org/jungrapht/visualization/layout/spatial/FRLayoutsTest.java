package org.jungrapht.visualization.layout.spatial;

import java.util.HashMap;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.IterativeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardFRRepulsion;
import org.jungrapht.visualization.layout.model.DefaultLayoutModel;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This test makes a very small graph, sets initial locations for each vertex, and each time, it
 * runs a version of FRLayout.
 *
 * <ul>
 *   <li>FRLayoutAlgorithm - the JUNG legacy version
 *   <li>FRBHLayoutAlgorithm - modified to use a BarnesHutOctTree to reduce the number of repulsion
 *       comparisons with a custom Iterator
 *   <li>FRBHVisitorLayoutAlgorithm - modified to use the BarnesHutOctTree as a visitor during the
 *       repulsion step.
 * </ul>
 *
 * <p>The LayoutModel is subclassed so that no relax thread is started. A total of 200 steps of the
 * layout relax is run. After all tests are run, the end values for both BarnesHut versions are
 * compared. The end values should be very close. The standard FRLayoutAlgorithm will vary because
 * force comparisons are approximated in the BarnesHut versions
 *
 * <p>The Iterator version of BarnesHut uses storage space to cache collections of 'vertices' (or
 * force vectors) to compare with. The Visitor version does not use that additional storage space,
 * so it should be better.
 *
 * @author Tom Nelson
 */
public class FRLayoutsTest {

  private static final Logger log = LoggerFactory.getLogger(FRLayoutsTest.class);
  Graph<String, Integer> graph;
  LayoutModel<String> layoutModel;
  static Map<String, Point> mapOne = new HashMap<>();
  static Map<String, Point> mapThree = new HashMap<>();
  static Map<String, Point> mapFour = new HashMap<>();

  /**
   * this runs again before each test. Build a simple graph, build a custom layout model (see below)
   * initialize the locations to be the same each time.
   */
  @Before
  public void setup() {
    int i = 0;
    graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedMultigraph())
            .buildGraph();

    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "B", i);
    graph.addEdge("B", "C", i);
    graph.addEdge("C", "A", i);
    graph.addEdge("D", "C", i);

    layoutModel =
        new TestLayoutModel<>(LayoutModel.<String>builder().graph(graph).size(500, 500), 200);
    layoutModel.set("A", Point.of(200, 100));
    layoutModel.set("B", Point.of(100, 200));
    layoutModel.set("C", Point.of(100, 100));
    layoutModel.set("D", Point.of(500, 100));
    for (String vertex : graph.vertexSet()) {
      log.debug("vertex {} starts at {}", vertex, layoutModel.apply(vertex));
    }
  }

  @Test
  public void testFRLayouts() {
    FRLayoutAlgorithm layoutAlgorithmOne =
        FRLayoutAlgorithm.builder()
            .repulsionContractBuilder(StandardFRRepulsion.builder())
            .randomSeed(0)
            .build();

    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmOne.setRandomSeed(0);
    doTest(layoutAlgorithmOne, mapOne);
  }

  @Test
  public void testFRBHVisitor() {
    FRLayoutAlgorithm layoutAlgorithmThree =
        FRLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutFRRepulsion.builder())
            .randomSeed(0)
            .build();
    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmThree.setRandomSeed(0);
    doTest(layoutAlgorithmThree, mapThree);
  }

  /**
   * a BarnesHutOctTree with THETA = 0 will not use any inner vertex estimations in the force
   * calculations. The values should be the same as would be produced by the same layout algorithm
   * with no BarnesHut optimization.
   */
  @Test
  public void testFRBHWithThetaZero() {
    FRLayoutAlgorithm layoutAlgorithmFour =
        FRLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutFRRepulsion.builder().theta(0))
            .randomSeed(0)
            .build();
    // using the same random seed each time for repeatable results from each test.
    doTest(layoutAlgorithmFour, mapFour);
  }

  /**
   * check to see if mapTwo and mapThree (the ones that used the BarnesHut optimization) returned
   * similar results
   */
  @AfterClass
  public static void check() {
    log.debug("mapOne:{}", mapOne);
    log.debug("mapThree:{}", mapThree);
    log.debug("mapFour:{}", mapFour);

    Assert.assertEquals(mapOne.keySet(), mapFour.keySet());
    for (String key : mapOne.keySet()) {
      Point p2 = mapOne.get(key);
      Point p3 = mapFour.get(key);
      log.info("p2.x:{} p3.x:{}", p2.x, p3.x);
      log.info("p2.y:{} p3.y:{}", p2.y, p3.y);
      Assert.assertTrue(Math.abs(p2.x - p3.x) < 1.0E-3);
      Assert.assertTrue(Math.abs(p2.y - p3.y) < 1.0E-3);
    }
  }

  private void doTest(LayoutAlgorithm<String> layoutAlgorithm, Map<String, Point> map) {
    log.debug("for {}", layoutAlgorithm.getClass());
    layoutModel.accept(layoutAlgorithm);
    for (String vertex : graph.vertexSet()) {
      map.put(vertex, layoutModel.apply(vertex));
      log.debug("vertex {} placed at {}", vertex, layoutModel.apply(vertex));
    }
  }

  /**
   * a LoadingCacheLayoutModel that will not start a relax thread, but will 'step' the layout the
   * number of times requested in a passed parameter
   *
   * @param <T>
   */
  private static class TestLayoutModel<T> extends DefaultLayoutModel<T> {

    // how many steps
    private int steps;

    public TestLayoutModel(LayoutModel.Builder<T, ?, ?> builder, int steps) {
      super(builder);
      this.steps = steps;
    }

    @Override
    public void accept(LayoutAlgorithm<T> layoutAlgorithm) {
      layoutAlgorithm.visit(this);
      if (layoutAlgorithm instanceof IterativeLayoutAlgorithm) {
        IterativeLayoutAlgorithm iterativeLayoutAlgorithm =
            (IterativeLayoutAlgorithm) layoutAlgorithm;
        for (int i = 0; i < steps; i++) {
          iterativeLayoutAlgorithm.step();
        }
      }
    }
  }
}
