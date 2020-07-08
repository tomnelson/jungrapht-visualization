package org.jungrapht.samples.large;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.GraphImporter;
import org.jgrapht.nio.csv.CSVImporter;
import org.jgrapht.nio.dot.DOTImporter;
import org.jgrapht.nio.gml.GmlImporter;
import org.jgrapht.nio.graphml.GraphMLImporter;
import org.jgrapht.nio.json.JSONImporter;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.samples.spatial.RTreeVisualization;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LayoutHelper;
import org.jungrapht.samples.util.LensControlHelper;
import org.jungrapht.samples.util.SpanningTreeAdapter;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.LensMagnificationGraphMousePlugin;
import org.jungrapht.visualization.control.ModalLensGraphMouse;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayout;
import org.jungrapht.visualization.layout.algorithms.util.LayoutPaintable;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.transform.HyperbolicTransformer;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.MagnifyTransformer;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.MagnifyShapeTransformer;
import org.jungrapht.visualization.transform.shape.ViewLensSupport;
import org.jungrapht.visualization.util.GraphImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates several of the graph layout algorithms. Allows the user to interactively select one
 * of several graphs, loaded from files using JGrapht io, and one of several layouts, and visualizes
 * the combination.
 *
 * @author Tom Nelson
 */
public class ShowLayoutsWithGraphml extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(ShowLayoutsWithGraphml.class);

  LayoutPaintable.BalloonRings balloonLayoutRings;
  LayoutPaintable.RadialRings radialLayoutRings;
  JFileChooser fileChooser;

  public ShowLayoutsWithGraphml() {

    Graph<String, DefaultEdge> graph =
        GraphTypeBuilder.undirected()
            .edgeClass(DefaultEdge.class)
            .vertexSupplier(SupplierUtil.createStringSupplier(1))
            .buildGraph();
    JPanel container = new JPanel(new BorderLayout());

    final DefaultModalGraphMouse<Integer, DefaultEdge> graphMouse = new DefaultModalGraphMouse<>();

    final VisualizationViewer<String, DefaultEdge> vv =
        VisualizationViewer.builder(graph)
            .layoutSize(new Dimension(3000, 3000))
            .viewSize(new Dimension(900, 900))
            .graphMouse(graphMouse)
            .build();

    vv.getRenderContext().setVertexLabelFunction(Object::toString);

    vv.setVertexToolTipFunction(
        vertex ->
            vertex
                + ". with neighbors:"
                + Graphs.neighborListOf(vv.getVisualizationModel().getGraph(), vertex));

    vv.scaleToLayout();

    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(
        ((DefaultModalGraphMouse<Integer, DefaultEdge>) vv.getGraphMouse()).getModeListener());

    container.add(vv.getComponent(), BorderLayout.CENTER);
    LayoutHelper.Layouts[] combos = LayoutHelper.getCombos();
    final JToggleButton animateLayoutTransition = new JToggleButton("Animate Layout Transition");

    final JComboBox layoutComboBox = new JComboBox(combos);
    layoutComboBox.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  LayoutHelper.Layouts layoutType =
                      (LayoutHelper.Layouts) layoutComboBox.getSelectedItem();
                  LayoutAlgorithm layoutAlgorithm = layoutType.getLayoutAlgorithm();
                  vv.removePreRenderPaintable(balloonLayoutRings);
                  vv.removePreRenderPaintable(radialLayoutRings);
                  if ((layoutAlgorithm instanceof TreeLayout)
                      && vv.getVisualizationModel().getGraph().getType().isUndirected()) {
                    Graph tree =
                        SpanningTreeAdapter.getSpanningTree(vv.getVisualizationModel().getGraph());
                    LayoutModel positionModel =
                        this.getTreeLayoutPositions(
                            tree, layoutAlgorithm, vv.getVisualizationModel().getLayoutModel());
                    vv.getVisualizationModel().getLayoutModel().setInitializer(positionModel);
                    layoutAlgorithm = new StaticLayoutAlgorithm();
                  }
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm, vv::scaleToLayout);
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm, vv::scaleToLayout);
                  }
                  if (layoutAlgorithm instanceof BalloonLayoutAlgorithm) {
                    balloonLayoutRings =
                        new LayoutPaintable.BalloonRings(
                            vv, (BalloonLayoutAlgorithm) layoutAlgorithm);
                    vv.addPreRenderPaintable(balloonLayoutRings);
                  }
                  if (layoutAlgorithm instanceof RadialTreeLayoutAlgorithm) {
                    radialLayoutRings =
                        new LayoutPaintable.RadialRings(
                            vv, (RadialTreeLayoutAlgorithm) layoutAlgorithm);
                    vv.addPreRenderPaintable(radialLayoutRings);
                  }
                }));

    layoutComboBox.setSelectedItem(LayoutHelper.Layouts.FR_BH_VISITOR);

    // create a lens to share between the two hyperbolic transformers
    LayoutModel<String> layoutModel = vv.getVisualizationModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
    Lens lens = new Lens(); /* provides a Hyperbolic lens for the view */
    LensSupport<ModalLensGraphMouse> hyperbolicViewSupport =
        ViewLensSupport.<String, DefaultEdge, ModalLensGraphMouse>builder(vv)
            .lensTransformer(
                HyperbolicShapeTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.VIEW))
                    .build())
            .lensGraphMouse(new ModalLensGraphMouse())
            .build();

    LensSupport<ModalLensGraphMouse> hyperbolicLayoutSupport =
        LayoutLensSupport.<String, DefaultEdge, ModalLensGraphMouse>builder(vv)
            .lensTransformer(
                HyperbolicTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.LAYOUT))
                    .build())
            .lensGraphMouse(new ModalLensGraphMouse())
            .build();

    // the magnification lens uses a different magnification than the hyperbolic lens
    // create a new one to share between the two magnigy transformers
    lens = new Lens();
    lens.setMagnification(3.f);
    LensSupport<ModalLensGraphMouse> magnifyViewSupport =
        ViewLensSupport.<String, DefaultEdge, ModalLensGraphMouse>builder(vv)
            .lensTransformer(
                MagnifyShapeTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.VIEW))
                    .build())
            .lensGraphMouse(
                new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)))
            .build();

    LensSupport<ModalLensGraphMouse> magnifyLayoutSupport =
        LayoutLensSupport.<String, DefaultEdge, ModalLensGraphMouse>builder(vv)
            .lensTransformer(
                MagnifyTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.LAYOUT))
                    .build())
            .lensGraphMouse(
                new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)))
            .build();

    hyperbolicLayoutSupport
        .getLensTransformer()
        .getLens()
        .setLensShape(hyperbolicViewSupport.getLensTransformer().getLens().getLensShape());
    magnifyViewSupport
        .getLensTransformer()
        .getLens()
        .setLensShape(hyperbolicLayoutSupport.getLensTransformer().getLens().getLensShape());
    magnifyLayoutSupport
        .getLensTransformer()
        .getLens()
        .setLensShape(magnifyViewSupport.getLensTransformer().getLens().getLensShape());

    graphMouse.addItemListener(hyperbolicLayoutSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(magnifyLayoutSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(magnifyViewSupport.getGraphMouse().getModeListener());

    JComponent lensBox =
        LensControlHelper.builder(
                Map.of(
                    "Hyperbolic View", hyperbolicViewSupport,
                    "Hyperbolic Layout", hyperbolicLayoutSupport,
                    "Magnified View", magnifyViewSupport,
                    "Magnified Layout", magnifyLayoutSupport))
            .containerSupplier(JPanel::new)
            .containerLayoutManager(new GridLayout(0, 2))
            .title("Lens Controls")
            .build()
            .container();

    JButton loadFileButton = new JButton("Load File");
    loadFileButton.addActionListener(
        e -> {
          if (fileChooser == null) {
            fileChooser = new JFileChooser();
          }
          int option = fileChooser.showOpenDialog(vv.getComponent());
          if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getName();
            String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            GraphImporter importer;
            switch (suffix) {
              case "graphml":
                importer = new GraphMLImporter();
                break;
              case "gml":
                importer = new GmlImporter();
                break;
              case "dot":
                importer = new DOTImporter();
                break;
              case "csv":
                importer = new CSVImporter();
                break;
              case "json":
                importer = new JSONImporter();
                break;
              default:
                JOptionPane.showMessageDialog(vv.getComponent(), "Unable to open " + fileName);
                return;
            }
            clear(graph);
            try (InputStreamReader inputStreamReader = new FileReader(file)) {
              importer.importGraph(graph, inputStreamReader);
            } catch (Exception ex) {
              ex.printStackTrace();
            }
            int size = (int) (50 * Math.sqrt(graph.vertexSet().size()));
            vv.getVisualizationModel().getLayoutModel().setSize(size, size);
            vv.getVisualizationModel().setGraph(graph);
            setTitle(
                "Graph of "
                    + fileName
                    + " with "
                    + graph.vertexSet().size()
                    + " vertices and "
                    + graph.edgeSet().size()
                    + " edges");
          }
        });

    JPanel controlPanel = new JPanel(new GridLayout(2, 1));
    JComponent top =
        ControlHelpers.getContainer(
            Box.createHorizontalBox(),
            ControlHelpers.getCenteredContainer("Layouts", layoutComboBox),
            ControlHelpers.getCenteredContainer(Box.createHorizontalBox(), loadFileButton));
    controlPanel.add(top);

    JButton showRTree = new JButton("Show RTree");
    showRTree.addActionListener(e -> RTreeVisualization.showRTree(vv));

    JButton imageButton = new JButton("Save Image");
    imageButton.addActionListener(e -> GraphImage.capture(vv));

    JComponent bottom =
        ControlHelpers.getContainer(
            Box.createHorizontalBox(),
            ControlHelpers.getZoomControls("Scale", vv),
            imageButton,
            ControlHelpers.getCenteredContainer("Mouse Mode", modeBox),
            lensBox,
            ControlHelpers.getCenteredContainer(
                "Effects", Box.createVerticalBox(), showRTree, animateLayoutTransition));
    controlPanel.add(bottom);
    container.add(controlPanel, BorderLayout.NORTH);

    setTitle(
        "Graph With "
            + graph.vertexSet().size()
            + " vertices and "
            + graph.edgeSet().size()
            + " edges");
    getContentPane().add(container);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    pack();
    setVisible(true);
  }

  void clear(Graph graph) {
    Set edges = new HashSet(graph.edgeSet());
    Set vertices = new HashSet(graph.vertexSet());
    edges.forEach(graph::removeEdge);
    vertices.forEach(graph::removeVertex);
  }

  LayoutModel getTreeLayoutPositions(
      Graph tree, LayoutAlgorithm treeLayout, LayoutModel layoutModel) {
    LayoutModel model =
        LayoutModel.builder()
            .size(layoutModel.getWidth(), layoutModel.getHeight())
            .graph(tree)
            .build();
    model.accept(treeLayout);
    return model;
  }

  private Collection getRoots(Graph graph) {
    Set roots = new HashSet<>();
    for (Object v : graph.vertexSet()) {
      if (Graphs.predecessorListOf(graph, v).isEmpty()) {
        roots.add(v);
      }
    }
    return roots;
  }

  public static void main(String[] args) {
    new ShowLayoutsWithGraphml();
  }
}
