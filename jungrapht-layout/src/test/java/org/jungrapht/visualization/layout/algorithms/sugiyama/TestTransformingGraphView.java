package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.util.synthetics.SingletonTransformer;
import org.jungrapht.visualization.layout.util.synthetics.TransformingGraphView;
import org.junit.Assert;
import org.junit.Test;

public class TestTransformingGraphView {

  @Test
  public void testRoundTrip() {
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    graph.addVertex("a");
    graph.addVertex("b");
    graph.addVertex("c");

    graph.addEdge("a", "b");
    graph.addEdge("b", "c");
    graph.addEdge("a", "c");

    TransformedGraphSupplier<String, Integer> supplier = new TransformedGraphSupplier<>(graph);

    Graph<LV<String>, LE<String, Integer>> transformedGraph = supplier.get();

    // transform it back

    TransformingGraphView.Builder<LV<String>, String, LE<String, Integer>, Integer, ?, ?>
        transformingGraphViewBuilder =
            TransformingGraphView.<LV<String>, String, LE<String, Integer>, Integer>builder(
                    transformedGraph)
                .vertexTransformFunction(new SingletonTransformer<>(v -> v.getVertex()))
                .edgeTransformFunction(e -> e.getEdge());
    TransformingGraphView<LV<String>, String, LE<String, Integer>, Integer> transformingGraphView =
        transformingGraphViewBuilder.build();

    Graph<String, Integer> transformedBackGraph = transformingGraphView.build();

    Assert.assertTrue(graph.vertexSet().containsAll(transformedBackGraph.vertexSet()));
    Assert.assertTrue(graph.edgeSet().containsAll(transformedBackGraph.edgeSet()));

    Assert.assertEquals(transformedBackGraph.getEdge("a", "b"), graph.getEdge("a", "b"));
  }
}
