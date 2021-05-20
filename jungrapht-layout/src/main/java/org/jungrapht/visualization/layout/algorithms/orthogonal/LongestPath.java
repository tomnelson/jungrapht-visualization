package org.jungrapht.visualization.layout.algorithms.orthogonal;

import java.util.*;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.NeighborCache;
import org.jungrapht.visualization.layout.model.Rectangle;

public class LongestPath {

  Map<Rectangle, Integer> indices = new HashMap<>();
  Map<Rectangle, Integer> ranks = new LinkedHashMap<>();
  Map<Rectangle, Rectangle> roots = new LinkedHashMap<>();

  public List<List<Rectangle>> longestPath(Graph<Rectangle, Integer> dag) {
    return longestPath(dag, new NeighborCache<>(dag));
  }

  public List<List<Rectangle>> longestPath(
      Graph<Rectangle, Integer> dag, NeighborCache<Rectangle, Integer> neighborCache) {
    List<List<Rectangle>> list = new ArrayList<>();

    List<Rectangle> endpoints =
        dag.vertexSet().stream().filter(v -> dag.degreeOf(v) > 0).collect(Collectors.toList());
    endpoints.forEach(v -> roots.put(v, v));

    endpoints.sort(Comparator.comparingDouble(r -> r.x));

    Set<Rectangle> U = new HashSet<>();
    Set<Rectangle> Z = new HashSet<>();
    Set<Rectangle> V = new LinkedHashSet<>(endpoints);
    int currentLayer = 0;
    list.add(new ArrayList<>());

    while (U.size() != endpoints.size()) {
      Optional<Rectangle> optional =
          V.stream()
              .filter(v -> !U.contains(v))
              .filter(v -> Z.containsAll(neighborCache.successorsOf(v)))
              .findFirst();
      if (optional.isPresent()) {
        Rectangle got = optional.get();
        //        got.setRank(currentLayer);
        list.get(currentLayer).add(got);
        U.add(got);
      } else {
        currentLayer++;
        list.add(new ArrayList<>());
        Z.addAll(U);
      }
    }

    Collections.reverse(list);
    for (int i = 0; i < list.size(); i++) {
      List<Rectangle> layer = list.get(i);
      for (int j = 0; j < layer.size(); j++) {
        Rectangle v = layer.get(j);
        //                roots.put(v, layer.get(0));
        ranks.put(v, i);
        // if i > 0, then then vertex at layer[0] is the root
        roots.put(v, layer.get(0));
        indices.put(v, j);
      }
    }
    return list;
  }

  public Map<Rectangle, Integer> getRanks() {
    return this.ranks;
  }

  public Map<Rectangle, Integer> getIndices() {
    return this.indices;
  }

  public Map<Rectangle, Rectangle> getRoots() {
    return this.roots;
  }
}
