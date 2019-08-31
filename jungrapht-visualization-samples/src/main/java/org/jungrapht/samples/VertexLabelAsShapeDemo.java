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
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.renderers.GradientVertexRenderer;
import org.jungrapht.visualization.renderers.VertexLabelAsShapeRenderer;

/**
 * This demo shows how to use the vertex labels themselves as the vertex shapes. Additionally, it
 * shows html labels so they are multi-line, and gradient painting of the vertex labels.
 *
 * @author Tom Nelson
 */
public class VertexLabelAsShapeDemo extends JPanel {

  /** */
  private static final long serialVersionUID = 1017336668368978842L;

  Graph<String, Number> graph;

  VisualizationViewer<String, Number> vv;

  LayoutAlgorithm<String> layoutAlgorithm;

  /** create an instance of a simple graph with basic controls */
  public VertexLabelAsShapeDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    layoutAlgorithm = FRLayoutAlgorithm.<String>builder().build();

    Dimension preferredSize = new Dimension(400, 400);
    final VisualizationModel<String, Number> visualizationModel =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .layoutSize(preferredSize)
            .build();
    vv = VisualizationViewer.builder(visualizationModel).viewSize(preferredSize).build();

    // this class will provide both label drawing and vertex shapes
    VertexLabelAsShapeRenderer<String, Number> vlasr =
        new VertexLabelAsShapeRenderer<>(visualizationModel, vv.getRenderContext());

    // customize the render context
    vv.getRenderContext()
        .setVertexLabelFunction(
            ((Function<String, String>) Object::toString)
                .andThen(input -> "<html><center>Node<p>" + input));
    vv.getRenderContext().setVertexShapeFunction(vlasr);
    //    vv.getRenderContext().setVertexLabelRenderer(new JLabelVertexLabelRenderer(Color.red));
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

    final DefaultModalGraphMouse<String, Number> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    VisualizationScrollPane visualizationScrollPane = new VisualizationScrollPane(vv);
    add(visualizationScrollPane);

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JPanel controls = new JPanel();
    JPanel zoomControls = new JPanel(new GridLayout(2, 1));
    zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
    zoomControls.add(plus);
    zoomControls.add(minus);
    controls.add(zoomControls);
    controls.add(modeBox);
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
