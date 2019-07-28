package org.jungrapht.visualization;

import java.awt.*;
import junit.framework.TestCase;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.selection.MutableSelectedState;

public class BasicVisualizationServerTest extends TestCase {

  /*
   * Previously, a bug was introduced where the RenderContext in BasicVisualizationServer was reassigned, resulting
   * in data like pickedVertexState to be lost.
   */
  public void testRenderContextNotOverridden() {
    Graph<Object, Object> graph = DirectedPseudograph.createBuilder(Object::new).build();
    CircleLayoutAlgorithm algorithm = CircleLayoutAlgorithm.builder().build();

    BasicVisualizationServer server =
        new BasicVisualizationServer<Object, Object>(graph, algorithm, new Dimension(600, 600));

    MutableSelectedState<Object> pickedVertexState =
        server.getRenderContext().getSelectedVertexState();
    assertNotNull(pickedVertexState);
  }
}
