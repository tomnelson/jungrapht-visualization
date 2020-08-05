package org.jungrapht.visualization.layout.algorithms.sugiyama;

import static org.jungrapht.visualization.layout.model.LayoutModel.PREFIX;

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
import java.util.function.Function;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.NeighborCache;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.InsertionSortCounter;
import org.jungrapht.visualization.layout.algorithms.util.LayeredRunnable;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
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
public class SugiyamaRunnable<V, E> implements LayeredRunnable<E> {

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
    protected Function<V, Rectangle> vertexShapeFunction;
    protected Predicate<V> vertexPredicate; // can be null
    protected Predicate<E> edgePredicate; // can be null
    protected Comparator<V> vertexComparator = (v1, v2) -> 0;
    protected Comparator<E> edgeComparator = (e1, e2) -> 0;
    protected boolean straightenEdges;
    protected boolean postStraighten;
    protected boolean transpose;
    protected int transposeLimit;
    protected int maxLevelCross;
    protected Layering layering = Layering.TOP_DOWN;
    protected boolean multiComponent;

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

    public B vertexShapeFunction(Function<V, Rectangle> vertexShapeFunction) {
      this.vertexShapeFunction = vertexShapeFunction;
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

    public B layering(Layering layering) {
      this.layering = layering;
      return self();
    }

    public B multiComponent(boolean multiComponent) {
      this.multiComponent = multiComponent;
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
  protected Function<V, Rectangle> vertexShapeFunction;
  protected Graph<V, E> graph;
  protected Graph<LV<V>, LE<V, E>> svGraph;
  protected NeighborCache<LV<V>, LE<V, E>> neighborCache;
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
  protected Layering layering;
  protected Map<LV<V>, VertexMetadata<V>> vertexMetadataMap = new HashMap<>();
  protected Map<E, List<Point>> edgePointMap = new HashMap<>();
  protected boolean multiComponent;
  protected boolean cancelled;

  protected SugiyamaRunnable(Builder<V, E, ?, ?> builder) {
    this(
        builder.layoutModel,
        builder.vertexShapeFunction,
        builder.vertexPredicate,
        builder.edgePredicate,
        builder.vertexComparator,
        builder.edgeComparator,
        builder.straightenEdges,
        builder.postStraighten,
        builder.transpose,
        builder.transposeLimit,
        builder.maxLevelCross,
        builder.layering,
        builder.multiComponent);
  }

  private SugiyamaRunnable(
      LayoutModel<V> layoutModel,
      Function<V, Rectangle> vertexShapeFunction,
      Predicate<V> vertexPredicate,
      Predicate<E> edgePredicate,
      Comparator<V> vertexComparator,
      Comparator<E> edgeComparator,
      boolean straightenEdges,
      boolean postStraighten,
      boolean transpose,
      int transposeLimit,
      int maxLevelCross,
      Layering layering,
      boolean multiComponent) {
    this.layoutModel = layoutModel;
    this.vertexShapeFunction = vertexShapeFunction;
    this.vertexComparator = vertexComparator;
    this.vertexPredicate = vertexPredicate;
    this.edgeComparator = edgeComparator;
    this.edgePredicate = edgePredicate;
    this.straightenEdges = straightenEdges;
    this.postStraighten = postStraighten;
    this.transpose = transpose;
    this.transposeLimit = transposeLimit;
    this.maxLevelCross = maxLevelCross;
    if (layering == null) {
      layering = Layering.TOP_DOWN;
    }
    this.layering = layering;
    this.multiComponent = multiComponent;
  }

  @Override
  public void cancel() {
    this.cancelled = true;
  }

  @Override
  public void run() {
    this.graph = layoutModel.getGraph();

    if (graph.vertexSet().isEmpty()) {
      return;
    }
    if (graph.vertexSet().size() == 1) {
      V v = graph.vertexSet().stream().findFirst().get();
      layoutModel.setSize(50, layoutModel.getHeight());
      layoutModel.set(v, layoutModel.getWidth() / 2, layoutModel.getHeight() / 2);
      return;
    }
    long startTime = System.currentTimeMillis();
    TransformedGraphSupplier<V, E> transformedGraphSupplier = new TransformedGraphSupplier(graph);
    this.svGraph = transformedGraphSupplier.get();
    this.neighborCache = new NeighborCache<>(svGraph);
    long transformTime = System.currentTimeMillis();
    log.trace("transform Graph took {}", (transformTime - startTime));

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

    // check for interrupted before layering
    if (cancelled || Thread.currentThread().isInterrupted()) {
      log.info("interrupted before layering, cancelled: {}", cancelled);
      return;
    }
    List<List<LV<V>>> layers;
    switch (layering) {
      case NETWORK_SIMPLEX:
        layers = GraphLayers.networkSimplex(svGraph);
        break;
      case LONGEST_PATH:
        layers = GraphLayers.longestPath(svGraph);
        break;
      case COFFMAN_GRAHAM:
        layers = GraphLayers.coffmanGraham(svGraph, 10);
        break;
      case TOP_DOWN:
      default:
        layers = GraphLayers.assign(svGraph);
    }
    long assignLayersTime = System.currentTimeMillis();
    log.trace("assign layers took {} ", (assignLayersTime - cycles));

    GraphLayers.checkLayers(layers);

    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
    List<LE<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    LV<V>[][] layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);
    GraphLayers.checkLayers(layersArray);

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

    VertexMetadata<V>[][] vertexMetadata = null;
    LV<V>[][] best = null;

    int lowestCrossCount = Integer.MAX_VALUE;
    // order the ranks
    for (int i = 0; i < maxLevelCross; i++) {
      if (cancelled || Thread.currentThread().isInterrupted()) {
        log.info("interrupted in level cross, cancelled: {}", cancelled);
        return;
      }
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
        vertexMetadataMap = save(layersArray);
        GraphLayers.checkLayers(layersArray);
        lowestCrossCount = allLevelCrossCount;
      }
    }
    log.trace("lowest cross count: {}", lowestCrossCount);

    restore(layersArray, vertexMetadataMap);

    Arrays.stream(layersArray)
        .forEach(layer -> Arrays.sort(layer, Comparator.comparingInt(LV::getIndex)));
    // compare best and layersArray
    if (log.isTraceEnabled()) {
      log.trace("best:{}", best);
      log.trace("layersArray:{}", layersArray);
    }

    for (int i = 0; i < best.length; i++) {
      LV<V>[] layer = best[i];
      for (int j = 0; j < layer.length; j++) {
        LV<V> v = layer[j];
        if (v.getVertex() != layersArray[i][j].getVertex()) {
          log.error("not equal");
        }
      }
    }

    // in case zero iterations of cross counting were requested:
    long crossCountTests = System.currentTimeMillis();
    log.trace("cross counts took {}", (crossCountTests - syntheticsTime));
    GraphLayers.checkLayers(layersArray);

    // done optimizing for edge crossing

    // figure out the avg size of rendered vertex
    Rectangle avgVertexBounds = avgVertexBounds(layersArray, vertexShapeFunction);

    int horizontalOffset =
        (int)
            Math.max(
                avgVertexBounds.width,
                Integer.getInteger(PREFIX + "mincross.horizontalOffset", 50));
    int verticalOffset =
        (int)
            Math.max(
                avgVertexBounds.height, Integer.getInteger(PREFIX + "mincross.verticalOffset", 50));
    GraphLayers.checkLayers(layersArray);
    Map<LV<V>, Point> vertexPointMap = new HashMap<>();

    if (cancelled || Thread.currentThread().isInterrupted()) {
      log.info("interrupted before compaction, cancelled: {}", cancelled);
      return;
    }
    if (straightenEdges) {
      HorizontalCoordinateAssignment<V, E> horizontalCoordinateAssignment =
          new HorizontalCoordinateAssignment<>(
              layersArray, svGraph, new HashSet<>(), horizontalOffset, verticalOffset);
      horizontalCoordinateAssignment.horizontalCoordinateAssignment();

      GraphLayers.checkLayers(layersArray);

      for (int i = 0; i < layersArray.length; i++) {
        for (int j = 0; j < layersArray[i].length; j++) {
          LV<V> v = layersArray[i][j];
          vertexPointMap.put(v, v.getPoint());
        }
      }

    } else {
      Unaligned.centerPoints(
          layersArray, vertexShapeFunction, horizontalOffset, verticalOffset, vertexPointMap);
    }

    Map<Integer, Integer> rowWidthMap = new HashMap<>(); // all the row widths
    Map<Integer, Integer> rowMaxHeightMap = new HashMap<>(); // all the row heights
    int layerIndex = 0;
    int totalHeight = 0;
    int totalWidth = 0;
    for (int i = 0; i < layersArray.length; i++) {

      int width = horizontalOffset;
      int maxHeight = 0;
      for (int j = 0; j < layersArray[i].length; j++) {
        LV<V> v = layersArray[i][j];
        if (!(v instanceof Synthetic)) {
          Rectangle bounds = vertexShapeFunction.apply(v.getVertex());
          width += bounds.width + horizontalOffset;
          maxHeight = Math.max(maxHeight, (int) bounds.height);
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
    for (int i = 0; i < layersArray.length; i++) {
      int previousVertexWidth = 0;
      // offset against widest row
      x += (widestRowWidth - rowWidthMap.get(layerIndex)) / 2;

      y += rowMaxHeightMap.get(layerIndex) / 2;
      if (layerIndex > 0) {
        y += rowMaxHeightMap.get(layerIndex - 1) / 2;
      }

      int rowWidth = 0;
      for (int j = 0; j < layersArray[i].length; j++) {
        LV<V> LV = layersArray[i][j];
        int vertexWidth = 0;
        if (!(LV instanceof Synthetic)) {
          vertexWidth = (int) vertexShapeFunction.apply(LV.getVertex()).width;
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
        multiComponent ? totalWidth : Math.max(maxDimension, layoutModel.getWidth()),
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

    //    Map<E, List<Point>> edgePointMap = new HashMap<>();
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

    long articulatedEdgeTime = System.currentTimeMillis();
    log.trace("articulated edges took {}", (articulatedEdgeTime - pointsSetTime));

    if (cancelled) {
      log.info("interrupted before setting layoutModel from svGraph, cancelled: {}", cancelled);
      return;
    }
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

          int vw = AccumulatorTreeUtil.crossingCount(biLayerEdges);
          if (log.isTraceEnabled()) {
            // make sure the accumulator tree count matches the insertion sort count
            int vw2 = crossingCount(biLayerEdges);
            if (vw != vw2) {
              log.error("{} != {}", vw, vw2);
            }
            int vw3 = AccumulatorTreeUtil.crossingWeight(biLayerEdges, idx -> 1);
            if (vw != vw3) {
              log.error("{} != {}", vw, vw3);
            }
          }
          if (vw == 0) {
            break; // perfect!
          }
          // count with j and j+1 swapped
          swap(rank, j, j + 1);
          int wv = AccumulatorTreeUtil.crossingCount(biLayerEdges);
          if (log.isTraceEnabled()) {
            int wv2 = crossingCount(biLayerEdges);
            if (wv != wv2) {
              log.error("{} != {}", wv, wv2);
            }
            int wv3 = AccumulatorTreeUtil.crossingWeight(biLayerEdges, idx -> 1);
            if (wv != wv3) {
              log.error("{} != {}", wv, wv3);
            }
          }
          swap(rank, j, j + 1);
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

          int vw = AccumulatorTreeUtil.crossingCount(biLayerEdges);
          if (log.isTraceEnabled()) {
            // make sure the accumulator tree count matches the insertion sort count
            int vw2 = crossingCount(biLayerEdges);
            if (vw != vw2) {
              log.error("{} != {}", vw, vw2);
            }
            int vw3 = AccumulatorTreeUtil.crossingWeight(biLayerEdges, idx -> 1);
            if (vw != vw3) {
              log.error("{} != {}", vw, vw3);
            }
          }
          if (vw == 0) {
            break; // no crossings. done with these two ranks!
          }
          // count with j and j+1 swapped
          swap(rank, j, j + 1); // swap for comparison
          int wv = AccumulatorTreeUtil.crossingCount(biLayerEdges);
          if (log.isTraceEnabled()) {
            int wv2 = crossingCount(biLayerEdges);
            if (wv != wv2) {
              log.error("{} != {}", wv, wv2);
            }
            int wv3 = AccumulatorTreeUtil.crossingWeight(biLayerEdges, idx -> 1);
            if (wv != wv3) {
              log.error("{} != {}", wv, wv3);
            }
          }
          swap(rank, j, j + 1); // swap back
          if (vw > wv) {
            improved = true;
            swap(rank, j, j + 1); // sawp to improved order
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

  public Map<E, List<Point>> getEdgePointMap() {
    return edgePointMap;
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

  Function<LV<V>, int[]> upperNeighborIndicesMethod = this::upperNeighborIndices;
  Function<LV<V>, int[]> lowerNeighborIndicesMethod = this::lowerNeighborIndices;

  //http://www.graphviz.org/Documentation/TSE93.pdf p 15
  void median(LV<V>[][] layers, int i, Graph<LV<V>, LE<V, E>> svGraph) {

    if (i % 2 == 0) {
      for (int r = 0; r < layers.length; r++) {
        for (LV<V> v : layers[r]) {
          double median = medianValue(v, upperNeighborIndicesMethod, svGraph);
          v.setMeasure(median);
        }
        medianSortAndFixMetadata(layers[r]);
        GraphLayers.checkLayers(layers);
      }
    } else {
      for (int r = layers.length - 1; r >= 0; r--) {
        for (LV<V> v : layers[r]) {
          double median = medianValue(v, lowerNeighborIndicesMethod, svGraph);
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
        double median = medianValue(v, upperNeighborIndicesMethod, svGraph);
        v.setMeasure(median);
      }
      medianSortAndFixMetadata(layers[r]);
      GraphLayers.checkLayers(layers);
    }
  }

  protected void medianUpwards(LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph) {

    for (int r = layers.length - 1; r >= 0; r--) {
      for (LV<V> v : layers[r]) {
        double median = medianValue(v, lowerNeighborIndicesMethod, svGraph);
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

  int[] upperNeighborIndices(LV<V> vertex) {
    return neighborCache.predecessorsOf(vertex).stream().mapToInt(LV::getIndex).sorted().toArray();
  }

  int[] lowerNeighborIndices(LV<V> vertex) {
    return neighborCache.successorsOf(vertex).stream().mapToInt(LV::getIndex).sorted().toArray();
  }

  int[] adjPosition(
      LV<V> v, Function<LV<V>, int[]> neighborFunction, Graph<LV<V>, LE<V, E>> svGraph) {
    return neighborFunction.apply(v);
  }

  double medianValue(
      LV<V> v, Function<LV<V>, int[]> neighborFunction, Graph<LV<V>, LE<V, E>> svGraph) {
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

  protected Map<LV<V>, VertexMetadata<V>> save(LV<V>[][] in) {
    Map<LV<V>, VertexMetadata<V>> vertexMetadataMap = new HashMap<>();
    VertexMetadata[][] saved = new VertexMetadata[in.length][];
    for (int i = 0; i < in.length; i++) {
      saved[i] = new VertexMetadata[in[i].length];
      for (int j = 0; j < in[i].length; j++) {
        saved[i][j] = VertexMetadata.of(in[i][j]);
        vertexMetadataMap.put(in[i][j], saved[i][j]);
      }
    }
    return vertexMetadataMap;
  }

  protected LV<V>[][] restore(LV<V>[][] layers, Map<LV<V>, VertexMetadata<V>> vertexMetadataMap) {
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        VertexMetadata<V> vertexMetadata = vertexMetadataMap.get(layers[i][j]);
        vertexMetadata.applyTo(layers[i][j]);
        //        saved[i][j].applyTo(layers[i][j]);
      }
    }
    return layers;
  }

  private static <V> Rectangle maxVertexBounds(
      List<List<LV<V>>> layers, Function<V, Rectangle> vertexShapeFunction) {
    // figure out the largest rendered vertex
    Rectangle maxVertexBounds = Rectangle.IDENTITY;

    for (List<LV<V>> list : layers) {
      for (LV<V> v : list) {
        if (!(v instanceof Synthetic)) {
          Rectangle bounds = vertexShapeFunction.apply(v.getVertex());
          int width = (int) Math.max(bounds.width, maxVertexBounds.width);
          int height = (int) Math.max(bounds.height, maxVertexBounds.height);
          maxVertexBounds = Rectangle.of(0, 0, width, height);
        }
      }
    }
    return maxVertexBounds;
  }

  private static <V> Rectangle avgVertexBounds(
      LV<V>[][] layers, Function<V, Rectangle> vertexShapeFunction) {

    LongSummaryStatistics w = new LongSummaryStatistics();
    LongSummaryStatistics h = new LongSummaryStatistics();
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        if (!(layers[i][j] instanceof Synthetic)) {
          Rectangle bounds = vertexShapeFunction.apply(layers[i][j].getVertex());
          w.accept((int) bounds.width);
          h.accept((int) bounds.height);
        }
      }
    }
    return Rectangle.of(0, 0, (int) w.getAverage(), (int) h.getAverage());
  }
}
