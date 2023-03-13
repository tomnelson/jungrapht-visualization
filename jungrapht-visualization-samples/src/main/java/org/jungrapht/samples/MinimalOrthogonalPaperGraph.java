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
import org.jungrapht.samples.util.LayoutGrid;
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
public class MinimalOrthogonalPaperGraph {

  private MinimalOrthogonalPaperGraph() {

    VisualizationViewer<String, Integer> vv =
        VisualizationViewer.builder(paperGraph())
            //    VisualizationViewer.builder(TestGraphs.createDirectedAcyclicGraph(9, 3, .2, 5L))
            .viewSize(new Dimension(700, 700))
            //            .layoutAlgorithm(OrthogonalLayoutAlgorithmThreaded.<String, Integer>builder().build())
            //            .layoutAlgorithm(OrthogonalLayoutAlgorithm.<String, Integer>builder().build())
            .build();
    vv.getVisualizationModel()
        .setLayoutAlgorithm(
            OrthogonalLayoutAlgorithm.<String, Integer>builder()
                .vertexBoundsFunction(vv.getRenderContext().getVertexBoundsFunction())
                .build());
    //    vv.addPreRenderPaintable(new LayoutGrid(vv, 12));
    vv.addPreRenderPaintable(new LayoutGrid(vv));

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
    new MinimalOrthogonalPaperGraph();
  }

  static Graph<String, Integer> paperGraph() {
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    IntStream.rangeClosed(1, 6).forEach(n -> graph.addVertex("V" + n));

    graph.addEdge("V1", "V2");
    graph.addEdge("V1", "V6");
    graph.addEdge("V2", "V4");
    graph.addEdge("V2", "V3");
    graph.addEdge("V2", "V5");
    graph.addEdge("V2", "V6");
    graph.addEdge("V3", "V4");
    graph.addEdge("V3", "V5");
    graph.addEdge("V4", "V1");
    graph.addEdge("V4", "V6");
    graph.addEdge("V5", "V4");
    graph.addEdge("V6", "V5");
    return graph;
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
