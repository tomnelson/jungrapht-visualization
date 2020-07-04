package org.jungrapht.samples.sugiyama;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.renderers.Renderer;

/**
 * Demo that uses the EiglspergerLayoutAlgorithm to display a directed graph that has several
 * components (not connected to each other by any edges).
 */
public class EiglspergerMulticomponent extends JFrame {

  public EiglspergerMulticomponent() {

    JPanel container = new JPanel(new BorderLayout());

    Graph<String, Integer> graph = TestGraphs.getDemoGraph(true);

    VisualizationViewer<String, Integer> vv3 = configureVisualizationViewer(graph);

    vv3.getRenderContext().setEdgeLabelFunction(Object::toString);

    EiglspergerLayoutAlgorithm<String, Integer> layoutAlgorithm3 =
        EiglspergerLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .postStraighten(true)
            .threaded(false)
            .layering(Layering.COFFMAN_GRAHAM)
            .after(vv3::scaleToLayout)
            .build();
    layoutAlgorithm3.setVertexShapeFunction(vv3.getRenderContext().getVertexShapeFunction());
    layoutAlgorithm3.setEdgeShapeFunctionConsumer(vv3.getRenderContext()::setEdgeShapeFunction);
    vv3.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm3);
    container.add(vv3.getComponent());

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    add(container);
    pack();
    setVisible(true);
  }

  private VisualizationViewer<String, Integer> configureVisualizationViewer(
      Graph<String, Integer> graph) {
    VisualizationViewer<String, Integer> vv =
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

  public static void main(String[] args) {
    new EiglspergerMulticomponent();
  }
}
