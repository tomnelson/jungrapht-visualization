/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.util.helpers.ControlHelpers;

/**
 * A demo that shows a minimal visualization
 *
 * @author Tom Nelson
 */
public class MinimalVisualization {

  Graph<Integer, Number> graph;

  VisualizationViewer<Integer, Number> vv;

  public MinimalVisualization() {

    // create a simple graph for the demo
    graph = createGraph();

    vv =
        VisualizationViewer.builder(graph)
            .viewSize(new Dimension(700, 700))
            .layoutAlgorithm(KKLayoutAlgorithm.<Integer>builder().build())
            .build();

    vv.setBackground(Color.white);

    // create a frame to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    content.add(vv.getComponent());
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    final DefaultModalGraphMouse<Integer, Number> gm = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(gm);

    Box controls = Box.createHorizontalBox();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    controls.add(Box.createGlue());
    controls.add(ControlHelpers.getCenteredContainer("Mouse Mode", gm.getModeComboBox()));
    content.add(controls, BorderLayout.SOUTH);

    frame.pack();
    frame.setVisible(true);
  }

  Graph<Integer, Number> createGraph() {
    Graph<Integer, Number> graph =
        GraphTypeBuilder.<Integer, Number>forGraphType(DefaultGraphType.dag()).buildGraph();

    IntStream.rangeClosed(0, 10).forEach(graph::addVertex);
    graph.addEdge(0, 1, Math.random());
    graph.addEdge(3, 0, Math.random());
    graph.addEdge(0, 4, Math.random());
    graph.addEdge(4, 5, Math.random());
    graph.addEdge(5, 3, Math.random());
    graph.addEdge(2, 1, Math.random());
    graph.addEdge(4, 1, Math.random());
    graph.addEdge(8, 2, Math.random());
    graph.addEdge(3, 8, Math.random());
    graph.addEdge(6, 7, Math.random());
    graph.addEdge(7, 5, Math.random());
    graph.addEdge(0, 9, Math.random());
    graph.addEdge(9, 8, Math.random());
    graph.addEdge(7, 6, Math.random());
    graph.addEdge(6, 5, Math.random());
    graph.addEdge(4, 2, Math.random());
    graph.addEdge(5, 4, Math.random());
    graph.addEdge(4, 10, Math.random());
    graph.addEdge(10, 4, Math.random());

    return graph;
  }

  public static void main(String[] args) {
    new MinimalVisualization();
  }
}
