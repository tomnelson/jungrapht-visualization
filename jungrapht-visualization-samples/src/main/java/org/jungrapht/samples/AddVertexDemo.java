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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A variation of old AddVertexDemo that animates transitions between graph algorithms.
 *
 * @author Tom Nelson
 */
public class AddVertexDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(AddVertexDemo.class);

  private static final long serialVersionUID = -5345319851341875800L;

  private Graph<Number, Number> graph;

  private VisualizationViewer<Number, Number> vv;

  private LayoutAlgorithm<Number> layoutAlgorithm;

  Timer timer;

  boolean done;

  protected JButton switchLayout;

  public static final int EDGE_LENGTH = 100;

  public AddVertexDemo() {

    this.graph =
        GraphTypeBuilder.<Number, Number>forGraphType(DefaultGraphType.directedPseudograph())
            .buildGraph();

    layoutAlgorithm = FRLayoutAlgorithm.<Number>builder().build();

    LayoutAlgorithm<Number> staticLayoutAlgorithm = new StaticLayoutAlgorithm<>();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(staticLayoutAlgorithm)
            .viewSize(new Dimension(600, 600))
            .build();

    this.setLayout(new BorderLayout());
    this.setBackground(java.awt.Color.lightGray);
    this.setFont(new Font("Serif", Font.PLAIN, 12));

    vv.setGraphMouse(new DefaultModalGraphMouse<Number, Number>());

    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.setForeground(Color.white);

    this.add(vv.getComponent());

    // add listener to change layout size and restart layoutalgorithm when
    // the view is resized
    vv.getComponent()
        .addComponentListener(
            new ComponentAdapter() {
              /**
               * Invoked when the component's size changes.
               *
               * @param e the event payload
               */
              @Override
              public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                VisualizationViewer vv = (VisualizationViewer) e.getComponent();
                VisualizationModel model = vv.getModel();
                LayoutModel layoutModel = model.getLayoutModel();
                layoutModel.setSize(vv.getWidth(), vv.getHeight());
                layoutModel.accept(model.getLayoutAlgorithm());
              }
            });

    final JRadioButton animateChange = new JRadioButton("Animate Layout Change");
    switchLayout = new JButton("Switch to SpringLayout");
    switchLayout.addActionListener(
        ae -> {
          if (switchLayout.getText().indexOf("Spring") > 0) {
            switchLayout.setText("Switch to FRLayout");
            layoutAlgorithm =
                SpringLayoutAlgorithm.<Number>builder()
                    .withLengthFunction(e -> EDGE_LENGTH)
                    //                    .repulsionContractBuilder(BarnesHutSpringRepulsion.barnesHutBuilder())
                    .build();
          } else {
            switchLayout.setText("Switch to SpringLayout");
            layoutAlgorithm = FRLayoutAlgorithm.<Number>builder().build();
          }
          if (animateChange.isSelected()) {
            LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
          } else {
            LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
          }
        });

    JPanel southPanel = new JPanel(new GridLayout(1, 2));
    southPanel.add(switchLayout);
    southPanel.add(animateChange);
    this.add(southPanel, BorderLayout.SOUTH);

    timer = new Timer();

    timer.schedule(new RemindTask(), 1000, 1000); //subsequent rate
    vv.repaint();
  }

  Integer v_prev = null;

  public void process() {

    vv.getRenderContext().getSelectedVertexState().clear();
    vv.getRenderContext().getSelectedEdgeState().clear();
    try {

      if (graph.vertexSet().size() < 100) {
        //add a vertex
        Integer v1 = graph.vertexSet().size();

        graph.addVertex(v1);
        vv.getRenderContext().getSelectedVertexState().select(v1);

        // wire it to some edges
        if (v_prev != null) {
          Integer edge = graph.edgeSet().size();
          vv.getRenderContext().getSelectedEdgeState().select(edge);
          graph.addEdge(v_prev, v1, edge);
          // let's connect to a random vertex, too!
          int rand = (int) (Math.random() * graph.vertexSet().size());
          edge = graph.edgeSet().size();
          vv.getRenderContext().getSelectedEdgeState().select(edge);
          graph.addEdge(v1, rand, edge);
        }

        v_prev = v1;

        // accept the algorithm again so that it will turn off the old relaxer and start a new one
        vv.getModel().getLayoutModel().accept(layoutAlgorithm);

        vv.repaint();

      } else {
        done = true;
      }

    } catch (Exception e) {
      log.warn("exception:", e);
    }
  }

  class RemindTask extends TimerTask {

    @Override
    public void run() {
      process();
      if (done) {
        cancel();
      }
    }
  }

  public static void main(String[] args) {
    AddVertexDemo and = new AddVertexDemo();
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(and);
    frame.pack();
    frame.setVisible(true);
  }
}
