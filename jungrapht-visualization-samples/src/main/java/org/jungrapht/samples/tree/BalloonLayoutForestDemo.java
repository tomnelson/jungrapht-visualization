/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples.tree;

import java.awt.*;
import java.util.Map;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.samples.util.LensControlHelper;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.control.DefaultLensGraphMouse;
import org.jungrapht.visualization.control.LensGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.LayoutPaintable;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.selection.SelectedState;
import org.jungrapht.visualization.transform.HyperbolicTransformer;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.ViewLensSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates the visualization of a Forest using several layout algorithms. An examiner lens
 * performing a hyperbolic transformation of the view is also included.
 *
 * @author Tom Nelson
 */
public class BalloonLayoutForestDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(BalloonLayoutForestDemo.class);

  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  /** provides a Hyperbolic lens for the view */
  LensSupport<LensGraphMouse> hyperbolicViewSupport;

  LensSupport<LensGraphMouse> hyperbolicSupport;

  Dimension layoutSize = new Dimension(600, 600);
  Dimension viewSize = new Dimension(600, 600);

  public BalloonLayoutForestDemo() {
    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.createForest();
    final DefaultGraphMouse<String, Integer> graphMouse = new DefaultGraphMouse<>();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(new StaticLayoutAlgorithm<>())
            .layoutSize(layoutSize)
            .viewSize(viewSize)
            .graphMouse(graphMouse)
            .build();

    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(a -> Color.lightGray);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    vv.addPreRenderPaintable(
        new LayoutPaintable.LayoutBounds(
            vv.getVisualizationModel(), vv.getRenderContext().getMultiLayerTransformer()));

    vv.getSelectedVertexState()
        .addItemListener(new SelectedState.StateChangeListener<>(this::selected, this::deselected));

    LayoutModel layoutModel = vv.getVisualizationModel().getLayoutModel();
    Lens lens = new Lens();
    hyperbolicViewSupport =
        ViewLensSupport.builder(vv)
            .lensTransformer(
                HyperbolicShapeTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW))
                    .build())
            .lensGraphMouse(new DefaultLensGraphMouse())
            .useGradient(true)
            .build();

    hyperbolicSupport =
        LayoutLensSupport.builder(vv)
            .lensTransformer(
                HyperbolicTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(Layer.LAYOUT))
                    .build())
            .lensGraphMouse(new DefaultLensGraphMouse())
            .useGradient(true)
            .build();

    vv.scaleToLayout(new CrossoverScalingControl());

    JComponent lensBox =
        LensControlHelper.builder(
                Map.of(
                    "Hyperbolic View", hyperbolicViewSupport,
                    "Hyperbolic Layout", hyperbolicSupport))
            .containerSupplier(Box::createVerticalBox)
            .build()
            .container();

    Box controls = Box.createHorizontalBox();
    controls.add(
        ControlHelpers.getCenteredContainer(
            "Layout Controls", TreeLayoutSelector.builder(vv).initialSelection(6).build()));
    controls.add(ControlHelpers.getCenteredContainer("Lens Controls", lensBox));
    add(controls, BorderLayout.SOUTH);
  }

  private void selected(Object o) {
    log.info("selected was {}", o);
  }

  private void deselected(Object o) {
    log.info("deselected: {}", o);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new BalloonLayoutForestDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
