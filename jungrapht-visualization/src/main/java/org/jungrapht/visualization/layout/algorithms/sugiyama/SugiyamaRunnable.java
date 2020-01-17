package org.jungrapht.visualization.layout.algorithms.sugiyama;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.brandeskopf.HorizontalCoordinateAssignment;
import org.jungrapht.visualization.layout.algorithms.brandeskopf.Unaligned;
import org.jungrapht.visualization.layout.algorithms.util.InsertionSortCounter;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code Runnable} part of the {@link SugiyamaLayoutAlgorithm} The Sugiyama Hierarchical
 * Minimum-Cross layout algorithm
 *
 * @see "Methods for Visual Understanding Hierarchical System Structures. KOZO SUGIYAMA, MEMBER,
 *     IEEE, SHOJIRO TAGAWA, AND MITSUHIKO TODA, MEMBER, IEEE"
 * @see "An E log E Line Crossing Algorithm for Levelled Graphs. Vance Waddle and Ashok Malhotra IBM
 *     Thomas J. Watson Research Center"
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz, Box D 188, 78457 Konstanz,
 *     Germany"
 * @param <V> vertex type
 * @param <E> edge type
 */
public class SugiyamaRunnable<V, E> implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(SugiyamaRunnable.class);

  /**
   * a Builder to create a configured instance
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  public static class Builder<
      V, E, T extends SugiyamaRunnable<V, E>, B extends Builder<V, E, T, B>> {
    protected LayoutModel<V> layoutModel;
    protected RenderContext<V, E> renderContext;
    protected Predicate<V> vertexPredicate; // can be null
    protected Predicate<E> edgePredicate; // can be null
    protected Comparator<V> vertexComparator = (v1, v2) -> 0;
    protected Comparator<E> edgeComparator = (e1, e2) -> 0;
    protected boolean straightenEdges;
    protected boolean postStraighten;

    /** {@inheritDoc} */
    protected B self() {
      return (B) this;
    }

    /**
     * @param vertexPredicate {@link Predicate} to apply to vertices
     * @return this Builder
     */
    public B vertexPredicate(Predicate<V> vertexPredicate) {
      this.vertexPredicate = vertexPredicate;
      return self();
    }

    /**
     * @param edgePredicate {@link Predicate} to apply to edges
     * @return this Builder
     */
    public B edgePredicate(Predicate<E> edgePredicate) {
      this.edgePredicate = edgePredicate;
      return self();
    }

    /**
     * @param vertexComparator {@link Comparator} to sort vertices
     * @return this Builder
     */
    public B vertexComparator(Comparator<V> vertexComparator) {
      this.vertexComparator = vertexComparator;
      return self();
    }

    /**
     * @param edgeComparator {@link Comparator} to sort edges
     * @return this Builder
     */
    public B edgeComparator(Comparator<E> edgeComparator) {
      this.edgeComparator = edgeComparator;
      return self();
    }

    public B layoutModel(LayoutModel<V> layoutModel) {
      this.layoutModel = layoutModel;
      return self();
    }

    public B renderContext(RenderContext<V, E> renderContext) {
      this.renderContext = renderContext;
      return self();
    }

    public B straightenEdges(boolean straightenEdges) {
      this.straightenEdges = straightenEdges;
      return self();
    }

    public B postStraighten(boolean postStraighten) {
      this.postStraighten = postStraighten;
      return self();
    }

    /** {@inheritDoc} */
    public T build() {
      return (T) new SugiyamaRunnable<>(this);
    }
  }

  /**
   * @param <V> vertex type
   * @param <E> edge type
   * @return a Builder ready to configure
   */
  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  final LayoutModel<V> layoutModel;
  final RenderContext<V, E> renderContext;
  Graph<V, E> graph;
  Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph;
  boolean stopit = false;
  protected Predicate<V> vertexPredicate;
  protected Predicate<E> edgePredicate;
  protected Comparator<V> vertexComparator;
  protected Comparator<E> edgeComparator;
  protected boolean straightenEdges;
  protected boolean postStraighten;

  private SugiyamaRunnable(Builder<V, E, ?, ?> builder) {
    this(
        builder.layoutModel,
        builder.renderContext,
        builder.vertexPredicate,
        builder.edgePredicate,
        builder.vertexComparator,
        builder.edgeComparator,
        builder.straightenEdges,
        builder.postStraighten);
  }

  private SugiyamaRunnable(
      LayoutModel<V> layoutModel,
      RenderContext<V, E> renderContext,
      Predicate<V> vertexPredicate,
      Predicate<E> edgePredicate,
      Comparator<V> vertexComparator,
      Comparator<E> edgeComparator,
      boolean straightenEdges,
      boolean postStraighten) {
    this.layoutModel = layoutModel;
    this.renderContext = renderContext;
    this.vertexComparator = vertexComparator;
    this.vertexPredicate = vertexPredicate;
    this.edgeComparator = edgeComparator;
    this.edgePredicate = edgePredicate;
    this.straightenEdges = straightenEdges;
    this.postStraighten = postStraighten;
  }

  private boolean checkStopped() {
    try {
      Thread.sleep(1);
      if (stopit) {
        return true;
      }
    } catch (InterruptedException ex) {
    }
    return false;
  }

  @Override
  public void run() {
    this.graph = layoutModel.getGraph();

    long startTime = System.currentTimeMillis();
    SugiyamaTransformedGraphSupplier<V, E> transformedGraphSupplier =
        new SugiyamaTransformedGraphSupplier(graph);
    this.svGraph = transformedGraphSupplier.get();
    long transformTime = System.currentTimeMillis();
    log.trace("transform Graph took {}", (transformTime - startTime));

    if (checkStopped()) {
      return;
    }
    GreedyCycleRemoval<SugiyamaVertex<V>, SugiyamaEdge<V, E>> greedyCycleRemoval =
        new GreedyCycleRemoval(svGraph);
    Collection<SugiyamaEdge<V, E>> feedbackArcs = greedyCycleRemoval.getFeedbackArcs();

    // reverse the direction of feedback arcs so that they no longer introduce cycles in the graph
    // the feedback arcs will be processed later to draw with the correct direction and correct articulation points
    for (SugiyamaEdge<V, E> se : feedbackArcs) {
      svGraph.removeEdge(se);
      SugiyamaEdge<V, E> newEdge = SugiyamaEdge.of(se.edge, se.target, se.source);
      svGraph.addEdge(newEdge.source, newEdge.target, newEdge);
    }
    long cycles = System.currentTimeMillis();
    log.trace("remove cycles took {}", (cycles - transformTime));

    List<List<SugiyamaVertex<V>>> layers = GraphLayers.assign(svGraph);
    long assignLayersTime = System.currentTimeMillis();
    log.trace("assign layers took {} ", (assignLayersTime - cycles));

    GraphLayers.checkLayers(layers);

    if (checkStopped()) {
      return;
    }

    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
    List<SugiyamaEdge<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    SugiyamaVertex<V>[][] layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);
    GraphLayers.checkLayers(layersArray);

    if (checkStopped()) {
      return;
    }

    // save off a map of edge lists keyed on the source vertex
    Map<Integer, List<SugiyamaEdge<V, E>>> edgesKeyedOnSource = new LinkedHashMap<>();
    edges.forEach(
        e -> {
          int sourceRank = e.source.rank;
          if (edgesKeyedOnSource.containsKey(sourceRank)) {
            edgesKeyedOnSource.get(sourceRank).add(e);
          } else {
            ArrayList<SugiyamaEdge<V, E>> list = new ArrayList<>();
            list.add(e);
            edgesKeyedOnSource.put(sourceRank, list);
          }
        });

    long syntheticsTime = System.currentTimeMillis();
    log.trace("synthetics took {}", (syntheticsTime - assignLayersTime));

    SugiyamaVertex<V>[][] best = null;
    int lowestCrossCount = Integer.MAX_VALUE;
    int maxLevelCross = Integer.getInteger(PREFIX + "sugiyama.max.level.cross", 23);
    // order the ranks
    for (int i = 0; i < maxLevelCross; i++) {
      median(layersArray, i, svGraph);
      transpose(layersArray, edges, edgesKeyedOnSource);
      AllLevelCross<V, E> allLevelCross = new AllLevelCross<>(svGraph, layersArray);
      int allLevelCrossCount = allLevelCross.allLevelCross();
      if (allLevelCrossCount < lowestCrossCount) {
        GraphLayers.checkLayers(layersArray);

        best = copy(layersArray);
        GraphLayers.checkLayers(best);
        lowestCrossCount = allLevelCrossCount;
      }
      if (checkStopped()) {
        return;
      }
    }

    // in case zero iterations of cross counting were requested:
    if (best == null) {
      best = layersArray;
    }

    long crossCountTests = System.currentTimeMillis();
    log.trace("cross counts took {}", (crossCountTests - syntheticsTime));
    GraphLayers.checkLayers(best);

    // done optimizing for edge crossing

    // figure out the avg size of rendered vertex
    Rectangle avgVertexBounds = avgVertexBounds(best, renderContext.getVertexShapeFunction());

    int horizontalOffset =
        Math.max(
            avgVertexBounds.width / 2,
            Integer.getInteger(PREFIX + "sugiyama.horizontal.offset", 50));
    int verticalOffset =
        Math.max(
            avgVertexBounds.height / 2,
            Integer.getInteger(PREFIX + "sugiyama.vertical.offset", 50));
    GraphLayers.checkLayers(best);
    Map<SugiyamaVertex<V>, Point> vertexPointMap = new HashMap<>();

    if (straightenEdges) {
      HorizontalCoordinateAssignment.horizontalCoordinateAssignment(
          best, svGraph, new HashSet<>(), horizontalOffset, verticalOffset);

      GraphLayers.checkLayers(best);

      for (int i = 0; i < best.length; i++) {
        for (int j = 0; j < best[i].length; j++) {
          SugiyamaVertex<V> sugiyamaVertex = best[i][j];
          vertexPointMap.put(sugiyamaVertex, sugiyamaVertex.getPoint());
        }
      }

    } else {
      Unaligned.setPoints(
          best,
          renderContext.getVertexShapeFunction(),
          horizontalOffset,
          verticalOffset,
          vertexPointMap);
    }

    Map<Integer, Integer> rowWidthMap = new HashMap<>(); // all the row widths
    Map<Integer, Integer> rowMaxHeightMap = new HashMap<>(); // all the row heights
    int layerIndex = 0;
    Function<V, Shape> vertexShapeFunction = renderContext.getVertexShapeFunction();
    int totalHeight = 0;
    int totalWidth = 0;
    for (int i = 0; i < best.length; i++) {

      int width = horizontalOffset;
      int maxHeight = 0;
      for (int j = 0; j < best[i].length; j++) {
        SugiyamaVertex<V> sugiyamaVertex = best[i][j];
        if (!(sugiyamaVertex instanceof SyntheticSugiyamaVertex)) {
          Rectangle bounds = vertexShapeFunction.apply(sugiyamaVertex.vertex).getBounds();
          width += bounds.width + horizontalOffset;
          maxHeight = Math.max(maxHeight, bounds.height);
        } else {
          width += horizontalOffset;
        }
      }
      rowWidthMap.put(layerIndex, width);
      rowMaxHeightMap.put(layerIndex, maxHeight);
      layerIndex++;
    }

    int widestRowWidth = rowWidthMap.values().stream().mapToInt(v -> v).max().getAsInt();
    int x = horizontalOffset;
    int y = verticalOffset;
    layerIndex = 0;
    log.trace("layerMaxHeights {}", rowMaxHeightMap);
    for (int i = 0; i < best.length; i++) {
      int previousVertexWidth = 0;
      // offset against widest row
      x += (widestRowWidth - rowWidthMap.get(layerIndex)) / 2;

      y += rowMaxHeightMap.get(layerIndex) / 2;
      if (layerIndex > 0) {
        y += rowMaxHeightMap.get(layerIndex - 1) / 2;
      }

      int rowWidth = 0;
      for (int j = 0; j < best[i].length; j++) {
        SugiyamaVertex<V> sugiyamaVertex = best[i][j];
        int vertexWidth = 0;
        if (!(sugiyamaVertex instanceof SyntheticSugiyamaVertex)) {
          vertexWidth = vertexShapeFunction.apply(sugiyamaVertex.vertex).getBounds().width;
        }

        x += previousVertexWidth / 2 + vertexWidth / 2 + horizontalOffset;

        rowWidth = x + vertexWidth / 2;
        log.trace("layerIndex {} y is {}", layerIndex, y);
        previousVertexWidth = vertexWidth;
      }
      totalWidth = Math.max(totalWidth, rowWidth);
      x = horizontalOffset;
      y += verticalOffset;
      totalHeight = y + rowMaxHeightMap.get(layerIndex) / 2;
      layerIndex++;
    }

    layoutModel.setSize(totalWidth + horizontalOffset, totalHeight);
    long pointsSetTime = System.currentTimeMillis();
    log.trace("setting points took {}", (pointsSetTime - crossCountTests));

    // now all the vertices in layers (best) have points associated with them
    // every vertex in vertexMap has a point value

    svGraph.vertexSet().forEach(v -> v.setPoint(vertexPointMap.get(v)));

    if (postStraighten) synthetics.alignArticulatedEdges();
    List<ArticulatedEdge<V, E>> articulatedEdges = synthetics.makeArticulatedEdges();

    Set<E> feedbackEdges = new HashSet<>();
    feedbackArcs.forEach(a -> feedbackEdges.add(a.edge));
    articulatedEdges
        .stream()
        .filter(ae -> feedbackEdges.contains(ae.edge))
        .forEach(
            ae -> {
              svGraph.removeEdge(ae);
              SugiyamaEdge<V, E> reversed = ae.reversed();
              svGraph.addEdge(reversed.source, reversed.target, reversed);
            });

    Map<E, List<Point>> edgePointMap = new HashMap<>();
    for (ArticulatedEdge<V, E> ae : articulatedEdges) {
      List<Point> points = new ArrayList<>();
      if (feedbackEdges.contains(ae.edge)) {
        points.add(ae.target.getPoint());
        points.addAll(ae.reversed().getIntermediatePoints());
        points.add(ae.source.getPoint());
      } else {
        points.add(ae.source.getPoint());
        points.addAll(ae.getIntermediatePoints());
        points.add(ae.target.getPoint());
      }

      edgePointMap.put(ae.edge, points);
    }
    EdgeShape.ArticulatedLine<V, E> edgeShape = new EdgeShape.ArticulatedLine<>();
    edgeShape.setEdgeArticulationFunction(
        e -> edgePointMap.getOrDefault(e, Collections.emptyList()));

    renderContext.setEdgeShapeFunction(edgeShape);

    long articulatedEdgeTime = System.currentTimeMillis();
    log.trace("articulated edges took {}", (articulatedEdgeTime - pointsSetTime));

    svGraph.vertexSet().forEach(v -> layoutModel.set(v.vertex, v.getPoint()));
  }

  private void transpose(
      SugiyamaVertex<V>[][] ranks,
      List<SugiyamaEdge<V, E>> edges,
      Map<Integer, List<SugiyamaEdge<V, E>>> reducedEdgeMap) {
    GraphLayers.checkLayers(ranks);

    boolean improved = true;
    int improvements = 0;
    int lastImprovements = 0;
    int sanityLimit = Integer.getInteger(PREFIX + "sugiyama.transpose.limit", 10);
    int sanityCheck = 0;
    while (improved) {
      improvements = 0;
      improved = false;
      for (int i = 0; i < ranks.length; i++) {
        SugiyamaVertex<V>[] rank = ranks[i];
        for (int j = 0; j <= rank.length - 2; j++) {
          SugiyamaVertex<V> v = rank[j];
          SugiyamaVertex<V> w = rank[j + 1];
          int vw = crossing(v, w, reducedEdgeMap.getOrDefault(i, Collections.emptyList()));
          int wv = crossing(w, v, reducedEdgeMap.getOrDefault(i, Collections.emptyList()));
          if (vw > wv) {
            improved = true;
            improvements++;
            swap(rank, j, j + 1);
            // change the indices of the swapped vertices!!
            v.setIndex(j + 1);
            w.setIndex(j);
          }
        }
      }
      sanityCheck++;
      if (sanityCheck > sanityLimit) break;
      if (improvements == lastImprovements) break;
      lastImprovements = improvements;
    }
    GraphLayers.checkLayers(ranks);
  }

  private <T> void swap(T[] array, int i, int j) {
    T temp = array[i];
    array[i] = array[j];
    array[j] = temp;
  }

  int crossing(SugiyamaVertex<V> v, SugiyamaVertex<V> w, List<SugiyamaEdge<V, E>> reducedEdges) {

    List<Integer> targetIndices = new ArrayList<>();
    List<SugiyamaEdge<V, E>> considered = new ArrayList<>();
    for (SugiyamaEdge<V, E> edge : reducedEdges) {
      if (edge.source.equals(v) || edge.source.equals(w)) {
        targetIndices.add(edge.target.getIndex());
        considered.add(edge);
      }
    }
    //    log.info("passed in {} edges and {} and {}", edges.size(), reducedEdges.size(), reducedEdges);
    //    log.info("considered {} edges {}", considered.size(), considered);

    return InsertionSortCounter.insertionSortCounter(targetIndices);
  }

  //http://www.graphviz.org/Documentation/TSE93.pdf p 15
  void median(
      SugiyamaVertex<V>[][] layers, int i, Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph) {

    if (i % 2 == 0) {
      for (int r = 0; r < layers.length; r++) {
        Map<SugiyamaVertex<V>, Double> medianMap = new HashMap<>();
        for (SugiyamaVertex<V> v : layers[r]) {
          double median = medianValue(v, this::upperNeighborIndices, layers, svGraph);
          medianMap.put(v, median);
        }
        medianSortAndFixMetadata(layers[r], medianMap);
        GraphLayers.checkLayers(layers);
      }
    } else {
      for (int r = layers.length - 1; r >= 0; r--) {
        Map<SugiyamaVertex<V>, Double> medianMap = new HashMap<>();
        for (SugiyamaVertex<V> v : layers[r]) {
          double median = medianValue(v, this::lowerNeighborIndices, layers, svGraph);
          medianMap.put(v, median);
        }
        medianSortAndFixMetadata(layers[r], medianMap);
        GraphLayers.checkLayers(layers);
      }
    }
  }

  void medianSortAndFixMetadata(
      SugiyamaVertex<V>[] layer, Map<SugiyamaVertex<V>, Double> medianMap) {
    //    Arrays.sort(layer,  (v1, v2) -> medianMap.get(v1).compareTo(medianMap.get(v2)));
    Arrays.sort(
        layer,
        (v1, v2) -> {
          //          if (medianMap.get(v1) == -1 || medianMap.get(v2) == -1) {
          //            return 0; // don't exchange
          //          } else {
          return medianMap.get(v1).compareTo(medianMap.get(v2));
          //          }
        });
    // fix up the metadata!
    fixMetadata(layer);
  }

  private void fixMetadata(SugiyamaVertex<V>[] layer) {
    for (int idx = 0; idx < layer.length; idx++) {
      layer[idx].setIndex(idx);
    }
  }

  int[] upperNeighborIndices(
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph, SugiyamaVertex<V> vertex) {
    return Graphs.predecessorListOf(graph, vertex)
        .stream()
        .mapToInt(SugiyamaVertex::getIndex)
        .sorted()
        .toArray();
  }

  int[] lowerNeighborIndices(
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph, SugiyamaVertex<V> vertex) {
    return Graphs.successorListOf(graph, vertex)
        .stream()
        .mapToInt(SugiyamaVertex::getIndex)
        .sorted()
        .toArray();
  }

  int[] adjPosition(
      SugiyamaVertex<V> v,
      BiFunction<Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>>, SugiyamaVertex<V>, int[]>
          neighborFunction,
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph) {
    return neighborFunction.apply(svGraph, v);
  }

  double medianValue(
      SugiyamaVertex<V> v,
      BiFunction<Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>>, SugiyamaVertex<V>, int[]>
          neighborFunction,
      SugiyamaVertex<V>[][] layers,
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph) {
    // get the positions of adjacent vertices in adj_rank
    int[] P = adjPosition(v, neighborFunction, svGraph);
    int m = P.length / 2;
    if (P.length == 0) {
      return -1;
    } else if (P.length % 2 == 1) {
      return P[m];
    } else if (P.length == 2) {
      return (P[0] + P[1]) / 2;
    } else {
      double left = P[m - 1] - P[0];
      double right = P[P.length - 1] - P[m];
      return (P[m - 1] * right + P[m] * left) / (left + right);
    }
  }

  SugiyamaVertex<V>[][] copy(SugiyamaVertex<V>[][] in) {
    SugiyamaVertex[][] copy = new SugiyamaVertex[in.length][];
    for (int i = 0; i < in.length; i++) {
      copy[i] = new SugiyamaVertex[in[i].length];
      for (int j = 0; j < in[i].length; j++) {
        copy[i][j] = in[i][j].copy();
      }
    }
    return copy;
  }

  private static <V> Rectangle maxVertexBounds(
      List<List<SugiyamaVertex<V>>> layers, Function<V, Shape> vertexShapeFunction) {
    // figure out the largest rendered vertex
    Rectangle maxVertexBounds = new Rectangle();

    for (List<SugiyamaVertex<V>> list : layers) {
      for (SugiyamaVertex<V> v : list) {
        if (!(v instanceof SyntheticSugiyamaVertex)) {
          Rectangle bounds = vertexShapeFunction.apply(v.vertex).getBounds();
          int width = Math.max(bounds.width, maxVertexBounds.width);
          int height = Math.max(bounds.height, maxVertexBounds.height);
          maxVertexBounds = new Rectangle(width, height);
        }
      }
    }
    return maxVertexBounds;
  }

  private static <V> Rectangle avgVertexBounds(
      SugiyamaVertex<V>[][] layers, Function<V, Shape> vertexShapeFunction) {

    LongSummaryStatistics w = new LongSummaryStatistics();
    LongSummaryStatistics h = new LongSummaryStatistics();
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        if (!(layers[i][j] instanceof SyntheticSugiyamaVertex)) {
          Rectangle bounds = vertexShapeFunction.apply(layers[i][j].vertex).getBounds();
          w.accept(bounds.width);
          h.accept(bounds.height);
        }
      }
    }
    return new Rectangle((int) w.getAverage(), (int) h.getAverage());
  }
}
