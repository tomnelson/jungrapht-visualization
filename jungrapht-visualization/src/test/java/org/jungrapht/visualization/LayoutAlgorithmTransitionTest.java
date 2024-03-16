package org.jungrapht.visualization;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.junit.Test;

public class LayoutAlgorithmTransitionTest {

  @Test
  public void testTransition() {
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.simple()).buildGraph();
    graph.addVertex("A");
    LayoutModel<String> model = LayoutModel.<String>builder().graph(graph).size(100, 100).build();

    model.set("A", 0, 0);
    LayoutAlgorithm newLayoutAlgorithm = new StaticLayoutAlgorithm();
  }
}
