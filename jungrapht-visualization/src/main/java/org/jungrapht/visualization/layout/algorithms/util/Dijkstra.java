package org.jungrapht.visualization.layout.algorithms.util;

import com.google.common.collect.Maps;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dijkstra<V> {

  private static final Logger log = LoggerFactory.getLogger(Dijkstra.class);

  public static class Pair<V> {
    final V first;
    final V second;

    public static <V> Pair<V> of(V first, V second) {
      return new Pair(first, second);
    }

    private Pair(V first, V second) {
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

  protected Graph<V, ?> graph;

  protected Map<Pair<V>, Integer> distanceMap = Maps.newHashMap();

  public Dijkstra(Graph<V, ?> graph) {
    this.graph = graph;
  }

  public Map<Pair<V>, Integer> getAllDistances() {
    Map<Pair<V>, Integer> distanceMap = Maps.newHashMap();
    for (V vertex : graph.vertexSet()) {
      Map<V, Integer> distances = getDistances(vertex);
      for (V n : distances.keySet()) {
        Pair<V> pair = Pair.of(vertex, n);
        if (distanceMap.containsKey(pair)) {
          log.trace(
              "about to replace {},{} with {},{},{}",
              pair,
              distanceMap.get(pair),
              vertex,
              n,
              distances.get(n));
        }
        if (distances.get(n) != null) {
          distanceMap.put(Pair.of(vertex, n), distances.get(n));
        }
      }
    }
    log.trace("distanceMap:{}", distanceMap);
    return distanceMap;
  }

  public double getDistance(V from, V to) {
    if (distanceMap.containsKey(Pair.of(from, to))) {
      return distanceMap.get(Pair.of(from, to));
    } else {
      Map<V, Integer> map = getDistances(from);
      for (V n : map.keySet()) {
        distanceMap.put(Pair.of(from, n), map.get(n));
      }
      return distanceMap.get(Pair.of(from, to));
    }
  }

  protected Map<V, Integer> getDistances(V source) {
    // initialize everything
    Queue<V> queue = new LinkedList<>();
    Map<V, Integer> distances = Maps.newHashMap();
    Map<V, V> previous = Maps.newLinkedHashMap();
    //    Map<V, Boolean> visitedMap = Maps.newHashMap();

    for (V vertex : graph.vertexSet()) {
      distances.put(vertex, Integer.MAX_VALUE);
      previous.put(vertex, null);
      queue.add(vertex);
      //        visitedMap.put(vertex, false);
    }

    distances.put(source, 0);

    while (!queue.isEmpty()) {
      V u = minFrom(queue, distances);
      //minValueFrom(distances, visitedMap);
      //      visitedMap.put(u, true);
      queue.remove(u);
      if (u == null) {
        break;
      }
      for (V v : Graphs.neighborListOf(graph, u)) {
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

  private V minFrom(Queue<V> queue, Map<V, Integer> distances) {
    double min = Double.POSITIVE_INFINITY;
    V winner = null;
    for (V vertex : queue) {
      if (distances.get(vertex) < min) {
        min = distances.get(vertex);
        winner = vertex;
      }
    }
    queue.remove(winner);
    if (winner == null) {
      log.info("winner null");
    }
    return winner;
  }

  private V minValueFrom(Map<V, Integer> distances, Map<V, Boolean> visited) {
    V minValue = null;
    int minDistance = Integer.MAX_VALUE;
    for (Map.Entry<V, Integer> entry : distances.entrySet()) {
      if (!visited.get(entry.getKey()) && entry.getValue() < minDistance) {
        minDistance = entry.getValue();
        minValue = entry.getKey();
      }
    }
    return minValue;
  }

  public Map<Pair<V>, Integer> getDistanceMap() {
    if (distanceMap.isEmpty()) {
      getAllDistances();
    }
    return distanceMap;
  }
}
