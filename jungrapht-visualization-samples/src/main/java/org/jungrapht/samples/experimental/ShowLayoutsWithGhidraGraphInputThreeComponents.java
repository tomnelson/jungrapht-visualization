package org.jungrapht.samples.experimental;

import static java.util.Map.entry;
import static org.jungrapht.visualization.util.Attributed.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.alg.scoring.*;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.spatial.RTreeVisualization;
import org.jungrapht.samples.util.ASAILoader;
import org.jungrapht.samples.util.Colors;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LensControlHelper;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.control.DefaultLensGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.*;
import org.jungrapht.visualization.transform.*;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.MagnifyShapeTransformer;
import org.jungrapht.visualization.transform.shape.ViewLensSupport;
import org.jungrapht.visualization.util.LayoutAlgorithmTransition;
import org.jungrapht.visualization.util.LayoutPaintable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class ShowLayoutsWithGhidraGraphInputThreeComponents extends JFrame {

  private static final Logger log =
      LoggerFactory.getLogger(ShowLayoutsWithGhidraGraphInputThreeComponents.class);

  LayoutPaintable.BalloonRings balloonLayoutRings;
  LayoutPaintable.RadialRings radialLayoutRings;
  LayoutPaintable.LayoutBounds layoutBounds;
  VisualizationViewer<AS, AI> vv;
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

  Function<AS, Paint> vertexFillPaintFunction = Colors::getColor;

  Function<AI, Paint> edgeDrawPaintFunction = Colors::getColor;

  static class EdgeSupplier implements Supplier<AI> {
    int counter = 0;

    @Override
    public AI get() {
      return new AI(counter++);
    }
  }

  public ShowLayoutsWithGhidraGraphInputThreeComponents() {

    Graph<AS, AI> graph =
        GraphTypeBuilder.<AS, AI>directed()
            .allowingSelfLoops(true)
            .allowingMultipleEdges(true)
            .edgeSupplier(new EdgeSupplier())
            .buildGraph();
    JPanel container = new JPanel(new BorderLayout());

    final DefaultGraphMouse<AS, AI> graphMouse = new DefaultGraphMouse<>();

    vv =
        VisualizationViewer.<AS, AI>builder(graph)
            .layoutSize(new Dimension(1800, 1800))
            .viewSize(new Dimension(900, 900))
            .graphMouse(graphMouse)
            .build();

    //    loadGraphFile(graph);

    //add a second component
    AS v1 = new AS("" + graph.vertexSet().size() + 1);
    graph.addVertex(v1);
    AS v2 = new AS("" + graph.vertexSet().size() + 1);
    graph.addVertex(v2);
    AI ed = graph.addEdge(v1, v2);
    ed.set("EdgeType", "Fall-Through");

    //add a third component
    AS v3 = new AS("" + graph.vertexSet().size() + 1);
    graph.addVertex(v3);
    AS v4 = new AS("" + graph.vertexSet().size() + 1);
    graph.addVertex(v4);
    AS v5 = new AS("" + graph.vertexSet().size() + 1);
    graph.addVertex(v5);
    AS v6 = new AS("" + graph.vertexSet().size() + 1);
    graph.addVertex(v6);
    AI ed1 = graph.addEdge(v3, v4);
    ed1.set("EdgeType", "Fall-Through");
    AI ed2 = graph.addEdge(v3, v5);
    ed2.set("EdgeType", "Unconditional-Jump");
    AI ed3 = graph.addEdge(v4, v6);
    ed3.set("EdgeType", "Fall-Through");
    AI ed4 = graph.addEdge(v3, v6);
    ed4.set("EdgeType", "Conditional-Jump");

    loadGraphFile(graph);

    vv.setVertexToolTipFunction(vertex -> vertex.toHtml());
    vv.getRenderContext().setVertexLabelFunction(vertex -> vertex.get("ID"));
    vv.setEdgeToolTipFunction(Object::toString);
    Function<AS, Stroke> vertexStrokeFunction =
        v ->
            vv.getSelectedVertexState().isSelected(v) ? new BasicStroke(8.f) : new BasicStroke(2.f);
    vv.getRenderContext().setVertexStrokeFunction(vertexStrokeFunction);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.scaleToLayout();

    container.add(vv.getComponent(), BorderLayout.CENTER);
    LayoutHelperEiglsperger.Layouts[] combos = LayoutHelperEiglsperger.getCombos();
    final JToggleButton animateLayoutTransition = new JToggleButton("Animate Layout Transition");

    final JComboBox layoutComboBox = new JComboBox(combos);
    layoutComboBox.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  LayoutHelperEiglsperger.Layouts layoutType =
                      (LayoutHelperEiglsperger.Layouts) layoutComboBox.getSelectedItem();
                  LayoutAlgorithm<AS> layoutAlgorithm = layoutType.getLayoutAlgorithm();
                  vv.removePreRenderPaintable(balloonLayoutRings);
                  vv.removePreRenderPaintable(radialLayoutRings);

                  if (layoutAlgorithm instanceof EdgeSorting) {

                    ((EdgeSorting) layoutAlgorithm)
                        .setEdgeComparator(
                            new EdgeComparator(
                                List.of(
                                    "Fall-Through",
                                    "Entry",
                                    "Conditional-Call",
                                    "Unconditional-Call",
                                    "Computed",
                                    "Indirection",
                                    "Unconditional-Jump",
                                    "Terminator",
                                    "Conditional-Return")));
                  }
                  if (layoutAlgorithm instanceof EdgePredicated) {
                    ((EdgePredicated<AI>) layoutAlgorithm)
                        .setEdgePredicate(new EdgePredicate(graph));
                  }
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

    layoutComboBox.setSelectedItem(LayoutHelperEiglsperger.Layouts.EIGLSPERGERTD);

    // create a lens to share between the two hyperbolic transformers
    Lens lens = new Lens(); /* provides a Hyperbolic lens for the view */
    LensSupport<DefaultLensGraphMouse> hyperbolicViewSupport =
        ViewLensSupport.<AS, AI, DefaultLensGraphMouse>builder(vv)
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
        LayoutLensSupport.<AS, AI, DefaultLensGraphMouse>builder(vv)
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
        ViewLensSupport.<AS, AI, DefaultLensGraphMouse>builder(vv)
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
        LayoutLensSupport.<AS, AI, DefaultLensGraphMouse>builder(vv)
            .lensTransformer(
                MagnifyTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.LAYOUT))
                    .build())
            .lensGraphMouse( // override with range for magnificaion
                DefaultLensGraphMouse.builder()
                    .magnificationFloor(1.0f)
                    .magnificationCeiling(4.0f)
                    .build())
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

  private void loadGraphFile(Graph<AS, AI> graph) {
    String fileName = "ghidra.json";
    if (ASAILoader.load("ghidra.json", graph)) {
      vv.getRenderContext().setVertexFillPaintFunction(vertexFillPaintFunction);
      vv.getRenderContext().setEdgeDrawPaintFunction(edgeDrawPaintFunction);
      vv.getRenderContext().setArrowFillPaintFunction(edgeDrawPaintFunction);
      vv.getRenderContext().setArrowDrawPaintFunction(edgeDrawPaintFunction);
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
  }

  private void computeScores(VertexScoringAlgorithm<AS, Double> scoring) {
    Map<AS, Double> scores = scoring.getScores();
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

  class EdgePredicate implements Predicate<AI> {
    Graph<AS, AI> graph;

    EdgePredicate(Graph<AS, AI> graph) {
      this.graph = graph;
    }

    @Override
    public boolean test(AI edge) {
      //      Map<String, Attribute> map = edgeAttributes.get(AI);
      if ("Fall-Through".equals(edge.get("EdgeType"))) {
        return true;
      }
      AS target = graph.getEdgeTarget(edge);
      Collection<AI> incomingEdges = graph.incomingEdgesOf(target);
      for (AI e : incomingEdges) {
        //        Map<String, Attribute> map2 = edgeAttributes.get(e);
        if ("Fall-Through".equals(edge.get("EdgeType"))) {
          return false;
        }
      }
      return true;
    }
  }

  static class EdgeComparator implements Comparator<AI> {
    private List<String> edgePriorityList;

    public EdgeComparator(List<String> edgePriorityList) {
      this.edgePriorityList = edgePriorityList;
    }

    @Override
    public int compare(AI o1, AI o2) {
      return priority(o1).compareTo(priority(o2));
    }

    Integer priority(AI e) {
      String edgeType = e.getOrDefault("EdgeType", "");
      int index = edgePriorityList.indexOf(edgeType);
      if (index != -1) {
        return edgePriorityList.indexOf(e.get("EdgeType"));
      }
      return 0;
    }
  }

  static class VertexComparator implements Comparator<AS> {
    private Graph<AS, AI> graph;

    public VertexComparator(Graph<AS, AI> graph) {
      this.graph = graph;
      //      this.edgeAttributes = edgeAttributes;
    }

    @Override
    public int compare(AS v1, AS v2) {
      boolean v1IsSpecial = false;
      for (AI edge : graph.incomingEdgesOf(v1)) {
        if ("Fall-Through".equals(edge.get("EdgeType"))) {
          v1IsSpecial = true;
          break;
        }
      }
      for (AI edge : graph.outgoingEdgesOf(v1)) {
        if ("Fall-Through".equals(edge.get("EdgeType"))) {
          v1IsSpecial = true;
          break;
        }
      }
      boolean v2IsSpecial = false;
      for (AI edge : graph.incomingEdgesOf(v2)) {
        if ("Fall-Through".equals(edge.get("EdgeType"))) {
          v2IsSpecial = true;
          break;
        }
      }
      for (AI edge : graph.outgoingEdgesOf(v1)) {
        if ("Fall-Through".equals(edge.get("EdgeType"))) {
          v2IsSpecial = true;
          break;
        }
      }
      if (v1IsSpecial && v2IsSpecial) {
        return 0;
      }
      if (v1IsSpecial) {
        return 1;
      }
      if (v2IsSpecial) {
        return -1;
      }
      return 0;
    }
  }

  public static void main(String[] args) {
    new ShowLayoutsWithGhidraGraphInputThreeComponents();
  }
}
