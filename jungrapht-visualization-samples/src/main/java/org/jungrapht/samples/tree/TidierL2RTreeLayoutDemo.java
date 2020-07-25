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
import java.awt.geom.Point2D;
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
import org.jungrapht.visualization.layout.algorithms.TidierRadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TidierTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.jungrapht.visualization.util.LayoutAlgorithmTransition;

/**
 * A variant of TidierTreeLayoutDemo that rotates the view by 90 degrees from the default
 * orientation.
 *
 * @author Tom Nelson
 */
public class TidierL2RTreeLayoutDemo extends JPanel {

  Graph<String, Integer> graph;

  VisualizationViewer<String, Integer> vv;

  VisualizationServer.Paintable rings;

  TidierTreeLayoutAlgorithm<String, Integer> treeLayoutAlgorithm;

  TidierRadialTreeLayoutAlgorithm<String, Integer> radialLayoutAlgorithm;

  public TidierL2RTreeLayoutDemo() {

    setLayout(new BorderLayout());

    graph = DemoTreeSupplier.createTreeOne();

    treeLayoutAlgorithm =
        TidierTreeLayoutAlgorithm.<String, Integer>edgeAwareBuilder().expandLayout(true).build();
    radialLayoutAlgorithm =
        TidierRadialTreeLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .expandLayout(true)
            .build();

    final DefaultGraphMouse<String, Integer> graphMouse = new DefaultGraphMouse<>();

    vv =
        VisualizationViewer.builder(graph)
            .viewSize(new Dimension(700, 700))
            .layoutSize(new Dimension(600, 600))
            .graphMouse(graphMouse)
            .build();
    treeLayoutAlgorithm.setVertexBoundsFunction(vv.getRenderContext().getVertexBoundsFunction());
    radialLayoutAlgorithm.setVertexBoundsFunction(vv.getRenderContext().getVertexBoundsFunction());
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(a -> Color.lightGray);
    vv.getVisualizationModel()
        .setLayoutAlgorithm(treeLayoutAlgorithm); // after the vertexShapeFunction is set
    setLtoR(vv);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    JToggleButton radial = new JToggleButton("Radial");
    radial.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            LayoutAlgorithmTransition.animate(vv, radialLayoutAlgorithm);
            if (rings == null) {
              rings = new Rings(vv.getVisualizationModel().getLayoutModel());
            }
            vv.addPreRenderPaintable(rings);
          } else {
            LayoutAlgorithmTransition.animate(vv, treeLayoutAlgorithm);
            vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
            setLtoR(vv);
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

  private void setLtoR(VisualizationViewer<String, Integer> vv) {
    Point2D viewCenter = vv.getCenter();
    Point2D viewCenterInLayout =
        vv.getRenderContext().getMultiLayerTransformer().inverseTransform(viewCenter);
    Point layoutCenter =
        Point.centroidOf(vv.getVisualizationModel().getLayoutModel().getLocations().values());

    // move the layout center to the viewCenterInLayout
    double deltaX = viewCenterInLayout.getX() - layoutCenter.x;
    double deltaY = viewCenterInLayout.getY() - layoutCenter.y;

    vv.getRenderContext()
        .getMultiLayerTransformer()
        .getTransformer(Layer.LAYOUT)
        .translate(deltaX, deltaY);

    vv.getRenderContext()
        .getMultiLayerTransformer()
        .getTransformer(Layer.LAYOUT)
        .rotate(-Math.PI / 2, viewCenterInLayout);
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

    content.add(new TidierL2RTreeLayoutDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
