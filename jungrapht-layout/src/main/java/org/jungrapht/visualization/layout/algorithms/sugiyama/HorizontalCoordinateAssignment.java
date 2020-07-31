package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.NeighborCache;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.synthetics.Synthetic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 */
public class HorizontalCoordinateAssignment<V, E> {

  private static Logger log = LoggerFactory.getLogger(HorizontalCoordinateAssignment.class);

  protected Graph<LV<V>, LE<V, E>> svGraph;
  protected NeighborCache<LV<V>, LE<V, E>> neighborCache;
  protected Set<LE<V, E>> markedSegments;
  protected LV<V>[][] layers;
  protected int horizontalOffset;
  protected int verticalOffset;
  protected boolean horizontalBalancing;

  public HorizontalCoordinateAssignment(
      LV<V>[][] layers,
      Graph<LV<V>, LE<V, E>> svGraph,
      Set<LE<V, E>> markedSegments,
      int horizontalOffset,
      int verticalOffset) {
    this.svGraph = svGraph;
    this.neighborCache = new NeighborCache<>(svGraph);
    this.markedSegments = markedSegments;
    this.layers = layers;
    this.horizontalOffset = horizontalOffset;
    this.verticalOffset = verticalOffset;
  }

  protected LV<V> pred(LV<V> v) {
    int layerOfV = v.getRank();
    int indexOfV = v.getIndex();
    if (indexOfV < 1) {
      return null;
    }
    LV<V>[] list = layers[layerOfV];
    return list[indexOfV - 1];
  }

  protected LV<V> succ(LV<V> v) {
    int layerOfV = v.getRank();
    int indexOfV = v.getIndex();
    if (indexOfV > layers[layerOfV].length - 2) {
      return null;
    }
    LV<V>[] list = layers[layerOfV];
    return list[indexOfV + 1];
  }

  public void horizontalCoordinateAssignment() {
    preprocessing();
    if (log.isTraceEnabled()) {
      log.trace("marked segments:{}", markedSegments);
    }
    VerticalAlignment.LeftmostUpper<V, E> upLeft =
        new VerticalAlignment.LeftmostUpper<>(layers, svGraph, markedSegments);
    upLeft.align();
    HorizontalCompaction<V> upLeftCompaction =
        new HorizontalCompaction<>(
            layers, upLeft.getRootMap(), upLeft.getAlignMap(), horizontalOffset, verticalOffset);
    upLeftCompaction.horizontalCompaction();

    VerticalAlignment.RightmostUpper<V, E> upRight =
        new VerticalAlignment.RightmostUpper<>(layers, svGraph, markedSegments);
    upRight.align();
    HorizontalCompaction<V> upRightCompaction =
        new HorizontalCompaction<>(
            layers, upRight.getRootMap(), upRight.getAlignMap(), horizontalOffset, verticalOffset);
    upRightCompaction.horizontalCompaction();

    VerticalAlignment.LeftmostLower<V, E> downLeft =
        new VerticalAlignment.LeftmostLower<>(layers, svGraph, markedSegments);
    downLeft.align();
    HorizontalCompaction<V> downLeftCompaction =
        new HorizontalCompaction<>(
            layers,
            downLeft.getRootMap(),
            downLeft.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    downLeftCompaction.horizontalCompaction();

    VerticalAlignment.RightmostLower<V, E> downRight =
        new VerticalAlignment.RightmostLower<>(layers, svGraph, markedSegments);
    downRight.align();
    HorizontalCompaction<V> downRightCompaction =
        new HorizontalCompaction<>(
            layers,
            downRight.getRootMap(),
            downRight.getAlignMap(),
            horizontalOffset,
            verticalOffset);
    downRightCompaction.horizontalCompaction();

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

  protected void horizontalBalancing(HorizontalCompaction<V>... compactions) {
    int leastWidthIndex = -1;
    int[] a = new int[4];
    int[] b = new int[4];

    int leastWidth = Integer.MAX_VALUE;
    for (int i = 0; i < 4; i++) {
      int[] bounds = bounds(compactions[i].x.values());
      a[i] = bounds[0];
      b[i] = bounds[1];
      int w = b[i] - a[i];
      if (w < leastWidth) {
        leastWidthIndex = i;
        leastWidth = w;
      }
    }

    for (int i = 0; i < 4; i++) {
      int delta;
      // 0 is upLeft, 2 is downLeft
      if (i == 0 || i == 2) delta = a[leastWidthIndex] - a[i];
      else delta = b[leastWidthIndex] - b[i];
      if (delta != 0) {
        compactions[i].x.entrySet().forEach(entry -> entry.setValue(entry.getValue() + delta));
      }
    }
  }

  protected int[] bounds(Collection<Integer> xValues) {
    if (xValues.size() == 0) {
      return new int[] {0, 0};
    }
    int min = xValues.stream().findFirst().get();
    int max = min;
    for (Integer i : xValues) {
      if (i < min) {
        min = i;
      } else if (i > max) {
        max = i;
      }
    }
    return new int[] {min, max};
  }

  protected int pos(LV<V> v) {
    return v.getIndex();
  }

  protected LV<V> upperNeighborFor(LV<V> v) {
    // any Synthetic vertex must have one upper and one lower neighbor
    return neighborCache.predecessorsOf(v).stream().findFirst().get();
  }

  /**
   * @param v vertex to check
   * @return true iv v is incident to an inner segment between v's rank and the preceding rank
   */
  protected boolean incidentToInnerSegment(LV<V> v) {
    if (v instanceof Synthetic) { // then there is one and only one predecessor
      Collection<LV<V>> predecessors = neighborCache.predecessorsOf(v);
      if (predecessors.size() > 0) {
        LV<V> pv = predecessors.stream().findFirst().get();
        return pv instanceof Synthetic; // both are synthetic so edge between them is an inner edge
      }
    }
    return false;
  }

  protected void preprocessing() {
    int h = layers.length;
    // compares current row 'i' with 'i+1' row
    // i starts at row 1 and goes to row h-2-1
    for (int i = 1; i <= h - 2 - 1; i++) {

      int k0 = 0;
      int el = 0;
      LV<V>[] Li = layers[i]; // Li
      LV<V>[] Liplus1 = layers[i + 1]; // Li+1
      for (int el1 = 0; el1 <= Liplus1.length - 1; el1++) {
        // get the vertex at next layer index el1
        LV<V> velOneOfIplusOne = Liplus1[el1];
        // incident to inner segment if velOneOfIplusOne is Synthetic AND its unique predecessor
        // in layer i is also a Synthetic
        boolean incidentToInnerSegment = incidentToInnerSegment(velOneOfIplusOne);
        if (el1 == Liplus1.length - 1 || incidentToInnerSegment) {
          int k1 = Li.length - 1;
          if (incidentToInnerSegment) {
            // velOneOfIplusOne is a SyntheticSugiyamaVertex and must have one upper neighbor
            k1 = pos(upperNeighborFor(velOneOfIplusOne));
          }
          while (el <= el1) {
            LV<V> velOfIplusOne = Liplus1[el];
            for (LV<V> vkOfI : getUpperNeighbors(velOfIplusOne)) {
              int k = pos(vkOfI);
              if (k < k0 || k > k1) {
                markedSegments.add(svGraph.getEdge(vkOfI, velOfIplusOne));
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
   * @param v the vertex of interest
   * @return a list of the upper neighbors for the supplied vertex, sorted in index order
   */
  protected List<LV<V>> getUpperNeighbors(LV<V> v) {
    return neighborCache
        .predecessorsOf(v)
        .stream()
        .sorted(Comparator.comparingInt(LV::getIndex))
        .collect(Collectors.toList());
  }
}
