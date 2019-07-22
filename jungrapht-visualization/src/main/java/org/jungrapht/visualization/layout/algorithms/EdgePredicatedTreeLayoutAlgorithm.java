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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class EdgePredicatedTreeLayoutAlgorithm<N, E> implements LayoutAlgorithm<N> {

  private static final Logger log =
      LoggerFactory.getLogger(EdgePredicatedTreeLayoutAlgorithm.class);

  protected Collection<N> roots = new HashSet<>();

  public static class Builder<N, E> {
    private int horizontalNodeSpacing = DEFAULT_HORIZONTAL_NODE_SPACING;
    private int verticalNodeSpacing = DEFAULT_VERTICAL_NODE_SPACING;
    private Predicate<E>
        edgePredicate; // edges that do not satisfy this predicate will not expand the tree width/height

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

    public Builder edgePredicate(Predicate<E> edgePredicate) {
      this.edgePredicate = edgePredicate;
      return this;
    }

    public EdgePredicatedTreeLayoutAlgorithm<N, E> build() {
      return new EdgePredicatedTreeLayoutAlgorithm(this);
    }
  }

  public static Builder builder() {
    return new Builder<>();
  }

  protected EdgePredicatedTreeLayoutAlgorithm(Builder<N, E> builder) {
    this(builder.horizontalNodeSpacing, builder.verticalNodeSpacing, builder.edgePredicate);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param horizontalNodeSpacing the horizontal spacing between adjacent siblings
   * @param verticalNodeSpacing the vertical spacing between adjacent siblings
   */
  private EdgePredicatedTreeLayoutAlgorithm(
      int horizontalNodeSpacing, int verticalNodeSpacing, Predicate<E> edgePredicate) {
    this.horizontalNodeSpacing = horizontalNodeSpacing;
    this.verticalNodeSpacing = verticalNodeSpacing;
    this.edgePredicate = edgePredicate;
  }

  protected LoadingCache<N, Integer> basePositions =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> 0));

  protected transient Set<N> alreadyDone = new HashSet<>();

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

  protected Predicate<E> edgePredicate;

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
    this.roots = roots;

    Preconditions.checkArgument(roots.size() > 0);
    // the width of the tree under 'roots'. Includes one 'horizontalNodeSpacing' per child node
    int overallWidth = calculateWidth(layoutModel, roots, new HashSet<>());
    // add one additional 'horizontalNodeSpacing' for each tree (each root)
    overallWidth += (roots.size() + 1) * horizontalNodeSpacing;
    int overallHeight = calculateHeight(layoutModel, roots);
    overallHeight += 2 * verticalNodeSpacing;

    if (overallWidth > overallHeight) {
      verticalNodeSpacing *= (float) overallWidth / (float) overallHeight / 4.0;
      overallHeight = overallWidth / 4;
    }

    layoutModel.setSize(
        Math.max(layoutModel.getWidth(), overallWidth),
        Math.max(layoutModel.getHeight(), overallHeight));
    Set<N> seen = new HashSet<>();
    roots.forEach(
        node -> {
          calculateWidth(layoutModel, node, seen); //new HashSet<>());
          currentX += (this.basePositions.getUnchecked(node) / 2 + this.horizontalNodeSpacing);
          log.debug("currentX after node {} is now {}", node, currentX);
          buildTree(layoutModel, node, (int) currentX);
        });
  }

  private Set<E> filteredEdges(Graph<N, E> graph, N source) {
    return graph
        .outgoingEdgesOf(source)
        .stream()
        .filter(e -> edgePredicate.test(e))
        .collect(Collectors.toSet());
  }

  private List<N> filteredTargets(Graph<N, E> graph, N source) {
    return filteredEdges(graph, source)
        .stream()
        .map(graph::getEdgeTarget)
        .collect(Collectors.toList());
  }

  protected void buildTree(LayoutModel<N> layoutModel, N node, int x) {
    Graph<N, E> graph = (Graph<N, E>) layoutModel.getGraph();
    if (alreadyDone.add(node)) {
      //go one level further down
      double newY = this.currentY + this.verticalNodeSpacing;
      this.currentX = x;
      this.currentY = newY;
      log.debug("Set node {} to {}", node, Point.of(currentX, currentY));
      layoutModel.set(node, currentX, currentY);

      int sizeXofCurrent = basePositions.getUnchecked(node);

      int lastX = x - sizeXofCurrent / 2;

      int sizeXofChild;
      int startXofChild;

      for (E edgeElement : graph.outgoingEdgesOf(node)) {
        N element = graph.getEdgeTarget(edgeElement);
        log.trace("get base position of {} from {}", element, basePositions);
        sizeXofChild = this.basePositions.getUnchecked(element);
        startXofChild = lastX + sizeXofChild / 2;
        buildTree(layoutModel, element, startXofChild);

        lastX =
            lastX + sizeXofChild + (edgePredicate.test(edgeElement) ? horizontalNodeSpacing : 0);
      }
      this.currentY -= this.verticalNodeSpacing;
    }
  }

  private int calculateWidth(LayoutModel<N> layoutModel, N node, Set<N> seen) {
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
            .filter(e -> edgePredicate.test(e)) // retain if the edgePredicate tests true
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

  private int calculateWidth(LayoutModel<N> layoutModel, Collection<N> roots, Set<N> seen) {
    return roots.stream().mapToInt(node -> calculateWidth(layoutModel, node, seen)).sum();
  }

  private int calculateHeight(LayoutModel<N> layoutModel, N node, Set<N> seen) {
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
        .filter(e -> edgePredicate.test(e)) // retain if the edgePredicate tests true
        .map(graph::getEdgeTarget) // get the edge target nodes
        .filter(successors::contains) // retain if the successors (filtered above) contain the node
        .mapToInt(element -> calculateHeight(layoutModel, element, seen) + verticalNodeSpacing)
        .max()
        .orElse(0);
  }

  private int calculateHeight(LayoutModel<N> layoutModel, Collection<N> roots) {

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
