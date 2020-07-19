/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.samples;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.spatial.RTreeVisualization;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LayoutFunction;
import org.jungrapht.samples.util.LayoutHelper;
import org.jungrapht.samples.util.SpanningTreeAdapter;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.algorithms.*;
import org.jungrapht.visualization.layout.algorithms.util.InitialDimensionFunction;
import org.jungrapht.visualization.layout.algorithms.util.LayoutPaintable;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.util.GraphImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates several of the graph layout algorithms. Allows the user to interactively select one
 * of several graphs, and one of several layouts, and visualizes the combination.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 * @author Tom Nelson - extensive modification
 */
public class ShowLayouts extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(ShowLayouts.class);

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

  LayoutPaintable.BalloonRings balloonLayoutRings;
  LayoutPaintable.RadialRings radialLayoutRings;
  LayoutPaintable.LayoutBounds layoutBounds;

  public ShowLayouts() {

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

    final VisualizationViewer<String, Integer> vv =
        VisualizationViewer.builder(initialGraph)
            .initialDimensionFunction(new InitialDimensionFunction<>())
            .layoutAlgorithm(new KKLayoutAlgorithm<>())
            .build();

    vv.getRenderContext().setVertexLabelFunction(Object::toString);

    vv.setVertexToolTipFunction(
        vertex ->
            vertex
                + ". with neighbors:"
                + Graphs.neighborListOf(vv.getVisualizationModel().getGraph(), vertex));

    vv.getRenderContext()
        .setVertexShapeFunction(
            v -> {
              Graph<String, Integer> g = vv.getVisualizationModel().getGraph();
              if (!g.vertexSet().contains(v)) {
                log.error("shapeFunction {} was not in {}", v, g.vertexSet());
              }
              int size = Math.max(5, 2 * (g.vertexSet().contains(v) ? g.degreeOf(v) : 20));
              return new Ellipse2D.Float(-size / 2.f, -size / 2.f, size, size);
            });

    vv.setInitialDimensionFunction(
        new InitialDimensionFunction<>(vv.getRenderContext().getVertexShapeFunction()));

    layoutBounds = new LayoutPaintable.LayoutBounds(vv);
    vv.addPreRenderPaintable(layoutBounds);
    // for the initial layout
    vv.scaleToLayout();

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
                  vv.removePreRenderPaintable(layoutBounds);
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
                  layoutBounds = new LayoutPaintable.LayoutBounds(vv);
                  vv.addPreRenderPaintable(layoutBounds);
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
                  //                  vv.getVisualizationModel().getLayoutModel().setSize(600, 600);
                  //                  vv.reset();
                  vv.getVisualizationModel().setGraph(graphArray[graphIndex]);
                  vv.getRenderContext()
                      .setVertexShapeFunction(
                          v -> {
                            int size =
                                Math.max(5, 2 * vv.getVisualizationModel().getGraph().degreeOf(v));
                            return new Ellipse2D.Float(-size / 2.f, -size / 2.f, size, size);
                          });
                }));

    JButton showRTree = new JButton("Show RTree");
    showRTree.addActionListener(e -> RTreeVisualization.showRTree(vv));

    JButton imageButton = new JButton("Save Image");
    imageButton.addActionListener(e -> GraphImage.capture(vv));

    JButton scaleToLayoutButton = new JButton("ScaleToLayout");
    scaleToLayoutButton.addActionListener(evt -> vv.scaleToLayout());

    topControls.add(jcb);
    topControls.add(graphChooser);
    bottomControls.add(animateLayoutTransition);
    bottomControls.add(ControlHelpers.getZoomControls("Zoom", vv));
    bottomControls.add(showRTree);
    bottomControls.add(scaleToLayoutButton);
  }

  LayoutModel getTreeLayoutPositions(Graph tree, LayoutAlgorithm treeLayout) {
    LayoutModel model = LayoutModel.builder().size(600, 600).graph(tree).build();
    model.accept(treeLayout);
    return model;
  }

  public static void main(String[] args) {
    JPanel jp = new ShowLayouts();

    JFrame jf = new JFrame();
    jf.setTitle(jp.getClass().getSimpleName());
    jf.getContentPane().add(jp);
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
