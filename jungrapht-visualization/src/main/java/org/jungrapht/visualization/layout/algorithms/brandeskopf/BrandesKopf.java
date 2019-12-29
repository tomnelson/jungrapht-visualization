package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
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
  //  List<List<SugiyamaVertex<V>>> layers;
  SugiyamaVertex<V>[][] layersArray;
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

    List<List<SugiyamaVertex<V>>> layers = GraphLayers.assign(svGraph);
    if (log.isTraceEnabled()) {
      GraphLayers.checkLayers(layers);
    }

    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
    List<SugiyamaEdge<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    this.layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);
  }

  void preprocessing(SugiyamaVertex<V>[][] layers) {
    int h = layers.length;
    //    for (int i = 2; i < h - 2; i++) {
    for (int i = 1; i < h - 1 - 2; i++) { // zero based

      int k0 = 0;
      int el = 1;
      SugiyamaVertex<V>[] thisLayer = layers[i]; // Li
      SugiyamaVertex<V>[] nextLayer = layers[i + 1]; // Li+1
      //      for (int el1 = 1; el1 <= nextLayer.size(); el1++) {
      for (int el1 = 0; el1 < nextLayer.length; el1++) { // zero based
        // get the vertex at next layer index el1
        SugiyamaVertex<V> vel1nextLayer = nextLayer[el1];
        if (el1 == nextLayer.length || vel1nextLayer instanceof SyntheticSugiyamaVertex) {
          int k1 = thisLayer.length;
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
                markedSegments.add(svGraph.getEdge(layers[i][k], layers[el][i + 1]));
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

  public void horizontalCoordinateAssignment() {
    preprocessing(layersArray);
    VerticalAlignment.LeftmostLower<V, E> one =
        new VerticalAlignment.LeftmostLower<>(layersArray, svGraph, markedSegments);
    HorizontalCompaction<V> hcOne =
        new HorizontalCompaction<>(layersArray, one.rootMap, one.alignMap, 20, 20);

    VerticalAlignment.LeftmostUpper<V, E> two =
        new VerticalAlignment.LeftmostUpper<>(layersArray, svGraph, markedSegments);
    HorizontalCompaction<V> hcTwo =
        new HorizontalCompaction<>(layersArray, two.rootMap, two.alignMap, 20, 20);

    VerticalAlignment.RightmostLower<V, E> three =
        new VerticalAlignment.RightmostLower<>(layersArray, svGraph, markedSegments);
    HorizontalCompaction<V> hcThree =
        new HorizontalCompaction<>(layersArray, three.rootMap, three.alignMap, 20, 20);

    VerticalAlignment.RightmostUpper<V, E> four =
        new VerticalAlignment.RightmostUpper<>(layersArray, svGraph, markedSegments);
    HorizontalCompaction<V> hcFour =
        new HorizontalCompaction<>(layersArray, four.rootMap, four.alignMap, 20, 20);

    for (int i = 0; i < layersArray.length; i++) {
      for (int j = 0; j < layersArray[i].length; j++) {
        SugiyamaVertex<V> v = layersArray[i][j];
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
