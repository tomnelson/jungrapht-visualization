/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.*;
import org.jungrapht.visualization.control.modal.Modal;
import org.jungrapht.visualization.control.modal.ModeComboBox;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.selection.MultiMutableSelectedState;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.selection.ShapePickSupport;

/**
 * Demonstrates 3 views of one graph in one model with one layout. Each view uses a different
 * scaling graph mouse. This demo uses the ModalGraphMouse
 *
 * @author Tom Nelson
 */
public class MultiViewDemoModal extends JPanel {

  /** the graph */
  Graph<String, Integer> graph;

  /** the visual components and renderers for the graph */
  VisualizationViewer<String, Integer> vv1;

  VisualizationViewer<String, Integer> vv2;
  VisualizationViewer<String, Integer> vv3;

  Dimension preferredSize = new Dimension(400, 400);

  final String messageOne =
      "The mouse wheel will scale the model's layout when activated"
          + " in View 1. Since all three views share the same layout Function, all three views will"
          + " show the same scaling of the layout.";

  final String messageTwo =
      "The mouse wheel will scale the view when activated in"
          + " View 2. Since all three views share the same view Function, all three views will be affected.";

  final String messageThree =
      "   The mouse wheel uses a 'crossover' feature in View 3."
          + " When the combined layout and view scale is greater than '1', the model's layout will be scaled."
          + " Since all three views share the same layout Function, all three views will show the same "
          + " scaling of the layout.\n   When the combined scale is less than '1', the scaling function"
          + " crosses over to the view, and then, since all three views share the same view Function,"
          + " all three views will show the same scaling.";

  JTextArea textArea;
  JScrollPane scrollPane;

  /** create an instance of a simple graph in two views with controls to demo the zoom features. */
  public MultiViewDemoModal() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    // create one layout for the graph
    FRLayoutAlgorithm<String> layoutAlgorithm = new FRLayoutAlgorithm<>();
    layoutAlgorithm.setMaxIterations(1000);

    // create one model that all 3 views will share
    VisualizationModel<String, Integer> visualizationModel =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .layoutSize(preferredSize)
            .build();

    // create 3 views that share the same model
    vv1 = VisualizationViewer.builder(visualizationModel).viewSize(preferredSize).build();
    vv2 = VisualizationViewer.builder(visualizationModel).viewSize(preferredSize).build();
    vv3 = VisualizationViewer.builder(visualizationModel).viewSize(preferredSize).build();

    vv1.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv2.getRenderContext().setVertexShapeFunction(n -> new Rectangle2D.Float(-6, -6, 12, 12));

    vv2.getRenderContext().setEdgeShapeFunction(EdgeShape.quadCurve());

    vv3.getRenderContext().setEdgeShapeFunction(EdgeShape.cubicCurve());

    vv2.getRenderContext()
        .setMultiLayerTransformer(vv1.getRenderContext().getMultiLayerTransformer());
    vv3.getRenderContext()
        .setMultiLayerTransformer(vv1.getRenderContext().getMultiLayerTransformer());

    vv1.getRenderContext().getMultiLayerTransformer().addChangeListener(vv1);
    vv2.getRenderContext().getMultiLayerTransformer().addChangeListener(vv2);
    vv3.getRenderContext().getMultiLayerTransformer().addChangeListener(vv3);

    // create one pick support for all 3 views to share
    GraphElementAccessor<String, Integer> pickSupport = new ShapePickSupport<>(vv1);
    vv1.setPickSupport(pickSupport);
    vv2.setPickSupport(pickSupport);
    vv3.setPickSupport(pickSupport);

    // create one selected state for all 3 views to share
    MutableSelectedState<Integer> pes = new MultiMutableSelectedState<>();
    MutableSelectedState<String> pvs = new MultiMutableSelectedState<>();
    vv1.setSelectedVertexState(pvs);
    vv2.setSelectedVertexState(pvs);
    vv3.setSelectedVertexState(pvs);
    vv1.setSelectedEdgeState(pes);
    vv2.setSelectedEdgeState(pes);
    vv3.setSelectedEdgeState(pes);

    // set an edge paint function that shows selected edges
    vv1.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableElementPaintFunction<>(pes, Color.black, Color.red));
    vv2.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableElementPaintFunction<>(pes, Color.black, Color.red));
    vv3.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableElementPaintFunction<>(pes, Color.black, Color.red));
    vv1.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(pvs, Color.red, Color.yellow));
    vv2.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(pvs, Color.blue, Color.cyan));
    vv3.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(pvs, Color.red, Color.yellow));

    // add default listener for ToolTips
    vv1.setVertexToolTipFunction(Object::toString);
    vv2.setVertexToolTipFunction(Object::toString);
    vv3.setVertexToolTipFunction(Object::toString);

    JPanel panel = new JPanel(new GridLayout(1, 0));

    final JPanel p1 = new JPanel(new BorderLayout());
    final JPanel p2 = new JPanel(new BorderLayout());
    final JPanel p3 = new JPanel(new BorderLayout());

    p1.add(new VisualizationScrollPane(vv1));
    p2.add(new VisualizationScrollPane(vv2));
    p3.add(new VisualizationScrollPane(vv3));

    JButton h1 = new JButton("?");
    h1.addActionListener(
        e -> {
          textArea.setText(messageOne);
          JOptionPane.showMessageDialog(p1, scrollPane, "View 1", JOptionPane.PLAIN_MESSAGE);
        });

    JButton h2 = new JButton("?");
    h2.addActionListener(
        e -> {
          textArea.setText(messageTwo);
          JOptionPane.showMessageDialog(p2, scrollPane, "View 2", JOptionPane.PLAIN_MESSAGE);
        });

    JButton h3 = new JButton("?");
    h3.addActionListener(
        e -> {
          textArea.setText(messageThree);
          textArea.setCaretPosition(0);
          JOptionPane.showMessageDialog(p3, scrollPane, "View 3", JOptionPane.PLAIN_MESSAGE);
        });

    // create a GraphMouse for each view
    // each one has a different scaling plugin
    DefaultModalGraphMouse<String, Integer> gm1 =
        new DefaultModalGraphMouse<>() {
          public void loadPlugins() {
            super.loadPlugins();
            scalingPlugin =
                ScalingGraphMousePlugin.builder()
                    .scalingControl(new LayoutScalingControl())
                    .build();
          }
        };

    DefaultModalGraphMouse<String, Integer> gm2 =
        new DefaultModalGraphMouse<>() {
          public void loadPlugins() {
            super.loadPlugins();
            scalingPlugin =
                ScalingGraphMousePlugin.builder().scalingControl(new ViewScalingControl()).build();
          }
        };

    DefaultModalGraphMouse<String, Integer> gm3 = new DefaultModalGraphMouse<>() {};

    vv1.setGraphMouse(gm1);
    vv2.setGraphMouse(gm2);
    vv3.setGraphMouse(gm3);

    vv1.setToolTipText("<html><center>MouseWheel Scales Layout</center></html>");
    vv2.setToolTipText("<html><center>MouseWheel Scales View</center></html>");
    vv3.setToolTipText(
        "<html><center>MouseWheel Scales Layout and<p>crosses over to view<p>ctrl+MouseWheel scales view</center></html>");

    vv1.addPostRenderPaintable(new BannerLabel(vv1, "View 1"));
    vv2.addPostRenderPaintable(new BannerLabel(vv2, "View 2"));
    vv3.addPostRenderPaintable(new BannerLabel(vv3, "View 3"));

    textArea = new JTextArea(6, 30);
    scrollPane =
        new JScrollPane(
            textArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(false);

    JPanel flow = new JPanel();
    flow.add(h1);
    flow.add(
        ModeComboBox.builder()
            .modes(Modal.Mode.TRANSFORMING, Modal.Mode.PICKING)
            .modals(gm1)
            .build()
            .buildUI());
    p1.add(flow, BorderLayout.SOUTH);

    flow = new JPanel();
    flow.add(h2);
    flow.add(
        ModeComboBox.builder()
            .modes(Modal.Mode.TRANSFORMING, Modal.Mode.PICKING)
            .modals(gm2)
            .build()
            .buildUI());
    p2.add(flow, BorderLayout.SOUTH);

    flow = new JPanel();
    flow.add(h3);
    flow.add(
        ModeComboBox.builder()
            .modes(Modal.Mode.TRANSFORMING, Modal.Mode.PICKING)
            .modals(gm3)
            .build()
            .buildUI());
    p3.add(flow, BorderLayout.SOUTH);

    panel.add(p1);
    panel.add(p2);
    panel.add(p3);
    add(panel);
  }

  static class BannerLabel implements VisualizationViewer.Paintable {
    int x;
    int y;
    Font font;
    FontMetrics metrics;
    int swidth;
    int sheight;
    String str;
    VisualizationViewer<String, Integer> vv;

    public BannerLabel(VisualizationViewer<String, Integer> vv, String label) {
      this.vv = vv;
      this.str = label;
    }

    public void paint(Graphics g) {
      Dimension d = vv.getSize();
      if (font == null) {
        font = new Font(g.getFont().getName(), Font.BOLD, 30);
        metrics = g.getFontMetrics(font);
        swidth = metrics.stringWidth(str);
        sheight = metrics.getMaxAscent() + metrics.getMaxDescent();
        x = (3 * d.width / 2 - swidth) / 2;
        y = d.height - sheight;
      }
      g.setFont(font);
      Color oldColor = g.getColor();
      g.setColor(Color.gray);
      g.drawString(str, x, y);
      g.setColor(oldColor);
    }

    public boolean useTransform() {
      return false;
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new MultiViewDemoModal());
    f.pack();
    f.setVisible(true);
  }
}
