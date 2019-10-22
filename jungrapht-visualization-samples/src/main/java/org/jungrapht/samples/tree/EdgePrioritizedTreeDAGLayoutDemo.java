package org.jungrapht.samples.tree;

import com.google.common.collect.Sets;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.decorators.IconShapeFunction;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.util.IconCache;
import org.jungrapht.visualization.util.helpers.ControlHelpers;
import org.jungrapht.visualization.util.helpers.TreeLayoutSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
@SuppressWarnings("serial")
public class EdgePrioritizedTreeDAGLayoutDemo extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(EdgePrioritizedTreeDAGLayoutDemo.class);

  static Set<Integer> prioritySet = Sets.newHashSet(0, 2, 6, 8);

  static Predicate<Integer> edgePredicate =
      //          e -> false;
      e -> e < 100;

  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  public EdgePrioritizedTreeDAGLayoutDemo() {

    JPanel container = new JPanel(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.generatePicture();
    //            .generateDag();
    //            .generateProgramGraph2();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(TreeLayoutAlgorithm.<String>builder().build())
            .viewSize(new Dimension(600, 600))
            .build();

    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    //    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);

    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelDrawPaintFunction(c -> Color.white);
    vv.getRenderContext().setEdgeLabelFunction(Objects::toString);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    container.add(panel);

    final DefaultGraphMouse<String, Integer> graphMouse = new DefaultGraphMouse<>();

    vv.setGraphMouse(graphMouse);

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
                  //                                                label.setOpaque(true);
                  Border lineBorder =
                      BorderFactory.createEtchedBorder(); //Border(BevelBorder.RAISED);
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
    vertexImageShapeFunction.setIconMap(iconCache);

    vv.getRenderContext().setVertexShapeFunction(vertexImageShapeFunction);
    vv.getRenderContext().setVertexIconFunction(iconCache::get);

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
            .vertexShapeFunction(vv.getRenderContext().getVertexShapeFunction())
            .alignFavoredEdges(false)
            .after(vv::scaleToLayout)
            .build();

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    setTitle("Prioritized Edges are " + prioritySet);
    add(container);

    Box controls = Box.createHorizontalBox();
    controls.add(ControlHelpers.getCenteredContainer("Layout Controls", treeLayoutSelector));
    controls.add(ControlHelpers.getZoomControls(vv));
    add(controls, BorderLayout.SOUTH);
    pack();
    setVisible(true);
  }

  private Graph<String, Integer> createDAG() {
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag()).buildGraph();
    int i = 0;
    // roots
    graph.addVertex("R1");
    graph.addVertex("R2");
    graph.addVertex("R3");
    graph.addVertex("R4");

    graph.addVertex("A1");
    graph.addVertex("A2");
    graph.addVertex("A3");
    graph.addVertex("A4");
    graph.addVertex("A5");
    graph.addVertex("A6");

    graph.addEdge("R1", "A1", i++);
    graph.addEdge("R1", "A2", i++);
    graph.addEdge("A1", "A3", i++);
    graph.addEdge("A1", "A4", i++);

    graph.addEdge("A4", "A3", i++);
    graph.addEdge("A3", "A4", i++);

    graph.addEdge("R2", "A5", i++);
    graph.addEdge("R3", "A5", i++);
    graph.addEdge("A5", "A6", i++);
    //    graph.addEdge("R1","A1", i++);
    return graph;
  }

  public static void main(String[] args) {
    new EdgePrioritizedTreeDAGLayoutDemo();
  }
}
