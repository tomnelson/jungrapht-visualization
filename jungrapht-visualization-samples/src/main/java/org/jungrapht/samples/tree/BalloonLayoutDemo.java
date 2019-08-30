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
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ModalLensGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.transform.HyperbolicTransformer;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.ViewLensSupport;
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
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
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

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    final JButton hyperView = new JButton("Hyperbolic View");
    hyperView.addActionListener(e -> hyperbolicViewSupport.activate());
    final JButton hyperLayout = new JButton("Hyperbolic Layout");
    hyperLayout.addActionListener(e -> hyperbolicSupport.activate());
    final JButton noLens = new JButton("No Lens");
    noLens.addActionListener(
        e -> {
          hyperbolicSupport.deactivate();
          hyperbolicViewSupport.deactivate();
        });

    JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
    scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
    JPanel viewControls = new JPanel();
    viewControls.setLayout(new GridLayout(0, 1));

    JPanel controls = new JPanel();
    scaleGrid.add(plus);
    scaleGrid.add(minus);
    JPanel layoutControls = new JPanel();
    layoutControls.add(TreeLayoutSelector.builder(vv).after(vv::scaleToLayout).build());
    controls.add(layoutControls);
    controls.add(scaleGrid);
    controls.add(modeBox);
    viewControls.add(hyperView);
    viewControls.add(hyperLayout);
    viewControls.add(noLens);
    controls.add(viewControls);
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
