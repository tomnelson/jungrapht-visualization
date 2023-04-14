/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.SatelliteVisualizationViewer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.*;
import org.jungrapht.visualization.control.modal.ModeControls;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.GEMLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;

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
  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  VisualizationViewer<String, Integer> satellite;

  JInternalFrame dialog;

  JDesktopPane desktop;

  /** create an instance of a simple graph with controls to demo the zoom features. */
  public InternalFrameSatelliteViewDemo() {

    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    LayoutAlgorithm<String> layout = new GEMLayoutAlgorithm<>();

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv =
        VisualizationViewer.builder(graph)
            .graphMouse(graphMouse)
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
    satellite =
        SatelliteVisualizationViewer.builder(vv)
            .viewSize(new Dimension(200, 200))
            .graphMouse(new ModalSatelliteGraphMouse<>())
            .build();
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

    satellite.scaleToLayout();

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

    //    final ScalingControl scaler = new CrossoverScalingControl();

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

    JComboBox modeBox =
        ModeControls.getStandardModeComboBox(
            graphMouse, (ModalGraphMouse) satellite.getGraphMouse());

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
