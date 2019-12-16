package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GraphLayers;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GreedyCycleRemoval;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaEdge;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaTransformedGraphSupplier;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaVertex;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticSugiyamaVertex;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Synthetics;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrandesKopf<V, E> {

  private static final Logger log = LoggerFactory.getLogger(BrandesKopf.class);
  Graph<V, E> originalGraph;
  List<List<SugiyamaVertex<V>>> layers;
  Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph;
  Set<SugiyamaEdge<V, E>> markedSegments = new HashSet<>();

  public BrandesKopf(Graph<V, E> originalGraph) {
    this.originalGraph = originalGraph;
    this.svGraph = new SugiyamaTransformedGraphSupplier<>(originalGraph).get();
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

    this.layers = GraphLayers.assign(svGraph);
    if (log.isTraceEnabled()) {
      GraphLayers.checkLayers(layers);
    }

    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
    List<SugiyamaEdge<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    this.layers = synthetics.createVirtualVerticesAndEdges(edges, layers);
  }

  void preprocessing() {
    int h = layers.size();
    //    for (int i = 2; i < h - 2; i++) {
    for (int i = 1; i < h - 1 - 2; i++) { // zero based

      int k0 = 0;
      int el = 1;
      List<SugiyamaVertex<V>> thisLayer = layers.get(i); // Li
      List<SugiyamaVertex<V>> nextLayer = layers.get(i + 1); // Li+1
      //      for (int el1 = 1; el1 <= nextLayer.size(); el1++) {
      for (int el1 = 0; el1 < nextLayer.size(); el1++) { // zero based
        // get the vertex at next layer index el1
        SugiyamaVertex<V> vel1nextLayer = nextLayer.get(el1);
        if (el1 == nextLayer.size() || vel1nextLayer instanceof SyntheticSugiyamaVertex) {
          int k1 = thisLayer.size();
          if (vel1nextLayer instanceof SyntheticSugiyamaVertex) {
            Optional<SugiyamaEdge<V, E>> incomingEdgeOpt =
                svGraph.incomingEdgesOf(vel1nextLayer).stream().findFirst();
            if (incomingEdgeOpt.isPresent()) {
              SugiyamaEdge<V, E> incomingEdge = incomingEdgeOpt.get();
              SugiyamaVertex<V> upperNeighbor = svGraph.getEdgeSource(incomingEdge);
              k1 = upperNeighbor.getIndex();
            }
          }
          while (el <= el1) {
            for (SugiyamaVertex<V> upperNeighbor : getUpperNeighbors(svGraph, vel1nextLayer)) {
              int k = upperNeighbor.getIndex();
              if (k < k0 || k > k1) {
                markedSegments.add(
                    svGraph.getEdge(layers.get(i).get(k), layers.get(el).get(i + 1)));
              }
            }
            el++;
          }
          k0 = k1;
        }
      }
    }
  }

  List<SugiyamaVertex<V>> getUpperNeighbors(
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph, SugiyamaVertex<V> v) {
    return graph
        .incomingEdgesOf(v)
        .stream()
        .map(e -> graph.getEdgeSource(e))
        .collect(Collectors.toList());
  }

  Map<SugiyamaVertex<V>, SugiyamaVertex<V>> root = new HashMap<>();
  Map<SugiyamaVertex<V>, SugiyamaVertex<V>> align = new HashMap<>();

  public void verticalAlignmentUpperLeft() {
    // initialize root and align
    layers
        .stream()
        .flatMap(Collection::stream)
        .forEach(
            v -> {
              root.put(v, v);
              align.put(v, v);
            });
    //    for (int i=1; i< layers.size(); i++) {
    for (int i = 0; i < layers.size() - 1; i++) { // zero based
      int r = 0;
      List<SugiyamaVertex<V>> currentLayer = layers.get(i);
      //      for (int k=1; k <= currentLayer.size(); k++) {
      for (int k = 0; k <= currentLayer.size() - 1; k++) { // zero based
        // if the vertex at k has source nodes
        SugiyamaVertex<V> vkofi = currentLayer.get(k);
        List<SugiyamaVertex<V>> upperNeighbors = Graphs.predecessorListOf(svGraph, vkofi);
        upperNeighbors.sort(Comparator.comparingInt(n -> n.getIndex()));
        int d = svGraph.inDegreeOf(vkofi);
        if (d > 0) {
          int floor = (int) Math.floor((d - 1) / 2.0); // zero based
          int ceil = (int) Math.ceil((d - 1) / 2.0); // zero based
          for (int m : new LinkedHashSet<>(Arrays.asList(floor, ceil))) {
            //                  Arrays.asList(floor, ceil)) {
            if (align.get(vkofi).equals(vkofi)) {
              SugiyamaVertex<V> um = upperNeighbors.get(m);
              // if edge vm->v is not marked
              SugiyamaEdge<V, E> edge = svGraph.getEdge(um, vkofi);
              if (!markedSegments.contains(edge)
                  && // not marked
                  r < um.getIndex()) {
                align.put(um, vkofi);
                root.put(vkofi, root.get(um));
                align.put(vkofi, root.get(vkofi));
                r = um.getIndex();
              }
            }
          }
        }
      }
    }
  }

  void verticalAlignmentLowerLeft() {
    // initialize root and align
    layers
        .stream()
        .flatMap(Collection::stream)
        .forEach(
            v -> {
              root.put(v, v);
              align.put(v, v);
            });
    //    for (int i=1; i< layers.size(); i++) {
    for (int i = 0; i < layers.size() - 1; i++) { // zero based
      int r = 0;
      List<SugiyamaVertex<V>> currentLayer = layers.get(i);
      //      for (int k=1; k <= currentLayer.size(); k++) {
      for (int k = 0; k <= currentLayer.size() - 1; k++) { // zero based
        // if the vertex at k has source nodes
        SugiyamaVertex<V> vkofi = currentLayer.get(k);
        List<SugiyamaVertex<V>> lowerNeighbors = Graphs.successorListOf(svGraph, vkofi);
        lowerNeighbors.sort(Comparator.comparingInt(n -> n.getIndex()));
        int d = svGraph.inDegreeOf(vkofi);
        if (d > 0) {
          int floor = (int) Math.floor((d - 1) / 2.0); // zero based
          int ceil = (int) Math.ceil((d - 1) / 2.0); // zero based
          for (int m : Arrays.asList(floor, ceil)) {
            if (align.get(vkofi).equals(vkofi)) {
              SugiyamaVertex<V> um = lowerNeighbors.get(m);
              // if edge vm->v is not marked
              SugiyamaEdge<V, E> edge = svGraph.getEdge(um, vkofi);
              if (!markedSegments.contains(edge)
                  && // not marked
                  r < um.getIndex()) {
                align.put(um, vkofi);
                root.put(vkofi, root.get(um));
                align.put(vkofi, root.get(vkofi));
                r = um.getIndex();
              }
            }
          }
        }
      }
    }
  }

  int delta = 20; //delta is some minimum separation value
  Map<SugiyamaVertex<V>, SugiyamaVertex<V>> sink = new HashMap<>();
  Map<SugiyamaVertex<V>, Integer> shift = new HashMap<>();
  Map<SugiyamaVertex<V>, Integer> x = new HashMap<>();

  void placeBlock(SugiyamaVertex<V> v) {
    if (!x.containsKey(v)) {
      x.put(v, 0);
      SugiyamaVertex<V> w = v;
      do {
        // if pos[w] > 0
        if (w.getIndex() > 0) {
          // u gets root[pred[w]]
          SugiyamaVertex<V> predecessorOfW = layers.get(w.getRank()).get(w.getIndex() - 1);
          SugiyamaVertex<V> u = root.get(predecessorOfW);
          placeBlock(u);
          if (sink.get(v).equals(v)) {
            sink.put(v, sink.get(u));
          }
          // if (sink[v] != sink[u]
          if (!sink.get(v).equals(sink.get(u))) {
            // shift[sink[u]] <- min({shift[sink[u]], x[v]-x[u]-delta}
            shift.put(sink.get(u), Math.min(shift.get(sink.get(u)), x.get(v) - x.get(u) - delta));
          } else {
            // x[v] <- max{x[v], x[u] + delta}
            x.put(v, Math.max(x.get(v), x.get(u) + delta));
            log.info("x.get(v) is {} and v is {}", x.get(v), v);
          }
        }
        w = align.get(w);
      } while (!w.equals(v));
    }
  }

  public void horizontalCompaction() {
    layers
        .stream()
        .flatMap(Collection::stream)
        .forEach(
            v -> {
              sink.put(v, v);
              shift.put(v, Integer.MAX_VALUE);
            });
    layers
        .stream()
        .flatMap(Collection::stream)
        .filter(v -> root.get(v).equals(v))
        .forEach(this::placeBlock);

    for (List<SugiyamaVertex<V>> list : layers) {
      for (SugiyamaVertex<V> v : list) {
        // x[v] <- x[root[v]]
        x.put(v, x.get(root.get(v)));
        if (shift.get(sink.get(root.get(v))) < Integer.MAX_VALUE) {
          // x[v] <- x[v] + shift[sink[root[v]]]
          x.put(v, x.get(v) + shift.get(sink.get(root.get(v))));
        }
      }
    }
  }

  public List<List<SugiyamaVertex<V>>> sortX() {
    List<List<SugiyamaVertex<V>>> sorted = new ArrayList<>();
    for (int i = 0; i < layers.size(); i++) {
      sorted.add(new ArrayList<>());
    }
    for (Map.Entry<SugiyamaVertex<V>, Integer> entry : x.entrySet()) {
      SugiyamaVertex<V> key = entry.getKey();
      int value = entry.getValue();
      key.setPoint(Point.of(x.get(key), key.getRank() * 4));
      sorted.get(key.getRank()).add(key);
    }
    for (List<SugiyamaVertex<V>> list : sorted) {
      list.sort(Comparator.comparing(v -> x.get(v)));
    }
    return sorted;
  }

  public void horizontalCoordinateAssignment() {
    preprocessing();
    VerticalAlignment.LeftmostLower<V, E> one =
        new VerticalAlignment.LeftmostLower<>(layers, svGraph, markedSegments);
    HorizontalCompaction<V> hcOne =
        new HorizontalCompaction<>(layers, one.rootMap, one.alignMap, 20, 20);

    VerticalAlignment.LeftmostUpper<V, E> two =
        new VerticalAlignment.LeftmostUpper<>(layers, svGraph, markedSegments);
    HorizontalCompaction<V> hcTwo =
        new HorizontalCompaction<>(layers, two.rootMap, two.alignMap, 20, 20);

    VerticalAlignment.RightmostLower<V, E> three =
        new VerticalAlignment.RightmostLower<>(layers, svGraph, markedSegments);
    HorizontalCompaction<V> hcThree =
        new HorizontalCompaction<>(layers, three.rootMap, three.alignMap, 20, 20);

    VerticalAlignment.RightmostUpper<V, E> four =
        new VerticalAlignment.RightmostUpper<>(layers, svGraph, markedSegments);
    HorizontalCompaction<V> hcFour =
        new HorizontalCompaction<>(layers, four.rootMap, four.alignMap, 20, 20);

    for (List<SugiyamaVertex<V>> list : layers) {
      for (SugiyamaVertex<V> v : list) {
        Point pointOne = Point.of(hcOne.x.get(v), hcOne.y.get(v));
        Point pointTwo = Point.of(hcTwo.x.get(v), hcTwo.y.get(v));
        Point pointThree = Point.of(hcThree.x.get(v), hcThree.y.get(v));
        Point pointFour = Point.of(hcFour.x.get(v), hcFour.y.get(v));

        Point balancedPoint =
            AverageMedian.averageMedianPoint(pointOne, pointTwo, pointThree, pointFour);
        v.setPoint(balancedPoint);
      }
    }
  }
}
