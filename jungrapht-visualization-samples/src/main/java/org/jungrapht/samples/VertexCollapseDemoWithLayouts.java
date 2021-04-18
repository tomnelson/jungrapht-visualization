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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.LayoutHelper;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.modal.Modal.Mode;
import org.jungrapht.visualization.control.modal.ModeControls;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.sublayout.Collapser;
import org.jungrapht.visualization.util.LayoutAlgorithmTransition;
import org.jungrapht.visualization.util.PredicatedParallelEdgeIndexFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A demo that shows how collections of vertices can be collapsed into a single vertex. In this
 * demo, the vertices that are collapsed are those mouse-selected by the user.
 *
 * @author Tom Nelson
 */
public class VertexCollapseDemoWithLayouts extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(VertexCollapseDemoWithLayouts.class);

  String instructions =
      "<html>Use the mouse to select multiple vertices"
          + "<p>either by dragging a region, or by shift-clicking"
          + "<p>on multiple vertices."
          + "<p>After you select vertices, use the Collapse button"
          + "<p>to combine them into a single vertex."
          + "<p>Select a 'collapsed' vertex and use the Expand button"
          + "<p>to restore the collapsed vertices."
          + "<p>The Restore button will restore the original graph."
          + "<p>If you select 2 (and only 2) vertices, then press"
          + "<p>the Compress Edges button, parallel edges between"
          + "<p>those two vertices will no longer be expanded."
          + "<p>If you select 2 (and only 2) vertices, then press"
          + "<p>the Expand Edges button, parallel edges between"
          + "<p>those two vertices will be expanded."
          + "<p>You can drag the vertices with the mouse."
          + "<p>Use the 'Picking'/'Transforming' combo-box to switch"
          + "<p>between picking and transforming mode.</html>";

  static int counter = 0;
  Supplier<String> vertexFactory = () -> "COL" + counter++;

  /** the graph */
  Graph<String, Integer> graph = TestGraphs.getOneComponentGraph(vertexFactory);

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  LayoutAlgorithm<String> layoutAlgorithm;
  Collapser<String, Integer> collapser;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public VertexCollapseDemoWithLayouts() {
    setLayout(new BorderLayout());

    layoutAlgorithm = new FRLayoutAlgorithm<>();

    Dimension preferredSize = new Dimension(400, 400);

    final VisualizationModel<String, Integer> visualizationModel =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .layoutSize(preferredSize)
            .build();

    // the regular graph mouse for the normal view
    final DefaultModalGraphMouse<String, Number> graphMouse = new DefaultModalGraphMouse();

    vv =
        VisualizationViewer.builder(visualizationModel)
            .graphMouse(graphMouse)
            .viewSize(preferredSize)
            .build();

    collapser = Collapser.forVisualization(vv);

    vv.getRenderContext()
        .setVertexShapeFunction(new ClusterShapeFunction(collapser.collapsedGraphFunction()));

    final Set exclusions = new HashSet();
    final PredicatedParallelEdgeIndexFunction eif =
        new PredicatedParallelEdgeIndexFunction(exclusions::contains);
    vv.getRenderContext().setParallelEdgeIndexFunction(eif);

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);

    VisualizationScrollPane visualizationScrollPane = new VisualizationScrollPane(vv);
    add(visualizationScrollPane);

    JComboBox modeBox = ModeControls.getStandardModeComboBox(Mode.PICKING, graphMouse);

    LayoutHelper.Layouts[] combos = LayoutHelper.getCombos();
    final JRadioButton animateLayoutTransition = new JRadioButton("Animate Layout Transition");

    final JComboBox jcb = new JComboBox(combos);
    jcb.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            SwingUtilities.invokeLater(
                () -> {
                  LayoutHelper.Layouts layoutType = (LayoutHelper.Layouts) jcb.getSelectedItem();
                  LayoutAlgorithm<String> layoutAlgorithm = layoutType.getLayoutAlgorithm();
                  log.trace("got a {}", layoutAlgorithm);
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
                  }
                });
          }
        });

    jcb.setSelectedItem(LayoutHelper.Layouts.FR);

    jcb.setSelectedItem(LayoutHelper.Layouts.FR);

    JButton collapse = new JButton("Collapse");
    collapse.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  Collection<String> picked = new HashSet(vv.getSelectedVertices());
                  collapser.collapse(picked, s -> vertexFactory.get());
                }));

    JButton expand = new JButton("Expand");
    expand.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  Collection<String> picked = new HashSet(vv.getSelectedVertices());
                  collapser.expand(picked);
                }));

    JButton compressEdges = new JButton("Compress Edges");
    compressEdges.addActionListener(
        e -> {
          Set picked = vv.getSelectedVertices();
          if (picked.size() == 2) {
            Iterator pickedIter = picked.iterator();
            Object vertexU = pickedIter.next();
            Object vertexV = pickedIter.next();
            Graph graph = vv.getVisualizationModel().getGraph();
            Collection edges = new HashSet(graph.edgesOf(vertexU));
            edges.retainAll(graph.edgesOf(vertexV));
            exclusions.addAll(edges);
            vv.repaint();
          }
        });

    JButton expandEdges = new JButton("Expand Edges");
    expandEdges.addActionListener(
        e -> {
          Set picked = vv.getSelectedVertices();
          if (picked.size() == 2) {
            Iterator pickedIter = picked.iterator();
            Object vertexU = pickedIter.next();
            Object vertexV = pickedIter.next();
            Graph graph = vv.getVisualizationModel().getGraph();
            Collection edges = new HashSet(graph.edgesOf(vertexU));
            edges.retainAll(graph.edgesOf(vertexV));
            exclusions.removeAll(edges);
            vv.repaint();
          }
        });

    JButton reset = new JButton("Reset");
    reset.addActionListener(
        e -> {
          this.graph = TestGraphs.getOneComponentGraph(vertexFactory);
          vv.getRenderContext().getParallelEdgeIndexFunction().reset();
          vv.getVisualizationModel().setGraph(graph);
          exclusions.clear();
          vv.repaint();
        });

    JButton help = new JButton("Help");
    help.addActionListener(
        e ->
            JOptionPane.showMessageDialog(
                (JComponent) e.getSource(), instructions, "Help", JOptionPane.PLAIN_MESSAGE));

    JPanel controls = new JPanel(new FlowLayout());
    JPanel collapseControls = new JPanel(new GridLayout(0, 1));
    collapseControls.setBorder(BorderFactory.createTitledBorder("Picked"));
    collapseControls.add(collapse);
    collapseControls.add(expand);
    collapseControls.add(compressEdges);
    collapseControls.add(expandEdges);
    collapseControls.add(reset);
    controls.add(collapseControls);
    JPanel controlPanel = new JPanel(new GridLayout(0, 1));
    JPanel modePanel = new JPanel();
    modePanel.setBorder(new TitledBorder("Mouse Mode"));
    modePanel.add(modeBox);

    controlPanel.add(modePanel);

    JPanel jcbPanel = new JPanel(new GridLayout(0, 1));
    jcbPanel.setBorder(new TitledBorder("Layouts"));
    jcbPanel.add(jcb);

    jcbPanel.add(animateLayoutTransition);
    controlPanel.add(jcbPanel);

    controls.add(controlPanel);
    controls.add(help);
    add(controls, BorderLayout.SOUTH);
  }

  /**
   * a demo class that will create a vertex shape that is either a polygon or star. The number of
   * sides corresponds to the number of vertices that were collapsed into the vertex represented by
   * this shape.
   */
  static class ClusterShapeFunction<V, E> extends EllipseShapeFunction<V> {

    Function<V, Graph<V, E>> function;

    ClusterShapeFunction(Function<V, Graph<V, E>> function) {
      this.function = function;
      setSizeFunction(new ClusterSizeFunction<>(function, 20));
    }

    @Override
    public Shape apply(V v) {
      Graph<V, E> graph = function.apply(v);
      if (graph != null) {
        int size = graph.vertexSet().size();
        if (size < 8) {
          int sides = Math.max(size, 3);
          return factory.getRegularPolygon(v, sides);
        } else {
          return factory.getRegularStar(v, size);
        }
      }
      return super.apply(v);
    }
  }

  /**
   * A demo class that will make vertices larger if they represent a collapsed collection of
   * original vertices
   */
  static class ClusterSizeFunction<V, E> implements Function<V, Integer> {
    int size;
    Function<V, Graph<V, E>> function;

    public ClusterSizeFunction(Function<V, Graph<V, E>> function, Integer size) {
      this.function = function;
      this.size = size;
    }

    public Integer apply(V v) {
      if (function.apply(v) != null) {
        return 30;
      }
      return size;
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new VertexCollapseDemoWithLayouts());
    f.pack();
    f.setVisible(true);
  }
}
