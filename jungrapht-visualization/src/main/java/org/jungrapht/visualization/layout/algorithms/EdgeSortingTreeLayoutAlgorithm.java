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
public class EdgeSortingTreeLayoutAlgorithm<V, E> extends TreeLayoutAlgorithm<V>
    implements LayoutAlgorithm<V> {

  private static final Logger log = LoggerFactory.getLogger(EdgeSortingTreeLayoutAlgorithm.class);

  /**
   * a builder to create an instance of a {@code} EdgeSortingTreeLayoutAlgorithm.
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that will be created
   * @param <B> the builder type
   */
  public static class Builder<
          V, E, T extends EdgeSortingTreeLayoutAlgorithm<V, E>, B extends Builder<V, E, T, B>>
      extends TreeLayoutAlgorithm.Builder<V, T, B> {

    /**
     * a comparator to sort edges
     *
     * @param <E> the edge type
     */
    private Comparator<E> edgeComparator = (e1, e2) -> 0;

    private boolean adjustProportions;

    /**
     * self reference with typecase
     *
     * @return this builder cast to B
     */
    protected B self() {
      return (B) this;
    }

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

    public B adjustProportions(boolean adjustProportions) {
      this.adjustProportions = adjustProportions;
      return self();
    }

    /** @return the instance */
    public T build() {
      return (T) new EdgeSortingTreeLayoutAlgorithm<>(this);
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

  protected EdgeSortingTreeLayoutAlgorithm(Builder<V, E, ?, ?> builder) {
    this(
        builder.horizontalVertexSpacing,
        builder.verticalVertexSpacing,
        builder.edgeComparator,
        builder.expandLayout,
        builder.adjustProportions);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param edgeComparator sorts the edges
   */
  protected EdgeSortingTreeLayoutAlgorithm(
      int horizontalVertexSpacing,
      int verticalVertexSpacing,
      Comparator<E> edgeComparator,
      boolean expandLayout,
      boolean adjustProportions) {
    super(horizontalVertexSpacing, verticalVertexSpacing, expandLayout);
    this.edgeComparator = edgeComparator;
    this.adjustProportions = adjustProportions;
  }

  protected Comparator<E> edgeComparator;
  protected boolean adjustProportions;

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
    if (adjustProportions) {
      adjustProportions(layoutModel);
    }
    Graph<V, E> graph = layoutModel.getGraph();
    if (alreadyDone.add(vertex)) {
      //go one level further down
      y += this.verticalVertexSpacing;
      log.trace("Set vertex {} to {}", vertex, Point.of(x, y));
      layoutModel.set(vertex, x, y);

      int sizeXofCurrent = basePositions.get(vertex);
      x -= sizeXofCurrent / 2;

      int sizeXofChild;

      for (E edgeElement :
          graph
              .outgoingEdgesOf(vertex)
              .stream()
              .sorted(edgeComparator)
              .collect(Collectors.toList())) {
        V element = graph.getEdgeTarget(edgeElement);
        log.trace("get base position of {} from {}", element, basePositions);
        sizeXofChild = this.basePositions.get(element);
        x += sizeXofChild / 2;
        buildTree(layoutModel, element, x, y);

        x += sizeXofChild / 2 + horizontalVertexSpacing;
      }
    }
  }
}
