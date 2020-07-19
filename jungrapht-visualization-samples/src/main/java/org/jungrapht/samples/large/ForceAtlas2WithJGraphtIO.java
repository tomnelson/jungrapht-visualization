package org.jungrapht.samples.large;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.gml.GmlImporter;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.ForceAtlas2ControlPanel;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.layout.algorithms.ForceAtlas2LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFA2Repulsion;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates settings for ForceAtlas2 layout algorithm. Allows the user to interactively select
 * one of several graphs.
 *
 * @author Tom Nelson
 */
public class ForceAtlas2WithJGraphtIO extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(ForceAtlas2WithJGraphtIO.class);

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

  BarnesHutFA2Repulsion.Builder<String> repulsion = BarnesHutFA2Repulsion.builder();
  ForceAtlas2LayoutAlgorithm.Builder<String, ?, ?> builder = ForceAtlas2LayoutAlgorithm.builder();

  public static GraphLinks[] getCombos() {
    return GraphLinks.values();
  }

  public ForceAtlas2WithJGraphtIO() {

    Graph<String, DefaultEdge> graph =
        GraphTypeBuilder.undirected()
            .edgeClass(DefaultEdge.class)
            .vertexSupplier(SupplierUtil.createStringSupplier(1))
            .buildGraph();

    JPanel container = new JPanel(new BorderLayout());

    final DefaultGraphMouse<Integer, DefaultEdge> graphMouse = new DefaultGraphMouse<>();

    final VisualizationViewer<String, DefaultEdge> vv =
        VisualizationViewer.builder(graph)
            .layoutSize(new Dimension(3000, 3000))
            .viewSize(new Dimension(900, 900))
            .graphMouse(graphMouse)
            .build();

    ForceAtlas2LayoutAlgorithm layoutAlgorithm =
        builder
            .repulsionContractBuilder(repulsion)
            //                .after(vv::resizeToLayout)
            .build();
    vv.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm);

    final JComboBox graphComboBox = new JComboBox(getCombos());
    graphComboBox.addActionListener(
        e -> {
          LayoutModel<String> layoutModel = vv.getVisualizationModel().getLayoutModel();
          layoutModel.stopRelaxer();
          SwingUtilities.invokeLater(
              () -> {
                clear(graph);
                String urlString = ((GraphLinks) graphComboBox.getSelectedItem()).url;
                try (InputStreamReader inputStreamReader = get(urlString)) {
                  GmlImporter gmlImporter = new GmlImporter();
                  gmlImporter.importGraph(graph, inputStreamReader);
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
                int size = (int) (50 * Math.sqrt(graph.vertexSet().size()));
                vv.getVisualizationModel().getLayoutModel().setSize(size, size);
                vv.getVisualizationModel().setGraph(graph, true);
                vv.scaleToLayout();
                setTitle(
                    "Graph With "
                        + graph.vertexSet().size()
                        + " vertices and "
                        + graph.edgeSet().size()
                        + " edges");
              });
        });

    graphComboBox.setSelectedItem(GraphLinks.NETSCIENCE);

    container.add(vv.getComponent(), BorderLayout.CENTER);

    JPanel controlPanel = new JPanel(new GridLayout(2, 1));

    JComponent top =
        ControlHelpers.getContainer(
            Box.createHorizontalBox(),
            ControlHelpers.getCenteredContainer("Graphs", graphComboBox),
            new ForceAtlas2ControlPanel<>(vv.getVisualizationModel()));
    controlPanel.add(top);
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
    new ForceAtlas2WithJGraphtIO();
  }
}
