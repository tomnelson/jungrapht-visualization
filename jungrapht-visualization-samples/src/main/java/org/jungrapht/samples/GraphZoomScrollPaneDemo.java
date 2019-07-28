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
import java.awt.event.MouseEvent;
import java.util.stream.IntStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.GraphZoomScrollPane;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.AbstractModalGraphMouse;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.GraphMouseListener;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.renderers.BasicVertexLabelRenderer;
import org.jungrapht.visualization.renderers.GradientVertexRenderer;
import org.jungrapht.visualization.renderers.Renderer;

/**
 * Demonstrates the use of <code>GraphZoomScrollPane</code>. This class shows the <code>
 * VisualizationViewer</code> zooming and panning capabilities, using horizontal and vertical
 * scrollbars.
 *
 * <p>This demo also shows ToolTips on graph vertices and edges, and a key listener to change graph
 * mouse modes.
 *
 * @author Tom Nelson
 */
public class GraphZoomScrollPaneDemo {

  /** the graph */
  Graph<Integer, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Integer, Number> vv;

  /** create an instance of a simple graph with controls to demo the zoom features. */
  public GraphZoomScrollPaneDemo() {

    // create a simple graph for the demo
    graph = createGraph();

    ImageIcon sandstoneIcon = null;
    String imageLocation = "/images/Sandstone.jpg";
    try {
      sandstoneIcon = new ImageIcon(getClass().getResource(imageLocation));
    } catch (Exception ex) {
      System.err.println("Can't load \"" + imageLocation + "\"");
    }
    final ImageIcon icon = sandstoneIcon;
    vv =
        new VisualizationViewer<>(
            graph, KKLayoutAlgorithm.<Integer>builder().build(), new Dimension(700, 700));

    if (icon != null) {
      vv.addPreRenderPaintable(
          new VisualizationViewer.Paintable() {
            public void paint(Graphics g) {
              Dimension d = vv.getSize();
              g.drawImage(icon.getImage(), 0, 0, d.width, d.height, vv);
            }

            public boolean useTransform() {
              return false;
            }
          });
    }
    vv.addPostRenderPaintable(
        new VisualizationViewer.Paintable() {
          int x;
          int y;
          Font font;
          FontMetrics metrics;
          int swidth;
          int sheight;
          String str = "GraphZoomScrollPane Demo";

          public void paint(Graphics g) {
            Dimension d = vv.getSize();
            if (font == null) {
              font = new Font(g.getFont().getName(), Font.BOLD, 30);
              metrics = g.getFontMetrics(font);
              swidth = metrics.stringWidth(str);
              sheight = metrics.getMaxAscent() + metrics.getMaxDescent();
              x = (d.width - swidth) / 2;
              y = (int) (d.height - sheight * 1.5);
            }
            g.setFont(font);
            Color oldColor = g.getColor();
            g.setColor(Color.lightGray);
            g.drawString(str, x, y);
            g.setColor(oldColor);
          }

          public boolean useTransform() {
            return false;
          }
        });

    vv.addGraphMouseListener(new TestGraphMouseListener<>());
    vv.getRenderer()
        .setVertexRenderer(
            new GradientVertexRenderer<>(
                vv.getSelectedVertexState(),
                Color.white,
                Color.red,
                Color.white,
                Color.blue,
                false));
    vv.getRenderContext().setEdgeDrawPaintFunction(e -> Color.lightGray);
    vv.getRenderContext().setArrowFillPaintFunction(a -> Color.lightGray);
    vv.getRenderContext().setArrowDrawPaintFunction(a -> Color.lightGray);

    // add my listeners for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.setEdgeToolTipFunction(
        edge -> "E" + graph.getEdgeSource(edge) + "-" + graph.getEdgeTarget(edge));

    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderer()
        .getVertexLabelRenderer()
        .setPositioner(new BasicVertexLabelRenderer.InsidePositioner());
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.AUTO);
    vv.setForeground(Color.lightGray);

    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<Integer, Number>();
    vv.setGraphMouse(graphMouse);

    vv.addKeyListener(graphMouse.getModeKeyListener());
    vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JButton reset = new JButton("reset");
    reset.addActionListener(
        e -> {
          vv.getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(Layer.LAYOUT)
              .setToIdentity();
          vv.getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(Layer.VIEW)
              .setToIdentity();
        });

    JPanel controls = new JPanel();
    controls.add(plus);
    controls.add(minus);
    controls.add(reset);
    content.add(controls, BorderLayout.SOUTH);

    frame.pack();
    frame.setVisible(true);
  }

  Graph<Integer, Number> createGraph() {
    Graph<Integer, Number> graph =
        GraphTypeBuilder.<Integer, Number>forGraphType(DefaultGraphType.directedMultigraph())
            .buildGraph();

    IntStream.rangeClosed(0, 10).forEach(graph::addVertex);
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

  /** A nested class to demo the GraphMouseListener finding the right vertices after zoom/pan */
  static class TestGraphMouseListener<V> implements GraphMouseListener<V> {

    public void graphClicked(V v, MouseEvent me) {
      System.err.println("Vertex " + v + " was clicked at (" + me.getX() + "," + me.getY() + ")");
    }

    public void graphPressed(V v, MouseEvent me) {
      System.err.println("Vertex " + v + " was pressed at (" + me.getX() + "," + me.getY() + ")");
    }

    public void graphReleased(V v, MouseEvent me) {
      System.err.println("Vertex " + v + " was released at (" + me.getX() + "," + me.getY() + ")");
    }
  }

  public static void main(String[] args) {
    new GraphZoomScrollPaneDemo();
  }
}
