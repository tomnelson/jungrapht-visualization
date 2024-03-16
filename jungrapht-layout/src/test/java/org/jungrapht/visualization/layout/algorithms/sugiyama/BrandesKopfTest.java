package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.HashSet;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrandesKopfTest {

  private static final Logger log = LoggerFactory.getLogger(BrandesKopfTest.class);

  Graph<Integer, Integer> graph;
  Graph<LV<Integer>, LE<Integer, Integer>> svGraph;

  private void createInitialGraph() {
    graph =
        GraphTypeBuilder.<Integer, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .vertexSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    IntStream.rangeClosed(1, 23).forEach(graph::addVertex);
    graph.addEdge(1, 3);
    graph.addEdge(1, 4);
    graph.addEdge(1, 13);
    graph.addEdge(1, 21);

    graph.addEdge(2, 3);
    graph.addEdge(2, 20);

    graph.addEdge(3, 4);
    graph.addEdge(3, 5);
    graph.addEdge(3, 23);

    graph.addEdge(4, 6);

    graph.addEdge(5, 7);

    graph.addEdge(6, 8);
    graph.addEdge(6, 16);
    graph.addEdge(6, 23);

    graph.addEdge(7, 9);

    graph.addEdge(8, 10);
    graph.addEdge(8, 11);

    graph.addEdge(9, 12);

    graph.addEdge(10, 13);
    graph.addEdge(10, 14);
    graph.addEdge(10, 15);

    graph.addEdge(11, 15);
    graph.addEdge(11, 16);

    graph.addEdge(12, 20);

    graph.addEdge(13, 17);

    graph.addEdge(14, 17);
    graph.addEdge(14, 18);
    // no 15 targets

    graph.addEdge(16, 18);
    graph.addEdge(16, 19);
    graph.addEdge(16, 20);

    graph.addEdge(18, 21);

    graph.addEdge(19, 22);

    graph.addEdge(21, 23);

    graph.addEdge(22, 23);
  }

  void createDelegateGraph() {
    svGraph = new TransformedGraphSupplier<>(graph).get();
  }

  @Before
  public void setup() {
    createInitialGraph();
  }

  @Test
  public void makeBrandesKopf() {
    BrandesKopf brandesKopf = new BrandesKopf(graph);
    svGraph = brandesKopf.svGraph;
    LV<Integer>[][] layers = brandesKopf.layersArray;
    Assert.assertEquals(10, layers.length);
    Assert.assertEquals(2, layers[0].length);
    Assert.assertEquals(5, layers[1].length);
    Assert.assertEquals(6, layers[2].length);
    Assert.assertEquals(6, layers[3].length);
    Assert.assertEquals(8, layers[4].length);
    Assert.assertEquals(9, layers[5].length);
    Assert.assertEquals(9, layers[6].length);
    Assert.assertEquals(7, layers[7].length);
    Assert.assertEquals(4, layers[8].length);
    Assert.assertEquals(1, layers[9].length);

    HorizontalCoordinateAssignment<Integer, Integer> horizontalCoordinateAssignment =
        new HorizontalCoordinateAssignment<>(layers, svGraph, new HashSet<>(), 20, 20);
    horizontalCoordinateAssignment.horizontalCoordinateAssignment();

    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        LV<Integer> v = layers[i][j];
        log.info("{} - {}", v.getClass(), v.getPoint());
      }
    }
  }
}
