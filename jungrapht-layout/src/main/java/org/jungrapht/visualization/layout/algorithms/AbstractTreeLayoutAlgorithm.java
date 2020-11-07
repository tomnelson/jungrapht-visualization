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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.NeighborCache;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for tree layout algorithms.
 *
 * @param <V> vertex type
 */
public abstract class AbstractTreeLayoutAlgorithm<V> extends AbstractLayoutAlgorithm<V>
    implements LayoutAlgorithm<V>, TreeLayout<V> {

  private static final Logger log = LoggerFactory.getLogger(AbstractTreeLayoutAlgorithm.class);

  /**
   * A {@code Builder} to create instances of {@link AbstractTreeLayoutAlgorithm}
   *
   * @param <V> the vertex type
   * @param <T> the type of TreeLayoutAlgorithm that is built
   * @param <B> the builder type
   */
  public abstract static class Builder<
          V, T extends AbstractTreeLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      extends AbstractLayoutAlgorithm.Builder<V, T, B> implements LayoutAlgorithm.Builder<V, T, B> {
    protected Predicate<V> rootPredicate;
    protected Comparator<V> rootComparator = (v1, v2) -> 0;
    protected int horizontalVertexSpacing = TREE_LAYOUT_HORIZONTAL_SPACING;
    protected int verticalVertexSpacing = TREE_LAYOUT_VERTICAL_SPACING;
    protected boolean expandLayout = true;
    protected Function<V, Rectangle> vertexBoundsFunction = v -> Rectangle.of(-5, -5, 10, 10);

    /** @return this builder cast to type B */
    protected B self() {
      return (B) this;
    }

    /**
     * @param rootPredicate the predicate function to determine root vertices. Default is to
     *     consider all vertices with no incoming edges as root vertices
     * @return this Builder
     */
    public B rootPredicate(Predicate<V> rootPredicate) {
      this.rootPredicate = rootPredicate;
      return self();
    }

    /**
     * @param rootComparator used to sort the roots if an order is desired
     * @return this Builder
     */
    public B rootComparator(Comparator<V> rootComparator) {
      this.rootComparator = rootComparator;
      return self();
    }

    /**
     * @param horizontalVertexSpacing horizontal spacing between tree elements
     * @return this Builder
     */
    public B horizontalVertexSpacing(int horizontalVertexSpacing) {
      if (horizontalVertexSpacing <= 0)
        throw new IllegalArgumentException("horizontalVertexSpacing must be positive");
      this.horizontalVertexSpacing = horizontalVertexSpacing;
      return self();
    }

    /**
     * @param verticalVertexSpacing vertical spacing between tree elements
     * @return this Builder
     */
    public B verticalVertexSpacing(int verticalVertexSpacing) {
      if (verticalVertexSpacing <= 0)
        throw new IllegalArgumentException("verticalVertexSpacing must be positive");
      this.verticalVertexSpacing = verticalVertexSpacing;
      return self();
    }

    /**
     * if provided, the horizontal and vertical spacings will be replaced by the average width and
     * height of the vertex {@code Shape}s returned by this {@link Function}
     *
     * @param vertexBoundsFunction source of vertex shapes
     * @return this builder
     */
    public B vertexBoundsFunction(Function<V, Rectangle> vertexBoundsFunction) {
      this.vertexBoundsFunction = vertexBoundsFunction;
      return self();
    }

    /**
     * @param expandLayout if {@code true} expand the layout width and height to accomodate the
     *     entire tree
     * @return the Builder
     */
    public B expandLayout(boolean expandLayout) {
      this.expandLayout = expandLayout;
      return self();
    }
  }

  /**
   * Create a {@link AbstractTreeLayoutAlgorithm} instance with the passed {@code Builder}
   *
   * @param builder the {@code Builder} with configuration properties
   */
  protected AbstractTreeLayoutAlgorithm(Builder<V, ?, ?> builder) {
    super(builder);
    Objects.requireNonNull(builder.vertexBoundsFunction);
    this.rootPredicate = builder.rootPredicate;
    this.rootComparator = builder.rootComparator;
    this.horizontalVertexSpacing = builder.horizontalVertexSpacing;
    this.verticalVertexSpacing = builder.verticalVertexSpacing;
    this.vertexBoundsFunction = builder.vertexBoundsFunction;
    this.expandLayout = builder.expandLayout;
  }

  /** the {}@link Predicate} to determine root vertices */
  protected Predicate<V> rootPredicate;

  protected Predicate<V> defaultRootPredicate;

  protected Comparator<V> rootComparator;

  public void setRootPredicate(Predicate<V> rootPredicate) {
    this.rootPredicate = rootPredicate;
  }

  public void setRootComparator(Comparator<V> rootComparator) {
    this.rootComparator = rootComparator;
  }

  protected Set<V> visitedVertices = new HashSet<>();

  protected NeighborCache<V, ?> neighborCache;

  @Override
  public void setVertexBoundsFunction(Function<V, Rectangle> vertexBoundsFunction) {
    Objects.requireNonNull(vertexBoundsFunction);
    this.vertexBoundsFunction = vertexBoundsFunction;
  }

  /**
   * a {}@link Map} of vertex to a {@link Rectangle} that will contain the vertex and all of its
   * children
   */
  protected Map<V, Rectangle> baseBounds = new HashMap<>();

  /** The horizontal vertex spacing. Defaults to {@code DEFAULT_HORIZONTAL_VERTEX_SPACING}. */
  protected int horizontalVertexSpacing;

  /** The vertical vertex spacing. Defaults to {@code DEFAULT_VERTICAL_VERTEX_SPACING}. */
  protected int verticalVertexSpacing;

  /**
   * if provided (non-null) then the horizontalVertexSpacing and verticalVertexSpacing values will
   * be replaced by 2 times the average width and height of all vertex shapes
   */
  protected Function<V, Rectangle> vertexBoundsFunction;

  /** if {@code true} then expand the layout size to accomodate the entire tree. */
  protected boolean expandLayout;

  /**
   * visit a {@link LayoutModel} to set all of the graph vertex positions according to the
   * LayoutAlgorithm logic.
   *
   * @param layoutModel the mediator between the container for vertices (the Graph) and the mapping
   */
  @Override
  public void visit(LayoutModel<V> layoutModel) {
    this.neighborCache = new NeighborCache<>(layoutModel.getGraph());
  }

  /**
   * @return the {@link Map} of vertex to {@link Rectangle} that will hold all of the subtree rooted
   *     at the vertex
   */
  @Override
  public Map<V, Rectangle> getBaseBounds() {
    return baseBounds;
  }

  protected void adjustToFill(int largerWidth, int largerHeight) {
    if (largerWidth > largerHeight) {
      double expansion = (double) largerWidth / largerHeight;
      verticalVertexSpacing *= expansion;
    } else if (largerWidth < largerHeight) {
      double expansion = (double) largerHeight / largerWidth;
      horizontalVertexSpacing *= expansion;
    }
  }

  /** @param layoutModel */
  protected void expandToFill(LayoutModel<V> layoutModel) {

    // find the dimensions of the layout's occupied area
    Rectangle vertexContainingRectangle = computeLayoutExtent(layoutModel);

    // add the padding
    vertexContainingRectangle =
        Rectangle.from(
            vertexContainingRectangle.min().add(-horizontalVertexSpacing, -verticalVertexSpacing),
            vertexContainingRectangle.max().add(horizontalVertexSpacing, verticalVertexSpacing));

    int maxDimension =
        Math.max((int) vertexContainingRectangle.width, (int) vertexContainingRectangle.height);
    layoutModel.setSize(maxDimension, maxDimension);

    super.expandToFill(layoutModel, vertexContainingRectangle);
  }

  protected <E> int moveVerticesThatOverlapVerticalEdges(LayoutModel<V> layoutModel, int offset) {
    int moved = 0;
    Graph<V, E> graph = layoutModel.getGraph();
    Map<Double, Set<E>> verticalEdgeMap = new LinkedHashMap<>();
    graph
        .edgeSet()
        .stream()
        .filter(
            e ->
                layoutModel.apply(graph.getEdgeSource(e)).x
                    == layoutModel.apply(graph.getEdgeTarget(e)).x)
        .forEach(
            e ->
                verticalEdgeMap
                    .computeIfAbsent(
                        layoutModel.apply(graph.getEdgeSource(e)).x, k -> new HashSet<>())
                    .add(e));

    for (V v : graph.vertexSet()) {
      double x = layoutModel.apply(v).x;
      for (E edge : verticalEdgeMap.getOrDefault(x, Collections.emptySet())) {
        V source = graph.getEdgeSource(edge);
        V target = graph.getEdgeTarget(edge);
        if (!v.equals(source) && !v.equals(target)) {
          double lowy = layoutModel.apply(source).y;
          double hiy = layoutModel.apply(target).y;
          if (lowy > hiy) {
            double temp = lowy;
            lowy = hiy;
            hiy = temp;
          }
          double vy = layoutModel.apply(v).y;
          if (lowy <= vy && vy <= hiy) {
            layoutModel.set(v, layoutModel.apply(v).add(offset, 0));
            log.trace("moved {}", v);
            moved++;
          }
        }
      }
    }
    return moved;
  }

  @Override
  public boolean constrained() {
    return false;
  }
}
