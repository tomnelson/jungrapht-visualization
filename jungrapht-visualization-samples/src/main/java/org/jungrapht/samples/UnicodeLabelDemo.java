/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.visualization.GraphZoomScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.decorators.EllipseNodeShapeFunction;
import org.jungrapht.visualization.decorators.NodeIconShapeFunction;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.renderers.DefaultEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.DefaultNodeLabelRenderer;

/**
 * A demo that shows flag images as nodes, and uses unicode to render node labels.
 *
 * @author Tom Nelson
 */
public class UnicodeLabelDemo {

  Graph<Integer, Number> graph;

  VisualizationViewer<Integer, Number> vv;

  boolean showLabels;

  public UnicodeLabelDemo() {

    // create a simple graph for the demo
    graph = createGraph();
    Map<Integer, Icon> iconMap = new HashMap<>();

    vv =
        new VisualizationViewer<>(
            graph, FRLayoutAlgorithm.<Integer>builder().build(), new Dimension(700, 700));
    vv.getRenderContext().setNodeLabelFunction(new UnicodeNodeStringer());
    vv.getRenderContext().setNodeLabelRenderer(new DefaultNodeLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));
    NodeIconShapeFunction<Integer> nodeIconShapeFunction =
        new NodeIconShapeFunction<>(new EllipseNodeShapeFunction<>());
    Function<Integer, Icon> nodeIconFunction = iconMap::get;
    vv.getRenderContext().setNodeShapeFunction(nodeIconShapeFunction);
    vv.getRenderContext().setNodeIconFunction(nodeIconFunction);
    loadImages(iconMap);
    nodeIconShapeFunction.setIconMap(iconMap);
    vv.getRenderContext()
        .setNodeFillPaintFunction(
            new PickableElementPaintFunction<>(
                vv.getSelectedNodeState(), Color.white, Color.yellow));
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(
                vv.getSelectedEdgeState(), Color.black, Color.lightGray));

    vv.setBackground(Color.white);

    // add my listener for ToolTips
    vv.setNodeToolTipFunction(Object::toString);

    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    final DefaultModalGraphMouse<Integer, Number> gm = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(gm);

    JCheckBox lo = new JCheckBox("Show Labels");
    lo.addItemListener(
        e -> {
          showLabels = e.getStateChange() == ItemEvent.SELECTED;
          vv.repaint();
        });
    lo.setSelected(true);

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls(vv, ""));
    controls.add(lo);
    controls.add(gm.getModeComboBox());
    content.add(controls, BorderLayout.SOUTH);

    frame.pack();
    frame.setVisible(true);
  }

  class UnicodeNodeStringer implements Function<Integer, String> {

    Map<Integer, String> map = new HashMap<>();
    String[] labels = {
      "\u0057\u0065\u006C\u0063\u006F\u006D\u0065\u0020\u0074\u006F\u0020JUNG\u0021",
      "\u6B22\u8FCE\u4F7F\u7528\u0020\u0020JUNG\u0021",
      "\u0414\u043E\u0431\u0440\u043E\u0020\u043F\u043E\u0436\u0430\u043B\u043E\u0432\u0430\u0422\u044A\u0020\u0432\u0020JUNG\u0021",
      "\u0042\u0069\u0065\u006E\u0076\u0065\u006E\u0075\u0065\u0020\u0061\u0075\u0020JUNG\u0021",
      "\u0057\u0069\u006C\u006B\u006F\u006D\u006D\u0065\u006E\u0020\u007A\u0075\u0020JUNG\u0021",
      "JUNG\u3078\u3087\u3045\u3053\u305D\u0021",
      //                "\u0053\u00E9\u006A\u0061\u0020\u0042\u0065\u006D\u0076\u0069\u006E\u0064\u006F\u0020JUNG\u0021",
      "\u0042\u0069\u0065\u006E\u0076\u0065\u006E\u0069\u0064\u0061\u0020\u0061\u0020JUNG\u0021"
    };

    public UnicodeNodeStringer() {
      for (Integer node : graph.vertexSet()) {
        map.put(node, labels[node % labels.length]);
      }
    }

    /** */
    public String getLabel(Integer v) {
      if (showLabels) {
        return map.get(v);
      } else {
        return "";
      }
    }

    public String apply(Integer input) {
      return getLabel(input);
    }
  }

  Graph<Integer, Number> createGraph() {
    Graph<Integer, Number> graph =
        GraphTypeBuilder.<Integer, Number>forGraphType(DefaultGraphType.dag()).buildGraph();
    IntStream.rangeClosed(0, 10).forEach(i -> graph.addVertex(i));
    graph.addEdge(0, 1, Math.random());
    graph.addEdge(3, 0, Math.random());
    graph.addEdge(0, 4, Math.random());
    graph.addEdge(4, 5, Math.random());
    graph.addEdge(5, 3, Math.random());
    graph.addEdge(2, 1, Math.random());
    graph.addEdge(4, 1, Math.random());
    graph.addEdge(8, 2, Math.random());
    graph.addEdge(3, 8, Math.random());
    graph.addEdge(6, 7, Math.random());
    graph.addEdge(7, 5, Math.random());
    graph.addEdge(0, 9, Math.random());
    graph.addEdge(9, 8, Math.random());
    graph.addEdge(7, 6, Math.random());
    graph.addEdge(6, 5, Math.random());
    graph.addEdge(4, 2, Math.random());
    graph.addEdge(5, 4, Math.random());
    graph.addEdge(4, 10, Math.random());
    graph.addEdge(10, 4, Math.random());

    return graph;
  }

  protected void loadImages(Map<Integer, Icon> imageMap) {

    ImageIcon[] icons = null;
    try {
      icons =
          new ImageIcon[] {
            new ImageIcon(getClass().getResource("/images/united-states.gif")),
            new ImageIcon(getClass().getResource("/images/china.gif")),
            new ImageIcon(getClass().getResource("/images/russia.gif")),
            new ImageIcon(getClass().getResource("/images/france.gif")),
            new ImageIcon(getClass().getResource("/images/germany.gif")),
            new ImageIcon(getClass().getResource("/images/japan.gif")),
            new ImageIcon(getClass().getResource("/images/spain.gif"))
          };
    } catch (Exception ex) {
      System.err.println("You need flags.jar in your classpath to see the flag icons.");
    }
    for (Integer node : graph.vertexSet()) {
      int i = node;
      imageMap.put(node, icons[i % icons.length]);
    }
  }

  public static void main(String[] args) {
    new UnicodeLabelDemo();
  }
}
