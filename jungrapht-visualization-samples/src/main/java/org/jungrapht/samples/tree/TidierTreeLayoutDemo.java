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
import java.util.function.Function;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.TidierTreeLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.util.AWT;
import org.jungrapht.visualization.util.LayoutPaintable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonsrates TreeLayout and RadialTreeLayout.
 *
 * @author Tom Nelson
 */
public class TidierTreeLayoutDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(TidierTreeLayoutDemo.class);
  Graph<String, Integer> graph;

  VisualizationViewer<String, Integer> vv;

  public TidierTreeLayoutDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.createForest();

    vv = VisualizationViewer.builder(graph).viewSize(new Dimension(600, 600)).build();
    vv.setGraphMouse(
        new DefaultGraphMouse()); // after VisualizationViewer is loaded so that properties are loaded
    Function<String, Shape> vertexShapeFunction = vv.getRenderContext().getVertexShapeFunction();
    TidierTreeLayoutAlgorithm<String, Integer> layoutAlgorithm =
        TidierTreeLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .vertexBoundsFunction(vertexShapeFunction.andThen(s -> AWT.convert(s.getBounds2D())))
            .build();

    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelDrawPaintFunction(n -> Color.white);
    vv.getRenderContext().setVertexShapeFunction(vertexShapeFunction);
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);
    if (log.isTraceEnabled()) {
      vv.addPreRenderPaintable(
          new LayoutPaintable.LayoutBounds(
              vv.getVisualizationModel(), vv.getRenderContext().getMultiLayerTransformer()));
    }
    vv.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm);
    vv.scaleToLayout();
    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    // temporary feature to draw the layout bounds in the viewer
    //    vv.addPreRenderPaintable(
    //        new LayoutPaintable.LayoutBounds(
    //            vv.getVisualizationModel(), vv.getRenderContext().getMultiLayerTransformer()));

    JPanel layoutPanel = new JPanel(new GridLayout(0, 1));
    layoutPanel.add(
        TreeLayoutSelector.<String, Integer>builder(vv)
            .vertexShapeFunction(vertexShapeFunction)
            .initialSelection(1)
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
    JPanel demo = new TidierTreeLayoutDemo();
    content.add(demo);
    frame.pack();
    frame.setVisible(true);
    log.trace("frame width {}", frame.getWidth());
    log.trace("demo width {}", demo.getWidth());
  }
}
