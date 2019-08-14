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

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.model.Dimension;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class TreeLayoutAlgorithm<V> implements LayoutAlgorithm<V> {

  private static final Logger log = LoggerFactory.getLogger(TreeLayoutAlgorithm.class);

  public static class Builder<V, T extends TreeLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected Predicate<V> rootPredicate;
    protected int horizontalVertexSpacing = DEFAULT_HORIZONTAL_VERTEX_SPACING;
    protected int verticalVertexSpacing = DEFAULT_VERTICAL_VERTEX_SPACING;
    protected boolean expandLayout = true;

    protected B self() {
      return (B) this;
    }

    public B rootPredicate(Predicate<V> rootPredicate) {
      this.rootPredicate = rootPredicate;
      return self();
    }

    public B horizontalVertexSpacing(int horizontalVertexSpacing) {
      Preconditions.checkArgument(
          horizontalVertexSpacing > 0, "horizontalVertexSpacing must be positive");
      this.horizontalVertexSpacing = horizontalVertexSpacing;
      return self();
    }

    public B verticalVertexSpacing(int verticalVertexSpacing) {
      Preconditions.checkArgument(
          verticalVertexSpacing > 0, "verticalVertexSpacing must be positive");
      this.verticalVertexSpacing = verticalVertexSpacing;
      return self();
    }

    public B expandLayout(boolean expandLayout) {
      this.expandLayout = expandLayout;
      return self();
    }

    public T build() {
      return (T) new TreeLayoutAlgorithm<>(this);
    }
  }

  public static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  protected TreeLayoutAlgorithm(Builder<V, ?, ?> builder) {
    this(
        builder.rootPredicate,
        builder.horizontalVertexSpacing,
        builder.verticalVertexSpacing,
        builder.expandLayout);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param horizontalVertexSpacing the horizontal spacing between adjacent siblings
   * @param verticalVertexSpacing the vertical spacing between adjacent siblings
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

  protected Predicate<V> rootPredicate;

  protected transient Set<V> alreadyDone = new HashSet<>();

  protected Map<V, Dimension> baseBounds = new HashMap<>();

  /** The default horizontal vertex spacing. Initialized to 50. */
  protected static final int DEFAULT_HORIZONTAL_VERTEX_SPACING = 50;

  /** The default vertical vertex spacing. Initialized to 50. */
  protected static final int DEFAULT_VERTICAL_VERTEX_SPACING = 50;

  /** The horizontal vertex spacing. Defaults to {@code DEFAULT_HORIZONTAL_VERTEX_SPACING}. */
  protected int horizontalVertexSpacing = DEFAULT_HORIZONTAL_VERTEX_SPACING;

  /** The vertical vertex spacing. Defaults to {@code DEFAULT_VERTICAL_VERTEX_SPACING}. */
  protected int verticalVertexSpacing = DEFAULT_VERTICAL_VERTEX_SPACING;

  protected boolean expandLayout;

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    buildTree(layoutModel);
  }

  public Map<V, Dimension> getBaseBounds() {
    return baseBounds;
  }

  /**
   * @param layoutModel the model to hold vertex positions
   * @return the roots vertices of the tree
   */
  protected Set<V> buildTree(LayoutModel<V> layoutModel) {
    alreadyDone = Sets.newHashSet();
    if (this.rootPredicate == null) {
      rootPredicate = v -> layoutModel.getGraph().incomingEdgesOf(v).isEmpty();
    }
    Set<V> roots =
        layoutModel.getGraph().vertexSet().stream().filter(rootPredicate).collect(toImmutableSet());

    Preconditions.checkArgument(roots.size() > 0);
    // the width of the tree under 'roots'. Includes one 'horizontalVertexSpacing' per child vertex
    int overallWidth = calculateWidth(layoutModel, roots, new HashSet<>());
    // add one additional 'horizontalVertexSpacing' for each tree (each root) + 1
    overallWidth += (roots.size() + 1) * horizontalVertexSpacing;
    int overallHeight = calculateHeight(layoutModel, roots);
    overallHeight += 2 * verticalVertexSpacing;

    log.trace("layoutModel.getWidth() {} overallWidth {}", layoutModel.getWidth(), overallWidth);
    log.trace(
        "layoutModel.getHeight() {} overallHeight {}", layoutModel.getHeight(), overallHeight);
    int largerWidth = Math.max(layoutModel.getWidth(), overallWidth);
    int largerHeight = Math.max(layoutModel.getHeight(), overallHeight);
    if (expandLayout) {
      layoutModel.setSize(largerWidth, largerHeight);
    }
    int x = horizontalVertexSpacing;
    if (overallWidth < layoutModel.getWidth()) {
      // start later
      x += layoutModel.getWidth() / 2 - overallWidth / 2;
    }

    int y = getInitialY(layoutModel.getHeight(), overallHeight);
    log.trace("got initial y of {}", y);

    for (V vertex : roots) {
      int w = this.baseBounds.get(vertex).width;
      log.trace("w is {} and baseWidths.get(vertex) = {}", w, baseBounds.get(vertex).width);
      x += w / 2;
      log.trace("currentX after vertex {} is now {}", vertex, x);
      buildTree(layoutModel, vertex, x, y);
      x += w / 2 + horizontalVertexSpacing;
    }
    return roots;
  }

  protected int getInitialY(int layoutHeight, int treeHeight) {
    if (layoutHeight == treeHeight) {
      return this.verticalVertexSpacing;
    }
    return layoutHeight / 2 - treeHeight / 2;
  }

  protected void buildTree(LayoutModel<V> layoutModel, V vertex, int x, int y) {
    if (alreadyDone.add(vertex)) {
      //go one level further down
      y += this.verticalVertexSpacing;
      log.trace("Set vertex {} to {}", vertex, Point.of(x, y));
      layoutModel.set(vertex, x, y);

      double sizeXofCurrent = baseBounds.get(vertex).width;
      x -= sizeXofCurrent / 2;

      for (V element : Graphs.successorListOf(layoutModel.getGraph(), vertex)) {
        log.trace("get base position of {} from {}", element, baseBounds);
        double sizeXofChild = this.baseBounds.get(element).width;
        x += sizeXofChild / 2;
        buildTree(layoutModel, element, x, y);
        x += sizeXofChild / 2 + horizontalVertexSpacing;
      }
    }
  }

  protected int calculateWidth(LayoutModel<V> layoutModel, V vertex, Set<V> seen) {
    Graph<V, ?> graph = layoutModel.getGraph();
    log.trace("graph is {}", graph);
    List<V> successors = Graphs.successorListOf(graph, vertex);
    log.trace("successors of {} are {}", vertex, successors);
    successors.removeIf(seen::contains);
    log.trace("filtered successors of {} are {}", vertex, successors);
    seen.addAll(successors);

    final int size =
        Math.max(
            0,
            successors
                    .stream()
                    .mapToInt(
                        element ->
                            calculateWidth(layoutModel, element, seen) + horizontalVertexSpacing)
                    .sum()
                - horizontalVertexSpacing);
    log.trace("calcWidth baseWidths put {} {}", vertex, size);
    //    baseBounds.merge(vertex, Dimension.of(0, size), (r, t) -> Dimension.of(size, r.height));
    baseBounds.merge(vertex, Dimension.of(size, 0), (r, t) -> Dimension.of(size, r.height));
    return size;
  }

  protected int calculateWidth(LayoutModel<V> layoutModel, Collection<V> roots, Set<V> seen) {
    int width = roots.stream().mapToInt(vertex -> calculateWidth(layoutModel, vertex, seen)).sum();
    log.debug("entire width from {} is {}", roots, width);
    return width;
  }

  protected int calculateHeight(LayoutModel<V> layoutModel, V vertex, Set<V> seen) {
    Graph<V, ?> graph = layoutModel.getGraph();
    List<V> successors = Graphs.successorListOf(graph, vertex);
    log.trace("graph is {}", graph);
    log.trace("h successors of {} are {}", vertex, successors);
    successors.removeIf(seen::contains);
    log.trace("filtered h successors of {} are {}", vertex, successors);

    seen.addAll(successors);

    final int height =
        successors
            .stream()
            .mapToInt(
                element -> calculateHeight(layoutModel, element, seen) + verticalVertexSpacing)
            .max()
            .orElse(0);
    baseBounds.merge(vertex, Dimension.of(0, height), (r, t) -> Dimension.of(r.width, height));
    return height;
  }

  protected int calculateHeight(LayoutModel<V> layoutModel, Collection<V> roots) {

    return roots
            .stream()
            .mapToInt(vertex -> calculateHeight(layoutModel, vertex, new HashSet<V>()))
            .max()
            .orElse(verticalVertexSpacing)
        + verticalVertexSpacing;
  }

  /** @return the center of this layout's area. */
  public Point getCenter(LayoutModel<V> layoutModel) {
    return Point.of(layoutModel.getWidth() / 2, layoutModel.getHeight() / 2);
  }
}
