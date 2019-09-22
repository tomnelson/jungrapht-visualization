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
import java.util.Set;
import java.util.function.Function;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.subLayout.Collapsable;
import org.jungrapht.visualization.subLayout.TreeCollapser;
import org.jungrapht.visualization.util.helpers.ControlHelpers;
import org.jungrapht.visualization.util.helpers.TreeLayoutSelector;

/**
 * Demonstrates "collapsing"/"expanding" of a tree's subtrees.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class TreeCollapseDemo extends JPanel {

  /** the original graph */
  Graph<Collapsable<?>, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Collapsable<?>, Integer> vv;

  @SuppressWarnings("unchecked")
  public TreeCollapseDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    Graph<String, Integer> generatedGraph = DemoTreeSupplier.createTreeTwo();
    // make a pseudograph with Collapsable vertex types
    // the graph has to allow self loops and parallel edges in order to
    // be collapsed and expanded without losing edges
    this.graph =
        GraphTypeBuilder.<Collapsable<?>, Integer>forGraphType(
                DefaultGraphType.directedPseudograph())
            .buildGraph();
    // add vertices and edges to the new graph
    for (Integer edge : generatedGraph.edgeSet()) {
      Collapsable<?> source = Collapsable.of(generatedGraph.getEdgeSource(edge));
      Collapsable<?> target = Collapsable.of(generatedGraph.getEdgeTarget(edge));
      this.graph.addVertex(source);
      this.graph.addVertex(target);
      this.graph.addEdge(source, target, edge);
    }

    Dimension viewSize = new Dimension(600, 600);
    Dimension layoutSize = new Dimension(600, 600);

    vv = VisualizationViewer.builder(graph).layoutSize(layoutSize).viewSize(viewSize).build();
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setVertexShapeFunction(new ClusterShapeFunction());
    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);
    vv.scaleToLayout();
    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    final DefaultGraphMouse<String, Integer> graphMouse = new DefaultGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    JButton collapse = new JButton("Collapse");
    collapse.addActionListener(
        e -> {
          Set<Collapsable<?>> picked = vv.getSelectedVertexState().getSelected();
          if (picked.size() == 1) {
            Collapsable<?> root = picked.iterator().next();
            Graph<Collapsable<?>, Integer> subTree = TreeCollapser.collapse(graph, root);
            LayoutModel<Collapsable<?>> layoutModel = vv.getVisualizationModel().getLayoutModel();
            layoutModel.set(Collapsable.of(subTree), layoutModel.apply(root));
            vv.getVisualizationModel().setGraph(graph, true);
            vv.getSelectedVertexState().clear();
            vv.repaint();
          }
        });

    JButton expand = new JButton("Expand");
    expand.addActionListener(
        e -> {
          for (Collapsable<?> v : vv.getSelectedVertexState().getSelected()) {
            if (v.get() instanceof Graph) {
              graph = TreeCollapser.expand(graph, (Collapsable<Graph>) v);
              LayoutModel<Collapsable<?>> layoutModel = vv.getVisualizationModel().getLayoutModel();
              layoutModel.set(Collapsable.of(graph), layoutModel.apply(v));
              vv.getVisualizationModel().setGraph(graph, true);
            }
            vv.getSelectedVertexState().clear();
            vv.repaint();
          }
        });

    JPanel controls = new JPanel();
    controls.add(
        TreeLayoutSelector.builder(vv).initialSelection(0).after(vv::scaleToLayout).build());

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
  static class ClusterShapeFunction extends EllipseShapeFunction<Collapsable<?>> {

    ClusterShapeFunction() {
      setSizeFunction(new ClusterSizeFunction(20));
    }

    @Override
    public Shape apply(Collapsable<?> v) {
      if (v.get() instanceof Graph) {
        @SuppressWarnings("rawtypes")
        int size = ((Graph) v.get()).vertexSet().size();
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
  static class ClusterSizeFunction implements Function<Collapsable<?>, Integer> {
    int size;

    public ClusterSizeFunction(Integer size) {
      this.size = size;
    }

    public Integer apply(Collapsable<?> v) {
      if (v.get() instanceof Graph) {
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
