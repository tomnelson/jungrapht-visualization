package org.jungrapht.samples;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.IconPalette;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.annotations.AnnotationControls;
import org.jungrapht.visualization.control.EditingModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.SelectionIconListener;
import org.jungrapht.visualization.control.dnd.VertexImageDropTargetListener;
import org.jungrapht.visualization.control.modal.ModeControls;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.decorators.IconShapeFunction;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.renderers.JLabelEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.JLabelVertexLabelRenderer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.spatial.Spatial;
import org.jungrapht.visualization.util.ParallelEdgeIndexFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This one uses the Modal graph mouse with picking and transforming modes, also with rotate and
 * shear
 *
 * @author Tom Nelson
 */
public class GraphEditorDemoWithPalette extends JPanel implements Printable {

  /** */
  private static final Logger log = LoggerFactory.getLogger(GraphEditorDemoWithPalette.class);
  /** the graph */
  Graph<Integer, Integer> graph;

  LayoutAlgorithm<Integer> layoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Integer, Integer> vv;

  /** Used in the vertexLabelFunction. Values may be inserted by the EditingGraphMouse */
  Map<Integer, String> vertexLabelMap = new HashMap<>();

  /** Used in the edgeLabelFunction Values may be inserted by the EditingGraphMouse */
  Map<Integer, String> edgeLabelMap = new HashMap<>();

  // Maps for the labels and icons
  Map<Integer, String> map = new HashMap<>();
  Map<Integer, Icon> iconMap = new HashMap<>();

  //  Supplier<Number> vertexFactory = new VertexFactory();
  //  Supplier<Number> edgeFactory = new EdgeFactory();

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
  public GraphEditorDemoWithPalette() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph =
        GraphTypeBuilder.<Integer, Integer>forGraphType(DefaultGraphType.directedPseudograph())
            .vertexSupplier(SupplierUtil.createIntegerSupplier())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    this.layoutAlgorithm = new StaticLayoutAlgorithm<>();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .viewSize(new Dimension(600, 600))
            .build();

    vv.getRenderContext().setParallelEdgeIndexFunction(new ParallelEdgeIndexFunction<>());

    Function<Integer, Paint> vpf =
        new PickableElementPaintFunction<>(vv.getSelectedVertexState(), Color.white, Color.yellow);
    vv.getRenderContext().setVertexFillPaintFunction(vpf);
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv.getSelectedEdgeState(), Color.black, Color.cyan));

    vv.getRenderContext().setVertexLabelFunction(map::get);
    vv.getRenderContext().setVertexLabelRenderer(new JLabelVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new JLabelEdgeLabelRenderer(Color.cyan));

    final IconShapeFunction<Integer> vertexImageShapeFunction =
        new IconShapeFunction<>(new EllipseShapeFunction<>());

    final Function<Integer, Icon> vertexIconFunction = iconMap::get;

    vertexImageShapeFunction.setIconFunction(iconMap::get);

    vv.getRenderContext().setVertexShapeFunction(vertexImageShapeFunction);
    vv.getRenderContext().setVertexIconFunction(vertexIconFunction);

    TransferHandler dnd =
        new TransferHandler() {
          @Override
          public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) {
              return false;
            }
            //only Strings
            if (!support.isDataFlavorSupported(DataFlavor.imageFlavor)) {
              return false;
            }
            return true;
          }

          @Override
          public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
              return false;
            }

            Transferable tansferable = support.getTransferable();
            Icon ico;
            try {
              ico = (Icon) tansferable.getTransferData(DataFlavor.imageFlavor);
            } catch (Exception e) {
              e.printStackTrace();
              return false;
            }
            vv.getComponent().add(new JLabel(ico));
            return true;
          }
        };

    vv.getComponent().setTransferHandler(dnd);

    // Get the pickedState and add a listener that will decorate the
    //Vertex images with a checkmark icon when they are selected
    MutableSelectedState<Integer> ps = vv.getSelectedVertexState();
    ps.addItemListener(new SelectionIconListener<>(vertexIconFunction));

    vv.getRenderContext()
        .setEdgeLabelFunction(
            e -> edgeLabelMap.containsKey(e) ? edgeLabelMap.get(e) : e.toString());

    vv.setVertexSpatial(new Spatial.NoOp.Vertex(vv.getVisualizationModel().getLayoutModel()));
    vv.setEdgeSpatial(new Spatial.NoOp.Edge(vv.getVisualizationModel()));

    vv.setVertexToolTipFunction(vv.getRenderContext().getVertexLabelFunction());

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    final EditingModalGraphMouse<Integer, Integer> graphMouse =
        EditingModalGraphMouse.<Integer, Integer>builder()
            .renderContextSupplier(vv::getRenderContext)
            .multiLayerTransformerSupplier(vv.getRenderContext()::getMultiLayerTransformer)
            .edgeFactory(graph.getEdgeSupplier())
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

    AnnotationControls<Integer, Integer> annotationControls =
        new AnnotationControls<>(graphMouse.getAnnotatingPlugin());
    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    JComboBox modeBox = ModeControls.getEditingModeComboBox(graphMouse);
    controls.add(ControlHelpers.getCenteredContainer("Mouse Mode", modeBox));
    controls.add(annotationControls.getAnnotationsToolBar());
    controls.add(help);
    add(controls, BorderLayout.SOUTH);
    add(
        ControlHelpers.getCenteredContainer("Drag icons to create vertices", new IconPalette()),
        BorderLayout.EAST);

    new VertexImageDropTargetListener<>(vv, iconMap::put);
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

  public int print(Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) {
    if (pageIndex > 0) {
      return (Printable.NO_SUCH_PAGE);
    } else {
      Graphics2D g2d = (Graphics2D) graphics;
      vv.setDoubleBuffered(false);
      g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

      vv.getComponent().paint(g2d);
      vv.setDoubleBuffered(true);

      return (Printable.PAGE_EXISTS);
    }
  }

  private String apply(Integer number) {
    if (vertexLabelMap.containsKey(number)) return vertexLabelMap.get(number);
    else return number.toString();
  }

  public Map<Integer, String> getVertexLabelMap() {
    return vertexLabelMap;
  }

  public Map<Integer, String> getEdgeLabelMap() {
    return edgeLabelMap;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    final GraphEditorDemoWithPalette demo = new GraphEditorDemoWithPalette();

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
