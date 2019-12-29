package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaEdge;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaVertex;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticSugiyamaVertex;
import org.jungrapht.visualization.layout.model.Point;

public class HorizontalCoordinateAssignment {

  public static <V, E> void horizontalCoordinateAssignment(
      SugiyamaVertex<V>[][] layers,
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph,
      Set<SugiyamaEdge<V, E>> markedSegments,
      int horizontalOffset,
      int verticalOffset) {
    preprocessing(layers, svGraph, markedSegments);
    VerticalAlignment.LeftmostUpper<V, E> upLeft =
        new VerticalAlignment.LeftmostUpper<>(layers, svGraph, markedSegments);
    HorizontalCompaction<V> upLeftCompaction =
        new HorizontalCompaction<>(
            layers, upLeft.getRootMap(), upLeft.getAlignMap(), horizontalOffset, verticalOffset);

    VerticalAlignment.RightmostUpper<V, E> upRight =
        new VerticalAlignment.RightmostUpper<>(layers, svGraph, markedSegments);
    HorizontalCompaction<V> upRightCompaction =
        new HorizontalCompaction<>(
            layers, upRight.getRootMap(), upRight.getAlignMap(), horizontalOffset, verticalOffset);

    VerticalAlignment.LeftmostLower<V, E> downLeft =
        new VerticalAlignment.LeftmostLower<>(layers, svGraph, markedSegments);
    HorizontalCompaction<V> downLeftCompaction =
        new HorizontalCompaction<>(
            layers,
            downLeft.getRootMap(),
            downLeft.getAlignMap(),
            horizontalOffset,
            verticalOffset);

    VerticalAlignment.RightmostLower<V, E> downRight =
        new VerticalAlignment.RightmostLower<>(layers, svGraph, markedSegments);
    HorizontalCompaction<V> downRightCompaction =
        new HorizontalCompaction<>(
            layers,
            downRight.getRootMap(),
            downRight.getAlignMap(),
            horizontalOffset,
            verticalOffset);

    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        SugiyamaVertex<V> v = layers[i][j];
        //    for (List<SugiyamaVertex<V>> list : layers) {
        //      for (SugiyamaVertex<V> v : list) {
        Point upLeftPoint = upLeftCompaction.getPoint(v).add(horizontalOffset, verticalOffset);
        Point upRightPoint = upRightCompaction.getPoint(v).add(horizontalOffset, verticalOffset);
        Point downLeftPoint = downLeftCompaction.getPoint(v).add(horizontalOffset, verticalOffset);
        Point downRightPoint =
            downRightCompaction.getPoint(v).add(horizontalOffset, verticalOffset);

        Point balancedPoint =
            AverageMedian.averageMedianPoint(
                upLeftPoint, upRightPoint, downLeftPoint, downRightPoint);
        v.setPoint(balancedPoint);
      }
    }
  }

  public static <V, E> void preprocessing(
      SugiyamaVertex<V>[][] layers,
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph,
      Set<SugiyamaEdge<V, E>> markedSegments) {
    int h = layers.length;
    // compares current row 'i' with 'i+1' row
    // i starts at row 1 and goes to row h-2-1
    //    for (int i = 2; i <= h - 2; i++) {
    for (int i = 1; i <= h - 2 - 1; i++) { // zero based

      int k0 = 0;
      int el = 0;
      SugiyamaVertex<V>[] thisLayer = layers[i]; // Li
      SugiyamaVertex<V>[] nextLayer = layers[i + 1]; // Li+1
      //      for (int el1 = 1; el1 <= nextLayer.size(); el1++) {
      for (int el1 = 0; el1 <= nextLayer.length - 1; el1++) { // zero based
        // get the vertex at next layer index el1
        SugiyamaVertex<V> vel1nextLayer = nextLayer[el1];
        //        SugiyamaVertex<V> velNextLayer = nextLayer.get(el);
        if (el1 == nextLayer.length - 1 || vel1nextLayer instanceof SyntheticSugiyamaVertex) {
          int k1 = thisLayer.length - 1;
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
            SugiyamaVertex<V> velNextLayer = nextLayer[el];
            for (SugiyamaVertex<V> upperNeighbor : getUpperNeighbors(svGraph, velNextLayer)) {
              int k = upperNeighbor.getIndex();
              if (k < k0 || k > k1) {
                markedSegments.add(svGraph.getEdge(upperNeighbor, velNextLayer));
                //                                log.info("added edge from {} to {} to marked segments", upperNeighbor, velNextLayer);
              }
            }
            el++;
          }
          k0 = k1;
        }
      }
    }
  }

  static <V, E> List<SugiyamaVertex<V>> getUpperNeighbors(
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph, SugiyamaVertex<V> v) {
    return graph.incomingEdgesOf(v).stream().map(graph::getEdgeSource).collect(Collectors.toList());
  }
}
