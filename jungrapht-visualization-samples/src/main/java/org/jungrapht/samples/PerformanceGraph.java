package org.jungrapht.samples;

import java.awt.*;
import java.util.Random;
import java.util.stream.IntStream;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFRRepulsion;
import org.jungrapht.visualization.util.LightweightRenderingVisitor;

public class PerformanceGraph {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(PerformanceGraph::createAndShowGUI);
  }

  private static void createAndShowGUI() {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    Graph<String, String> g = createGraph();

    Dimension size = new Dimension(800, 800);
    VisualizationViewer<String, String> vv =
        new VisualizationViewer<>(
            g,
            FRLayoutAlgorithm.<String>builder()
                .repulsionContractBuilder(BarnesHutFRRepulsion.barnesHutBuilder())
                .build(),
            size,
            size);

    LightweightRenderingVisitor.visit(vv);

    DefaultModalGraphMouse<String, Double> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    f.getContentPane().add(vv);
    f.setSize(size);
    f.setLocationRelativeTo(null);
    f.setVisible(true);
  }

  // This method summarizes several options
  public static Graph<String, String> createGraph() {
    Random random = new Random(0);
    int numVertices = 2500;
    int numEdges = 5000;
    Graph<String, String> g =
        GraphTypeBuilder.<String, String>forGraphType(DefaultGraphType.directedPseudograph())
            .buildGraph();
    IntStream.range(0, numVertices).forEach(i -> g.addVertex("v" + i));

    for (int i = 0; i < numEdges; i++) {
      int v0 = random.nextInt(numVertices);
      int v1 = random.nextInt(numVertices);

      g.addEdge("v" + v0, "v" + v1, "e" + i);
    }
    return g;
  }
}
