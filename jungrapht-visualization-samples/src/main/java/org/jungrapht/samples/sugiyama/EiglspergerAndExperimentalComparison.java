package org.jungrapht.samples.sugiyama;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.eiglsperger.experimental.ExpEiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.renderers.Renderer;

/** The two graphs should look the same! */
public class EiglspergerAndExperimentalComparison extends JFrame {

  public EiglspergerAndExperimentalComparison() {

    JPanel container = new JPanel(new GridLayout(0, 2));

    Graph<String, Integer> graph = TestGraphs.getDemoGraph(true);

    VisualizationViewer<String, Integer> vv1 = configureVisualizationViewer(graph);
    EiglspergerLayoutAlgorithm<String, Integer> layoutAlgorithm1 =
        EiglspergerLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .postStraighten(true)
            .threaded(false)
            .layering(Layering.COFFMAN_GRAHAM)
            .separateComponents(false)
            .build();
    layoutAlgorithm1.setVertexBoundsFunction(vv1.getRenderContext().getVertexBoundsFunction());
    vv1.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm1);
    container.add(vv1.getComponent());

    VisualizationViewer<String, Integer> vv2 = configureVisualizationViewer(graph);
    ExpEiglspergerLayoutAlgorithm<String, Integer> layoutAlgorithm2 =
        ExpEiglspergerLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .postStraighten(true)
            .threaded(false)
            .layering(Layering.COFFMAN_GRAHAM)
            .separateComponents(false)
            .build();
    layoutAlgorithm2.setVertexBoundsFunction(vv2.getRenderContext().getVertexBoundsFunction());
    vv2.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm2);
    container.add(vv2.getComponent());

    vv2.setSelectedVertexState(vv1.getSelectedVertexState());

    //    vv1.addPreRenderPaintable(new TitlePaintable("Separated Components", vv1.getPreferredSize()));
    //    vv2.addPreRenderPaintable(
    //        new TitlePaintable("Not Separated Components", vv2.getPreferredSize()));

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
    new EiglspergerAndExperimentalComparison();
  }
}
