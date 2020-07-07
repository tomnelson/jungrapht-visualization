package org.jungrapht.samples.large;

import java.awt.*;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardSpringRepulsion;

/**
 * First demo I made that shows how the Lightweight rendering and the BH optimization of the layout
 * algorithm can acceptably render a larger graph.
 */
public class PerformanceGraph {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(PerformanceGraph::createAndShowGUI);
  }

  private static void createAndShowGUI() {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    Graph<String, Integer> graph = TestGraphs.getGeneratedGraph2();

    Dimension layoutSize = new Dimension(1600, 1600);
    Dimension viewSize = new Dimension(800, 800);
    DefaultModalGraphMouse<String, Double> graphMouse = new DefaultModalGraphMouse<>();

    VisualizationViewer<String, Integer> vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(
                SpringLayoutAlgorithm.<String, Integer>builder()
                    .repulsionContractBuilder(StandardSpringRepulsion.builder())
                    .build())
            .layoutSize(layoutSize)
            .viewSize(viewSize)
            .graphMouse(graphMouse)
            .build();

    vv.scaleToLayout();

    f.getContentPane().add(vv.getComponent());
    f.setSize(viewSize);
    f.setVisible(true);
  }
}
