/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples.tree;

import com.google.common.collect.ImmutableSortedMap;
import java.awt.*;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ModalLensGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
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
 * Demonstrates the visualization of a Tree using TreeLayout and BalloonLayout. An examiner lens
 * performing a hyperbolic transformation of the view is also included.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class BalloonLayoutDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(BalloonLayoutDemo.class);

  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  /** provides a Hyperbolic lens for the view */
  LensSupport<ModalLensGraphMouse> hyperbolicViewSupport;

  LensSupport<ModalLensGraphMouse> hyperbolicSupport;

  public BalloonLayoutDemo() {
    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.createTreeTwo();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(new StaticLayoutAlgorithm<>())
            .layoutSize(new Dimension(900, 900))
            .viewSize(new Dimension(600, 600))
            .build();

    vv.setBackground(Color.white);
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(a -> Color.black);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());
    LayoutModel layoutModel = vv.getVisualizationModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
    Lens lens = new Lens(d);
    hyperbolicViewSupport =
        new ViewLensSupport<>(
            vv,
            new HyperbolicShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)),
            new ModalLensGraphMouse());
    hyperbolicSupport =
        new LayoutLensSupport<>(
            vv,
            new HyperbolicTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)),
            new ModalLensGraphMouse());

    graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(hyperbolicSupport.getGraphMouse().getModeListener());

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

    final ScalingControl scaler = new CrossoverScalingControl();

    vv.scaleToLayout(scaler);

    JComponent lensBox =
        LensControlHelper.with(
                Box.createVerticalBox(),
                ImmutableSortedMap.of(
                    "Hyperbolic View", hyperbolicViewSupport,
                    "Hyperbolic Layout", hyperbolicSupport))
            .container();

    Box controls = Box.createHorizontalBox();
    controls.add(
        ControlHelpers.getCenteredContainer(
            "Layout Controls", TreeLayoutSelector.builder(vv).after(vv::scaleToLayout).build()));
    controls.add(ControlHelpers.getCenteredContainer("Scale", ControlHelpers.getZoomControls(vv)));
    controls.add(
        ControlHelpers.getCenteredContainer("Mouse Mode", ControlHelpers.getModeRadio(graphMouse)));
    controls.add(ControlHelpers.getCenteredContainer("Lens Controls", lensBox));
    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {

    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new BalloonLayoutDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
