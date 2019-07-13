/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.*;
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
import org.jungrapht.visualization.GraphZoomScrollPane;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse.Mode;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonsrates TreeLayout and RadialTreeLayout.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class TreeLayoutDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(TreeLayoutDemo.class);
  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  VisualizationServer.Paintable rings;

  String root;

  TreeLayoutAlgorithm<String> treeLayoutAlgorithm;

  RadialTreeLayoutAlgorithm<String> radialLayoutAlgorithm;

  public TreeLayoutDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.createTreeTwo();

    treeLayoutAlgorithm = TreeLayoutAlgorithm.builder().build();
    radialLayoutAlgorithm = RadialTreeLayoutAlgorithm.builder().build();
    //    radialLayout.setSize(new Dimension(600, 600));
    vv = new VisualizationViewer<>(graph, treeLayoutAlgorithm, new Dimension(600, 600));
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    // add a listener for ToolTips
    vv.setNodeToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);

    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    add(panel);

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

    JRadioButton animate = new JRadioButton("Animate Transition");
    JToggleButton radial = new JToggleButton("Radial");
    radial.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {

            if (animate.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, radialLayoutAlgorithm);
            } else {
              LayoutAlgorithmTransition.apply(vv, radialLayoutAlgorithm);
            }
            vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
            if (rings == null) {
              rings = new Rings(vv.getModel().getLayoutModel());
            }
            vv.addPreRenderPaintable(rings);
          } else {
            if (animate.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, treeLayoutAlgorithm);
            } else {
              LayoutAlgorithmTransition.apply(vv, treeLayoutAlgorithm);
            }
            vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
            vv.removePreRenderPaintable(rings);
          }
          vv.repaint();
        });

    JPanel layoutPanel = new JPanel(new GridLayout(2, 1));
    layoutPanel.add(radial);
    layoutPanel.add(animate);
    JPanel controls = new JPanel();
    controls.add(layoutPanel);
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));
    controls.add(modeBox);

    add(controls, BorderLayout.SOUTH);
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

    content.add(new TreeLayoutDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
