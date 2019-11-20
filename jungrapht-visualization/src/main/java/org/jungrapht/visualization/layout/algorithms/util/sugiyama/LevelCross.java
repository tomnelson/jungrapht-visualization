package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the LevelCross algorithm
 *
 * @see "An E log E Line Crossing Algorithm for Levelled Graphs. Vance Waddle and Ashok Malhotra IBM
 *     Thomas J. Watson Research Center"
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LevelCross<V, E> {

  private static final Logger log = LoggerFactory.getLogger(LevelCross.class);

  private List<SE<V, E>> edges;

  private List<SV<V>> level;
  private List<SV<V>> successorLevel;

  private AccumulatorTree levelTree;
  private AccumulatorTree successorTree;

  public LevelCross(Graph<SV<V>, SE<V, E>> graph, List<SV<V>> level, List<SV<V>> successorLevel) {
    this(new ArrayList<>(graph.edgeSet()), level, successorLevel);
  }

  public LevelCross(List<SE<V, E>> edgeList, List<SV<V>> level, List<SV<V>> successorLevel) {
    this.edges = edgeList;
    this.level = level;
    this.successorLevel = successorLevel;
    this.levelTree = new AccumulatorTree(level.size());
    this.successorTree = new AccumulatorTree(successorLevel.size());
  }

  public AccumulatorTree getLevelTree() {
    return levelTree;
  }

  public AccumulatorTree getSuccessorTree() {
    return successorTree;
  }

  boolean isTop(SE<V, E> edge) {
    return edge.source.index < edge.target.index;
  }

  boolean isBottom(SE<V, E> edge) {
    return edge.source.index > edge.target.index;
  }

  boolean isPair(SE<V, E> edge) {
    return edge.source.index == edge.target.index;
  }

  boolean isRight(SE<V, E> edge, int sweepPos) {
    return (edge.source.index == sweepPos && isTop(edge))
        || (edge.target.index == sweepPos && isBottom(edge));
  }

  boolean isTrailing(SE<V, E> edge, int sweepPos) {
    return (edge.source.index < sweepPos && sweepPos < edge.target.index)
        || (edge.target.index < sweepPos && sweepPos < edge.source.index);
  }

  boolean isTrailingTop(SE<V, E> edge, int sweepPos) {
    return edge.source.index < sweepPos && sweepPos < edge.target.index;
  }

  boolean isTrailingBottom(SE<V, E> edge, int sweepPos) {
    return edge.target.index < sweepPos && sweepPos < edge.source.index;
  }

  public int levelCross() {
    int max = Arrays.stream(levelTree.accumulatorTree).max().getAsInt();

    assert max == 0;
    List<SE<V, E>> rightTop = new ArrayList<>();
    List<SE<V, E>> rightBottom = new ArrayList<>();
    List<SE<V, E>> topTrailing = new ArrayList<>();
    List<SE<V, E>> bottomTrailing = new ArrayList<>();

    //trees should be all zeros
    int count = 0;
    for (int sweepPosition = 0;
        sweepPosition < Math.min(level.size(), successorLevel.size());
        sweepPosition++) {

      //      log.trace("top of loop. topTrailing: {}, bottomTrailing: {}", topTrailing, bottomTrailing);
      // remove trailing edges to sweepPos
      // if i changed any right edges to trailing edges last time thru,
      // remove any that and at sweepPos (top trailing with target at sweep
      // or bottom trailing with source at sweep
      for (SE<V, E> edge : topTrailing) {
        if (edge.target.index == sweepPosition) {
          successorTree.subtractEdge(edge.target.index);
        }
      }
      for (SE<V, E> edge : bottomTrailing) {
        if (edge.source.index == sweepPosition) {
          levelTree.subtractEdge(edge.source.index);
        }
      }
      // any left-over right edges that are trailing edges get
      // added to their accumulatorTree and to the topTrailing list, likewise for right bottom edges
      //      log.trace("top of loop. rightTop: {}, rightBottom: {}", rightTop, rightBottom);
      for (SE<V, E> edge : rightTop) {
        if (isTrailingTop(edge, sweepPosition)) {
          topTrailing.add(edge);
          successorTree.addEdge(edge.target.index);
        }
      }
      for (SE<V, E> edge : rightBottom) {
        if (isTrailingBottom(edge, sweepPosition)) {
          bottomTrailing.add(edge);
          levelTree.addEdge(edge.source.index);
        }
      }
      // re-initialize things:

      int pair = 0;
      int Rt = 0;
      int Rb = 0;
      int same = 0;
      rightTop.clear(); // will find new right edges from sweep
      rightBottom.clear(); // same

      int sweepPos = sweepPosition;
      List<SE<V, E>> topEdges =
          edges
              .stream()
              .filter(e -> level.contains(e.source))
              .filter(e -> e.source.index == sweepPos)
              .collect(Collectors.toList());

      //      log.trace("at sweep {}, topEdges are {}", sweepPosition, topEdges);

      for (SE<V, E> edge : topEdges) {
        int Li = sweepPosition;
        int x = edge.target.index;
        if (isRight(edge, sweepPosition)) {
          //          log.trace("topEdge {} is a right edge", edge);
          rightTop.add(edge);
          Rt++;
        } else if (isPair(edge)) {
          //          log.trace("topEdge {} is a pairEdge", edge);
          pair++;
        }
        same += successorTree.countEdges(x, successorTree.last);
      }
      // these are swapped from what is in the published algorithm:
      int trb = levelTree.accumulatorTree[0];
      int trt = successorTree.accumulatorTree[0];
      List<SE<V, E>> bottomEdges =
          edges
              .stream()
              .filter(e -> successorLevel.contains(e.target))
              .filter(e -> e.target.index == sweepPos)
              .collect(Collectors.toList());

      //      log.trace("at sweep {}, bottomEdges are {}", bottomEdges);
      for (SE<V, E> edge : bottomEdges) {
        int x = edge.source.index;
        int Mi = sweepPosition;
        if (isRight(edge, sweepPosition)) {
          //          log.trace("bottomEdge {} is a right edge", edge);
          rightBottom.add(edge);
          Rb++;
          same += levelTree.countEdges(x, levelTree.last);
        }
      }
      count += trt * Rb + trb * (Rt + pair) + Rt * Rb + same;
      //      if (count > 0) {
      //        log.trace("count: {}", count);
      //      }
    }

    return count;
  }

  @Override
  public String toString() {
    return "LevelCross{"
        + "\nedges="
        + edges
        + "\nlevel="
        + level
        + "\nsuccessorLevel="
        + successorLevel
        + "\nlevelTree="
        + levelTree
        + "\nsuccessorTree="
        + successorTree
        + '}';
  }
}
