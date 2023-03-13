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
public class ThreeVertexOrthogonal {

  private ThreeVertexOrthogonal() {

    VisualizationViewer<Integer, Integer> vv =
        VisualizationViewer.builder(createGraph())
            .viewSize(new Dimension(700, 700))
            .layoutAlgorithm(OrthogonalLayoutAlgorithm.<Integer, Integer>builder().build())
            .build();

    LayoutModel<Integer> layoutModel = vv.getVisualizationModel().getLayoutModel();
    vv.setVertexToolTipFunction(v -> v + " p:" + layoutModel.apply(v));
    // create a frame to hold the graph visualization
    final JFrame frame = new JFrame();
    frame.getContentPane().add(vv.getComponent());
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    new ThreeVertexOrthogonal();
  }

  private Graph<Integer, Integer> createGraph() {
    Graph<Integer, Integer> graph =
        GraphTypeBuilder.<Integer, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    IntStream.rangeClosed(0, 2).forEach(graph::addVertex);
    graph.addEdge(0, 1);
    graph.addEdge(1, 2);

    return graph;
  }
}
