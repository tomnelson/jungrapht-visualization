package org.jungrapht.visualization;

import static org.junit.Assert.assertNotNull;

import java.awt.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.junit.Test;

public class BasicVisualizationServerTest {

  /*
   * Previously, a bug was introduced where the RenderContext in DefaultVisualizationServer was reassigned, resulting
   * in data like pickedVertexState to be lost.
   */
  @Test
  public void testRenderContextNotOverridden() {
    Graph<Object, Object> graph = DirectedPseudograph.createBuilder(Object::new).build();
    CircleLayoutAlgorithm algorithm = CircleLayoutAlgorithm.builder().build();

    VisualizationServer server =
        VisualizationServer.builder(graph)
            .layoutAlgorithm(algorithm)
            .viewSize(new Dimension(600, 600))
            .build();

    MutableSelectedState<Object> pickedVertexState =
        server.getRenderContext().getSelectedVertexState();
    assertNotNull(pickedVertexState);
  }
}
