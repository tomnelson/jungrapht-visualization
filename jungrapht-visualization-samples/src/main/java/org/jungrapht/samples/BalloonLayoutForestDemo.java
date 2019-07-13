/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.visualization.GraphZoomScrollPane;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ModalLensGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.HyperbolicTransformer;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.ViewLensSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates the visualization of a Forest using TreeLayout and BalloonLayout. An examiner lens
 * performing a hyperbolic transformation of the view is also included.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class BalloonLayoutForestDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(BalloonLayoutForestDemo.class);

  Graph<String, Integer> graph;

  VisualizationServer.Paintable rings;

  TreeLayoutAlgorithm<String> treeLayoutAlgorithm;

  BalloonLayoutAlgorithm<String> radialLayoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  /** provides a Hyperbolic lens for the view */
  LensSupport hyperbolicViewSupport;

  LensSupport hyperbolicSupport;

  public BalloonLayoutForestDemo() {
    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = DemoTreeSupplier.createForest();

    treeLayoutAlgorithm = TreeLayoutAlgorithm.builder().build();
    radialLayoutAlgorithm = BalloonLayoutAlgorithm.builder().build();

    vv =
        new VisualizationViewer<>(
            graph, treeLayoutAlgorithm, new Dimension(900, 900), new Dimension(600, 600));
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    // add a listener for ToolTips
    vv.setNodeToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(a -> Color.lightGray);
    rings = new Rings(radialLayoutAlgorithm);

    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    add(panel);

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());
    LayoutModel layoutModel = vv.getModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
    Lens lens = new Lens(d);
    hyperbolicViewSupport =
        new ViewLensSupport<>(
            vv,
            new HyperbolicShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)),
            new ModalLensGraphMouse());
    hyperbolicSupport =
        new LayoutLensSupport<>(
            vv,
            new HyperbolicTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)),
            new ModalLensGraphMouse());

    graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(hyperbolicSupport.getGraphMouse().getModeListener());

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

    final ScalingControl scaler = new CrossoverScalingControl();

    vv.scaleToLayout(scaler);

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JToggleButton radial = new JToggleButton("Balloon");
    final JRadioButton animateTransition = new JRadioButton("Animate Transition");

    radial.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            ((JToggleButton) e.getSource()).setText("Tree");
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, radialLayoutAlgorithm);
            } else {
              LayoutAlgorithmTransition.apply(vv, radialLayoutAlgorithm);
            }

            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT)
                .setToIdentity();
            vv.addPreRenderPaintable(rings);

          } else {
            ((JToggleButton) e.getSource()).setText("Balloon");
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, treeLayoutAlgorithm);
            } else {
              LayoutAlgorithmTransition.apply(vv, treeLayoutAlgorithm);
            }

            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT)
                .setToIdentity();
            vv.removePreRenderPaintable(rings);
          }
          vv.repaint();
        });

    final JRadioButton hyperView = new JRadioButton("Hyperbolic View");
    hyperView.addItemListener(
        e -> hyperbolicViewSupport.activate(e.getStateChange() == ItemEvent.SELECTED));
    final JRadioButton hyperLayout = new JRadioButton("Hyperbolic Layout");
    hyperLayout.addItemListener(
        e -> hyperbolicSupport.activate(e.getStateChange() == ItemEvent.SELECTED));
    final JRadioButton noLens = new JRadioButton("No Lens");
    noLens.setSelected(true);

    ButtonGroup radio = new ButtonGroup();
    radio.add(hyperView);
    radio.add(hyperLayout);
    radio.add(noLens);

    JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
    scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
    JPanel viewControls = new JPanel();
    viewControls.setLayout(new GridLayout(0, 1));

    JPanel controls = new JPanel();
    scaleGrid.add(plus);
    scaleGrid.add(minus);
    JPanel layoutControls = new JPanel();
    layoutControls.add(radial);
    layoutControls.add(animateTransition);
    controls.add(layoutControls);
    controls.add(scaleGrid);
    controls.add(modeBox);
    viewControls.add(hyperView);
    viewControls.add(hyperLayout);
    viewControls.add(noLens);
    controls.add(viewControls);
    add(controls, BorderLayout.SOUTH);
  }

  class Rings implements VisualizationServer.Paintable {

    BalloonLayoutAlgorithm<String> layoutAlgorithm;

    public Rings(BalloonLayoutAlgorithm<String> layoutAlgorithm) {
      this.layoutAlgorithm = layoutAlgorithm;
    }

    public void paint(Graphics g) {
      g.setColor(Color.gray);

      Graphics2D g2d = (Graphics2D) g;

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (String v : vv.getModel().getNetwork().vertexSet()) {
        Double radius = layoutAlgorithm.getRadii().get(v);
        if (radius == null) {
          continue;
        }
        Point p = vv.getModel().getLayoutModel().apply(v);
        ellipse.setFrame(-radius, -radius, 2 * radius, 2 * radius);
        AffineTransform at = AffineTransform.getTranslateInstance(p.x, p.y);
        Shape shape = at.createTransformedShape(ellipse);
        shape = vv.getTransformSupport().transform(vv, shape);
        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return false;
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new BalloonLayoutForestDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
