package org.jungrapht.visualization.layout.spatial;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFRRepulsion;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.util.RandomLocationTransformer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Measures the time required to do the same work with each of 3 versions of the FRLayoutAlgorithm:
 *
 * <ul>
 *   <li>FRLayoutAlgorithm - the JUNG legacy version
 *   <li>FRBHLayoutAlgorithm - modified to use a BarnesHutOctTree to reduce the number of repulsion
 *       comparisons with a custom Iterator
 *   <li>FRBHVisitorLayoutAlgorithm - modified to use the BarnesHutOctTree as a visitor during the
 *       repulsion step
 * </ul>
 *
 * @author Tom Nelson
 */
public class FRLayoutsTimingTest {

  private static final Logger log = LoggerFactory.getLogger(FRLayoutsTimingTest.class);
  Graph<String, Integer> graph;
  LayoutModel<String> layoutModel;

  /**
   * this runs again before each test. Build a simple graph, build a custom layout model (see below)
   * initialize the locations to be the same each time.
   */
  @Before
  public void setup() {
    graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.multigraph()).buildGraph();
    int edge = 0;
    IntStream.rangeClosed(0, 100).forEach(i -> graph.addVertex("N" + i));
    for (int i = 0; i < 100; i++) {
      for (int j = i + 1; j < 100; j++) {
        graph.addEdge("N" + i, "N" + j, edge++);
      }
    }
    layoutModel = LayoutModel.<String>builder().graph(graph).size(500, 500).build();
    layoutModel.setInitializer(new RandomLocationTransformer<>(500, 500));
  }

  @Test
  public void testFRLayouts() {
    FRLayoutAlgorithm layoutAlgorithmOne = new FRLayoutAlgorithm<>();
    doTest(layoutAlgorithmOne);
    doTest(layoutAlgorithmOne);
    doTest(layoutAlgorithmOne);
  }

  @Test
  public void testFRBHVisitor() {
    FRLayoutAlgorithm layoutAlgorithmThree =
        FRLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutFRRepulsion.builder()) //.randomSeed(0))
            .build();
    doTest(layoutAlgorithmThree);
    doTest(layoutAlgorithmThree);
    doTest(layoutAlgorithmThree);
  }

  private void doTest(FRLayoutAlgorithm<String> layoutAlgorithm) {
    long startTime = System.currentTimeMillis();
    layoutModel.accept(layoutAlgorithm);
    if (layoutModel.getTheFuture() instanceof CompletableFuture) {
      ((CompletableFuture) layoutModel.getTheFuture())
          .thenRun(
              () ->
                  log.info(
                      "elapsed time for {} was {}",
                      layoutAlgorithm,
                      System.currentTimeMillis() - startTime))
          .join();
    }
  }
}
