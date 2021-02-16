package org.jungrapht.samples.large;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.alg.scoring.AlphaCentrality;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.alg.scoring.ClosenessCentrality;
import org.jgrapht.alg.scoring.ClusteringCoefficient;
import org.jgrapht.alg.scoring.HarmonicCentrality;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.GraphImporter;
import org.jgrapht.nio.csv.CSVImporter;
import org.jgrapht.nio.dimacs.DIMACSImporter;
import org.jgrapht.nio.dot.DOTImporter;
import org.jgrapht.nio.gml.GmlImporter;
import org.jgrapht.nio.graphml.GraphMLImporter;
import org.jgrapht.nio.json.JSONImporter;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.samples.spatial.RTreeVisualization;
import org.jungrapht.samples.util.Colors;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LayoutHelper;
import org.jungrapht.samples.util.LensControlHelper;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.control.DefaultLensGraphMouse;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.transform.HyperbolicTransformer;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.MagnifyTransformer;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.MagnifyShapeTransformer;
import org.jungrapht.visualization.transform.shape.ViewLensSupport;
import org.jungrapht.visualization.util.LayoutAlgorithmTransition;
import org.jungrapht.visualization.util.LayoutPaintable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates several of the graph layout algorithms. Allows the user to interactively select one
 * of several graphs, loaded from files using JGrapht io, and one of several layouts, and visualizes
 * the combination.
 *
 * <p>This application has been tested with the graphml sample files in
 * https://github.com/melaniewalsh/sample-social-network-datasets
 *
 * @author Tom Nelson
 */
public class ShowLayoutsWithGraphFileImport extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(ShowLayoutsWithGraphFileImport.class);

  LayoutPaintable.BalloonRings balloonLayoutRings;
  LayoutPaintable.RadialRings radialLayoutRings;
  LayoutPaintable.LayoutBounds layoutBounds;
  JFileChooser fileChooser;
  Map<String, Map<String, Attribute>> vertexAttributes = new HashMap<>();
  Map<DefaultEdge, Map<String, Attribute>> edgeAttributes = new HashMap<>();
  VisualizationViewer<String, DefaultEdge> vv;
  Paint[] colorArray =
      new Paint[] {
        Color.red,
        Color.green,
        Color.blue,
        Color.cyan,
        Color.magenta,
        Color.yellow,
        Color.pink,
        Color.gray,
        Color.darkGray,
        Color.lightGray,
        Color.orange
      };

  Function<String, Paint> vertexFillPaintFunction =
      v -> {
        // try to parse from attributemap
        Map<String, Attribute> map =
            vertexAttributes
                .getOrDefault(v, Collections.emptyMap())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
        return Colors.getColor(map);
      };

  public ShowLayoutsWithGraphFileImport() {

    Graph<String, DefaultEdge> graph =
        GraphTypeBuilder.undirected()
            .edgeClass(DefaultEdge.class)
            .vertexSupplier(SupplierUtil.createStringSupplier(1))
            .allowingSelfLoops(true)
            .allowingMultipleEdges(true)
            .vertexSupplier(SupplierUtil.createStringSupplier(1))
            .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
            .buildGraph();
    JPanel container = new JPanel(new BorderLayout());

    final DefaultGraphMouse<Integer, DefaultEdge> graphMouse = new DefaultGraphMouse<>();

    vv =
        VisualizationViewer.builder(graph)
            .layoutSize(new Dimension(1800, 1800))
            .viewSize(new Dimension(900, 900))
            .graphMouse(graphMouse)
            .build();

    vv.setVertexToolTipFunction(vertex -> vertex + " " + vertexAttributes.get(vertex));
    vv.getRenderContext()
        .setVertexLabelFunction(
            vertex -> {
              Map<String, Attribute> map =
                  vertexAttributes.getOrDefault(vertex, Collections.emptyMap());
              return map.getOrDefault(
                      "label",
                      map.getOrDefault("ID", new DefaultAttribute(vertex, AttributeType.STRING)))
                  .getValue();
            });
    vv.setEdgeToolTipFunction(edge -> edgeAttributes.get(edge).toString());
    vv.getRenderContext().setVertexFillPaintFunction(vertexFillPaintFunction);

    vv.getRenderContext()
        .setVertexShapeFunction(
            v -> {
              Graph<String, DefaultEdge> g = vv.getVisualizationModel().getGraph();
              if (!g.containsVertex(v)) {
                log.error("shapeFunction {} was not in {}", v, g.vertexSet());
              }
              int size = Math.max(5, 2 * (g.containsVertex(v) ? g.degreeOf(v) : 20));
              return new Ellipse2D.Float(-size / 2.f, -size / 2.f, size, size);
            });
    Function<String, Paint> vertexDrawPaintFunction =
        v -> vv.getSelectedVertexState().isSelected(v) ? Color.pink : Color.black;
    Function<String, Stroke> vertexStrokeFunction =
        v ->
            vv.getSelectedVertexState().isSelected(v) ? new BasicStroke(8.f) : new BasicStroke(2.f);
    vv.getRenderContext().setVertexStrokeFunction(vertexStrokeFunction);

    vv.scaleToLayout();

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
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
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
                  if (layoutBounds != null) {
                    vv.removePreRenderPaintable(layoutBounds);
                  }
                  vv.addPreRenderPaintable(new LayoutPaintable.LayoutBounds(vv));
                }));

    layoutComboBox.setSelectedItem(LayoutHelper.Layouts.FR_BH_VISITOR);

    // create a lens to share between the two hyperbolic transformers
    LayoutModel<String> layoutModel = vv.getVisualizationModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
    Lens lens = new Lens(); /* provides a Hyperbolic lens for the view */
    LensSupport<DefaultLensGraphMouse> hyperbolicViewSupport =
        ViewLensSupport.<String, DefaultEdge, DefaultLensGraphMouse>builder(vv)
            .lensTransformer(
                HyperbolicShapeTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.VIEW))
                    .build())
            .lensGraphMouse(new DefaultLensGraphMouse<>())
            .build();

    LensSupport<DefaultLensGraphMouse> hyperbolicLayoutSupport =
        LayoutLensSupport.<String, DefaultEdge, DefaultLensGraphMouse>builder(vv)
            .lensTransformer(
                HyperbolicTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.LAYOUT))
                    .build())
            .lensGraphMouse(new DefaultLensGraphMouse<>())
            .build();

    // the magnification lens uses a different magnification than the hyperbolic lens
    // create a new one to share between the two magnigy transformers
    lens = new Lens();
    lens.setMagnification(3.f);
    LensSupport<DefaultLensGraphMouse> magnifyViewSupport =
        ViewLensSupport.<String, DefaultEdge, DefaultLensGraphMouse>builder(vv)
            .lensTransformer(
                MagnifyShapeTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.VIEW))
                    .build())
            .lensGraphMouse(new DefaultLensGraphMouse<>())
            .build();

    LensSupport<DefaultLensGraphMouse> magnifyLayoutSupport =
        LayoutLensSupport.<String, DefaultEdge, DefaultLensGraphMouse>builder(vv)
            .lensTransformer(
                MagnifyTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.LAYOUT))
                    .build())
            .lensGraphMouse(new DefaultLensGraphMouse<>())
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

    JPanel loadFilePanel = new JPanel();
    JButton loadFileButton = new JButton("Load File");
    loadFilePanel.add(loadFileButton);
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
            GraphImporter<String, DefaultEdge> importer;
            switch (suffix) {
              case "graphml":
                importer = new GraphMLImporter<>();
                ((GraphMLImporter) importer).setSchemaValidation(false);
                GraphMLImporter gmlImporter = (GraphMLImporter) importer;
                gmlImporter.addVertexAttributeConsumer(
                    (BiConsumer<Pair<String, String>, Attribute>)
                        (pair, attribute) -> {
                          String vertex = pair.getFirst();
                          String key = pair.getSecond();
                          vertexAttributes
                              .computeIfAbsent(vertex, m -> new HashMap<>())
                              .put(key, attribute);
                        });
                gmlImporter.addEdgeAttributeConsumer(
                    (BiConsumer<Pair<DefaultEdge, String>, Attribute>)
                        (pair, attribute) -> {
                          DefaultEdge de = pair.getFirst();
                          String key = pair.getSecond();
                          edgeAttributes
                              .computeIfAbsent(de, m -> new HashMap<>())
                              .put(key, attribute);
                        });
                break;
              case "gml":
                importer = new GmlImporter<>();
                break;
              case "dot":
              case "gv":
                importer = new DOTImporter<>();
                break;
              case "csv":
                importer = new CSVImporter<>();
                break;
              case "col":
                importer = new DIMACSImporter<>();
                break;
              case "json":
                JSONImporter<String, DefaultEdge> jsonImporter = new JSONImporter<>();
                jsonImporter.addVertexAttributeConsumer(
//                    (BiConsumer<Pair<String, String>, Attribute>)
                        (pair, attribute) -> {
                          String vertex = pair.getFirst();
                          String key = pair.getSecond();
                          vertexAttributes
                              .computeIfAbsent(vertex, m -> new HashMap<>())
                              .put(key, attribute);
                        });
                jsonImporter.addEdgeAttributeConsumer(
//                    (BiConsumer<Pair<String, String>, Attribute>)
                        (pair, attribute) -> {
                          DefaultEdge de = pair.getFirst();
                          String key = pair.getSecond();
                          edgeAttributes
                              .computeIfAbsent(de, m -> new HashMap<>())
                              .put(key, attribute);
                        });

                importer = jsonImporter;
                break;
              default:
                JOptionPane.showMessageDialog(vv.getComponent(), "Unable to open " + fileName);
                return;
            }
            clear(graph);
            vertexAttributes.clear();
            vv.getRenderContext().setVertexFillPaintFunction(vertexFillPaintFunction);
            try (InputStreamReader inputStreamReader = new FileReader(file)) {
              importer.importGraph(graph, inputStreamReader);
            } catch (Exception ex) {
              ex.printStackTrace();
            }
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

    JButton pageRankButton = new JButton("Page Rank");
    pageRankButton.addActionListener(event -> computeScores(new PageRank(graph)));
    JButton betweennessButton = new JButton("Betweenness");
    betweennessButton.addActionListener(event -> computeScores(new BetweennessCentrality<>(graph)));
    JButton alphaButton = new JButton("Alpha");
    alphaButton.addActionListener(event -> computeScores(new AlphaCentrality<>(graph)));
    JButton closenessButton = new JButton("Closeness");
    closenessButton.addActionListener(event -> computeScores(new ClosenessCentrality<>(graph)));
    JButton clusteringButton = new JButton("Clustering");
    clusteringButton.addActionListener(event -> computeScores(new ClusteringCoefficient<>(graph)));
    //    JButton corenessButton = new JButton("Coreness");
    //        corenessButton.addActionListener(event -> computeScores(new Coreness<>(graph)));
    JButton harmonicButton = new JButton("Harmonic");
    harmonicButton.addActionListener(event -> computeScores(new HarmonicCentrality<>(graph)));
    JButton noScores = new JButton("None");
    noScores.addActionListener(
        event -> {
          vv.getRenderContext().setVertexFillPaintFunction(vertexFillPaintFunction);
          vv.repaint();
        });

    JPanel scoringGrid = new JPanel(new GridLayout(0, 2));
    scoringGrid.add(pageRankButton);
    scoringGrid.add(betweennessButton);
    //    scoringGrid.add(alphaButton);
    scoringGrid.add(closenessButton);
    scoringGrid.add(clusteringButton);
    scoringGrid.add(harmonicButton);
    scoringGrid.add(noScores);

    JPanel controlPanel = new JPanel(new GridLayout(2, 1));
    JComponent top =
        ControlHelpers.getContainer(
            Box.createHorizontalBox(),
            ControlHelpers.getCenteredContainer("Graph Source", loadFilePanel),
            ControlHelpers.getCenteredContainer("Layouts", layoutComboBox),
            ControlHelpers.getCenteredContainer("Scoring", scoringGrid));

    controlPanel.add(top);

    JButton showRTree = new JButton("Show RTree");
    showRTree.addActionListener(e -> RTreeVisualization.showRTree(vv));

    JComponent bottom =
        ControlHelpers.getContainer(
            Box.createHorizontalBox(),
            //            ControlHelpers.getZoomControls("Scale", vv),
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

  private void computeScores(VertexScoringAlgorithm<String, Double> scoring) {
    Map<String, Double> scores = scoring.getScores();
    if (scores.isEmpty()) return;
    double min = scores.values().stream().min(Double::compare).get();
    double max = scores.values().stream().max(Double::compare).get();
    double range = max - min;
    log.trace("min:{}, max:{}, range:{}", min, max, range);
    double delta = range / colorArray.length;
    log.trace("delta:{}", delta);
    vv.getRenderContext()
        .setVertexFillPaintFunction(
            v -> {
              if (scores.isEmpty() || !scores.containsKey(v)) return Color.red;
              double score = scores.get(v);
              double index = score / delta;
              int idx = (int) Math.max(0, Math.min(colorArray.length - 1, index));
              return colorArray[idx];
            });
    vv.repaint();
  }

  void clear(Graph graph) {
    Set edges = new HashSet(graph.edgeSet());
    Set vertices = new HashSet(graph.vertexSet());
    edges.forEach(graph::removeEdge);
    vertices.forEach(graph::removeVertex);
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
    new ShowLayoutsWithGraphFileImport();
  }
}
