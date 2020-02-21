package org.jungrapht.samples.large;

import com.google.common.base.Strings;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.EdgeProvider;
import org.jgrapht.io.GmlImporter;
import org.jgrapht.io.VertexProvider;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.layout.algorithms.ForceAtlas2LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFA2Repulsion;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates several of the graph layout algorithms. Allows the user to interactively select one
 * of several graphs, and one of several layouts, and visualizes the combination.
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

    Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
    JPanel container = new JPanel(new BorderLayout());

    VertexProvider<String> vp = (label, attributes) -> label;
    EdgeProvider<String, DefaultEdge> ep =
        (from, to, label, attributes) -> graph.getEdgeSupplier().get();

    final DefaultGraphMouse<Integer, DefaultEdge> graphMouse = new DefaultGraphMouse<>();

    final VisualizationViewer<String, DefaultEdge> vv =
        VisualizationViewer.builder(graph)
            .layoutSize(new Dimension(3000, 3000))
            .viewSize(new Dimension(900, 900))
            .graphMouse(graphMouse)
            .build();

    ForceAtlas2LayoutAlgorithm layoutAlgorithm =
        builder.repulsionContractBuilder(repulsion).build();

    vv.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm);

    vv.scaleToLayout();

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
              });
        });

    graphComboBox.setSelectedItem(GraphLinks.NETSCIENCE);

    container.add(vv.getComponent(), BorderLayout.CENTER);

    JPanel layoutControls = new JPanel(new GridLayout(0, 3));

    JTextField repulsionText = new JTextField("100.0", 15);
    repulsionText.setBorder(new TitledBorder("Repulsion"));

    JRadioButton useLinLogButton = new JRadioButton("Use LinLog");
    layoutControls.add(useLinLogButton);

    JRadioButton attractionByWeights = new JRadioButton("Attraction By Weights");

    JRadioButton dissuadeHubs = new JRadioButton("Dissuade Hubs");

    JTextField weightsDeltaText = new JTextField("1.0", 15);
    weightsDeltaText.setBorder(new TitledBorder("Weights Delta"));

    JTextField gravityKText = new JTextField("5.0", 15);
    gravityKText.setBorder(new TitledBorder("GravityK"));

    JTextField maxIterationsText = new JTextField("1000", 15);
    maxIterationsText.setBorder(new TitledBorder("Max Iterations"));

    JTextField toleranceText = new JTextField("1.0", 15);
    toleranceText.setBorder(new TitledBorder("Tolerance"));
    ActionListener action =
        e -> {
          if (!Strings.isNullOrEmpty(repulsionText.getText())) {
            try {
              repulsion.repulsionK(Double.parseDouble(repulsionText.getText()));
            } catch (Exception ex) {
            }
          }

          if (!Strings.isNullOrEmpty(weightsDeltaText.getText())) {
            try {
              builder.delta(Double.parseDouble(weightsDeltaText.getText()));
            } catch (Exception ex) {
            }
          }
          if (!Strings.isNullOrEmpty(maxIterationsText.getText())) {
            try {
              builder.maxIterations(Integer.parseInt(maxIterationsText.getText()));
            } catch (Exception ex) {
            }
          }
          if (!Strings.isNullOrEmpty(gravityKText.getText())) {
            try {
              builder.gravityK(Double.parseDouble(gravityKText.getText()));
            } catch (Exception ex) {
            }
          }

          builder.linLog(useLinLogButton.isSelected());
          builder.attractionByWeights(attractionByWeights.isSelected());
          builder.dissuadeHubs(dissuadeHubs.isSelected());
          builder.repulsionContractBuilder(repulsion);
          vv.getVisualizationModel().setLayoutAlgorithm(builder.build());
        };
    repulsionText.addActionListener(action);
    useLinLogButton.addActionListener(action);
    attractionByWeights.addActionListener(action);
    dissuadeHubs.addActionListener(action);
    weightsDeltaText.addActionListener(action);
    gravityKText.addActionListener(action);
    maxIterationsText.addActionListener(action);
    toleranceText.addActionListener(action);

    layoutControls.add(attractionByWeights);
    layoutControls.add(repulsionText);
    layoutControls.add(weightsDeltaText);
    layoutControls.add(useLinLogButton);
    layoutControls.add(gravityKText);
    layoutControls.add(toleranceText);
    layoutControls.add(dissuadeHubs);
    layoutControls.add(maxIterationsText);

    JButton resetDefaultsButton = new JButton("Reset Defaults");
    resetDefaultsButton.addActionListener(
        e -> {
          repulsionText.setText("100.0");
          useLinLogButton.setSelected(false);
          attractionByWeights.setSelected(false);
          dissuadeHubs.setSelected(false);
          weightsDeltaText.setText("1.0");
          gravityKText.setText("5.0");
          maxIterationsText.setText("1000");
          toleranceText.setText("1.0");
        });
    layoutControls.add(resetDefaultsButton);

    JPanel controlPanel = new JPanel(new GridLayout(2, 1));
    JComponent top =
        ControlHelpers.getContainer(
            Box.createHorizontalBox(),
            ControlHelpers.getCenteredContainer("Graphs", graphComboBox),
            layoutControls);
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
