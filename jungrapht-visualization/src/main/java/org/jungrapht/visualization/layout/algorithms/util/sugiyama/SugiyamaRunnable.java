package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
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

  private SugiyamaRunnable(Builder<V, E, ?, ?> builder) {
    this(
        builder.layoutModel,
        builder.renderContext,
        builder.vertexPredicate,
        builder.edgePredicate,
        builder.vertexComparator,
        builder.edgeComparator,
        builder.straightenEdges);
  }

  private SugiyamaRunnable(
      LayoutModel<V> layoutModel,
      RenderContext<V, E> renderContext,
      Predicate<V> vertexPredicate,
      Predicate<E> edgePredicate,
      Comparator<V> vertexComparator,
      Comparator<E> edgeComparator,
      boolean straightenEdges) {
    this.layoutModel = layoutModel;
    this.renderContext = renderContext;
    this.vertexComparator = vertexComparator;
    this.vertexPredicate = vertexPredicate;
    this.edgeComparator = edgeComparator;
    this.edgePredicate = edgePredicate;
    this.straightenEdges = straightenEdges;
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
    if (log.isTraceEnabled()) {
      GraphLayers.checkLayers(layers);
    }

    if (checkStopped()) {
      return;
    }

    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
    List<SugiyamaEdge<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    layers = synthetics.createVirtualVerticesAndEdges(edges, layers);

    if (checkStopped()) {
      return;
    }

    long syntheticsTime = System.currentTimeMillis();
    log.trace("synthetics took {}", (syntheticsTime - assignLayersTime));

    List<List<SugiyamaVertex<V>>> best = null;
    int lowestCrossCount = Integer.MAX_VALUE;
    int maxLevelCross = Integer.getInteger(PREFIX + "sugiyama.max.level.cross", 23);
    // order the ranks
    for (int i = 0; i < maxLevelCross; i++) {
      median(layers, i, svGraph);
      transpose(layers, edges);
      AllLevelCross<V, E> allLevelCross = new AllLevelCross<>(svGraph, layers);
      int allLevelCrossCount = allLevelCross.allLevelCross();
      if (allLevelCrossCount < lowestCrossCount) {
        best = copy(layers);
        lowestCrossCount = allLevelCrossCount;
      }
      if (checkStopped()) {
        return;
      }
    }

    // in case zero iterations of cross counting were requested:
    if (best == null) {
      best = layers;
    }

    long crossCountTests = System.currentTimeMillis();
    log.trace("cross counts took {}", (crossCountTests - syntheticsTime));

    Map<SugiyamaVertex<V>, SugiyamaVertex<V>> vertexMap = new HashMap<>();
    for (List<SugiyamaVertex<V>> layer : best) {
      for (SugiyamaVertex<V> sugiyamaVertex : layer) {
        vertexMap.put(sugiyamaVertex, sugiyamaVertex);
      }
    }

    int horizontalOffset = Integer.getInteger(PREFIX + "sugiyama.horizontal.offset", 50);
    int verticalOffset = Integer.getInteger(PREFIX + "sugiyama.vertical.offset", 50);
    Map<Integer, Integer> rowWidthMap = new HashMap<>();
    Map<Integer, Integer> rowMaxHeightMap = new HashMap<>();
    int layerIndex = 0;
    Function<V, Shape> vertexShapeFunction = renderContext.getVertexShapeFunction();
    int totalHeight = 0;
    int totalWidth = 0;
    for (List<SugiyamaVertex<V>> layer : best) {
      int width = horizontalOffset;
      int maxHeight = 0;
      for (SugiyamaVertex<V> sugiyamaVertex : layer) {
        if (!(sugiyamaVertex instanceof SyntheticVertex)) {
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
    int x = 0; //horizontalOffset;
    int y = verticalOffset;
    layerIndex = 0;
    log.trace("layerMaxHeights {}", rowMaxHeightMap);
    for (List<SugiyamaVertex<V>> layer : best) {
      int previousVertexWidth = 0;
      // offset against widest row
      x += (widestRowWidth - rowWidthMap.get(layerIndex)) / 2;

      y += rowMaxHeightMap.get(layerIndex) / 2;
      if (layerIndex > 0) {
        y += rowMaxHeightMap.get(layerIndex - 1) / 2;
      }

      int rowWidth = 0;
      for (SugiyamaVertex<V> sugiyamaVertex : layer) {
        int vertexWidth = 0;
        if (!(sugiyamaVertex instanceof SyntheticVertex)) {
          vertexWidth = vertexShapeFunction.apply(sugiyamaVertex.vertex).getBounds().width;
        }

        x += previousVertexWidth / 2 + vertexWidth / 2 + horizontalOffset;

        rowWidth = x + vertexWidth / 2;
        log.trace("layerIndex {} y is {}", layerIndex, y);
        sugiyamaVertex.setPoint(Point.of(x, y));

        if (vertexMap.containsKey(sugiyamaVertex)) {
          vertexMap.get(sugiyamaVertex).setPoint(sugiyamaVertex.getPoint());
        }
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

    svGraph
        .vertexSet()
        .forEach(
            v -> {
              SugiyamaVertex<V> sugiyamaVertex2 = vertexMap.get(v);
              if (sugiyamaVertex2 != null) {
                Point p = sugiyamaVertex2.getPoint();
                v.setPoint(p);
              } else {
                log.error("got null");
              }
            });

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

    if (straightenEdges) {
      // if the edges are to be straigntened, find the extreme compared to source/vertex
      for (ArticulatedEdge<V, E> ae : articulatedEdges) {
        // source x
        SugiyamaVertex<V> source = svGraph.getEdgeSource(ae);
        double sourceX = vertexMap.get(source).getPoint().x;
        SugiyamaVertex<V> target = svGraph.getEdgeTarget(ae);
        double targetX = vertexMap.get(target).getPoint().x;

        double maxX =
            ae.getIntermediateVertices()
                .stream()
                .mapToDouble(v -> vertexMap.get(v).getPoint().x)
                .max()
                .orElse(sourceX);
        double minX =
            ae.getIntermediateVertices()
                .stream()
                .mapToDouble(v -> vertexMap.get(v).getPoint().x)
                .max()
                .orElse(sourceX);
        List<SugiyamaVertex<V>> intermediateVertices = ae.getIntermediateVertices();
        List<Point> intermediatePoints = ae.getIntermediatePoints();

        double newX = maxX >= Math.max(sourceX, targetX) ? maxX : minX;
        for (int i = 0; i < intermediateVertices.size(); i++) {
          SugiyamaVertex<V> v = intermediateVertices.get(i);
          Point newPoint = Point.of(newX, vertexMap.get(v).getPoint().y);
          intermediatePoints.set(i, newPoint);
          v.setPoint(newPoint);
        }
        log.trace("intermediates now {}", ae.getIntermediateVertices());
      }
    } else {
      for (ArticulatedEdge<V, E> ae : articulatedEdges) {
        for (SugiyamaVertex<V> sugiyamaVertex : ae.getIntermediateVertices()) {
          sugiyamaVertex.setPoint(vertexMap.get(sugiyamaVertex).getPoint());
        }
      }
    }

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

  private void transpose(List<List<SugiyamaVertex<V>>> ranks, List<SugiyamaEdge<V, E>> edges) {
    if (log.isTraceEnabled()) {
      checkLayers(ranks);
    }

    boolean improved = true;
    int improvements = 0;
    int lastImprovements = 0;
    int sanityLimit = Integer.getInteger(PREFIX + "sugiyama.transpose.limit", 10);
    int sanityCheck = 0;
    while (improved) {
      improvements = 0;
      improved = false;
      for (List<SugiyamaVertex<V>> rank : ranks) {
        for (int j = 0; j < rank.size() - 1; j++) {
          SugiyamaVertex<V> v = rank.get(j);
          SugiyamaVertex<V> w = rank.get(j + 1);
          int vw = crossing(v, w, edges);
          int wv = crossing(w, v, edges);
          if (vw > wv) {
            //            // are the indices good going in?
            //            if (v.getIndex() != rank.indexOf(v)) {
            //              log.error("wrong index already");
            //            }
            //            if (w.getIndex() != rank.indexOf(w)) {
            //              log.error("wrong index already");
            //            }
            improved = true;
            improvements++;
            Collections.swap(rank, j, j + 1);

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
    if (log.isTraceEnabled()) {
      // check that all vertices have the right rank and index
      checkLayers(ranks);
    }
  }

  private void checkLayers(List<List<SugiyamaVertex<V>>> layers) {
    for (int i = 0; i < layers.size(); i++) {
      List<SugiyamaVertex<V>> layer = layers.get(i);
      log.trace("layer: {}", layer);
      for (int j = 0; j < layer.size(); j++) {
        SugiyamaVertex<V> sugiyamaVertex = layer.get(j);
        log.trace("sv {},{}: {}", i, j, sugiyamaVertex);
        assert i == sugiyamaVertex.getRank();
        if (j != sugiyamaVertex.getIndex()) {
          log.error("j = {} and index = {}", j, sugiyamaVertex.getIndex());
        }
        assert j == sugiyamaVertex.getIndex();
      }
    }
  }

  private int insertionSortCounter(List<Integer> list) {
    int counter = 0;
    for (int i = 1; i < list.size(); i++) {
      int value = list.get(i);
      int j = i - 1;
      while (j >= 0 && list.get(j) > value) {
        list.set(j + 1, list.get(j));
        counter++;
        j--;
      }
      list.set(j + 1, value);
    }
    return counter;
  }

  int crossing(SugiyamaVertex<V> v, SugiyamaVertex<V> w, List<SugiyamaEdge<V, E>> edges) {

    List<Integer> targetIndices = new LinkedList<>();
    for (SugiyamaEdge<V, E> edge : edges) {
      if (edge.source.equals(v) || edge.source.equals(w)) {
        targetIndices.add(edge.target.getIndex());
      }
    }
    return this.insertionSortCounter(targetIndices);
  }

  //http://www.graphviz.org/Documentation/TSE93.pdf p 15
  void median(
      List<List<SugiyamaVertex<V>>> layers,
      int i,
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph) {
    if (i % 2 == 0) {
      for (int r = 1; r < layers.size(); r++) {
        position(layers.get(r), layers.get(r - 1), svGraph);
      }
    } else {
      for (int r = layers.size() - 1; r > 0; r--) {
        position(layers.get(r), layers.get(r - 1), svGraph);
      }
    }
  }

  /**
   * for every vertex in a layer, place the vertex at the position of the median of the source
   * vertices for its incoming edges. TODO: Override to instead place the vertex at the same
   * position as the position of the source vertex of a 'favored' edge type or a 'favored' source
   * vertex (vertex/edge predicate)
   *
   * @param layer
   * @param previousLayer
   * @param svGraph
   */
  protected void position(
      List<SugiyamaVertex<V>> layer,
      List<SugiyamaVertex<V>> previousLayer,
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph) {
    Map<SugiyamaVertex<V>, Integer> pos = new HashMap<>();
    for (SugiyamaVertex<V> v : layer) {
      // for each v in my layer
      int[] adjacentPositions = adjacentPositions(v, previousLayer, svGraph);
      // get the median of adjacentPositions
      Arrays.sort(adjacentPositions);
      int median = adjacentPositions[adjacentPositions.length / 2];
      //SortingFunctions.quickSelectMedian(adjacentPositions);
      pos.put(v, median);
    }
    layer.sort(
        (a, b) -> {
          // if a or b is not a key in pos, return 0
          if (!pos.containsKey(a) || !pos.containsKey(b) || Objects.equals(pos.get(a), pos.get(b)))
            return 0;
          if (pos.get(a) < pos.get(b)) return -1;
          return 1;
        });
    // update the indices of the layer
    for (int i = 0; i < layer.size(); i++) {
      layer.get(i).setIndex(i);
    }
  }

  /**
   * get all sources of edges that end with v get each one's index in the level at 'adjacentLevel'
   * push onto a list (no -1 values)
   *
   * @param v
   * @param previousLayer // * @param edges
   * @return
   */
  int[] adjacentPositions(
      SugiyamaVertex<V> v,
      List<SugiyamaVertex<V>> previousLayer,
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph) {
    Predicate<SugiyamaEdge<V, E>> sePredicate = null;
    Predicate<SugiyamaVertex<V>> vPredicate = null;
    if (edgePredicate != null) {
      sePredicate = se -> edgePredicate.test(se.edge);
    }
    if (vertexPredicate != null) {
      vPredicate = sv -> vertexPredicate.test(sv.vertex);
    }
    List<SugiyamaVertex<V>> predecessors;
    if (sePredicate != null) {
      predecessors =
          svGraph
              .incomingEdgesOf(v)
              .stream()
              .filter(sePredicate)
              .map(svGraph::getEdgeSource)
              .collect(Collectors.toList());
    } else {
      predecessors = Graphs.predecessorListOf(svGraph, v);
    }

    if (vPredicate != null) {
      predecessors = predecessors.stream().filter(vPredicate).collect(Collectors.toList());
    }

    // sanity check:
    //    for (SV<V> p : predecessors) {
    //      assert previousLayer.indexOf(p) == p.getIndex();
    //    }
    // end sanity check
    List<Integer> indexList = new ArrayList<>();
    for (SugiyamaVertex<V> p : predecessors) {
      if (p.getIndex() != -1) {
        indexList.add(p.getIndex());
      }
    }
    int[] toReturn = new int[indexList.size()];
    int i = 0;
    for (Integer integer : indexList) {
      toReturn[i++] = integer;
    }
    return toReturn;
  }

  List<List<SugiyamaVertex<V>>> copy(List<List<SugiyamaVertex<V>>> in) {
    List<List<SugiyamaVertex<V>>> copy = new ArrayList<>();
    for (List<SugiyamaVertex<V>> list : in) {
      LinkedList<SugiyamaVertex<V>> ll = new LinkedList<>();
      for (SugiyamaVertex<V> sugiyamaVertex : list) {
        if (sugiyamaVertex instanceof SyntheticVertex) {
          ll.add(new SyntheticVertex<>((SyntheticVertex) sugiyamaVertex));
        } else {
          ll.add(new SugiyamaVertex<>(sugiyamaVertex));
        }
      }
      copy.add(ll);
    }
    return copy;
  }
}
