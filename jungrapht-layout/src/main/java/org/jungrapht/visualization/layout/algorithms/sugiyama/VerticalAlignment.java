package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.NeighborCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 * @param <V>
 * @param <E>
 */
public abstract class VerticalAlignment<V, E> {

  private static final Logger log = LoggerFactory.getLogger(VerticalAlignment.class);

  protected Map<LV<V>, LV<V>> rootMap = new HashMap<>();
  protected Map<LV<V>, LV<V>> alignMap = new HashMap<>();
  protected LV<V>[][] layers;
  protected Graph<LV<V>, LE<V, E>> svGraph;
  protected Set<LE<V, E>> markedSegments;
  protected NeighborCache<LV<V>, LE<V, E>> neighborCache;

  public Map<LV<V>, LV<V>> getRootMap() {
    return rootMap;
  }

  public Map<LV<V>, LV<V>> getAlignMap() {
    return alignMap;
  }

  public abstract void align();

  protected VerticalAlignment(
      LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {
    this.layers = layers;
    this.svGraph = svGraph;
    this.neighborCache = new NeighborCache<>(svGraph);
    this.markedSegments = markedSegments;
    // initialize root and align
    Arrays.stream(layers)
        .flatMap(Arrays::stream)
        .forEach(
            v -> {
              rootMap.put(v, v);
              alignMap.put(v, v);
            });
  }

  protected boolean notMarked(LE<V, E> edge) {
    return !markedSegments.contains(edge);
  }

  protected LV<V> root(LV<V> v) {
    return rootMap.get(v);
  }

  protected void root(LV<V> k, LV<V> v) {
    rootMap.put(k, v);
  }

  protected LV<V> align(LV<V> v) {
    return alignMap.get(v);
  }

  protected void align(LV<V> k, LV<V> v) {
    alignMap.put(k, v);
  }

  /**
   * @param v vertix to get pos for
   * @return the pos (index in rank) of the passed vertex
   */
  protected int pos(LV<V> v) {
    return v.getIndex();
  }

  protected int alignMoveCursor(LV<V> um, LV<V> vkofi) {
    align(um, vkofi);
    root(vkofi, root(um));
    align(vkofi, root(vkofi));
    return pos(um);
  }

  /**
   * start at first layer, work down, looking at predecessors
   *
   * @param <V>
   * @param <E>
   */
  public static class LeftmostUpper<V, E> extends VerticalAlignment<V, E> {

    public LeftmostUpper(
        LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {
      super(layers, svGraph, markedSegments);
    }

    @Override
    public void align() {
      for (int i = 0; i <= layers.length - 1; i++) {
        int r = -1;
        LV<V>[] currentLayer = layers[i];
        for (int k = 0; k <= currentLayer.length - 1; k++) {
          LV<V> vkofi = currentLayer[k];
          List<LV<V>> neighbors =
              neighborCache
                  .predecessorsOf(vkofi)
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
                if (notMarked(edge) && r < pos(um)) {
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

  public static class RightmostUpper<V, E> extends VerticalAlignment<V, E> {

    // up right from top to bottom of layers, from left to right of rows
    public RightmostUpper(
        LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {
      super(layers, svGraph, markedSegments);
    }

    @Override
    public void align() {
      for (int i = 1; i <= layers.length - 1; i++) {
        LV<V>[] currentLayer = layers[i];
        LV<V>[] previousLayerInSweep = layers[i - 1];
        int r =
            previousLayerInSweep.length
                + 1; // one past the last index in the previous layer of this sweep
        for (int k = currentLayer.length - 1; k >= 0; k--) {
          LV<V> vkofi = currentLayer[k];
          List<LV<V>> neighbors =
              neighborCache
                  .predecessorsOf(vkofi)
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
                if (notMarked(edge) && r > pos(um)) {
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
  public static class LeftmostLower<V, E> extends VerticalAlignment<V, E> {

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
              neighborCache
                  .successorsOf(vkofi)
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
                if (notMarked(edge) && r < pos(um)) {
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
  public static class RightmostLower<V, E> extends VerticalAlignment<V, E> {

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
            previousLayerInSweep.length
                + 1; // one past the last index in the previous layer of this sweep
        for (int k = currentLayer.length - 1; k >= 0; k--) {
          LV<V> vkofi = currentLayer[k];
          List<LV<V>> neighbors =
              neighborCache
                  .successorsOf(vkofi)
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
                if (notMarked(edge) && r > pos(um)) {
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
}
