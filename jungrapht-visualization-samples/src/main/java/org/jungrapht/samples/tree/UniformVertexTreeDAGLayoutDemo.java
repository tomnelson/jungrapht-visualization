package org.jungrapht.samples.tree;

import java.awt.*;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class UniformVertexTreeDAGLayoutDemo extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(UniformVertexTreeDAGLayoutDemo.class);

  static Predicate<Integer> edgePredicate = e -> e < 100;

  Graph<String, Integer> graph;

  VisualizationViewer<String, Integer> vv;

  public UniformVertexTreeDAGLayoutDemo() {

    JPanel container = new JPanel(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.generatePicture();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(new TreeLayoutAlgorithm<>())
            .viewSize(new Dimension(600, 600))
            .build();

    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);

    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelDrawPaintFunction(c -> Color.white);
    vv.getRenderContext().setEdgeLabelFunction(Objects::toString);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    container.add(panel);

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
            //            .after(vv::scaleToLayout)
            .build();

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    add(container);

    Box controls = Box.createHorizontalBox();
    controls.add(ControlHelpers.getCenteredContainer("Layout Controls", treeLayoutSelector));
    controls.add(ControlHelpers.getZoomControls(vv));
    add(controls, BorderLayout.SOUTH);
    pack();
    setVisible(true);
  }

  public static void main(String[] args) {
    new UniformVertexTreeDAGLayoutDemo();
  }
}
