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
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.annotations.AnnotationControls;
import org.jungrapht.visualization.control.EditingModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.modal.ModeControls;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.spatial.Spatial;
import org.jungrapht.visualization.util.ParallelEdgeIndexFunction;

/**
 * Shows how to create a graph editor. Mouse modes and actions are explained in the help text.
 * GraphEditorDemo provides a File menu with an option to save the visible graph as a jpeg file.
 *
 * @author Tom Nelson
 */
public class GraphEditorDemo extends JPanel implements Printable {

  /** */
  private static final long serialVersionUID = -2023243689258876709L;

  /** the graph */
  Graph<Number, Number> graph;

  LayoutAlgorithm<Number> layoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Number, Number> vv;

  /** Used in the vertexLabelFunction. Values may be inserted by the EditingGraphMouse */
  Map<Number, String> vertexLabelMap = new HashMap<>();

  /** Used in the edgeLabelFunction Values may be inserted by the EditingGraphMouse */
  Map<Number, String> edgeLabelMap = new HashMap<>();

  String instructions =
      "<html>"
          + "<h3>All Modes:</h3>"
          + "<ul>"
          + "<li>Right-click an empty area for <b>Create vertex</b> popup"
          + "<li>Right-click on a vertex for <b>Delete vertex</b> popup"
          + "<li>Right-click on a vertex for <b>Add Edge</b> menus <br>(if there are selected Vertices)"
          + "<li>Right-click on an Edge for <b>Delete Edge</b> popup"
          + "<li>Mousewheel scales with a crossover value of 1.0.<p>"
          + "     - scales the graph layout when the combined scale is greater than 1<p>"
          + "     - scales the graph view when the combined scale is less than 1"
          + "</ul>"
          + "<h3>Editing Mode:</h3>"
          + "<ul>"
          + "<li>Left-click an empty area to create a new vertex"
          + "<li>Left-click on a vertex and drag to another vertex to create an Undirected Edge"
          + "<li>Shift+Left-click on a vertex and drag to another vertex to create a Directed Edge"
          + "</ul>"
          + "<h3>Picking Mode:</h3>"
          + "<ul>"
          + "<li>Mouse1 on a vertex selects the vertex"
          + "<li>Mouse1 elsewhere unselects all Vertices"
          + "<li>Mouse1+Shift on a vertex adds/removes vertex selection"
          + "<li>Mouse1+drag on a vertex moves all selected Vertices"
          + "<li>Mouse1+drag elsewhere selects Vertices in a region"
          + "<li>Mouse1+Shift+drag adds selection of Vertices in a new region"
          + "<li>Mouse1+CTRL on a vertex selects the vertex and centers the display on it"
          + "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"
          + "</ul>"
          + "<h3>Transforming Mode:</h3>"
          + "<ul>"
          + "<li>Mouse1+drag pans the graph"
          + "<li>Mouse1+Shift+drag rotates the graph"
          + "<li>Mouse1+CTRL(or Command)+drag shears the graph"
          + "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"
          + "</ul>"
          + "<h3>Annotation Mode:</h3>"
          + "<ul>"
          + "<li>Mouse1 begins drawing of a Rectangle"
          + "<li>Mouse1+drag defines the Rectangle shape"
          + "<li>Mouse1 release adds the Rectangle as an annotation"
          + "<li>Mouse1+Shift begins drawing of an Ellipse"
          + "<li>Mouse1+Shift+drag defines the Ellipse shape"
          + "<li>Mouse1+Shift release adds the Ellipse as an annotation"
          + "<li>Mouse3 shows a popup to input text, which will become"
          + "<li>a text annotation on the graph at the mouse location"
          + "</ul>"
          + "</html>";

  /** create an instance of a simple graph with popup controls to create a graph. */
  public GraphEditorDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph =
        GraphTypeBuilder.<Number, Number>forGraphType(DefaultGraphType.directedPseudograph())
            .buildGraph();

    this.layoutAlgorithm = new StaticLayoutAlgorithm<>();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .viewSize(new Dimension(600, 600))
            .build();

    vv.getRenderContext().setParallelEdgeIndexFunction(new ParallelEdgeIndexFunction<>());
    vv.getRenderContext()
        .setVertexLabelFunction(
            v -> vertexLabelMap.containsKey(v) ? vertexLabelMap.get(v) : v.toString());
    vv.getRenderContext()
        .setEdgeLabelFunction(
            e -> edgeLabelMap.containsKey(e) ? edgeLabelMap.get(e) : e.toString());

    vv.setVertexSpatial(new Spatial.NoOp.Vertex(vv.getVisualizationModel().getLayoutModel()));
    vv.setEdgeSpatial(new Spatial.NoOp.Edge(vv.getVisualizationModel()));

    vv.setVertexToolTipFunction(vv.getRenderContext().getVertexLabelFunction());

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);
    Supplier<Number> vertexFactory = new VertexFactory();
    Supplier<Number> edgeFactory = new EdgeFactory();

    final EditingModalGraphMouse<Number, Number> graphMouse =
        EditingModalGraphMouse.<Number, Number>builder()
            .renderContextSupplier(vv::getRenderContext)
            .multiLayerTransformerSupplier(vv.getRenderContext()::getMultiLayerTransformer)
            .vertexFactory(vertexFactory)
            .edgeFactory(edgeFactory)
            .vertexLabelMapSupplier(this::getVertexLabelMap)
            .edgeLabelMapSupplier(this::getEdgeLabelMap)
            .build();

    // the EditingGraphMouse will pass mouse event coordinates to the
    // vertexLocations function to set the locations of the vertices as
    // they are created
    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    graphMouse.setMode(ModalGraphMouse.Mode.EDITING);

    JButton help = new JButton("Help");
    help.addActionListener(e -> JOptionPane.showMessageDialog(vv.getComponent(), instructions));

    AnnotationControls<Number, Number> annotationControls =
        new AnnotationControls<>(graphMouse.getAnnotatingPlugin());
    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    JComboBox modeBox = ModeControls.getEditingModeComboBox(graphMouse);
    controls.add(ControlHelpers.getCenteredContainer("Mouse Mode", modeBox));
    controls.add(annotationControls.getAnnotationsToolBar());
    controls.add(help);
    add(controls, BorderLayout.SOUTH);
  }

  /**
   * copy the visible part of the graph to a file as a jpeg image
   *
   * @param file the file in which to save the graph image
   */
  public void writeJPEGImage(File file) {
    int width = vv.getWidth();
    int height = vv.getHeight();

    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = bi.createGraphics();
    vv.getComponent().paint(graphics);
    graphics.dispose();

    try {
      ImageIO.write(bi, "jpeg", file);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public int print(
      java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) {
    if (pageIndex > 0) {
      return (Printable.NO_SUCH_PAGE);
    } else {
      java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
      vv.setDoubleBuffered(false);
      g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

      vv.getComponent().paint(g2d);
      vv.setDoubleBuffered(true);

      return (Printable.PAGE_EXISTS);
    }
  }

  private String apply(Number number) {
    if (vertexLabelMap.containsKey(number)) return vertexLabelMap.get(number);
    else return number.toString();
  }

  static class VertexFactory implements Supplier<Number> {

    int i = 0;

    public Number get() {
      return i++;
    }
  }

  static class EdgeFactory implements Supplier<Number> {

    int i = 0;

    public Number get() {
      return i++;
    }
  }

  public Map<Number, String> getVertexLabelMap() {
    return vertexLabelMap;
  }

  public Map<Number, String> getEdgeLabelMap() {
    return edgeLabelMap;
  }

  @SuppressWarnings("serial")
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    final GraphEditorDemo demo = new GraphEditorDemo();

    JMenu menu = new JMenu("File");
    menu.add(
        new AbstractAction("Make Image") {
          public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            int option = chooser.showSaveDialog(demo);
            if (option == JFileChooser.APPROVE_OPTION) {
              File file = chooser.getSelectedFile();
              demo.writeJPEGImage(file);
            }
          }
        });
    menu.add(
        new AbstractAction("Print") {
          public void actionPerformed(ActionEvent e) {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setPrintable(demo);
            if (printJob.printDialog()) {
              try {
                printJob.print();
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
          }
        });
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(menu);
    frame.setJMenuBar(menuBar);
    frame.getContentPane().add(demo);
    frame.pack();
    frame.setVisible(true);
  }
}
