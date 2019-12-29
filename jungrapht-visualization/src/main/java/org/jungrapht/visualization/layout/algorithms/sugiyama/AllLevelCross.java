package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.jgrapht.Graph;

public class AllLevelCross<V, E> {

  final SugiyamaVertex<V>[][] levels;
  final Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph;

  public AllLevelCross(
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph, SugiyamaVertex<V>[][] levels) {
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
