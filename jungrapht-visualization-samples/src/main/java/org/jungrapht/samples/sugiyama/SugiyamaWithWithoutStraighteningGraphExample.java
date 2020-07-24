package org.jungrapht.samples.sugiyama;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.util.RectangleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SugiyamaWithWithoutStraighteningGraphExample extends JFrame {

  private static final Logger log =
      LoggerFactory.getLogger(SugiyamaWithWithoutStraighteningGraphExample.class);

  public SugiyamaWithWithoutStraighteningGraphExample() {

    JPanel container = new JPanel(new GridLayout(2, 2));

    Graph<String, Integer> graph = TestGraphs.createDirectedAcyclicGraph(9, 3, .3); //, 123);

    VisualizationViewer<String, Integer> vv1 = configureVisualizationViewer(graph);
    vv1.addPreRenderPaintable(new TitlePaintable("No Edge Straightening", vv1.getPreferredSize()));
    VisualizationViewer<String, Integer> vv2 = configureVisualizationViewer(graph);
    vv2.addPreRenderPaintable(
        new TitlePaintable("BrandesKopf Edge Alignment", vv1.getPreferredSize()));
    VisualizationViewer<String, Integer> vv3 = configureVisualizationViewer(graph);
    vv3.addPreRenderPaintable(
        new TitlePaintable("Brandes Kopf plus post-processing", vv1.getPreferredSize()));

    SugiyamaLayoutAlgorithm<String, Integer> layoutAlgorithm1 =
        SugiyamaLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .straightenEdges(false)
            .postStraighten(false)
            .build();
    layoutAlgorithm1.setVertexShapeFunction(vv1.getRenderContext().getVertexShapeFunction().andThen(s -> RectangleUtils.convert(s.getBounds2D())));
    vv1.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm1);
    container.add(vv1.getComponent());

    SugiyamaLayoutAlgorithm<String, Integer> layoutAlgorithm2 =
        SugiyamaLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .straightenEdges(true)
            .postStraighten(false)
            .build();
    layoutAlgorithm2.setVertexShapeFunction(vv2.getRenderContext().getVertexShapeFunction().andThen(s -> RectangleUtils.convert(s.getBounds2D())));
    vv2.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm2);
    container.add(vv2.getComponent());

    SugiyamaLayoutAlgorithm<String, Integer> layoutAlgorithm3 =
        SugiyamaLayoutAlgorithm.<String, Integer>edgeAwareBuilder().straightenEdges(true).build();
    layoutAlgorithm3.setVertexShapeFunction(vv3.getRenderContext().getVertexShapeFunction().andThen(s -> RectangleUtils.convert(s.getBounds2D())));
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

  static class TitlePaintable implements VisualizationViewer.Paintable {
    int x;
    int y;
    Font font;
    FontMetrics metrics;
    int swidth;
    int sheight;
    String str;
    Dimension overallSize;

    TitlePaintable(String title, Dimension overallSize) {
      this.str = title;
      this.overallSize = overallSize;
    }

    public void paint(Graphics g) {
      Dimension d = overallSize;
      if (font == null) {
        font = new Font(g.getFont().getName(), Font.BOLD, 30);
        metrics = g.getFontMetrics(font);
        swidth = metrics.stringWidth(str);
        sheight = metrics.getMaxAscent() + metrics.getMaxDescent();
        x = (d.width - swidth) / 2;
        y = (int) (d.height - sheight * 1.5);
      }
      g.setFont(font);
      Color oldColor = g.getColor();
      g.setColor(Color.lightGray);
      g.drawString(str, x, y);
      g.setColor(oldColor);
    }

    public boolean useTransform() {
      return false;
    }
  }

  public static void main(String[] args) {
    new SugiyamaWithWithoutStraighteningGraphExample();
  }
}
