package org.jungrapht.samples;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.LayeredIcon;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.util.RandomLocationTransformer;
import org.jungrapht.visualization.renderers.JLabelEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.JLabelVertexLabelRenderer;
import org.jungrapht.visualization.renderers.Renderer.VertexLabel.Position;
import org.jungrapht.visualization.util.VertexStyleConfiguration;

/** @author Tom Nelson */
public class VertexImagesOrNotDemo extends JPanel {
  Graph<String, Integer> graph;

  VisualizationViewer<String, Integer> vv;

  VertexStyleConfiguration<String, Integer> vertexStyleConfiguration;

  /** some icon names to use */
  String[] iconNames = {
    "apple",
    "os",
    "x",
    "linux",
    "inputdevices",
    "wireless",
    "graphics3",
    "gamespcgames",
    "humor",
    "music",
    "privacy"
  };

  public VertexImagesOrNotDemo() {
    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getDemoGraph();

    // Maps for the labels and icons
    Map<String, String> map = new HashMap<>();
    Map<String, Icon> iconMap = new HashMap<>();
    for (String vertex : graph.vertexSet()) {
      int index = (int) (Math.random() * iconNames.length * 1.5);
      if (index < iconNames.length) {
        map.put(vertex, iconNames[index]);

        String name = "/images/topic" + iconNames[index] + ".gif";
        try {
          Icon icon =
              new LayeredIcon(
                  new ImageIcon(VertexImagesOrNotDemo.class.getResource(name)).getImage());
          iconMap.put(vertex, icon);
        } catch (Exception ex) {
          System.err.println(
              "You need slashdoticons.jar in your classpath to see the image " + name);
        }
      }
    }

    LayoutAlgorithm<String> layoutAlgorithm = new KKLayoutAlgorithm<>();

    final DefaultGraphMouse<String, Integer> graphMouse = new DefaultGraphMouse<>();

    vv =
        VisualizationViewer.builder(
                (VisualizationModel<String, Integer>)
                    VisualizationModel.builder(graph)
                        .layoutAlgorithm(layoutAlgorithm)
                        .initializer(new RandomLocationTransformer<>(600, 600, 0))
                        .layoutSize(new Dimension(600, 600))
                        .build())
            .viewSize(new Dimension(600, 600))
            .graphMouse(graphMouse)
            .build();

    vv.getRenderContext().setVertexShapeFunction(v -> new Ellipse2D.Float(-20, -20, 40, 40));

    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv.getSelectedEdgeState(), Color.black, Color.cyan));

    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setVertexLabelRenderer(new JLabelVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new JLabelEdgeLabelRenderer(Color.cyan));

    vv.addPostRenderPaintable(
        new VisualizationViewer.Paintable() {
          int x;
          int y;
          Font font;
          FontMetrics metrics;
          int swidth;
          int sheight;
          String str = "Thank You, slashdot.org, for the images!";

          public void paint(Graphics g) {
            Dimension d = vv.getSize();
            if (font == null) {
              font = new Font(g.getFont().getName(), Font.BOLD, 20);
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
        });

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    JCheckBox showIcons = new JCheckBox("Show Icons", true);
    showIcons.addItemListener(
        e -> {
          vertexStyleConfiguration.configure();
          vv.repaint();
        });

    JCheckBox showLabels = new JCheckBox("Show Labels", true);
    showLabels.addItemListener(
        e -> {
          vertexStyleConfiguration.configure();
          vv.repaint();
        });

    JComboBox<Position> positions = new JComboBox(Position.values());
    positions.addItemListener(
        e -> {
          vertexStyleConfiguration.configure();
          vv.repaint();
        });

    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Label Position"));
    modePanel.add(positions);

    JPanel labelFeatures = new JPanel(new GridLayout(1, 0));
    labelFeatures.setBorder(BorderFactory.createTitledBorder("Image Effects"));
    JPanel controls = new JPanel();
    labelFeatures.add(showIcons);
    labelFeatures.add(showLabels);
    labelFeatures.add(positions);

    controls.add(labelFeatures);
    controls.add(modePanel);
    add(controls, BorderLayout.SOUTH);

    this.vertexStyleConfiguration =
        VertexStyleConfiguration.builder(vv)
            .showVertexIconSupplier(showIcons::isSelected)
            .showVertexLabelSupplier(showLabels::isSelected)
            .vertexShapeFunction(v -> new Ellipse2D.Float(-20, -20, 40, 40))
            .labelFunction(Object::toString)
            .labelPositionSupplier(() -> (Position) positions.getSelectedItem())
            .iconFunction(iconMap::get)
            .vertexShapeFunction(vv.getRenderContext().getVertexShapeFunction())
            .build();
    vertexStyleConfiguration.configure();
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new VertexImagesOrNotDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
