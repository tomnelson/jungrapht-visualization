package org.jungrapht.visualization.layout.algorithms.barthmutzel;

import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;

/**
 * Counts edge crossing on all levels
 *
 * @see "W. Barth, M. Jünger, and P. Mutzel. Simple and efficient bilayer cross counting"
 * @see "V. Waddle and A. Malhotra, An E log E line crossing algorithm for levelled graphs"
 */
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
