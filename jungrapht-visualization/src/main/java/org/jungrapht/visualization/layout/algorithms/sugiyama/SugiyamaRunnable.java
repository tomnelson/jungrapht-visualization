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
import org.jungrapht.visualization.layout.algorithms.util.InsertionSortCounter;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.synthetics.Synthetic;
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
 * @see "Simple and Efficient Bilayer Cross Counting. Wilhelm Barth, Petra Mutzel, Institut für
 *     Computergraphik und Algorithmen Technische Universität Wien, Michael Jünger, Institut für
 *     Informatik Universität zu Köln"
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
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
    protected boolean transpose;
    protected int transposeLimit;
    protected int maxLevelCross;
    boolean useLongestPathLayering;

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

    public B transpose(boolean transpose) {
      this.transpose = transpose;
      return self();
    }

    public B transposeLimit(int transposeLimit) {
      this.transposeLimit = transposeLimit;
      return self();
    }

    public B maxLevelCross(int maxLevelCross) {
      this.maxLevelCross = maxLevelCross;
      return self();
    }

    public B useLongestPathLayering(boolean useLongestPathLayering) {
      this.useLongestPathLayering = useLongestPathLayering;
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

  protected final LayoutModel<V> layoutModel;
  protected final RenderContext<V, E> renderContext;
  protected Graph<V, E> graph;
  protected Graph<LV<V>, LE<V, E>> svGraph;
  boolean stopit = false;
  protected Predicate<V> vertexPredicate;
  protected Predicate<E> edgePredicate;
  protected Comparator<V> vertexComparator;
  protected Comparator<E> edgeComparator;
  protected boolean straightenEdges;
  protected boolean postStraighten;
  protected boolean transpose;
  protected int transposeLimit;
  protected int maxLevelCross;
  protected boolean useLongestPathLayering;

  protected SugiyamaRunnable(Builder<V, E, ?, ?> builder) {
    this(
        builder.layoutModel,
        builder.renderContext,
        builder.vertexPredicate,
        builder.edgePredicate,
        builder.vertexComparator,
        builder.edgeComparator,
        builder.straightenEdges,
        builder.postStraighten,
        builder.transpose,
        builder.transposeLimit,
        builder.maxLevelCross,
        builder.useLongestPathLayering);
  }

  private SugiyamaRunnable(
      LayoutModel<V> layoutModel,
      RenderContext<V, E> renderContext,
      Predicate<V> vertexPredicate,
      Predicate<E> edgePredicate,
      Comparator<V> vertexComparator,
      Comparator<E> edgeComparator,
      boolean straightenEdges,
      boolean postStraighten,
      boolean transpose,
      int transposeLimit,
      int maxLevelCross,
      boolean useLongestPathLayering) {
    this.layoutModel = layoutModel;
    this.renderContext = renderContext;
    this.vertexComparator = vertexComparator;
    this.vertexPredicate = vertexPredicate;
    this.edgeComparator = edgeComparator;
    this.edgePredicate = edgePredicate;
    this.straightenEdges = straightenEdges;
    this.postStraighten = postStraighten;
    this.transpose = transpose;
    this.transposeLimit = transposeLimit;
    this.maxLevelCross = maxLevelCross;
    this.useLongestPathLayering = useLongestPathLayering;
  }

  protected boolean checkStopped() {
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
    TransformedGraphSupplier<V, E> transformedGraphSupplier = new TransformedGraphSupplier(graph);
    this.svGraph = transformedGraphSupplier.get();
    long transformTime = System.currentTimeMillis();
    log.trace("transform Graph took {}", (transformTime - startTime));

    if (checkStopped()) {
      return;
    }
    GreedyCycleRemoval<LV<V>, LE<V, E>> greedyCycleRemoval = new GreedyCycleRemoval(svGraph);
    Collection<LE<V, E>> feedbackArcs = greedyCycleRemoval.getFeedbackArcs();

    // reverse the direction of feedback arcs so that they no longer introduce cycles in the graph
    // the feedback arcs will be processed later to draw with the correct direction and correct articulation points
    for (LE<V, E> se : feedbackArcs) {
      svGraph.removeEdge(se);
      LE<V, E> newEdge = LE.of(se.getEdge(), se.getTarget(), se.getSource());
      svGraph.addEdge(newEdge.getSource(), newEdge.getTarget(), newEdge);
    }
    long cycles = System.currentTimeMillis();
    log.trace("remove cycles took {}", (cycles - transformTime));

    List<List<LV<V>>> layers;
    if (useLongestPathLayering) {
      layers = GraphLayers.longestPath(svGraph);
    } else {
      layers = GraphLayers.assign(svGraph);
    }
    long assignLayersTime = System.currentTimeMillis();
    log.trace("assign layers took {} ", (assignLayersTime - cycles));

    GraphLayers.checkLayers(layers);

    if (checkStopped()) {
      return;
    }

    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
    List<LE<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    LV<V>[][] layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);
    GraphLayers.checkLayers(layersArray);

    if (checkStopped()) {
      return;
    }

    //  save off a map of edge lists keyed on the target vertex rank
    Map<Integer, List<LE<V, E>>> edgesKeyedOnTarget = new LinkedHashMap<>();
    edges.forEach(
        e -> {
          int targetRank = e.getTarget().getRank();
          if (edgesKeyedOnTarget.containsKey(targetRank)) {
            edgesKeyedOnTarget.get(targetRank).add(e);
          } else {
            ArrayList<LE<V, E>> list = new ArrayList<>();
            list.add(e);
            edgesKeyedOnTarget.put(targetRank, list);
          }
        });
    //  save off a map of edge lists keyed on the source vertex rank
    Map<Integer, List<LE<V, E>>> edgesKeyedOnSource = new LinkedHashMap<>();
    edges.forEach(
        e -> {
          int sourceRank = e.getSource().getRank();
          if (edgesKeyedOnSource.containsKey(sourceRank)) {
            edgesKeyedOnSource.get(sourceRank).add(e);
          } else {
            ArrayList<LE<V, E>> list = new ArrayList<>();
            list.add(e);
            edgesKeyedOnSource.put(sourceRank, list);
          }
        });

    long syntheticsTime = System.currentTimeMillis();
    log.trace("synthetics took {}", (syntheticsTime - assignLayersTime));

    LV<V>[][] best = null;
    int lowestCrossCount = Integer.MAX_VALUE;
    // order the ranks
    for (int i = 0; i < maxLevelCross; i++) {
      if (i % 2 == 0) {
        medianDownwards(layersArray, svGraph);
        if (transpose) transposeDownwards(layersArray, edgesKeyedOnTarget);
      } else {
        medianUpwards(layersArray, svGraph);
        if (transpose) transposeUpwards(layersArray, edgesKeyedOnSource);
      }
      AllLevelCross<V, E> allLevelCross = new AllLevelCross<>(svGraph, layersArray);
      int allLevelCrossCount = allLevelCross.allLevelCross();
      log.trace(" cross count: {}", allLevelCrossCount);
      GraphLayers.checkLayers(layersArray);
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
    log.trace("lowest cross count: {}", lowestCrossCount);

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
            avgVertexBounds.width, Integer.getInteger(PREFIX + "mincross.horizontalOffset", 50));
    int verticalOffset =
        Math.max(
            avgVertexBounds.height, Integer.getInteger(PREFIX + "mincross.verticalOffset", 50));
    GraphLayers.checkLayers(best);
    Map<LV<V>, Point> vertexPointMap = new HashMap<>();

    if (straightenEdges) {
      HorizontalCoordinateAssignment<V, E> horizontalCoordinateAssignment =
          new HorizontalCoordinateAssignment<>(
              best, svGraph, new HashSet<>(), horizontalOffset, verticalOffset);
      horizontalCoordinateAssignment.horizontalCoordinateAssignment();

      GraphLayers.checkLayers(best);

      for (int i = 0; i < best.length; i++) {
        for (int j = 0; j < best[i].length; j++) {
          LV<V> v = best[i][j];
          vertexPointMap.put(v, v.getPoint());
        }
      }

    } else {
      Unaligned.centerPoints(
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
        LV<V> v = best[i][j];
        if (!(v instanceof Synthetic)) {
          Rectangle bounds = vertexShapeFunction.apply(v.getVertex()).getBounds();
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
        LV<V> LV = best[i][j];
        int vertexWidth = 0;
        if (!(LV instanceof Synthetic)) {
          vertexWidth = vertexShapeFunction.apply(LV.getVertex()).getBounds().width;
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

    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = -1;
    int maxY = -1;
    for (Point p : vertexPointMap.values()) {
      minX = Math.min((int) p.x, minX);
      maxX = Math.max((int) p.x, maxX);
      minY = Math.min((int) p.y, minY);
      maxY = Math.max((int) p.y, maxY);
    }
    maxX += horizontalOffset;
    maxY += verticalOffset;
    int pointRangeWidth = maxX - minX;
    int pointRangeHeight = maxY - minY;
    int offsetX = 0;
    int offsetY = 0;
    if (minX < 0) {
      offsetX += -minX + horizontalOffset;
    }
    if (minY < 0) {
      offsetY += -minY + verticalOffset;
    }
    pointRangeWidth *= 1.1;
    pointRangeHeight *= 1.1;

    int maxDimension = Math.max(totalWidth, totalHeight);

    layoutModel.setSize(
        Math.max(maxDimension, layoutModel.getWidth()),
        Math.max(maxDimension, layoutModel.getHeight()));
    long pointsSetTime = System.currentTimeMillis();
    double scalex = (double) layoutModel.getWidth() / pointRangeWidth;
    double scaley = (double) layoutModel.getHeight() / pointRangeHeight;

    for (Map.Entry<LV<V>, Point> entry : vertexPointMap.entrySet()) {
      Point p = entry.getValue();
      Point q = Point.of((offsetX + p.x) * scalex, (offsetY + p.y) * scaley);
      entry.setValue(q);
    }

    // now all the vertices in layers (best) have points associated with them
    // every vertex in vertexMap has a point value

    svGraph.vertexSet().forEach(v -> v.setPoint(vertexPointMap.get(v)));

    if (postStraighten) synthetics.alignArticulatedEdges();
    List<ArticulatedEdge<V, E>> articulatedEdges = synthetics.makeArticulatedEdges();

    Set<E> feedbackEdges = new HashSet<>();
    feedbackArcs.forEach(a -> feedbackEdges.add(a.getEdge()));
    articulatedEdges
        .stream()
        .filter(ae -> feedbackEdges.contains(ae.edge))
        .forEach(
            ae -> {
              svGraph.removeEdge(ae);
              LE<V, E> reversed = ae.reversed();
              svGraph.addEdge(reversed.getSource(), reversed.getTarget(), reversed);
            });

    Map<E, List<Point>> edgePointMap = new HashMap<>();
    for (ArticulatedEdge<V, E> ae : articulatedEdges) {
      List<Point> points = new ArrayList<>();
      if (feedbackEdges.contains(ae.edge)) {
        points.add(ae.getTarget().getPoint());
        points.addAll(ae.reversed().getIntermediatePoints());
        points.add(ae.getSource().getPoint());
      } else {
        points.add(ae.getSource().getPoint());
        points.addAll(ae.getIntermediatePoints());
        points.add(ae.getTarget().getPoint());
      }

      edgePointMap.put(ae.edge, points);
    }
    EdgeShape.ArticulatedLine<V, E> edgeShape = new EdgeShape.ArticulatedLine<>();
    edgeShape.setEdgeArticulationFunction(
        e -> edgePointMap.getOrDefault(e, Collections.emptyList()));

    renderContext.setEdgeShapeFunction(edgeShape);

    long articulatedEdgeTime = System.currentTimeMillis();
    log.trace("articulated edges took {}", (articulatedEdgeTime - pointsSetTime));

    svGraph.vertexSet().forEach(v -> layoutModel.set(v.getVertex(), v.getPoint()));
  }

  protected void transposeDownwards(LV<V>[][] ranks, Map<Integer, List<LE<V, E>>> reducedEdgeMap) {
    GraphLayers.checkLayers(ranks);

    boolean improved = true;
    int sanityCheck = 0;
    while (improved) {
      improved = false;
      for (int i = 0; i < ranks.length; i++) {
        LV<V>[] rank = ranks[i];
        for (int j = 0; j < rank.length - 1; j++) {
          List<LE<V, E>> biLayerEdges = reducedEdgeMap.getOrDefault(i, Collections.emptyList());

          int vw = crossingCount(biLayerEdges);
          if (vw == 0) {
            continue;
          }
          // count with j and j+1 swapped
          int wv = crossingCountSwapped(j, j + 1, rank, biLayerEdges);
          if (vw > wv) {
            improved = true;
            swap(rank, j, j + 1);
          }
        }
      }
      sanityCheck++;
      if (sanityCheck > transposeLimit) {
        break;
      }
    }
    GraphLayers.checkLayers(ranks);
  }

  protected void transposeUpwards(LV<V>[][] ranks, Map<Integer, List<LE<V, E>>> reducedEdgeMap) {
    GraphLayers.checkLayers(ranks);

    boolean improved = true;
    int sanityLimit = Integer.getInteger(PREFIX + "mincross.transposeLimit", 10);
    int sanityCheck = 0;
    while (improved) {
      improved = false;
      for (int i = ranks.length - 1; i >= 0; i--) {
        LV<V>[] rank = ranks[i];
        for (int j = 0; j < rank.length - 1; j++) {
          List<LE<V, E>> biLayerEdges = reducedEdgeMap.getOrDefault(i, Collections.emptyList());

          int vw = crossingCount(biLayerEdges);
          if (vw == 0) {
            continue;
          }
          // count with j and j+1 swapped
          int wv = crossingCountSwapped(j, j + 1, rank, biLayerEdges);
          if (vw > wv) {
            improved = true;
            swap(rank, j, j + 1);
          }
        }
      }
      sanityCheck++;
      if (sanityCheck > sanityLimit) {
        break;
      }
    }
    GraphLayers.checkLayers(ranks);
  }

  private <V> void swap(LV<V>[] array, int i, int j) {
    LV<V> temp = array[i];
    array[i] = array[j];
    array[j] = temp;
    array[i].setIndex(i);
    array[j].setIndex(j);
  }

  Comparator<LE<V, E>> biLevelEdgeComparator = Comparators.biLevelEdgeComparator();

  private int crossingCount(List<LE<V, E>> edges) {
    edges.sort(biLevelEdgeComparator);
    List<Integer> targetIndices = new ArrayList<>();
    for (LE<V, E> edge : edges) {
      targetIndices.add(edge.getTarget().getIndex());
    }
    return InsertionSortCounter.insertionSortCounter(targetIndices);
  }

  private int crossingCountSwapped(int i, int j, LV<V>[] layer, List<LE<V, E>> edges) {
    swap(layer, i, j);
    edges.sort(biLevelEdgeComparator);
    List<Integer> targetIndices = new ArrayList<>();
    for (LE<V, E> edge : edges) {
      targetIndices.add(edge.getTarget().getIndex());
    }
    swap(layer, i, j);
    return InsertionSortCounter.insertionSortCounter(targetIndices);
  }

  //http://www.graphviz.org/Documentation/TSE93.pdf p 15
  void median(LV<V>[][] layers, int i, Graph<LV<V>, LE<V, E>> svGraph) {

    if (i % 2 == 0) {
      for (int r = 0; r < layers.length; r++) {
        for (LV<V> v : layers[r]) {
          double median = medianValue(v, this::upperNeighborIndices, svGraph);
          v.setMeasure(median);
        }
        medianSortAndFixMetadata(layers[r]);
        GraphLayers.checkLayers(layers);
      }
    } else {
      for (int r = layers.length - 1; r >= 0; r--) {
        for (LV<V> v : layers[r]) {
          double median = medianValue(v, this::lowerNeighborIndices, svGraph);
          v.setMeasure(median);
        }
        medianSortAndFixMetadata(layers[r]);
        GraphLayers.checkLayers(layers);
      }
    }
  }

  protected void medianDownwards(LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph) {

    for (int r = 0; r < layers.length; r++) {
      for (LV<V> v : layers[r]) {
        double median = medianValue(v, this::upperNeighborIndices, svGraph);
        v.setMeasure(median);
      }
      medianSortAndFixMetadata(layers[r]);
      GraphLayers.checkLayers(layers);
    }
  }

  protected void medianUpwards(LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph) {

    for (int r = layers.length - 1; r >= 0; r--) {
      for (LV<V> v : layers[r]) {
        double median = medianValue(v, this::lowerNeighborIndices, svGraph);
        v.setMeasure(median);
      }
      medianSortAndFixMetadata(layers[r]);
      GraphLayers.checkLayers(layers);
    }
  }

  void medianSortAndFixMetadata(LV<V>[] layer) {
    Arrays.sort(layer, Comparator.comparing(LV::getMeasure));
    // fix up the metadata!
    fixMetadata(layer);
  }

  private void fixMetadata(LV<V>[] layer) {
    for (int idx = 0; idx < layer.length; idx++) {
      layer[idx].setIndex(idx);
    }
  }

  int[] upperNeighborIndices(Graph<LV<V>, LE<V, E>> graph, LV<V> vertex) {
    return Graphs.predecessorListOf(graph, vertex)
        .stream()
        .mapToInt(LV::getIndex)
        .sorted()
        .toArray();
  }

  int[] lowerNeighborIndices(Graph<LV<V>, LE<V, E>> graph, LV<V> vertex) {
    List<LV<V>> neighbors = Graphs.successorListOf(graph, vertex);
    return Graphs.successorListOf(graph, vertex).stream().mapToInt(LV::getIndex).sorted().toArray();
  }

  int[] adjPosition(
      LV<V> v,
      BiFunction<Graph<LV<V>, LE<V, E>>, LV<V>, int[]> neighborFunction,
      Graph<LV<V>, LE<V, E>> svGraph) {
    return neighborFunction.apply(svGraph, v);
  }

  double medianValue(
      LV<V> v,
      BiFunction<Graph<LV<V>, LE<V, E>>, LV<V>, int[]> neighborFunction,
      Graph<LV<V>, LE<V, E>> svGraph) {
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

  double medianValue(int[] P) {
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

  protected LV<V>[][] copy(LV<V>[][] in) {
    LV[][] copy = new LV[in.length][];
    for (int i = 0; i < in.length; i++) {
      copy[i] = new LV[in[i].length];
      for (int j = 0; j < in[i].length; j++) {
        copy[i][j] = in[i][j].copy();
      }
    }
    return copy;
  }

  private static <V> Rectangle maxVertexBounds(
      List<List<LV<V>>> layers, Function<V, Shape> vertexShapeFunction) {
    // figure out the largest rendered vertex
    Rectangle maxVertexBounds = new Rectangle();

    for (List<LV<V>> list : layers) {
      for (LV<V> v : list) {
        if (!(v instanceof Synthetic)) {
          Rectangle bounds = vertexShapeFunction.apply(v.getVertex()).getBounds();
          int width = Math.max(bounds.width, maxVertexBounds.width);
          int height = Math.max(bounds.height, maxVertexBounds.height);
          maxVertexBounds = new Rectangle(width, height);
        }
      }
    }
    return maxVertexBounds;
  }

  private static <V> Rectangle avgVertexBounds(
      LV<V>[][] layers, Function<V, Shape> vertexShapeFunction) {

    LongSummaryStatistics w = new LongSummaryStatistics();
    LongSummaryStatistics h = new LongSummaryStatistics();
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        if (!(layers[i][j] instanceof Synthetic)) {
          Rectangle bounds = vertexShapeFunction.apply(layers[i][j].getVertex()).getBounds();
          w.accept(bounds.width);
          h.accept(bounds.height);
        }
      }
    }
    return new Rectangle((int) w.getAverage(), (int) h.getAverage());
  }
}
