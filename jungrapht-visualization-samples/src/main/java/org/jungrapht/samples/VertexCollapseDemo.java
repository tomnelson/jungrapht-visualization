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
import java.awt.Shape;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.sublayout.Collapser;
import org.jungrapht.visualization.util.PredicatedParallelEdgeIndexFunction;

/**
 * A demo that shows how collections of vertices can be collapsed into a single vertex. In this
 * demo, the vertices that are collapsed are those mouse-selected by the user.
 *
 * @author Tom Nelson
 */
public class VertexCollapseDemo extends JPanel {

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
  /** the graph */
  Graph<MyVertex, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<MyVertex, Integer> vv;

  LayoutAlgorithm<MyVertex> layoutAlgorithm;

  Collapser<MyVertex, Integer> collapser;

  public VertexCollapseDemo() {

    setLayout(new BorderLayout());

    // the graph has to allow self loops and parallel edges in order to
    // be collapsed and expanded without losing edges
    this.graph = TestGraphs.getOneComponentGraph(MyVertex::new);

    layoutAlgorithm = new FRLayoutAlgorithm<>();

    Dimension preferredSize = new Dimension(400, 400);

    final VisualizationModel<MyVertex, Integer> visualizationModel =
        VisualizationModel.<MyVertex, Integer>builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .layoutSize(preferredSize)
            .build();

    // the regular graph mouse for the normal view
    final DefaultModalGraphMouse<MyVertex, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv =
        VisualizationViewer.builder(visualizationModel)
            .graphMouse(graphMouse)
            .viewSize(preferredSize)
            .build();

    collapser = Collapser.forVisualization(vv);

    vv.getRenderContext()
        .setVertexShapeFunction(new ClusterShapeFunction<>(collapser.collapsedGraphFunction()));

    final Set<Integer> exclusions = new HashSet<>();
    final PredicatedParallelEdgeIndexFunction<MyVertex, Integer> eif =
        new PredicatedParallelEdgeIndexFunction<>(exclusions::contains);

    vv.getRenderContext().setParallelEdgeIndexFunction(eif);

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.setEdgeToolTipFunction(Object::toString);

    VisualizationScrollPane visualizationScrollPane = new VisualizationScrollPane(vv);
    add(visualizationScrollPane);

    JComboBox<ModalGraphMouse.Mode> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

    JButton collapse = new JButton("Collapse");
    collapse.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () ->
                    collapser.collapse(
                        vv.getSelectedVertexState().getSelected(), s -> new MyVertex())));

    JButton expand = new JButton("Expand");
    expand.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () ->
                    collapser.expand(
                        vv.getRenderContext().getSelectedVertexState().getSelected())));

    JButton compressEdges = new JButton("Compress Edges");
    compressEdges.addActionListener(
        e -> {
          Set<MyVertex> picked = vv.getSelectedVertexState().getSelected();
          if (picked.size() == 2) {
            Iterator<MyVertex> pickedIter = picked.iterator();
            MyVertex vertexU = pickedIter.next();
            MyVertex vertexV = pickedIter.next();
            Graph<MyVertex, Integer> graph = vv.getVisualizationModel().getGraph();
            Collection<Integer> edges = new HashSet<>(graph.edgesOf(vertexU));
            edges.retainAll(graph.edgesOf(vertexV));
            exclusions.addAll(edges);
            vv.repaint();
          }
        });

    JButton expandEdges = new JButton("Expand Edges");
    expandEdges.addActionListener(
        e -> {
          Set<MyVertex> picked = vv.getSelectedVertexState().getSelected();
          if (picked.size() == 2) {
            Iterator<MyVertex> pickedIter = picked.iterator();
            MyVertex vertexU = pickedIter.next();
            MyVertex vertexV = pickedIter.next();
            Graph<MyVertex, Integer> graph = vv.getVisualizationModel().getGraph();
            Collection<Integer> edges = new HashSet<>(graph.edgesOf(vertexU));
            edges.retainAll(graph.edgesOf(vertexV));
            exclusions.removeAll(edges);
            vv.repaint();
          }
        });

    JButton reset = new JButton("Reset");
    reset.addActionListener(
        e -> {
          this.graph = TestGraphs.getOneComponentGraph(MyVertex::new);
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

    Box controls = Box.createHorizontalBox();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    controls.add(
        ControlHelpers.getCenteredContainer(
            "Picked",
            Box.createVerticalBox(),
            collapse,
            expand,
            compressEdges,
            expandEdges,
            reset));
    controls.add(ControlHelpers.getCenteredContainer("Mouse Mode", modeBox));
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

  /**
   * simple vertex class for demo Any type will work as long as there is a Supplier to provide a new
   * instance. The noarg constructor is used in this demo {@code }MyVertex::new}
   */
  static class MyVertex {

    static int count;
    int id;

    MyVertex() {
      this.id = count++;
    }

    @Override
    public String toString() {
      return "MyVertex{" + "id=" + id + '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MyVertex myVertex = (MyVertex) o;
      return id == myVertex.id;
    }

    @Override
    public int hashCode() {
      return Objects.hash(id);
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new VertexCollapseDemo());
    f.pack();
    f.setVisible(true);
  }
}
