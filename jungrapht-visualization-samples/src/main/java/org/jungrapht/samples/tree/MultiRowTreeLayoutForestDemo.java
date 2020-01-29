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
import java.util.Map;
import java.util.function.Predicate;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
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
import org.jungrapht.visualization.util.helpers.ControlHelpers;
import org.jungrapht.visualization.util.helpers.LensControlHelper;
import org.jungrapht.visualization.util.helpers.TreeLayoutSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates the visualization of a Forest using TreeLayout and BalloonLayout. An examiner lens
 * performing a hyperbolic transformation of the view is also included.
 *
 * @author Tom Nelson
 */
public class MultiRowTreeLayoutForestDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(MultiRowTreeLayoutForestDemo.class);

  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  /** provides a Hyperbolic lens for the view */
  LensSupport<LensGraphMouse> hyperbolicViewSupport;

  LensSupport<LensGraphMouse> hyperbolicSupport;

  Dimension layoutSize = new Dimension(900, 900);
  Dimension viewSize = new Dimension(600, 600);

  public MultiRowTreeLayoutForestDemo() {
    setLayout(new BorderLayout());
    graph = DemoTreeSupplier.createForest2();

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

    Predicate<Integer> edgePredicate = e -> false;
    vv.addPreRenderPaintable(
        new LayoutPaintable.LayoutBounds(
            vv.getVisualizationModel(), vv.getRenderContext().getMultiLayerTransformer()));

    vv.getRenderContext().setEdgeLabelFunction(Object::toString);

    vv.getSelectedVertexState()
        .addItemListener(new SelectedState.StateChangeListener<>(this::selected, this::deselected));

    LayoutModel layoutModel = vv.getVisualizationModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
    Lens lens = new Lens();
    hyperbolicViewSupport =
        ViewLensSupport.builder(vv)
            .lensTransformer(
                HyperbolicShapeTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW))
                    .build())
            .lensGraphMouse(new DefaultLensGraphMouse())
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
            .build();

    vv.scaleToLayout();

    Box controls = Box.createHorizontalBox();
    TreeLayoutSelector<String, Integer> treeLayoutSelector =
        TreeLayoutSelector.<String, Integer>builder(vv)
            .edgePredicate(edgePredicate)
            .vertexPredicate(
                v ->
                    graph.incomingEdgesOf((String) v).stream().anyMatch(edgePredicate)
                        | graph.outgoingEdgesOf((String) v).stream().anyMatch(edgePredicate))
            .after(vv::scaleToLayout)
            .build();

    controls.add(ControlHelpers.getCenteredContainer("Layout Controls", treeLayoutSelector));
    controls.add(ControlHelpers.getZoomControls("Scale", vv));
    controls.add(
        LensControlHelper.builder(
                Map.of(
                    "Hyperbolic View", hyperbolicViewSupport,
                    "Hyperbolic Layout", hyperbolicSupport))
            .containerSupplier(Box::createVerticalBox)
            .title("Lens Controls")
            .build()
            .container());

    add(controls, BorderLayout.SOUTH);
  }

  private void selected(Object o) {
    log.debug("selected was {}", o);
  }

  private void deselected(Object o) {
    log.debug("deselected: {}", o);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new MultiRowTreeLayoutForestDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
