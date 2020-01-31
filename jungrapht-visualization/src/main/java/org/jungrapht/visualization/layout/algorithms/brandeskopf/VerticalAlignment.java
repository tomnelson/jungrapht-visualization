package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class VerticalAlignment<V, E> {

  private static final Logger log = LoggerFactory.getLogger(VerticalAlignment.class);

  protected Map<LV<V>, LV<V>> rootMap = new HashMap<>();
  protected Map<LV<V>, LV<V>> alignMap = new HashMap<>();
  protected LV<V>[][] layers;
  protected Graph<LV<V>, LE<V, E>> svGraph;
  protected Set<LE<V, E>> markedSegments;

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
    this.markedSegments = markedSegments;
    // initialize root and align
    Arrays.stream(layers)
        .flatMap(Arrays::stream)
        .forEach(
            v -> {
              rootMap.put(v, v);
              alignMap.put(v, v);
            });
    align();
  }

  boolean notMarked(LE<V, E> edge) {
    return !markedSegments.contains(edge);
  }

  LV<V> root(LV<V> v) {
    return rootMap.get(v);
  }

  void root(LV<V> k, LV<V> v) {
    rootMap.put(k, v);
  }

  LV<V> align(LV<V> v) {
    return alignMap.get(v);
  }

  void align(LV<V> k, LV<V> v) {
    alignMap.put(k, v);
  }

  int pos(LV<V> v) {
    return v.getIndex();
  }

  int alignMoveCursor(LV<V> um, LV<V> vkofi) {
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
      //    for (int i=1; i< layers.size(); i++) {
      for (int i = 0; i <= layers.length - 1; i++) { // zero based
        int r = -1;
        LV<V>[] currentLayer = layers[i];
        //      for (int k=1; k <= currentLayer.size(); k++) {
        for (int k = 0; k <= currentLayer.length - 1; k++) { // zero based
          // if the vertex at k has source nodes
          LV<V> vkofi = currentLayer[k];
          List<LV<V>> neighbors =
              Graphs.predecessorListOf(svGraph, vkofi)
                  .stream()
                  .sorted(Comparator.comparingInt(LV::getIndex))
                  .collect(Collectors.toList());
          log.trace("predecessors of {} are {}", vkofi, neighbors);
          int d = neighbors.size();
          if (d > 0) {
            int floor = (int) Math.floor((d - 1) / 2.0); // zero based
            int ceil = (int) Math.ceil((d - 1) / 2.0); // zero based
            log.trace("ceil: {}, floor: {}", ceil, floor);
            for (int m : new LinkedHashSet<>(Arrays.asList(floor, ceil))) {
              if (align(vkofi) == vkofi) {
                LV<V> um = neighbors.get(m);
                // if edge um->vkofi is not marked
                LE<V, E> edge = svGraph.getEdge(um, vkofi);
                if (notMarked(edge) && r < pos(um)) {
                  r = alignMoveCursor(um, vkofi);
                }
              }
            }
          }
        }
      }
    }
  }

  public static class RightmostUpper<V, E> extends VerticalAlignment<V, E> {
    public RightmostUpper(
        LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {
      super(layers, svGraph, markedSegments);
    }

    @Override
    public void align() {
      //    for (int i=1; i< layers.size(); i++) {
      for (int i = 1; i <= layers.length - 1; i++) { // zero based
        LV<V>[] currentLayer = layers[i];
        LV<V>[] previousLayer = layers[i - 1];
        int r = previousLayer.length + 1;
        for (int k = currentLayer.length - 1; k >= 0; k--) {
          LV<V> vkofi = currentLayer[k];
          List<LV<V>> neighbors =
              Graphs.predecessorListOf(svGraph, vkofi)
                  .stream()
                  .sorted(Comparator.comparingInt(LV::getIndex))
                  .collect(Collectors.toList());
          int d = neighbors.size();
          if (d > 0) {
            int floor = (int) Math.floor((d - 1) / 2.0); // zero based
            int ceil = (int) Math.ceil((d - 1) / 2.0); // zero based
            for (int m : new LinkedHashSet<>(Arrays.asList(ceil, floor))) {
              if (align(vkofi) == vkofi) {
                LV<V> um = neighbors.get(m);
                LE<V, E> edge = svGraph.getEdge(um, vkofi);
                if (notMarked(edge) && r > pos(um)) {
                  r = alignMoveCursor(um, vkofi);
                }
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
    public LeftmostLower(
        LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {
      super(layers, svGraph, markedSegments);
    }

    @Override
    public void align() {
      for (int i = layers.length - 2; i >= 0; i--) { // zero based
        int r = -1;
        LV<V>[] currentLayer = layers[i];
        for (int k = 0; k <= currentLayer.length - 1; k++) { // zero based
          LV<V> vkofi = currentLayer[k];
          List<LV<V>> neighbors =
              Graphs.successorListOf(svGraph, vkofi)
                  .stream()
                  .sorted(Comparator.comparingInt(LV::getIndex))
                  .collect(Collectors.toList());
          log.trace("successors of {} are {}", vkofi, neighbors);
          int d = neighbors.size();
          if (d > 0) {
            int floor = (int) Math.floor((d - 1) / 2.0); // zero based
            int ceil = (int) Math.ceil((d - 1) / 2.0); // zero based
            for (int m : new LinkedHashSet<>(Arrays.asList(floor, ceil))) {
              if (align(vkofi) == vkofi) {
                LV<V> um = neighbors.get(m);
                // if edge um->vkofi is not marked
                LE<V, E> edge = svGraph.getEdge(vkofi, um);
                if (notMarked(edge) && r < pos(um)) {
                  r = alignMoveCursor(um, vkofi);
                }
              }
            }
          }
        }
      }
    }
  }

  public static class RightmostLower<V, E> extends VerticalAlignment<V, E> {
    public RightmostLower(
        LV<V>[][] layers, Graph<LV<V>, LE<V, E>> svGraph, Set<LE<V, E>> markedSegments) {
      super(layers, svGraph, markedSegments);
    }

    @Override
    public void align() {
      //    for (int i=1; i< layers.size(); i++) {
      for (int i = layers.length - 2; i >= 0; i--) { // zero based
        //                for (int i = 0; i < layers.size() - 1; i++) { // zero based
        LV<V>[] currentLayer = layers[i];
        LV<V>[] nextLayer = layers[i + 1];
        int r = nextLayer.length + 1;
        //      for (int k=1; k <= currentLayer.size(); k++) {
        for (int k = currentLayer.length - 1; k >= 0; k--) {
          //                    for (int k = 0; k <= currentLayer.size() - 1; k++) { // zero based
          // if the vertex at k has target nodes
          LV<V> vkofi = currentLayer[k];
          List<LV<V>> neighbors =
              Graphs.successorListOf(svGraph, vkofi)
                  .stream()
                  .sorted(Comparator.comparingInt(LV::getIndex))
                  .collect(Collectors.toList());
          int d = neighbors.size();
          if (d > 0) {
            int floor = (int) Math.floor((d - 1) / 2.0); // zero based
            int ceil = (int) Math.ceil((d - 1) / 2.0); // zero based
            for (int m : new LinkedHashSet<>(Arrays.asList(ceil, floor))) {
              if (align(vkofi) == vkofi) {
                LV<V> um = neighbors.get(m);
                // if edge vm->v is not marked
                LE<V, E> edge = svGraph.getEdge(vkofi, um);
                if (notMarked(edge) && r > pos(um)) {
                  r = alignMoveCursor(um, vkofi);
                }
              }
            }
          }
        }
      }
    }
  }
}
