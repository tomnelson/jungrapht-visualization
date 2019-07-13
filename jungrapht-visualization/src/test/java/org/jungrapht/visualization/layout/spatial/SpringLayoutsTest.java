package org.jungrapht.visualization.layout.spatial;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Maps;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.layout.algorithms.IterativeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutSpringRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardSpringRepulsion;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.LoadingCacheLayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.jungrapht.visualization.layout.algorithms.SpringBHVisitorLayoutAlgorithm;

/**
 * This test makes a very small graph, sets initial locations for each node, and each time, it runs
 * a version of FRLayout.
 *
 * <ul>
 *   <li>SpringLayoutAlgorithm - the JUNG legacy version
 *   <li>SpringBHLayoutAlgorithm - modified to use a BarnesHutOctTree to reduce the number of
 *       repulsion comparisons with a custom Iterator
 *   <li>SpringBHVisitorLayoutAlgorithm - modified to use the BarnesHutOctTree as a visitor during
 *       the repulsion step.
 * </ul>
 *
 * <p>The LayoutModel is subclassed so that no relax thread is started. A total of 200 steps of the
 * layout relax is run. After all tests are run, the end values for both BarnesHut versions are
 * compared. The end values should be very close. The standard FRLayoutAlgorithm will vary because
 * force comparisons are approximated in the BarnesHut versions
 *
 * <p>The Iterator version of BarnesHut uses storage space to cache collections of 'nodes' (or force
 * vectors) to compare with. The Visitor version does not use that additional storage space, so it
 * should be better.
 *
 * @author Tom Nelson
 */
public class SpringLayoutsTest {

  private static final Logger log = LoggerFactory.getLogger(SpringLayoutsTest.class);
  Graph<String, Integer> graph;
  LayoutModel<String> layoutModel;
  static Map<String, Point> mapOne = Maps.newHashMap();
  static Map<String, Point> mapThree = Maps.newHashMap();
  static Map<String, Point> mapFour = Maps.newHashMap();

  /**
   * this runs again before each test. Build a simple graph, build a custom layout model (see below)
   * initialize the locations to be the same each time.
   */
  @Before
  public void setup() {
    int i = 0;
    graph = GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag()).buildGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "B", i++);
    graph.addEdge("B", "C", i++);
    graph.addEdge("C", "A", i++);
    graph.addEdge("D", "C", i++);

    layoutModel =
        new TestLayoutModel<>(
            LoadingCacheLayoutModel.<String>builder().graph(graph).size(500, 500), 30);
    layoutModel.set("A", Point.of(200, 100));
    layoutModel.set("B", Point.of(100, 200));
    layoutModel.set("C", Point.of(100, 100));
    layoutModel.set("D", Point.of(500, 100));
    for (String node : graph.vertexSet()) {
      log.debug("node {} starts at {}", node, layoutModel.apply(node));
    }
  }

  @Test
  public void testSpringLayoutAlgorithm() {
    // using the same random seed each time for repeatable results from each test.
    SpringLayoutAlgorithm layoutAlgorithmOne =
        SpringLayoutAlgorithm.builder()
            .repulsionContractBuilder(StandardSpringRepulsion.standardBuilder())
            .randomSeed(0)
            .build();
    doTest(layoutAlgorithmOne, mapOne);
  }

  @Test
  public void testSpringBHVisitorLayoutAlgorithm() {
    // using the same random seed each time for repeatable results from each test.
    SpringLayoutAlgorithm layoutAlgorithmThree =
        SpringLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutSpringRepulsion.barnesHutBuilder())
            .randomSeed(0)
            .build();
    doTest(layoutAlgorithmThree, mapThree);
  }

  /**
   * a BarnesHutOctTree with THETA = 0 will not use any inner node estimations in the force
   * calculations. The values should be the same as would be produced by the same layout algorithm
   * with no BarnesHut optimization.
   */
  @Test
  public void testFRBHWithThetaZero() {
    // using the same random seed each time for repeatable results from each test.
    SpringLayoutAlgorithm layoutAlgorithmFour =
        SpringLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutSpringRepulsion.barnesHutBuilder().theta(0))
            .randomSeed(0)
            .build();
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

    assertThat(mapOne.keySet()).isEqualTo(mapFour.keySet());
    for (String key : mapOne.keySet()) {
      Point p2 = mapOne.get(key);
      Point p3 = mapFour.get(key);
      assertThat(p2.x).isWithin(1.0E-3).of(p3.x);
      assertThat(p2.y).isWithin(1.0E-3).of(p3.y);
    }
  }

  private void doTest(LayoutAlgorithm<String> layoutAlgorithm, Map<String, Point> map) {
    log.debug("for {}", layoutAlgorithm.getClass());
    layoutModel.accept(layoutAlgorithm);
    for (String node : graph.vertexSet()) {
      map.put(node, layoutModel.apply(node));
      log.debug("node {} placed at {}", node, layoutModel.apply(node));
    }
  }

  /**
   * a LoadingCacheLayoutModel that will not start a relax thread, but will 'step' the layout the
   * number of times requested in a passed parameter
   *
   * @param <T>
   */
  private static class TestLayoutModel<T> extends LoadingCacheLayoutModel<T> {

    // how many steps
    private int steps;

    public TestLayoutModel(Builder<T, ?, ?> builder, int steps) {
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
