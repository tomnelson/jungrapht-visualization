package org.jungrapht.samples.sugiyama;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.stream.IntStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates the original sugiyama layout algorithm with and without edge straightening Also
 * shows the Eiglsperger optimized sugiyama layout algorithm with/without edge straightening.
 *
 * @author Tom Nelson
 */
public class SugiyamaEiglspergerWithWithoutHorizontalCompaction extends JFrame {

  private static final Logger log =
      LoggerFactory.getLogger(SugiyamaEiglspergerWithWithoutHorizontalCompaction.class);

  public SugiyamaEiglspergerWithWithoutHorizontalCompaction() {

    JPanel container = new JPanel(new GridLayout(2, 2));

    Graph<Integer, Integer> graph = createInitialGraph();

    VisualizationViewer<Integer, Integer> vv1 = configureVisualizationViewer(graph);
    vv1.addPreRenderPaintable(
        new TitlePaintable("Sugiyama No Edge Straightening", vv1.getPreferredSize()));
    vv1.getRenderContext().setEdgeLabelFunction(Object::toString);
    VisualizationViewer<Integer, Integer> vv2 = configureVisualizationViewer(graph);
    vv2.addPreRenderPaintable(
        new TitlePaintable("BrandesKopf Edge Alignment", vv1.getPreferredSize()));
    vv2.getRenderContext().setEdgeLabelFunction(Object::toString);
    VisualizationViewer<Integer, Integer> vv3 = configureVisualizationViewer(graph);
    vv3.addPreRenderPaintable(
        new TitlePaintable("Eiglsperger No Edge Straightening", vv1.getPreferredSize()));
    vv3.getRenderContext().setEdgeLabelFunction(Object::toString);
    VisualizationViewer<Integer, Integer> vv4 = configureVisualizationViewer(graph);
    vv4.addPreRenderPaintable(
        new TitlePaintable("Eiglsperger Edge Alignment", vv1.getPreferredSize()));
    vv4.getRenderContext().setEdgeLabelFunction(Object::toString);

    SugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm1 =
        SugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .straightenEdges(false)
            .postStraighten(false)
            .transpose(true)
            .after(vv1::scaleToLayout)
            .build();
    layoutAlgorithm1.setVertexShapeFunction(vv1.getRenderContext().getVertexShapeFunction());
    layoutAlgorithm1.setEdgeShapeFunctionConsumer(vv1.getRenderContext()::setEdgeShapeFunction);
    vv1.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm1);
    container.add(vv1.getComponent());

    SugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm2 =
        SugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .straightenEdges(true)
            .postStraighten(false)
            .transpose(true)
            .after(vv2::scaleToLayout)
            .build();
    layoutAlgorithm2.setVertexShapeFunction(vv2.getRenderContext().getVertexShapeFunction());
    layoutAlgorithm2.setEdgeShapeFunctionConsumer(vv2.getRenderContext()::setEdgeShapeFunction);
    vv2.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm2);
    container.add(vv2.getComponent());

    EiglspergerLayoutAlgorithm<Integer, Integer> layoutAlgorithm3 =
        EiglspergerLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .straightenEdges(false)
            .postStraighten(false)
            .after(vv3::scaleToLayout)
            .build();
    layoutAlgorithm3.setVertexShapeFunction(vv3.getRenderContext().getVertexShapeFunction());
    layoutAlgorithm3.setEdgeShapeFunctionConsumer(vv3.getRenderContext()::setEdgeShapeFunction);
    vv3.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm3);
    container.add(vv3.getComponent());

    EiglspergerLayoutAlgorithm<Integer, Integer> layoutAlgorithm4 =
        EiglspergerLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .straightenEdges(true)
            .postStraighten(false)
            .after(vv3::scaleToLayout)
            .build();
    layoutAlgorithm4.setVertexShapeFunction(vv4.getRenderContext().getVertexShapeFunction());
    layoutAlgorithm4.setEdgeShapeFunctionConsumer(vv4.getRenderContext()::setEdgeShapeFunction);
    vv4.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm4);
    container.add(vv4.getComponent());

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    add(container);
    pack();
    setVisible(true);
  }

  private VisualizationViewer<Integer, Integer> configureVisualizationViewer(
      Graph<Integer, Integer> graph) {
    VisualizationViewer<Integer, Integer> vv =
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

  /**
   * creates a graph to look like the one in the paper
   *
   * @return
   */
  Graph<Integer, Integer> createInitialGraph() {

    Graph<Integer, Integer> graph =
        GraphTypeBuilder.<Integer, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .vertexSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    IntStream.rangeClosed(1, 23).forEach(graph::addVertex);
    graph.addEdge(1, 3);
    graph.addEdge(1, 4);
    graph.addEdge(1, 13);
    graph.addEdge(1, 21);

    graph.addEdge(2, 3);
    graph.addEdge(2, 20);

    graph.addEdge(3, 4);
    graph.addEdge(3, 5);
    graph.addEdge(3, 23);

    graph.addEdge(4, 6);

    graph.addEdge(5, 7);

    graph.addEdge(6, 8);
    graph.addEdge(6, 16);
    graph.addEdge(6, 23);

    graph.addEdge(7, 9);

    graph.addEdge(8, 10);
    graph.addEdge(8, 11);

    graph.addEdge(9, 12);

    graph.addEdge(10, 13);
    graph.addEdge(10, 14);
    graph.addEdge(10, 15);

    graph.addEdge(11, 15);
    graph.addEdge(11, 16);

    graph.addEdge(12, 20);

    graph.addEdge(13, 17);

    graph.addEdge(14, 17);
    graph.addEdge(14, 18);
    // no 15 targets

    graph.addEdge(16, 18);
    graph.addEdge(16, 19);
    graph.addEdge(16, 20);

    graph.addEdge(18, 21);

    graph.addEdge(19, 22);

    graph.addEdge(21, 23);

    graph.addEdge(22, 23);
    return graph;
  }

  public static void main(String[] args) {
    new SugiyamaEiglspergerWithWithoutHorizontalCompaction();
  }
}
