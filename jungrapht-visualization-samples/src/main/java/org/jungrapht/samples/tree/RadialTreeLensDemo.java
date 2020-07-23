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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.samples.util.LensControlHelper;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.control.DefaultLensGraphMouse;
import org.jungrapht.visualization.control.LensGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.HyperbolicTransformer;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.ViewLensSupport;

/**
 * Shows a RadialTreeLayout view of a Forest. A hyperbolic projection lens may also be applied to
 * the view
 *
 * @author Tom Nelson
 */
public class RadialTreeLensDemo extends JPanel {

  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  /** provides a Hyperbolic lens for the view */
  LensSupport<LensGraphMouse> hyperbolicViewSupport;

  LensSupport<LensGraphMouse> hyperbolicLayoutSupport;

  /** create an instance of a simple graph with controls to demo the zoomand hyperbolic features. */
  public RadialTreeLensDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.createTreeTwo();

    Dimension preferredSize = new Dimension(600, 600);

    final VisualizationModel<String, Integer> visualizationModel =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(new StaticLayoutAlgorithm())
            .layoutSize(preferredSize)
            .build();
    final DefaultGraphMouse<String, Integer> graphMouse = new DefaultGraphMouse<>();

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
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableElementPaintFunction<>(pes, Color.black, Color.cyan));

    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);

    VisualizationScrollPane visualizationScrollPane = new VisualizationScrollPane(vv);
    add(visualizationScrollPane);

    LayoutModel<String> layoutModel = vv.getVisualizationModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());

    hyperbolicViewSupport =
        ViewLensSupport.builder(vv)
            .lensTransformer(
                HyperbolicShapeTransformer.builder(d)
                    .delegate(
                        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW))
                    .build())
            .lensGraphMouse(new DefaultLensGraphMouse())
            .useGradient(true)
            .build();

    hyperbolicLayoutSupport =
        LayoutLensSupport.builder(vv)
            .lensTransformer(
                HyperbolicTransformer.builder(d)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(Layer.LAYOUT))
                    .build())
            .lensGraphMouse(new DefaultLensGraphMouse())
            .useGradient(true)
            .build();

    final JButton hyperView = new JButton("Hyperbolic View");
    hyperView.addActionListener(
        e -> {
          hyperbolicLayoutSupport.deactivate();
          hyperbolicViewSupport.activate(true);
        });
    final JButton hyperLayout = new JButton("Hyperbolic Layout");
    hyperLayout.addActionListener(
        e -> {
          hyperbolicViewSupport.deactivate();
          hyperbolicLayoutSupport.activate(true);
        });
    final JButton noLens = new JButton("No Lens");
    noLens.addActionListener(
        e -> {
          hyperbolicLayoutSupport.deactivate();
          hyperbolicViewSupport.deactivate();
        });
    noLens.setSelected(true);

    ButtonGroup radio = new ButtonGroup();
    radio.add(hyperView);
    radio.add(hyperLayout);
    radio.add(noLens);

    JMenuBar menubar = new JMenuBar();
    visualizationScrollPane.setCorner(menubar);

    Box controls = Box.createHorizontalBox();

    JComponent lensBox =
        LensControlHelper.builder(
                Map.of(
                    "Hyperbolic View", hyperbolicViewSupport,
                    "Hyperbolic Layout", hyperbolicLayoutSupport))
            .containerSupplier(Box::createVerticalBox)
            .build()
            .container();

    controls.add(ControlHelpers.getZoomControls("Scale", vv));
    controls.add(ControlHelpers.getCenteredContainer("Lens Controls", lensBox));
    JPanel layoutControls = new JPanel(new GridLayout(0, 1));
    layoutControls.add(TreeLayoutSelector.builder(vv).initialSelection(7).build());
    controls.add(ControlHelpers.getCenteredContainer("Layouts", layoutControls));
    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new RadialTreeLensDemo());
    f.pack();
    f.setVisible(true);
  }
}
