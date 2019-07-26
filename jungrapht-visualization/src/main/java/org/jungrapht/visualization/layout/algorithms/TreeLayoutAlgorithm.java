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
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class TreeLayoutAlgorithm<N> implements LayoutAlgorithm<N> {

  private static final Logger log = LoggerFactory.getLogger(TreeLayoutAlgorithm.class);

  public static class Builder<N, T extends TreeLayoutAlgorithm<N>, B extends Builder<N, T, B>> {
    protected int horizontalNodeSpacing = DEFAULT_HORIZONTAL_NODE_SPACING;
    protected int verticalNodeSpacing = DEFAULT_VERTICAL_NODE_SPACING;

    protected B self() {
      return (B) this;
    }

    public B horizontalNodeSpacing(int horizontalNodeSpacing) {
      Preconditions.checkArgument(
          horizontalNodeSpacing > 0, "horizontalNodeSpacing must be positive");
      this.horizontalNodeSpacing = horizontalNodeSpacing;
      return self();
    }

    public B verticalNodeSpacing(int verticalNodeSpacing) {
      Preconditions.checkArgument(verticalNodeSpacing > 0, "verticalNodeSpacing must be positive");
      this.verticalNodeSpacing = verticalNodeSpacing;
      return self();
    }

    public T build() {
      return (T) new TreeLayoutAlgorithm<>(this);
    }
  }

  public static <N> Builder<N, ?, ?> builder() {
    return new Builder<>();
  }

  protected TreeLayoutAlgorithm(Builder<N, ?, ?> builder) {
    this(builder.horizontalNodeSpacing, builder.verticalNodeSpacing);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param horizontalNodeSpacing the horizontal spacing between adjacent siblings
   * @param verticalNodeSpacing the vertical spacing between adjacent siblings
   */
  protected TreeLayoutAlgorithm(int horizontalNodeSpacing, int verticalNodeSpacing) {
    this.horizontalNodeSpacing = horizontalNodeSpacing;
    this.verticalNodeSpacing = verticalNodeSpacing;
  }

  protected Map<N, Integer> basePositions = new HashMap<>();

  protected transient Set<N> alreadyDone = new HashSet<>();

  /** The default horizontal node spacing. Initialized to 50. */
  protected static final int DEFAULT_HORIZONTAL_NODE_SPACING = 50;

  /** The default vertical node spacing. Initialized to 50. */
  protected static final int DEFAULT_VERTICAL_NODE_SPACING = 50;

  /** The horizontal node spacing. Defaults to {@code DEFAULT_HORIZONTAL_NODE_SPACING}. */
  protected int horizontalNodeSpacing = DEFAULT_HORIZONTAL_NODE_SPACING;

  /** The vertical node spacing. Defaults to {@code DEFAULT_VERTICAL_NODE_SPACING}. */
  protected int verticalNodeSpacing = DEFAULT_VERTICAL_NODE_SPACING;

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    buildTree(layoutModel);
  }

  /**
   * @param layoutModel the model to hold node positions
   * @return the roots nodes of the tree
   */
  protected Set<N> buildTree(LayoutModel<N> layoutModel) {
    alreadyDone = Sets.newHashSet();
    Set<N> roots =
        layoutModel
            .getGraph()
            .vertexSet()
            .stream()
            .filter(node -> Graphs.predecessorListOf(layoutModel.getGraph(), node).isEmpty())
            .collect(toImmutableSet());

    Preconditions.checkArgument(roots.size() > 0);
    // the width of the tree under 'roots'. Includes one 'horizontalNodeSpacing' per child node
    int overallWidth = calculateWidth(layoutModel, roots, new HashSet<>());
    // add one additional 'horizontalNodeSpacing' for each tree (each root)
    overallWidth += (roots.size() + 1) * horizontalNodeSpacing;
    int overallHeight = calculateHeight(layoutModel, roots);
    overallHeight += 2 * verticalNodeSpacing;

    int largerWidth = Math.max(layoutModel.getWidth(), overallWidth);
    int largerHeight = Math.max(layoutModel.getHeight(), overallHeight);
    layoutModel.setSize(largerWidth, largerHeight);
    log.trace("layoutModel.getWidth() {}", layoutModel.getWidth());
    log.trace("overallWidth {}", overallWidth);
    log.trace("layoutModel.getHeight() {}", layoutModel.getHeight());
    log.trace("overallHeight {}", overallHeight);

    int x = 0;
    int y = getInitialY(layoutModel.getHeight(), overallHeight);
    log.trace("got initial y of {}", y);

    Set<N> seen = new HashSet<>();
    for (N node : roots) {
      calculateWidth(layoutModel, node, seen);
      x += (this.basePositions.get(node) / 2 + this.horizontalNodeSpacing);
      log.trace("currentX after node {} is now {}", node, x);
      buildTree(layoutModel, node, x, y);
    }
    return roots;
  }

  protected int getInitialY(int layoutHeight, int treeHeight) {
    if (layoutHeight == treeHeight) {
      return this.verticalNodeSpacing;
    }
    return layoutHeight / 2 - treeHeight / 2;
  }

  protected void buildTree(LayoutModel<N> layoutModel, N node, int x, int y) {
    if (alreadyDone.add(node)) {
      //go one level further down
      y += this.verticalNodeSpacing;
      log.trace("Set node {} to {}", node, Point.of(x, y));
      layoutModel.set(node, x, y);

      int sizeXofCurrent = basePositions.get(node);
      x -= sizeXofCurrent / 2;

      int sizeXofChild;

      for (N element : Graphs.successorListOf(layoutModel.getGraph(), node)) {
        sizeXofChild = this.basePositions.get(element);
        x += sizeXofChild / 2;
        buildTree(layoutModel, element, x, y);
        x += sizeXofChild / 2 + horizontalNodeSpacing;
      }
    }
  }

  protected int calculateWidth(LayoutModel<N> layoutModel, N node, Set<N> seen) {

    Graph<N, ?> graph = layoutModel.getGraph();
    log.trace("graph is {}", graph);
    List<N> successors = Graphs.successorListOf(graph, node);
    log.trace("successors of {} are {}", node, successors);
    successors.removeIf(seen::contains);
    log.trace("filtered successors of {} are {}", node, successors);
    seen.addAll(successors);

    int size =
        successors
            .stream()
            .mapToInt(element -> calculateWidth(layoutModel, element, seen) + horizontalNodeSpacing)
            .sum();
    size = Math.max(0, size - horizontalNodeSpacing);
    log.trace("calcWidth basePositions put {} {}", node, size);
    basePositions.put(node, size);

    return size;
  }

  protected int calculateWidth(LayoutModel<N> layoutModel, Collection<N> roots, Set<N> seen) {

    return roots.stream().mapToInt(node -> calculateWidth(layoutModel, node, seen)).sum();
  }

  protected int calculateHeight(LayoutModel<N> layoutModel, N node, Set<N> seen) {

    Graph<N, ?> graph = layoutModel.getGraph();
    List<N> successors = Graphs.successorListOf(graph, node);
    log.trace("graph is {}", graph);
    log.trace("h successors of {} are {}", node, successors);
    successors.removeIf(seen::contains);
    log.trace("filtered h successors of {} are {}", node, successors);

    seen.addAll(successors);

    return successors
        .stream()
        .mapToInt(element -> calculateHeight(layoutModel, element, seen) + verticalNodeSpacing)
        .max()
        .orElse(0);
  }

  protected int calculateHeight(LayoutModel<N> layoutModel, Collection<N> roots) {

    return roots
            .stream()
            .mapToInt(node -> calculateHeight(layoutModel, node, new HashSet<N>()))
            .max()
            .orElse(verticalNodeSpacing)
        + verticalNodeSpacing;
  }

  /** @return the center of this layout's area. */
  public Point getCenter(LayoutModel<N> layoutModel) {
    return Point.of(layoutModel.getWidth() / 2, layoutModel.getHeight() / 2);
  }
}
