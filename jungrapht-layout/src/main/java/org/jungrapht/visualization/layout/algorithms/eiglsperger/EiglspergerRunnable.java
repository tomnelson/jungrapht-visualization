package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.algorithms.Layered;
import org.jungrapht.visualization.layout.algorithms.sugiyama.*;
import org.jungrapht.visualization.layout.algorithms.util.LayeredRunnable;
import org.jungrapht.visualization.layout.algorithms.util.PointSummaryStatistics;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.jungrapht.visualization.layout.util.PropertyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see "Methods for Visual Understanding Hierarchical System Structures. KOZO SUGIYAMA, MEMBER,
 *     IEEE, SHOJIRO TAGAWA, AND MITSUHIKO TODA, MEMBER, IEEE"
 * @see "An E log E Line Crossing Algorithm for Levelled Graphs. Vance Waddle and Ashok Malhotra IBM
 *     Thomas J. Watson Research Center"
 * @see "Simple and Efficient Bilayer Cross Counting. Wilhelm Barth, Petra Mutzel, Institut für
 *     Computergraphik und Algorithmen Technische Universität Wien, Michael Jünger, Institut für
 *     Informatik Universität zu Köln"
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 * @see "An Efficient Implementation of Sugiyama's Algorithm for Layered Graph Drawing. Markus
 *     Eiglsperger, Martin Siebenhaller, Michael Kaufman"
 * @param <V> vertex type
 * @param <E> edge type
 */
public class EiglspergerRunnable<V, E> implements LayeredRunnable<E> {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerRunnable.class);

  static {
    PropertyLoader.load();
  }
  /**
   * a Builder to create a configured instance
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  public static class Builder<
      V, E, T extends EiglspergerRunnable<V, E>, B extends Builder<V, E, T, B>> {

    protected LayoutModel<V> layoutModel;
    protected Function<V, Rectangle> vertexShapeFunction;
    protected boolean straightenEdges;
    protected boolean postStraighten;
    protected boolean transpose;
    protected int maxLevelCross;
    protected boolean minimizeEdgeLength = false;
    protected Layering layering = Layering.LONGEST_PATH;
    protected boolean multiComponent;
    protected Comparator<E> edgeComparator = Layered.noopComparator;
    protected Function<Graph<V, E>, Collection<E>> cycleRemovalFunction =
        new GreedyFeedbackArcFunction<>();
    protected Predicate<E> favoredEdgePredicate = Layered.truePredicate;

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    protected B self() {
      return (B) this;
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

    public B maxLevelCross(int maxLevelCross) {
      this.maxLevelCross = maxLevelCross;
      return self();
    }

    public B minimzeEdgeLength(boolean minimizeEdgeLength) {
      this.minimizeEdgeLength = minimizeEdgeLength;
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

    public B edgeComparator(Comparator<E> edgeComparator) {
      this.edgeComparator = edgeComparator;
      return self();
    }

    public B favoredEdgePredicate(Predicate<E> favoredEdgePredicate) {
      this.favoredEdgePredicate = favoredEdgePredicate;
      return self();
    }

    /** {@inheritDoc} */
    public T build() {
      return (T) new EiglspergerRunnable<>(this);
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
  protected Predicate<V> vertexPredicate;
  protected Predicate<E> edgePredicate;
  protected boolean straightenEdges;
  protected boolean postStraighten;
  protected boolean transpose;
  protected int maxLevelCross;
  protected boolean minimizeEdgeLength;
  protected BiFunction<Graph<V, E>, Comparator<V>, Collection<E>> cycleRemovalFunction;
  protected Layering layering;
  protected Map<LV<V>, VertexMetadata<V>> vertexMetadataMap = new HashMap<>();
  protected Map<E, List<Point>> edgePointMap = new HashMap<>();
  protected EiglspergerStepsForward<V, E> stepsForward;
  protected EiglspergerStepsBackward<V, E> stepsBackward;
  protected EiglspergerSteps<V, E> steps = null;
  protected Comparator<E> edgeComparator;
  protected boolean multiComponent;
  protected boolean cancelled;
  protected Predicate<E> favoredEdgePredicate;

  protected EiglspergerRunnable(Builder<V, E, ?, ?> builder) {
    this(
        builder.layoutModel,
        builder.vertexShapeFunction,
        builder.straightenEdges,
        builder.postStraighten,
        builder.transpose,
        builder.maxLevelCross,
        builder.minimizeEdgeLength,
        builder.layering,
        builder.edgeComparator,
        builder.favoredEdgePredicate,
        builder.multiComponent);
  }

  protected EiglspergerRunnable(
      LayoutModel<V> layoutModel,
      Function<V, Rectangle> vertexShapeFunction,
      boolean straightenEdges,
      boolean postStraighten,
      boolean transpose,
      int maxLevelCross,
      boolean minimizeEdgeLength,
      Layering layering,
      Comparator<E> edgeComparator,
      Predicate<E> favoredEdgePredicate,
      boolean multiComponent) {
    this.layoutModel = layoutModel;
    this.vertexShapeFunction = vertexShapeFunction;
    this.straightenEdges = straightenEdges;
    this.postStraighten = postStraighten;
    this.transpose = transpose;
    this.maxLevelCross = maxLevelCross;
    this.minimizeEdgeLength = minimizeEdgeLength;
    if (layering == null) {
      layering = Layering.LONGEST_PATH;
    }
    this.layering = layering;
    this.multiComponent = multiComponent;
    this.edgeComparator = edgeComparator;
    this.favoredEdgePredicate = favoredEdgePredicate;
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
    TransformedGraphSupplier<V, E> transformedGraphSupplier = new TransformedGraphSupplier<>(graph);
    this.svGraph = transformedGraphSupplier.get();
    neighborCache = new NeighborCache<>(svGraph);
    long transformTime = System.currentTimeMillis();
    log.trace("transform Graph took {}", (transformTime - startTime));

    Collection<LE<V, E>> feedbackArcs;
    if (edgeComparator == Layered.noopComparator) {
      GreedyFeedbackArcFunction<LV<V>, LE<V, E>> greedyFeedbackArcFunction =
          new GreedyFeedbackArcFunction<>();
      feedbackArcs = greedyFeedbackArcFunction.apply(svGraph);

    } else {
      Comparator<LE<V, E>> svComparator =
          (e1, e2) -> edgeComparator.compare(e1.getEdge(), e2.getEdge());
      ConstructiveFeedbackArcFunction<LV<V>, LE<V, E>> constructiveFeedbackArcFunction =
          new ConstructiveFeedbackArcFunction<>(svComparator);
      feedbackArcs = constructiveFeedbackArcFunction.apply(svGraph);
    }

    // reverse the direction of feedback arcs so that they no longer introduce cycles in the graph
    // the feedback arcs will be processed later to draw with the correct direction and correct articulation points
    for (LE<V, E> se : feedbackArcs) {
      svGraph.removeEdge(se);
      LE<V, E> newEdge = LE.of(se.getEdge(), se.getTarget(), se.getSource());
      svGraph.addEdge(newEdge.getSource(), newEdge.getTarget(), newEdge);
    }
    long cycles = System.currentTimeMillis();
    log.trace("remove cycles took {}", (cycles - transformTime));

    if (cancelled || Thread.currentThread().isInterrupted()) {
      log.trace("interrupted before layering, cancelled: {}", cancelled);
      return;
    }
    Comparator<LE<V, E>> svComparator =
        (e1, e2) -> edgeComparator.compare(e1.getEdge(), e2.getEdge());
    List<List<LV<V>>> layers;
    switch (layering) {
      case LONGEST_PATH:
        if (edgeComparator != Layered.noopComparator) {
          layers = GraphLayers.longestPath(svGraph, svComparator);
        } else {
          layers = GraphLayers.longestPath(svGraph);
        }
        break;
      case COFFMAN_GRAHAM:
        if (edgeComparator != Layered.noopComparator) {
          layers = GraphLayers.coffmanGraham(svGraph, neighborCache, 0, svComparator);
        } else {
          layers = GraphLayers.coffmanGraham(svGraph, neighborCache, 0);
        }
        break;
      case NETWORK_SIMPLEX:
        if (edgeComparator != Layered.noopComparator) {
          layers = GraphLayers.networkSimplex(svGraph, svComparator);
        } else {
          layers = GraphLayers.networkSimplex(svGraph);
        }
        break;
      case TOP_DOWN:
      default:
        if (edgeComparator != Layered.noopComparator) {
          layers = GraphLayers.assign(svGraph, svComparator);
        } else {
          layers = GraphLayers.assign(svGraph);
        }
    }
    if (minimizeEdgeLength) {
      GraphLayers.minimizeEdgeLength(svGraph, layers);
    }
    long assignLayersTime = System.currentTimeMillis();
    log.trace("assign layers took {} ", (assignLayersTime - cycles));
    if (log.isTraceEnabled()) {
      GraphLayers.checkLayers(layers);
    }

    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
    List<LE<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    LV<V>[][] layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);

    if (log.isTraceEnabled()) {
      GraphLayers.checkLayers(layersArray);
    }

    long syntheticsTime = System.currentTimeMillis();
    log.trace("synthetics took {}", (syntheticsTime - assignLayersTime));

    if (svGraph.edgeSet().size() > 200) {
      maxLevelCross = 2;
    }
    stepsForward = new EiglspergerStepsForward<>(svGraph, neighborCache, layersArray, transpose);
    stepsBackward = new EiglspergerStepsBackward<>(svGraph, neighborCache, layersArray, transpose);

    int bestCrossCount = Integer.MAX_VALUE;
    Graph<LV<V>, Integer> bestCompactionGraph = null;
    for (int i = 0; i < maxLevelCross; i++) {
      if (cancelled || Thread.currentThread().isInterrupted()) {
        log.trace("interrupted in level cross, cancelled: {}", cancelled);
        return;
      }
      if (i % 2 == 0) {
        int sweepCrossCount = stepsForward.sweep(layersArray);
        Graph<LV<V>, Integer> compactionGraph = stepsForward.compactionGraph;
        if (sweepCrossCount < bestCrossCount) {
          bestCrossCount = sweepCrossCount;
          vertexMetadataMap = save(layersArray);
          bestCompactionGraph = copy(compactionGraph);
        } else {
          if (log.isTraceEnabled()) {
            log.trace("best:{}", layersArray);
          }
          //                    break;
        }
      } else {
        int sweepCrossCount = stepsBackward.sweep(layersArray);
        Graph<LV<V>, Integer> compactionGraph = stepsBackward.compactionGraph;
        if (sweepCrossCount < bestCrossCount) {
          bestCrossCount = sweepCrossCount;
          vertexMetadataMap = save(layersArray);
          bestCompactionGraph = copy(compactionGraph);
        } else {
          if (log.isTraceEnabled()) {
            log.trace("best:{}", layersArray);
          }
          //                    break;
        }
      }
    }
    log.trace("bestCrossCount: {}", bestCrossCount);

    restore(layersArray, vertexMetadataMap);
    Arrays.stream(layersArray)
        .forEach(layer -> Arrays.sort(layer, Comparator.comparingInt(LV::getIndex)));

    // figure out the avg size of rendered vertex
    Rectangle avgVertexBounds = maxVertexBounds(layersArray, vertexShapeFunction);
    log.trace("avgVertexBounds: {}", avgVertexBounds);
    int horizontalOffset =
        (int)
            Math.max(
                avgVertexBounds.width,
                Integer.getInteger(PREFIX + "mincross.horizontalOffset", 50));
    log.trace("horizontalOffset: {}", horizontalOffset);
    int verticalOffset =
        (int)
            Math.max(
                avgVertexBounds.height, Integer.getInteger(PREFIX + "mincross.verticalOffset", 50));
    log.trace("verticalOffset: {}", verticalOffset);
    GraphLayers.checkLayers(layersArray);

    // update the indices of the all layers
    for (LV<V>[] value : layersArray) {
      for (int j = 0; j < value.length; j++) {
        value[j].setIndex(j);
      }
    }
    if (cancelled || Thread.currentThread().isInterrupted()) {
      log.trace("interrupted before compaction, cancelled: {}", cancelled);
      return;
    }

    // do the horizontal compaction
    if (straightenEdges) {
      HorizontalCoordinateAssignment<V, E> horizontalCoordinateAssignment =
          new HorizontalCoordinateAssignment<>(
              layersArray,
              svGraph,
              bestCompactionGraph,
              new HashSet<>(),
              horizontalOffset,
              verticalOffset);
      horizontalCoordinateAssignment.horizontalCoordinateAssignment();

      GraphLayers.checkLayers(layersArray);

    } else {
      // just center the rows
      Unaligned.centerPoints(
          layersArray, vertexShapeFunction, horizontalOffset, verticalOffset); //, vertexPointMap);
    }

    Map<Integer, Integer> rowWidthMap = new HashMap<>(); // all the row widths
    Map<Integer, Integer> rowMaxHeightMap = new HashMap<>(); // all the row heights
    int layerIndex = 0;
    int totalHeight = 0;
    int totalWidth = 0;

    for (LV<V>[] lvs : layersArray) {

      int width = horizontalOffset;
      int maxHeight = 0;
      for (LV<V> v : lvs) {
        if (!(v instanceof SyntheticLV)) {
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
    log.trace("rowWidthMap: {}", rowWidthMap);
    log.trace("rowMaxHeightMap: {}", rowMaxHeightMap);

    int widestRowWidth = rowWidthMap.values().stream().mapToInt(v -> v).max().orElse(0);
    int x = horizontalOffset;
    int y = verticalOffset;
    layerIndex = 0;
    if (log.isTraceEnabled()) {
      log.trace("layerMaxHeights {}", rowMaxHeightMap);
    }
    for (LV<V>[] lvs : layersArray) {
      int previousVertexWidth = 0;
      // offset against widest row
      x += (widestRowWidth - rowWidthMap.get(layerIndex)) / 2;

      y += rowMaxHeightMap.get(layerIndex) / 2;
      if (layerIndex > 0) {
        y += rowMaxHeightMap.get(layerIndex - 1) / 2;
      }

      int rowWidth = 0;
      for (LV<V> EiglspergerVertex : lvs) {
        int vertexWidth = 0;
        if (!(EiglspergerVertex instanceof SyntheticLV)) {
          vertexWidth = (int) vertexShapeFunction.apply(EiglspergerVertex.getVertex()).width;
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
    log.trace("widestRowWidth: {}", widestRowWidth);
    // end determine the max width and height

    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = -1;
    int maxY = -1;
    for (Point p :
        Arrays.stream(layersArray)
            .flatMap(Arrays::stream)
            .map(LV::getPoint)
            .collect(Collectors.toList())) {
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

    log.trace("minX: {}, maxX: {}, minY: {}, maxY: {}", minX, maxX, minY, maxY);

    int maxDimension = Math.max(totalWidth, totalHeight);
    log.trace("maxDimension: {}", maxDimension);

    layoutModel.setSize(totalWidth, Math.max(maxDimension, layoutModel.getHeight()));

    log.trace(
        "set the layoutModel size to {} by {}", layoutModel.getWidth(), layoutModel.getHeight());

    long pointsSetTime = System.currentTimeMillis();
    double scalex = (double) layoutModel.getWidth() / pointRangeWidth;
    double scaley = (double) layoutModel.getHeight() / pointRangeHeight;

    for (LV v : Arrays.stream(layersArray).flatMap(Arrays::stream).collect(Collectors.toList())) {
      Point p = v.getPoint();
      Point q = Point.of((offsetX + p.x) * scalex, (offsetY + p.y) * scaley);
      v.setPoint(q);
    }

    if (favoredEdgePredicate != Layered.truePredicate) {
      //     normalize on favored edges
      normalizeOnFavoredEdges(layersArray, favoredEdgePredicate, horizontalOffset / 4);
    }

    if (postStraighten) {
      synthetics.alignArticulatedEdges();
    }
    List<ArticulatedEdge<V, E>> articulatedEdges = synthetics.makeArticulatedEdges();

    Set<E> feedbackEdges = new HashSet<>();
    feedbackArcs.forEach(a -> feedbackEdges.add(a.getEdge()));
    articulatedEdges
        .stream()
        .filter(ae -> feedbackEdges.contains(ae.getEdge()))
        .forEach(
            ae -> {
              svGraph.removeEdge(ae);
              LE<V, E> reversed = ae.reversed();
              svGraph.addEdge(reversed.getSource(), reversed.getTarget(), reversed);
            });

    for (ArticulatedEdge<V, E> ae : articulatedEdges) {
      List<Point> points = new ArrayList<>();
      if (feedbackEdges.contains(ae.getEdge())) {
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
      log.debug("interrupted before setting layoutModel from svGraph, cancelled: {}", cancelled);
      return;
    }
    svGraph.vertexSet().forEach(v -> layoutModel.set(v.getVertex(), v.getPoint()));
  }

  public void setFavoredEdgePredicate(Predicate<E> favoredEdgePredicate) {
    this.favoredEdgePredicate = favoredEdgePredicate;
  }

  public Map<E, List<Point>> getEdgePointMap() {
    return edgePointMap;
  }

  // experimental
  protected void normalizeOnFavoredEdges(
      LV<V>[][] layers, Predicate<E> favoredEdgePredicate, int horizontalOffset) {

    Predicate<LE<V, E>> svPredicate = e -> favoredEdgePredicate.test(e.getEdge());
    log.trace("before {}", layers);
    for (int i = 0; i < layers.length; i++) {
      Map<LV<V>, Point> verticesInThisRow = new HashMap<>();
      for (int j = 0; j < layers[i].length; j++) {
        // save off all the points for the vertices in this row
        LV<V> v = layers[i][j];
        verticesInThisRow.put(v, v.getPoint());
      }
      for (int j = 0; j < layers[i].length; j++) {
        // get a vertex and look at outgoing edges
        LV<V> v = layers[i][j];
        if (svGraph.containsVertex(v)) {
          Collection<LE<V, E>> edges = svGraph.outgoingEdgesOf(v);
          if (!edges.isEmpty()) {
            Optional<LE<V, E>> maybeFavoredEdge = edges.stream().filter(svPredicate).findFirst();
            if (maybeFavoredEdge.isPresent()) {
              LV<V> favoredTarget = svGraph.getEdgeTarget(maybeFavoredEdge.get());
              // compare points in vertexPointMap
              Point sp = v.getPoint();
              Point tp = favoredTarget.getPoint();
              // if the x values are not the same, move the target to under the source
              if (sp.x != tp.x) {
                double offset = sp.x - tp.x;
                favoredTarget.setPoint(tp.add(offset, 0));
                // see if i created an overlap
                // get the row # (rank) for the vertex i moved
                int movedVertexRow = favoredTarget.getRank();
                // check everything else in that rank to see if there is an x overlap
                Arrays.stream(layers[movedVertexRow])
                    .filter(vInRank -> vInRank != favoredTarget)
                    .forEach(
                        vInRank -> {
                          double vx = vInRank.getPoint().x;
                          if (vx == favoredTarget.getPoint().x) {
                            // move vInRank a little
                            Point movedPoint = vInRank.getPoint().add(horizontalOffset, 0);
                            vInRank.setPoint(movedPoint);
                          }
                        });
              }
            }
          }
        }
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("after {}", layers);
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
      }
    }
    return layers;
  }

  private static <V> Rectangle maxVertexBounds(
      LV<V>[][] layers, Function<V, Rectangle> vertexShapeFunction) {
    // figure out the largest rendered vertex
    Rectangle maxVertexBounds = Rectangle.IDENTITY;

    for (LV<V>[] layer : layers) {
      for (LV<V> vlv : layer) {
        if (!(vlv instanceof SyntheticLV)) {
          Rectangle bounds = vertexShapeFunction.apply(vlv.getVertex());
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
    for (LV<V>[] layer : layers) {
      for (LV<V> vlv : layer) {
        if (!(vlv instanceof SyntheticLV)) {
          Rectangle bounds = vertexShapeFunction.apply(vlv.getVertex());
          w.accept((int) bounds.width);
          h.accept((int) bounds.height);
        }
      }
    }
    return Rectangle.of((int) w.getAverage(), (int) h.getAverage());
  }

  protected Graph<LV<V>, Integer> copy(Graph<LV<V>, Integer> graph) {
    Graph<LV<V>, Integer> out =
        GraphTypeBuilder.forGraph(graph)
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    graph.vertexSet().forEach(out::addVertex);
    graph.edgeSet().forEach(e -> out.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e)));
    return out;
  }

  private void updatePositions(LV<V>[][] layersArray, Graph<LV<V>, Integer> compactionGraph) {
    if (log.isTraceEnabled()) {
      log.trace("vertices of compactionGraph:{}", compactionGraph.vertexSet());
      log.trace("edges of compactionGraph:{}", compactionGraph.edgeSet());
    }
    for (LV<V>[] layer : layersArray) {
      for (LV<V> v : layer) {
        // set the pos of v to the distance to the root in this layer
        v.setPos(distanceToRoot(compactionGraph, v));
      }
    }
  }

  int distanceToRoot(Graph<LV<V>, Integer> compactionGraph, LV<V> v) {
    int distance = 0;
    Set<LV<V>> preds = neighborCache.predecessorsOf(v);
    while (!preds.isEmpty()) {
      distance++;
      // pick one
      LV<V> pred = preds.stream().findFirst().get();
      preds = neighborCache.predecessorsOf(pred);
    }
    return distance;
  }

  protected Rectangle computeLayoutExtent(Collection<Point> points) {
    // find the dimensions of the layout
    PointSummaryStatistics pss = new PointSummaryStatistics();
    points.forEach(pss::accept);
    return Rectangle.from(pss.getMin(), pss.getMax());
  }
}
