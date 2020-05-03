package org.jungrapht.samples.large;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.EdgeProvider;
import org.jgrapht.io.GmlImporter;
import org.jgrapht.io.VertexProvider;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates several of the graph layout algorithms. Allows the user to interactively select one
 * of several graphs, and one of several layouts, and visualizes the combination.
 *
 * @author Tom Nelson
 */
public class ShowLayoutsWithJGraphtIO extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(ShowLayoutsWithJGraphtIO.class);

  enum GraphLinks {
    ROUTERS("https://gephi.org/datasets/internet_routers-22july06.gml.zip"),
    LES_MISERABLES("https://gephi.org/datasets/lesmiserables.gml.zip"),
    KARATE("https://gephi.org/datasets/karate.gml.zip"),
    NETSCIENCE("https://gephi.org/datasets/netscience.gml.zip"),
    WORD_ADJACENCIES("https://gephi.org/datasets/word_adjacencies.gml.zip"),
    POWER("https://gephi.org/datasets/power.gml.zip");
    private final String url;

    GraphLinks(String url) {
      this.url = url;
    }
  }

  public static GraphLinks[] getCombos() {
    return GraphLinks.values();
  }

  LayoutPaintable.BalloonRings balloonLayoutRings;
  LayoutPaintable.RadialRings radialLayoutRings;

  public ShowLayoutsWithJGraphtIO() {

    Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
    JPanel container = new JPanel(new BorderLayout());

    VertexProvider<String> vp = (label, attributes) -> label;
    EdgeProvider<String, DefaultEdge> ep =
        (from, to, label, attributes) -> graph.getEdgeSupplier().get();

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

    final JComboBox graphComboBox = new JComboBox(getCombos());
    graphComboBox.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  clear(graph);
                  String urlString = ((GraphLinks) graphComboBox.getSelectedItem()).url;
                  try (InputStreamReader inputStreamReader = get(urlString)) {
                    GmlImporter gmlImporter = new GmlImporter(vp, ep);
                    gmlImporter.importGraph(graph, inputStreamReader);
                  } catch (Exception ex) {
                    ex.printStackTrace();
                  }
                  int size = (int) (50 * Math.sqrt(graph.vertexSet().size()));
                  vv.getVisualizationModel().getLayoutModel().setSize(size, size);
                  vv.getVisualizationModel().setGraph(graph);
                  setTitle(
                      "Graph With "
                          + graph.vertexSet().size()
                          + " vertices and "
                          + graph.edgeSet().size()
                          + " edges");
                }));

    graphComboBox.setSelectedItem(GraphLinks.NETSCIENCE);

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

    JPanel controlPanel = new JPanel(new GridLayout(2, 1));
    JComponent top =
        ControlHelpers.getContainer(
            Box.createHorizontalBox(),
            ControlHelpers.getCenteredContainer("Layouts", layoutComboBox),
            ControlHelpers.getCenteredContainer("Graphs", graphComboBox));
    controlPanel.add(top);

    JButton showRTree = new JButton("Show RTree");
    showRTree.addActionListener(e -> RTreeVisualization.showRTree(vv));

    JComponent bottom =
        ControlHelpers.getContainer(
            Box.createHorizontalBox(),
            ControlHelpers.getZoomControls("Scale", vv),
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

  static InputStreamReader get(String urlString) throws Exception {
    URL url = new URL(urlString);
    InputStream inputStream = url.openStream();
    if (urlString.endsWith(".zip")) {
      inputStream = new ZipInputStream(inputStream);
      ((ZipInputStream) inputStream).getNextEntry();
    } else if (urlString.endsWith(".gz")) {
      inputStream = new GZIPInputStream(inputStream);
    }
    return new InputStreamReader(inputStream);
  }

  public static void main(String[] args) {
    new ShowLayoutsWithJGraphtIO();
  }
}
