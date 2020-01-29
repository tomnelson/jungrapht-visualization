package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.*;
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
import org.jgrapht.Graphs;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.brandeskopf.Unaligned;
import org.jungrapht.visualization.layout.algorithms.sugiyama.ArticulatedEdge;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GraphLayers;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GreedyCycleRemoval;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaTransformedGraphSupplier;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticLV;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable portion of {@link SugiyamaLayoutAlgorithm}
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class SubEiglspergerRunnable<V, E> implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(SubEiglspergerRunnable.class);

  /**
   * a Builder to create a configured instance
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  public static class Builder<
      V, E, T extends SubEiglspergerRunnable<V, E>, B extends Builder<V, E, T, B>> {
    protected LayoutModel<V> layoutModel;
    protected RenderContext<V, E> renderContext;
    protected boolean straightenEdges;
    protected boolean postStraighten;

    /** {@inheritDoc} */
    protected B self() {
      return (B) this;
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
      return (T) new SubEiglspergerRunnable<>(this);
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
  Graph<LV<V>, LE<V, E>> svGraph;
  boolean stopit = false;
  protected Predicate<V> vertexPredicate;
  protected Predicate<E> edgePredicate;
  protected Comparator<V> vertexComparator;
  protected Comparator<E> edgeComparator;
  protected boolean straightenEdges;
  protected boolean postStraighten;

  private SubEiglspergerRunnable(Builder<V, E, ?, ?> builder) {
    this(
        builder.layoutModel,
        builder.renderContext,
        builder.straightenEdges,
        builder.postStraighten);
  }

  private SubEiglspergerRunnable(
      LayoutModel<V> layoutModel,
      RenderContext<V, E> renderContext,
      boolean straightenEdges,
      boolean postStraighten) {
    this.layoutModel = layoutModel;
    this.renderContext = renderContext;
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

    List<List<LV<V>>> layers = GraphLayers.assign(svGraph);
    long assignLayersTime = System.currentTimeMillis();
    log.trace("assign layers took {} ", (assignLayersTime - cycles));
    if (log.isTraceEnabled()) {
      GraphLayers.checkLayers(layers);
    }

    if (checkStopped()) {
      return;
    }

    EiglspergerSynthetics<V, E> synthetics = new EiglspergerSynthetics<>(svGraph);
    List<LE<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    LV<V>[][] layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);

    if (log.isTraceEnabled()) {
      GraphLayers.checkLayers(layersArray);
    }

    if (checkStopped()) {
      return;
    }

    // save off a map of edge lists keyed on the source vertex
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

    // save off a map of edge lists keyed on the target vertex
    Map<Integer, List<LE<V, E>>> edgesKeyedOnTarget = new LinkedHashMap<>();
    svGraph
        .edgeSet()
        .forEach(
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

    long syntheticsTime = System.currentTimeMillis();
    log.trace("synthetics took {}", (syntheticsTime - assignLayersTime));

    Map<LV<V>, Integer> pos = new HashMap<>();
    Map<LV<V>, Integer> measure = new HashMap<>();

    for (int i = 0; i < 6; i++) {
      if (i % 2 == 0) {
        //        log.trace("vertexCount:\n{}, edgeCount:\n{}, layersArray:\n{}", svGraph.vertexSet().size(), svGraph.edgeSet().size(), Arrays.toString(layersArray));
        sweepForward(svGraph, layersArray, edgesKeyedOnSource, pos, measure);
        SubEiglspergerUtil.check(layersArray);
      } else {
        sweepBackwards(svGraph, layersArray, edgesKeyedOnTarget, pos, measure);
        SubEiglspergerUtil.check(layersArray);
      }
    }
    //    sweepForward(svGraph, layersArray, edgesKeyedOnSource, pos, measure);
    //    log.info("FLayers: "+EiglspergerUtil.stringify(layersArray));
    ////    log.info("compactionGraph: {}", compactionGraph);
    ////    EiglspergerUtil.check(layersArray);
    //    sweepBackwards(svGraph, layersArray, edgesKeyedOnTarget, pos, measure);
    //    EiglspergerUtil.check(layersArray);
    //    log.info("RLayers: "+EiglspergerUtil.stringify(layersArray));

    // done optimizing for edge crossing
    LV<V>[][] best = layersArray;

    //    for (int i=0; i<best.length; i++) {
    //      for (int j=0; j<best[i].length; j++) {
    //        Arrays.sort(best[i], Comparator.comparingInt(v -> v.getIndex()));
    //        for (int idx=0; idx<best[i].length; idx++) {
    //          best[i][idx].setIndex(idx);
    //        }
    //      }
    //    }
    //    EiglspergerUtil.check(best);

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
    Map<LV<V>, Point> vertexPointMap = new HashMap<>();

    // update the indices of the all layers to
    for (int i = 0; i < best.length; i++) {
      for (int j = 0; j < best[i].length; j++) {
        best[i][j].setIndex(j);
      }
    }
    if (straightenEdges) {
      HorizontalCoordinateAssignment.horizontalCoordinateAssignment(
          best, svGraph, new HashSet<>(), horizontalOffset, verticalOffset, pos, measure);

      GraphLayers.checkLayers(best);

      for (int i = 0; i < best.length; i++) {
        for (int j = 0; j < best[i].length; j++) {
          LV<V> EiglspergerVertex = best[i][j];
          vertexPointMap.put(EiglspergerVertex, EiglspergerVertex.getPoint());
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
        LV<V> EiglspergerVertex = best[i][j];
        if (!(EiglspergerVertex instanceof SyntheticLV)) {
          Rectangle bounds = vertexShapeFunction.apply(EiglspergerVertex.getVertex()).getBounds();
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
        LV<V> EiglspergerVertex = best[i][j];
        int vertexWidth = 0;
        if (!(EiglspergerVertex instanceof SyntheticLV)) {
          vertexWidth = vertexShapeFunction.apply(EiglspergerVertex.getVertex()).getBounds().width;
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
    //    log.trace("setting points took {}", (pointsSetTime - crossCountTests));

    // now all the vertices in layers (best) have points associated with them
    // every vertex in vertexMap has a point value

    svGraph.vertexSet().forEach(v -> v.setPoint(vertexPointMap.get(v)));

    //    if (postStraighten) synthetics.alignArticulatedEdges();
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

    Map<E, List<Point>> edgePointMap = new HashMap<>();
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
    EdgeShape.ArticulatedLine<V, E> edgeShape = new EdgeShape.ArticulatedLine<>();
    edgeShape.setEdgeArticulationFunction(
        e -> edgePointMap.getOrDefault(e, Collections.emptyList()));

    renderContext.setEdgeShapeFunction(edgeShape);

    long articulatedEdgeTime = System.currentTimeMillis();
    log.trace("articulated edges took {}", (articulatedEdgeTime - pointsSetTime));

    svGraph.vertexSet().forEach(v -> layoutModel.set(v.getVertex(), v.getPoint()));
  }

  public static <V, E> Graph<LV<V>, Integer> sweepForward(
      Graph<LV<V>, LE<V, E>> svGraph,
      LV<V>[][] layersArray,
      Map<Integer, List<LE<V, E>>> edgesKeyedOnSource,
      Map<LV<V>, Integer> pos,
      Map<LV<V>, Integer> measure) {

    log.info("sweepForward");
    //    EiglspergerUtil.check(layersArray);
    List<LV<V>> layerEye = null;

    // make the compaction graph
    Graph<LV<V>, Integer> compactionGraph =
        GraphTypeBuilder.<LV<V>, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    //    Map<LV<V>, Integer> pos = new HashMap<>();
    //
    //    Map<LV<V>, Integer> measure = new HashMap<>();

    for (int i = 0; i < layersArray.length - 1; i++) {
      List<LE<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
      if (layerEye == null) {
        layerEye =
            SubEiglspergerUtil.scan(
                SubEiglspergerUtil.createListOfVertices(layersArray[i])); // first rank
      }

      BiLayer<V, E> biLayer =
          BiLayer.of(
              i,
              i + 1,
              layerEye,
              SubEiglspergerUtil.createListOfVertices(layersArray[i + 1]),
              layersArray[i + 1],
              PVertex.class::isInstance,
              QVertex.class::isInstance,
              Graphs::predecessorListOf,
              pos,
              measure);

      //      EiglspergerUtil.check(layersArray);

      SubEiglspergerSteps.stepOne(biLayer);
      // handled PVertices by merging them into containers
      log.trace("stepOneOut:{}", biLayer.currentLayer);

      List<VirtualEdge<V, E>> virtualEdges = new ArrayList<>();

      SubEiglspergerSteps.stepTwo(biLayer, virtualEdges, svGraph);
      log.trace("stepTwoOut:{}", biLayer.downstreamLayer);

      SubEiglspergerSteps.stepThree(biLayer);
      log.trace("stepThreeOut:{}", biLayer.downstreamLayer);
      SubEiglspergerUtil.fixIndices(biLayer.downstreamLayer);

      SubEiglspergerSteps.stepFour(biLayer, virtualEdges);
      log.trace("stepFourOut:{}", biLayer.downstreamLayer);

      // i want the edges keyed on this rank, plus any virtual edges
      List<LE<V, E>> reducedEdges = new ArrayList<>();
      reducedEdges.addAll(edgesKeyedOnSource.getOrDefault(i + 1, Collections.emptyList()));
      reducedEdges.addAll(virtualEdges);

      SubEiglspergerSteps.stepFive(svGraph, true, biLayer, virtualEdges);
      log.trace("stepFiveOut:{}", biLayer.downstreamLayer);

      SubEiglspergerSteps.stepSix(biLayer);
      log.trace("stepSixOut:{}", biLayer.downstreamLayer);

      Arrays.sort(layersArray[i], Comparator.comparingInt(LV::getIndex));
      SubEiglspergerUtil.fixIndices(layersArray[i]);
      Arrays.sort(layersArray[i + 1], Comparator.comparingInt(LV::getIndex));
      SubEiglspergerUtil.fixIndices(layersArray[i + 1]);
      log.trace("XXXXXXX\n\n");
      layerEye = biLayer.downstreamLayer;
      //      EiglspergerUtil.check(layersArray);
      //      addLayerToCompactionGraph(biLayer.downstreamLayer, compactionGraph);
    }

    //    log.info("compactionGraph edges: {}", compactionGraph.edgeSet());
    //    compactionGraph
    //        .edgeSet()
    //        .forEach(
    //            e -> {
    //              log.info(
    //                  "{} -> {}", compactionGraph.getEdgeSource(e), compactionGraph.getEdgeTarget(e));
    //            });
    //    List<LV<V>> roots =
    //        compactionGraph
    //            .vertexSet()
    //            .stream()
    //            .filter(v -> compactionGraph.inDegreeOf(v) == 0)
    //            .collect(Collectors.toList());
    //    log.info("roots are {}", roots);
    return compactionGraph;
  }

  public static <V, E> Graph<LV<V>, Integer> sweepBackwards(
      Graph<LV<V>, LE<V, E>> svGraph,
      LV<V>[][] layersArray,
      Map<Integer, List<LE<V, E>>> edgesKeyedOnSource,
      Map<LV<V>, Integer> pos,
      Map<LV<V>, Integer> measure) {

    log.info("sweepBackwards");
    List<LV<V>> layerEye = null;
    //    EiglspergerUtil.check(layersArray);

    // make the compaction graph
    Graph<LV<V>, Integer> compactionGraph =
        GraphTypeBuilder.<LV<V>, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    for (int i = layersArray.length - 1; i > 0; i--) {
      List<LE<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
      if (layerEye == null) {
        layerEye =
            SubEiglspergerUtil.scan(
                SubEiglspergerUtil.createListOfVertices(layersArray[i])); // last rank
        for (LV<V> v : layerEye) {
          if (v instanceof SubContainer) {
            continue;
          }
          if (v instanceof SegmentVertex) {
            SegmentVertex<V> segmentVertex = (SegmentVertex<V>) v;
            compactionGraph.addVertex(segmentVertex.getSegment());
          } else {
            compactionGraph.addVertex(v);
          }
        }
      }

      BiLayer<V, E> biLayer =
          BiLayer.of(
              i,
              i - 1,
              layerEye,
              SubEiglspergerUtil.createListOfVertices(layersArray[i - 1]),
              layersArray[i - 1],
              QVertex.class::isInstance,
              PVertex.class::isInstance,
              Graphs::successorListOf,
              pos,
              measure);
      //      EiglspergerUtil.check(layersArray);

      SubEiglspergerSteps.stepOne(biLayer);
      // handled PVertices by merging them into containers
      log.trace("stepOneOut:{}", biLayer.currentLayer);

      List<VirtualEdge<V, E>> virtualEdges = new ArrayList<>();

      SubEiglspergerSteps.stepTwo(biLayer, virtualEdges, svGraph);
      log.trace("stepTwoOut:{}", biLayer.downstreamLayer);

      SubEiglspergerSteps.stepThree(biLayer);
      log.trace("stepThreeOut:{}", biLayer.downstreamLayer);
      SubEiglspergerUtil.fixIndices(biLayer.downstreamLayer);

      SubEiglspergerSteps.stepFour(biLayer, virtualEdges);
      log.trace("stepFourOut:{}", biLayer.downstreamLayer);

      // i want the edges keyed on this rank, plus any virtual edges
      List<LE<V, E>> reducedEdges = new ArrayList<>();
      reducedEdges.addAll(edgesKeyedOnSource.getOrDefault(i + 1, Collections.emptyList()));
      reducedEdges.addAll(virtualEdges);

      SubEiglspergerSteps.stepFive(svGraph, false, biLayer, virtualEdges);
      log.trace("stepFiveOut:{}", biLayer.downstreamLayer);

      SubEiglspergerSteps.stepSix(biLayer);
      log.trace("stepSixOut:{}", biLayer.downstreamLayer);

      Arrays.sort(layersArray[i], Comparator.comparingInt(LV::getIndex));
      SubEiglspergerUtil.fixIndices(layersArray[i]);
      Arrays.sort(layersArray[i - 1], Comparator.comparingInt(LV::getIndex));
      SubEiglspergerUtil.fixIndices(layersArray[i - 1]);
      log.trace("XXXXXXX\n\n");
      layerEye = biLayer.downstreamLayer;
      //      previousLayer = stepSixOut;
      //      EiglspergerUtil.check(layersArray);
      //      addLayerToCompactionGraph(biLayer.downstreamLayer, compactionGraph);
    }
    return compactionGraph;
  }

  private static <V, E> void addLayerToCompactionGraph(
      List<LV<V>> layer, Graph<LV<V>, Integer> compactionGraph) {
    // add edges betwee the 2 layers
    LV<V> previous = null;
    for (LV<V> v : layer) {
      if (v instanceof SubContainer) {
        SubContainer<V, Segment<V>> container = (SubContainer<V, Segment<V>>) v;
        if (container.size() == 0) {
          continue;
        }
        Segment<V> max = container.max().key;
        if (!(previous instanceof Segment)) {
          compactionGraph.addVertex(previous);
          compactionGraph.addVertex(max);
          compactionGraph.addEdge(previous, max);
        }
        previous = max;
        continue;
      }
      if (v instanceof SegmentVertex) {
        SegmentVertex<V> segmentVertex = (SegmentVertex<V>) v;
        Segment<V> segment = segmentVertex.getSegment();
        compactionGraph.addVertex(segment);
        if (previous != null) {
          compactionGraph.addVertex(previous);
          compactionGraph.addVertex(segment);
          compactionGraph.addEdge(previous, segment);
        }
        previous = segmentVertex;
      } else {
        compactionGraph.addVertex(v);
        if (previous != null && previous != v) {
          if (previous instanceof SegmentVertex) {
            compactionGraph.addVertex(((SegmentVertex<V>) previous).getSegment());
            compactionGraph.addVertex(v);
            compactionGraph.addEdge(((SegmentVertex<V>) previous).getSegment(), v);
          } else {
            compactionGraph.addVertex(previous);
            compactionGraph.addVertex(v);
            compactionGraph.addEdge(previous, v);
          }
        }
        previous = v;
      }
    }
    //    log.info("compactionGraph: {}", compactionGraph);
  }

  private static <V> Rectangle maxVertexBounds(
      List<List<LV<V>>> layers, Function<V, Shape> vertexShapeFunction) {
    // figure out the largest rendered vertex
    Rectangle maxVertexBounds = new Rectangle();

    for (List<LV<V>> list : layers) {
      for (LV<V> v : list) {
        if (!(v instanceof SyntheticLV)) {
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
        if (!(layers[i][j] instanceof SyntheticLV)) {
          Rectangle bounds = vertexShapeFunction.apply(layers[i][j].getVertex()).getBounds();
          w.accept(bounds.width);
          h.accept(bounds.height);
        }
      }
    }
    return new Rectangle((int) w.getAverage(), (int) h.getAverage());
  }
}
