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
import java.util.stream.IntStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.algorithms.orthogonal.OrthogonalLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * A demo that shows a minimal visualization configuration
 *
 * <p>Use the mouse wheel to scale Use Mouse Button 1 to pan Use CTRL-Mouse Button 1 to select
 * (select individual vertices/edges or drag a rectangular area to select contained vertices)
 *
 * @author Tom Nelson
 */
public class MinimalOrthogonal2 {

  private MinimalOrthogonal2() {

    VisualizationViewer<String, Integer> vv =
        VisualizationViewer.builder(TestGraphs.createDirectedAcyclicGraph(9, 3, .2, 5L))
            .viewSize(new Dimension(700, 700))
            .layoutAlgorithm(OrthogonalLayoutAlgorithm.<String, Integer>builder().build())
            .build();

    //    vv.getRenderContext().setVertexShapeFunction(v -> new Ellipse2D.Double(-1, -1, 2, 2));
    LayoutModel<String> layoutModel = vv.getVisualizationModel().getLayoutModel();
    vv.setVertexToolTipFunction(v -> v + " p:" + layoutModel.apply(v));
    // create a frame to hold the graph visualization
    final JFrame frame = new JFrame();
    frame.getContentPane().add(vv.getComponent());
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    new MinimalOrthogonal2();
  }

  private Graph<Integer, Integer> createGraph() {
    Graph<Integer, Integer> graph =
        GraphTypeBuilder.<Integer, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    IntStream.rangeClosed(0, 10).forEach(graph::addVertex);
    graph.addEdge(0, 1);
    graph.addEdge(3, 0);
    graph.addEdge(0, 4);
    graph.addEdge(4, 5);
    graph.addEdge(5, 3);
    graph.addEdge(2, 1);
    graph.addEdge(4, 1);
    graph.addEdge(8, 2);
    graph.addEdge(3, 8);
    graph.addEdge(6, 7);
    graph.addEdge(7, 5);
    graph.addEdge(0, 9);
    graph.addEdge(9, 8);
    graph.addEdge(7, 6);
    graph.addEdge(6, 5);
    graph.addEdge(4, 2);
    graph.addEdge(5, 4);
    graph.addEdge(4, 10);
    graph.addEdge(10, 4);

    return graph;
  }
}
