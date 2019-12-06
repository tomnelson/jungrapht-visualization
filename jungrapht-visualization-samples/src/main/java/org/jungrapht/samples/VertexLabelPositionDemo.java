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
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.AbstractModalGraphMouse;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.renderers.Renderer.VertexLabel.Position;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.util.helpers.ControlHelpers;

/**
 * Demonstrates vertex label positioning controlled by the user. In the AUTO setting, labels are
 * placed according to which quadrant the vertex is in
 *
 * @author Tom Nelson
 */
public class VertexLabelPositionDemo extends JPanel {

  /** the graph */
  Graph<String, Integer> graph;

  FRLayoutAlgorithm<String> graphLayoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  /** create an instance of a simple graph with controls to demo the zoomand hyperbolic features. */
  public VertexLabelPositionDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    graphLayoutAlgorithm = new FRLayoutAlgorithm<>();
    graphLayoutAlgorithm.setMaxIterations(1000);

    Dimension preferredSize = new Dimension(600, 600);

    final VisualizationModel<String, Integer> visualizationModel =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(graphLayoutAlgorithm)
            .layoutSize(preferredSize)
            .build();
    // the regular graph mouse for the normal view
    final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<>();

    vv =
        VisualizationViewer.builder(visualizationModel)
            .graphMouse(graphMouse)
            .viewSize(preferredSize)
            .build();

    MutableSelectedState<String> ps = vv.getSelectedVertexState();
    MutableSelectedState<Integer> pes = vv.getSelectedEdgeState();
    vv.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(ps, Color.red, Color.yellow));
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableElementPaintFunction<>(pes, Color.black, Color.cyan));

    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.W);

    vv.getRenderContext().setVertexLabelFunction(n -> n);

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(n -> n);

    VisualizationScrollPane visualizationScrollPane = new VisualizationScrollPane(vv);
    add(visualizationScrollPane);

    JPanel positionPanel = new JPanel();
    JMenuBar menubar = new JMenuBar();
    menubar.add(graphMouse.getModeMenu());
    visualizationScrollPane.setCorner(menubar);
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

    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    controls.add(ControlHelpers.getCenteredContainer("Label", positionPanel));
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
