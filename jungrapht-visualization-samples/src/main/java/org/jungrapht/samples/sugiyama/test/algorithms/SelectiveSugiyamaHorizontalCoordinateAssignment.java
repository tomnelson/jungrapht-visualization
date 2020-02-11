package org.jungrapht.samples.sugiyama.test.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.AverageMedian;
import org.jungrapht.visualization.layout.algorithms.sugiyama.HorizontalCompaction;
import org.jungrapht.visualization.layout.algorithms.sugiyama.HorizontalCoordinateAssignment;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.sugiyama.VerticalAlignment;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gather the coordinate assignments from four passes of the VerticalAlignment algorithm
 * (up-down-left-right) and assign final coordinates using the average median value of the 4 results
 * for each vertex.
 *
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz, Box D 188, 78457 Konstanz,
 *     Germany"
 */
public class SelectiveSugiyamaHorizontalCoordinateAssignment<V, E>
    extends HorizontalCoordinateAssignment<V, E> {

  private static final Logger log =
      LoggerFactory.getLogger(SelectiveSugiyamaHorizontalCoordinateAssignment.class);
  boolean doUpLeft;
  boolean doDownLeft;
  boolean doUpRight;
  boolean doDownRight;

  public SelectiveSugiyamaHorizontalCoordinateAssignment(
      LV<V>[][] layers,
      Graph<LV<V>, LE<V, E>> svGraph,
      Set<LE<V, E>> markedSegments,
      int horizontalOffset,
      int verticalOffset,
      boolean doUpLeft,
      boolean doUpRight,
      boolean doDownLeft,
      boolean doDownRight) {
    super(layers, svGraph, markedSegments, horizontalOffset, verticalOffset);
    this.doUpLeft = doUpLeft;
    this.doDownLeft = doDownLeft;
    this.doUpRight = doUpRight;
    this.doDownRight = doDownRight;
  }

  public void horizontalCoordinateAssignment() {
    preprocessing();
    if (log.isTraceEnabled()) {
      log.trace("inner segments: {}", markedSegments);
    }
    HorizontalCompaction<V> upLeftCompaction = null;
    HorizontalCompaction<V> upRightCompaction = null;
    HorizontalCompaction<V> downLeftCompaction = null;
    HorizontalCompaction<V> downRightCompaction = null;

    if (doUpLeft) {
      VerticalAlignment.LeftmostUpper<V, E> upLeft =
          new VerticalAlignment.LeftmostUpper<>(layers, svGraph, markedSegments);
      upLeft.align();
      upLeftCompaction =
          new HorizontalCompaction<>(
              layers, upLeft.getRootMap(), upLeft.getAlignMap(), horizontalOffset, verticalOffset);
    }
    if (doUpRight) {
      VerticalAlignment.RightmostUpper<V, E> upRight =
          new VerticalAlignment.RightmostUpper<>(layers, svGraph, markedSegments);
      upRight.align();
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
      downLeft.align();
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
      downRight.align();
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
