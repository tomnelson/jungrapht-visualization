package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.AverageMedian;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 */
public class HorizontalCoordinateAssignmentDeprecated<V, E>
    extends org.jungrapht.visualization.layout.algorithms.sugiyama.HorizontalCoordinateAssignment<
        V, E> {

  private static Logger log =
      LoggerFactory.getLogger(HorizontalCoordinateAssignmentDeprecated.class);

  public HorizontalCoordinateAssignmentDeprecated(
      LV<V>[][] layers,
      Graph<LV<V>, LE<V, E>> svGraph,
      Set<LE<V, E>> markedSegments,
      int horizontalOffset,
      int verticalOffset) {
    super(layers, svGraph, markedSegments, horizontalOffset, verticalOffset);
  }

  public void horizontalCoordinateAssignment() {
    VerticalAlignmentDeprecated.LeftmostUpper<V, E> upLeft =
        new VerticalAlignmentDeprecated.LeftmostUpper<>(layers, svGraph, markedSegments);
    upLeft.align();

    HorizontalCompactionDeprecated<V> upLeftCompaction =
        new HorizontalCompactionDeprecated<>(
            layers, upLeft.getRootMap(), upLeft.getAlignMap(), horizontalOffset, verticalOffset);
    upLeftCompaction.horizontalCompaction();
    if (log.isTraceEnabled()) {
      log.trace("upLeft");
      log.trace("alignMap:{}", upLeft.getAlignMap());
      log.trace("rootMap:{}", upLeft.getRootMap());
      log.trace("shift:{}", upLeftCompaction.getShift());
      log.trace("sink:{}", upLeftCompaction.getSink());
    }

    VerticalAlignmentDeprecated.RightmostUpper<V, E> upRight =
        new VerticalAlignmentDeprecated.RightmostUpper<>(layers, svGraph, markedSegments);
    upRight.align();
    HorizontalCompactionDeprecated<V> upRightCompaction =
        new HorizontalCompactionDeprecated<>(
            layers, upRight.getRootMap(), upRight.getAlignMap(), horizontalOffset, verticalOffset);
    upRightCompaction.horizontalCompaction();
    if (log.isTraceEnabled()) {
      log.trace("upRight");
      log.trace("alignMap:{}", upRight.getAlignMap());
      log.trace("rootMap:{}", upRight.getRootMap());
      log.trace("shift:{}", upRightCompaction.getShift());
      log.trace("sink:{}", upRightCompaction.getSink());
    }

    VerticalAlignmentDeprecated.LeftmostLower<V, E> downLeft =
        new VerticalAlignmentDeprecated.LeftmostLower<>(layers, svGraph, markedSegments);
    downLeft.align();
    HorizontalCompactionDeprecated<V> downLeftCompaction =
        new HorizontalCompactionDeprecated<>(
            layers,
            downLeft.getRootMap(),
            downLeft.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    downLeftCompaction.horizontalCompaction();
    if (log.isTraceEnabled()) {
      log.trace("downLeft");
      log.trace("alignMap:{}", downLeft.getAlignMap());
      log.trace("rootMap:{}", downLeft.getRootMap());
      log.trace("shift:{}", downLeftCompaction.getShift());
      log.trace("sink:{}", downLeftCompaction.getSink());
    }

    VerticalAlignmentDeprecated.RightmostLower<V, E> downRight =
        new VerticalAlignmentDeprecated.RightmostLower<>(layers, svGraph, markedSegments);
    downRight.align();
    HorizontalCompactionDeprecated<V> downRightCompaction =
        new HorizontalCompactionDeprecated<>(
            layers,
            downRight.getRootMap(),
            downRight.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    downRightCompaction.horizontalCompaction();
    if (log.isTraceEnabled()) {
      log.trace("downRight");
      log.trace("alignMap:{}", downRight.getAlignMap());
      log.trace("rootMap:{}", downRight.getRootMap());
      log.trace("shift:{}", downRightCompaction.getShift());
      log.trace("sink:{}", downRightCompaction.getSink());
    }

    if (horizontalBalancing) {
      horizontalBalancing(
          upLeftCompaction, upRightCompaction, downLeftCompaction, downRightCompaction);
    }

    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        LV<V> v = layers[i][j];
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

  /**
   * override to use pos instead of index
   *
   * @param v vertex to consider
   * @return v's pos (not its index in the rank)
   */
  @Override
  protected int pos(LV<V> v) {
    return v.getPos();
  }

  protected int idx(LV<V> v) {
    return v.getIndex();
  }

  /**
   * override to say that only QVertices are incident to an inner edge that spans from previous rank
   * to this one
   *
   * @param v vertex to check
   * @return true iv v is incident to an inner segment between v's rank and the preceding rank
   */
  @Override
  protected boolean incidentToInnerSegment(LV<V> v) {
    return v instanceof QVertex;
  }

  protected List<LV<V>> misAligned(Map<LV<V>, Point> pointMap) {
    List<LV<V>> misAligned = new ArrayList<>();
    for (Map.Entry<LV<V>, Point> entry : pointMap.entrySet()) {
      if (misAligned(entry.getKey(), pointMap)) {
        misAligned.add(entry.getKey());
      }
    }
    return misAligned;
  }

  protected List<LV<V>> misAligned(LV<V>[][] layers) {
    List<LV<V>> misAligned = new ArrayList<>();
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        LV<V> v = layers[i][j];
        if (misAligned(v)) {
          misAligned.add(v);
        }
      }
    }
    return misAligned;
  }

  protected boolean misAligned(LV<V> v) {
    if (v instanceof SegmentVertex) {
      SegmentVertex<V> segmentVertex = (SegmentVertex<V>) v;
      PVertex<V> pVertex = segmentVertex.getSegment().pVertex;
      QVertex<V> qVertex = segmentVertex.getSegment().qVertex;
      // do pVertex and qVertex have different x values?
      Point p = pVertex.getPoint();
      Point q = qVertex.getPoint();
      if (p.x != q.x) {
        return true;
      }
    }
    return false;
  }

  protected boolean misAligned(LV<V> v, Map<LV<V>, Point> map) {
    if (v instanceof SegmentVertex) {
      SegmentVertex<V> segmentVertex = (SegmentVertex<V>) v;
      PVertex<V> pVertex = segmentVertex.getSegment().pVertex;
      QVertex<V> qVertex = segmentVertex.getSegment().qVertex;
      // do pVertex and qVertex have different x values?
      Point p = map.get(pVertex);
      Point q = map.get(qVertex);
      if (p.x != q.x && log.isTraceEnabled()) {
        log.trace(
            "segment {} misaligned with p at {} and q at {}", segmentVertex.getSegment(), p, q);
        return true;
      }
    }
    return false;
  }
}
