/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A variant of TreeLayoutDemo that rotates the view by 90 degrees from the default orientation.
 *
 * @author Tom Nelson
 */
public class L2RTreeLayoutDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(L2RTreeLayoutDemo.class);
  /** the graph */
  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  VisualizationServer.Paintable rings;

  TreeLayoutAlgorithm<String> treeLayoutAlgorithm;

  RadialTreeLayoutAlgorithm<String> radialLayoutAlgorithm;

  public L2RTreeLayoutDemo() {

    setLayout(new BorderLayout());

    // create a simple graph for the demo
    graph = DemoTreeSupplier.createTreeOne();

    treeLayoutAlgorithm = new TreeLayoutAlgorithm();

    radialLayoutAlgorithm = new RadialTreeLayoutAlgorithm<>();
    final DefaultGraphMouse<String, Integer> graphMouse = new DefaultGraphMouse<>();

    vv =
        VisualizationViewer.builder(graph)
            .viewSize(new Dimension(600, 600))
            .graphMouse(graphMouse)
            .build();

    treeLayoutAlgorithm.setAfter(new LtoR(vv.getVisualizationModel().getLayoutModel()));

    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(a -> Color.lightGray);

    //    vv.getVisualizationModel()
    //        .getLayoutModel()
    //        .getLayoutStateChangeSupport()
    //        .addLayoutStateChangeListener(
    //            evt -> {
    //              if (!evt.active) {
    //                LayoutModel<String> layoutModel = evt.layoutModel;
    //                layoutModel
    //                    .getLocations()
    //                    .forEach(
    //                        (v, p) -> layoutModel.set(v, Point.of(
    //                                layoutModel.getHeight() - p.y,  p.x)));
    //              }
    //            });

    vv.getVisualizationModel().setLayoutAlgorithm(treeLayoutAlgorithm);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    JToggleButton radial = new JToggleButton("Radial");
    radial.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
            vv.getVisualizationModel().setLayoutAlgorithm(radialLayoutAlgorithm);
            if (rings == null) {
              rings = new Rings(vv.getVisualizationModel().getLayoutModel());
            }
            vv.addPreRenderPaintable(rings);
          } else {
            vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
            vv.getVisualizationModel().setLayoutAlgorithm(treeLayoutAlgorithm);
            vv.removePreRenderPaintable(rings);
          }
          vv.repaint();
        });

    JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
    scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));

    Box controls = Box.createHorizontalBox();
    controls.add(ControlHelpers.getCenteredContainer("Layout Control", radial));
    controls.add(ControlHelpers.getCenteredContainer("Scale", ControlHelpers.getZoomControls(vv)));
    add(controls, BorderLayout.SOUTH);
  }

  class LtoR<V> implements Runnable {
    LayoutModel<V> layoutModel;

    LtoR(LayoutModel layoutModel) {
      this.layoutModel = layoutModel;
    }

    @Override
    public void run() {
      layoutModel
          .getLocations()
          .forEach((v, p) -> layoutModel.set(v, Point.of(layoutModel.getHeight() - p.y, p.x)));
    }
  }

  class Rings implements VisualizationServer.Paintable {

    Collection<Double> depths;
    LayoutModel<String> layoutModel;

    public Rings(LayoutModel<String> layoutModel) {
      this.layoutModel = layoutModel;
      depths = getDepths();
    }

    private Collection<Double> getDepths() {
      Set<Double> depths = new HashSet<>();
      Map<String, PolarPoint> polarLocations = radialLayoutAlgorithm.getPolarLocations();
      for (String v : graph.vertexSet()) {
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

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new L2RTreeLayoutDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
