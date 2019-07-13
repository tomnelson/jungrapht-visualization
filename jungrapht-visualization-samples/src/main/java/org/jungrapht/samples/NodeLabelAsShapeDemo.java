/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.*;
import java.util.function.Function;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.BaseVisualizationModel;
import org.jungrapht.visualization.GraphZoomScrollPane;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.renderers.DefaultNodeLabelRenderer;
import org.jungrapht.visualization.renderers.GradientNodeRenderer;
import org.jungrapht.visualization.renderers.NodeLabelAsShapeRenderer;

/**
 * This demo shows how to use the node labels themselves as the node shapes. Additionally, it shows
 * html labels so they are multi-line, and gradient painting of the node labels.
 *
 * @author Tom Nelson
 */
public class NodeLabelAsShapeDemo extends JPanel {

  /** */
  private static final long serialVersionUID = 1017336668368978842L;

  Graph<String, Number> graph;

  VisualizationViewer<String, Number> vv;

  LayoutAlgorithm<String> layoutAlgorithm;

  /** create an instance of a simple graph with basic controls */
  public NodeLabelAsShapeDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    layoutAlgorithm = FRLayoutAlgorithm.<String>builder().build();

    Dimension preferredSize = new Dimension(400, 400);
    final VisualizationModel<String, Number> visualizationModel =
        new BaseVisualizationModel<>(graph, layoutAlgorithm, preferredSize);
    vv = new VisualizationViewer<>(visualizationModel, preferredSize);

    // this class will provide both label drawing and node shapes
    NodeLabelAsShapeRenderer<String, Number> vlasr =
        new NodeLabelAsShapeRenderer<>(visualizationModel, vv.getRenderContext());

    // customize the render context
    vv.getRenderContext()
        .setNodeLabelFunction(
            ((Function<String, String>) Object::toString)
                .andThen(input -> "<html><center>Node<p>" + input));
    vv.getRenderContext().setNodeShapeFunction(vlasr);
    vv.getRenderContext().setNodeLabelRenderer(new DefaultNodeLabelRenderer(Color.red));
    vv.getRenderContext().setEdgeDrawPaintFunction(e -> Color.yellow);
    vv.getRenderContext().setEdgeStrokeFunction(e -> new BasicStroke(2.5f));

    // customize the renderer
    vv.getRenderer().setNodeRenderer(new GradientNodeRenderer<>(vv, Color.gray, Color.white, true));
    vv.getRenderer().setNodeLabelRenderer(vlasr);

    vv.setBackground(Color.black);

    // add a listener for ToolTips
    vv.setNodeToolTipFunction(n -> n);

    final DefaultModalGraphMouse<String, Number> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    add(gzsp);

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
    f.getContentPane().add(new NodeLabelAsShapeDemo());
    f.pack();
    f.setVisible(true);
  }
}
