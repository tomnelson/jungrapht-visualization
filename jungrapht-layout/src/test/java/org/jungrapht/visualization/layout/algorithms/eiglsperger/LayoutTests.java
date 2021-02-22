package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LayoutTests {

  private static final Logger log = LoggerFactory.getLogger(LayoutTests.class);

  private Graph<Integer, Integer> getFiveVertexGraph() {

    Graph<Integer, Integer> graph =
        GraphTypeBuilder.<Integer, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .vertexSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    IntStream.rangeClosed(0, 4).forEach(graph::addVertex);
    graph.addEdge(0, 1);
    graph.addEdge(1, 2);
    graph.addEdge(2, 3);
    graph.addEdge(3, 4);
    graph.addEdge(0, 4);
    graph.addEdge(2, 4);
    graph.addEdge(1, 3);
    graph.addEdge(1, 4);

    return graph;
  }

  private Graph<Integer, Integer> getTwoVertexGraph() {
    Graph<Integer, Integer> graph =
        GraphTypeBuilder.<Integer, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .vertexSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    IntStream.rangeClosed(0, 1).forEach(graph::addVertex);
    graph.addEdge(0, 1);
    return graph;
  }

  private Graph<Integer, Integer> getTwoComponentGraph() {
    Graph<Integer, Integer> graph =
        GraphTypeBuilder.<Integer, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .vertexSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    IntStream.rangeClosed(5, 6).forEach(graph::addVertex);
    graph.addEdge(5, 6);

    IntStream.rangeClosed(0, 4).forEach(graph::addVertex);
    graph.addEdge(0, 1);
    graph.addEdge(1, 2);
    graph.addEdge(2, 3);
    graph.addEdge(3, 4);
    graph.addEdge(0, 4);
    graph.addEdge(2, 4);
    graph.addEdge(1, 3);
    graph.addEdge(1, 4);

    return graph;
  }

  LayoutModel<Integer> layoutModel;
  LayoutAlgorithm<Integer> layoutAlgorithm;

  @Before
  public void setup() {

    layoutAlgorithm =
        EiglspergerLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .vertexBoundsFunction(v -> Rectangle.of(100, 100))
            //                .postStraighten(true)
            .threaded(false)
            //                .layering(Layering.TOP_DOWN)
            .build();
  }

  @Test
  public void testTwo() {
    layoutModel = LayoutModel.<Integer>builder().size(100, 100).graph(getTwoVertexGraph()).build();
    layoutModel.accept(layoutAlgorithm);

    log.info("layoutModel: {}", layoutModel);
  }

  @Test
  public void testFive() {
    layoutModel = LayoutModel.<Integer>builder().size(100, 100).graph(getFiveVertexGraph()).build();
    layoutModel.accept(layoutAlgorithm);

    log.info("layoutModel: {}", layoutModel);
  }
}
