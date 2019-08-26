package org.jungrapht.samples.tree;

import com.google.common.collect.Sets;
import java.awt.*;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
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
    graph = DemoTreeSupplier.generateProgramGraph2();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(TreeLayoutAlgorithm.<String>builder().build())
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

    final DefaultGraphMouse<String, Integer> graphMouse = new DefaultGraphMouse<>();

    vv.setGraphMouse(graphMouse);

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

    JPanel layoutPanel = new JPanel(new GridLayout(2, 1));
    JPanel controls = new JPanel();
    controls.add(layoutPanel);
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));

    // layout controls
    JPanel layoutControls = new JPanel(new GridLayout(0, 1));
    layoutControls.setBorder(new TitledBorder("Layout Selections"));

    TreeLayoutSelector<String, Integer> treeLayoutSelector =
        TreeLayoutSelector.<String, Integer>builder(vv)
            .edgePredicate(edgePredicate)
            .after(vv::scaleToLayout)
            .build();

    layoutControls.add(treeLayoutSelector);
    controls.add(layoutControls);

    container.add(controls, BorderLayout.SOUTH);

    getContentPane();
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    setTitle("Prioritized Edges are " + prioritySet);
    add(container);
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
