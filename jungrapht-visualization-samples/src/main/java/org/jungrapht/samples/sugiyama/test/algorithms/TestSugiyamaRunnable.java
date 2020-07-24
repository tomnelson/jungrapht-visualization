package org.jungrapht.samples.sugiyama.test.algorithms;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.jgrapht.alg.util.NeighborCache;
import org.jungrapht.visualization.layout.algorithms.sugiyama.AllLevelCross;
import org.jungrapht.visualization.layout.algorithms.sugiyama.ArticulatedEdge;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GraphLayers;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GreedyCycleRemoval;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaRunnable;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Synthetics;
import org.jungrapht.visualization.layout.algorithms.sugiyama.TransformedGraphSupplier;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Unaligned;
import org.jungrapht.visualization.layout.algorithms.sugiyama.VertexMetadata;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.jungrapht.visualization.layout.util.synthetics.Synthetic;
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
public class TestSugiyamaRunnable<V, E> extends SugiyamaRunnable<V, E> implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(TestSugiyamaRunnable.class);

  /**
   * a Builder to create a configured instance
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  public static class Builder<
          V, E, T extends TestSugiyamaRunnable<V, E>, B extends Builder<V, E, T, B>>
      extends SugiyamaRunnable.Builder<V, E, T, B> {
    boolean doUpLeft;
    boolean doDownLeft;
    boolean doUpRight;
    boolean doDownRight;

    public B doUpLeft(boolean doUpLeft) {
      this.doUpLeft = doUpLeft;
      return self();
    }

    public B doUpRight(boolean doUpRight) {
      this.doUpRight = doUpRight;
      return self();
    }

    public B doDownLeft(boolean doDownLeft) {
      this.doDownLeft = doDownLeft;
      return self();
    }

    public B doDownRight(boolean doDownRight) {
      this.doDownRight = doDownRight;
      return self();
    }

    /** {@inheritDoc} */
    public T build() {
      return (T) new TestSugiyamaRunnable<>(this);
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

  boolean doUpLeft;
  boolean doDownLeft;
  boolean doUpRight;
  boolean doDownRight;

  protected TestSugiyamaRunnable(Builder<V, E, ?, ?> builder) {
    super(builder);
    this.doUpLeft = builder.doUpLeft;
    this.doDownLeft = builder.doDownLeft;
    this.doUpRight = builder.doUpRight;
    this.doDownRight = builder.doDownRight;
  }

  @Override
  public void run() {
    this.graph = layoutModel.getGraph();

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

    List<List<LV<V>>> layers;
    switch (layering) {
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
        vertexMetadataMap = save(layersArray);
        GraphLayers.checkLayers(layersArray);
        lowestCrossCount = allLevelCrossCount;
      }
    }
    log.trace("lowest cross count: {}", lowestCrossCount);

    restore(layersArray, vertexMetadataMap);
    Arrays.stream(layersArray)
        .forEach(layer -> Arrays.sort(layer, Comparator.comparingInt(LV::getIndex)));

    long crossCountTests = System.currentTimeMillis();
    log.trace("cross counts took {}", (crossCountTests - syntheticsTime));
    GraphLayers.checkLayers(layersArray);

    // done optimizing for edge crossing

    // figure out the avg size of rendered vertex
    Rectangle avgVertexBounds = avgVertexBounds(layersArray, vertexShapeFunction);

    int horizontalOffset =
            (int) Math.max(
                avgVertexBounds.width, Integer.getInteger(PREFIX + "mincross.horizontalOffset", 50));
    int verticalOffset =
            (int) Math.max(
                avgVertexBounds.height, Integer.getInteger(PREFIX + "mincross.verticalOffset", 50));
    GraphLayers.checkLayers(layersArray);
    Map<LV<V>, Point> vertexPointMap = new HashMap<>();

    if (straightenEdges) {
      SelectiveSugiyamaHorizontalCoordinateAssignment<V, E> horizontalCoordinateAssignment =
          new SelectiveSugiyamaHorizontalCoordinateAssignment(
              layersArray,
              svGraph,
              new HashSet<>(),
              50,
              50,
              doUpLeft,
              doUpRight,
              doDownLeft,
              doDownRight);
      horizontalCoordinateAssignment.horizontalCoordinateAssignment();

      //      HorizontalCoordinateAssignment<V, E> horizontalCoordinateAssignment = new HorizontalCoordinateAssignment<>(
      //              layersArray, svGraph, new HashSet<>(), horizontalOffset, verticalOffset);
      //      horizontalCoordinateAssignment.horizontalCoordinateAssignment();

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
    //    Function<V, Shape> vertexShapeFunction = vertexShapeFunction;
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
          maxHeight = (int) Math.max(maxHeight, bounds.height);
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

    // now all the vertices in layers (layersArray) have points associated with them
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
    //    EdgeShape.ArticulatedLine<V, E> edgeShape = new EdgeShape.ArticulatedLine<>();
    //    edgeShape.setEdgeArticulationFunction(
    //        e -> edgePointMap.getOrDefault(e, Collections.emptyList()));

    //    edgeShapeConsumer.accept(edgeShape);

    long articulatedEdgeTime = System.currentTimeMillis();
    log.trace("articulated edges took {}", (articulatedEdgeTime - pointsSetTime));

    svGraph.vertexSet().forEach(v -> layoutModel.set(v.getVertex(), v.getPoint()));
  }

  private static <V> Rectangle maxVertexBounds(
      LV<V>[][] layers, Function<V, Rectangle> vertexShapeFunction) {
    // figure out the largest rendered vertex
    Rectangle maxVertexBounds = Rectangle.IDENTITY;

    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        if (!(layers[i][j] instanceof Synthetic)) {
          Rectangle bounds = vertexShapeFunction.apply(layers[i][j].getVertex());
          int width = (int) Math.max(bounds.width, maxVertexBounds.width);
          int height = (int) Math.max(bounds.height, maxVertexBounds.height);
          maxVertexBounds = Rectangle.of(width, height);
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
    return Rectangle.of((int) w.getAverage(), (int) h.getAverage());
  }
}
