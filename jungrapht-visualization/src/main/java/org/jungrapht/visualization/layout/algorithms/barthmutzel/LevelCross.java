package org.jungrapht.visualization.layout.algorithms.barthmutzel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaEdge;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaVertex;
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

  private List<SugiyamaEdge<V, E>> edges;
  private int levelSize;
  private int levelRank;
  private int successorLevelSize;
  private int successorLevelRank;

  private AccumulatorTree levelTree;
  private AccumulatorTree successorTree;

  public LevelCross(
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph,
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
      List<SugiyamaEdge<V, E>> edgeList,
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

  boolean isTop(SugiyamaEdge<V, E> edge) {
    return edge.source.getIndex() < edge.target.getIndex();
  }

  boolean isBottom(SugiyamaEdge<V, E> edge) {
    return edge.source.getIndex() > edge.target.getIndex();
  }

  boolean isPair(SugiyamaEdge<V, E> edge) {
    return edge.source.getIndex() == edge.target.getIndex();
  }

  boolean isRight(SugiyamaEdge<V, E> edge, int sweepPos) {
    return (edge.source.getIndex() == sweepPos && isTop(edge))
        || (edge.target.getIndex() == sweepPos && isBottom(edge));
  }

  boolean isTrailing(SugiyamaEdge<V, E> edge, int sweepPos) {
    return (edge.source.getIndex() < sweepPos && sweepPos < edge.target.getIndex())
        || (edge.target.getIndex() < sweepPos && sweepPos < edge.source.getIndex());
  }

  boolean isTrailingTop(SugiyamaEdge<V, E> edge, int sweepPos) {
    return edge.source.getIndex() < sweepPos && sweepPos < edge.target.getIndex();
  }

  boolean isTrailingBottom(SugiyamaEdge<V, E> edge, int sweepPos) {
    return edge.target.getIndex() < sweepPos && sweepPos < edge.source.getIndex();
  }

  public int levelCross() {
    int max = Arrays.stream(levelTree.tree).max().getAsInt();

    if (max != 0) throw new IllegalArgumentException("max " + max + " should be 0");
    List<SugiyamaEdge<V, E>> rightTop = new ArrayList<>();
    List<SugiyamaEdge<V, E>> rightBottom = new ArrayList<>();
    List<SugiyamaEdge<V, E>> topTrailing = new ArrayList<>();
    List<SugiyamaEdge<V, E>> bottomTrailing = new ArrayList<>();

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
      for (SugiyamaEdge<V, E> edge : topTrailing) {
        if (edge.target.getIndex() == sweepPosition) {
          successorTree.subtractEdge(edge.target.getIndex());
        }
      }
      for (SugiyamaEdge<V, E> edge : bottomTrailing) {
        if (edge.source.getIndex() == sweepPosition) {
          levelTree.subtractEdge(edge.source.getIndex());
        }
      }
      // any left-over right edges that are trailing edges get
      // added to their accumulatorTree and to the topTrailing list, likewise for right bottom edges
      for (SugiyamaEdge<V, E> edge : rightTop) {
        if (isTrailingTop(edge, sweepPosition)) {
          topTrailing.add(edge);
          successorTree.addEdge(edge.target.getIndex());
        }
      }
      for (SugiyamaEdge<V, E> edge : rightBottom) {
        if (isTrailingBottom(edge, sweepPosition)) {
          bottomTrailing.add(edge);
          levelTree.addEdge(edge.source.getIndex());
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
      List<SugiyamaEdge<V, E>> topEdges =
          edges
              .stream()
              .filter(e -> levelRank == e.source.getRank())
              .filter(e -> e.source.getIndex() == sweepPos)
              .collect(Collectors.toList());

      for (SugiyamaEdge<V, E> edge : topEdges) {
        int Li = sweepPosition;
        int x = edge.target.getIndex();
        if (isRight(edge, sweepPosition)) {
          rightTop.add(edge); ////////////////////////////////////
          Rt++;
        } else if (isPair(edge)) {
          pair++;
        }
        same += successorTree.countEdges(x, successorTree.last);
      }
      // these are swapped from what is in the published algorithm:
      int trb = levelTree.tree[0];
      int trt = successorTree.tree[0];
      List<SugiyamaEdge<V, E>> bottomEdges =
          edges
              .stream()
              .filter(e -> successorLevelRank == e.target.getRank())
              .filter(e -> e.target.getIndex() == sweepPos)
              .collect(Collectors.toList());

      for (SugiyamaEdge<V, E> edge : bottomEdges) {
        int x = edge.source.getIndex();
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
