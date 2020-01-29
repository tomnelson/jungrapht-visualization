package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;

/**
 * Represents 2 adjacent layers of a layered graph. If the processing direction is from top to
 * bottom:
 *
 * <p>
 *
 * <ul>
 *   <li>the current layer is 'i', then the downstream layer is 'i+1'
 *   <li>the join vertex is PVertex.class and the split vertex is QVertex.class
 * </ul>
 *
 * If the processing direction is from bottom to top:
 *
 * <p>
 *
 * <ul>
 *   <li>the current layer is 'i', then the downstream layer is 'i-1'
 *   <li>the join vertex is QVertex.class and the split vertex is PVertex.class
 * </ul>
 *
 * @param <V>
 */
public class BiLayer<V, E> {

  /** this is PVertex when proceeding top to bottom and is QVertex when proceeding bottom to top */
  final Predicate<LV<V>> joinVertexPredicate;

  /** this is QVertex when proceeding top to bottom and is PVertex when proceeding bottom to top */
  final Predicate<LV<V>> splitVertexPredicate;

  /** the current layer. Li in the paper */
  List<LV<V>> currentLayer;

  /**
   * the downstream layer. Li+1 in the paper If the direction is bottom to top, then this layer is
   * Li-1
   */
  List<LV<V>> downstreamLayer;

  LV<V>[] downstreamArray;

  final BiFunction<Graph<LV<V>, LE<V, E>>, LV<V>, List<LV<V>>> neighborFunction;

  Map<LV<V>, Integer> pos;

  Map<LV<V>, Integer> measure;

  int currentRank;
  int downstreamRank;

  public static <V, E> BiLayer<V, E> of(
      int currentRank,
      int downstreamRank,
      List<LV<V>> currentLayer,
      List<LV<V>> downstreamLayer,
      LV<V>[] downstreamArray,
      Predicate<LV<V>> joinVertexPredicate,
      Predicate<LV<V>> splitVertexPredicate,
      BiFunction<Graph<LV<V>, LE<V, E>>, LV<V>, List<LV<V>>> neighborFunction,
      Map<LV<V>, Integer> pos,
      Map<LV<V>, Integer> measure) {
    return new BiLayer(
        currentRank,
        downstreamRank,
        currentLayer,
        downstreamLayer,
        downstreamArray,
        joinVertexPredicate,
        splitVertexPredicate,
        neighborFunction,
        pos,
        measure);
  }

  private BiLayer(
      int currentRank,
      int downstreamRank,
      List<LV<V>> currentLayer,
      List<LV<V>> downstreamLayer,
      LV<V>[] downstreamArray,
      Predicate<LV<V>> joinVertexPredicate,
      Predicate<LV<V>> splitVertexPredicate,
      //BiFunction<Graph<LV<V>, LE<V, E>>, LV<V>, int[]> neighborIndexFunction,
      BiFunction<Graph<LV<V>, LE<V, E>>, LV<V>, List<LV<V>>> neighborFunction,
      Map<LV<V>, Integer> pos,
      Map<LV<V>, Integer> measure) {
    this.currentRank = currentRank;
    this.downstreamRank = downstreamRank;
    this.currentLayer = currentLayer;
    this.downstreamLayer = downstreamLayer;
    this.downstreamArray = downstreamArray;
    this.joinVertexPredicate = joinVertexPredicate;
    this.splitVertexPredicate = splitVertexPredicate;
    this.neighborFunction = neighborFunction;
    this.pos = pos;
    this.measure = measure;
  }

  int[] upperNeighborPoses(Graph<LV<V>, LE<V, E>> graph, LV<V> vertex) {
    return Graphs.predecessorListOf(graph, vertex)
        .stream()
        .mapToInt(v -> pos.getOrDefault(v, 0))
        .sorted()
        .toArray();
  }

  int[] lowerNeighborIndices(Graph<LV<V>, LE<V, E>> graph, LV<V> vertex) {
    return Graphs.successorListOf(graph, vertex)
        .stream()
        .mapToInt(v -> pos.getOrDefault(v, 0))
        .sorted()
        .toArray();
  }
}
