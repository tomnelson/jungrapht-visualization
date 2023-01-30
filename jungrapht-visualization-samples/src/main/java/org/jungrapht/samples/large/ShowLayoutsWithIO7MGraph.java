package org.jungrapht.samples.large;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.alg.scoring.*;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.samples.spatial.RTreeVisualization;
import org.jungrapht.samples.util.*;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.control.DefaultLensGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.*;
import org.jungrapht.visualization.layout.util.LeftToRight;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.transform.*;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.MagnifyShapeTransformer;
import org.jungrapht.visualization.transform.shape.ViewLensSupport;
import org.jungrapht.visualization.util.LayoutAlgorithmTransition;
import org.jungrapht.visualization.util.LayoutPaintable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Map.entry;
import static org.jungrapht.visualization.util.Attributed.*;

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
public class ShowLayoutsWithIO7MGraph extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(ShowLayoutsWithIO7MGraph.class);

  LayoutPaintable.BalloonRings balloonLayoutRings;
  LayoutPaintable.RadialRings radialLayoutRings;
  LayoutPaintable.LayoutBounds layoutBounds;
  JFileChooser fileChooser;
  VisualizationViewer<String, Integer> vv;
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

//  Function<String, Paint> vertexFillPaintFunction = v -> Colors.getColor(v.getAttributeMap());

//  Function<Integer, Paint> edgeDrawPaintFunction = e -> Colors.getColor(e.getAttributeMap());

  public ShowLayoutsWithIO7MGraph() {

    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>directed()
            .allowingSelfLoops(true)
            .allowingMultipleEdges(true)
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    JPanel container = new JPanel(new BorderLayout());

    final DefaultGraphMouse<String, Integer> graphMouse = new DefaultGraphMouse<>();

    vv =
        VisualizationViewer.builder(graph)
            .layoutSize(new Dimension(600, 600))
            .viewSize(new Dimension(600, 600))
            .graphMouse(graphMouse)
            .build();

    populateGraph(graph);

//    if (!ASAILoader.load("ghidra.json", graph)) {
//      return;
//    }
    vv.getVisualizationModel().setGraph(graph);
    setTitle(
        "Graph of "
            + "ghidra.json"
            + " with "
            + graph.vertexSet().size()
            + " vertices and "
            + graph.edgeSet().size()
            + " edges");

    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.S);
    vv.getRenderContext()
        .setVertexLabelFunction(Object::toString);
//            vertex -> {
//              Map<String, String> map = vertex.getAttributeMap();
//              //                  vertexAttributes.getOrDefault(vertex, Collections.emptyMap());
//              return map.getOrDefault("Name", map.getOrDefault("ID", "NONE"));
//            });
//    vv.setEdgeToolTipFunction(e -> e.toHtml());

    Function<String, Stroke> vertexStrokeFunction =
        v ->
            vv.getSelectedVertexState().isSelected(v) ? new BasicStroke(8.f) : new BasicStroke(2.f);
    vv.getRenderContext().setVertexStrokeFunction(vertexStrokeFunction);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.scaleToLayout();

    container.add(vv.getComponent(), BorderLayout.CENTER);
    LayoutHelperDirectedGraphs.Layouts[] combos = LayoutHelperDirectedGraphs.getCombos();
    final JToggleButton animateLayoutTransition = new JToggleButton("Animate Layout Transition");

    final JComboBox layoutComboBox = new JComboBox(combos);
    layoutComboBox.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  LayoutHelperDirectedGraphs.Layouts layoutType =
                      (LayoutHelperDirectedGraphs.Layouts) layoutComboBox.getSelectedItem();
                  LayoutAlgorithm<String> layoutAlgorithm = layoutType.getLayoutAlgorithm();

                  vv.removePreRenderPaintable(balloonLayoutRings);
                  vv.removePreRenderPaintable(radialLayoutRings);

//                  if (layoutAlgorithm instanceof EdgeSorting) {
//
//                    ((EdgeSorting) layoutAlgorithm)
//                        .setEdgeComparator(
//                            new EdgeComparator(
//                                List.of(
//                                    "Fall-Through",
//                                    "Entry",
//                                    "Conditional-Call",
//                                    "Unconditional-Call",
//                                    "Computed",
//                                    "Indirection",
//                                    "Unconditional-Jump",
//                                    "Terminator",
//                                    "Conditional-Return")));
//                  }
//                  if (layoutAlgorithm instanceof NormalizesFavoredEdge) {
//                    ((NormalizesFavoredEdge<AI>) layoutAlgorithm)
//                        .setFavoredEdgePredicate(favoredEdgePredicate);
//                  }
//                  if (layoutAlgorithm instanceof EdgePredicated) {
//                    ((EdgePredicated<AI>) layoutAlgorithm)
//                        .setEdgePredicate(new EdgePredicate(graph));
//                  }
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm,
                            new LeftToRight(vv.getVisualizationModel().getLayoutModel()));
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm,
                            new LeftToRight(vv.getVisualizationModel().getLayoutModel()));
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

    layoutComboBox.setSelectedItem(LayoutHelperDirectedGraphs.Layouts.FR_BH_VISITOR);

    // create a lens to share between the two hyperbolic transformers
    Lens lens = new Lens(); /* provides a Hyperbolic lens for the view */
    LensSupport<DefaultLensGraphMouse> hyperbolicViewSupport =
        ViewLensSupport.<String, Integer, DefaultLensGraphMouse>builder(vv)
            .lensTransformer(
                HyperbolicShapeTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.VIEW))
                    .build())
            .lensGraphMouse(
                DefaultLensGraphMouse.builder()
                    .magnificationFloor(0.4f)
                    .magnificationCeiling(1.0f)
                    .magnificationDelta(0.05f)
                    .build())
            .build();

    LensSupport<DefaultLensGraphMouse> hyperbolicLayoutSupport =
        LayoutLensSupport.<String, Integer, DefaultLensGraphMouse>builder(vv)
            .lensTransformer(
                HyperbolicTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.LAYOUT))
                    .build())
            .lensGraphMouse(
                DefaultLensGraphMouse.builder()
                    .magnificationFloor(0.4f)
                    .magnificationCeiling(1.0f)
                    .magnificationDelta(0.05f)
                    .build())
            .build();

    // the magnification lens uses a different magnification than the hyperbolic lens
    // create a new one to share between the two magnigy transformers
    lens = new Lens();
    lens.setMagnification(3.f);
    LensSupport<DefaultLensGraphMouse> magnifyViewSupport =
        ViewLensSupport.<String, Integer, DefaultLensGraphMouse>builder(vv)
            .lensTransformer(
                MagnifyShapeTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.VIEW))
                    .build())
            .lensGraphMouse( // override with range for magnificaion
                DefaultLensGraphMouse.builder()
                    .magnificationFloor(1.0f)
                    .magnificationCeiling(4.0f)
                    .build())
            .build();

    LensSupport<DefaultLensGraphMouse> magnifyLayoutSupport =
        LayoutLensSupport.<String, Integer, DefaultLensGraphMouse>builder(vv)
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

    JButton pageRankButton = new JButton("Page Rank");
    pageRankButton.addActionListener(event -> computeScores(new PageRank(graph)));
    JButton betweennessButton = new JButton("Betweenness");
    betweennessButton.addActionListener(event -> computeScores(new BetweennessCentrality<>(graph)));
    JButton closenessButton = new JButton("Closeness");
    closenessButton.addActionListener(event -> computeScores(new ClosenessCentrality<>(graph)));
    JButton clusteringButton = new JButton("Clustering");
    clusteringButton.addActionListener(event -> computeScores(new ClusteringCoefficient<>(graph)));
    JButton harmonicButton = new JButton("Harmonic");
    harmonicButton.addActionListener(event -> computeScores(new HarmonicCentrality<>(graph)));
    JButton noScores = new JButton("None");

    JPanel scoringGrid = new JPanel(new GridLayout(0, 2));
    scoringGrid.add(pageRankButton);
    scoringGrid.add(betweennessButton);
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

  /**
   * these are vertex or edge types that have defined colors (the keys are the property values for
   * the vertex/edge keys: VertexType and EdgeType)
   */
  public static Map<String, Paint> VERTEX_TYPE_TO_COLOR_MAP =
      Map.ofEntries(
          entry("Body", Color.blue),
          entry("Entry", Colors.WEB_COLOR_MAP.get("DarkOrange")),
          entry("Exit", Color.magenta),
          entry("Switch", Color.cyan),
          entry("Bad", Color.red),
          entry("Entry-Nexus", Color.white),
          entry("External", Color.green),
          entry("Folder", Colors.WEB_COLOR_MAP.get("DarkOrange")),
          entry("Fragment", Colors.WEB_COLOR_MAP.get("Purple")),
          entry("Data", Color.pink));

  /**
   * these are vertex or edge types that have defined colors (the keys are the property values for
   * the vertex/edge keys: VertexType and EdgeType)
   */
  public static Map<String, Paint> EDGE_TYPE_TO_COLOR_MAP =
      Map.ofEntries(
          entry("Entry", Color.gray), // white??
          entry("Fall-Through", Color.blue),
          entry("Conditional-Call", Colors.WEB_COLOR_MAP.get("DarkOrange")),
          entry("Unconditional-Call", Colors.WEB_COLOR_MAP.get("DarkOrange")),
          entry("Computed", Color.cyan),
          entry("Indirection", Color.pink),
          entry("Unconditional-Jump", Color.green),
          entry("Conditional-Jump", Color.yellow.darker()),
          entry("Terminator", Colors.WEB_COLOR_MAP.get("Purple")),
          entry("Conditional-Return", Colors.WEB_COLOR_MAP.get("Purple")));

  public static Paint getColor(Map<String, String> map) {
    //        Map<String, String> map = attributed.getAttributeMap();
    // if there is a 'VertexType' attribute key, use its value to choose a predefined color
    if (map.containsKey("VertexType")) {
      String typeValue = map.get("VertexType");
      return VERTEX_TYPE_TO_COLOR_MAP.getOrDefault(typeValue, Color.blue);
    }
    // if there is an 'EdgeType' attribute key, use its value to choose a predefined color
    if (map.containsKey("EdgeType")) {
      String typeValue = map.get("EdgeType");
      return EDGE_TYPE_TO_COLOR_MAP.getOrDefault(typeValue, Color.green);
    }
    // if there is a 'Color' attribute key, use its value (either a color name or an RGB hex string)
    // to choose a color
    if (map.containsKey("Color")) {
      String colorName = map.get("Color");
      if (Colors.WEB_COLOR_MAP.containsKey(colorName)) {
        return Colors.WEB_COLOR_MAP.get(colorName);
      }
      // if the value matches an RGB hex string, turn that into a color
      Color c = Colors.getHexColor(colorName);
      if (c != null) {
        return c;
      }
    }
    // default value when nothing else matches
    return Color.green;
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

  private void populateGraph(Graph<String, Integer> graph) {
    graph.addVertex("PFX");
    graph.addVertex("CMP");
    graph.addVertex("EQ3");
    graph.addVertex("FX1");
    graph.addVertex("DIV1");
    graph.addVertex("DS1");
    graph.addVertex("DS2");
    graph.addVertex("SEND/RET 1");
    graph.addVertex("SEND/RET 2");
    graph.addVertex("AMP1");
    graph.addVertex("AMP2");
    graph.addVertex("NS1");
    graph.addVertex("NS2");
    graph.addVertex("EQ1");
    graph.addVertex("EQ2");
    graph.addVertex("MIX1");
    graph.addVertex("FVOL");
    graph.addVertex("DIV2");
    graph.addVertex("MIX2");
    graph.addVertex("DIV3");
    graph.addVertex("EQ4");
    graph.addVertex("DLY4");
    graph.addVertex("FX2");
    graph.addVertex("FX3");
    graph.addVertex("FX4");
    graph.addVertex("DLY3");
    graph.addVertex("MIX3");
    graph.addVertex("DLY1");
    graph.addVertex("DLY2");
    graph.addVertex("MDLY");
    graph.addVertex("CHO");
    graph.addVertex("SINK");

    graph.addEdge("PFX", "CMP");
    graph.addEdge("CMP", "EQ3");
    graph.addEdge("EQ3", "FX1");
    graph.addEdge("FX1", "DIV1");
    graph.addEdge("DIV1", "DS1");
    graph.addEdge("DIV1", "DS2");

    graph.addEdge("DS1", "SEND/RET 1");
    graph.addEdge("SEND/RET 1", "AMP1");
    graph.addEdge("AMP1", "NS1");
    graph.addEdge("NS1", "EQ1");
    graph.addEdge("EQ1", "MIX1");


    graph.addEdge("DS2", "SEND/RET 2");
    graph.addEdge("SEND/RET 2", "AMP2");
    graph.addEdge("AMP2", "NS2");
    graph.addEdge("NS2", "EQ2");
    graph.addEdge("EQ2", "MIX1");

    graph.addEdge("MIX1", "FVOL");
    graph.addEdge("FVOL", "DIV2");
    graph.addEdge("DIV2", "MIX2");
    graph.addEdge("DIV2", "DIV3");
    graph.addEdge("DIV3", "EQ4");
    graph.addEdge("EQ4", "FX2");
    graph.addEdge("FX2", "FX3");
    graph.addEdge("FX3", "FX4");
    graph.addEdge("FX4", "DLY3");
    graph.addEdge("DLY3", "MIX3");
    graph.addEdge("DIV3", "DLY4");
    graph.addEdge("DLY4", "MIX3");
    graph.addEdge("MIX3", "MIX2");
    graph.addEdge("MIX2", "DLY1");
    graph.addEdge("DLY1", "DLY2");
    graph.addEdge("DLY2", "MDLY");
    graph.addEdge("MDLY", "CHO");
    graph.addEdge("CHO", "SINK");
  }

  public static void main(String[] args) {
    new ShowLayoutsWithIO7MGraph();
  }
}
