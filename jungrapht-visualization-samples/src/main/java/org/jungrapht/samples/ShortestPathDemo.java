/*
 * Created on Jan 2, 2004
 */
package org.jungrapht.samples;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.AbstractModalGraphMouse;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.selection.SelectedState;

/** Demonstrates use of the JGrapht shortest path algorithm and visualization of the results. */
public class ShortestPathDemo extends JPanel {

  /** */
  private static final long serialVersionUID = 7526217664458188502L;

  /** Starting vertex */
  private String fromVertex;

  /** Ending vertex */
  private String toVertex;

  private Graph<String, Integer> graph;
  private Set<String> path = new HashSet<>();

  private final Stroke THIN = new BasicStroke(1);
  private final Stroke THICK = new BasicStroke(1);

  private ShortestPathDemo() {

    this.graph = getGraph();
    setBackground(Color.WHITE);

    final LayoutAlgorithm<String> layoutAlgorithm = new FRLayoutAlgorithm<>();

    final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<Integer, Number>();

    final VisualizationViewer<String, Integer> vv =
        VisualizationViewer.builder(graph)
            .graphMouse(graphMouse)
            .layoutAlgorithm(layoutAlgorithm)
            .viewSize(new Dimension(1000, 1000))
            .build();
    vv.setBackground(Color.WHITE);

    vv.getRenderContext().setVertexDrawPaintFunction(n -> Color.black);
    vv.getRenderContext()
        .setVertexFillPaintFunction(
            n -> {
              if (n.equals(fromVertex)) {
                return Color.BLUE;
              }
              if (n.equals(toVertex)) {
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
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getSelectedVertexState()
        .addItemListener(new SelectedState.StateChangeListener<>(this::select, this::deselect));

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
            for (Integer e : graph.edgeSet()) {
              if (onShortestPath(e)) {
                Renderer<String, Integer> renderer = vv.getRenderer();
                renderer.renderEdge(vv.getRenderContext(), vv.getVisualizationModel(), e);
              }
            }
          }
        });

    setLayout(new BorderLayout());
    add(vv.getComponent(), BorderLayout.CENTER);

    String labelString =
        "<html><ul><li>Type 'p' to enter picking mode"
            + "<li>pick the first vertex by clicking on it"
            + "<li>pick the second vertex by Shift-clicking on it"
            + "<li>Type 't' to return to transforming mode";
    JLabel instructions = new JLabel(labelString);
    add(instructions, BorderLayout.SOUTH);
  }

  private void select(Object item) {
    if (item instanceof String) {
      select((String) item);
    } else if (item instanceof Collection) {
      select((Collection<String>) item);
    }
    drawShortestPath();
  }

  private void select(String item) {
    if (fromVertex == null) {
      fromVertex = item;
    } else {
      toVertex = item;
    }
  }

  private void select(Collection<String> items) {
    items.stream().findFirst().ifPresent(this::select);
  }

  private void deselect(Object item) {
    if (item instanceof String) {
      deselect((String) item);
    } else if (item instanceof Collection) {
      deselect((Collection<String>) item);
    }
    drawShortestPath();
  }

  private void deselect(Collection<String> items) {
    items.stream().findFirst().ifPresent(this::deselect);
  }

  private void deselect(String e) {
    if (Objects.equals(e, fromVertex)) {
      fromVertex = null;
    } else if (Objects.equals(e, toVertex)) {
      toVertex = null;
    }
  }

  private boolean onShortestPath(Integer e) {
    String v1 = graph.getEdgeSource(e);
    String v2 = graph.getEdgeTarget(e);
    return !v1.equals(v2) && path.contains(v1) && path.contains(v2);
  }

  /** */
  private void drawShortestPath() {

    if (fromVertex == null || toVertex == null) {
      path.clear();
      return;
    }
    path.clear();
    BFSShortestPath<String, Integer> bfsShortestPath = new BFSShortestPath<>(this.graph);
    GraphPath<String, Integer> path = bfsShortestPath.getPath(fromVertex, toVertex);
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

  /** @return the graph for this demo */
  private Graph<String, Integer> getGraph() {
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.pseudograph())
            .vertexSupplier(new VertexSupplier())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    BarabasiAlbertGraphGenerator<String, Integer> gen =
        new BarabasiAlbertGraphGenerator<>(4, 3, 20);
    gen.generateGraph(graph, null);
    return graph;
  }

  static class VertexSupplier implements Supplier<String> {
    char a = 'a';

    public String get() {
      return Character.toString(a++);
    }
  }
}
