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
import org.jungrapht.visualization.SatelliteVisualizationViewer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse.Mode;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.ISOMLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.util.helpers.ControlHelpers;

/**
 * Similar to the SatelliteViewDemo, but using JInternalFrame.
 *
 * @author Tom Nelson
 */
public class InternalFrameSatelliteViewDemo {

  static final String instructions =
      "<html>"
          + "<b><h2><center>Instructions for Mouse Listeners</center></h2></b>"
          + "<p>There are two modes, Transforming and Picking."
          + "<p>The modes are selected with a toggle button."
          + "<p><p><b>Transforming Mode:</b>"
          + "<ul>"
          + "<li>Mouse1+drag pans the graph"
          + "<li>Mouse1+Shift+drag rotates the graph"
          + "<li>Mouse1+CTRL(or Command)+drag shears the graph"
          + "</ul>"
          + "<b>Picking Mode:</b>"
          + "<ul>"
          + "<li>Mouse1 on a Vertex selects the vertex"
          + "<li>Mouse1 elsewhere unselects all Vertices"
          + "<li>Mouse1+Shift on a Vertex adds/removes Vertex selection"
          + "<li>Mouse1+drag on a Vertex moves all selected Vertices"
          + "<li>Mouse1+drag elsewhere selects Vertices in a region"
          + "<li>Mouse1+Shift+drag adds selection of Vertices in a new region"
          + "<li>Mouse1+CTRL on a Vertex selects the vertex and centers the display on it"
          + "</ul>"
          + "<b>Both Modes:</b>"
          + "<ul>"
          + "<li>Mousewheel scales the layout &gt; 1 and scales the view &lt; 1";

  /** the graph */
  Graph<String, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Number> vv;

  VisualizationViewer<String, Number> satellite;

  JInternalFrame dialog;

  JDesktopPane desktop;

  /** create an instance of a simple graph with controls to demo the zoom features. */
  public InternalFrameSatelliteViewDemo() {

    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    LayoutAlgorithm<String> layout = ISOMLayoutAlgorithm.<String>builder().build();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(layout)
            .viewSize(new Dimension(600, 600))
            .build();
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv.getSelectedEdgeState(), Color.black, Color.cyan));
    vv.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(
                vv.getSelectedVertexState(), Color.red, Color.yellow));

    // add my listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    final DefaultModalGraphMouse<String, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    satellite = SatelliteVisualizationViewer.builder(vv).viewSize(new Dimension(200, 200)).build();
    satellite
        .getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(
                satellite.getSelectedEdgeState(), Color.black, Color.cyan));
    satellite
        .getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(
                satellite.getSelectedVertexState(), Color.red, Color.yellow));

    ScalingControl satelliteScaler = new CrossoverScalingControl();
    satellite.scaleToLayout(satelliteScaler);

    JFrame frame = new JFrame();
    desktop = new JDesktopPane();
    Container content = frame.getContentPane();
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(desktop);
    content.add(panel);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    JInternalFrame vvFrame = new JInternalFrame();
    vvFrame.getContentPane().add(vv.getComponent());
    vvFrame.pack();
    vvFrame.setVisible(true); //necessary as of 1.3
    desktop.add(vvFrame);
    try {
      vvFrame.setSelected(true);
    } catch (java.beans.PropertyVetoException e) {
    }

    dialog = new JInternalFrame();
    desktop.add(dialog);
    content = dialog.getContentPane();

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton dismiss = new JButton("Dismiss");
    dismiss.addActionListener(e -> dialog.setVisible(false));

    JButton help = new JButton("Help");
    help.addActionListener(
        e ->
            JOptionPane.showInternalMessageDialog(
                dialog, instructions, "Instructions", JOptionPane.PLAIN_MESSAGE));
    Box controls = Box.createHorizontalBox();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    controls.add(dismiss);
    controls.add(help);
    content.add(satellite.getComponent());
    content.add(controls, BorderLayout.SOUTH);

    JButton zoomer = new JButton("Show Satellite View");
    zoomer.addActionListener(
        e -> {
          dialog.pack();
          dialog.setLocation(desktop.getWidth() - dialog.getWidth(), 0);
          dialog.show();
          try {
            dialog.setSelected(true);
          } catch (java.beans.PropertyVetoException ex) {
          }
        });

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(((ModalGraphMouse) satellite.getGraphMouse()).getModeListener());
    JPanel p = new JPanel();
    p.add(zoomer);
    p.add(modeBox);

    frame.getContentPane().add(p, BorderLayout.SOUTH);
    frame.setSize(800, 800);
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    new InternalFrameSatelliteViewDemo();
  }
}
