package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 * @param <V>
 * @param <E>
 */
public abstract class VerticalAlignmentDeprecated<V, E>
    extends org.jungrapht.visualization.layout.algorithms.sugiyama.VerticalAlignment<V, E> {

  private static final Logger log = LoggerFactory.getLogger(VerticalAlignmentDeprecated.class);

  protected VerticalAlignmentDeprecated(
      LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {
    super(layers, svGraph, markedSegments);
  }

  @Override
  protected int alignMoveCursor(LV<V> neighbor, LV<V> vertex) {
    align(neighbor, vertex);
    root(vertex, root(neighbor));
    align(vertex, root(vertex));
    return pos(neighbor);
  }

  protected int alignSegmentVertexMoveCursor(SegmentVertex<V> segmentVertex, LV<V> vertex) {
    SegmentVertex<V> neighborSegmentVertex;
    if (segmentVertex instanceof PVertex) {
      neighborSegmentVertex = ((PVertex<V>) segmentVertex).segment.qVertex;
    } else {
      neighborSegmentVertex = ((QVertex<V>) segmentVertex).segment.pVertex;
    }
    //    align(neighbor, vertex);
    root(vertex, root(neighborSegmentVertex));
    align(vertex, root(neighborSegmentVertex));
    return pos(neighborSegmentVertex);
  }

  /**
   * start at first layer, work down, looking at predecessors
   *
   * @param <V>
   * @param <E>
   */
  public static class LeftmostUpper<V, E> extends VerticalAlignmentDeprecated<V, E> {

    public LeftmostUpper(
        LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {
      super(layers, svGraph, markedSegments);
    }

    @Override
    public void align() {
      for (int i = 0; i < layers.length; i++) {
        int r = -1;
        LV<V>[] currentLayer = layers[i];
        for (int k = 0; k <= currentLayer.length - 1; k++) {
          LV<V> vkofi = currentLayer[k];
          List<LV<V>> neighbors =
              Graphs.predecessorListOf(svGraph, vkofi)
                  .stream()
                  .sorted(Comparator.comparingInt(LV::getIndex))
                  .collect(Collectors.toList());
          int d = neighbors.size();
          if (d > 0) {
            int floor = (int) Math.floor((d - 1) / 2.0);
            int ceil = (int) Math.ceil((d - 1) / 2.0);
            for (int m : new int[] {floor, ceil}) {
              if (align(vkofi) == vkofi) {
                LV<V> um = neighbors.get(m);
                LE<V, E> edge = svGraph.getEdge(um, vkofi);
                if ((notMarked(edge) && r < pos(um)) || vkofi instanceof QVertex) {
                  r = alignMoveCursor(um, vkofi);
                }
              }
              if (floor == ceil) {
                break;
              }
            }
          }
        }
      }
    }
  }

  public static class RightmostUpper<V, E> extends VerticalAlignmentDeprecated<V, E> {

    // up right from top to bottom of layers, from left to right of rows
    public RightmostUpper(
        LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {
      super(layers, svGraph, markedSegments);
    }

    @Override
    public void align() {
      for (int i = 1; i < layers.length; i++) {
        LV<V>[] currentLayer = layers[i];
        LV<V>[] previousLayerInSweep = layers[i - 1];
        int r = pos(previousLayerInSweep[previousLayerInSweep.length - 1]) + 1;
        // one past the last pos in the previous layer of this sweep
        for (int k = currentLayer.length - 1; k >= 0; k--) {
          LV<V> vkofi = currentLayer[k];
          List<LV<V>> neighbors =
              Graphs.predecessorListOf(svGraph, vkofi)
                  .stream()
                  .sorted(Comparator.comparingInt(LV::getIndex))
                  .collect(Collectors.toList());
          int d = neighbors.size();
          if (d > 0) {
            int floor = (int) Math.floor((d - 1) / 2.0);
            int ceil = (int) Math.ceil((d - 1) / 2.0);
            for (int m : new int[] {ceil, floor}) {
              if (align(vkofi) == vkofi) {
                LV<V> um = neighbors.get(m);
                LE<V, E> edge = svGraph.getEdge(um, vkofi);
                if ((notMarked(edge) && r > pos(um)) || vkofi instanceof QVertex) {
                  r = alignMoveCursor(um, vkofi);
                }
              }
              if (floor == ceil) {
                break;
              }
            }
          }
        }
      }
    }
  }

  /**
   * start at last layer, work upwards looking at successor positions
   *
   * @param <V>
   * @param <E>
   */
  public static class LeftmostLower<V, E> extends VerticalAlignmentDeprecated<V, E> {

    // down left from bottom to top of layers from left to right of rows
    public LeftmostLower(
        LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {
      super(layers, svGraph, markedSegments);
    }

    @Override
    public void align() {
      for (int i = layers.length - 2; i >= 0; i--) {
        int r = -1; // one before the first index of the previous layer of this sweep
        LV<V>[] currentLayer = layers[i];
        for (int k = 0; k < currentLayer.length; k++) {
          LV<V> vkofi = currentLayer[k];
          List<LV<V>> neighbors =
              Graphs.successorListOf(svGraph, vkofi)
                  .stream()
                  .sorted(Comparator.comparingInt(LV::getIndex))
                  .collect(Collectors.toList());
          int d = neighbors.size();
          if (d > 0) {
            int floor = (int) Math.floor((d - 1) / 2.0);
            int ceil = (int) Math.ceil((d - 1) / 2.0);
            for (int m : new int[] {floor, ceil}) {
              if (align(vkofi) == vkofi) {
                LV<V> um = neighbors.get(m);
                LE<V, E> edge = svGraph.getEdge(vkofi, um);
                if ((notMarked(edge) && r < pos(um)) || vkofi instanceof PVertex) {
                  r = alignMoveCursor(um, vkofi);
                }
              }
              if (floor == ceil) {
                break;
              }
            }
          }
        }
      }
    }
  }

  /**
   * start at last layer, work up, looking at successors
   *
   * @param <V>
   * @param <E>
   */
  public static class RightmostLower<V, E> extends VerticalAlignmentDeprecated<V, E> {

    // down right from bottom to top of layers from right to left
    public RightmostLower(
        LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {
      super(layers, svGraph, markedSegments);
    }

    @Override
    public void align() {
      for (int i = layers.length - 2; i >= 0; i--) {
        LV<V>[] currentLayer = layers[i];
        LV<V>[] previousLayerInSweep = layers[i + 1];
        int r =
            pos(previousLayerInSweep[previousLayerInSweep.length - 1])
                + 1; // one past the last pos in the previous layer of this sweep
        for (int k = currentLayer.length - 1; k >= 0; k--) {
          LV<V> vkofi = currentLayer[k];
          List<LV<V>> neighbors =
              Graphs.successorListOf(svGraph, vkofi)
                  .stream()
                  .sorted(Comparator.comparingInt(LV::getIndex))
                  .collect(Collectors.toList());
          int d = neighbors.size();
          if (d > 0) {
            int floor = (int) Math.floor((d - 1) / 2.0);
            int ceil = (int) Math.ceil((d - 1) / 2.0);
            for (int m : new int[] {ceil, floor}) {
              if (align(vkofi) == vkofi) {
                LV<V> um = neighbors.get(m);
                LE<V, E> edge = svGraph.getEdge(vkofi, um);
                if ((notMarked(edge) && r > pos(um)) || vkofi instanceof PVertex) {
                  r = alignMoveCursor(um, vkofi);
                }
              }
              if (floor == ceil) {
                break;
              }
            }
          }
        }
      }
    }
  }

  /**
   * @param v vertix to get pos for
   * @return the pos (not index in rank) of the passed vertex
   */
  protected int pos(LV<V> v) {
    return v.getPos();
  }

  /**
   * @param v vertix to get pos for
   * @return the pos (index in rank) of the passed vertex
   */
  protected int idx(LV<V> v) {
    return v.getIndex();
  }
}
