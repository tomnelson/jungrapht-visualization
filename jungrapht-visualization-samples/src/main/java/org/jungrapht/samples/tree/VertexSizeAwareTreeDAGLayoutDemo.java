package org.jungrapht.samples.tree;

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
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.decorators.IconShapeFunction;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.util.IconCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class VertexSizeAwareTreeDAGLayoutDemo extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(VertexSizeAwareTreeDAGLayoutDemo.class);

  static Set<Integer> prioritySet = Set.of(0, 2, 6, 8);

  static Predicate<Integer> edgePredicate =
      //          e -> false;
      e -> e < 100;

  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  public VertexSizeAwareTreeDAGLayoutDemo() {

    JPanel container = new JPanel(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.generatePicture();
    //            .generateDag();
    //            .generateProgramGraph2();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(new TreeLayoutAlgorithm<>())
            .viewSize(new Dimension(600, 600))
            .build();

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

  public static void main(String[] args) {
    new VertexSizeAwareTreeDAGLayoutDemo();
  }
}
