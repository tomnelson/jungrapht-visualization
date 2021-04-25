package org.jungrapht.samples.sugiyama;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
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
import org.jungrapht.visualization.util.Attributed;
import org.jungrapht.visualization.util.DefaultAttributed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SugiyamaAndEiglspergerAttributed extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(SugiyamaAndEiglspergerAttributed.class);

  public SugiyamaAndEiglspergerAttributed() {

    JPanel container = new JPanel(new GridLayout(2, 1));

    Graph<Attributed<Integer>, Attributed<Integer>> graph = createInitialGraph();

    VisualizationViewer<Attributed<Integer>, Attributed<Integer>> vv1 =
        configureVisualizationViewer(graph);
    vv1.addPreRenderPaintable(new TitlePaintable("Sugiyama ", vv1.getPreferredSize()));
    vv1.getRenderContext().setEdgeLabelFunction(Object::toString);
    VisualizationViewer<Attributed<Integer>, Attributed<Integer>> vv2 =
        configureVisualizationViewer(graph);
    vv2.addPreRenderPaintable(new TitlePaintable("Eiglsperger", vv1.getPreferredSize()));
    vv2.getRenderContext().setEdgeLabelFunction(Object::toString);

    SugiyamaLayoutAlgorithm<Attributed<Integer>, Attributed<Integer>> layoutAlgorithm1 =
        SugiyamaLayoutAlgorithm.<Attributed<Integer>, Attributed<Integer>>edgeAwareBuilder()
            .straightenEdges(true)
            .postStraighten(false)
            .transpose(true)
            .build();
    layoutAlgorithm1.setVertexBoundsFunction(vv1.getRenderContext().getVertexBoundsFunction());
    vv1.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm1);
    container.add(vv1.getComponent());

    EiglspergerLayoutAlgorithm<Attributed<Integer>, Attributed<Integer>> layoutAlgorithm4 =
        EiglspergerLayoutAlgorithm.<Attributed<Integer>, Attributed<Integer>>edgeAwareBuilder()
            .straightenEdges(true)
            .build();
    layoutAlgorithm4.setVertexBoundsFunction(vv2.getRenderContext().getVertexBoundsFunction());
    vv2.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm4);
    container.add(vv2.getComponent());

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    add(container);
    pack();
    setVisible(true);
  }

  private VisualizationViewer<Attributed<Integer>, Attributed<Integer>>
      configureVisualizationViewer(Graph<Attributed<Integer>, Attributed<Integer>> graph) {
    VisualizationViewer<Attributed<Integer>, Attributed<Integer>> vv =
        VisualizationViewer.builder(graph)
            .layoutSize(new Dimension(600, 600))
            .viewSize(new Dimension(700, 500))
            .build();

    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);

    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelDrawPaintFunction(c -> Color.white);
    vv.getRenderContext().setVertexLabelFunction(v -> v.getValue().toString());
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
  Graph<Attributed<Integer>, Attributed<Integer>> createInitialGraph() {
    Supplier<Integer> integerSupplier = SupplierUtil.createIntegerSupplier(); // for edges
    Graph<Attributed<Integer>, Attributed<Integer>> graph =
        GraphTypeBuilder.<Attributed<Integer>, Attributed<Integer>>directed()
                .edgeSupplier(() -> (Attributed<Integer>)new DefaultAttributed<>(integerSupplier.get()))
            .buildGraph();

    List<Attributed<Integer>> list = new ArrayList<>();
    IntStream.rangeClosed(1, 24).forEach(n -> list.add(new DefaultAttributed<>(n)));
    list.forEach(graph::addVertex);
    graph.addEdge(list.get(1), list.get(3));
    graph.addEdge(list.get(1), list.get(4));
    graph.addEdge(list.get(1), list.get(13));
    graph.addEdge(list.get(1), list.get(21));

    graph.addEdge(list.get(2), list.get(3));
    graph.addEdge(list.get(2), list.get(20));

    graph.addEdge(list.get(3), list.get(4));
    graph.addEdge(list.get(3), list.get(5));
    graph.addEdge(list.get(3), list.get(23));

    graph.addEdge(list.get(4), list.get(6));

    graph.addEdge(list.get(5), list.get(7));

    graph.addEdge(list.get(6), list.get(8));
    graph.addEdge(list.get(6), list.get(16));
    graph.addEdge(list.get(6), list.get(23));

    graph.addEdge(list.get(7), list.get(9));

    graph.addEdge(list.get(8), list.get(10));
    graph.addEdge(list.get(8), list.get(11));

    graph.addEdge(list.get(9), list.get(12));

    graph.addEdge(list.get(10), list.get(13));
    graph.addEdge(list.get(10), list.get(14));
    graph.addEdge(list.get(10), list.get(15));

    graph.addEdge(list.get(11), list.get(15));
    graph.addEdge(list.get(11), list.get(16));

    graph.addEdge(list.get(12), list.get(20));

    graph.addEdge(list.get(13), list.get(17));

    graph.addEdge(list.get(14), list.get(17));
    graph.addEdge(list.get(14), list.get(18));
    // no 15 targets

    graph.addEdge(list.get(16), list.get(18));
    graph.addEdge(list.get(16), list.get(19));
    graph.addEdge(list.get(16), list.get(20));

    graph.addEdge(list.get(18), list.get(21));

    graph.addEdge(list.get(19), list.get(22));

    graph.addEdge(list.get(21), list.get(23));

    graph.addEdge(list.get(22), list.get(23));
    return graph;
  }

  public static void main(String[] args) {
    new SugiyamaAndEiglspergerAttributed();
  }
}
