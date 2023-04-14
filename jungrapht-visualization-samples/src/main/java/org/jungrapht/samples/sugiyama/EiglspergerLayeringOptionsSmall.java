package org.jungrapht.samples.sugiyama;

import java.awt.*;
import java.awt.event.ItemEvent;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LayeringConfiguration;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * shows the 4 layering options for the Eiglsperger optimized version of the Sugiyama layout
 * algorithm
 */
public class EiglspergerLayeringOptionsSmall extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerLayeringOptionsSmall.class);

  public EiglspergerLayeringOptionsSmall() {

    JPanel container = new JPanel(new BorderLayout());

    Graph<String, Integer> graph = createInitialGraph();

    VisualizationViewer<String, Integer> vv3 = configureVisualizationViewer(graph);

    vv3.getRenderContext().setEdgeLabelFunction(Object::toString);

    LayeringConfiguration layeringConfiguration = new LayeringConfiguration();

    layeringConfiguration.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            EiglspergerLayoutAlgorithm<String, Integer> layoutAlgorithm =
                EiglspergerLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
                    .postStraighten(false)
                    .threaded(false)
                    .layering((Layering) e.getItem())
                    .build();
            layoutAlgorithm.setVertexBoundsFunction(
                vv3.getRenderContext().getVertexBoundsFunction());
            vv3.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm);
          }
        });

    EiglspergerLayoutAlgorithm<String, Integer> layoutAlgorithm3 =
        EiglspergerLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .postStraighten(false)
            .threaded(false)
            .layering(layeringConfiguration.getLayeringPreference())
            .build();
    layoutAlgorithm3.setVertexBoundsFunction(vv3.getRenderContext().getVertexBoundsFunction());
    vv3.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm3);
    container.add(vv3.getComponent());

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    add(
        ControlHelpers.getCenteredContainer("Layering Style", layeringConfiguration),
        BorderLayout.SOUTH);

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

    dag.addEdge("a", "e");
    dag.addEdge("a", "f");
    dag.addEdge("a", "b");
    dag.addEdge("b", "c");
    dag.addEdge("e", "g");
    dag.addEdge("f", "g");
    dag.addEdge("c", "d");
    dag.addEdge("g", "h");
    dag.addEdge("d", "h");

    return dag;
  }

  public static void main(String[] args) {
    new EiglspergerLayeringOptionsSmall();
  }
}
