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
import java.net.URL;
import java.util.function.Function;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse.Mode;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.renderers.JLabelEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.JLabelVertexLabelRenderer;
import org.jungrapht.visualization.util.helpers.ControlHelpers;

/**
 * Demonstrates the use of images on graph edge labels.
 *
 * @author Tom Nelson
 */
public class ImageEdgeLabelDemo extends JPanel {

  /** */
  private static final long serialVersionUID = -4332663871914930864L;

  private static final int VERTEX_COUNT = 11;

  /** the graph */
  Graph<Number, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Number, Number> vv;

  public ImageEdgeLabelDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = createGraph(VERTEX_COUNT);

    FRLayoutAlgorithm<Number> layoutAlgorithm = new FRLayoutAlgorithm<>();
    layoutAlgorithm.setMaxIterations(100);

    final DefaultModalGraphMouse<Number, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv =
        VisualizationViewer.builder(graph)
            .graphMouse(graphMouse)
            .layoutAlgorithm(layoutAlgorithm)
            .viewSize(new Dimension(600, 600))
            .build();

    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv.getSelectedEdgeState(), Color.black, Color.cyan));

    vv.getRenderContext().setVertexLabelRenderer(new JLabelVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new JLabelEdgeLabelRenderer(Color.cyan));
    vv.getRenderContext()
        .setEdgeLabelFunction(
            new Function<>() {
              URL url = getClass().getResource("/images/lightning-s.gif");

              public String apply(Number input) {
                return "<html><img src=" + url + " height=10 width=21>";
              }
            });

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.setEdgeToolTipFunction(Object::toString);
    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(modeBox);

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    controls.add(modePanel);
    add(controls, BorderLayout.SOUTH);
  }

  /**
   * create some vertices
   *
   * @param vertexCount how many to create
   * @return the Vertices in an array
   */
  private Graph<Number, Number> createGraph(int vertexCount) {
    Graph<Number, Number> graph =
        GraphTypeBuilder.<Number, Number>forGraphType(DefaultGraphType.directedMultigraph())
            .buildGraph();

    for (int i = 0; i < vertexCount; i++) {
      graph.addVertex(i);
    }
    int j = 0;
    graph.addEdge(0, 1, j++);
    graph.addEdge(3, 0, j++);
    graph.addEdge(0, 4, j++);
    graph.addEdge(4, 5, j++);
    graph.addEdge(5, 3, j++);
    graph.addEdge(2, 1, j++);
    graph.addEdge(4, 1, j++);
    graph.addEdge(8, 2, j++);
    graph.addEdge(3, 8, j++);
    graph.addEdge(6, 7, j++);
    graph.addEdge(7, 5, j++);
    graph.addEdge(0, 9, j++);
    graph.addEdge(9, 8, j++);
    graph.addEdge(7, 6, j++);
    graph.addEdge(6, 5, j++);
    graph.addEdge(4, 2, j++);
    graph.addEdge(5, 4, j++);
    graph.addEdge(4, 10, j++);
    graph.addEdge(10, 4, j++);

    return graph;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new ImageEdgeLabelDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
