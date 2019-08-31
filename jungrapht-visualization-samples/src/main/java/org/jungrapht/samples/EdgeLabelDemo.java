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
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.ParallelEdgeShapeFunction;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.renderers.EdgeLabelRenderer;
import org.jungrapht.visualization.renderers.VertexLabelRenderer;
import org.jungrapht.visualization.util.Context;
import org.jungrapht.visualization.util.helpers.ControlHelpers;

/**
 * Demonstrates jung support for drawing edge labels that can be positioned at any point along the
 * edge, and can be rotated to be parallel with the edge.
 *
 * @author Tom Nelson
 */
public class EdgeLabelDemo extends JPanel {
  private static final long serialVersionUID = -6077157664507049647L;

  /** the graph */
  Graph<Integer, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Integer, Number> vv;

  /** */
  VertexLabelRenderer vertexLabelRenderer;

  EdgeLabelRenderer edgeLabelRenderer;

  ScalingControl scaler = new CrossoverScalingControl();

  /** create an instance of a simple graph with controls to demo the label positioning features */
  @SuppressWarnings("serial")
  public EdgeLabelDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = buildGraph();

    LayoutAlgorithm<Integer> layoutAlgorithm = CircleLayoutAlgorithm.<Integer>builder().build();
    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .viewSize(new Dimension(600, 400))
            .build();
    vv.setBackground(Color.white);

    vertexLabelRenderer = vv.getRenderContext().getVertexLabelRenderer();
    edgeLabelRenderer = vv.getRenderContext().getEdgeLabelRenderer();

    Function<Number, String> stringer =
        e -> "Edge:" + graph.getEdgeSource(e) + "-" + graph.getEdgeTarget(e);

    vv.getRenderContext().setEdgeLabelFunction(stringer);
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            v -> vv.getSelectedEdgeState().isSelected(v) ? Color.cyan : Color.black);
    vv.getRenderContext()
        .setVertexFillPaintFunction(
            v -> vv.getSelectedVertexState().isSelected(v) ? Color.yellow : Color.red);

    // add my listener for ToolTips
    vv.setVertexToolTipFunction(
        o -> o + " " + vv.getVisualizationModel().getLayoutModel().apply(o));

    // create a frame to hold the graph
    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    final DefaultModalGraphMouse<Integer, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    ButtonGroup radio = new ButtonGroup();
    JRadioButton lineButton = new JRadioButton("Line");
    lineButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
            vv.repaint();
          }
        });

    JRadioButton quadButton = new JRadioButton("QuadCurve");
    quadButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            vv.getRenderContext().setEdgeShapeFunction(EdgeShape.quadCurve());
            vv.repaint();
          }
        });

    JRadioButton cubicButton = new JRadioButton("CubicCurve");
    cubicButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            vv.getRenderContext().setEdgeShapeFunction(EdgeShape.cubicCurve());
            vv.repaint();
          }
        });

    radio.add(lineButton);
    radio.add(quadButton);
    radio.add(cubicButton);

    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

    JCheckBox rotate = new JCheckBox("<html><center>EdgeType<p>Parallel</center></html>");
    rotate.addItemListener(
        e -> {
          AbstractButton b = (AbstractButton) e.getSource();
          edgeLabelRenderer.setRotateEdgeLabels(b.isSelected());
          vv.repaint();
        });

    rotate.setSelected(true);
    EdgeClosenessUpdater edgeClosenessUpdater = new EdgeClosenessUpdater();
    JSlider closenessSlider =
        new JSlider(edgeClosenessUpdater.rangeModel) {
          public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width /= 2;
            return d;
          }
        };

    JSlider edgeOffsetSlider =
        new JSlider(0, 50) {
          public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width /= 2;
            return d;
          }
        };
    edgeOffsetSlider.addChangeListener(
        e -> {
          JSlider s = (JSlider) e.getSource();
          Function<Context<Graph<Integer, Number>, Number>, Shape> edgeShapeFunction =
              vv.getRenderContext().getEdgeShapeFunction();
          if (edgeShapeFunction instanceof ParallelEdgeShapeFunction) {
            ((ParallelEdgeShapeFunction) edgeShapeFunction).setControlOffsetIncrement(s.getValue());
            vv.repaint();
          }
        });

    Box controls = Box.createHorizontalBox();

    JPanel edgePanel = new JPanel(new GridLayout(0, 1));
    edgePanel.setBorder(BorderFactory.createTitledBorder("Edge Shape"));
    edgePanel.add(lineButton);
    edgePanel.add(quadButton);
    edgePanel.add(cubicButton);

    JPanel rotatePanel = new JPanel();
    rotatePanel.setBorder(BorderFactory.createTitledBorder("Alignment"));
    rotatePanel.add(rotate);

    JPanel labelPanel = new JPanel(new BorderLayout());
    JPanel sliderPanel = new JPanel(new GridLayout(3, 1));
    JPanel sliderLabelPanel = new JPanel(new GridLayout(3, 1));
    JPanel offsetPanel = new JPanel(new BorderLayout());
    offsetPanel.setBorder(BorderFactory.createTitledBorder("Offset"));
    sliderPanel.add(closenessSlider);
    sliderPanel.add(edgeOffsetSlider);
    sliderLabelPanel.add(new JLabel("Closeness", JLabel.RIGHT));
    sliderLabelPanel.add(new JLabel("Edges", JLabel.RIGHT));
    offsetPanel.add(sliderLabelPanel, BorderLayout.WEST);
    offsetPanel.add(sliderPanel);
    labelPanel.add(offsetPanel);
    labelPanel.add(rotatePanel, BorderLayout.WEST);

    JPanel modePanel = new JPanel(new GridLayout(2, 1));
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(graphMouse.getModeComboBox());

    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    controls.add(edgePanel);
    controls.add(labelPanel);
    controls.add(modePanel);
    add(controls, BorderLayout.SOUTH);
    quadButton.setSelected(true);
  }

  /**
   * subclassed to hold two BoundedRangeModel instances that are used by JSliders to move the edge
   * label positions
   *
   * @author Tom Nelson
   */
  class EdgeClosenessUpdater {
    BoundedRangeModel rangeModel;

    public EdgeClosenessUpdater() {
      int initialValue = ((int) vv.getRenderContext().getEdgeLabelCloseness() * 10) / 10;
      this.rangeModel = new DefaultBoundedRangeModel(initialValue, 0, 0, 10);

      rangeModel.addChangeListener(
          e -> {
            vv.getRenderContext().setEdgeLabelCloseness(rangeModel.getValue() / 10f);
            vv.repaint();
          });
    }
  }

  Graph<Integer, Number> buildGraph() {
    Graph<Integer, Number> graph =
        GraphTypeBuilder.<Integer, Number>forGraphType(DefaultGraphType.directedMultigraph())
            .buildGraph();
    IntStream.rangeClosed(0, 2).forEach(graph::addVertex);
    graph.addEdge(0, 1, Math.random());
    graph.addEdge(0, 1, Math.random());
    graph.addEdge(0, 1, Math.random());
    graph.addEdge(1, 0, Math.random());
    graph.addEdge(1, 0, Math.random());
    graph.addEdge(1, 2, Math.random());
    graph.addEdge(1, 2, Math.random());

    return graph;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    Container content = frame.getContentPane();
    content.add(new EdgeLabelDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
