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
import org.jungrapht.visualization.renderers.Renderer.VertexLabel.Position;
import org.jungrapht.visualization.selection.MutableSelectedState;

/**
 * Demonstrates vertex label positioning controlled by the user. In the AUTO setting, labels are
 * placed according to which quadrant the vertex is in
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class VertexLabelPositionDemo extends JPanel {

  /** the graph */
  Graph<String, Number> graph;

  FRLayoutAlgorithm<String> graphLayoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Number> vv;

  ScalingControl scaler;

  /** create an instance of a simple graph with controls to demo the zoomand hyperbolic features. */
  public VertexLabelPositionDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    graphLayoutAlgorithm = FRLayoutAlgorithm.<String>builder().build();
    graphLayoutAlgorithm.setMaxIterations(1000);

    Dimension preferredSize = new Dimension(600, 600);

    final VisualizationModel<String, Number> visualizationModel =
        BaseVisualizationModel.builder(graph)
            .layoutAlgorithm(graphLayoutAlgorithm)
            .layoutSize(preferredSize)
            .build();
    vv = VisualizationViewer.builder(visualizationModel).viewSize(preferredSize).build();

    MutableSelectedState<String> ps = vv.getSelectedVertexState();
    MutableSelectedState<Number> pes = vv.getSelectedEdgeState();
    vv.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(ps, Color.red, Color.yellow));
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableElementPaintFunction<>(pes, Color.black, Color.cyan));
    vv.setBackground(Color.white);
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.W);

    vv.getRenderContext().setVertexLabelFunction(n -> n);

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(n -> n);

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
    cb.addItem(Renderer.VertexLabel.Position.N);
    cb.addItem(Renderer.VertexLabel.Position.NE);
    cb.addItem(Renderer.VertexLabel.Position.E);
    cb.addItem(Renderer.VertexLabel.Position.SE);
    cb.addItem(Renderer.VertexLabel.Position.S);
    cb.addItem(Renderer.VertexLabel.Position.SW);
    cb.addItem(Renderer.VertexLabel.Position.W);
    cb.addItem(Renderer.VertexLabel.Position.NW);
    cb.addItem(Renderer.VertexLabel.Position.N);
    cb.addItem(Renderer.VertexLabel.Position.CNTR);
    cb.addItem(Renderer.VertexLabel.Position.AUTO);
    cb.addItemListener(
        e -> {
          Renderer.VertexLabel.Position position = (Renderer.VertexLabel.Position) e.getItem();
          vv.getRenderContext().setVertexLabelPosition(position);
          vv.repaint();
        });

    cb.setSelectedItem(Renderer.VertexLabel.Position.SE);
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
    f.getContentPane().add(new VertexLabelPositionDemo());
    f.pack();
    f.setVisible(true);
  }
}
