package org.jungrapht.visualization.layout.algorithms.sugiyama;

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
 * @see "Simple and Efficient Bilayer Cross Counting. Wilhelm Barth, Petra Mutzel, Institut für
 *     Computergraphik und Algorithmen Technische Universität Wien, Michael Jünger, Institut für
 *     Informatik Universität zu Köln"
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LevelCross<V, E> {

  private static final Logger log = LoggerFactory.getLogger(LevelCross.class);

  private List<LE<V, E>> edges;
  private int levelSize;
  private int levelRank;
  private int successorLevelSize;
  private int successorLevelRank;

  private AccumulatorTree levelTree;
  private AccumulatorTree successorTree;

  public LevelCross(
      Graph<LV<V>, LE<V, E>> graph,
      int levelSize,
      int levelRank,
      int successorLevelSize,
      int successorLevelRank) {
    this(
        new ArrayList<>(graph.edgeSet()),
        levelSize,
        levelRank,
        successorLevelSize,
        successorLevelRank);
  }

  public LevelCross(
      List<LE<V, E>> edgeList,
      int levelSize,
      int levelRank,
      int successorLevelSize,
      int successorLevelRank) {
    this.edges = edgeList;
    this.levelSize = levelSize;
    this.levelRank = levelRank;
    this.successorLevelSize = successorLevelSize;
    this.successorLevelRank = successorLevelRank;
    this.levelTree = new AccumulatorTree(levelSize);
    this.successorTree = new AccumulatorTree(successorLevelSize);
  }

  public AccumulatorTree getLevelTree() {
    return levelTree;
  }

  public AccumulatorTree getSuccessorTree() {
    return successorTree;
  }

  boolean isTop(LE<V, E> edge) {
    return edge.getSource().getIndex() < edge.getTarget().getIndex();
  }

  boolean isBottom(LE<V, E> edge) {
    return edge.getSource().getIndex() > edge.getTarget().getIndex();
  }

  boolean isPair(LE<V, E> edge) {
    return edge.getSource().getIndex() == edge.getTarget().getIndex();
  }

  boolean isRight(LE<V, E> edge, int sweepPos) {
    return (edge.getSource().getIndex() == sweepPos && isTop(edge))
        || (edge.getTarget().getIndex() == sweepPos && isBottom(edge));
  }

  boolean isTrailing(LE<V, E> edge, int sweepPos) {
    return (edge.getSource().getIndex() < sweepPos && sweepPos < edge.getTarget().getIndex())
        || (edge.getTarget().getIndex() < sweepPos && sweepPos < edge.getSource().getIndex());
  }

  boolean isTrailingTop(LE<V, E> edge, int sweepPos) {
    return edge.getSource().getIndex() < sweepPos && sweepPos < edge.getTarget().getIndex();
  }

  boolean isTrailingBottom(LE<V, E> edge, int sweepPos) {
    return edge.getTarget().getIndex() < sweepPos && sweepPos < edge.getSource().getIndex();
  }

  public int levelCross() {
    int max = Arrays.stream(levelTree.tree).max().getAsInt();

    if (max != 0) throw new IllegalArgumentException("max " + max + " should be 0");
    List<LE<V, E>> rightTop = new ArrayList<>();
    List<LE<V, E>> rightBottom = new ArrayList<>();
    List<LE<V, E>> topTrailing = new ArrayList<>();
    List<LE<V, E>> bottomTrailing = new ArrayList<>();

    //trees should be all zeros
    int count = 0;
    for (int sweepPosition = 0;
        sweepPosition < Math.min(levelSize, successorLevelSize);
        sweepPosition++) {

      //      log.trace("top of loop. topTrailing: {}, bottomTrailing: {}", topTrailing, bottomTrailing);
      // remove trailing edges to sweepPos
      // if i changed any right edges to trailing edges last time thru,
      // remove any that and at sweepPos (top trailing with target at sweep
      // or bottom trailing with source at sweep
      for (LE<V, E> edge : topTrailing) {
        if (edge.getTarget().getIndex() == sweepPosition) {
          successorTree.subtractEdge(edge.getTarget().getIndex());
        }
      }
      for (LE<V, E> edge : bottomTrailing) {
        if (edge.getSource().getIndex() == sweepPosition) {
          levelTree.subtractEdge(edge.getSource().getIndex());
        }
      }
      // any left-over right edges that are trailing edges get
      // added to their accumulatorTree and to the topTrailing list, likewise for right bottom edges
      for (LE<V, E> edge : rightTop) {
        if (isTrailingTop(edge, sweepPosition)) {
          topTrailing.add(edge);
          successorTree.addEdge(edge.getTarget().getIndex());
        }
      }
      for (LE<V, E> edge : rightBottom) {
        if (isTrailingBottom(edge, sweepPosition)) {
          bottomTrailing.add(edge);
          levelTree.addEdge(edge.getSource().getIndex());
        }
      }
      // re-initialize things:

      List<LE<V, E>> filteredEdges =
          edges
              .stream()
              .filter(e -> e.getSource().getRank() == levelRank)
              .filter(e -> e.getTarget().getRank() == successorLevelRank)
              .collect(Collectors.toList());

      int pair = 0;
      int Rt = 0;
      int Rb = 0;
      int same = 0;
      rightTop.clear(); // will find new right edges from sweep
      rightBottom.clear(); // same

      int sweepPos = sweepPosition;
      List<LE<V, E>> topEdges =
          edges
              .stream()
              .filter(e -> levelRank == e.getSource().getRank())
              .filter(e -> e.getSource().getIndex() == sweepPos)
              .collect(Collectors.toList());

      for (LE<V, E> edge : topEdges) {
        int Li = sweepPosition;
        int x = edge.getTarget().getIndex();
        if (isRight(edge, sweepPosition)) {
          rightTop.add(edge);
          Rt++;
        } else if (isPair(edge)) {
          pair++;
        }
        same += successorTree.countEdges(x, successorTree.last);
      }
      // these are swapped from what is in the published algorithm:
      int trb = levelTree.tree[0];
      int trt = successorTree.tree[0];
      List<LE<V, E>> bottomEdges =
          edges
              .stream()
              .filter(e -> successorLevelRank == e.getTarget().getRank())
              .filter(e -> e.getTarget().getIndex() == sweepPos)
              .collect(Collectors.toList());

      for (LE<V, E> edge : bottomEdges) {
        int x = edge.getSource().getIndex();
        int Mi = sweepPosition;
        if (isRight(edge, sweepPosition)) {
          rightBottom.add(edge);
          Rb++;
          same += levelTree.countEdges(x, levelTree.last);
        }
      }
      count += trt * Rb + trb * (Rt + pair) + Rt * Rb + same;
    }

    return count;
  }

  @Override
  public String toString() {
    return "LevelCross{"
        + "\nedges="
        + edges
        + "\nlevelSize="
        + levelSize
        + "\nsuccessorLevelSize="
        + successorLevelSize
        + "\nlevelRank="
        + levelRank
        + "\nsuccessorLevelRank="
        + successorLevelRank
        + "\nlevelTree="
        + levelTree
        + "\nsuccessorTree="
        + successorTree
        + '}';
  }
}
