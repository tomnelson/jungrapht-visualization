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
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.selection.MultiMutableSelectedState;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.util.helpers.ControlHelpers;
import org.jungrapht.visualization.util.helpers.SpanningTreeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates a single graph with 3 layouts in 3 views. The first view is an undirected graph
 * using KKLayout The second view show a TreeLayout view of a MinimumSpanningTree of the first
 * graph. The third view shows the complete graph of the first view, using the layout positions of
 * the MinimumSpanningTree tree view.
 *
 * @author Tom Nelson
 */
public class MinimumSpanningTreeDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(MinimumSpanningTreeDemo.class);

  /** the graph */
  Graph<String, Integer> graph;

  Graph<String, Integer> tree;

  /** the visual components and renderers for the graph */
  VisualizationViewer<String, Integer> vv0;

  VisualizationViewer<String, Integer> vv1;
  VisualizationViewer<String, Integer> vv2;

  Dimension preferredSize = new Dimension(300, 300);
  Dimension preferredSizeRect = new Dimension(1100, 300);
  Dimension viewSizeRect = new Dimension(1100, 300);

  /** create an instance of a simple graph in two views with controls to demo the zoom features. */
  public MinimumSpanningTreeDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    // both models will share one graph
    graph = TestGraphs.getDemoGraph();

    tree = SpanningTreeAdapter.getSpanningTree(graph);

    LayoutAlgorithm<String> kkLayoutAlgorithm = KKLayoutAlgorithm.<String>builder().build();
    LayoutAlgorithm<String> treeLayoutAlgorithm = TreeLayoutAlgorithm.<String>builder().build();
    LayoutAlgorithm<String> staticLayoutAlgorithm = new StaticLayoutAlgorithm<>();

    // create the models, each with a different layout
    VisualizationModel<String, Integer> vm0 =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(kkLayoutAlgorithm)
            .layoutSize(preferredSize)
            .build();
    VisualizationModel<String, Integer> vm1 =
        VisualizationModel.builder(tree)
            .layoutAlgorithm(treeLayoutAlgorithm)
            .layoutSize(preferredSizeRect)
            .build();
    // initializer is the layout model for vm1
    // and the size is also set to the same size required for the Tree in treeLayoutAlgorithm
    VisualizationModel<String, Integer> vm2 =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(staticLayoutAlgorithm)
            .initializer(vm1.getLayoutModel())
            .layoutSize(vm1.getLayoutSize())
            .build();

    // create the two views, one for each model
    // they share the same renderer
    vv0 = VisualizationViewer.builder(vm0).viewSize(preferredSize).build();
    vv1 = VisualizationViewer.builder(vm1).viewSize(viewSizeRect).build();
    vv2 = VisualizationViewer.builder(vm2).viewSize(viewSizeRect).build();

    vv1.getRenderContext()
        .setMultiLayerTransformer(vv0.getRenderContext().getMultiLayerTransformer());
    vv2.getRenderContext()
        .setMultiLayerTransformer(vv0.getRenderContext().getMultiLayerTransformer());

    vv1.getRenderContext().setEdgeShapeFunction(EdgeShape.line());

    vv0.addChangeListener(vv1);
    vv1.addChangeListener(vv2);

    vv0.getRenderContext().setVertexLabelFunction(Object::toString);
    vv2.getRenderContext().setVertexLabelFunction(Object::toString);

    Color back = Color.decode("0xffffbb");
    vv0.setBackground(back);
    vv1.setBackground(back);
    vv2.setBackground(back);

    vv0.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv0.setForeground(Color.darkGray);
    vv1.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv1.setForeground(Color.darkGray);
    vv2.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv2.setForeground(Color.darkGray);

    // share one PickedState between the two views
    MutableSelectedState<String> ps = new MultiMutableSelectedState<>();
    vv0.setSelectedVertexState(ps);
    vv1.setSelectedVertexState(ps);
    vv2.setSelectedVertexState(ps);

    MutableSelectedState<Integer> pes = new MultiMutableSelectedState<>();
    vv0.setSelectedEdgeState(pes);
    vv1.setSelectedEdgeState(pes);
    vv2.setSelectedEdgeState(pes);

    // set an edge paint function that will show picking for edges
    vv0.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv0.getSelectedEdgeState(), Color.black, Color.red));
    vv0.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(
                vv0.getSelectedVertexState(), Color.red, Color.yellow));
    vv1.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv1.getSelectedEdgeState(), Color.black, Color.red));
    vv1.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(
                vv1.getSelectedVertexState(), Color.red, Color.yellow));

    // add default listeners for ToolTips
    vv0.setVertexToolTipFunction(Object::toString);
    vv1.setVertexToolTipFunction(Object::toString);
    vv2.setVertexToolTipFunction(Object::toString);

    vv0.setLayout(new BorderLayout());
    vv1.setLayout(new BorderLayout());
    vv2.setLayout(new BorderLayout());

    Font font = vv0.getFont().deriveFont(Font.BOLD, 16);
    JLabel vv0Label = new JLabel("<html>Original Graph<p>using KKLayout");
    vv0Label.setFont(font);
    JLabel vv1Label = new JLabel("Minimum Spanning Trees");
    vv1Label.setFont(font);
    JLabel vv2Label = new JLabel("Original Graph using TreeLayout");
    vv2Label.setFont(font);
    JPanel flow0 = new JPanel();
    flow0.setOpaque(false);
    JPanel flow1 = new JPanel();
    flow1.setOpaque(false);
    JPanel flow2 = new JPanel();
    flow2.setOpaque(false);
    flow0.add(vv0Label);
    flow1.add(vv1Label);
    flow2.add(vv2Label);
    vv0.add(flow0, BorderLayout.NORTH);
    vv1.add(flow1, BorderLayout.NORTH);
    vv2.add(flow2, BorderLayout.NORTH);

    JPanel grid = new JPanel(new GridLayout(0, 1));
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new VisualizationScrollPane(vv0), BorderLayout.WEST);
    grid.add(new VisualizationScrollPane(vv1));
    grid.add(new VisualizationScrollPane(vv2));
    panel.add(grid);

    add(panel);

    // create a GraphMouse for each view
    DefaultModalGraphMouse<String, Integer> gm0 = new DefaultModalGraphMouse<>();
    DefaultModalGraphMouse<String, Integer> gm1 = new DefaultModalGraphMouse<>();
    DefaultModalGraphMouse<String, Integer> gm2 = new DefaultModalGraphMouse<>();

    vv0.setGraphMouse(gm0);
    vv1.setGraphMouse(gm1);
    vv2.setGraphMouse(gm2);

    // create zoom buttons for scaling the Function that is
    // shared between the two models.
    final ScalingControl scaler = new CrossoverScalingControl();
    vv0.scaleToLayout(scaler);

    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    gm1.getModeComboBox().addItemListener(gm2.getModeListener());
    gm1.getModeComboBox().addItemListener(gm0.getModeListener());
    modePanel.add(gm1.getModeComboBox());

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv1));
    controls.add(modePanel);
    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new MinimumSpanningTreeDemo());
    f.pack();
    f.setVisible(true);
  }
}
