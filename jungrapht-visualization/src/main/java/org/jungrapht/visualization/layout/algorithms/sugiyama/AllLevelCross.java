package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.jgrapht.Graph;

/**
 * Counts edge crossing on all levels
 *
 * @see "An E log E Line Crossing Algorithm for Levelled Graphs. Vance Waddle and Ashok Malhotra IBM
 *     Thomas J. Watson Research Center"
 * @see "Simple and Efficient Bilayer Cross Counting. Wilhelm Barth, Petra Mutzel, Institut für
 *     Computergraphik und Algorithmen Technische Universität Wien, Michael Jünger, Institut für
 *     Informatik Universität zu Köln"
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
