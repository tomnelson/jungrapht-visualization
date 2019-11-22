/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import static org.jungrapht.visualization.renderers.BiModalRenderer.HEAVYWEIGHT;

import java.awt.*;
import java.util.function.Function;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.renderers.GradientVertexRenderer;
import org.jungrapht.visualization.renderers.VertexLabelAsShapeRenderer;
import org.jungrapht.visualization.util.helpers.ControlHelpers;

/**
 * This demo shows how to use the vertex labels themselves as the vertex shapes. Additionally, it
 * shows html labels so they are multi-line, and gradient painting of the vertex labels.
 *
 * @author Tom Nelson
 */
public class VertexLabelAsShapeDemo extends JPanel {

  /** */
  private static final long serialVersionUID = 1017336668368978842L;

  Graph<String, Integer> graph;

  VisualizationViewer<String, Integer> vv;

  LayoutAlgorithm<String> layoutAlgorithm;

  /** create an instance of a simple graph with basic controls */
  public VertexLabelAsShapeDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    layoutAlgorithm = new FRLayoutAlgorithm<>();

    Dimension preferredSize = new Dimension(400, 400);
    final VisualizationModel<String, Integer> visualizationModel =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .layoutSize(preferredSize)
            .build();
    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv =
        VisualizationViewer.builder(visualizationModel)
            .graphMouse(graphMouse)
            .viewSize(preferredSize)
            .build();

    // this class will provide both label drawing and vertex shapes
    VertexLabelAsShapeRenderer<String, Integer> vlasr =
        new VertexLabelAsShapeRenderer<>(visualizationModel, vv.getRenderContext());

    // customize the render context
    vv.getRenderContext()
        .setVertexLabelFunction(
            ((Function<String, String>) Object::toString)
                .andThen(input -> "<html><center>Node<p>" + input));
    vv.getRenderContext().setVertexShapeFunction(vlasr);
    vv.getRenderContext().setEdgeDrawPaintFunction(e -> Color.yellow);
    vv.getRenderContext().setEdgeStrokeFunction(e -> new BasicStroke(2.5f));

    // customize the renderer
    vv.getRenderer()
        .setVertexRenderer(
            HEAVYWEIGHT, new GradientVertexRenderer<>(Color.gray, Color.white, true));
    vv.getRenderer().setVertexLabelRenderer(HEAVYWEIGHT, vlasr);

    vv.setBackground(Color.black);

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(n -> n);

    VisualizationScrollPane visualizationScrollPane = new VisualizationScrollPane(vv);
    add(visualizationScrollPane);

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    controls.add(ControlHelpers.getModeControls("Mouse Mode", vv));
    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new VertexLabelAsShapeDemo());
    f.pack();
    f.setVisible(true);
  }
}
