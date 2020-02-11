package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccumulatorTreeUtil {

  private static final Logger log = LoggerFactory.getLogger(AccumulatorTreeUtil.class);

  /**
   * count edge crossings in the passed List of edges
   *
   * @param edges edges to count crossings for
   * @param <V> vertex type
   * @param <E> edge type
   * @return the count of edge crossings
   */
  public static <V, E> int crossingCount(List<LE<V, E>> edges) {
    edges.sort(Comparators.biLevelEdgeComparator());
    //    log.info("biLayerEdges");
    //    edges.forEach(e -> log.info(" - {}", e.toString()));
    return accumulatorTreeCount(edges);
  }

  public static <V, E> int crossingCount(List<LE<V, E>> edges, int size) {
    edges.sort(Comparators.biLevelEdgeComparator());
    //    log.info("biLayerEdges");
    //    edges.forEach(e -> log.info(" - {}", e.toString()));
    return accumulatorTreeCount(edges, size);
  }

  /**
   * @param i first of 2 indices to swap
   * @param j second of 2 indices to swap
   * @param layer array holding vertices in order. i and j will be swapped, counted, then swapped
   *     back
   * @param edges edges to count crossings for
   * @param <V> vertex type
   * @param <E> edge type
   * @return the count of edge crossings
   */
  public static <V, E> int crossingCountSwapped(int i, int j, LV<V>[] layer, List<LE<V, E>> edges) {
    swap(layer, i, j);
    Collections.swap(edges, i, j);
    edges.sort(Comparators.biLevelEdgeComparator());
    //    log.info("swapped {} {} biLayerEdges", i, j);
    //    edges.forEach(e -> log.info(" - {}", e.toString()));
    int count = accumulatorTreeCount(edges);
    swap(layer, i, j);
    Collections.swap(edges, i, j);
    return count;
  }

  public static <V, E> int crossingCountSwapped(
      int i, int j, LV<V>[] layer, List<LE<V, E>> edges, int size) {
    edges.sort(Comparators.biLevelEdgeComparator());
    Collections.swap(edges, i, j);
    swap(layer, i, j);
    int count = accumulatorTreeCount(edges, size);
    swap(layer, i, j);
    Collections.swap(edges, i, j);
    return count;
  }

  /**
   * @param i first of 2 indices to swap
   * @param j second of 2 indices to swap
   * @param layer List holding vertices in order. i and j will be swapped, counted, then swapped
   *     back
   * @param edges edges to count crossings for
   * @param <V> vertex type
   * @param <E> edge type
   * @return the count of edge crossings
   */
  public static <V, E> int crossingCountSwapped(
      int i, int j, List<LV<V>> layer, List<LE<V, E>> edges) {
    swap(layer, i, j);
    edges.sort(Comparators.biLevelEdgeComparator());
    int count = accumulatorTreeCount(edges);
    swap(layer, i, j);
    return count;
  }

  public static <V, E> int crossingCountSwapped(
      int i, int j, List<LV<V>> layer, List<LE<V, E>> edges, int size) {
    swap(layer, i, j);
    edges.sort(Comparators.biLevelEdgeComparator());
    int count = accumulatorTreeCount(edges, size);
    swap(layer, i, j);
    return count;
  }

  // private methods
  /**
   * swap members i and j in the passed array, and update the index metadata in the swapped members
   * to reflect their new positions
   *
   * @param array an array with members to swap
   * @param i first of 2 members to swap
   * @param j second of 2 members to swap
   * @param <V> the vertex type
   */
  private static <V> void swap(LV<V>[] array, int i, int j) {
    LV<V> temp = array[i];
    array[i] = array[j];
    array[j] = temp;
    array[i].setIndex(i);
    array[j].setIndex(j);
  }

  /**
   * swap members i and j in the passed List, and update the index metadata in the swapped members
   * to reflect their new positions
   *
   * @param layer a List with members to swap
   * @param i first of 2 members to swap
   * @param j second of 2 members to swap
   * @param <V> the vertex type
   */
  private static <V> void swap(List<LV<V>> layer, int i, int j) {
    Collections.swap(layer, i, j);
    layer.get(i).setIndex(i);
    layer.get(j).setIndex(j);
  }

  /**
   * get an int array holding the indices of the target vertices for the passed edges
   *
   * @param edges edges to get target vertex indices for
   * @param <V> vertex type
   * @param <E> edge type
   * @return an int array holding the target indices for the passed edges
   */
  private static <V, E> int[] getTargetIndices(List<LE<V, E>> edges) {
    int[] targetIndices = new int[edges.size()];
    for (int i = 0; i < edges.size(); i++) {
      LE<V, E> edge = edges.get(i);
      targetIndices[i] = edge.getTarget().getIndex();
    }
    return targetIndices;
  }

  /**
   * count edge crossings in the passed List of edges
   *
   * @param edges edges to count crossings for
   * @param <V> vertex type
   * @param <E> edge type
   * @return the count of edge crossings
   */
  private static <V, E> int accumulatorTreeCount(List<LE<V, E>> edges) {
    int[] targetIndices = getTargetIndices(edges);
    //    log.info("targetIndicies len:{}, size:{}", targetIndices.length, size);
    AccumulatorTree<V, E> accumulatorTree = new AccumulatorTree<>(edges);
    int atcount = accumulatorTree.crossCount(targetIndices);
    return atcount;
  }

  private static <V, E> int accumulatorTreeCount(List<LE<V, E>> edges, int size) {
    int[] targetIndices = getTargetIndices(edges);

    //    if (edges.size() != size) {
    //      log.info("sizes differ");
    //    }
    size = Math.max(size, edges.size());
    //    log.info("targetIndicies len:{}, size:{}", targetIndices.length, size);
    AccumulatorTree<V, E> accumulatorTree = new AccumulatorTree<>(size);
    int atcount = accumulatorTree.crossCount(targetIndices);
    return atcount;
  }
}
