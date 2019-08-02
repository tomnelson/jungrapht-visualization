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
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.renderers.DefaultEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.DefaultVertexLabelRenderer;

/**
 * A demo that shows drawn Icons as vertices
 *
 * @author Tom Nelson
 */
public class DrawnIconVertexDemo {

  /** the graph */
  Graph<Integer, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Integer, Number> vv;

  public DrawnIconVertexDemo() {

    // create a simple graph for the demo
    graph = createGraph();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(FRLayoutAlgorithm.<Integer>builder().build())
            .viewSize(new Dimension(700, 700))
            .build();
    vv.getRenderContext().setVertexLabelFunction(v -> "Vertex " + v);

    vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));

    vv.getRenderContext()
        .setVertexIconFunction(
            v ->
                new Icon() {

                  public int getIconHeight() {
                    return 20;
                  }

                  public int getIconWidth() {
                    return 20;
                  }

                  public void paintIcon(Component c, Graphics g, int x, int y) {
                    if (vv.getSelectedVertexState().isSelected(v)) {
                      g.setColor(Color.yellow);
                    } else {
                      g.setColor(Color.red);
                    }
                    g.fillOval(x, y, 20, 20);
                    if (vv.getSelectedVertexState().isSelected(v)) {
                      g.setColor(Color.black);
                    } else {
                      g.setColor(Color.white);
                    }
                    g.drawString("" + v, x + 6, y + 15);
                  }
                });

    vv.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(
                vv.getSelectedVertexState(), Color.white, Color.yellow));
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(
                vv.getSelectedEdgeState(), Color.black, Color.lightGray));

    vv.setBackground(Color.white);

    // add my listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);

    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    content.add(panel);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    final DefaultModalGraphMouse<Integer, Number> gm = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(gm);

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls(vv, ""));
    controls.add(gm.getModeComboBox());
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
    new DrawnIconVertexDemo();
  }
}
