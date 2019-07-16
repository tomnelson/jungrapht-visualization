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
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.visualization.GraphZoomScrollPane;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse.Mode;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonsrates TreeLayout and RadialTreeLayout.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class TreeDAGLayoutDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(TreeDAGLayoutDemo.class);
  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  VisualizationServer.Paintable rings;

  String root;

  TreeLayoutAlgorithm<String> treeLayoutAlgorithm;

  BalloonLayoutAlgorithm<String> radialLayoutAlgorithm;

  public TreeDAGLayoutDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = createDAG();

    treeLayoutAlgorithm = TreeLayoutAlgorithm.builder().build();
    radialLayoutAlgorithm = BalloonLayoutAlgorithm.builder().build();
    //    radialLayout.setSize(new Dimension(600, 600));
    vv = new VisualizationViewer<>(graph, treeLayoutAlgorithm, new Dimension(600, 600));
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    // add a listener for ToolTips
    vv.setNodeToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);

    vv.getRenderContext().setNodeLabelPosition(Renderer.NodeLabel.Position.CNTR);
    vv.getRenderContext().setNodeLabelDrawPaintFunction(c -> Color.white);

    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    add(panel);

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(Mode.TRANSFORMING);

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
              rings = new Rings(radialLayoutAlgorithm);
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
        shape = vv.getTransformSupport().transform(vv, shape, Layer.LAYOUT);
        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return true;
    }
  }
  //  class RadialRings implements VisualizationServer.Paintable {
  //
  //    Collection<Double> depths;
  //    LayoutModel<String> layoutModel;
  //
  //    public RadialRings(LayoutModel<String> layoutModel) {
  //      this.layoutModel = layoutModel;
  //      depths = getDepths();
  //    }
  //
  //    private Collection<Double> getDepths() {
  //      Set<Double> depths = new HashSet<>();
  //      Map<String, PolarPoint> polarLocations = radialLayoutAlgorithm.getPolarLocations();
  //      for (String v : graph.vertexSet()) {
  //        PolarPoint pp = polarLocations.get(v);
  //        depths.add(pp.radius);
  //      }
  //      return depths;
  //    }
  //
  //    public void paint(Graphics g) {
  //      g.setColor(Color.lightGray);
  //
  //      Graphics2D g2d = (Graphics2D) g;
  //      Point center = radialLayoutAlgorithm.getCenter(layoutModel);
  //
  //      Ellipse2D ellipse = new Ellipse2D.Double();
  //      for (double d : depths) {
  //        ellipse.setFrameFromDiagonal(center.x - d, center.y - d, center.x + d, center.y + d);
  //        Shape shape =
  //            vv.getRenderContext()
  //                .getMultiLayerTransformer()
  //                .getTransformer(Layer.LAYOUT)
  //                .transform(ellipse);
  //        g2d.draw(shape);
  //      }
  //    }
  //
  //    public boolean useTransform() {
  //      return true;
  //    }
  //  }

  private Graph<String, Integer> createDAG() {
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag()).buildGraph();
    Integer i = 0;
    // roots
    graph.addVertex("R1");
    graph.addVertex("R2");
    graph.addVertex("R3");
    graph.addVertex("R4");

    graph.addVertex("A1");
    graph.addVertex("A2");
    graph.addVertex("A3");
    graph.addVertex("A4");
    graph.addVertex("A5");
    graph.addVertex("A6");

    graph.addEdge("R1", "A1", i++);
    graph.addEdge("R1", "A2", i++);
    graph.addEdge("A1", "A3", i++);
    graph.addEdge("A1", "A4", i++);

    graph.addEdge("A4", "A3", i++);
    graph.addEdge("A3", "A4", i++);

    graph.addEdge("R2", "A5", i++);
    graph.addEdge("R3", "A5", i++);
    graph.addEdge("A5", "A6", i++);
    //    graph.addEdge("R1","A1", i++);
    return graph;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new TreeDAGLayoutDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
