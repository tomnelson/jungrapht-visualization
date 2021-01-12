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
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.modal.ModeControls;
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
public class TwoModelDemo extends JPanel {

  /** the graph */
  Graph<String, Integer> graph;

  /** the visual components and renderers for the graph */
  VisualizationViewer<String, Integer> vv1;

  VisualizationViewer<String, Integer> vv2;

  Dimension preferredSize = new Dimension(300, 300);

  /** create an instance of a simple graph in two views with controls to demo the zoom features. */
  public TwoModelDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    // both models will share one graph
    graph = TestGraphs.getOneComponentGraph();

    // create two layouts for the one graph, one layout for each model
    LayoutAlgorithm<String> layoutAlgorithm1 = new FRLayoutAlgorithm<>();
    LayoutAlgorithm<String> layoutAlgorithm2 = new ISOMLayoutAlgorithm<>();

    // create the two models, each with a different layout
    VisualizationModel<String, Integer> vm1 =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(layoutAlgorithm1)
            .layoutSize(preferredSize)
            .build();
    VisualizationModel<String, Integer> vm2 =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(layoutAlgorithm2)
            .layoutSize(preferredSize)
            .build();

    // create a GraphMouse for each view
    final DefaultModalGraphMouse<String, Integer> gm1 = new DefaultModalGraphMouse<>();
    final DefaultModalGraphMouse<String, Integer> gm2 = new DefaultModalGraphMouse<>();

    // create the two views, one for each model
    // they share the same renderer
    vv1 = VisualizationViewer.builder(vm1).graphMouse(gm1).viewSize(preferredSize).build();
    vv2 = VisualizationViewer.builder(vm2).graphMouse(gm2).viewSize(preferredSize).build();
    vv1.setRenderContext(vv2.getRenderContext());

    vv2.getRenderContext()
        .setMultiLayerTransformer(vv1.getRenderContext().getMultiLayerTransformer());
    vv2.getRenderContext().getMultiLayerTransformer().addChangeListener(vv1);

    // share one MutableSelectedState between the two views
    MutableSelectedState<String> ps = new MultiMutableSelectedState<>();
    vv1.setSelectedVertexState(ps);
    vv2.setSelectedVertexState(ps);
    MutableSelectedState<Integer> pes = new MultiMutableSelectedState<>();
    vv1.setSelectedEdgeState(pes);
    vv2.setSelectedEdgeState(pes);

    // set an edge paint function that will show picking for edges
    vv1.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv1.getSelectedEdgeState(), Color.black, Color.red));
    vv1.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(
                vv1.getSelectedVertexState(), Color.red, Color.yellow));
    // add default listeners for ToolTips
    vv1.setVertexToolTipFunction(Object::toString);
    vv2.setVertexToolTipFunction(Object::toString);

    JPanel panel = new JPanel(new GridLayout(1, 0));
    panel.add(new VisualizationScrollPane(vv1));
    panel.add(new VisualizationScrollPane(vv2));

    add(panel);

    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(ModeControls.getStandardModeComboBox(gm1, gm2));

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv1));
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
