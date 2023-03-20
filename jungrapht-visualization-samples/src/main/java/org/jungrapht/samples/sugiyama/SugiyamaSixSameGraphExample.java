package org.jungrapht.samples.sugiyama;

import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows the four HorizontalCompaction algorithms, plus the average median for
 * SugiyamaLayoutAlgorithm
 *
 * @author Tom Nelson
 */
public class SugiyamaSixSameGraphExample extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(SugiyamaSixSameGraphExample.class);

  public SugiyamaSixSameGraphExample() {

    JPanel container = new JPanel(new GridLayout(2, 3));

    Graph<Integer, Integer> graph1 = createInitialGraph();
    Graph<Integer, Integer> graph2 = createInitialGraph();
    Graph<Integer, Integer> graph3 = createInitialGraph();
    Graph<Integer, Integer> graph4 = createInitialGraph();
    Graph<Integer, Integer> graph5 = createInitialGraph();
    Graph<Integer, Integer> graph6 = createInitialGraph();

    VisualizationViewer<Integer, Integer> vv1 = configureVisualizationViewer(graph1);
    VisualizationViewer<Integer, Integer> vv2 = configureVisualizationViewer(graph2);
    VisualizationViewer<Integer, Integer> vv3 = configureVisualizationViewer(graph3);
    VisualizationViewer<Integer, Integer> vv4 = configureVisualizationViewer(graph4);
    VisualizationViewer<Integer, Integer> vv5 = configureVisualizationViewer(graph5);
    VisualizationViewer<Integer, Integer> vv6 = configureVisualizationViewer(graph6);

    SugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm1 =
        SugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder().threaded(false).build();
    layoutAlgorithm1.accept(vv1.getRenderContext().getVertexBoundsFunction());
    vv1.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm1);

    SugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm2 =
        SugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder().threaded(false).build();
    layoutAlgorithm2.accept(vv2.getRenderContext().getVertexBoundsFunction());
    vv2.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm2);

    // this one will look different because of edge sorting change
    SugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm3 =
        SugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            .threaded(false)
            .edgeComparator((e1, e2) -> -e1.compareTo(-e2))
            .build();
    layoutAlgorithm3.accept(vv3.getRenderContext().getVertexBoundsFunction());
    vv3.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm3);

    SugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm4 =
        SugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder().threaded(false).build();
    layoutAlgorithm4.accept(vv4.getRenderContext().getVertexBoundsFunction());
    vv4.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm4);

    SugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm5 =
        SugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder().threaded(false).build();
    layoutAlgorithm5.accept(vv5.getRenderContext().getVertexBoundsFunction());
    vv5.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm5);

    SugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm6 =
        SugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder().threaded(false).build();
    layoutAlgorithm6.accept(vv6.getRenderContext().getVertexBoundsFunction());
    vv6.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm6);

    container.add(vv1.getComponent());
    container.add(vv2.getComponent());
    container.add(vv5.getComponent());
    container.add(vv3.getComponent());
    container.add(vv4.getComponent());
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
    new SugiyamaSixSameGraphExample();
  }
}
