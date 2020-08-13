/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples.tree;

import static org.jungrapht.samples.util.DemoTreeSupplier.createTreeTwo;

import java.awt.*;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.TitlePaintable;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.control.MultiSelectionStrategy;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates mulit-selection with arbitrary containing shape instead of a rectangle.<br>
 * CTRL-click and drag to trace a shape containing vertices to select.
 *
 * @author Tom Nelson
 */
public class ArbitraryShapeMultiSelectDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(ArbitraryShapeMultiSelectDemo.class);

  public ArbitraryShapeMultiSelectDemo() {

    setLayout(new BorderLayout());

    Graph<String, Integer> graph = createTreeTwo();

    final DefaultGraphMouse<String, Integer> graphMouse = new DefaultGraphMouse<>();

    VisualizationViewer<String, Integer> vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(new StaticLayoutAlgorithm<>())
            .multiSelectionStrategySupplier(() -> MultiSelectionStrategy.arbitrary())
            .viewSize(new Dimension(600, 600))
            .graphMouse(graphMouse)
            .build();
    vv.addPreRenderPaintable(
        new TitlePaintable(
            "Ctrl-MouseButton 1\nand drag to draw\nselection area", vv.getPreferredSize()));

    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setVertexLabelFunction(Object::toString);

    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);

    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelDrawPaintFunction(c -> Color.white);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    JPanel layoutPanel = new JPanel(new GridLayout(0, 1));
    layoutPanel.add(
        ControlHelpers.getCenteredContainer("Layouts", TreeLayoutSelector.builder(vv).build()));
    Box controls = Box.createHorizontalBox();
    controls.add(layoutPanel);
    controls.add(ControlHelpers.getCenteredContainer("Zoom", ControlHelpers.getZoomControls(vv)));
    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new ArbitraryShapeMultiSelectDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
