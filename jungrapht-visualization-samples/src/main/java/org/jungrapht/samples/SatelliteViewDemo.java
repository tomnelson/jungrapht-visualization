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
import java.awt.event.ItemEvent;
import java.awt.geom.GeneralPath;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.SatelliteVisualizationViewer;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.renderers.GradientVertexRenderer;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.transform.shape.ShapeTransformer;
import org.jungrapht.visualization.util.LightweightRenderingVisitor;

/**
 * Demonstrates the construction of a graph visualization with a main and a satellite view. The
 * satellite view is smaller, always contains the entire graph, and contains a lens shape that shows
 * the boundaries of the visible part of the graph in the main view. Using the mouse, you can pick,
 * translate, layout-scale, view-scale, rotate, shear, and region-select in either view. Using the
 * mouse in either window affects only the main view and the lens shape in the satellite view.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class SatelliteViewDemo extends JPanel {

  static final String instructions =
      "<html>"
          + "<b><h2><center>Instructions for Mouse Listeners</center></h2></b>"
          + "<p>There are two modes, Transforming and Picking."
          + "<p>The modes are selected with a combo box."
          + "<p><p><b>Transforming Mode:</b>"
          + "<ul>"
          + "<li>Mouse1+drag pans the graph"
          + "<li>Mouse1+Shift+drag rotates the graph"
          + "<li>Mouse1+CTRL(or Command)+drag shears the graph"
          + "</ul>"
          + "<b>Picking Mode:</b>"
          + "<ul>"
          + "<li>Mouse1 on a Vertex selects the vertex"
          + "<li>Mouse1 elsewhere unselects all Vertices"
          + "<li>Mouse1+Shift on a Vertex adds/removes Vertex selection"
          + "<li>Mouse1+drag on a Vertex moves all selected Vertices"
          + "<li>Mouse1+drag elsewhere selects Vertices in a region"
          + "<li>Mouse1+Shift+drag adds selection of Vertices in a new region"
          + "<li>Mouse1+CTRL on a Vertex selects the vertex and centers the display on it"
          + "</ul>"
          + "<b>Both Modes:</b>"
          + "<ul>"
          + "<li>Mousewheel scales with a crossover value of 1.0.<p>"
          + "     - scales the graph layout when the combined scale is greater than 1<p>"
          + "     - scales the graph view when the combined scale is less than 1";

  JDialog helpDialog;

  VisualizationServer.Paintable viewGrid;

  /** create an instance of a simple graph in two views with controls to demo the features. */
  public SatelliteViewDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    Graph<String, Number> graph = TestGraphs.getOneComponentGraph();

    // the preferred sizes for the two views
    Dimension preferredSize1 = new Dimension(1000, 1000);
    Dimension preferredSize2 = new Dimension(250, 250);
    Dimension layoutSize = new Dimension(1000, 1000);

    // create one layout for the graph
    FRLayoutAlgorithm<String> layoutAlgorithm = FRLayoutAlgorithm.<String>builder().build();
    // not used, for testing only
    //    CircleLayoutAlgorithm<String, Point2D> clayout = new CircleLayoutAlgorithm<>();
    layoutAlgorithm.setMaxIterations(500);

    // create one model that both views will share
    VisualizationModel<String, Number> vm =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .layoutSize(layoutSize)
            .build();

    // create 2 views that share the same model
    final VisualizationViewer<String, Number> mainVisualizationViewer =
        VisualizationViewer.builder(vm).viewSize(preferredSize1).build();
    final SatelliteVisualizationViewer<String, Number> satelliteVisualizationViewer =
        SatelliteVisualizationViewer.builder(mainVisualizationViewer)
            .viewSize(preferredSize2)
            .build();
    LightweightRenderingVisitor.visit(mainVisualizationViewer);
    LightweightRenderingVisitor.visit(satelliteVisualizationViewer);
    mainVisualizationViewer.setBackground(Color.white);
    mainVisualizationViewer
        .getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(
                mainVisualizationViewer.getSelectedEdgeState(), Color.black, Color.cyan));
    mainVisualizationViewer
        .getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(
                mainVisualizationViewer.getSelectedVertexState(), Color.red, Color.yellow));
    satelliteVisualizationViewer
        .getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(
                satelliteVisualizationViewer.getSelectedEdgeState(), Color.black, Color.cyan));
    satelliteVisualizationViewer
        .getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(
                satelliteVisualizationViewer.getSelectedVertexState(), Color.red, Color.yellow));
    mainVisualizationViewer
        .getRenderer()
        .setVertexRenderer(new GradientVertexRenderer<>(Color.red, Color.white, true));
    mainVisualizationViewer.getRenderContext().setVertexLabelFunction(Object::toString);
    mainVisualizationViewer
        .getRenderContext()
        .setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);

    mainVisualizationViewer.scaleToLayout(new CrossoverScalingControl());
    satelliteVisualizationViewer.scaleToLayout(new CrossoverScalingControl());

    viewGrid = new ViewGrid(satelliteVisualizationViewer, mainVisualizationViewer);

    // add default listener for ToolTips
    mainVisualizationViewer.setVertexToolTipFunction(Object::toString);
    satelliteVisualizationViewer.setVertexToolTipFunction(Object::toString);

    satelliteVisualizationViewer
        .getRenderContext()
        .setVertexLabelFunction(
            mainVisualizationViewer.getRenderContext().getVertexLabelFunction());

    ToolTipManager.sharedInstance().setDismissDelay(10000);

    Container panel = new JPanel(new BorderLayout());
    Container rightPanel = new JPanel(new BorderLayout());

    VisualizationScrollPane gzsp = new VisualizationScrollPane(mainVisualizationViewer);
    panel.add(gzsp);
    rightPanel.add(new JPanel());
    rightPanel.add(satelliteVisualizationViewer.getComponent(), BorderLayout.SOUTH);
    panel.add(rightPanel, BorderLayout.EAST);

    helpDialog = new JDialog();
    helpDialog.getContentPane().add(new JLabel(instructions));

    // create a GraphMouse for the main view
    final DefaultModalGraphMouse<String, Number> graphMouse = new DefaultModalGraphMouse<>();
    mainVisualizationViewer.setGraphMouse(graphMouse);

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(
        ((DefaultModalGraphMouse<?, ?>) satelliteVisualizationViewer.getGraphMouse())
            .getModeListener());

    JCheckBox gridBox = new JCheckBox("Show Grid");
    gridBox.addItemListener(
        e -> showGrid(satelliteVisualizationViewer, e.getStateChange() == ItemEvent.SELECTED));

    JButton help = new JButton("Help");
    help.addActionListener(
        e -> {
          helpDialog.pack();
          helpDialog.setVisible(true);
        });

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls(mainVisualizationViewer, ""));
    //    controls.add(minus);
    controls.add(modeBox);
    controls.add(gridBox);
    controls.add(help);
    add(panel);
    add(controls, BorderLayout.SOUTH);
  }

  protected void showGrid(VisualizationViewer<?, ?> vv, boolean state) {
    if (state) {
      vv.addPreRenderPaintable(viewGrid);
    } else {
      vv.removePreRenderPaintable(viewGrid);
    }
    vv.repaint();
  }

  /**
   * draws a grid on the SatelliteViewer's lens
   *
   * @author Tom Nelson
   */
  static class ViewGrid implements VisualizationServer.Paintable {

    VisualizationViewer<?, ?> master;
    VisualizationViewer<?, ?> vv;

    public ViewGrid(VisualizationViewer<?, ?> vv, VisualizationViewer<?, ?> master) {
      this.vv = vv;
      this.master = master;
    }

    public void paint(Graphics g) {
      ShapeTransformer masterViewTransformer =
          master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
      ShapeTransformer masterLayoutTransformer =
          master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
      ShapeTransformer vvLayoutTransformer =
          vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

      Rectangle rect = master.getBounds();
      GeneralPath path = new GeneralPath();
      path.moveTo(rect.x, rect.y);
      path.lineTo(rect.width, rect.y);
      path.lineTo(rect.width, rect.height);
      path.lineTo(rect.x, rect.height);
      path.lineTo(rect.x, rect.y);

      for (int i = 0; i <= rect.width; i += rect.width / 10) {
        path.moveTo(rect.x + i, rect.y);
        path.lineTo(rect.x + i, rect.height);
      }
      for (int i = 0; i <= rect.height; i += rect.height / 10) {
        path.moveTo(rect.x, rect.y + i);
        path.lineTo(rect.width, rect.y + i);
      }
      Shape lens = path;
      lens = masterViewTransformer.inverseTransform(lens);
      lens = masterLayoutTransformer.inverseTransform(lens);
      lens = vvLayoutTransformer.transform(lens);
      Graphics2D g2d = (Graphics2D) g;
      Color old = g.getColor();
      g.setColor(Color.cyan);
      g2d.draw(lens);

      path = new GeneralPath();
      path.moveTo((float) rect.getMinX(), (float) rect.getCenterY());
      path.lineTo((float) rect.getMaxX(), (float) rect.getCenterY());
      path.moveTo((float) rect.getCenterX(), (float) rect.getMinY());
      path.lineTo((float) rect.getCenterX(), (float) rect.getMaxY());
      Shape crosshairShape = path;
      crosshairShape = masterViewTransformer.inverseTransform(crosshairShape);
      crosshairShape = masterLayoutTransformer.inverseTransform(crosshairShape);
      crosshairShape = vvLayoutTransformer.transform(crosshairShape);
      g.setColor(Color.black);
      g2d.setStroke(new BasicStroke(3));
      g2d.draw(crosshairShape);

      g.setColor(old);
    }

    public boolean useTransform() {
      return true;
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new SatelliteViewDemo());
    f.pack();
    f.setVisible(true);
  }
}
