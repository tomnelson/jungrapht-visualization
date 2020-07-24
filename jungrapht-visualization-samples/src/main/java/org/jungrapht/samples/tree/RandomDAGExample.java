package org.jungrapht.samples.tree;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.common.base.Strings;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.decorators.IconShapeFunction;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.util.IconCache;
import org.jungrapht.visualization.util.LayoutPaintable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class RandomDAGExample extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(RandomDAGExample.class);

  static Set<Integer> prioritySet = Set.of(0, 2, 6, 8);

  static Predicate<Integer> edgePredicate = e -> false;

  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  public RandomDAGExample() {

    LayoutPaintable.LayoutBounds paintable;
    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.createDirectedAcyclicGraph(9, 3, .2, 5L);

    vv = VisualizationViewer.builder(graph).viewSize(new Dimension(900, 600)).build();
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    // need to update the edge spatial structure
    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);

    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelDrawPaintFunction(c -> Color.white);
    // for the first layout
    vv.scaleToLayout();
    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    this.add(panel);

    IconCache<String> iconCache =
        IconCache.<String>builder(Object::toString)
            .vertexShapeFunction(vv.getRenderContext().getVertexShapeFunction())
            .colorFunction(
                n -> {
                  if (graph.degreeOf(n) > 9) return Color.red;
                  if (graph.degreeOf(n) < 7) return Color.green;
                  return Color.lightGray;
                })
            .stylist(
                (label, vertex, colorFunction) -> {
                  label.setFont(new Font("Serif", Font.BOLD, 20));
                  label.setForeground(Color.black);
                  label.setBackground(Color.white);
                  Border lineBorder = BorderFactory.createEtchedBorder();
                  Border marginBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
                  label.setBorder(new CompoundBorder(lineBorder, marginBorder));
                })
            .preDecorator(
                (graphics, vertex, labelBounds, vertexShapeFunction, colorFunction) -> {
                  Color color = (Color) colorFunction.apply(vertex);
                  color =
                      new Color(
                          color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 4);
                  // save off the old color
                  Color oldColor = graphics.getColor();
                  // fill the image background with white
                  graphics.setPaint(Color.white);
                  graphics.fill(labelBounds);

                  Shape shape = vertexShapeFunction.apply(vertex);
                  Rectangle2D bounds = shape.getBounds2D();

                  AffineTransform scale =
                      AffineTransform.getScaleInstance(
                          labelBounds.width / bounds.getWidth(),
                          labelBounds.height / bounds.getHeight());
                  AffineTransform translate =
                      AffineTransform.getTranslateInstance(
                          labelBounds.width / 2, labelBounds.height / 2);
                  translate.concatenate(scale);
                  shape = translate.createTransformedShape(shape);
                  graphics.setColor(color);
                  graphics.fill(shape);
                  graphics.setColor(oldColor);
                })
            .build();

    final IconShapeFunction<String> vertexImageShapeFunction =
        new IconShapeFunction<>(new EllipseShapeFunction<>());
    vertexImageShapeFunction.setIconFunction(iconCache);

    vv.getRenderContext().setVertexShapeFunction(vertexImageShapeFunction);
    vv.getRenderContext().setVertexIconFunction(iconCache);

    vv.getRenderContext()
        .getSelectedVertexState()
        .select(
            Stream.concat(
                    vv.getVisualizationModel()
                        .getGraph()
                        .edgeSet()
                        .stream()
                        .filter(edgePredicate)
                        .map(e -> graph.getEdgeTarget(e)),
                    vv.getVisualizationModel()
                        .getGraph()
                        .edgeSet()
                        .stream()
                        .filter(edgePredicate)
                        .map(e -> graph.getEdgeSource(e)))
                .collect(Collectors.toList()));

    TreeLayoutSelector<String, Integer> treeLayoutSelector =
        TreeLayoutSelector.<String, Integer>builder(vv)
            .edgePredicate(edgePredicate)
            .initialSelection(2)
            .vertexShapeFunction(vv.getRenderContext().getVertexShapeFunction())
            .alignFavoredEdges(false)
            .build();

    JRadioButton showSpatialEffects = new JRadioButton("Show Structure");
    showSpatialEffects.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            System.err.println("TURNED ON LOGGING");
            // turn on the logging
            // programmatically set the log level so that the spatial grid is drawn for this demo and the SpatialGrid logging is output
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            ctx.getLogger("org.jungrapht.visualization.layout.spatial").setLevel(Level.DEBUG);
            ctx.getLogger("org.jungrapht.visualization.DefaultVisualizationServer")
                .setLevel(Level.TRACE);
            ctx.getLogger("org.jungrapht.visualization.picking").setLevel(Level.TRACE);
            repaint();

          } else if (e.getStateChange() == ItemEvent.DESELECTED) {
            System.err.println("TURNED OFF LOGGING");
            // turn off the logging
            // programmatically set the log level so that the spatial grid is drawn for this demo and the SpatialGrid logging is output
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            ctx.getLogger("org.jungrapht.visualization.layout.spatial").setLevel(Level.INFO);
            ctx.getLogger("org.jungrapht.visualization.DefaultVisualizationServer")
                .setLevel(Level.INFO);
            ctx.getLogger("org.jungrapht.visualization.picking").setLevel(Level.INFO);
            repaint();
          }
        });

    JTextField layersText = new JTextField("10", 15);
    layersText.setBorder(new TitledBorder("Graph Layers"));
    JTextField maxVerticesPerLayerText = new JTextField("4", 15);
    maxVerticesPerLayerText.setBorder(new TitledBorder("Max V per Layer"));
    JTextField linkProbabilityText = new JTextField("0.2", 15);
    linkProbabilityText.setBorder(new TitledBorder("Link Probabilty"));
    JTextField randomSeedText = new JTextField("7", 15);
    randomSeedText.setBorder(new TitledBorder("Random Seed"));

    JTextField maxLevelCrossText = new JTextField("23", 15);
    maxLevelCrossText.setBorder(new TitledBorder("Max Level Cross"));

    JPanel graphPropertyPanel = new JPanel(new GridLayout(2, 2));
    graphPropertyPanel.add(layersText);
    graphPropertyPanel.add(maxVerticesPerLayerText);
    graphPropertyPanel.add(linkProbabilityText);
    graphPropertyPanel.add(randomSeedText);
    JPanel floater = new JPanel();
    floater.setBorder(new TitledBorder("Graph Generator"));
    floater.add(graphPropertyPanel);
    ActionListener action =
        e -> {
          int layers = 9;
          if (!Strings.isNullOrEmpty(layersText.getText())) {
            try {
              layers = Integer.parseInt(layersText.getText());
            } catch (Exception ex) {
            }
          }
          int maxVerticesPerLayer = 3;
          if (!Strings.isNullOrEmpty(layersText.getText())) {
            try {
              maxVerticesPerLayer = Integer.parseInt(maxVerticesPerLayerText.getText());
            } catch (Exception ex) {
            }
          }
          double linkProbability = 0.2;
          if (!Strings.isNullOrEmpty(linkProbabilityText.getText())) {
            try {
              linkProbability = Double.parseDouble(linkProbabilityText.getText());
            } catch (Exception ex) {
            }
          }
          long randomSeed = System.currentTimeMillis();
          if (!Strings.isNullOrEmpty(randomSeedText.getText())) {
            try {
              randomSeed = Long.parseLong(randomSeedText.getText());
            } catch (Exception ex) {
            }
          }
          resetGraph(layers, maxVerticesPerLayer, linkProbability, randomSeed);
        };
    layersText.addActionListener(action);
    maxVerticesPerLayerText.addActionListener(action);
    linkProbabilityText.addActionListener(action);
    randomSeedText.addActionListener(action);

    Box controls = Box.createHorizontalBox();
    controls.add(ControlHelpers.getCenteredContainer("Layout Controls", treeLayoutSelector));
    Box rightControls = Box.createVerticalBox();
    rightControls.add(floater);
    JPanel spatialPanel = new JPanel();
    spatialPanel.setBorder(new TitledBorder("Spatial Structure"));
    spatialPanel.add(showSpatialEffects);
    rightControls.add(spatialPanel);
    controls.add(rightControls);
    add(controls, BorderLayout.SOUTH);
    setVisible(true);
  }

  private void resetGraph(
      int layers, int maxVerticesPerLayer, double linkProbability, long randomSeed) {
    this.graph =
        TestGraphs.createDirectedAcyclicGraph(
            layers, maxVerticesPerLayer, linkProbability, randomSeed);
    vv.getVisualizationModel().getLayoutModel().setSize(600, 600); // put back original size
    vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
    vv.getVisualizationModel().setGraph(this.graph, true);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new RandomDAGExample());
    f.pack();
    f.setVisible(true);
  }
}
