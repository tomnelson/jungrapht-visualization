package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.HDirection.LtoR;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.HDirection.RtoL;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.LeftmostLower;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.LeftmostUpper;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.RightmostLower;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.RightmostUpper;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.VDirection.BtoT;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.VDirection.TtoB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
public class HorizontalCoordinateAssignment<V, E>
    extends org.jungrapht.visualization.layout.algorithms.sugiyama.HorizontalCoordinateAssignment<
        V, E> {

  private static Logger log = LoggerFactory.getLogger(HorizontalCoordinateAssignment.class);
  protected Graph<LV<V>, Integer> compactionGraph;
  //  protected Set<LV<V>> verticesNotInCompactionGraph;
  protected Set<LV<V>> isolatedCompactionGraphVertices;

  public HorizontalCoordinateAssignment(
      LV<V>[][] layers,
      Graph<LV<V>, LE<V, E>> svGraph,
      Graph<LV<V>, Integer> compactionGraph,
      Set<LE<V, E>> markedSegments,
      int horizontalOffset,
      int verticalOffset) {
    super(layers, svGraph, markedSegments, horizontalOffset, verticalOffset);
    this.horizontalBalancing = true;
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
    HorizontalCompaction<V, E> upLeftCompaction =
        new HorizontalCompaction<>(
            LtoR,
            TtoB,
            svGraph,
            compactionGraph,
            layers,
            upLeft.getRootMap(),
            upLeft.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    upLeftCompaction.horizontalCompaction();
    //    upLeftCompaction.checkValuesInLayersForSameX(layers);

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
    HorizontalCompaction<V, E> upRightCompaction =
        new HorizontalCompaction<>(
            RtoL,
            TtoB,
            svGraph,
            compactionGraph,
            layers,
            upRight.getRootMap(),
            upRight.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    upRightCompaction.horizontalCompaction();
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
    HorizontalCompaction<V, E> downLeftCompaction =
        new HorizontalCompaction<>(
            LtoR,
            BtoT,
            svGraph,
            compactionGraph,
            layers,
            downLeft.getRootMap(),
            downLeft.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    downLeftCompaction.horizontalCompaction();
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
    HorizontalCompaction<V, E> downRightCompaction =
        new HorizontalCompaction<>(
            RtoL,
            BtoT,
            svGraph,
            compactionGraph,
            layers,
            downRight.getRootMap(),
            downRight.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    downRightCompaction.horizontalCompaction();
    downRightCompaction.checkValuesInLayersForSameX(layers);
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

    int offset = horizontalOffset;
    for (int i = 0; i < 10; i++) {
      int sum = 0;
      log.trace("try {}", i);
      sum += correctOverlappedVertices(offset);
      sum += edgeOverlapCorrection(offset);
      sum += moveVerticesThatOverlapInnerEdges(offset);

      if (sum == 0) {
        log.trace("done after {} tries ", i);
        break;
      }
      offset *= 0.9;
    }
  }

  // gather all vertical inner edges
  private Set<LE<V, E>> getInnerEdges() {
    // find all PVertices
    List<LV<V>> pVertices =
        Arrays.stream(layers)
            .flatMap(Arrays::stream)
            .filter(v -> v instanceof PVertex)
            .collect(Collectors.toList());
    Set<LE<V, E>> innerEdges =
        pVertices
            .stream()
            .map(v -> svGraph.outgoingEdgesOf(v).stream().findFirst().get())
            .collect(Collectors.toSet());
    return innerEdges;
  }

  // gather all vertical lines and for any that share the same x, move one
  private int edgeOverlapCorrection(int offset) {
    int moved = 0;
    // find all PVertices
    List<PVertex> pVertices =
        Arrays.stream(layers)
            .flatMap(Arrays::stream)
            .filter(v -> v instanceof PVertex)
            .map(v -> (PVertex) v)
            .collect(Collectors.toList());
    // if any PVertices share the same x value, then the PVertex and QVertex should be offset
    Set<Double> xValues = new HashSet<>();
    log.trace("checking {} pVertices", pVertices.size());
    for (PVertex<V> pVertex : pVertices) {
      if (xValues.contains(pVertex.getPoint().x)) {
        // offset the PVertex and its QVertex
        QVertex<V> qVertex =
            (QVertex<V>) neighborCache.successorsOf(pVertex).stream().findFirst().get();
        // move them both
        log.trace("got q  check {}", qVertex);
        pVertex.setPoint(pVertex.getPoint().add(offset, 0));
        qVertex.setPoint(qVertex.getPoint().add(offset, 0));
        log.trace("edge ol moved {} {} for edge overlap", pVertex, qVertex);
        moved++;
      }
      xValues.add(pVertex.getPoint().x);
    }
    log.trace("done checking");
    return moved;
  }

  // offset any vertices that intersect an edge that they are not adjacent to
  private int moveVerticesThatOverlapInnerEdges(int offset) {
    int moved = 0;
    Set<LE<V, E>> innerEdges = getInnerEdges();
    Map<Double, LE<V, E>> innerEdgeMap =
        innerEdges
            .stream()
            .collect(
                Collectors.toMap(e -> svGraph.getEdgeSource(e).getPoint().x, e -> e, (a, b) -> b));

    for (LV<V> v : svGraph.vertexSet()) {
      if (v instanceof PVertex) continue;
      if (v instanceof QVertex) continue;
      double x = v.getPoint().x;
      if (innerEdgeMap.keySet().contains(x)) {
        double lowy = innerEdgeMap.get(x).getSource().getPoint().y;
        double hiy = innerEdgeMap.get(x).getTarget().getPoint().y;
        if (lowy > hiy) {
          double temp = lowy;
          lowy = hiy;
          hiy = temp;
        }
        double vy = v.getPoint().y;
        if (lowy <= vy && vy <= hiy) {
          v.setPoint(v.getPoint().add(offset, 0));
          log.trace("moved {}", v);
          moved++;
        }
      }
    }
    return moved;
  }

  private int correctOverlappedVertices(int offset) {
    int moved = 0;
    Set<Point> usedPoints = new HashSet<>();
    for (LV<V> v : svGraph.vertexSet()) {
      if (v instanceof PVertex) continue;
      if (v instanceof QVertex) continue;
      Point balancedPoint = v.getPoint();
      if (usedPoints.contains(balancedPoint)) {
        log.trace("changed {} point ", v);
        log.trace(" from {} ", balancedPoint);
        balancedPoint = balancedPoint.add(offset, 0);
        log.trace("   to {}", balancedPoint);
        moved++;
      }
      usedPoints.add(balancedPoint);
    }
    return moved;
  }

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
