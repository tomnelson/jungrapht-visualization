/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
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

/**
 * @author Karlheinz Toni
 * @author Tom Nelson - converted to jung2, refactored into Algorithm/Visitor
 */
public class TreeLayoutAlgorithm<N> implements LayoutAlgorithm<N> {

  private static final Logger log = LoggerFactory.getLogger(TreeLayoutAlgorithm.class);

  public static class Builder<N> {
    private int horizontalNodeSpacing = DEFAULT_HORIZONTAL_NODE_SPACING;
    private int verticalNodeSpacing = DEFAULT_VERTICAL_NODE_SPACING;

    public Builder horizontalNodeSpacing(int horizontalNodeSpacing) {
      Preconditions.checkArgument(
          horizontalNodeSpacing > 0, "horizontalNodeSpacing must be positive");
      this.horizontalNodeSpacing = horizontalNodeSpacing;
      return this;
    }

    public Builder verticalNodeSpacing(int verticalNodeSpacing) {
      Preconditions.checkArgument(verticalNodeSpacing > 0, "verticalNodeSpacing must be positive");
      this.verticalNodeSpacing = verticalNodeSpacing;
      return this;
    }

    public TreeLayoutAlgorithm<N> build() {
      return new TreeLayoutAlgorithm(this);
    }
  }

  public static Builder builder() {
    return new Builder<>();
  }

  protected TreeLayoutAlgorithm(Builder<N> builder) {
    this(builder.horizontalNodeSpacing, builder.verticalNodeSpacing);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param horizontalNodeSpacing the horizontal spacing between adjacent siblings
   * @param verticalNodeSpacing the vertical spacing between adjacent siblings
   */
  private TreeLayoutAlgorithm(int horizontalNodeSpacing, int verticalNodeSpacing) {
    this.horizontalNodeSpacing = horizontalNodeSpacing;
    this.verticalNodeSpacing = verticalNodeSpacing;
  }

  protected Map<N, Integer> basePositions = new HashMap<>();

  protected transient Set<N> alreadyDone = new HashSet<N>();

  /** The default horizontal node spacing. Initialized to 50. */
  protected static final int DEFAULT_HORIZONTAL_NODE_SPACING = 50;

  /** The default vertical node spacing. Initialized to 50. */
  protected static final int DEFAULT_VERTICAL_NODE_SPACING = 50;

  /** The horizontal node spacing. Defaults to {@code DEFAULT_HORIZONTAL_NODE_SPACING}. */
  protected int horizontalNodeSpacing = DEFAULT_HORIZONTAL_NODE_SPACING;

  /** The vertical node spacing. Defaults to {@code DEFAULT_VERTICAL_NODE_SPACING}. */
  protected int verticalNodeSpacing = DEFAULT_VERTICAL_NODE_SPACING;

  protected double currentX;
  protected double currentY;

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    buildTree(layoutModel);
  }

  protected void buildTree(LayoutModel<N> layoutModel) {
    alreadyDone = Sets.newHashSet();
    this.currentX = 0;
    this.currentY = 0;
    Set<N> roots =
        layoutModel
            .getGraph()
            .vertexSet()
            .stream()
            .filter(node -> Graphs.predecessorListOf(layoutModel.getGraph(), node).isEmpty())
            .collect(toImmutableSet());
    //            TreeUtils.roots(layoutModel.getGraph());
    Preconditions.checkArgument(roots.size() > 0);
    // the width of the tree under 'roots'. Includes one 'horizontalNodeSpacing' per child node
    int overallWidth = calculateWidth(layoutModel, roots);
    // add one additional 'horizontalNodeSpacing' for each tree (each root)
    overallWidth += (roots.size() + 1) * horizontalNodeSpacing;
    int overallHeight = calculateHeight(layoutModel, roots);
    overallHeight += 2 * verticalNodeSpacing;
    layoutModel.setSize(
        Math.max(layoutModel.getWidth(), overallWidth),
        Math.max(layoutModel.getHeight(), overallHeight));
    Set<N> seen = new HashSet<>();
    roots.forEach(
        node -> {
          calculateWidth(layoutModel, node, seen); //new HashSet<>());
          currentX += (this.basePositions.get(node) / 2 + this.horizontalNodeSpacing);
          log.debug("currentX after node {} is now {}", node, currentX);
          buildTree(layoutModel, node, (int) currentX);
        });
  }

  protected void buildTree(LayoutModel<N> layoutModel, N node, int x) {
    if (alreadyDone.add(node)) {
      //go one level further down
      double newY = this.currentY + this.verticalNodeSpacing;
      this.currentX = x;
      this.currentY = newY;
      log.debug("Set node {} to {}", node, Point.of(currentX, currentY));
      layoutModel.set(node, currentX, currentY);

      int sizeXofCurrent = basePositions.get(node);

      int lastX = x - sizeXofCurrent / 2;

      int sizeXofChild;
      int startXofChild;

      for (N element : Graphs.successorListOf(layoutModel.getGraph(), node)) {
        sizeXofChild = this.basePositions.get(element);
        startXofChild = lastX + sizeXofChild / 2;
        buildTree(layoutModel, element, startXofChild);

        lastX = lastX + sizeXofChild + horizontalNodeSpacing;
      }

      this.currentY -= this.verticalNodeSpacing;
    }
  }

  private int calculateWidth(LayoutModel<N> layoutModel, N node, Set<N> seen) {

    Graph<N, ?> graph = layoutModel.getGraph();
    log.trace("graph is {}", graph);
    List<N> successors = Graphs.successorListOf(graph, node);
    log.trace("successors of {} are {}", node, successors);
    //    for (N n : successors) {
    //      if (seen.contains(n)) {
    //        graph.removeEdge(node, n);
    //      }
    //    }
    successors.removeIf(n -> seen.contains(n));
    log.trace("filtered successors of {} are {}", node, successors);
    seen.addAll(successors);

    int size =
        successors
            .stream()
            .mapToInt(element -> calculateWidth(layoutModel, element, seen) + horizontalNodeSpacing)
            .sum();
    size = Math.max(0, size - horizontalNodeSpacing);
    log.debug("calcWidth basePositions put {} {}", node, size);
    basePositions.put(node, size);

    return size;
  }

  private int calculateWidth(LayoutModel<N> layoutModel, Collection<N> roots) {

    return roots
        .stream()
        .mapToInt(node -> calculateWidth(layoutModel, node, new HashSet<>()))
        .sum();
  }

  private int calculateHeight(LayoutModel<N> layoutModel, N node, Set<N> seen) {

    Graph<N, ?> graph = layoutModel.getGraph();
    List<N> successors = Graphs.successorListOf(graph, node);
    log.trace("graph is {}", graph);
    log.trace("h successors of {} are {}", node, successors);
    successors.removeIf(n -> seen.contains(n));
    log.trace("filtered h successors of {} are {}", node, successors);

    seen.addAll(successors);

    return successors
        .stream()
        .mapToInt(element -> calculateHeight(layoutModel, element, seen) + verticalNodeSpacing)
        .max()
        .orElse(0);
  }

  private int calculateHeight(LayoutModel<N> layoutModel, Collection<N> roots) {

    return roots
        .stream()
        .mapToInt(node -> calculateHeight(layoutModel, node, new HashSet<N>()))
        .sum();
  }

  /** @return the center of this layout's area. */
  public Point getCenter(LayoutModel<N> layoutModel) {
    return Point.of(layoutModel.getWidth() / 2, layoutModel.getHeight() / 2);
  }
}
