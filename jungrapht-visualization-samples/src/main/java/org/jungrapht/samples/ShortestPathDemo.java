/*
 * Created on Jan 2, 2004
 */
package org.jungrapht.samples;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.AbstractModalGraphMouse;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;

/** Demonstrates use of the JGrapht shortest path algorithm and visualization of the results. */
public class ShortestPathDemo extends JPanel {

  /** */
  private static final long serialVersionUID = 7526217664458188502L;

  /** Starting node */
  private String fromNode;

  /** Ending node */
  private String toNode;

  private Graph<String, Number> network;
  private Set<String> path = new HashSet<>();

  private final Stroke THIN = new BasicStroke(1);
  private final Stroke THICK = new BasicStroke(1);

  private ShortestPathDemo() {

    this.network = getNetwork();
    setBackground(Color.WHITE);

    final LayoutAlgorithm<String> layoutAlgorithm = FRLayoutAlgorithm.<String>builder().build();
    final VisualizationViewer<String, Number> vv =
        new VisualizationViewer<>(network, layoutAlgorithm, new Dimension(1000, 1000));
    vv.setBackground(Color.WHITE);

    vv.getRenderContext().setNodeDrawPaintFunction(n -> Color.black);
    vv.getRenderContext()
        .setNodeFillPaintFunction(
            n -> {
              if (n.equals(fromNode)) {
                return Color.BLUE;
              }
              if (n.equals(toNode)) {
                return Color.BLUE;
              }
              if (path == null) {
                return Color.LIGHT_GRAY;
              } else {
                if (path.contains(n)) {
                  return Color.RED;
                } else {
                  return Color.LIGHT_GRAY;
                }
              }
            });
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            e -> {
              if (path == null || path.size() == 0) {
                return Color.BLACK;
              }
              if (onShortestPath(e)) {
                return new Color(0.0f, 0.0f, 1.0f, 0.5f); //Color.BLUE;
              } else {
                return Color.LIGHT_GRAY;
              }
            });

    vv.getRenderContext()
        .setEdgeStrokeFunction(
            e -> {
              if (path == null || path.size() == 0) {
                return THIN;
              }
              if (onShortestPath(e)) {
                return THICK;
              } else {
                return THIN;
              }
            });
    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<Integer, Number>();
    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    vv.getSelectedNodeState()
        .addItemListener(
            e -> {
              if (e.getStateChange() == ItemEvent.DESELECTED) {
                if (e.getItem().equals(fromNode)) {
                  fromNode = null;
                } else if (e.getItem().equals(toNode)) {
                  toNode = null;
                }
              } else if (e.getStateChange() == ItemEvent.SELECTED) {
                String item = (String) e.getItem();
                if (fromNode == null) {
                  fromNode = item;
                } else {
                  toNode = item;
                }
              }
              drawShortestPath();
            });

    vv.addPostRenderPaintable(
        new VisualizationViewer.Paintable() {

          public boolean useTransform() {
            return true;
          }

          public void paint(Graphics g) {
            if (path == null) {
              return;
            }

            // for all edges, paint edges that are in shortest path
            for (Number e : network.edgeSet()) {
              if (onShortestPath(e)) {
                Renderer<String, Number> renderer = vv.getRenderer();
                renderer.renderEdge(vv.getRenderContext(), vv.getModel(), e);
              }
            }
          }
        });

    setLayout(new BorderLayout());
    add(vv, BorderLayout.CENTER);

    String labelString =
        "<html><ul><li>Type 'p' to enter picking mode"
            + "<li>pick the first node by clicking on it"
            + "<li>pick the second node by Shift-clicking on it"
            + "<li>Type 't' to return to transforming mode";
    JLabel instructions = new JLabel(labelString);
    add(instructions, BorderLayout.SOUTH);
  }

  private boolean onShortestPath(Number e) {
    String v1 = network.getEdgeSource(e);
    String v2 = network.getEdgeTarget(e);
    return !v1.equals(v2) && path.contains(v1) && path.contains(v2);
  }

  /** */
  private void drawShortestPath() {

    if (fromNode == null || toNode == null) {
      path.clear();
      return;
    }
    path.clear();
    BFSShortestPath<String, Number> bfsShortestPath = new BFSShortestPath<>(this.network);
    GraphPath<String, Number> path = bfsShortestPath.getPath(fromNode, toNode);
    this.path.addAll(path.getVertexList());
    repaint();
  }

  public static void main(String[] s) {
    JFrame jf = new JFrame();
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.getContentPane().add(new ShortestPathDemo());
    jf.pack();
    jf.setVisible(true);
  }

  /** @return the network for this demo */
  private Graph<String, Number> getNetwork() {
    Graph<String, Number> network =
        GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.pseudograph())
            .vertexSupplier(new NodeSupplier())
            .edgeSupplier(new EdgeSupplier())
            .buildGraph();

    BarabasiAlbertGraphGenerator<String, Number> gen = new BarabasiAlbertGraphGenerator<>(4, 3, 20);
    gen.generateGraph(network, null);
    return network;
  }

  static class NodeSupplier implements Supplier<String> {
    char a = 'a';

    public String get() {
      return Character.toString(a++);
    }
  }

  static class EdgeSupplier implements Supplier<Number> {
    int count;

    public Number get() {
      return count++;
    }
  }
}
