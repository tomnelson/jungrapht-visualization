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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class TreeLayoutAlgorithm<V> implements LayoutAlgorithm<V>, TreeLayout<V> {

  private static final Logger log = LoggerFactory.getLogger(TreeLayoutAlgorithm.class);

  /**
   * A {@code Builder} to create instances of {@link TreeLayoutAlgorithm}
   *
   * @param <V> the vertex type
   * @param <T> the type of TreeLayoutAlgorithm that is built
   * @param <B> the builder type
   */
  public static class Builder<V, T extends TreeLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected Predicate<V> rootPredicate;
    protected int horizontalVertexSpacing = TREE_LAYOUT_HORIZONTAL_SPACING;
    protected int verticalVertexSpacing = TREE_LAYOUT_VERTICAL_SPACING;
    protected boolean expandLayout = true;

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
     * @param horizontalVertexSpacing horizontal spacing between tree elements
     * @return this Builder
     */
    public B horizontalVertexSpacing(int horizontalVertexSpacing) {
      Preconditions.checkArgument(
          horizontalVertexSpacing > 0, "horizontalVertexSpacing must be positive");
      this.horizontalVertexSpacing = horizontalVertexSpacing;
      return self();
    }

    /**
     * @param verticalVertexSpacing vertical spacing between tree elements
     * @return this Builder
     */
    public B verticalVertexSpacing(int verticalVertexSpacing) {
      Preconditions.checkArgument(
          verticalVertexSpacing > 0, "verticalVertexSpacing must be positive");
      this.verticalVertexSpacing = verticalVertexSpacing;
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

    /** @return the Builder with its set parameters */
    public T build() {
      return (T) new TreeLayoutAlgorithm<>(this);
    }
  }

  /**
   * @param <V> the vertex type
   * @return a {@code Builder} ready to configure
   */
  public static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  /**
   * Create a {@link TreeLayoutAlgorithm} instance with the passed {@code Builder}
   *
   * @param builder the {@code Builder} with configuration properties
   */
  protected TreeLayoutAlgorithm(Builder<V, ?, ?> builder) {
    this(
        builder.rootPredicate,
        builder.horizontalVertexSpacing,
        builder.verticalVertexSpacing,
        builder.expandLayout);
  }

  /**
   * Creates an instance for the specified parameters
   *
   * @param rootPredicate the {@link Predicate} function to determine root vertices
   * @param horizontalVertexSpacing the horizontal spacing between adjacent siblings
   * @param verticalVertexSpacing the vertical spacing between adjacent siblings
   * @param expandLayout whether to expand the layout size to accomodate the entire tree
   */
  protected TreeLayoutAlgorithm(
      Predicate<V> rootPredicate,
      int horizontalVertexSpacing,
      int verticalVertexSpacing,
      boolean expandLayout) {
    this.rootPredicate = rootPredicate;
    this.horizontalVertexSpacing = horizontalVertexSpacing;
    this.verticalVertexSpacing = verticalVertexSpacing;
    this.expandLayout = expandLayout;
  }

  /** the {}@link Predicate} to determine root vertices */
  protected Predicate<V> rootPredicate;

  public void setRootPredicate(Predicate<V> rootPredicate) {
    this.rootPredicate = rootPredicate;
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
    buildTree(layoutModel);
  }

  /**
   * @return the {@link Map} of vertex to {@link Rectangle} that will hold all of the subtree rooted
   *     at the vertex
   */
  @Override
  public Map<V, Rectangle> getBaseBounds() {
    return baseBounds;
  }

  /**
   * @param layoutModel the model to hold vertex positions
   * @return the roots vertices of the tree
   */
  protected Set<V> buildTree(LayoutModel<V> layoutModel) {
    Graph<V, ?> graph = layoutModel.getGraph();
    if (this.rootPredicate == null) {
      rootPredicate = v -> graph.incomingEdgesOf(v).isEmpty();
    }
    Set<V> roots =
        graph
            .vertexSet()
            .stream()
            .filter(rootPredicate)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    Preconditions.checkArgument(roots.size() > 0);
    // the width of the tree under 'roots'. Includes one 'horizontalVertexSpacing' per child vertex
    int overallWidth = calculateWidth(layoutModel, roots, new HashSet<>());
    // add one additional 'horizontalVertexSpacing' for each tree (each root) + 1
    overallWidth += (roots.size() + 1) * horizontalVertexSpacing;
    int overallHeight = calculateHeight(layoutModel, roots, new HashSet<>());
    overallHeight += 2 * verticalVertexSpacing;

    log.trace("layoutModel.getWidth() {} overallWidth {}", layoutModel.getWidth(), overallWidth);
    log.trace(
        "layoutModel.getHeight() {} overallHeight {}", layoutModel.getHeight(), overallHeight);
    int largerWidth = Math.max(layoutModel.getWidth(), overallWidth);
    int largerHeight = Math.max(layoutModel.getHeight(), overallHeight);
    if (expandLayout) {
      layoutModel.setSize(largerWidth, largerHeight);
    }
    int x = getInitialPosition(horizontalVertexSpacing, layoutModel.getWidth(), overallWidth);

    int y = getInitialPosition(verticalVertexSpacing, layoutModel.getHeight(), overallHeight);
    log.trace("got initial y of {}", y);

    Set<V> seen = new HashSet<>();
    for (V vertex : roots) {
      int w = (int) this.baseBounds.get(vertex).width;
      log.trace("w is {} and baseWidths.get(vertex) = {}", w, baseBounds.get(vertex).width);
      x += w / 2;
      log.trace("currentX after vertex {} is now {}", vertex, x);
      buildTree(layoutModel, vertex, x, y, seen);
      merge(layoutModel, vertex);
      x += w / 2 + horizontalVertexSpacing;
    }
    return roots;
  }

  /**
   * @param initialPosition default initial position for when the layoutSpan is greater than the
   *     tree span
   * @param layoutSpan the dimension (width or height) of the layout
   * @param treeSpan the dimension (width or height) of the current subtree
   * @return the average of the layoutSpan and treeSpan or the passed initial position
   */
  protected int getInitialPosition(int initialPosition, int layoutSpan, int treeSpan) {
    if (layoutSpan <= treeSpan) {
      return initialPosition;
    }
    return layoutSpan / 2 - treeSpan / 2;
  }

  /**
   * Place the passed vertex and descend into the child vertices, recursively placing each child
   * vertex
   *
   * @param layoutModel the {@link LayoutModel} to hold the vertex positions
   * @param vertex the vertex to place in position
   * @param x the x position
   * @param y the y position
   * @param seen a set of vertices that were already 'seen' (and placed in the layoutModel)
   */
  protected void buildTree(LayoutModel<V> layoutModel, V vertex, int x, int y, Set<V> seen) {
    if (seen.add(vertex)) {
      log.trace("buildTree placing {}", vertex);
      // go one level further down
      y += this.verticalVertexSpacing;
      log.trace("Set vertex {} to {}", vertex, Point.of(x, y));
      layoutModel.set(vertex, x, y);
      merge(layoutModel, vertex);

      double sizeXofCurrent = baseBounds.get(vertex).width;
      x -= sizeXofCurrent / 2;

      for (V element : Graphs.successorListOf(layoutModel.getGraph(), vertex)) {
        if (!rootPredicate.test(element) && !seen.contains(element)) {
          log.trace("get base position of {} from {}", element, baseBounds);
          double sizeXofChild = this.baseBounds.get(element).width;
          x += sizeXofChild / 2;
          buildTree(layoutModel, element, x, y, seen);
          merge(layoutModel, element);
          x += sizeXofChild / 2 + horizontalVertexSpacing;
        }
      }
    }
  }

  /**
   * update the baseBounds Map value for the passed vertex, with the (x,y) position of this vertex
   *
   * @param layoutModel the source of the vertex coordinates
   * @param vertex the vertex key to update in the baseBounds map
   */
  protected void merge(LayoutModel<V> layoutModel, V vertex) {
    Point p = layoutModel.apply(vertex);
    baseBounds.merge(
        vertex,
        Rectangle.of(p.x, p.y, 0, 0),
        (r, s) -> Rectangle.of(s.x - r.width / 2, s.y, r.width, r.height));
  }

  /**
   * calculate the width of the subtree descended from the passed vertex
   *
   * @param layoutModel the source of the graph vertices
   * @param vertex the vertex at the root of the current subtree
   * @param seen a set of vertices that were already measured
   * @return the width of the subtree rooted at the passed vertex
   */
  protected int calculateWidth(LayoutModel<V> layoutModel, V vertex, Set<V> seen) {
    if (seen.add(vertex)) {
      Graph<V, ?> graph = layoutModel.getGraph();

      int width =
          Math.max(
              0,
              Graphs.successorListOf(graph, vertex)
                      .stream()
                      .filter(v -> !rootPredicate.test(v) && !seen.contains(v))
                      .mapToInt(
                          element ->
                              calculateWidth(layoutModel, element, seen) + horizontalVertexSpacing)
                      .sum()
                  - horizontalVertexSpacing);
      log.trace("calcWidth baseWidths put {} {}", vertex, width);
      baseBounds.merge(
          vertex,
          Rectangle.of(0, 0, width, 0),
          (r, t) -> Rectangle.of(r.x, r.y, t.width, r.height));
      return width;
    }
    return 0;
  }

  /**
   * Calculate the width of the forest as the sum of the width of each tree (plus offsets)
   *
   * @param layoutModel the source of the graph and its vertices
   * @param roots the root vertices of the forest
   * @param seen a set of vertices that were already placed
   * @return the overall width
   */
  protected int calculateWidth(LayoutModel<V> layoutModel, Collection<V> roots, Set<V> seen) {

    int width =
        roots
            .stream()
            .filter(v -> !seen.contains(v))
            .mapToInt(vertex -> calculateWidth(layoutModel, vertex, seen))
            .sum();
    log.debug("entire width from {} is {}", roots, width);
    return width;
  }

  /**
   * Calculate the height of the subtree under the passed vertex
   *
   * @param layoutModel the source of the Graph and its vertices
   * @param vertex the vertex at the top of the current subtree
   * @param seen a set of vertices that were already counted
   * @return the height of the subtree rooted at the passed vertex
   */
  protected int calculateHeight(LayoutModel<V> layoutModel, V vertex, Set<V> seen) {
    if (seen.add(vertex)) {
      Graph<V, ?> graph = layoutModel.getGraph();

      int height =
          Graphs.successorListOf(graph, vertex)
              .stream()
              //              .filter(v -> !seen.contains(v))
              .filter(v -> !rootPredicate.test(v) && !seen.contains(v))
              .mapToInt(
                  element -> calculateHeight(layoutModel, element, seen) + verticalVertexSpacing)
              .max()
              .orElse(0);
      baseBounds.merge(
          vertex,
          Rectangle.of(0, 0, 0, height),
          (r, t) -> Rectangle.of(r.x, r.y, r.width, t.height));
      return height;
    }
    return 0;
  }

  /**
   * Calculate the height of the forest as the max height of all of its subtrees
   *
   * @param layoutModel the source of the graph and vertices
   * @param roots the root vertices of the forest
   * @param seen a set of vertices that were already measured
   * @return the height that will accomodate the entire forest
   */
  protected int calculateHeight(LayoutModel<V> layoutModel, Collection<V> roots, Set<V> seen) {

    return roots
            .stream()
            .filter(v -> !seen.contains(v))
            .mapToInt(vertex -> calculateHeight(layoutModel, vertex, seen))
            .max()
            .orElse(verticalVertexSpacing)
        + verticalVertexSpacing;
  }

  /** @return the center of this layout's area. */
  public Point getCenter(LayoutModel<V> layoutModel) {
    return Point.of(layoutModel.getWidth() / 2, layoutModel.getHeight() / 2);
  }
}
