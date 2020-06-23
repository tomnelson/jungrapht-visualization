/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.samples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Paint;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.color.GreedyColoring;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.spatial.RTreeVisualization;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LayoutFunction;
import org.jungrapht.samples.util.LayoutHelper;
import org.jungrapht.samples.util.SpanningTreeAdapter;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayout;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayout;
import org.jungrapht.visualization.layout.algorithms.util.LayoutPaintable;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.util.GraphImage;

/**
 * Demonstrates graph vertex coloring for several {@link Graph}s and {@link LayoutAlgorithm}s.
 *
 * @author Tom Nelson
 */
public class ShowLayoutsWithGreedyVertexColoring extends JPanel {

  protected static Graph<String, Integer>[] graphArray;
  protected static int graphIndex;
  protected static String[] graphNames = {
    "Two component graph",
    "Random mixed-mode graph",
    "Miscellaneous multicomponent graph",
    "One component graph",
    "Chain+isolate graph",
    "Trivial (disconnected) graph",
    "Little Graph",
    "Bipartite Graph"
  };
  Paint[] colorArray =
      new Paint[] {
        Color.red,
        Color.green,
        Color.blue,
        Color.cyan,
        Color.magenta,
        Color.yellow,
        Color.pink,
        Color.gray,
        Color.darkGray,
        Color.lightGray,
        Color.orange
      };

  LayoutPaintable.BalloonRings balloonLayoutRings;
  LayoutPaintable.RadialRings radialLayoutRings;
  VertexColoringAlgorithm.Coloring<String> coloring;

  public ShowLayoutsWithGreedyVertexColoring() {

    graphArray = new Graph[graphNames.length];

    graphArray[0] = TestGraphs.createTestGraph(false);
    graphArray[1] = TestGraphs.getGeneratedGraph();
    graphArray[2] = TestGraphs.getDemoGraph();
    graphArray[3] = TestGraphs.getOneComponentGraph();
    graphArray[4] = TestGraphs.createChainPlusIsolates(18, 5);
    graphArray[5] = TestGraphs.createChainPlusIsolates(0, 20);
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedMultigraph())
            .buildGraph();

    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B", 1);
    graph.addEdge("A", "C", 2);

    graphArray[6] = graph;
    graphArray[7] = TestGraphs.getGeneratedBipartiteGraph();

    Graph<String, Integer> initialGraph = graphArray[3]; // initial graph
    coloring = new GreedyColoring<>(initialGraph).getColoring();

    final VisualizationViewer<String, Integer> vv =
        VisualizationViewer.builder(initialGraph)
            .layoutAlgorithm(new KKLayoutAlgorithm<>())
            .build();

    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setVertexFillPaintFunction(v -> colorArray[coloring.getColors().get(v)]);

    vv.setVertexToolTipFunction(
        vertex ->
            vertex
                + ". with neighbors:"
                + Graphs.neighborListOf(vv.getVisualizationModel().getGraph(), vertex));

    setLayout(new BorderLayout());
    add(vv.getComponent(), BorderLayout.CENTER);

    final JRadioButton animateLayoutTransition = new JRadioButton("Animate Layout Transition");

    LayoutFunction<String> layoutFunction = new LayoutFunction.FullLayoutFunction<>();

    final JComboBox jcb = new JComboBox(layoutFunction.getNames().toArray());
    jcb.setSelectedItem(LayoutHelper.Layouts.KK);

    jcb.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  vv.getVisualizationModel().getLayoutModel().setPreferredSize(600, 600);
                  vv.reset();
                  LayoutAlgorithm.Builder<String, ?, ?> builder =
                      layoutFunction.apply((String) jcb.getSelectedItem());
                  LayoutAlgorithm<String> layoutAlgorithm = builder.build();
                  vv.removePreRenderPaintable(balloonLayoutRings);
                  vv.removePreRenderPaintable(radialLayoutRings);
                  if ((layoutAlgorithm instanceof TreeLayout)
                      && vv.getVisualizationModel().getGraph().getType().isUndirected()) {
                    Graph<String, Integer> tree =
                        SpanningTreeAdapter.getSpanningTree(vv.getVisualizationModel().getGraph());
                    LayoutModel<String> positionModel =
                        this.getTreeLayoutPositions(tree, layoutAlgorithm);
                    vv.getVisualizationModel().getLayoutModel().setInitializer(positionModel);
                    layoutAlgorithm = new StaticLayoutAlgorithm<>();
                  }
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
                  }
                  if (layoutAlgorithm instanceof BalloonLayoutAlgorithm) {
                    balloonLayoutRings =
                        new LayoutPaintable.BalloonRings(
                            vv, (BalloonLayoutAlgorithm) layoutAlgorithm);
                    vv.addPreRenderPaintable(balloonLayoutRings);
                  }
                  if (layoutAlgorithm instanceof RadialTreeLayout) {
                    radialLayoutRings =
                        new LayoutPaintable.RadialRings(vv, (RadialTreeLayout) layoutAlgorithm);
                    vv.addPreRenderPaintable(radialLayoutRings);
                  }
                }));

    JPanel control_panel = new JPanel(new GridLayout(2, 1));
    JPanel topControls = new JPanel();
    JPanel bottomControls = new JPanel();
    control_panel.add(topControls);
    control_panel.add(bottomControls);
    add(control_panel, BorderLayout.NORTH);

    final JComboBox graphChooser = new JComboBox(graphNames);
    graphChooser.setSelectedIndex(3);

    graphChooser.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  graphIndex = graphChooser.getSelectedIndex();
                  vv.getVertexSpatial().clear();
                  vv.getEdgeSpatial().clear();
                  vv.getVisualizationModel().getLayoutModel().setSize(600, 600);
                  vv.reset();
                  vv.getVisualizationModel().setGraph(graphArray[graphIndex]);
                  coloring = new GreedyColoring<>(graphArray[graphIndex]).getColoring();
                }));

    JButton showRTree = new JButton("Show RTree");
    showRTree.addActionListener(e -> RTreeVisualization.showRTree(vv));

    JButton imageButton = new JButton("Save Image");
    imageButton.addActionListener(e -> GraphImage.capture(vv));

    topControls.add(jcb);
    topControls.add(graphChooser);
    bottomControls.add(animateLayoutTransition);
    bottomControls.add(ControlHelpers.getZoomControls("Zoom", vv));
    bottomControls.add(showRTree);
    bottomControls.add(imageButton);
  }

  LayoutModel getTreeLayoutPositions(Graph tree, LayoutAlgorithm treeLayout) {
    LayoutModel model = LayoutModel.builder().size(600, 600).graph(tree).build();
    model.accept(treeLayout);
    return model;
  }

  public static void main(String[] args) {
    JPanel jp = new ShowLayoutsWithGreedyVertexColoring();

    JFrame jf = new JFrame();
    jf.setTitle(jp.getClass().getSimpleName());
    jf.getContentPane().add(jp);
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
