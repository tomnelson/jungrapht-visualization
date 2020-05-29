/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
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
public class VisualizationImageServerLargeDemo {

  /** the graph */
  Graph<Integer, Double> graph;

  private static final int size = 10000;

  /** the visual component and renderer for the graph */
  VisualizationImageServer<Integer, Double> vv;

  /** */
  public VisualizationImageServerLargeDemo() {

    // create a simple graph for the demo
    graph = createGraph();

    vv =
        VisualizationImageServer.builder(graph)
            .layoutAlgorithm(new KKLayoutAlgorithm<>())
            .viewSize(new Dimension(size, size))
            .layoutSize(new Dimension(size, size))
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
    vv.getRenderContext().setVertexShapeFunction(v -> new Ellipse2D.Double(-100, -100, 200, 200));

    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    Image im = vv.getImage(new Point2D.Double(size / 2, size / 2), new Dimension(size, size));
    Icon icon = new ImageIcon(im);
    JLabel label = new JLabel(icon);
    content.add(label);
    frame.pack();
    frame.setVisible(true);
    File outputfile = new File("/tmp/image.jpg");
    try {
      ImageIO.write((BufferedImage) im, "jpg", outputfile);
    } catch (IOException e) {
      e.printStackTrace();
    }
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
    new VisualizationImageServerLargeDemo();
  }
}
