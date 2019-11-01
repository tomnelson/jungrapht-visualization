package org.jungrapht.samples.large;

import java.awt.*;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutSpringRepulsion;

/**
 * demonstrates a large graph with animated layout lightweight rendering is enabled for better
 * performance
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
    VisualizationViewer<String, Integer> vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(
                SpringLayoutAlgorithm.<String>builder()
                    .repulsionContractBuilder(BarnesHutSpringRepulsion.barnesHutBuilder())
                    .build())
            .layoutSize(layoutSize)
            .viewSize(viewSize)
            .build();

    vv.scaleToLayout(new CrossoverScalingControl());

    DefaultModalGraphMouse<String, Double> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    f.getContentPane().add(vv.getComponent());
    f.setSize(viewSize);
    f.setVisible(true);
  }
}
