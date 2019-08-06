package org.jungrapht.samples;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
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
import org.jgrapht.io.ImportException;
import org.jgrapht.io.VertexProvider;
import org.jungrapht.samples.util.LayoutHelper;
import org.jungrapht.samples.util.SpanningTreeAdapter;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.LoadingCacheLayoutModel;
import org.jungrapht.visualization.util.BalloonLayoutRings;
import org.jungrapht.visualization.util.RadialLayoutRings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates several of the graph layout algorithms. Allows the user to interactively select one
 * of several graphs, and one of several layouts, and visualizes the combination.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
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

  BalloonLayoutRings balloonLayoutRings;
  RadialLayoutRings radialLayoutRings;

  public ShowLayoutsWithJGraphtIO() throws Exception {

    Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
    JPanel container = new JPanel(new BorderLayout());

    VertexProvider<String> vp = (label, attributes) -> label;
    EdgeProvider<String, DefaultEdge> ep =
        (from, to, label, attributes) -> graph.getEdgeSupplier().get();

    final VisualizationViewer<String, DefaultEdge> vv =
        VisualizationViewer.builder(graph)
            .layoutSize(new Dimension(3000, 3000))
            .viewSize(new Dimension(1000, 1000))
            .build();

    vv.getRenderContext().setVertexLabelFunction(Object::toString);

    final DefaultModalGraphMouse<Integer, DefaultEdge> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    vv.setVertexToolTipFunction(
        vertex ->
            vertex.toString()
                + ". with neighbors:"
                + Graphs.neighborListOf(vv.getVisualizationModel().getGraph(), vertex));

    final ScalingControl scaler = new CrossoverScalingControl();
    vv.scaleToLayout(scaler);

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(
        ((DefaultModalGraphMouse<Integer, DefaultEdge>) vv.getGraphMouse()).getModeListener());

    vv.setBackground(Color.WHITE);

    final JComboBox graphComboBox = new JComboBox(getCombos());
    graphComboBox.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  clear(graph);
                  String urlString = ((GraphLinks) graphComboBox.getSelectedItem()).url;
                  try (InputStreamReader inputStreamReader = get(urlString)) {
                    GmlImporter gmlImporter = new GmlImporter(vp, ep);
                    try {
                      gmlImporter.importGraph(graph, inputStreamReader);
                    } catch (ImportException ex) {
                      ex.printStackTrace();
                    }
                  } catch (IOException ex) {
                    ex.printStackTrace();
                  } catch (Exception ex) {
                    ex.printStackTrace();
                  }
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
    final JRadioButton animateLayoutTransition = new JRadioButton("Animate Layout Transition");

    final JComboBox jcb = new JComboBox(combos);
    jcb.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  LayoutHelper.Layouts layoutType = (LayoutHelper.Layouts) jcb.getSelectedItem();
                  LayoutAlgorithm layoutAlgorithm = layoutType.getLayoutAlgorithm();
                  vv.removePreRenderPaintable(balloonLayoutRings);
                  vv.removePreRenderPaintable(radialLayoutRings);
                  if ((layoutAlgorithm instanceof TreeLayoutAlgorithm)
                      && vv.getVisualizationModel().getGraph().getType().isUndirected()) {
                    Graph tree =
                        SpanningTreeAdapter.getSpanningTree(vv.getVisualizationModel().getGraph());
                    LayoutModel positionModel = this.getTreeLayoutPositions(tree, layoutAlgorithm);
                    vv.getVisualizationModel().getLayoutModel().setInitializer(positionModel);
                    layoutAlgorithm = new StaticLayoutAlgorithm();
                  }
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
                  }
                  if (layoutAlgorithm instanceof BalloonLayoutAlgorithm) {
                    balloonLayoutRings =
                        new BalloonLayoutRings(vv, (BalloonLayoutAlgorithm) layoutAlgorithm);
                    vv.addPreRenderPaintable(balloonLayoutRings);
                  }
                  if (layoutAlgorithm instanceof RadialTreeLayoutAlgorithm) {
                    radialLayoutRings =
                        new RadialLayoutRings(vv, (RadialTreeLayoutAlgorithm) layoutAlgorithm);
                    vv.addPreRenderPaintable(radialLayoutRings);
                  }

                  Preconditions.checkState(
                      true,
                      "oops3",
                      vv.getVisualizationModel()
                          .getModelChangeSupport()
                          .getModelChangeListeners()
                          .contains(this));

                  Preconditions.checkState(
                      true,
                      "oops4",
                      vv.getVisualizationModel()
                          .getLayoutModel()
                          .getModelChangeSupport()
                          .getModelChangeListeners()
                          .contains(vv.getVisualizationModel()));
                }));

    jcb.setSelectedItem(LayoutHelper.Layouts.FR_BH_VISITOR);

    JPanel control_panel = new JPanel(new GridLayout(2, 1));
    JPanel topControls = new JPanel();
    JPanel bottomControls = new JPanel();
    control_panel.add(topControls);
    control_panel.add(bottomControls);
    container.add(control_panel, BorderLayout.NORTH);

    JButton showRTree = new JButton("Show RTree");
    showRTree.addActionListener(e -> RTreeVisualization.showRTree(vv));

    topControls.add(jcb);
    topControls.add(graphComboBox);
    bottomControls.add(animateLayoutTransition);
    bottomControls.add(plus);
    bottomControls.add(minus);
    bottomControls.add(modeBox);
    bottomControls.add(showRTree);

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
    Set edges = Sets.newHashSet(graph.edgeSet());
    Set vertices = Sets.newHashSet(graph.vertexSet());
    edges.stream().forEach(e -> graph.removeEdge(e));
    vertices.stream().forEach(v -> graph.removeVertex(v));
  }

  LayoutModel getTreeLayoutPositions(Graph tree, LayoutAlgorithm treeLayout) {
    LayoutModel model = LoadingCacheLayoutModel.builder().size(600, 600).graph(tree).build();
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

  public static void main(String[] args) throws Exception {
    new ShowLayoutsWithJGraphtIO();
  }
}
