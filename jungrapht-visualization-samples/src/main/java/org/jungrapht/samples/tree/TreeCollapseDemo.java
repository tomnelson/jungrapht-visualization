package org.jungrapht.samples.tree;
/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Shape;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.subLayout.TreeCollapser;
import org.jungrapht.visualization.subLayout.VisualTreeCollapser;

/**
 * Demonstrates "collapsing"/"expanding" of a tree's subtrees.
 *
 * @author Tom Nelson
 */
public class TreeCollapseDemo extends JPanel {

  /** the original graph */
  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  static int counter = 0;
  Supplier<String> vertexFactory = () -> "COL" + counter++;

  public TreeCollapseDemo() {

    setLayout(new BorderLayout());

    this.graph = DemoTreeSupplier.createTreeTwo();

    Dimension viewSize = new Dimension(600, 600);
    Dimension layoutSize = new Dimension(600, 600);

    vv = VisualizationViewer.builder(graph).layoutSize(layoutSize).viewSize(viewSize).build();
    TreeCollapser<String, Integer> collapser = new VisualTreeCollapser(vv, vertexFactory);

    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext()
        .setVertexShapeFunction(new ClusterShapeFunction(collapser.collapsedGraphFunction()));
    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.setEdgeToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);
    vv.scaleToLayout();
    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    JButton collapse = new JButton("Collapse");
    collapse.addActionListener(e -> collapser.collapse(vv.getSelectedVertexState().getSelected()));

    JButton expand = new JButton("Expand");
    expand.addActionListener(e -> collapser.expand(vv.getSelectedVertexState().getSelected()));

    JPanel controls = new JPanel();
    controls.add(TreeLayoutSelector.builder(vv).initialSelection(0).build());

    controls.add(ControlHelpers.getZoomControls(vv));
    controls.add(collapse);
    controls.add(expand);
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
      setSizeFunction(new ClusterSizeFunction<V, E>(function, 20));
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
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new TreeCollapseDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
