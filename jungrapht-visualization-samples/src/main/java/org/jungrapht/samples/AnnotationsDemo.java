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
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.annotations.AnnotatingGraphMousePlugin;
import org.jungrapht.visualization.annotations.AnnotatingModalGraphMouse;
import org.jungrapht.visualization.annotations.AnnotationControls;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse.Mode;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.util.helpers.ControlHelpers;

/**
 * Demonstrates annotation of graph elements.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class AnnotationsDemo extends JPanel {

  static final String instructions =
      "<html>"
          + "<b><h2><center>Instructions for Annotations</center></h2></b>"
          + "<p>The Annotation Controls allow you to select:"
          + "<ul>"
          + "<li>Shape"
          + "<li>Color"
          + "<li>Fill (or outline)"
          + "<li>Above or below (UPPER/LOWER) the graph display"
          + "</ul>"
          + "<p>Mouse Button one press starts a Shape,"
          + "<p>drag and release to complete."
          + "<p>Mouse Button three pops up an input dialog"
          + "<p>for text. This will create a text annotation."
          + "<p>You may use html for multi-line, etc."
          + "<p>You may even use an image tag and image url"
          + "<p>to put an image in the annotation."
          + "<p><p>"
          + "<p>To remove an annotation, shift-click on it"
          + "<p>in the Annotations mode."
          + "<p>If there is overlap, the Annotation with center"
          + "<p>closest to the mouse point will be removed.";

  JDialog helpDialog;

  /** create an instance of a simple graph in two views with controls to demo the features. */
  public AnnotationsDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    Graph<String, Integer> graph = TestGraphs.getOneComponentGraph();

    // the preferred sizes for the two views
    Dimension preferredSize1 = new Dimension(600, 600);

    // create one layout for the graph
    FRLayoutAlgorithm<String> layoutAlgorithm = FRLayoutAlgorithm.<String>builder().build();
    layoutAlgorithm.setMaxIterations(500);

    VisualizationModel<String, Integer> vm =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .layoutSize(preferredSize1)
            .build();

    final VisualizationViewer<String, Integer> vv =
        VisualizationViewer.builder(vm).viewSize(preferredSize1).build();

    vv.setBackground(Color.white);
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);

    // add default listener for ToolTips
    vv.setVertexToolTipFunction(n -> n);

    Container panel = new JPanel(new BorderLayout());

    VisualizationScrollPane visualizationScrollPane = new VisualizationScrollPane(vv);
    panel.add(visualizationScrollPane);

    helpDialog = new JDialog();
    helpDialog.getContentPane().add(new JLabel(instructions));

    RenderContext<String, Integer> rc = vv.getRenderContext();
    AnnotatingGraphMousePlugin<String, Integer> annotatingPlugin =
        new AnnotatingGraphMousePlugin<>(rc);

    final AnnotatingModalGraphMouse<String, Integer> graphMouse =
        new AnnotatingModalGraphMouse<>(rc, annotatingPlugin);
    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    modeBox.setSelectedItem(ModalGraphMouse.Mode.ANNOTATING);

    JButton help = new JButton("Help");
    help.addActionListener(
        e -> {
          helpDialog.pack();
          helpDialog.setVisible(true);
        });

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls("Scale", vv));

    JPanel modeControls = new JPanel();
    modeControls.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modeControls.add(graphMouse.getModeComboBox());
    controls.add(modeControls);

    JPanel annotationControlPanel = new JPanel();
    annotationControlPanel.setBorder(BorderFactory.createTitledBorder("Annotation Controls"));

    AnnotationControls<String, Integer> annotationControls =
        new AnnotationControls<>(annotatingPlugin);

    annotationControlPanel.add(annotationControls.getAnnotationsToolBar());
    controls.add(annotationControlPanel);

    JPanel helpControls = new JPanel();
    helpControls.setBorder(BorderFactory.createTitledBorder("Help"));
    helpControls.add(help);
    controls.add(helpControls);
    add(panel);
    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new AnnotationsDemo());
    f.pack();
    f.setVisible(true);
  }
}
