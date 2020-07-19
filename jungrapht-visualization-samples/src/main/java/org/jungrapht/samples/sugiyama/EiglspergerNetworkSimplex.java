package org.jungrapht.samples.sugiyama;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.layout.algorithms.sugiyama.TransformedGraphSupplier;
import org.jungrapht.visualization.layout.algorithms.util.NetworkSimplex;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EiglspergerNetworkSimplex extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerNetworkSimplex.class);

  public EiglspergerNetworkSimplex() {

    JPanel container = new JPanel(new BorderLayout());

    Graph<String, Integer> graph = createInitialGraph();

    VisualizationViewer<String, Integer> vv3 = configureVisualizationViewer(graph);

    vv3.getRenderContext().setEdgeLabelFunction(Object::toString);

    EiglspergerLayoutAlgorithm<String, Integer> layoutAlgorithm3 =
        EiglspergerLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            //                        .straightenEdges(false)
            .postStraighten(true)
            .threaded(false)
            .layering(Layering.NETWORK_SIMPLEX)
            //            .after(vv3::scaleToLayout)
            .build();
    layoutAlgorithm3.setVertexShapeFunction(vv3.getRenderContext().getVertexShapeFunction());
    layoutAlgorithm3.setEdgeShapeFunctionConsumer(vv3.getRenderContext()::setEdgeShapeFunction);
    vv3.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm3);
    container.add(vv3.getComponent());

    TransformedGraphSupplier<String, Integer> transformedGraphSupplier =
        new TransformedGraphSupplier<>(graph);
    Graph<LV<String>, LE<String, Integer>> svGraph = transformedGraphSupplier.get();
    NetworkSimplex<String, Integer> ns = NetworkSimplex.builder(svGraph).build();
    ns.run();
    Map<LE<String, Integer>, Boolean> eiMap = ns.getEdgeInTreeMap();
    Map<Integer, Boolean> map = new HashMap<>();
    eiMap.entrySet().stream().forEach(entry -> map.put(entry.getKey().getEdge(), entry.getValue()));
    vv3.getRenderContext()
        .setEdgeDrawPaintFunction(e -> map.getOrDefault(e, false) ? Color.red : Color.black);

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

  /**
   * creates a graph to look like the one in the paper
   *
   * @return
   */
  Graph<String, Integer> createInitialGraph() {
    Graph<String, Integer> dag =
        GraphTypeBuilder.<String, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    dag.addVertex("a");
    dag.addVertex("b");
    dag.addVertex("c");
    dag.addVertex("d");
    dag.addVertex("e");
    dag.addVertex("f");
    dag.addVertex("g");
    dag.addVertex("h");

    int ae = dag.addEdge("a", "e");
    int af = dag.addEdge("a", "f");
    int ab = dag.addEdge("a", "b");
    int bc = dag.addEdge("b", "c");
    int eg = dag.addEdge("e", "g");
    int fg = dag.addEdge("f", "g");
    int cd = dag.addEdge("c", "d");
    int gh = dag.addEdge("g", "h");
    int dh = dag.addEdge("d", "h");

    return dag;
  }

  public static void main(String[] args) {
    new EiglspergerNetworkSimplex();
  }
}
