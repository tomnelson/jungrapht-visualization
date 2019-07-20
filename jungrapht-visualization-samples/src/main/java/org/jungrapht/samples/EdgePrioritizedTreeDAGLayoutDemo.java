/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import com.google.common.collect.Sets;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.visualization.GraphZoomScrollPane;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse.Mode;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.EdgePrioritizedTreeLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates TreeLayout and RadialTreeLayout.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class EdgePrioritizedTreeDAGLayoutDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(EdgePrioritizedTreeDAGLayoutDemo.class);
  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  VisualizationServer.Paintable rings;

  public EdgePrioritizedTreeDAGLayoutDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = createDAG();

    // get some edges to prioritize
    Set<Integer> prioritySet = Sets.newHashSet(0, 2, 6, 8);
    List<Set<Integer>> priorityList = new ArrayList<>();
    priorityList.add(prioritySet);
    EdgePrioritizedTreeLayoutAlgorithm layoutAlgorithm =
        EdgePrioritizedTreeLayoutAlgorithm.builder().edgePriorityList(priorityList).build();

    vv = new VisualizationViewer<>(graph, layoutAlgorithm, new Dimension(600, 600));
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    // add a listener for ToolTips
    vv.setNodeToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);

    vv.getRenderContext().setNodeLabelPosition(Renderer.NodeLabel.Position.CNTR);
    vv.getRenderContext().setNodeLabelDrawPaintFunction(c -> Color.white);
    vv.getRenderContext().setEdgeLabelFunction(Objects::toString);

    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    add(panel);

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(Mode.TRANSFORMING);

    JPanel layoutPanel = new JPanel(new GridLayout(2, 1));
    //    layoutPanel.add(new TreeLayoutSelector(vv));
    JPanel controls = new JPanel();
    controls.add(layoutPanel);
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));
    controls.add(modeBox);

    add(controls, BorderLayout.SOUTH);
  }

  private Graph<String, Integer> createDAG() {
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag()).buildGraph();
    Integer i = 0;
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
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new EdgePrioritizedTreeDAGLayoutDemo());
    frame.pack();
    frame.setVisible(true);
  }
}