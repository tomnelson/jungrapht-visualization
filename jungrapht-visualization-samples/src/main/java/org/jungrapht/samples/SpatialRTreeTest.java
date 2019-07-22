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
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.BaseVisualizationModel;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.EditingModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.util.NetworkNodeAccessor;
import org.jungrapht.visualization.layout.util.RadiusNetworkNodeAccessor;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.spatial.Spatial;
import org.jungrapht.visualization.spatial.SpatialQuadTree;
import org.jungrapht.visualization.spatial.SpatialRTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test that puts a lot of nodes on the screen with a visible quadtree. When the button is pushed,
 * 1000 random points are generated in order to find the closest node for each point. The search is
 * done both with the SpatialQuadTree and with the RadiusNetworkElementAccessor. If they don't find
 * the same node, the testing halts after highlighting the problem nodes along with the search
 * point.
 *
 * <p>A mouse click at a location will highlight the closest node to the pick point.
 *
 * <p>A toggle button will turn on/off the display of the spatialquadtree features, including the
 * expansion of the search target (red circle) in order to find the closest node.
 *
 * @author Tom Nelson
 */
public class SpatialRTreeTest extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(SpatialRTreeTest.class);

  public SpatialRTreeTest() {
    setLayout(new BorderLayout());
    Graph<String, Number> g =
        GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.directedMultigraph())
            .buildGraph();
    Dimension viewPreferredSize = new Dimension(600, 600);
    Dimension layoutPreferredSize = new Dimension(600, 600);
    LayoutAlgorithm layoutAlgorithm = new StaticLayoutAlgorithm();

    ScalingControl scaler = new CrossoverScalingControl();
    VisualizationModel model =
        new BaseVisualizationModel(g, layoutAlgorithm, null, layoutPreferredSize);
    VisualizationViewer vv = new VisualizationViewer(model, viewPreferredSize);

    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    vv.getRenderContext().setNodeLabelPosition(Renderer.NodeLabel.Position.CNTR);

    Supplier<String> nodeFactory = new NodeFactory();
    Supplier<Number> edgeFactory = new EdgeFactory();

    final EditingModalGraphMouse<String, Number> graphMouse =
        new EditingModalGraphMouse<>(vv.getRenderContext(), nodeFactory, edgeFactory);

    // the EditingGraphMouse will pass mouse event coordinates to the
    // nodeLocations function to set the locations of the nodes as
    // they are created
    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

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
            ctx.getLogger("org.jungrapht.visualization.layout.spatial.rtree").setLevel(Level.DEBUG);

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
            ctx.getLogger("org.jungrapht.visualization.layout.spatial.rtree").setLevel(Level.INFO);

            ctx.getLogger("org.jungrapht.visualization.BasicVisualizationServer")
                .setLevel(Level.INFO);
            ctx.getLogger("org.jungrapht.visualization.picking").setLevel(Level.INFO);
            repaint();
          }
        });

    JComboBox<ModalGraphMouse.Mode> modeBox = graphMouse.getModeComboBox();

    JButton recalculate = new JButton("Recalculate");
    recalculate.addActionListener(e -> vv.getNodeSpatial().recalculate());
    vv.scaleToLayout(scaler);
    this.add(vv);
    JPanel buttons = new JPanel();
    JButton search = new JButton("Test 1000 Searches");
    buttons.add(search);
    buttons.add(showSpatialEffects);
    buttons.add(recalculate);
    buttons.add(modeBox);

    Spatial spatial = vv.getNodeSpatial();
    if (spatial instanceof SpatialQuadTree) {
      search.addActionListener(
          e -> testClosestNodes(vv, g, model.getLayoutModel(), (SpatialQuadTree<String>) spatial));
    } else if (spatial instanceof SpatialRTree.Nodes) {
      search.addActionListener(
          e ->
              testClosestNodes(
                  vv, g, model.getLayoutModel(), (SpatialRTree.Nodes<String>) spatial));
    }

    this.add(buttons, BorderLayout.SOUTH);
  }

  public void testClosestNodes(
      VisualizationViewer<String, String> vv,
      Graph<String, Number> graph,
      LayoutModel<String> layoutModel,
      SpatialQuadTree<String> tree) {
    vv.getSelectedNodeState().clear();
    NetworkNodeAccessor<String> slowWay = new RadiusNetworkNodeAccessor<>(Double.MAX_VALUE);

    // look for nodes closest to 1000 random locations
    for (int i = 0; i < 1000; i++) {
      double x = Math.random() * layoutModel.getWidth();
      double y = Math.random() * layoutModel.getHeight();
      // use the slowWay
      String winnerOne = slowWay.getNode(layoutModel, x, y);
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

        log.info(
            "the cell for winnerOne {} is {}",
            winnerOne,
            tree.getContainingQuadTreeLeaf(winnerOne));
        log.info(
            "the cell for winnerTwo {} is {}",
            winnerTwo,
            tree.getContainingQuadTreeLeaf(winnerTwo));
        log.info(
            "the cell for the search point {},{} is {}",
            x,
            y,
            tree.getContainingQuadTreeLeaf(x, y));
        vv.getSelectedNodeState().pick(winnerOne, true);
        vv.getSelectedNodeState().pick(winnerTwo, true);
        graph.addVertex("P");
        layoutModel.set("P", x, y);
        vv.getRenderContext().getSelectedNodeState().pick("P", true);
        break;
      }
    }
  }

  public void testClosestNodes(
      VisualizationViewer<String, String> vv,
      Graph<String, Number> graph,
      LayoutModel<String> layoutModel,
      SpatialRTree.Nodes<String> tree) {
    vv.getSelectedNodeState().clear();
    NetworkNodeAccessor<String> slowWay = new RadiusNetworkNodeAccessor<>(Double.MAX_VALUE);

    // look for nodes closest to 1000 random locations
    for (int i = 0; i < 1000; i++) {
      double x = Math.random() * layoutModel.getWidth();
      double y = Math.random() * layoutModel.getHeight();
      // use the slowWay
      String winnerOne = slowWay.getNode(layoutModel, x, y);
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
        vv.getSelectedNodeState().pick(winnerOne, true);
        vv.getSelectedNodeState().pick(winnerTwo, true);
        graph.addVertex("P");
        layoutModel.set("P", x, y);
        vv.getRenderContext().getSelectedNodeState().pick("P", true);
        break;
      }
    }
  }

  class NodeFactory implements Supplier<String> {

    int i = 0;

    public String get() {
      return "N" + i++;
    }
  }

  class EdgeFactory implements Supplier<Number> {

    int i = 0;

    public Number get() {
      return i++;
    }
  }

  public static void main(String[] args) {

    JFrame jf = new JFrame();

    jf.getContentPane().add(new SpatialRTreeTest());
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
