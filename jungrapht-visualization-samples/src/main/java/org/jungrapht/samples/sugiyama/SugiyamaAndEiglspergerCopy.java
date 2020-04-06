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
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithmWithGraph;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SugiyamaAndEiglspergerCopy extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(SugiyamaAndEiglspergerCopy.class);

  public SugiyamaAndEiglspergerCopy() {

    JPanel container = new JPanel(new GridLayout(2, 1));

    Graph<Integer, Integer> graph = createInitialGraph();

    VisualizationViewer<Integer, Integer> vv1 = configureVisualizationViewer(graph);
    vv1.addPreRenderPaintable(new TitlePaintable("Sugiyama ", vv1.getPreferredSize()));
    vv1.getRenderContext().setEdgeLabelFunction(Object::toString);
    VisualizationViewer<Integer, Integer> vv2 = configureVisualizationViewer(graph);
    vv2.addPreRenderPaintable(new TitlePaintable("Eiglsperger", vv1.getPreferredSize()));
    vv2.getRenderContext().setEdgeLabelFunction(Object::toString);

    SugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm1 =
        SugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .straightenEdges(true)
            .postStraighten(false)
            .transpose(true)
            //                .useLongestPathLayering(true)
            .after(vv1::scaleToLayout)
            .build();
    layoutAlgorithm1.setRenderContext(vv1.getRenderContext());
    vv1.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm1);
    container.add(vv1.getComponent());

    EiglspergerLayoutAlgorithmWithGraph<Integer, Integer> layoutAlgorithm4 =
        EiglspergerLayoutAlgorithmWithGraph.<Integer, Integer>edgeAwareBuilder()
            .straightenEdges(true)
            //                .useLongestPathLayering(true)
            .after(vv2::scaleToLayout)
            .build();
    layoutAlgorithm4.setRenderContext(vv2.getRenderContext());
    vv2.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm4);
    container.add(vv2.getComponent());

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

  Graph<Integer, Integer> createSmallGraph() {

    Graph<Integer, Integer> graph =
        GraphTypeBuilder.<Integer, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .vertexSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    IntStream.rangeClosed(1, 7).forEach(graph::addVertex);

    graph.addEdge(1, 2);
    graph.addEdge(2, 3);
    graph.addEdge(3, 4);
    graph.addEdge(1, 3);

    graph.addEdge(1, 4);

    graph.addEdge(1, 5);
    graph.addEdge(5, 6);
    graph.addEdge(6, 4);

    graph.addEdge(2, 5);

    graph.addEdge(6, 7);

    return graph;
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
    new SugiyamaAndEiglspergerCopy();
  }
}
