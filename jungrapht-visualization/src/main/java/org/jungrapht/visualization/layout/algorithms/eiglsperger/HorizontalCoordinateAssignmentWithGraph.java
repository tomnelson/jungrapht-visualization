package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.HDirection.LtoR;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.HDirection.RtoL;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.LeftmostLower;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.LeftmostUpper;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.RightmostLower;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.RightmostUpper;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.VDirection.BtoT;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.VDirection.TtoB;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
public class HorizontalCoordinateAssignmentWithGraph<V, E>
    extends org.jungrapht.visualization.layout.algorithms.sugiyama.HorizontalCoordinateAssignment<
        V, E> {

  private static Logger log =
      LoggerFactory.getLogger(HorizontalCoordinateAssignmentWithGraph.class);
  protected Graph<LV<V>, Integer> compactionGraph;
  //  protected Set<LV<V>> verticesNotInCompactionGraph;
  protected Set<LV<V>> isolatedCompactionGraphVertices;

  public HorizontalCoordinateAssignmentWithGraph(
      LV<V>[][] layers,
      Graph<LV<V>, LE<V, E>> svGraph,
      Graph<LV<V>, Integer> compactionGraph,
      Set<LE<V, E>> markedSegments,
      int horizontalOffset,
      int verticalOffset) {
    super(layers, svGraph, markedSegments, horizontalOffset, verticalOffset);
    this.compactionGraph = compactionGraph;
    Set<Integer> compactionGraphEdges = compactionGraph.edgeSet();
    isolatedCompactionGraphVertices =
        compactionGraph
            .vertexSet()
            .stream()
            .filter(v -> compactionGraph.degreeOf(v) == 0)
            .collect(Collectors.toSet());
  }

  public void horizontalCoordinateAssignment() {

    LeftmostUpper<V, E> upLeft =
        new LeftmostUpper(LtoR, TtoB, layers, compactionGraph, svGraph, markedSegments);
    upLeft.align();
    HorizontalCompactionWithGraph<V, E> upLeftCompaction =
        new HorizontalCompactionWithGraph<>(
            LtoR,
            TtoB,
            svGraph,
            compactionGraph,
            layers,
            upLeft.getRootMap(),
            upLeft.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    upLeftCompaction.checkValuesInLayersForSameX(layers);

    if (log.isTraceEnabled()) {
      log.trace("upLeft");
      log.trace("alignMap:{}", upLeft.getAlignMap());
      log.trace("rootMap:{}", upLeft.getRootMap());
      log.trace("shift:{}", upLeftCompaction.getShift());
      log.trace("sink:{}", upLeftCompaction.getSink());
    }

    RightmostUpper<V, E> upRight =
        new RightmostUpper<>(RtoL, TtoB, layers, compactionGraph, svGraph, markedSegments);
    upRight.align();
    HorizontalCompactionWithGraph<V, E> upRightCompaction =
        new HorizontalCompactionWithGraph<>(
            RtoL,
            TtoB,
            svGraph,
            compactionGraph,
            layers,
            upRight.getRootMap(),
            upRight.getAlignMap(),
            horizontalOffset,
            verticalOffset);

    upRightCompaction.checkValuesInLayersForSameX(layers);
    if (log.isTraceEnabled()) {
      log.trace("upRight");
      log.trace("alignMap:{}", upRight.getAlignMap());
      log.trace("rootMap:{}", upRight.getRootMap());
      log.trace("shift:{}", upRightCompaction.getShift());
      log.trace("sink:{}", upRightCompaction.getSink());
    }

    LeftmostLower<V, E> downLeft =
        new LeftmostLower<>(LtoR, BtoT, layers, compactionGraph, svGraph, markedSegments);
    downLeft.align();
    HorizontalCompactionWithGraph<V, E> downLeftCompaction =
        new HorizontalCompactionWithGraph<>(
            LtoR,
            BtoT,
            svGraph,
            compactionGraph,
            layers,
            downLeft.getRootMap(),
            downLeft.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    downLeftCompaction.checkValuesInLayersForSameX(layers);
    if (log.isTraceEnabled()) {
      log.trace("downLeft");
      log.trace("alignMap:{}", downLeft.getAlignMap());
      log.trace("rootMap:{}", downLeft.getRootMap());
      log.trace("shift:{}", downLeftCompaction.getShift());
      log.trace("sink:{}", downLeftCompaction.getSink());
    }

    RightmostLower<V, E> downRight =
        new RightmostLower<>(RtoL, BtoT, layers, compactionGraph, svGraph, markedSegments);
    downRight.align();
    HorizontalCompactionWithGraph<V, E> downRightCompaction =
        new HorizontalCompactionWithGraph<>(
            RtoL,
            BtoT,
            svGraph,
            compactionGraph,
            layers,
            downRight.getRootMap(),
            downRight.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    downRightCompaction.checkValuesInLayersForSameX(layers);
    if (log.isTraceEnabled()) {
      log.trace("downRight");
      log.trace("alignMap:{}", downRight.getAlignMap());
      log.trace("rootMap:{}", downRight.getRootMap());
      log.trace("shift:{}", downRightCompaction.getShift());
      log.trace("sink:{}", downRightCompaction.getSink());
    }

    horizontalBalancing(
        upLeftCompaction, upRightCompaction, downLeftCompaction, downRightCompaction);

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

//  protected void horizontalBalancing(HorizontalCompactionWithGraph<V, E>... compactions) {
//    int leastWidthIndex = -1;
//    int[] a = new int[4];
//    int[] b = new int[4];
//
//    int leastWidth = Integer.MAX_VALUE;
//    for (int i = 0; i < 4; i++) {
//      int[] bounds = bounds(compactions[i].x.values());
//      a[i] = bounds[0];
//      b[i] = bounds[1];
//      int w = b[i] - a[i];
//      if (w < leastWidth) {
//        leastWidthIndex = i;
//        leastWidth = w;
//      }
//    }
//
//    for (int i = 0; i < 4; i++) {
//      int delta;
//      // 0 is upLeft, 2 is downLeft
//      if (i == 0 || i == 2) delta = a[leastWidthIndex] - a[i];
//      else delta = b[leastWidthIndex] - b[i];
//      if (delta != 0) {
//        compactions[i].x.entrySet().forEach(entry -> entry.setValue(entry.getValue() + delta));
//      }
//    }
//  }

//  protected HorizontalCompactionWithGraph<V, E> leastWidthCompaction(
//      HorizontalCompactionWithGraph<V, E>... compactions) {
//    int least = Integer.MAX_VALUE;
//    HorizontalCompactionWithGraph<V, E> narrowest = null;
//    for (HorizontalCompactionWithGraph<V, E> compaction : compactions) {
//      int width = boundsWidth(compaction.x.values());
//      if (width < least) {
//        least = width;
//        narrowest = compaction;
//      }
//    }
//    return narrowest;
//  }

//  protected int[] bounds(Collection<Integer> xValues) {
//    if (xValues.size() == 0) {
//      return new int[] {0, 0};
//    }
//    int min = xValues.stream().findFirst().get();
//    int max = min;
//    for (Integer i : xValues) {
//      if (i < min) {
//        min = i;
//      } else if (i > max) {
//        max = i;
//      }
//    }
//    return new int[] {min, max};
//  }

  protected int boundsWidth(Collection<Integer> xValues) {
    int[] bounds = bounds(xValues);
    return bounds[1] - bounds[0];
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
