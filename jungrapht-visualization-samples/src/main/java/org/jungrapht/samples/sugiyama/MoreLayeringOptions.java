package org.jungrapht.samples.sugiyama;

import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LayeringConfiguration;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.stream.IntStream;

/**
 * Demonstrates 4 layering algorithms with the SugiyamaLayoutAlgoithm
 *
 * @author Tom Nelson
 */
public class MoreLayeringOptions extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(MoreLayeringOptions.class);

  public MoreLayeringOptions() {

    JPanel container = new JPanel(new BorderLayout());

    Graph<String, Integer> graph = createInitialGraph();

    VisualizationViewer<String, Integer> vv3 = configureVisualizationViewer(graph);

    vv3.getRenderContext().setEdgeLabelFunction(Object::toString);

    LayeringConfiguration layeringConfiguration = new LayeringConfiguration();

    layeringConfiguration.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            SugiyamaLayoutAlgorithm<String, Integer> layoutAlgorithm =
                    SugiyamaLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
                    .postStraighten(true)
                    .threaded(false)
                    .layering((Layering) e.getItem())
                    .build();
            layoutAlgorithm.setVertexBoundsFunction(
                vv3.getRenderContext().getVertexBoundsFunction());
            vv3.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm);
          }
        });

    SugiyamaLayoutAlgorithm<String, Integer> layoutAlgorithm3 =
            SugiyamaLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .postStraighten(true)
            .threaded(false)
            .layering(layeringConfiguration.getLayeringPreference())
            .build();
    layoutAlgorithm3.setVertexBoundsFunction(vv3.getRenderContext().getVertexBoundsFunction());
    vv3.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm3);
    container.add(vv3.getComponent());

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    add(
        ControlHelpers.getCenteredContainer("Layering Style", layeringConfiguration),
        BorderLayout.SOUTH);

    add(container);
    pack();
    setVisible(true);
  }

  private <V, E> VisualizationViewer<V, E> configureVisualizationViewer(
      Graph<V, E> graph) {
    VisualizationViewer<V, E> vv =
        VisualizationViewer.builder(graph)
            .layoutSize(new Dimension(600, 600))
            .viewSize(new Dimension(700, 500))
            .build();

    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);

    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelDrawPaintFunction(c -> Color.white);
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    return vv;
  }

  /**
   * creates a graph to look like the one in the paper
   *
   * @return
   */
  Graph<String, Integer> createInitialGraph() {

    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addVertex("E");
    graph.addVertex("F");
    graph.addVertex("G");
    graph.addVertex("H");

    graph.addEdge("A", "B");
    graph.addEdge("A", "E");
    graph.addEdge("A", "F");
    graph.addEdge("B", "C");
    graph.addEdge("E", "G");
    graph.addEdge("F", "G");

    graph.addEdge("C", "D");
    graph.addEdge("D", "H");
    graph.addEdge("G", "H");

    return graph;
  }

  public static void main(String[] args) {
    new MoreLayeringOptions();
  }
}
