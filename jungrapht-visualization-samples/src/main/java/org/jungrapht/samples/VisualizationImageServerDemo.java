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
import org.jungrapht.visualization.VisualizationImageServer;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.renderers.GradientVertexRenderer;
import org.jungrapht.visualization.renderers.HeavyweightVertexLabelRenderer;
import org.jungrapht.visualization.renderers.Renderer;

/**
 * Demonstrates VisualizationImageServer.
 *
 * @author Tom Nelson
 */
public class VisualizationImageServerDemo {

  /** the graph */
  Graph<Integer, Double> graph;

  /** the visual component and renderer for the graph */
  VisualizationImageServer<Integer, Double> vv;

  /** */
  public VisualizationImageServerDemo() {

    // create a simple graph for the demo
    graph = createGraph();

    vv =
        VisualizationImageServer.builder(graph)
            .layoutAlgorithm(new KKLayoutAlgorithm<>())
            .viewSize(new Dimension(600, 600))
            .build();

    vv.getRenderer()
        .setVertexRenderer(
            new GradientVertexRenderer<>(
                vv.getSelectedVertexState(),
                Color.white,
                Color.red,
                Color.white,
                Color.blue,
                false));
    vv.getRenderContext().setEdgeDrawPaintFunction(e -> Color.lightGray);
    vv.getRenderContext().setArrowFillPaintFunction(e -> Color.lightGray);
    vv.getRenderContext().setArrowDrawPaintFunction(e -> Color.lightGray);

    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderer()
        .getVertexLabelRenderer()
        .setPositioner(new HeavyweightVertexLabelRenderer.InsidePositioner());
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.AUTO);

    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    Image im = vv.getFullImage();
    Icon icon = new ImageIcon(im);
    JLabel label = new JLabel(icon);
    content.add(label);
    frame.pack();
    frame.setVisible(true);
  }

  Graph<Integer, Double> createGraph() {
    Graph<Integer, Double> graph =
        GraphTypeBuilder.<Integer, Double>forGraphType(DefaultGraphType.dag()).buildGraph();
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
    new VisualizationImageServerDemo();
  }
}
