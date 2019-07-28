/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.samples;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.LayoutHelper;
import org.jungrapht.samples.util.SpanningTreeAdapter;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardRepulsion;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.LoadingCacheLayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.quadtree.BarnesHutQuadTree;
import org.jungrapht.visualization.layout.quadtree.ForceObject;
import org.jungrapht.visualization.layout.quadtree.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This demo is adapted from ShowLayouts, but when a LayoutAlgorithm that uses the BarnesHutOctTree
 * is selected, the Barnes-Hut structure is drawn on the view under the Graph. For the most dramatic
 * effect, choose the SpringBHVisitorLayoutAlgorithm, then, in picking mode, drag a vertex or
 * vertices around to watch the Barnes Hut Tree rebuild itself.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class ShowLayoutsWithBarnesHutVisualization extends JPanel {

  private static final Logger log =
      LoggerFactory.getLogger(ShowLayoutsWithBarnesHutVisualization.class);
  protected static Graph[] g_array;
  protected static int graph_index;
  protected static String[] graph_names = {
    "Two component graph",
    //    "Random mixed-mode graph",
    "Miscellaneous multicomponent graph",
    "One component graph",
    "Chain+isolate graph",
    "Trivial (disconnected) graph",
    "Little Graph"
  };

  public ShowLayoutsWithBarnesHutVisualization() {

    g_array = new Graph[graph_names.length];

    Supplier<Integer> NodeFactory =
        new Supplier<Integer>() {
          int count;

          public Integer get() {
            return count++;
          }
        };
    Supplier<Number> edgeFactory =
        new Supplier<Number>() {
          int count;

          public Number get() {
            return count++;
          }
        };

    g_array[0] = TestGraphs.createTestGraph(false);
    g_array[1] = TestGraphs.getDemoGraph();
    g_array[2] = TestGraphs.getOneComponentGraph();
    g_array[3] = TestGraphs.createChainPlusIsolates(18, 5);
    g_array[4] = TestGraphs.createChainPlusIsolates(0, 20);
    Graph network =
        GraphTypeBuilder.forGraphType(DefaultGraphType.directedMultigraph()).buildGraph();
    Stream.of("A", "B", "C").forEach(network::addVertex);
    network.addEdge("A", "B", 1);
    network.addEdge("A", "C", 2);

    g_array[5] = network;

    Graph g = g_array[2]; // initial graph

    final VisualizationViewer vv =
        (VisualizationViewer)
            VisualizationViewer.builder(g).viewSize(new Dimension(600, 600)).build();
    //    {
    //
    //          @Override
    //          public void paint(Graphics g) {
    //            updatePaintables(this);
    //            super.paint(g);
    //          }
    //        };

    VisualizationServer.Paintable paintable =
        new VisualizationServer.Paintable() {
          @Override
          public void paint(Graphics g) {
            //        log.info("paint");
            BarnesHutQuadTree tree = getBarnesHutQuadTreeFrom(vv.getModel().getLayoutAlgorithm());
            //        log.info("tree: {}", tree);
            log.info("layoutAlgorithm is {}", vv.getModel().getLayoutAlgorithm());

            if (tree != null) {
              Set<Shape> shapes = new HashSet<>();
              getShapes(shapes, tree.getRoot());

              for (Shape shape : shapes) {
                shape = vv.getTransformSupport().transform(vv, shape);

                g.setColor(Color.blue);
                ((Graphics2D) g).draw(shape);
                //            log.info("drew {}", shape);
              }
            }
          }

          @Override
          public boolean useTransform() {
            return false;
          }
        };

    vv.addPreRenderPaintable(paintable);

    vv.setBackground(Color.white);

    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    //    vv.getRenderContext().setVertexLabelPosition(Renderer.NodeLabel.Position.CNTR);

    final DefaultModalGraphMouse<Integer, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    // this reinforces that the generics (or lack of) declarations are correct
    vv.setVertexToolTipFunction(
        vertex ->
            vertex.toString()
                + ". with neighbors:"
                + Graphs.neighborListOf(vv.getModel().getGraph(), vertex));

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(
        ((DefaultModalGraphMouse<Integer, Number>) vv.getGraphMouse()).getModeListener());

    setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    add(vv, BorderLayout.CENTER);
    LayoutHelper.Layouts[] combos = LayoutHelper.getCombos();
    final JRadioButton animateLayoutTransition = new JRadioButton("Animate Layout Transition");

    final JComboBox jcb = new JComboBox(combos);
    jcb.addItemListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  if (e.getStateChange() == ItemEvent.SELECTED) {
                    log.info("item was {}", e.getItem());
                    LayoutHelper.Layouts layoutType = (LayoutHelper.Layouts) e.getItem();
                    log.info("selected layout type is {}", layoutType);
                    LayoutAlgorithm layoutAlgorithm = layoutType.getLayoutAlgorithm();
                    log.info("selected layout alg {}", layoutAlgorithm);
                    if (layoutAlgorithm instanceof TreeLayoutAlgorithm) {
                      LayoutModel positionModel =
                          this.getTreeLayoutPositions(
                              SpanningTreeAdapter.getSpanningTree(vv.getModel().getGraph()),
                              layoutAlgorithm);
                      vv.getModel().getLayoutModel().setInitializer(positionModel);
                      layoutAlgorithm = new StaticLayoutAlgorithm();
                    }
                    if (animateLayoutTransition.isSelected()) {
                      LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
                    } else {
                      LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
                    }
                  }
                }));

    jcb.setSelectedItem(LayoutHelper.Layouts.FR);

    JPanel control_panel = new JPanel(new GridLayout(2, 1));
    JPanel topControls = new JPanel();
    JPanel bottomControls = new JPanel();
    control_panel.add(topControls);
    control_panel.add(bottomControls);
    add(control_panel, BorderLayout.NORTH);

    final JComboBox graph_chooser = new JComboBox(graph_names);
    // do this before adding the listener so there is no event fired
    graph_chooser.setSelectedIndex(2);

    graph_chooser.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  graph_index = graph_chooser.getSelectedIndex();
                  vv.getVertexSpatial().clear();
                  vv.getEdgeSpatial().clear();
                  vv.getModel().setGraph(g_array[graph_index]);
                }));

    topControls.add(jcb);
    topControls.add(graph_chooser);
    bottomControls.add(animateLayoutTransition);
    bottomControls.add(plus);
    bottomControls.add(minus);
    bottomControls.add(modeBox);
  }

  // a hack because I do not want to expose the BH Tree in general
  // but i need a reference to it for this demo
  static BarnesHutQuadTree getBarnesHutQuadTreeFrom(LayoutAlgorithm layoutAlgorithm) {
    try {
      Field field = layoutAlgorithm.getClass().getDeclaredField("repulsionContract");
      if (field != null) {
        field.setAccessible(true);
        StandardRepulsion repulsionContract = (StandardRepulsion) field.get(layoutAlgorithm);
        field = repulsionContract.getClass().getDeclaredField("tree");
        if (field != null) {
          field.setAccessible(true);
          return (BarnesHutQuadTree) field.get(repulsionContract);
        }
      }
    } catch (Exception ex) {
      return null;
    }
    return null;
  }

  LayoutModel getTreeLayoutPositions(Graph tree, LayoutAlgorithm treeLayout) {
    LayoutModel model = LoadingCacheLayoutModel.builder().size(600, 600).graph(tree).build();
    model.accept(treeLayout);
    return model;
  }

  private void getShapes(Collection<Shape> shapes, Node node) {
    org.jungrapht.visualization.layout.quadtree.Rectangle bounds = node.getBounds();
    Rectangle2D r = new Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height);
    shapes.add(r);
    ForceObject forceObject = node.getForceObject();
    if (forceObject != null) {
      Point center = Point.of(node.getForceObject().p.x, node.getForceObject().p.y);
      Ellipse2D forceCenter = new Ellipse2D.Double(center.x - 4, center.y - 4, 8, 8);
      Point2D centerOfNode = new Point2D.Double((r.getCenterX()), r.getCenterY());
      Point2D centerOfForce = new Point2D.Double(center.x, center.y);
      shapes.add(new Line2D.Double(centerOfNode, centerOfForce));
      shapes.add(forceCenter);
    }
    if (node.getNW() != null) {
      getShapes(shapes, node.getNW());
    }
    if (node.getNE() != null) {
      getShapes(shapes, node.getNE());
    }
    if (node.getSW() != null) {
      getShapes(shapes, node.getSW());
    }
    if (node.getSE() != null) {
      getShapes(shapes, node.getSE());
    }
  }

  // save off the paintable so I can remove and re-create it each time
  //  VisualizationServer.Paintable paintable = null;
  //
  //  private void updatePaintables(VisualizationViewer vv) {
  //    vv.removePreRenderPaintable(paintable);
  //    BarnesHutQuadTree tree = getBarnesHutQuadTreeFrom(vv.getModel().getLayoutAlgorithm());
  //    if (tree != null) {
  //      Set<Shape> shapes = new HashSet<>();
  //      getShapes(shapes, tree.getRoot());
  //
  //      paintable =
  //          new VisualizationServer.Paintable() {
  //
  //            @Override
  //            public void paint(Graphics g) {
  //              for (Shape shape : shapes) {
  //                shape = vv.getTransformSupport().transform(vv, shape);
  //
  //                g.setColor(Color.blue);
  //                ((Graphics2D) g).draw(shape);
  //              }
  //            }
  //
  //            @Override
  //            public boolean useTransform() {
  //              return false;
  //            }
  //          };
  //      vv.addPreRenderPaintable(paintable);
  //    }
  //  }

  public static void main(String[] args) {
    JPanel jp = new ShowLayoutsWithBarnesHutVisualization();

    JFrame jf = new JFrame();
    jf.getContentPane().add(jp);
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
