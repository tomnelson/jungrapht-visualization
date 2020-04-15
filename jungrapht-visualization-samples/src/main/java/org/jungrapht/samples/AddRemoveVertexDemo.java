package org.jungrapht.samples;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.LayoutFunction;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.layout.algorithms.*;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds vertices one at a time, adds edges to connect them, then removes vertices one a a time.
 * Repeats.
 *
 * @author Tom Nelson
 */
public class AddRemoveVertexDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(AddRemoveVertexDemo.class);

  private static final int MAX_VERTICES = 10;

  private Graph<Number, Number> graph;

  private VisualizationViewer<Number, Number> vv;

  private LayoutAlgorithm<Number> layoutAlgorithm;

  private Timer timer;

  private JButton switchLayout;

  private static final int EDGE_LENGTH = 100;

  private int previousVertex = -1;

  private AddRemoveVertexDemo() {

    this.graph =
        GraphTypeBuilder.<Number, Number>forGraphType(DefaultGraphType.directedPseudograph())
            .buildGraph();

    layoutAlgorithm = new FRLayoutAlgorithm<>();

    LayoutAlgorithm<Number> staticLayoutAlgorithm = new StaticLayoutAlgorithm<>();

    vv =
        VisualizationViewer.builder(graph)
            .graphMouse(new DefaultModalGraphMouse<>())
            .layoutAlgorithm(staticLayoutAlgorithm)
            .viewSize(new Dimension(600, 600))
            .build();

    this.setLayout(new BorderLayout());
    this.setBackground(java.awt.Color.lightGray);
    this.setFont(new Font("Serif", Font.PLAIN, 12));

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
                VisualizationModel model = vv.getVisualizationModel();
                LayoutModel layoutModel = model.getLayoutModel();
                layoutModel.setSize(vv.getWidth(), vv.getHeight());
                layoutModel.accept(model.getLayoutAlgorithm());
              }
            });

    final JRadioButton animateLayoutTransition = new JRadioButton("Animate Layout Transition");

    LayoutFunction<Number> layoutFunction =
        new LayoutFunction(
            LayoutFunction.Layout.of("Kamada Kawai", KKLayoutAlgorithm.<Number>builder()),
            LayoutFunction.Layout.of("Circle", CircleLayoutAlgorithm.<Number>builder()),
            LayoutFunction.Layout.of("Self Organizing Map", ISOMLayoutAlgorithm.<Number>builder()),
            LayoutFunction.Layout.of("Fruchterman Reingold", FRLayoutAlgorithm.<Number>builder()),
            LayoutFunction.Layout.of("Spring", SpringLayoutAlgorithm.<Number, Number>builder()));

    final JComboBox graphChooser = new JComboBox(layoutFunction.getNames().toArray());
    graphChooser.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  LayoutAlgorithm.Builder<Number, ?, ?> builder =
                      layoutFunction.apply((String) graphChooser.getSelectedItem());
                  layoutAlgorithm = builder.build();
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
                  }
                }));
    graphChooser.setSelectedIndex(0);

    JPanel southPanel = new JPanel(new GridLayout(1, 2));
    southPanel.setBorder(BorderFactory.createTitledBorder("Layout Functions"));
    southPanel.add(animateLayoutTransition);
    southPanel.add(graphChooser);
    this.add(southPanel, BorderLayout.SOUTH);

    timer = new Timer();
    timer.schedule(new AddRemoveTask(this::processAdd), 500, 500); //subsequent rate
    vv.repaint();
  }

  private void processAdd() {
    vv.getRenderContext().getSelectedVertexState().clear();
    vv.getRenderContext().getSelectedEdgeState().clear();
    try {

      if (graph.vertexSet().size() < MAX_VERTICES) {
        //add a vertex
        SwingUtilities.invokeLater(
            () -> {
              int v1 = graph.vertexSet().size();
              graph.addVertex(v1);
              vv.getRenderContext().getSelectedVertexState().select(v1);

              // wire it to some edges
              if (previousVertex != -1) {
                int edge = graph.edgeSet().size();
                vv.getRenderContext().getSelectedEdgeState().select(edge);
                graph.addEdge(previousVertex, v1, edge);
                // connect to a random vertex
                int rand = new Random().nextInt(v1);
                vv.getRenderContext().getSelectedEdgeState().select(edge);
                graph.addEdge(v1, rand, ++edge);
                vv.getVisualizationModel().getLayoutModel().accept(layoutAlgorithm);
                vv.repaint();
              }
              previousVertex = v1;
            });

      } else {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new AddRemoveTask(this::processRemove), 500, 500); //subsequent rate
      }

    } catch (Exception e) {
      log.warn("exception:", e);
    }
  }

  private void processRemove() {
    vv.getRenderContext().getSelectedVertexState().clear();
    vv.getRenderContext().getSelectedEdgeState().clear();
    try {

      if (graph.vertexSet().size() > 0) {
        //remove the last vertex
        SwingUtilities.invokeLater(
            () -> {
              int v1 = graph.vertexSet().size() - 1;
              graph.removeVertex(v1);
              vv.getVisualizationModel().getLayoutModel().accept(layoutAlgorithm);
              vv.repaint();
            });

      } else {
        timer.cancel();
        timer = new Timer();
        previousVertex = -1;
        timer.schedule(new AddRemoveTask(this::processAdd), 500, 500); //subsequent rate
      }

    } catch (Exception e) {
      log.warn("exception:", e);
    }
  }

  private static class AddRemoveTask extends TimerTask {
    Runnable runnable;

    AddRemoveTask(Runnable runnable) {
      this.runnable = runnable;
    }

    public void run() {
      runnable.run();
    }
  }

  public static void main(String[] args) {
    AddRemoveVertexDemo and = new AddRemoveVertexDemo();
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(and);
    frame.pack();
    frame.setVisible(true);
  }
}
