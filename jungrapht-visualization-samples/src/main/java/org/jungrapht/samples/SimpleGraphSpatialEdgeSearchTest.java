/*
 * Copyright (c) 2008, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.samples;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.BaseVisualizationModel;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.util.RadiusVertexAccessor;
import org.jungrapht.visualization.layout.util.RandomLocationTransformer;
import org.jungrapht.visualization.layout.util.VertexAccessor;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.spatial.Spatial;
import org.jungrapht.visualization.spatial.SpatialQuadTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test that puts a lot of vertices on the screen with a visible quadtree. When the button is
 * pushed, 1000 random points are generated in order to find the closest vertex for each point. The
 * search is done both with the SpatialQuadTree and with the RadiusGraphElementAccessor. If they
 * don't find the same vertex, the testing halts after highlighting the problem vertices along with
 * the search point.
 *
 * <p>A mouse click at a location will highlight the closest edge to the pick point.
 *
 * <p>A toggle button will turn on/off the display of the quadtree features, including the expansion
 * of the search target (red circle) in order to find the closest vertex.
 *
 * @author Tom Nelson
 */
public class SimpleGraphSpatialEdgeSearchTest extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(SimpleGraphSpatialEdgeSearchTest.class);

  public SimpleGraphSpatialEdgeSearchTest() {
    setLayout(new BorderLayout());

    Graph<String, Number> g = TestGraphs.getOneComponentGraph();
    Dimension viewPreferredSize = new Dimension(600, 600);
    Dimension layoutPreferredSize = new Dimension(600, 600);
    LayoutAlgorithm layoutAlgorithm = new StaticLayoutAlgorithm();

    ScalingControl scaler = new CrossoverScalingControl();
    VisualizationModel<String, Number> model =
        BaseVisualizationModel.builder(g)
            .layoutAlgorithm(layoutAlgorithm)
            .initializer(new RandomLocationTransformer(600, 600, System.currentTimeMillis()))
            .layoutSize(layoutPreferredSize)
            .build();
    VisualizationViewer<String, Number> vv =
        VisualizationViewer.builder(model).viewSize(viewPreferredSize).build();

    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);

    // use a QuadTree in this demo instead of the default R-Tree
    vv.setVertexSpatial(new SpatialQuadTree(model.getLayoutModel()));
    vv.setEdgeSpatial(new Spatial.NoOp.Edge(model));

    vv.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            MultiLayerTransformer multiLayerTransformer =
                vv.getRenderContext().getMultiLayerTransformer();
            Point2D layoutPoint = multiLayerTransformer.inverseTransform(e.getX(), e.getY());
            Number edge = vv.getEdgeSpatial().getClosestElement(layoutPoint);
            if (edge != null) {
              vv.getSelectedEdgeState().clear();
              vv.getSelectedEdgeState().select(edge);
            }
          }
        });

    JRadioButton showSpatialEffects = new JRadioButton("Show Spatial Structure");
    showSpatialEffects.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            System.err.println("TURNED ON LOGGING");
            // turn on the logging
            // programmatically set the log level so that the spatial grid is drawn for this demo and the SpatialGrid logging is output
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            ctx.getLogger("org.jungrapht.visualization.layout.spatial").setLevel(Level.DEBUG);
            ctx.getLogger("org.jungrapht.visualization.BasicVisualizationServer")
                .setLevel(Level.TRACE);
            ctx.getLogger("org.jungrapht.visualization.picking").setLevel(Level.TRACE);
            repaint();

          } else if (e.getStateChange() == ItemEvent.DESELECTED) {
            System.err.println("TURNED OFF LOGGING");
            // turn off the logging
            // programmatically set the log level so that the spatial grid is drawn for this demo and the SpatialGrid logging is output
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            ctx.getLogger("org.jungrapht.visualization.layout.spatial").setLevel(Level.INFO);
            ctx.getLogger("org.jungrapht.visualization.BasicVisualizationServer")
                .setLevel(Level.INFO);
            ctx.getLogger("org.jungrapht.visualization.picking").setLevel(Level.INFO);
            repaint();
          }
        });

    vv.scaleToLayout(scaler);
    this.add(vv);
    JPanel buttons = new JPanel();
    JButton search = new JButton("Test 1000 Searches");
    buttons.add(search);
    buttons.add(showSpatialEffects);

    search.addActionListener(
        e -> testClosestVertices(vv, g, model.getLayoutModel(), vv.getVertexSpatial()));

    this.add(buttons, BorderLayout.SOUTH);
  }

  public void testClosestVertices(
      VisualizationViewer<String, Number> vv,
      Graph<String, Number> graph,
      LayoutModel<String> layoutModel,
      Spatial<String> tree) {
    vv.getSelectedVertexState().clear();
    VertexAccessor<String> slowWay = new RadiusVertexAccessor<>(Double.MAX_VALUE);

    // look for vertices closest to 1000 random locations
    for (int i = 0; i < 1000; i++) {
      double x = Math.random() * layoutModel.getWidth();
      double y = Math.random() * layoutModel.getHeight();
      // use the slowWay
      String winnerOne = slowWay.getVertex(layoutModel, x, y);
      // use the quadtree
      String winnerTwo = tree.getClosestElement(x, y);

      log.trace("{} and {} should be the same...", winnerOne, winnerTwo);

      if (!winnerOne.equals(winnerTwo)) {
        log.info(
            "the radius distanceSq from winnerOne {} at {} to {},{} is {}",
            winnerOne,
            layoutModel.apply(winnerOne),
            x,
            y,
            layoutModel.apply(winnerOne).distanceSquared(x, y));
        log.info(
            "the radius distanceSq from winnerTwo {} at {} to {},{} is {}",
            winnerTwo,
            layoutModel.apply(winnerTwo),
            x,
            y,
            layoutModel.apply(winnerTwo).distanceSquared(x, y));

        log.info("the cell for winnerOne {} is {}", winnerOne, tree.getContainingLeaf(winnerOne));
        log.info("the cell for winnerTwo {} is {}", winnerTwo, tree.getContainingLeaf(winnerTwo));
        log.info("the cell for the search point {},{} is {}", x, y, tree.getContainingLeafs(x, y));
        vv.getSelectedVertexState().select(winnerOne);
        vv.getSelectedVertexState().select(winnerTwo);
        graph.addVertex("P");
        layoutModel.set("P", x, y);
        vv.getRenderContext().getSelectedVertexState().select("P");
        break;
      }
    }
  }

  public static void main(String[] args) {

    JFrame jf = new JFrame();

    jf.getContentPane().add(new SimpleGraphSpatialEdgeSearchTest());
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
