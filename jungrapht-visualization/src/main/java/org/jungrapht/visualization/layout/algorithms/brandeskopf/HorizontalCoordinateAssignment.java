package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaEdge;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaVertex;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticSugiyamaVertex;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorizontalCoordinateAssignment {
  private static Logger log = LoggerFactory.getLogger(HorizontalCoordinateAssignment.class);

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

  static <V, E> int upperNeighborIndexFor(
      SyntheticSugiyamaVertex<V> v, Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph) {
    // any Synthetic vertex must have one upper and one lower neighbor
    return Graphs.predecessorListOf(svGraph, v).get(0).getIndex();
  }

  private static <V> boolean incidentToInnerSegment(SugiyamaVertex<V> v) {
    return v instanceof SyntheticSugiyamaVertex;
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
      SugiyamaVertex<V>[] Li = layers[i]; // Li
      SugiyamaVertex<V>[] Liplus1 = layers[i + 1]; // Li+1
      //      for (int el1 = 1; el1 <= nextLayer.size(); el1++) {
      for (int el1 = 0; el1 <= Liplus1.length - 1; el1++) { // zero based
        // get the vertex at next layer index el1
        SugiyamaVertex<V> vel1iplus1 = Liplus1[el1];
        if (el1 == Liplus1.length - 1 || incidentToInnerSegment(vel1iplus1)) {
          int k1 = Li.length - 1;
          if (incidentToInnerSegment(vel1iplus1)) {
            // vel1iplus1 is a SyntheticSugiyamaVertex and must have one upper neighbor
            k1 = upperNeighborIndexFor((SyntheticSugiyamaVertex) vel1iplus1, svGraph);
          }
          while (el <= el1) {
            SugiyamaVertex<V> velNextLayer = Liplus1[el];
            for (SugiyamaVertex<V> upperNeighbor : getUpperNeighbors(svGraph, velNextLayer)) {
              int k = upperNeighbor.getIndex();
              if (k < k0 || k > k1) {
                if (!(upperNeighbor instanceof SyntheticSugiyamaVertex
                    && velNextLayer instanceof SyntheticSugiyamaVertex)) {
                  // only marking segments that are not inner segments
                  markedSegments.add(svGraph.getEdge(upperNeighbor, velNextLayer));
                }
              }
            }
            el++;
          }
          k0 = k1;
        }
      }
    }
  }

  /**
   * return a list of the upper neighbors for the supplied vertex, sorted in index order
   *
   * @param graph graph with vertex/edge relationships
   * @param v the vertex of interest
   * @param <V> vertex type
   * @param <E> edge type
   * @return a list of the upper neighbors for the supplied vertex, sorted in index order
   */
  static <V, E> List<SugiyamaVertex<V>> getUpperNeighbors(
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph, SugiyamaVertex<V> v) {
    return Graphs.predecessorListOf(graph, v)
        .stream()
        .sorted(Comparator.comparingInt(SugiyamaVertex::getIndex))
        .collect(Collectors.toList());
  }
}
