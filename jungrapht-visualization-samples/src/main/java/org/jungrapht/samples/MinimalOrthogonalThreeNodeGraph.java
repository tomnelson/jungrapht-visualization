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
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.algorithms.orthogonal.OrthogonalLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.util.LayoutPaintable;

/**
 * A demo that shows a minimal visualization configuration
 *
 * <p>Use the mouse wheel to scale Use Mouse Button 1 to pan Use CTRL-Mouse Button 1 to select
 * (select individual vertices/edges or drag a rectangular area to select contained vertices)
 *
 * @author Tom Nelson
 */
public class MinimalOrthogonalThreeNodeGraph {

  private MinimalOrthogonalThreeNodeGraph() {

    VisualizationViewer<String, Integer> vv =
        VisualizationViewer.builder(threeNodeGraph())
            //    VisualizationViewer.builder(TestGraphs.createDirectedAcyclicGraph(9, 3, .2, 5L))
            .viewSize(new Dimension(700, 700))
            //            .layoutAlgorithm(OrthogonalLayoutAlgorithmThreaded.<String, Integer>builder().build())
            //            .layoutAlgorithm(OrthogonalLayoutAlgorithm.<String, Integer>builder()
            //                    .vertexBoundsFunction(vv.getRenderContext().getVertexBoundsFunction())
            //                    .build())
            .build();

    vv.getVisualizationModel()
        .setLayoutAlgorithm(
            OrthogonalLayoutAlgorithm.<String, Integer>builder()
                .vertexBoundsFunction(vv.getRenderContext().getVertexBoundsFunction())
                .build());
    //    vv.getRenderContext().setVertexShapeFunction(v -> new Ellipse2D.Double(-1, -1, 2, 2));
    LayoutModel<String> layoutModel = vv.getVisualizationModel().getLayoutModel();
    vv.setVertexToolTipFunction(v -> v + " p:" + layoutModel.apply(v));
    vv.getRenderContext().setVertexLabelFunction(v -> layoutModel.apply(v).toString());

    VisualizationServer.Paintable layoutBounds = new LayoutPaintable.LayoutBounds(vv, 1, 1);

    vv.addPreRenderPaintable(layoutBounds);
    // create a frame to hold the graph visualization
    final JFrame frame = new JFrame();
    frame.getContentPane().add(vv.getComponent());
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    new MinimalOrthogonalThreeNodeGraph();
  }

  static Graph<String, Integer> threeNodeGraph() {
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    IntStream.rangeClosed(1, 3).forEach(n -> graph.addVertex("V" + n));

    graph.addEdge("V1", "V2");
    graph.addEdge("V2", "V3");
    return graph;
  }
}
