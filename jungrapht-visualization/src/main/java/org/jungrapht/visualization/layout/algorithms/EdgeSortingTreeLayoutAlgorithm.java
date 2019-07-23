/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */

package org.jungrapht.visualization.layout.algorithms;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class EdgeSortingTreeLayoutAlgorithm<N, E> extends TreeLayoutAlgorithm<N>
    implements LayoutAlgorithm<N> {

  private static final Logger log = LoggerFactory.getLogger(EdgeSortingTreeLayoutAlgorithm.class);

  public static class Builder<
          N, E, T extends EdgeSortingTreeLayoutAlgorithm<N, E>, B extends Builder<N, E, T, B>>
      extends TreeLayoutAlgorithm.Builder<N, T, B> {

    private Comparator<E> edgeComparator;

    protected B self() {
      return (B) this;
    }

    public B edgeComparator(Comparator<E> edgeComparator) {
      this.edgeComparator = edgeComparator;
      return self();
    }

    public T build() {
      return (T) new EdgeSortingTreeLayoutAlgorithm<>(this);
    }
  }

  public static <N, E> Builder<N, E, ?, ?> sortingBuilder() {
    return new Builder<>();
  }

  protected EdgeSortingTreeLayoutAlgorithm(Builder<N, E, ?, ?> builder) {
    this(builder.horizontalNodeSpacing, builder.verticalNodeSpacing, builder.edgeComparator);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param edgeComparator sorts the edges
   */
  protected EdgeSortingTreeLayoutAlgorithm(
      int horizontalNodeSpacing, int verticalNodeSpacing, Comparator<E> edgeComparator) {
    super(horizontalNodeSpacing, verticalNodeSpacing);
    this.edgeComparator = edgeComparator;
  }

  protected Comparator<E> edgeComparator;

  public void setEdgeComparator(Comparator<E> edgeComparator) {
    this.edgeComparator = edgeComparator;
  }

  @Override
  protected void buildTree(LayoutModel<N> layoutModel, N node, int x) {
    Graph<N, E> graph = (Graph<N, E>) layoutModel.getGraph();
    if (alreadyDone.add(node)) {
      //go one level further down
      double newY = this.currentY + this.verticalNodeSpacing;
      this.currentX = x;
      this.currentY = newY;
      log.trace("Set node {} to {}", node, Point.of(currentX, currentY));
      layoutModel.set(node, currentX, currentY);

      int sizeXofCurrent = basePositions.get(node);

      int lastX = x - sizeXofCurrent / 2;

      int sizeXofChild;
      int startXofChild;

      for (E edgeElement :
          graph
              .outgoingEdgesOf(node)
              .stream()
              .sorted(edgeComparator)
              .collect(Collectors.toList())) {
        N element = graph.getEdgeTarget(edgeElement);
        log.trace("get base position of {} from {}", element, basePositions);
        sizeXofChild = this.basePositions.get(element);
        startXofChild = lastX + sizeXofChild / 2;
        buildTree(layoutModel, element, startXofChild);

        lastX = lastX + sizeXofChild + horizontalNodeSpacing;
      }

      this.currentY -= this.verticalNodeSpacing;
    }
  }

  @Override
  protected int calculateWidth(LayoutModel<N> layoutModel, N node, Set<N> seen) {
    Graph<N, E> graph = (Graph<N, E>) layoutModel.getGraph();
    log.trace("graph is {}", graph);
    List<N> successors = Graphs.successorListOf(graph, node);
    log.trace("successors of {} are {}", node, successors);
    successors.removeIf(seen::contains);
    log.trace("filtered successors of {} are {}", node, successors);
    seen.addAll(successors);

    int size =
        graph
            .outgoingEdgesOf(node)
            .stream()
            .sorted(edgeComparator)
            //            .filter(e -> edgePredicate.test(e)) // retain if the edgePredicate tests true
            .map(graph::getEdgeTarget) // get the edge target nodes
            .filter(
                successors::contains) // retain if the successors (filtered above) contain the node
            .mapToInt(element -> calculateWidth(layoutModel, element, seen) + horizontalNodeSpacing)
            .sum();
    size = Math.max(0, size - horizontalNodeSpacing);
    log.trace("calcWidth basePositions put {} {}", node, size);
    basePositions.put(node, size);

    return size;
  }

  @Override
  protected int calculateHeight(LayoutModel<N> layoutModel, N node, Set<N> seen) {
    Graph<N, E> graph = (Graph<N, E>) layoutModel.getGraph();
    List<N> successors = Graphs.successorListOf(graph, node);
    log.trace("graph is {}", graph);
    log.trace("h successors of {} are {}", node, successors);
    successors.removeIf(seen::contains);
    log.trace("filtered h successors of {} are {}", node, successors);
    seen.addAll(successors);

    return graph
        .outgoingEdgesOf(node)
        .stream()
        .sorted(edgeComparator)
        .map(graph::getEdgeTarget) // get the edge target nodes
        .filter(successors::contains) // retain if the successors (filtered above) contain the node
        .mapToInt(element -> calculateHeight(layoutModel, element, seen) + verticalNodeSpacing)
        .max()
        .orElse(0);
  }
}
