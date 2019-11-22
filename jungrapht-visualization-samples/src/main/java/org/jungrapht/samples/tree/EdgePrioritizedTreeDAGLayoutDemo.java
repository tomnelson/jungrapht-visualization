package org.jungrapht.samples.tree;

import com.google.common.collect.Sets;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.util.helpers.ControlHelpers;
import org.jungrapht.visualization.util.helpers.TreeLayoutSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This demo shows how the edgePredicate can be used to construct the desired tree shape. Note that
 * in non-edge aware layouts, node B1 and its subtree are under node A14. The edge predicate will
 * cause edge 12 (from A14 to B1 to be skipped in favor of edge 4 from B0 to B1.
 *
 * @author Tom Nelson
 */
public class EdgePrioritizedTreeDAGLayoutDemo extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(EdgePrioritizedTreeDAGLayoutDemo.class);

  static Set<Integer> prioritySet = Sets.newHashSet(0, 2, 6, 8);

  static Predicate<Integer> edgePredicate = e -> e < 100;

  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  public EdgePrioritizedTreeDAGLayoutDemo() {

    JPanel container = new JPanel(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.generateProgramGraph2();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(new TreeLayoutAlgorithm<>())
            .viewSize(new Dimension(600, 600))
            .build();

    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    // add a listener for ToolTips
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
    new EdgePrioritizedTreeDAGLayoutDemo();
  }
}
