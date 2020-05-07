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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
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
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.decorators.IconShapeFunction;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayout;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayout;
import org.jungrapht.visualization.layout.algorithms.util.LayoutPaintable;
import org.jungrapht.visualization.layout.algorithms.util.VertexShapeAware;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.renderers.JLabelEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.JLabelVertexLabelRenderer;
import org.jungrapht.visualization.util.IconCache;

/**
 * Demonstrates several of the graph layout algorithms. Allows the user to interactively select one
 * of several graphs, and one of several layouts, and visualizes the combination.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 * @author Tom Nelson - extensive modification
 */
public class ShowLayoutsWithImageIconVertices extends JPanel {

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

  public ShowLayoutsWithImageIconVertices() {

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
            .layoutAlgorithm(new KKLayoutAlgorithm<>())
            .build();

    IconCache<String> iconCache =
        IconCache.<String>builder(n -> "<html>This<br>is a<br>multiline<br>label " + n + "</html>")
            .vertexShapeFunction(vv.getRenderContext().getVertexShapeFunction())
            .colorFunction(
                n -> {
                  if (vv.getVisualizationModel().getGraph().degreeOf(n) > 9) return Color.red;
                  if (vv.getVisualizationModel().getGraph().degreeOf(n) < 7) return Color.green;
                  return Color.lightGray;
                })
            .stylist(
                (label, vertex, colorFunction) -> {
                  label.setFont(new Font("Serif", Font.BOLD, 20));
                  label.setForeground(Color.black);
                  label.setBackground(Color.white);
                  //                  label.setOpaque(true);
                  Border lineBorder =
                      BorderFactory.createEtchedBorder(); //Border(BevelBorder.RAISED);
                  Border marginBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
                  label.setBorder(new CompoundBorder(lineBorder, marginBorder));
                })
            .preDecorator(
                (graphics, vertex, labelBounds, vertexShapeFunction, colorFunction) -> {
                  Color color = (Color) colorFunction.apply(vertex);
                  color =
                      new Color(
                          color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 4);
                  // save off the old color
                  Color oldColor = graphics.getColor();
                  // fill the image background with white
                  graphics.setPaint(Color.white);
                  graphics.fill(labelBounds);

                  Shape shape = vertexShapeFunction.apply(vertex);
                  Rectangle shapeBounds = shape.getBounds();

                  AffineTransform scale =
                      AffineTransform.getScaleInstance(
                          1.3 * labelBounds.width / shapeBounds.getWidth(),
                          1.3 * labelBounds.height / shapeBounds.getHeight());
                  AffineTransform translate =
                      AffineTransform.getTranslateInstance(
                          labelBounds.width / 2, labelBounds.height / 2);
                  translate.concatenate(scale);
                  shape = translate.createTransformedShape(shape);
                  graphics.setColor(Color.pink);
                  graphics.fill(shape);
                  graphics.setColor(oldColor);
                })
            .build();

    vv.getRenderContext().setVertexLabelRenderer(new JLabelVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new JLabelEdgeLabelRenderer(Color.cyan));

    final IconShapeFunction<String> vertexImageShapeFunction =
        new IconShapeFunction<>(new EllipseShapeFunction<>());
    vertexImageShapeFunction.setIconFunction(iconCache);

    vv.getRenderContext()
        .setVertexShapeFunction(v -> new Ellipse2D.Float(-20 / 2.f, -20 / 2.f, 20, 20));
    vv.getRenderContext().setVertexIconFunction(iconCache);
    //      vv.getRenderContext().setVertexLabelFunction(Object::toString);

    vv.setVertexToolTipFunction(
        vertex ->
            vertex
                + ". with neighbors:"
                + Graphs.neighborListOf(vv.getVisualizationModel().getGraph(), vertex));

    vv.getRenderContext()
        .setVertexShapeFunction(
            v -> {
              int size = Math.max(5, 2 * vv.getVisualizationModel().getGraph().degreeOf(v));
              return new Ellipse2D.Float(-size / 2.f, -size / 2.f, size, size);
            });

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
                  LayoutAlgorithm layoutAlgorithm = builder.build();
                  vv.removePreRenderPaintable(balloonLayoutRings);
                  vv.removePreRenderPaintable(radialLayoutRings);
                  if (layoutAlgorithm instanceof VertexShapeAware) {
                    ((VertexShapeAware<String>) layoutAlgorithm)
                        .setVertexShapeFunction(vv.getRenderContext().getVertexShapeFunction());
                  }
                  if ((layoutAlgorithm instanceof TreeLayout)
                      && vv.getVisualizationModel().getGraph().getType().isUndirected()) {
                    Graph tree =
                        SpanningTreeAdapter.getSpanningTree(vv.getVisualizationModel().getGraph());
                    LayoutModel positionModel = this.getTreeLayoutPositions(tree, layoutAlgorithm);
                    vv.getVisualizationModel().getLayoutModel().setInitializer(positionModel);
                    layoutAlgorithm = new StaticLayoutAlgorithm();
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
                  vv.getRenderContext()
                      .setVertexShapeFunction(
                          v -> {
                            int size = Math.max(5, 2 * graphArray[graphIndex].degreeOf(v));
                            return new Ellipse2D.Float(-size / 2.f, -size / 2.f, size, size);
                          });
                }));

    JButton showRTree = new JButton("Show RTree");
    showRTree.addActionListener(e -> RTreeVisualization.showRTree(vv));

    topControls.add(jcb);
    topControls.add(graphChooser);
    bottomControls.add(animateLayoutTransition);
    bottomControls.add(ControlHelpers.getZoomControls("Zoom", vv));
    bottomControls.add(showRTree);
  }

  LayoutModel getTreeLayoutPositions(Graph tree, LayoutAlgorithm treeLayout) {
    LayoutModel model = LayoutModel.builder().size(600, 600).graph(tree).build();
    model.accept(treeLayout);
    return model;
  }

  public static void main(String[] args) {
    JPanel jp = new ShowLayoutsWithImageIconVertices();

    JFrame jf = new JFrame();
    jf.getContentPane().add(jp);
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
