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
import org.jgrapht.util.SupplierUtil;
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

  Graph<Integer, Integer> graph;

  VisualizationViewer<Integer, Integer> vv;

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

  Graph<Integer, Integer> createGraph() {
    Graph<Integer, Integer> graph =
        GraphTypeBuilder.<Integer, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    IntStream.rangeClosed(0, 10).forEach(graph::addVertex);
    graph.addEdge(0, 1);
    graph.addEdge(3, 0);
    graph.addEdge(0, 4);
    graph.addEdge(4, 5);
    graph.addEdge(5, 3);
    graph.addEdge(2, 1);
    graph.addEdge(4, 1);
    graph.addEdge(8, 2);
    graph.addEdge(3, 8);
    graph.addEdge(6, 7);
    graph.addEdge(7, 5);
    graph.addEdge(0, 9);
    graph.addEdge(9, 8);
    graph.addEdge(7, 6);
    graph.addEdge(6, 5);
    graph.addEdge(4, 2);
    graph.addEdge(5, 4);
    graph.addEdge(4, 10);
    graph.addEdge(10, 4);

    return graph;
  }

  public static void main(String[] args) {
    new MinimalVisualization();
  }
}
