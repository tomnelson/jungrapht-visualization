package org.jungrapht.samples.sugiyama;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.samples.util.TitlePaintable;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.renderers.Renderer;

/**
 * Demo that uses the EiglspergerLayoutAlgorithm to display a directed graph that has several
 * components (not connected to each other by any edges).
 */
public class SugiyamaMulticomponent extends JFrame {

  public SugiyamaMulticomponent() {

    JPanel container = new JPanel(new GridLayout(0, 2));

    Graph<String, Integer> graph = TestGraphs.getDemoGraph(true);

    VisualizationViewer<String, Integer> vv1 = configureVisualizationViewer(graph);
    SugiyamaLayoutAlgorithm<String, Integer> layoutAlgorithm1 =
        SugiyamaLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .postStraighten(true)
            .threaded(false)
            .layering(Layering.COFFMAN_GRAHAM)
            .after(vv1::scaleToLayout)
            .build();
    layoutAlgorithm1.setVertexShapeFunction(vv1.getRenderContext().getVertexShapeFunction());
    layoutAlgorithm1.setEdgeShapeFunctionConsumer(vv1.getRenderContext()::setEdgeShapeFunction);
    vv1.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm1);
    container.add(vv1.getComponent());

    VisualizationViewer<String, Integer> vv2 = configureVisualizationViewer(graph);
    SugiyamaLayoutAlgorithm<String, Integer> layoutAlgorithm2 =
        SugiyamaLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .postStraighten(true)
            .threaded(false)
            .layering(Layering.COFFMAN_GRAHAM)
            .after(vv2::scaleToLayout)
            .separateComponents(false)
            .build();
    layoutAlgorithm2.setVertexShapeFunction(vv2.getRenderContext().getVertexShapeFunction());
    layoutAlgorithm2.setEdgeShapeFunctionConsumer(vv2.getRenderContext()::setEdgeShapeFunction);
    vv2.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm2);
    container.add(vv2.getComponent());

    vv2.setSelectedVertexState(vv1.getSelectedVertexState());

    vv1.addPreRenderPaintable(new TitlePaintable("Separated Components", vv1.getPreferredSize()));
    vv2.addPreRenderPaintable(
        new TitlePaintable("Not Separated Components", vv2.getPreferredSize()));

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
    new SugiyamaMulticomponent();
  }
}
