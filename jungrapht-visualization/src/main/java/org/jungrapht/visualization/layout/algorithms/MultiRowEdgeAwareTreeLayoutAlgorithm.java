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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.model.Dimension;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class MultiRowEdgeAwareTreeLayoutAlgorithm<V, E> extends EdgeAwareTreeLayoutAlgorithm<V, E>
    implements EdgeAwareLayoutAlgorithm<V, E>,
        EdgeSorting<E>,
        EdgePredicated<E>,
        VertexSorting<V>,
        VertexPredicated<V> {

  private static final Logger log =
      LoggerFactory.getLogger(MultiRowEdgeAwareTreeLayoutAlgorithm.class);

  public static class Builder<
          V, E, T extends MultiRowEdgeAwareTreeLayoutAlgorithm<V, E>, B extends Builder<V, E, T, B>>
      extends EdgeAwareTreeLayoutAlgorithm.Builder<V, E, T, B>
      implements EdgeAwareLayoutAlgorithm.Builder<V, E, T, B> {

    public T build() {
      return (T) new MultiRowEdgeAwareTreeLayoutAlgorithm<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected MultiRowEdgeAwareTreeLayoutAlgorithm(Builder<V, E, ?, ?> builder) {
    super(builder);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param horizontalVertexSpacing the horizontal spacing between adjacent siblings
   * @param verticalVertexSpacing the vertical spacing between adjacent siblings
   */
  protected MultiRowEdgeAwareTreeLayoutAlgorithm(
      Predicate<V> rootPredicate,
      int horizontalVertexSpacing,
      int verticalVertexSpacing,
      Predicate<V> vertexPredicate,
      Predicate<E> edgePredicate,
      Comparator<V> vertexComparator,
      Comparator<E> edgeComparator,
      boolean expandLayout) {
    super(
        rootPredicate,
        horizontalVertexSpacing,
        verticalVertexSpacing,
        vertexPredicate,
        edgePredicate,
        vertexComparator,
        edgeComparator,
        expandLayout);
  }

  protected int rowCount = 1;

  /**
   * @param layoutModel the model to hold vertex positions
   * @return the roots vertices of the tree
   */
  @Override
  protected Set<V> buildTree(LayoutModel<V> layoutModel) {
    rowCount = 1;
    alreadyDone = Sets.newHashSet();
    Graph<V, E> graph = layoutModel.getGraph();
    if (this.rootPredicate == null) {
      rootPredicate = v -> layoutModel.getGraph().incomingEdgesOf(v).isEmpty();
    }
    Set<V> roots =
        graph
            .edgeSet()
            .stream()
            .sorted(edgeComparator)
            .map(graph::getEdgeSource)
            .sorted(vertexComparator)
            .filter(rootPredicate)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    Preconditions.checkArgument(roots.size() > 0);

    // measure the tree

    // the width of the tree under 'roots'. Includes one 'horizontalVertexSpacing' per child vertex
    int overallWidth = calculateWidth(layoutModel, roots, new HashSet<>());
    log.debug("after calculating overallWidth {}, row count is {}", overallWidth, rowCount);
    int tallestTreeHeight = calculateOverallHeight(layoutModel, roots);
    int overallHeight = tallestTreeHeight; // * rowCount;
    overallHeight += verticalVertexSpacing;

    log.trace("layoutModel.getWidth() {}", layoutModel.getWidth());
    log.trace("overallWidth {}", overallWidth);
    int largerHeight = Math.max(layoutModel.getHeight(), overallHeight);
    if (expandLayout) {
      layoutModel.setSize(layoutModel.getWidth(), largerHeight);
    }
    log.trace("layoutModel.getHeight() {}", layoutModel.getHeight());
    log.trace("overallHeight {}", overallHeight);

    //    int cursor = horizontalVertexSpacing;
    //    if (overallWidth < layoutModel.getWidth()) {
    // start later
    int cursor = getInitialPosition(horizontalVertexSpacing, layoutModel.getWidth(), overallWidth);
    //    }
    int y = getInitialPosition(0, layoutModel.getHeight(), overallHeight);
    log.trace("got initial y of {}", y);

    Set<V> rootsInRow = new HashSet<>();
    for (V vertex : roots) {

      int w = this.baseBounds.get(vertex).width;
      log.trace("w is {} and baseWidths.get({}) = {}", w, vertex, baseBounds.get(vertex));
      cursor += w;
      cursor += horizontalVertexSpacing;

      if (cursor > layoutModel.getWidth()) {
        cursor = getInitialPosition(horizontalVertexSpacing, layoutModel.getWidth(), overallWidth);
        cursor += w;
        cursor += horizontalVertexSpacing;
        int rowHeight = calculateHeight(layoutModel, rootsInRow);
        log.trace("height for {} is {}", rootsInRow, rowHeight);
        y += rowHeight;
        rootsInRow.clear();
      }
      rootsInRow.add(vertex);

      boolean onFilteredPath = false;
      onFilteredPath |= vertexPredicate.test(vertex);
      for (E edge : graph.outgoingEdgesOf(vertex)) {
        onFilteredPath |= edgePredicate.test(edge);
      }

      int x = cursor - horizontalVertexSpacing - w / 2;

      if (onFilteredPath) {
        x -= w / 2;
        buildTree(layoutModel, vertex, x, y);
      } else {
        buildTree(layoutModel, vertex, x, y);
      }
    }
    // last row
    int rowHeight = calculateHeight(layoutModel, rootsInRow);
    log.trace("height for (last) {} is {}", rootsInRow, rowHeight);
    log.debug("rowCount is {}", rowCount);
    return roots;
  }

  @Override
  protected void buildTree(LayoutModel<V> layoutModel, V sourceVertex, int x, int y) {

    Graph<V, E> graph = layoutModel.getGraph();
    if (alreadyDone.add(sourceVertex)) {
      //go one level further down
      y += this.verticalVertexSpacing;
      log.trace("Set vertex {} to {}", sourceVertex, Point.of(x, y));
      layoutModel.set(sourceVertex, x, y);

      double sizeXofCurrent = baseBounds.getOrDefault(sourceVertex, Dimension.of(0, 0)).width;
      x -= sizeXofCurrent / 2;

      double sizeXofChild;

      for (E outgoingEdge :
          graph
              .outgoingEdgesOf(sourceVertex)
              .stream()
              .sorted(edgeComparator)
              .collect(Collectors.toList())) {
        V targetVertex = graph.getEdgeTarget(outgoingEdge);

        boolean onFilteredPath =
            edgePredicate.test(outgoingEdge) || vertexPredicate.test(targetVertex);
        if (onFilteredPath) {
          x += sizeXofCurrent / 2;
          sizeXofChild = this.baseBounds.getOrDefault(targetVertex, Dimension.of(0, 0)).width;
          log.trace("get base position of {} from {}", targetVertex, baseBounds);
          buildTree(layoutModel, targetVertex, x, y);
          x += sizeXofChild + horizontalVertexSpacing;
        } else {
          sizeXofChild = this.baseBounds.getOrDefault(targetVertex, Dimension.of(0, 0)).width;
          x += sizeXofChild / 2;
          log.trace("get base position of {} from {}", targetVertex, baseBounds);
          buildTree(layoutModel, targetVertex, x, y);
          x += sizeXofChild / 2 + horizontalVertexSpacing;
        }
      }
    }
  }

  @Override
  protected int calculateWidth(LayoutModel<V> layoutModel, Collection<V> roots, Set<V> seen) {
    int overallWidth = 0;
    int cursor = horizontalVertexSpacing;
    for (V root : roots) {
      int w = calculateWidth(layoutModel, root, seen);
      cursor += w;
      cursor += horizontalVertexSpacing;
      log.trace("width of {} is {}", root, w);
      if (cursor > layoutModel.getWidth()) {
        cursor = horizontalVertexSpacing;
        cursor += w;
        cursor += horizontalVertexSpacing;
        rowCount++;
        log.trace("row count now {}", rowCount);
      }
      overallWidth = Math.max(cursor, overallWidth);
    }
    log.trace("entire width from {} is {}", roots, overallWidth);
    return overallWidth;
  }

  protected int calculateWidth(LayoutModel<V> layoutModel, V sourceVertex, Set<V> seen) {
    Graph<V, E> graph = layoutModel.getGraph();
    log.trace("graph is {}", graph);
    List<V> successors = Graphs.successorListOf(graph, sourceVertex);
    log.trace("successors of {} are {}", sourceVertex, successors);
    successors.removeIf(seen::contains);
    log.trace("filtered successors of {} are {}", sourceVertex, successors);
    seen.addAll(successors);

    int size =
        Math.max(
            0,
            graph
                    .outgoingEdgesOf(sourceVertex)
                    .stream()
                    .sorted(edgeComparator)
                    .map(graph::getEdgeTarget)
                    .filter(successors::contains)
                    .mapToInt(
                        element ->
                            calculateWidth(layoutModel, element, seen) + horizontalVertexSpacing)
                    .sum()
                - horizontalVertexSpacing);

    log.trace("calcWidth baseWidths put {} {}", sourceVertex, size);
    baseBounds.merge(sourceVertex, Dimension.of(size, 0), (r, s) -> Dimension.of(size, r.height));
    return size;
  }

  protected int calculateHeight(LayoutModel<V> layoutModel, V vertex, Set<V> seen) {
    Graph<V, E> graph = layoutModel.getGraph();
    List<V> successors = Graphs.successorListOf(graph, vertex);
    log.trace("graph is {}", graph);
    log.trace("h successors of {} are {}", vertex, successors);
    successors.removeIf(seen::contains);
    log.trace("filtered h successors of {} are {}", vertex, successors);
    seen.addAll(successors);

    final int height =
        graph
            .outgoingEdgesOf(vertex)
            .stream()
            .sorted(edgeComparator)
            .map(graph::getEdgeTarget)
            .filter(successors::contains)
            .sorted(vertexComparator)
            .mapToInt(
                element -> calculateHeight(layoutModel, element, seen) + verticalVertexSpacing)
            .max()
            .orElse(0);
    baseBounds.merge(vertex, Dimension.of(0, height), (r, t) -> Dimension.of(r.width, height));
    return height;
  }

  protected int calculateOverallHeight(LayoutModel<V> layoutModel, Collection<V> roots) {

    int overallHeight = 0;
    int cursor = horizontalVertexSpacing;
    Set<V> rootsInRow = new HashSet<>();
    for (V root : roots) {
      int w = calculateWidth(layoutModel, root, new HashSet<>());
      cursor += w;
      cursor += horizontalVertexSpacing;
      log.trace("width of {} is {}", root, w);
      if (cursor > layoutModel.getWidth()) {
        cursor = horizontalVertexSpacing;
        cursor += w;
        cursor += horizontalVertexSpacing;
        overallHeight += super.calculateHeight(layoutModel, rootsInRow);
        rootsInRow.clear();
      }
      rootsInRow.add(root);
    }
    // last row
    overallHeight += super.calculateHeight(layoutModel, rootsInRow);
    return overallHeight;
  }
}
