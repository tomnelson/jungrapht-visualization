/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.*;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.BaseVisualizationModel;
import org.jungrapht.visualization.GraphZoomScrollPane;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.ISOMLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.selection.MultiMutableSelectedState;
import org.jungrapht.visualization.selection.MutableSelectedState;

/**
 * Demonstrates a single graph with 2 layouts in 2 views. They share picking, transforms, and a
 * pluggable renderer
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class TwoModelDemo extends JPanel {

  /** the graph */
  Graph<String, Number> graph;

  /** the visual components and renderers for the graph */
  VisualizationViewer<String, Number> vv1;

  VisualizationViewer<String, Number> vv2;

  Dimension preferredSize = new Dimension(300, 300);

  /** create an instance of a simple graph in two views with controls to demo the zoom features. */
  public TwoModelDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    // both models will share one graph
    graph = TestGraphs.getOneComponentGraph();

    // create two layouts for the one graph, one layout for each model
    LayoutAlgorithm<String> layoutAlgorithm1 = FRLayoutAlgorithm.<String>builder().build();
    LayoutAlgorithm<String> layoutAlgorithm2 = ISOMLayoutAlgorithm.<String>builder().build();

    // create the two models, each with a different layout
    VisualizationModel<String, Number> vm1 =
        new BaseVisualizationModel<>(graph, layoutAlgorithm1, preferredSize);
    VisualizationModel<String, Number> vm2 =
        new BaseVisualizationModel<>(graph, layoutAlgorithm2, preferredSize);

    // create the two views, one for each model
    // they share the same renderer
    vv1 = new VisualizationViewer<>(vm1, preferredSize);
    vv2 = new VisualizationViewer<>(vm2, preferredSize);
    vv1.setRenderContext(vv2.getRenderContext());

    // share the model Function between the two models
    //        layoutTransformer = vv1.getLayoutTransformer();
    //        vv2.setLayoutTransformer(layoutTransformer);
    //
    //        // share the view Function between the two models
    //        vv2.setViewTransformer(vv1.getViewTransformer());

    vv2.getRenderContext()
        .setMultiLayerTransformer(vv1.getRenderContext().getMultiLayerTransformer());
    vv2.getRenderContext().getMultiLayerTransformer().addChangeListener(vv1);

    vv1.setBackground(Color.white);
    vv2.setBackground(Color.white);

    // share one MutableSelectedState between the two views
    MutableSelectedState<String> ps = new MultiMutableSelectedState<>();
    vv1.setSelectedNodeState(ps);
    vv2.setSelectedNodeState(ps);
    MutableSelectedState<Number> pes = new MultiMutableSelectedState<>();
    vv1.setSelectedEdgeState(pes);
    vv2.setSelectedEdgeState(pes);

    // set an edge paint function that will show picking for edges
    vv1.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv1.getSelectedEdgeState(), Color.black, Color.red));
    vv1.getRenderContext()
        .setNodeFillPaintFunction(
            new PickableElementPaintFunction<>(
                vv1.getSelectedNodeState(), Color.red, Color.yellow));
    // add default listeners for ToolTips
    vv1.setNodeToolTipFunction(Object::toString);
    vv2.setNodeToolTipFunction(Object::toString);

    JPanel panel = new JPanel(new GridLayout(1, 0));
    panel.add(new GraphZoomScrollPane(vv1));
    panel.add(new GraphZoomScrollPane(vv2));

    add(panel);

    // create a GraphMouse for each view
    final DefaultModalGraphMouse<String, Number> gm1 = new DefaultModalGraphMouse<>();

    DefaultModalGraphMouse<String, Number> gm2 = new DefaultModalGraphMouse<>();

    vv1.setGraphMouse(gm1);
    vv2.setGraphMouse(gm2);

    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    gm1.getModeComboBox().addItemListener(gm2.getModeListener());
    modePanel.add(gm1.getModeComboBox());

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls(vv1, "Zoom"));
    controls.add(modePanel);
    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new TwoModelDemo());
    f.pack();
    f.setVisible(true);
  }
}
