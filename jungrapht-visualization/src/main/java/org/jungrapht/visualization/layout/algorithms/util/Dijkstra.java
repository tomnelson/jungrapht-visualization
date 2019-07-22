package org.jungrapht.visualization.layout.algorithms.util;

import com.google.common.collect.Maps;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dijkstra<N> {

  private static final Logger log = LoggerFactory.getLogger(Dijkstra.class);

  public static class Pair<N> {
    final N first;
    final N second;

    public static <N> Pair<N> of(N first, N second) {
      return new Pair(first, second);
    }

    private Pair(N first, N second) {
      this.first = first;
      this.second = second;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Pair<?> pair = (Pair<?>) o;

      if (!first.equals(pair.first) && !first.equals(pair.second)) return false;
      return second.equals(pair.second) || second.equals(pair.first);
    }

    @Override
    public int hashCode() {
      return first.hashCode() + second.hashCode();
    }

    @Override
    public String toString() {
      return "Pair{" + first + "," + second + '}';
    }
  }

  protected Graph<N, ?> graph;

  protected Map<Pair<N>, Integer> distanceMap = Maps.newHashMap();

  public Dijkstra(Graph<N, ?> graph) {
    this.graph = graph;
  }

  public Map<Pair<N>, Integer> getAllDistances() {
    Map<Pair<N>, Integer> distanceMap = Maps.newHashMap();
    for (N node : graph.vertexSet()) {
      Map<N, Integer> distances = getDistances(node);
      for (N n : distances.keySet()) {
        Pair<N> pair = Pair.of(node, n);
        if (distanceMap.containsKey(pair)) {
          log.trace(
              "about to replace {},{} with {},{},{}",
              pair,
              distanceMap.get(pair),
              node,
              n,
              distances.get(n));
        }
        if (distances.get(n) != null) {
          distanceMap.put(Pair.of(node, n), distances.get(n));
        }
      }
    }
    log.trace("distanceMap:{}", distanceMap);
    return distanceMap;
  }

  public double getDistance(N from, N to) {
    if (distanceMap.containsKey(Pair.of(from, to))) {
      return distanceMap.get(Pair.of(from, to));
    } else {
      Map<N, Integer> map = getDistances(from);
      for (N n : map.keySet()) {
        distanceMap.put(Pair.of(from, n), map.get(n));
      }
      return distanceMap.get(Pair.of(from, to));
    }
  }

  protected Map<N, Integer> getDistances(N source) {
    // initialize everything
    Queue<N> queue = new LinkedList<>();
    Map<N, Integer> distances = Maps.newHashMap();
    Map<N, N> previous = Maps.newLinkedHashMap();
    //    Map<N, Boolean> visitedMap = Maps.newHashMap();

    for (N node : graph.vertexSet()) {
      distances.put(node, Integer.MAX_VALUE);
      previous.put(node, null);
      queue.add(node);
      //        visitedMap.put(node, false);
    }

    distances.put(source, 0);

    while (!queue.isEmpty()) {
      N u = minFrom(queue, distances);
      //minValueFrom(distances, visitedMap);
      //      visitedMap.put(u, true);
      queue.remove(u);
      if (u == null) {
        break;
      }
      for (N v : Graphs.neighborListOf(graph, u)) {
        if (queue.contains(v)) {
          //        if (!visitedMap.get(v)) {
          int dist = distances.get(u) + 1;
          if (dist < distances.get(v)) {
            distances.put(v, dist);
            previous.put(v, u);
          }
          //        }
        }
      }
    }
    return distances;
  }

  private N minFrom(Queue<N> queue, Map<N, Integer> distances) {
    double min = Double.POSITIVE_INFINITY;
    N winner = null;
    for (N node : queue) {
      if (distances.get(node) < min) {
        min = distances.get(node);
        winner = node;
      }
    }
    queue.remove(winner);
    if (winner == null) {
      log.info("winner null");
    }
    return winner;
  }

  private N minValueFrom(Map<N, Integer> distances, Map<N, Boolean> visited) {
    N minValue = null;
    int minDistance = Integer.MAX_VALUE;
    for (Map.Entry<N, Integer> entry : distances.entrySet()) {
      if (!visited.get(entry.getKey()) && entry.getValue() < minDistance) {
        minDistance = entry.getValue();
        minValue = entry.getKey();
      }
    }
    return minValue;
  }

  public Map<Pair<N>, Integer> getDistanceMap() {
    if (distanceMap.isEmpty()) {
      getAllDistances();
    }
    return distanceMap;
  }
}
