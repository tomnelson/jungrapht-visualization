package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
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
    return accumulatorTreeCount(edges);
  }

  /**
   * swap members i and j in the passed array, and update the index metadata in the swapped members
   * to reflect their new positions
   *
   * @param array an array with members to swap
   * @param i first of 2 members to swap
   * @param j second of 2 members to swap
   * @param <V> the vertex type
   */
  protected static <V> void swap(LV<V>[] array, int i, int j) {
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
  protected static <V> void swap(List<LV<V>> layer, int i, int j) {
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
  protected static <V, E> int[] getTargetIndices(List<LE<V, E>> edges) {
    int[] targetIndices = new int[edges.size()];
    for (int i = 0; i < edges.size(); i++) {
      LE<V, E> edge = edges.get(i);
      targetIndices[i] = edge.getTarget().getIndex();
    }
    return targetIndices;
  }

  /**
   * get an int array holding the indices of the target vertices for the passed edges
   *
   * @param edges edges to get target vertex indices for
   * @param <V> vertex type
   * @param <E> edge type
   * @return an array holding the edges
   */
  protected static <V, E> LE<V, E>[] getEdgeArray(List<LE<V, E>> edges) {
    LE<V, E>[] edgeArray = new LE[edges.size()];
    for (int i = 0; i < edges.size(); i++) {
      LE<V, E> edge = edges.get(i);
      edgeArray[i] = edge;
    }
    return edgeArray;
  }

  /**
   * count edge crossings in the passed List of edges
   *
   * @param edges edges to count crossings for
   * @param <V> vertex type
   * @param <E> edge type
   * @return the count of edge crossings
   */
  protected static <V, E> int accumulatorTreeCount(List<LE<V, E>> edges) {
    int[] targetIndices = getTargetIndices(edges);
    if (targetIndices.length == 0) {
      return 0;
    }
    int maxIndex = Arrays.stream(targetIndices).max().getAsInt();
    AccumulatorTree<V, E> accumulatorTree = new AccumulatorTree<>(maxIndex + 1);
    int atcount = accumulatorTree.crossCount(targetIndices);
    return atcount;
  }

  /**
   * count edge crossing weight in the passed List of edges
   *
   * @param edges edges to count crossings for
   * @param <V> vertex type
   * @param <E> edge type
   * @return the count of edge crossings
   */
  public static <V, E> int crossingWeight(
      List<LE<V, E>> edges, Function<Integer, Integer> weightFunction) {
    edges.sort(Comparators.biLevelEdgeComparator());
    return accumulatorTreeWeight(edges, weightFunction);
  }

  /**
   * count edge crossing weight in the passed List of edges
   *
   * @param edges edges to count crossings for
   * @param <V> vertex type
   * @param <E> edge type
   * @return the count of edge crossings
   */
  protected static <V, E> int accumulatorTreeWeight(
      List<LE<V, E>> edges, Function<Integer, Integer> weightFunction) {
    int[] targetIndices = getTargetIndices(edges);
    LE<V, E>[] edgeArray = getEdgeArray(edges);
    if (targetIndices.length == 0) {
      return 0;
    }
    int maxIndex = Arrays.stream(targetIndices).max().getAsInt();
    AccumulatorTree<V, E> accumulatorTree = new AccumulatorTree<>(maxIndex + 1);
    int atcount = accumulatorTree.crossWeight(edgeArray, weightFunction);
    return atcount;
  }
}
