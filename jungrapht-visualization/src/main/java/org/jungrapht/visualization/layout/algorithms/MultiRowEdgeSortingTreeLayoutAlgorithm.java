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
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of {@code TreeLayoutAlgorithm} that allows a {@code Comparator<E>} for edges so that
 * the children of each vertex are accessed in a supplied order.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 * @author Tom Nelson
 */
public class MultiRowEdgeSortingTreeLayoutAlgorithm<V, E> extends MultiRowTreeLayoutAlgorithm<V>
    implements LayoutAlgorithm<V>, EdgeSorting<E> {

  private static final Logger log =
      LoggerFactory.getLogger(MultiRowEdgeSortingTreeLayoutAlgorithm.class);

  /**
   * a builder to create an instance of a {@code} EdgeSortingTreeLayoutAlgorithm.
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that will be created
   * @param <B> the builder type
   */
  public static class Builder<
          V,
          E,
          T extends MultiRowEdgeSortingTreeLayoutAlgorithm<V, E>,
          B extends Builder<V, E, T, B>>
      extends MultiRowTreeLayoutAlgorithm.Builder<V, T, B> {

    /**
     * a comparator to sort edges
     *
     * @param <E> the edge type
     */
    private Comparator<E> edgeComparator = (e1, e2) -> 0;

    /**
     * use the supplied comparator to sort edges
     *
     * @param edgeComparator supplied edge Comparator
     * @return the builder
     */
    public B edgeComparator(Comparator<E> edgeComparator) {
      this.edgeComparator = edgeComparator;
      return self();
    }

    /** @return the instance */
    public T build() {
      return (T) new MultiRowEdgeSortingTreeLayoutAlgorithm<>(this);
    }
  }

  /**
   * create a new {@code EdgeSortingTreeLayoutAlgorithm.Builder}
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @return a new {@code EdgeSortingTreeLayoutAlgorithm.Builder}
   */
  public static <V, E> Builder<V, E, ?, ?> sortingBuilder() {
    return new Builder<>();
  }

  protected MultiRowEdgeSortingTreeLayoutAlgorithm(Builder<V, E, ?, ?> builder) {
    this(
        builder.roots,
        builder.horizontalVertexSpacing,
        builder.verticalVertexSpacing,
        builder.edgeComparator,
        builder.expandLayout);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param edgeComparator sorts the edges
   */
  protected MultiRowEdgeSortingTreeLayoutAlgorithm(
      Set<V> roots,
      int horizontalVertexSpacing,
      int verticalVertexSpacing,
      Comparator<E> edgeComparator,
      boolean expandLayout) {
    super(roots, horizontalVertexSpacing, verticalVertexSpacing, expandLayout);
    this.edgeComparator = edgeComparator;
  }

  protected Comparator<E> edgeComparator;

  public void setEdgeComparator(Comparator<E> edgeComparator) {
    this.edgeComparator = edgeComparator;
  }

  private void adjustProportions(LayoutModel<V> layoutModel) {
    int overallWidth = layoutModel.getWidth();
    int overallHeight = layoutModel.getHeight();
    // if the tree is way wider than it is high, adjust the verticalVertexSpacing and the layoutModel height
    if (overallWidth > 2 * overallHeight) {
      double ratio = overallWidth / (2 * overallHeight);
      verticalVertexSpacing *= ratio;
      layoutModel.setSize(overallWidth, (int) (overallHeight * ratio));
    }
  }

  @Override
  protected void buildTree(LayoutModel<V> layoutModel, V vertex, int x, int y) {
    Graph<V, E> graph = layoutModel.getGraph();
    if (alreadyDone.add(vertex)) {
      //go one level further down
      y += this.verticalVertexSpacing;
      log.trace("Set vertex {} to {}", vertex, Point.of(x, y));
      layoutModel.set(vertex, x, y);

      int sizeXofCurrent = baseWidths.get(vertex);
      x -= sizeXofCurrent / 2;

      int sizeXofChild;

      for (E edgeElement :
          graph
              .outgoingEdgesOf(vertex)
              .stream()
              .sorted(edgeComparator)
              .collect(Collectors.toList())) {
        V element = graph.getEdgeTarget(edgeElement);
        log.trace("get base position of {} from {}", element, baseWidths);
        sizeXofChild = this.baseWidths.get(element);
        x += sizeXofChild / 2;
        buildTree(layoutModel, element, x, y);

        x += sizeXofChild / 2 + horizontalVertexSpacing;
      }
    }
  }

  protected int calculateWidth(LayoutModel<V> layoutModel, V vertex, Set<V> seen) {
    Graph<V, E> graph = layoutModel.getGraph();
    log.trace("graph is {}", graph);

    List<V> sortedSuccessorList =
        graph
            .outgoingEdgesOf(vertex)
            .stream()
            .sorted(edgeComparator)
            .map(element -> graph.getEdgeTarget(element))
            .collect(Collectors.toList());
    sortedSuccessorList.removeIf(seen::contains);
    seen.addAll(sortedSuccessorList);

    int size =
        sortedSuccessorList
            .stream()
            .mapToInt(
                element -> calculateWidth(layoutModel, element, seen) + horizontalVertexSpacing)
            .sum();
    size = Math.max(0, size - horizontalVertexSpacing);
    log.trace("calcWidth baseWidths put {} {}", vertex, size);
    baseWidths.put(vertex, size);

    return size;
  }
}
