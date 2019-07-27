/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.samples.util.TreeLayoutSelector;
import org.jungrapht.visualization.GraphZoomScrollPane;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse.Mode;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
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

  public TreeLayoutDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.createTreeTwo();

    vv =
        new VisualizationViewer<>(
            graph, TreeLayoutAlgorithm.<String>builder().build(), new Dimension(600, 600));
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    vv.getRenderContext().setNodeLabelPosition(Renderer.NodeLabel.Position.CNTR);
    vv.getRenderContext().setNodeLabelDrawPaintFunction(n -> Color.white);
    // add a listener for ToolTips
    vv.setNodeToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);
    if (log.isTraceEnabled()) {
      vv.addPreRenderPaintable(
          new VisualizationServer.Paintable() {
            @Override
            public void paint(Graphics g) {
              Graphics2D g2d = (Graphics2D) g;
              for (int i = 0; i < 20; i++) {
                Rectangle2D r = new Rectangle2D.Double(i * 100, 0, 100, 500);
                g2d.setPaint(Color.cyan);
                g2d.draw(r);
              }
            }

            @Override
            public boolean useTransform() {
              return false;
            }
          });
    }

    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    add(panel);

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);
    // temporary feature to draw the layout bounds in the viewer
    vv.addPreRenderPaintable(
        new VisualizationServer.Paintable() {

          @Override
          public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            // get the layout dimensions:
            Dimension layoutSize = vv.getModel().getLayoutSize();
            g.setColor(Color.cyan);
            Shape layoutRectangle =
                new Rectangle2D.Double(0, 0, layoutSize.width, layoutSize.height);
            layoutRectangle =
                vv.getRenderContext().getMultiLayerTransformer().transform(layoutRectangle);
            g2d.draw(layoutRectangle);
          }

          @Override
          public boolean useTransform() {
            return false;
          }
        });

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

    JPanel layoutPanel = new JPanel(new GridLayout(0, 1));
    layoutPanel.add(new TreeLayoutSelector<>(vv));
    JPanel controls = new JPanel();
    controls.add(layoutPanel);
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));
    controls.add(modeBox);

    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    JPanel demo = new TreeLayoutDemo();
    content.add(demo);
    frame.pack();
    frame.setVisible(true);
    log.trace("frame width {}", frame.getWidth());
    log.trace("demo width {}", demo.getWidth());
  }
}
