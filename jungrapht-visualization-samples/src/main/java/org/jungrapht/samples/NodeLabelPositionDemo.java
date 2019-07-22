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
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.BaseVisualizationModel;
import org.jungrapht.visualization.GraphZoomScrollPane;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.AbstractModalGraphMouse;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.renderers.Renderer.NodeLabel.Position;
import org.jungrapht.visualization.selection.MutableSelectedState;

/**
 * Demonstrates node label positioning controlled by the user. In the AUTO setting, labels are
 * placed according to which quadrant the node is in
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class NodeLabelPositionDemo extends JPanel {

  /** the graph */
  Graph<String, Number> graph;

  FRLayoutAlgorithm<String> graphLayoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Number> vv;

  ScalingControl scaler;

  /** create an instance of a simple graph with controls to demo the zoomand hyperbolic features. */
  public NodeLabelPositionDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    graphLayoutAlgorithm = FRLayoutAlgorithm.<String>builder().build();
    graphLayoutAlgorithm.setMaxIterations(1000);

    Dimension preferredSize = new Dimension(600, 600);

    final VisualizationModel<String, Number> visualizationModel =
        new BaseVisualizationModel<>(graph, graphLayoutAlgorithm, preferredSize);
    vv = new VisualizationViewer<>(visualizationModel, preferredSize);

    MutableSelectedState<String> ps = vv.getSelectedNodeState();
    MutableSelectedState<Number> pes = vv.getSelectedEdgeState();
    vv.getRenderContext()
        .setNodeFillPaintFunction(new PickableElementPaintFunction<>(ps, Color.red, Color.yellow));
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableElementPaintFunction<>(pes, Color.black, Color.cyan));
    vv.setBackground(Color.white);
    vv.getRenderContext().setNodeLabelPosition(Renderer.NodeLabel.Position.W);

    vv.getRenderContext().setNodeLabelFunction(n -> n);

    // add a listener for ToolTips
    vv.setNodeToolTipFunction(n -> n);

    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    add(gzsp);

    // the regular graph mouse for the normal view
    final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JPanel positionPanel = new JPanel();
    positionPanel.setBorder(BorderFactory.createTitledBorder("Label Position"));
    JMenuBar menubar = new JMenuBar();
    menubar.add(graphMouse.getModeMenu());
    gzsp.setCorner(menubar);
    JComboBox<Position> cb = new JComboBox<>();
    cb.addItem(Renderer.NodeLabel.Position.N);
    cb.addItem(Renderer.NodeLabel.Position.NE);
    cb.addItem(Renderer.NodeLabel.Position.E);
    cb.addItem(Renderer.NodeLabel.Position.SE);
    cb.addItem(Renderer.NodeLabel.Position.S);
    cb.addItem(Renderer.NodeLabel.Position.SW);
    cb.addItem(Renderer.NodeLabel.Position.W);
    cb.addItem(Renderer.NodeLabel.Position.NW);
    cb.addItem(Renderer.NodeLabel.Position.N);
    cb.addItem(Renderer.NodeLabel.Position.CNTR);
    cb.addItem(Renderer.NodeLabel.Position.AUTO);
    cb.addItemListener(
        e -> {
          Renderer.NodeLabel.Position position = (Renderer.NodeLabel.Position) e.getItem();
          vv.getRenderContext().setNodeLabelPosition(position);
          vv.repaint();
        });

    cb.setSelectedItem(Renderer.NodeLabel.Position.SE);
    positionPanel.add(cb);
    JPanel controls = new JPanel();
    JPanel zoomControls = new JPanel(new GridLayout(2, 1));
    zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
    zoomControls.add(plus);
    zoomControls.add(minus);

    controls.add(zoomControls);
    controls.add(positionPanel);
    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new NodeLabelPositionDemo());
    f.pack();
    f.setVisible(true);
  }
}
