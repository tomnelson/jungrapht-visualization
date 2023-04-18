/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.function.Function;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.util.AWT;
import org.jungrapht.visualization.util.LayoutPaintable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates TreeLayout and RadialTreeLayout.
 *
 * @author Tom Nelson
 */
public class TreeLayoutDemoOneVertex extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(TreeLayoutDemoOneVertex.class);
  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  public TreeLayoutDemoOneVertex() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.createTreeZero();
    int width = 20;
    int height = 20;
    Function<String, Shape> vertexShapeFunction =
        v -> new Ellipse2D.Double(-width / 2.f, -height / 2.f, width, height);

    final DefaultGraphMouse<String, Integer> graphMouse = new DefaultGraphMouse<>();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(
                TreeLayoutAlgorithm.<String>builder()
                    .vertexBoundsFunction(
                        vertexShapeFunction.andThen(s -> AWT.convert(s.getBounds2D())))
                    .build())
            .viewSize(new Dimension(600, 600))
            .graphMouse(graphMouse)
            .build();

    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelDrawPaintFunction(n -> Color.white);
    vv.getRenderContext().setVertexShapeFunction(vertexShapeFunction);
    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);
    if (log.isTraceEnabled()) {
      vv.addPreRenderPaintable(
          new LayoutPaintable.LayoutBounds(
              vv.getVisualizationModel(), vv.getRenderContext().getMultiLayerTransformer()));
    }

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    // temporary feature to draw the layout bounds in the viewer
    vv.addPreRenderPaintable(
        new LayoutPaintable.LayoutBounds(
            vv.getVisualizationModel(), vv.getRenderContext().getMultiLayerTransformer()));

    JPanel layoutPanel = new JPanel(new GridLayout(0, 1));
    layoutPanel.add(
        TreeLayoutSelector.<String, Integer>builder(vv)
            .vertexShapeFunction(vertexShapeFunction)
            .build());
    JPanel controls = new JPanel();
    controls.add(layoutPanel);
    controls.add(ControlHelpers.getZoomControls(vv));

    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    JPanel demo = new TreeLayoutDemoOneVertex();
    content.add(demo);
    frame.pack();
    frame.setVisible(true);
    log.trace("frame width {}", frame.getWidth());
    log.trace("demo width {}", demo.getWidth());
  }
}
