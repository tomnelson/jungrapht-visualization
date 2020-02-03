package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.sugiyama.AverageMedian;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticLV;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 */
class HorizontalCoordinateAssignment {
  private static Logger log = LoggerFactory.getLogger(HorizontalCoordinateAssignment.class);

  public static <V, E> void horizontalCoordinateAssignment(
      LV<V>[][] layers,
      Graph<LV<V>, LE<V, E>> svGraph,
      Set<LE<V, E>> markedSegments,
      int horizontalOffset,
      int verticalOffset) {
    preprocessing(layers, svGraph, markedSegments);

    VerticalAlignment.LeftmostUpper<V, E> upLeft =
        new VerticalAlignment.LeftmostUpper<>(layers, svGraph, markedSegments);
    HorizontalCompaction<V> upLeftCompaction =
        new HorizontalCompaction<>(
            layers, upLeft.getRootMap(), upLeft.getAlignMap(), horizontalOffset, verticalOffset);
    List<LV<V>> misAligned = misAligned(upLeftCompaction.p);
    if (misAligned.size() > 0 && log.isTraceEnabled()) {
      log.trace("misAligned from upLeft: {}", misAligned.size());
    }

    VerticalAlignment.RightmostUpper<V, E> upRight =
        new VerticalAlignment.RightmostUpper<>(layers, svGraph, markedSegments);
    HorizontalCompaction<V> upRightCompaction =
        new HorizontalCompaction<>(
            layers, upRight.getRootMap(), upRight.getAlignMap(), horizontalOffset, verticalOffset);
    misAligned = misAligned(upRightCompaction.p);
    if (misAligned.size() > 0 && log.isTraceEnabled()) {
      log.trace("misAligned from upRight: {}", misAligned.size());
    }

    VerticalAlignment.LeftmostLower<V, E> downLeft =
        new VerticalAlignment.LeftmostLower<>(layers, svGraph, markedSegments);
    HorizontalCompaction<V> downLeftCompaction =
        new HorizontalCompaction<>(
            layers,
            downLeft.getRootMap(),
            downLeft.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    misAligned = misAligned(downLeftCompaction.p);
    if (misAligned.size() > 0 && log.isTraceEnabled()) {
      log.trace("misAligned from downLeft: {}", misAligned.size());
    }

    VerticalAlignment.RightmostLower<V, E> downRight =
        new VerticalAlignment.RightmostLower<>(layers, svGraph, markedSegments);
    HorizontalCompaction<V> downRightCompaction =
        new HorizontalCompaction<>(
            layers,
            downRight.getRootMap(),
            downRight.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    misAligned = misAligned(downRightCompaction.p);
    if (misAligned.size() > 0 && log.isTraceEnabled()) {
      log.trace("misAligned from downRight: {}", misAligned.size());
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
                upLeftPoint, upRightPoint,
                downLeftPoint, downRightPoint);
        v.setPoint(balancedPoint);
      }
    }
    misAligned = misAligned(layers);
    if (misAligned.size() > 0 && log.isTraceEnabled()) {
      log.trace("misAligned: {} {}", misAligned, misAligned.size());
    }
  }

  private static <V> List<LV<V>> misAligned(Map<LV<V>, Point> pointMap) {
    List<LV<V>> misAligned = new ArrayList<>();
    for (Map.Entry<LV<V>, Point> entry : pointMap.entrySet()) {
      if (misAligned(entry.getKey(), pointMap)) {
        misAligned.add(entry.getKey());
      }
    }
    return misAligned;
  }

  private static <V> List<LV<V>> misAligned(LV<V>[][] layers) {
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

  private static <V> boolean misAligned(LV<V> v) {
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

  private static <V> boolean misAligned(LV<V> v, Map<LV<V>, Point> map) {
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

  static <V, E> int upperNeighborIndexFor(SegmentVertex<V> v, Graph<LV<V>, LE<V, E>> svGraph) {
    // any Synthetic vertex must have one upper and one lower neighbor
    if (Graphs.predecessorListOf(svGraph, v).size() > 1) {
      log.error("error");
    }
    return Graphs.predecessorListOf(svGraph, v).get(0).getIndex();
  }

  static <V, E> int upperNeighborIndexFor(SyntheticLV<V> v, Graph<LV<V>, LE<V, E>> svGraph) {
    // any Synthetic vertex must have one upper and one lower neighbor
    if (Graphs.predecessorListOf(svGraph, v).size() > 1) {
      log.error("error");
    }
    return Graphs.predecessorListOf(svGraph, v).get(0).getIndex();
  }

  private static <V> boolean incidentToInnerSegment(LV<V> v) {
    return v instanceof SegmentVertex; // || v instanceof SyntheticLV;
  }

  public static <V, E> void preprocessing(
      LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {

    if (log.isTraceEnabled()) {
      Arrays.stream(layers).forEach(a -> log.trace("-" + Arrays.toString(a)));
    }
    int h = layers.length;
    // compares current row 'i' with 'i+1' row
    // i starts at row 1 and goes to row h-2-1
    //    for (int i = 2; i <= h - 2; i++) {
    for (int i = 1; i <= h - 2 - 1; i++) { // zero based

      int k0 = 0;
      int el = 0;
      LV<V>[] Li = layers[i]; // Li
      LV<V>[] Liplus1 = layers[i + 1]; // Li+1
      //      for (int el1 = 1; el1 <= nextLayer.size(); el1++) {
      for (int el1 = 0; el1 <= Liplus1.length - 1; el1++) { // zero based
        // get the vertex at next layer index el1
        LV<V> vel1iplus1 = Liplus1[el1];
        if (el1 == Liplus1.length - 1 || incidentToInnerSegment(vel1iplus1)) {
          int k1 = Li.length - 1;
          if (incidentToInnerSegment(vel1iplus1)) {
            // vel1iplus1 is a SyntheticEiglspergerVertex and must have one upper neighbor
            k1 = upperNeighborIndexFor((SyntheticLV<V>) vel1iplus1, svGraph);
          }
          while (el <= el1) {
            LV<V> velNextLayer = Liplus1[el];
            for (LV<V> upperNeighbor : getUpperNeighbors(svGraph, velNextLayer)) {
              int k = upperNeighbor.getIndex();
              if (k < k0 || k > k1) {
                if (!(upperNeighbor instanceof SegmentVertex
                    && velNextLayer instanceof SegmentVertex)) {
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
    log.trace("markedSegments are {}", markedSegments);
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
  static <V, E> List<LV<V>> getUpperNeighbors(Graph<LV<V>, LE<V, E>> graph, LV<V> v) {
    return Graphs.predecessorListOf(graph, v)
        .stream()
        .sorted(Comparator.comparingInt(LV::getIndex))
        .collect(Collectors.toList());
  }
}
