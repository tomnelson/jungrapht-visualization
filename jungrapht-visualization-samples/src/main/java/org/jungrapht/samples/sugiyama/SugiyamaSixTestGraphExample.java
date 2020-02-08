package org.jungrapht.samples.sugiyama;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.stream.IntStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.samples.sugiyama.test.algorithms.TestSugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a test graph from the BrandesKopf paper, layers the graph, then manually sets all the
 * synthetic vertices and edges to make a good starting point for the BrandesKopf Horizontal
 * Coordinate Assignment algorithm
 *
 * @author Tom Nelson
 */
public class SugiyamaSixTestGraphExample extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(SugiyamaSixTestGraphExample.class);

  public SugiyamaSixTestGraphExample() {

    JPanel container = new JPanel(new GridLayout(2, 3));

    Graph<Integer, Integer> graph1 = createInitialGraph();
    Graph<Integer, Integer> graph2 = createInitialGraph();
    Graph<Integer, Integer> graph3 = createInitialGraph();
    Graph<Integer, Integer> graph4 = createInitialGraph();
    Graph<Integer, Integer> graph5 = createInitialGraph();
    Graph<Integer, Integer> graph6 = createInitialGraph();

    VisualizationViewer<Integer, Integer> vv1 = configureVisualizationViewer(graph1);
    vv1.addPreRenderPaintable(new TitlePaintable("Upper Left", vv1.getPreferredSize()));
    VisualizationViewer<Integer, Integer> vv2 = configureVisualizationViewer(graph2);
    vv2.addPreRenderPaintable(new TitlePaintable("Upper Right", vv2.getPreferredSize()));
    VisualizationViewer<Integer, Integer> vv3 = configureVisualizationViewer(graph3);
    vv3.addPreRenderPaintable(new TitlePaintable("Lower Left", vv3.getPreferredSize()));
    VisualizationViewer<Integer, Integer> vv4 = configureVisualizationViewer(graph4);
    vv4.addPreRenderPaintable(new TitlePaintable("Lower Right", vv4.getPreferredSize()));
    VisualizationViewer<Integer, Integer> vv5 = configureVisualizationViewer(graph5);
    vv5.addPreRenderPaintable(new TitlePaintable("Average Median", vv5.getPreferredSize()));
    VisualizationViewer<Integer, Integer> vv6 = configureVisualizationViewer(graph6);
    vv6.addPreRenderPaintable(new TitlePaintable("Upper & Lower Left", vv6.getPreferredSize()));

    TestSugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm1 =
        TestSugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .doUpLeft(true)
            .threaded(false)
            .after(vv1::scaleToLayout)
            .build();
    layoutAlgorithm1.setRenderContext(vv1.getRenderContext());
    vv1.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm1);

    TestSugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm2 =
        TestSugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .doUpRight(true)
            .threaded(false)
            .after(vv2::scaleToLayout)
            .build();
    layoutAlgorithm2.setRenderContext(vv2.getRenderContext());
    vv2.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm2);

    TestSugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm3 =
        TestSugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .doDownLeft(true)
            .threaded(false)
            .after(vv3::scaleToLayout)
            .build();
    layoutAlgorithm3.setRenderContext(vv3.getRenderContext());
    vv3.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm3);

    TestSugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm4 =
        TestSugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .doDownRight(true)
            .threaded(false)
            .after(vv4::scaleToLayout)
            .build();
    layoutAlgorithm4.setRenderContext(vv4.getRenderContext());
    vv4.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm4);

    TestSugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm5 =
        TestSugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .doUpLeft(true)
            .doUpRight(true)
            .doDownLeft(true)
            .doDownRight(true)
            .after(vv5::scaleToLayout)
            .build();
    layoutAlgorithm5.setRenderContext(vv5.getRenderContext());
    vv5.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm5);

    TestSugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm6 =
        TestSugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .doUpLeft(true)
            .doUpRight(false)
            .doDownLeft(true)
            .doDownRight(false)
            .after(vv6::scaleToLayout)
            .build();
    layoutAlgorithm6.setRenderContext(vv6.getRenderContext());
    vv6.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm6);

    container.add(vv1.getComponent());
    container.add(vv2.getComponent());
    container.add(vv3.getComponent());
    container.add(vv4.getComponent());
    container.add(vv5.getComponent());
    container.add(vv6.getComponent());

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
            .viewSize(new Dimension(300, 300))
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
    new SugiyamaSixTestGraphExample();
  }
}
