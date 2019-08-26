/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples.spatial;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.spatial.Spatial;
import org.jungrapht.visualization.spatial.SpatialRTree;
import org.jungrapht.visualization.spatial.rtree.InnerNode;
import org.jungrapht.visualization.spatial.rtree.LeafNode;
import org.jungrapht.visualization.spatial.rtree.Node;
import org.jungrapht.visualization.spatial.rtree.RTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Visualization for the RTree used in a graph visualization
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class RTreeVisualization<V> extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(RTreeVisualization.class);
  Graph<Object, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Object, Integer> vv;

  VisualizationServer.Paintable rings;

  VisualizationServer.Paintable balloonRings;

  TreeLayoutAlgorithm<Object> treeLayoutAlgorithm;

  RTree rtree;

  RadialTreeLayoutAlgorithm<Object> radialLayoutAlgorithm;

  BalloonLayoutAlgorithm<Object> balloonLayoutAlgorithm;

  Map<Node, VisualizationServer.Paintable> thePaintables = new HashMap<>();

  VisualizationServer othervv;

  public RTreeVisualization(RTree rtree, VisualizationServer othervv) {
    this.rtree = rtree;
    this.othervv = othervv;

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = createTreeFromRTree(rtree);

    treeLayoutAlgorithm = TreeLayoutAlgorithm.builder().verticalVertexSpacing(200).build();
    radialLayoutAlgorithm = RadialTreeLayoutAlgorithm.builder().build();
    balloonLayoutAlgorithm = BalloonLayoutAlgorithm.builder().build();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(balloonLayoutAlgorithm)
            .layoutSize(new Dimension(2400, 2400))
            .viewSize(new Dimension(600, 600))
            .build();
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.orthogonal());
    vv.getRenderContext().setVertexShapeFunction(v -> new Rectangle2D.Double(-10, -10, 20, 20));

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);
    vv.setVertexSpatial(new Spatial.NoOp.Vertex(vv.getVisualizationModel().getLayoutModel()));
    vv.setEdgeSpatial(new Spatial.NoOp.Edge(vv.getVisualizationModel()));

    vv.scaleToLayout();

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    final DefaultGraphMouse<Object, Integer> graphMouse = new DefaultGraphMouse<>();

    vv.setGraphMouse(graphMouse);
    MutableSelectedState selectedState = othervv.getSelectedVertexState();
    selectedState.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.DESELECTED) {
            Object unpicked = e.getItem();
            if (unpicked instanceof Collection) {
              Collection selections = (Collection) unpicked;
              selections.forEach(
                  selection -> {
                    if (selection instanceof Node) {
                      Node node = (Node) selection;
                      othervv.removePreRenderPaintable(thePaintables.get(node));
                      thePaintables.remove(node);
                    }
                  });
            } else if (unpicked instanceof Node) {
              othervv.removePreRenderPaintable(thePaintables.get(unpicked));
              thePaintables.remove(unpicked);
            }
          }
          if (e.getStateChange() == ItemEvent.SELECTED) {
            Object picked = e.getItem();
            if (picked instanceof Collection) {
              Collection selections = (Collection) picked;
              selections.forEach(
                  selection -> {
                    if (selection instanceof Node) {
                      Node node = (Node) selection;
                      RTreePaintable paintable = new RTreePaintable(node);
                      othervv.addPreRenderPaintable(paintable);
                      thePaintables.put(node, paintable);
                    }
                  });
            } else if (picked instanceof Node) {
              Node node = (Node) picked;
              RTreePaintable paintable = new RTreePaintable(node);
              othervv.addPreRenderPaintable(paintable);
              thePaintables.put(node, paintable);
            }
          }
        });
    vv.setSelectedVertexState(othervv.getSelectedVertexState());

    JRadioButton animate = new JRadioButton("Animate Transition");
    JRadioButton treeLayout = new JRadioButton("Tree Layout");
    treeLayout.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            vv.getRenderContext().setEdgeShapeFunction(EdgeShape.orthogonal());
            if (animate.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, treeLayoutAlgorithm, () -> {});
            } else {
              LayoutAlgorithmTransition.apply(vv, treeLayoutAlgorithm, () -> {});
            }
            if (rings != null) {
              vv.removePreRenderPaintable(rings);
            }
            if (balloonRings != null) {
              vv.removePreRenderPaintable(balloonRings);
            }
          }
        });
    JRadioButton radialLayout = new JRadioButton("Radial Layout");
    radialLayout.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
            if (animate.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, radialLayoutAlgorithm, vv::scaleToLayout);
            } else {
              LayoutAlgorithmTransition.apply(vv, radialLayoutAlgorithm, vv::scaleToLayout);
            }
            if (rings == null) {
              rings = new Rings(vv.getVisualizationModel().getLayoutModel());
            }
            if (balloonRings != null) {
              vv.removePreRenderPaintable(balloonRings);
            }
            vv.addPreRenderPaintable(rings);
          }
        });
    JRadioButton balloonLayout = new JRadioButton("Balloon Layout");
    balloonLayout.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
            if (animate.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, balloonLayoutAlgorithm, vv::scaleToLayout);
            } else {
              LayoutAlgorithmTransition.apply(vv, balloonLayoutAlgorithm, vv::scaleToLayout);
            }
            if (balloonRings == null) {
              balloonRings = new BalloonRings(balloonLayoutAlgorithm);
            }
            if (rings != null) {
              vv.removePreRenderPaintable(rings);
            }
            vv.addPreRenderPaintable(balloonRings);
          }
        });
    ButtonGroup radio = new ButtonGroup();
    radio.add(treeLayout);
    radio.add(radialLayout);
    radio.add(balloonLayout);
    balloonLayout.setSelected(true);

    JPanel layoutPanel = new JPanel(new GridLayout(2, 1));
    layoutPanel.add(treeLayout);
    layoutPanel.add(radialLayout);
    layoutPanel.add(balloonLayout);
    layoutPanel.add(animate);
    JPanel controls = new JPanel();
    controls.add(layoutPanel);
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));

    add(controls, BorderLayout.SOUTH);
  }

  class RTreePaintable implements VisualizationServer.Paintable {
    Node node;

    RTreePaintable(Node node) {
      this.node = node;
    }

    @Override
    public void paint(Graphics g) {
      g.setColor(Color.magenta);
      Shape shape = node.getBounds();
      shape = othervv.getRenderContext().getMultiLayerTransformer().transform(shape);
      ((Graphics2D) g).draw(shape);
    }

    @Override
    public boolean useTransform() {
      return false;
    }
  }

  class Rings implements VisualizationServer.Paintable {

    Collection<Double> depths;
    LayoutModel<Object> layoutModel;

    public Rings(LayoutModel<Object> layoutModel) {
      this.layoutModel = layoutModel;
      depths = getDepths();
    }

    private Collection<Double> getDepths() {
      Set<Double> depths = new HashSet<>();
      Map<Object, PolarPoint> polarLocations = radialLayoutAlgorithm.getPolarLocations();
      for (Object v : graph.vertexSet()) {
        PolarPoint pp = polarLocations.get(v);
        depths.add(pp.radius);
      }
      return depths;
    }

    public void paint(Graphics g) {
      g.setColor(Color.lightGray);

      Graphics2D g2d = (Graphics2D) g;
      Point center = radialLayoutAlgorithm.getCenter(layoutModel);

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (double d : depths) {
        ellipse.setFrameFromDiagonal(center.x - d, center.y - d, center.x + d, center.y + d);
        Shape shape =
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT)
                .transform(ellipse);
        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return true;
    }
  }

  class BalloonRings implements VisualizationServer.Paintable {

    BalloonLayoutAlgorithm<Object> layoutAlgorithm;

    public BalloonRings(BalloonLayoutAlgorithm<Object> layoutAlgorithm) {
      this.layoutAlgorithm = layoutAlgorithm;
    }

    public void paint(Graphics g) {
      g.setColor(Color.gray);

      Graphics2D g2d = (Graphics2D) g;

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (Object v : vv.getVisualizationModel().getGraph().vertexSet()) {
        Double radius = layoutAlgorithm.getRadii().get(v);
        if (radius == null) {
          continue;
        }
        Point p = vv.getVisualizationModel().getLayoutModel().apply(v);
        ellipse.setFrame(-radius, -radius, 2 * radius, 2 * radius);
        AffineTransform at = AffineTransform.getTranslateInstance(p.x, p.y);
        Shape shape = at.createTransformedShape(ellipse);
        shape = vv.getTransformSupport().transform(vv, shape, Layer.LAYOUT);
        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return true;
    }
  }

  private Graph<Object, Integer> createTreeFromRTree(RTree<V> rtree) {
    Graph<Object, Integer> tree =
        GraphTypeBuilder.<Object, Integer>forGraphType(DefaultGraphType.dag()).buildGraph();

    if (rtree.getRoot().isPresent()) {
      addChildren(tree, rtree.getRoot().get());
    }

    return tree;
  }

  private void addChildren(Graph<Object, Integer> graph, Node<V> parent) {
    if (parent instanceof InnerNode) {
      InnerNode<V> innerVertex = (InnerNode<V>) parent;
      graph.addVertex(parent);
      for (Node<V> kid : innerVertex.getChildren()) {
        graph.addVertex(kid);
        graph.addEdge(parent, kid, graph.edgeSet().size() + 1);
        addChildren(graph, kid);
      }
    } else if (parent instanceof LeafNode) {
      LeafNode<V> leafVertex = (LeafNode<V>) parent;
      try {
        Method method = leafVertex.getClass().getDeclaredMethod("getKeys");
        method.setAccessible(true);
        Collection<V> got = (Collection<V>) method.invoke(leafVertex);
        for (V kid : got) {
          graph.addVertex(kid);
          graph.addEdge(parent, kid, graph.edgeSet().size() + 1);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  public static void showRTree(VisualizationViewer vv) {

    JFrame frame = new JFrame();
    Container content = frame.getContentPane();

    Spatial spatial = vv.getVertexSpatial();
    if (spatial instanceof SpatialRTree.Vertices) {
      try {
        SpatialRTree mySpatial = (SpatialRTree) spatial;
        Field rtreeField = mySpatial.getClass().getSuperclass().getDeclaredField("rtree");
        rtreeField.setAccessible(true);
        RTree rtree = (RTree) rtreeField.get(spatial);
        content.add(new RTreeVisualization<>(rtree, vv));
        // where is the calling visualization:
        Point2D callerLocation = vv.getComponent().getLocationOnScreen();
        int callerWidth = vv.getComponent().getWidth();
        frame.setLocation(
            (int) callerLocation.getX() + callerWidth + 50, (int) callerLocation.getY());
        frame.pack();
        frame.setVisible(true);

      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
