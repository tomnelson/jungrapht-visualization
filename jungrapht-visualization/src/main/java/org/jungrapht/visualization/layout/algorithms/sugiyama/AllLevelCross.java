package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.jgrapht.Graph;

public class AllLevelCross<V, E> {

  final LV<V>[][] levels;
  final Graph<LV<V>, LE<V, E>> graph;

  public AllLevelCross(Graph<LV<V>, LE<V, E>> graph, LV<V>[][] levels) {
    this.levels = levels;
    this.graph = graph;
  }

  public int allLevelCross() {
    int count = 0;
    for (int i = 0; i < levels.length - 2; i++) {
      LevelCross<V, E> levelCross =
          new LevelCross(graph, levels[i].length, i, levels[i + 1].length, i + 1);
      count += levelCross.levelCross();
    }
    return count;
  }
}
