package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SugiyamaRunnable<V, E> implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(SugiyamaRunnable.class);

  final LayoutModel<V> layoutModel;
  final RenderContext<V, E> renderContext;
  Graph<V, E> graph;
  Graph<SV<V>, SE<V, E>> svGraph;
  boolean stopit = false;

  public SugiyamaRunnable(LayoutModel<V> layoutModel, RenderContext<V, E> renderContext) {
    this.layoutModel = layoutModel;
    this.renderContext = renderContext;
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
  /**
   * When an object implementing interface <code>Runnable</code> is used to create a thread,
   * starting the thread causes the object's <code>run</code> method to be called in that separately
   * executing thread.
   *
   * <p>The general contract of the method <code>run</code> is that it may take any action
   * whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    this.graph = layoutModel.getGraph();

    long startTime = System.currentTimeMillis();
    SVTransformedGraphSupplier<V, E> transformedGraphSupplier =
        new SVTransformedGraphSupplier<>(graph);
    this.svGraph = transformedGraphSupplier.get();
    long transformTime = System.currentTimeMillis();
    System.err.println("transform Graph took " + (transformTime - startTime));

    if (checkStopped()) {
      return;
    }
    RemoveCycles<SV<V>, SE<V, E>> removeCycles = new RemoveCycles<>(svGraph);
    svGraph = removeCycles.removeCycles();
    long cycles = System.currentTimeMillis();
    System.err.println("remove cycles took " + (cycles - transformTime));

    AssignLayers<V, E> assignLayers = new AssignLayers<>(svGraph);

    List<List<SV<V>>> layers = assignLayers.assignLayers();
    long assignLayersTime = System.currentTimeMillis();
    System.err.println("assign layers took " + (assignLayersTime - cycles));
    if (log.isTraceEnabled()) {
      AssignLayers.checkLayers(layers);
    }

    if (checkStopped()) {
      return;
    }

    //    log.info("assign layers:");
    //    for (List<SV<V>> layer : layers) {
    //      log.info("Layer: {}", layer);
    //    }
    //    log.info("virtual vertices and edges:");
    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
    List<SE<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    //    log.info("there are {} edges ", edges.size());
    //    log.info("edges: {}", edges);
    layers = synthetics.createVirtualVerticesAndEdges(edges, layers);

    if (checkStopped()) {
      return;
    }

    long syntheticsTime = System.currentTimeMillis();
    System.err.println("synthetics took " + (syntheticsTime - assignLayersTime));

    List<List<SV<V>>> best = null;
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
    System.err.println("cross counts took " + (crossCountTests - syntheticsTime));

    Map<SV<V>, SV<V>> vertexMap = new HashMap<>();
    for (List<SV<V>> layer : best) {
      for (SV<V> sv : layer) {
        vertexMap.put(sv, sv);
      }
    }

    int horizontalOffset = Integer.getInteger(PREFIX + "sugiyama.horizontal.offset", 50);
    int verticalOffset = Integer.getInteger(PREFIX + "sugiyama.vertical.offset", 50);
    Map<Integer, Integer> rowWidthMap = new HashMap<>();
    Map<Integer, Integer> rowMaxHeightMap = new HashMap<>();
    int layerIndex = 0;
    Function<V, Shape> vertexShapeFunction = renderContext.getVertexShapeFunction();
    for (List<SV<V>> layer : best) {
      int width = horizontalOffset;
      int maxHeight = 0;
      for (SV<V> sv : layer) {
        if (!(sv instanceof SyntheticVertex)) {
          Rectangle bounds = vertexShapeFunction.apply(sv.vertex).getBounds();
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
    log.info("layerMaxHeights {}", rowMaxHeightMap);
    for (List<SV<V>> layer : best) {
      int previousVertexWidth = 0;
      // offset against widest row
      x += (widestRowWidth - rowWidthMap.get(layerIndex)) / 2;

      y += rowMaxHeightMap.get(layerIndex) / 2;
      if (layerIndex > 0) {
        y += rowMaxHeightMap.get(layerIndex - 1) / 2;
      }

      for (SV<V> sv : layer) {
        int vertexWidth = 0;
        if (!(sv instanceof SyntheticVertex)) {
          vertexWidth = vertexShapeFunction.apply(sv.vertex).getBounds().width;
        }
        x += previousVertexWidth / 2 + vertexWidth / 2 + horizontalOffset;

        log.info("layerIndex {} y is {}", layerIndex, y);
        sv.setPoint(Point.of(x, y));

        if (vertexMap.containsKey(sv)) {
          vertexMap.get(sv).setPoint(sv.getPoint());
        }
        previousVertexWidth = vertexWidth;
      }
      x = horizontalOffset;
      y += verticalOffset;
      layerIndex++;
    }

    long pointsSetTime = System.currentTimeMillis();
    System.err.println("setting points took " + (pointsSetTime - crossCountTests));

    // now all the vertices in layers (best) have points associated with them
    // every vertex in vertexMap has a point value

    for (SV<V> v : svGraph.vertexSet()) {
      SV<V> sv2 = vertexMap.get(v);
      if (sv2 != null) {
        Point p = sv2.getPoint();
        v.setPoint(p);
      } else {
        log.error("got null");
      }
    }

    List<ArticulatedEdge<V, E>> articulatedEdges = synthetics.makeArticulatedEdges();

    for (ArticulatedEdge<V, E> ae : articulatedEdges) {
      for (SV<V> sv : ae.getIntermediateVertices()) {

        sv.setPoint(vertexMap.get(sv).getPoint());
      }
    }

    Map<E, List<Point>> edgePointMap = new HashMap<>();
    for (ArticulatedEdge<V, E> ae : articulatedEdges) {
      List<Point> points = new ArrayList<>();
      points.add(ae.source.getPoint());
      points.addAll(ae.getIntermediatePoints());
      points.add(ae.target.getPoint());
      edgePointMap.put(ae.edge, points);
    }
    //    articulatedEdges.forEach(ae -> edgePointMap.put(ae.edge, ae.getIntermediatePoints()));
    EdgeShape.ArticulatedLine<V, E> edgeShape = new EdgeShape.ArticulatedLine<>();
    edgeShape.setEdgeArticulationFunction(
        e -> edgePointMap.getOrDefault(e, Collections.emptyList()));

    renderContext.setEdgeShapeFunction(edgeShape);

    long articulatedEdgeTime = System.currentTimeMillis();
    System.err.println("articulated edges took " + (articulatedEdgeTime - pointsSetTime));

    //    svGraph.vertexSet().forEach(v -> layoutModel.set(v.vertex, v.getPoint()));

    for (SV<V> v : svGraph.vertexSet()) {
      if (v == null) {
        log.error("whoa");
      }
      if (v.vertex == null) {
        log.error("whoa");
      }
      if (v.getPoint() == null) {
        log.error("whoa");
      }
      layoutModel.set(v.vertex, v.getPoint());
    }
  }

  private void transpose(List<List<SV<V>>> ranks, List<SE<V, E>> edges) {
    if (log.isTraceEnabled()) {
      checkLayers(ranks);
    }

    boolean improved = true;
    int improvements = 0;
    int lastImprovements = 0;
    int sanityLimit = Integer.getInteger(PREFIX + "sugiyama.transpose.limit", 10);
    int sanityCheck = 0;
    while (improved) {
      //      log.trace("{} improvements so far", improvements);
      improvements = 0;
      improved = false;
      for (int i = 0; i < ranks.size(); i++) {
        List<SV<V>> rank = ranks.get(i);
        for (int j = 0; j < rank.size() - 1; j++) {
          SV<V> v = rank.get(j);
          SV<V> w = rank.get(j + 1);
          int vw = crossing(v, w, edges);
          int wv = crossing(w, v, edges);
          if (vw > wv) {
            // are the indices good going in?
            if (v.getIndex() != rank.indexOf(v)) {
              log.error("wrong index already");
            }
            if (w.getIndex() != rank.indexOf(w)) {
              log.error("wrong index already");
            }
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

  private void checkLayers(List<List<SV<V>>> layers) {
    for (int i = 0; i < layers.size(); i++) {
      List<SV<V>> layer = layers.get(i);
      log.trace("layer: {}", layer);
      for (int j = 0; j < layer.size(); j++) {
        SV<V> sv = layer.get(j);
        log.trace("sv {},{}: {}", i, j, sv);
        assert i == sv.getRank();
        if (j != sv.getIndex()) {
          log.error("j = {} and index = {}", j, sv.getIndex());
        }
        assert j == sv.getIndex();
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

  int crossing(SV<V> v, SV<V> w, List<SE<V, E>> edges) {

    List<Integer> targetIndices = new LinkedList<>();
    // get all edges that start with v
    //    edges
    //        .stream()
    //        .filter(e -> e.source.equals(v))
    //        .map(e -> e.target.getIndex())
    //        .forEach(targetIndices::add);
    for (SE<V, E> edge : edges) {
      if (edge.source.equals(v) || edge.source.equals(w)) {
        targetIndices.add(edge.target.getIndex());
      }
    }

    //    edges
    //        .stream()
    //        .filter(e -> e.source.equals(w))
    //        .map(e -> e.target.getIndex())
    //        .forEach(targetIndices::add);

    return this.insertionSortCounter(targetIndices);
  }
  /*
    private void transpose(rank)
  2. improved = True;
  3. while improved do
            4. improved = False;
  5. for r = 0 to Max_rank do
            6. for i = 0 to  rank[r] -2 do
            7. v = rank[r][i];
  8. w = rank[r][i+1];
  9. if crossing(v,w) > crossing(w,v) then
  10. improved = True;
  11. exchange(rank[r][i],rank[r][i+1]);
  12. endif
  13. end
  14. end
  15. end
  16. end
  */
  //http://www.graphviz.org/Documentation/TSE93.pdf p 15
  void median(List<List<SV<V>>> layers, int i, Graph<SV<V>, SE<V, E>> svGraph) {
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

  private void position(
      List<SV<V>> layer, List<SV<V>> previousLayer, Graph<SV<V>, SE<V, E>> svGraph) {
    Map<SV<V>, Integer> pos = new HashMap<>();
    for (SV<V> v : layer) {
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
          if (!pos.containsKey(a) || !pos.containsKey(b) || pos.get(a) == pos.get(b)) return 0;
          if (pos.get(a) < pos.get(b)) return -1;
          return 1;
        });
    // update the indices of the layer
    for (int i = 0; i < layer.size(); i++) {
      layer.get(i).setIndex(i);
    }
    if (checkStopped()) {
      return;
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
  int[] adjacentPositions(SV<V> v, List<SV<V>> previousLayer, Graph<SV<V>, SE<V, E>> svGraph) {
    List<SV<V>> predecessors = Graphs.predecessorListOf(svGraph, v);
    // sanity check:
    //    for (SV<V> p : predecessors) {
    //      assert previousLayer.indexOf(p) == p.getIndex();
    //    }
    // end sanity check
    //    return predecessors
    //        .stream()
    //        .map(p -> p.getIndex())
    //        //        .map(p -> previousLayer.indexOf(p))
    //        .filter(idx -> idx != -1)
    //        .mapToInt(i -> i)
    //        .toArray();
    List<Integer> indexList = new ArrayList<>();
    for (SV<V> p : predecessors) {
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

  List<List<SV<V>>> copy(List<List<SV<V>>> in) {
    List<List<SV<V>>> copy = new ArrayList<>();
    for (List<SV<V>> list : in) {
      LinkedList<SV<V>> ll = new LinkedList<>();
      for (SV<V> sv : list) {
        if (sv instanceof SyntheticVertex) {
          ll.add(new SyntheticVertex<>((SyntheticVertex) sv));
        } else {
          ll.add(new SV<>(sv));
        }
      }
      copy.add(ll);
    }
    return copy;
  }
}
