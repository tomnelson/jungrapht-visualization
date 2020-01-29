package org.jungrapht.samples.tree.test.layout.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.brandeskopf.AverageMedian;
import org.jungrapht.visualization.layout.algorithms.brandeskopf.HorizontalCompaction;
import org.jungrapht.visualization.layout.algorithms.brandeskopf.HorizontalCoordinateAssignment;
import org.jungrapht.visualization.layout.algorithms.brandeskopf.VerticalAlignment;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticLV;
import org.jungrapht.visualization.layout.model.Point;

/**
 * Gather the coordinate assignments from four passes of the VerticalAlignment algorithm
 * (up-down-left-right) and assign final coordinates using the average median value of the 4 results
 * for each vertex.
 *
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz, Box D 188, 78457 Konstanz,
 *     Germany"
 */
public class SelectiveHorizontalCoordinateAssignment {

  /**
   * Assign coordinates to the vertices in the supplied layered graph
   *
   * @param layers two dimensional 'array' of graph vertices
   * @param svGraph the graph holding the vertices in the layers
   * @param markedSegments edges that are marked as crossing
   * @param horizontalOffset amount to add to x coordinate
   * @param verticalOffset amount to add to y coordinate
   * @param <V> vertex type
   * @param <E> edge type
   */
  public static <V, E> void horizontalCoordinateAssignment(
      LV<V>[][] layers,
      Graph<LV<V>, LE<V, E>> svGraph,
      Set<LE<V, E>> markedSegments,
      int horizontalOffset,
      int verticalOffset,
      boolean doUpLeft,
      boolean doUpRight,
      boolean doDownLeft,
      boolean doDownRight) {
    HorizontalCoordinateAssignment.preprocessing(layers, svGraph, markedSegments);

    HorizontalCompaction<V> upLeftCompaction = null;
    HorizontalCompaction<V> upRightCompaction = null;
    HorizontalCompaction<V> downLeftCompaction = null;
    HorizontalCompaction<V> downRightCompaction = null;

    if (doUpLeft) {
      VerticalAlignment.LeftmostUpper<V, E> upLeft =
          new VerticalAlignment.LeftmostUpper<>(layers, svGraph, markedSegments);
      upLeftCompaction =
          new HorizontalCompaction<>(
              layers, upLeft.getRootMap(), upLeft.getAlignMap(), horizontalOffset, verticalOffset);
    }
    if (doUpRight) {
      VerticalAlignment.RightmostUpper<V, E> upRight =
          new VerticalAlignment.RightmostUpper<>(layers, svGraph, markedSegments);
      upRightCompaction =
          new HorizontalCompaction<>(
              layers,
              upRight.getRootMap(),
              upRight.getAlignMap(),
              horizontalOffset,
              verticalOffset);
    }

    if (doDownLeft) {
      VerticalAlignment.LeftmostLower<V, E> downLeft =
          new VerticalAlignment.LeftmostLower<>(layers, svGraph, markedSegments);
      downLeftCompaction =
          new HorizontalCompaction<>(
              layers,
              downLeft.getRootMap(),
              downLeft.getAlignMap(),
              horizontalOffset,
              verticalOffset);
    }

    if (doDownRight) {
      VerticalAlignment.RightmostLower<V, E> downRight =
          new VerticalAlignment.RightmostLower<>(layers, svGraph, markedSegments);
      downRightCompaction =
          new HorizontalCompaction<>(
              layers,
              downRight.getRootMap(),
              downRight.getAlignMap(),
              horizontalOffset,
              verticalOffset);
    }

    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        LV<V> v = layers[i][j];
        //    for (List<SugiyamaVertex<V>> list : layers) {
        //      for (SugiyamaVertex<V> v : list) {
        List<Point> points = new ArrayList<>();

        if (doUpLeft) {
          points.add(upLeftCompaction.getPoint(v).add(horizontalOffset, verticalOffset));
        }
        if (doUpRight) {
          points.add(upRightCompaction.getPoint(v).add(horizontalOffset, verticalOffset));
        }
        if (doDownLeft) {
          points.add(downLeftCompaction.getPoint(v).add(horizontalOffset, verticalOffset));
        }
        if (doDownRight) {
          points.add(downRightCompaction.getPoint(v).add(horizontalOffset, verticalOffset));
        }
        Point balancedPoint = AverageMedian.averageMedianPoint(points.toArray(new Point[0]));
        v.setPoint(balancedPoint);
      }
    }
  }

  /**
   * Discovers and saves off (non-segment) edges that will cross with segments<br>
   * These are Type-1 crossings in the referenced paper.
   *
   * <ul>
   *   <li>original graph edges. Neither endpoint vertex is synthetic
   *   <li>sythetic edges that have one endpoint that is an original (not synthetic) vertex in the
   *       graph
   * </ul>
   *
   * @param layers
   * @param svGraph
   * @param markedSegments
   * @param <V>
   * @param <E>
   */
  static <V, E> void preprocessing(
      List<List<LV<V>>> layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {
    int h = layers.size();
    // compares current row 'i' with 'i+1' row
    // i starts at row 1 and goes to row h-2-1
    //    for (int i = 2; i <= h - 2; i++) {
    for (int i = 1; i <= h - 2 - 1; i++) { // zero based

      int k0 = 0;
      int el = 0;
      List<LV<V>> thisLayer = layers.get(i); // Li
      List<LV<V>> nextLayer = layers.get(i + 1); // Li+1
      //      for (int el1 = 1; el1 <= nextLayer.size(); el1++) {
      for (int el1 = 0; el1 <= nextLayer.size() - 1; el1++) { // zero based
        // get the vertex at next layer index el1
        LV<V> vel1nextLayer = nextLayer.get(el1);
        if (el1 == nextLayer.size() - 1 || vel1nextLayer instanceof SyntheticLV) {
          int k1 = thisLayer.size() - 1;
          if (vel1nextLayer instanceof SyntheticLV) {
            Optional<LE<V, E>> incomingEdgeOpt =
                svGraph.incomingEdgesOf(vel1nextLayer).stream().findFirst();
            if (incomingEdgeOpt.isPresent()) {
              LE<V, E> incomingEdge = incomingEdgeOpt.get();
              LV<V> upperNeighbor = svGraph.getEdgeSource(incomingEdge);
              k1 = upperNeighbor.getIndex();
            }
          }
          while (el <= el1) {
            LV<V> velNextLayer = nextLayer.get(el);
            for (LV<V> upperNeighbor : getUpperNeighbors(svGraph, velNextLayer)) {
              int k = upperNeighbor.getIndex();
              if (k < k0 || k > k1) {
                markedSegments.add(svGraph.getEdge(upperNeighbor, velNextLayer));
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
   * For a provided vertex, return a {@List} of the source vertices for its incoming edges.
   *
   * @param graph holds vertex relationships
   * @param v provided vertex
   * @param <V> vertex type
   * @param <E> edge type
   * @return a {@List} of the source vertices for the provided vertex
   */
  static <V, E> List<LV<V>> getUpperNeighbors(Graph<LV<V>, LE<V, E>> graph, LV<V> v) {
    return graph.incomingEdgesOf(v).stream().map(graph::getEdgeSource).collect(Collectors.toList());
  }
}
