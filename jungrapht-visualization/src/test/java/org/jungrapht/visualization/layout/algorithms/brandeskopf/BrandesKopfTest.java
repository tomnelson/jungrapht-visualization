package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.util.List;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaEdge;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaTransformedGraphSupplier;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaVertex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrandesKopfTest {

  private static final Logger log = LoggerFactory.getLogger(BrandesKopfTest.class);

  Graph<Integer, Integer> graph;
  Graph<SugiyamaVertex<Integer>, SugiyamaEdge<Integer, Integer>> svGraph;

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
    svGraph = new SugiyamaTransformedGraphSupplier<>(graph).get();
  }

  @Before
  public void setup() {
    createInitialGraph();
    //        createDelegateGraph();
  }

  @Test
  public void makeBrandesKopf() {
    BrandesKopf brandesKopf = new BrandesKopf(graph);
    List<List<SugiyamaVertex<Integer>>> layers = brandesKopf.layers;
    Assert.assertEquals(10, layers.size());
    Assert.assertEquals(2, layers.get(0).size());
    Assert.assertEquals(5, layers.get(1).size());
    Assert.assertEquals(6, layers.get(2).size());
    Assert.assertEquals(6, layers.get(3).size());
    Assert.assertEquals(8, layers.get(4).size());
    Assert.assertEquals(9, layers.get(5).size());
    Assert.assertEquals(9, layers.get(6).size());
    Assert.assertEquals(7, layers.get(7).size());
    Assert.assertEquals(4, layers.get(8).size());
    Assert.assertEquals(1, layers.get(9).size());

    brandesKopf.horizontalCoordinateAssignment();

    for (List<SugiyamaVertex<Integer>> list : layers) {
      for (SugiyamaVertex<Integer> v : list) {
        log.info("{} - {}", v.getClass(), v.getPoint());
      }
    }
    //    brandesKopf.preprocessing();
    //
    //    brandesKopf.verticalAlignmentUpperLeft();
    //    log.info("look around");
    //    brandesKopf.horizontalCompaction();
    //    log.info("look around");
    //    List<List<SugiyamaVertex<Integer>>> listList = brandesKopf.sortX();
    //    for (List<SugiyamaVertex<Integer>> list : listList) {
    //      log.info("{}", list);
    //    }
  }
}
