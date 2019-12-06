package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.List;
import org.jgrapht.Graph;

public class AllLevelCross<V, E> {

  final List<List<SugiyamaVertex<V>>> levels;
  final Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph;

  public AllLevelCross(
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph, List<List<SugiyamaVertex<V>>> levels) {
    this.levels = levels;
    this.graph = graph;
  }

  public int allLevelCross() {
    int count = 0;
    for (int i = 0; i < levels.size() - 2; i++) {
      LevelCross<V, E> levelCross = new LevelCross(graph, levels.get(i), levels.get(i + 1));
      count += levelCross.levelCross();
    }
    return count;
  }
}
