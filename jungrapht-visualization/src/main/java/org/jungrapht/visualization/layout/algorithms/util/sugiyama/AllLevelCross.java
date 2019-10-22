package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.List;
import org.jgrapht.Graph;

public class AllLevelCross<V, E> {

  final List<List<SV<V>>> levels;
  final Graph<SV<V>, SE<V, E>> graph;

  public AllLevelCross(Graph<SV<V>, SE<V, E>> graph, List<List<SV<V>>> levels) {
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
