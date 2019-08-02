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
import java.awt.event.ItemEvent;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.DefaultVisualizationModel;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalLensGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.HyperbolicTransformer;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.ViewLensSupport;

/**
 * Shows a RadialTreeLayout view of a Forest. A hyperbolic projection lens may also be applied to
 * the view
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class RadialTreeLensDemo extends JPanel {

  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  /** provides a Hyperbolic lens for the view */
  LensSupport hyperbolicViewSupport;

  LensSupport hyperbolicLayoutSupport;

  /** create an instance of a simple graph with controls to demo the zoomand hyperbolic features. */
  public RadialTreeLensDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.createTreeTwo();

    Dimension preferredSize = new Dimension(600, 600);

    final VisualizationModel<String, Integer> visualizationModel =
        DefaultVisualizationModel.builder(graph)
            .layoutAlgorithm(new StaticLayoutAlgorithm())
            .layoutSize(preferredSize)
            .build();
    vv = VisualizationViewer.builder(visualizationModel).viewSize(preferredSize).build();

    MutableSelectedState<String> ps = vv.getSelectedVertexState();
    MutableSelectedState<Integer> pes = vv.getSelectedEdgeState();
    vv.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(ps, Color.red, Color.yellow));
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableElementPaintFunction<>(pes, Color.black, Color.cyan));
    vv.setBackground(Color.white);

    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);

    VisualizationScrollPane gzsp = new VisualizationScrollPane(vv);
    add(gzsp);

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());
    LayoutModel<String> layoutModel = vv.getModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());

    Lens lens = new Lens(d);
    hyperbolicViewSupport =
        new ViewLensSupport<>(
            vv,
            new HyperbolicShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)),
            new ModalLensGraphMouse());
    hyperbolicLayoutSupport =
        new LayoutLensSupport<>(
            vv,
            new HyperbolicTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)),
            new ModalLensGraphMouse());

    final JRadioButton hyperView = new JRadioButton("Hyperbolic View");
    hyperView.addItemListener(
        e -> hyperbolicViewSupport.activate(e.getStateChange() == ItemEvent.SELECTED));
    final JRadioButton hyperLayout = new JRadioButton("Hyperbolic Layout");
    hyperLayout.addItemListener(
        e -> hyperbolicLayoutSupport.activate(e.getStateChange() == ItemEvent.SELECTED));
    final JRadioButton noLens = new JRadioButton("No Lens");
    noLens.setSelected(true);

    ButtonGroup radio = new ButtonGroup();
    radio.add(hyperView);
    radio.add(hyperLayout);
    radio.add(noLens);

    graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(hyperbolicLayoutSupport.getGraphMouse().getModeListener());

    JMenuBar menubar = new JMenuBar();
    menubar.add(graphMouse.getModeMenu());
    gzsp.setCorner(menubar);

    JPanel controls = new JPanel(new GridLayout(1, 0));
    JPanel hyperControls = new JPanel(new GridLayout(3, 2));
    hyperControls.setBorder(BorderFactory.createTitledBorder("Examiner Lens"));
    JPanel modeControls = new JPanel();
    modeControls.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modeControls.add(graphMouse.getModeComboBox());
    hyperControls.add(hyperView);
    hyperControls.add(hyperLayout);
    hyperControls.add(noLens);

    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));
    controls.add(hyperControls);
    controls.add(modeControls);
    JPanel layoutControls = new JPanel(new GridLayout(0, 1));
    layoutControls.setBorder(BorderFactory.createTitledBorder("Layouts"));
    layoutControls.add(new TreeLayoutSelector<>(vv, 2));
    controls.add(layoutControls);
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
