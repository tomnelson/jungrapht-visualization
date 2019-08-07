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
public class EdgePredicatedTreeLayoutAlgorithm<V, E> implements LayoutAlgorithm<V> {

  private static final Logger log =
      LoggerFactory.getLogger(EdgePredicatedTreeLayoutAlgorithm.class);

  protected Collection<V> roots = new HashSet<>();

  public static class Builder<V, E> {
    private int horizontalVertexSpacing = DEFAULT_HORIZONTAL_VERTEX_SPACING;
    private int verticalVertexSpacing = DEFAULT_VERTICAL_VERTEX_SPACING;
    private Predicate<E>
        edgePredicate; // edges that do not satisfy this predicate will not expand the tree width/height

    public Builder horizontalVertexSpacing(int horizontalVertexSpacing) {
      Preconditions.checkArgument(
          horizontalVertexSpacing > 0, "horizontalVertexSpacing must be positive");
      this.horizontalVertexSpacing = horizontalVertexSpacing;
      return this;
    }

    public Builder verticalVertexSpacing(int verticalVertexSpacing) {
      Preconditions.checkArgument(
          verticalVertexSpacing > 0, "verticalVertexSpacing must be positive");
      this.verticalVertexSpacing = verticalVertexSpacing;
      return this;
    }

    public Builder edgePredicate(Predicate<E> edgePredicate) {
      this.edgePredicate = edgePredicate;
      return this;
    }

    public EdgePredicatedTreeLayoutAlgorithm<V, E> build() {
      return new EdgePredicatedTreeLayoutAlgorithm(this);
    }
  }

  public static Builder builder() {
    return new Builder<>();
  }

  protected EdgePredicatedTreeLayoutAlgorithm(Builder<V, E> builder) {
    this(builder.horizontalVertexSpacing, builder.verticalVertexSpacing, builder.edgePredicate);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param horizontalVertexSpacing the horizontal spacing between adjacent siblings
   * @param verticalVertexSpacing the vertical spacing between adjacent siblings
   */
  private EdgePredicatedTreeLayoutAlgorithm(
      int horizontalVertexSpacing, int verticalVertexSpacing, Predicate<E> edgePredicate) {
    this.horizontalVertexSpacing = horizontalVertexSpacing;
    this.verticalVertexSpacing = verticalVertexSpacing;
    this.edgePredicate = edgePredicate;
  }

  protected LoadingCache<V, Integer> basePositions =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> 0));

  protected transient Set<V> alreadyDone = new HashSet<>();

  /** The default horizontal vertex spacing. Initialized to 50. */
  protected static final int DEFAULT_HORIZONTAL_VERTEX_SPACING = 50;

  /** The default vertical vertex spacing. Initialized to 50. */
  protected static final int DEFAULT_VERTICAL_VERTEX_SPACING = 50;

  /** The horizontal vertex spacing. Defaults to {@code DEFAULT_HORIZONTAL_VERTEX_SPACING}. */
  protected int horizontalVertexSpacing = DEFAULT_HORIZONTAL_VERTEX_SPACING;

  /** The vertical vertex spacing. Defaults to {@code DEFAULT_VERTICAL_VERTEX_SPACING}. */
  protected int verticalVertexSpacing = DEFAULT_VERTICAL_VERTEX_SPACING;

  protected double currentX;
  protected double currentY;

  protected Predicate<E> edgePredicate;

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    buildTree(layoutModel);
  }

  protected void buildTree(LayoutModel<V> layoutModel) {
    alreadyDone = Sets.newHashSet();
    this.currentX = 0;
    this.currentY = 0;
    if (roots.isEmpty()) {
      Set<V> roots =
          layoutModel
              .getGraph()
              .vertexSet()
              .stream()
              .filter(vertex -> Graphs.predecessorListOf(layoutModel.getGraph(), vertex).isEmpty())
              .collect(toImmutableSet());

      this.roots = roots;
    }

    Preconditions.checkArgument(roots.size() > 0);
    // the width of the tree under 'roots'. Includes one 'horizontalVertexSpacing' per child vertex
    int overallWidth = calculateWidth(layoutModel, roots, new HashSet<>());
    // add one additional 'horizontalVertexSpacing' for each tree (each root)
    overallWidth += (roots.size() + 1) * horizontalVertexSpacing;
    int overallHeight = calculateHeight(layoutModel, roots);
    overallHeight += 2 * verticalVertexSpacing;

    if (overallWidth > overallHeight) {
      verticalVertexSpacing *= (float) overallWidth / (float) overallHeight / 4.0;
      overallHeight = overallWidth / 4;
    }

    layoutModel.setSize(
        Math.max(layoutModel.getWidth(), overallWidth),
        Math.max(layoutModel.getHeight(), overallHeight));
    Set<V> seen = new HashSet<>();
    roots.forEach(
        vertex -> {
          calculateWidth(layoutModel, vertex, seen); //new HashSet<>());
          currentX += (this.basePositions.getUnchecked(vertex) / 2 + this.horizontalVertexSpacing);
          log.debug("currentX after vertex {} is now {}", vertex, currentX);
          buildTree(layoutModel, vertex, (int) currentX);
        });
  }

  private Set<E> filteredEdges(Graph<V, E> graph, V source) {
    return graph
        .outgoingEdgesOf(source)
        .stream()
        .filter(e -> edgePredicate.test(e))
        .collect(Collectors.toSet());
  }

  private List<V> filteredTargets(Graph<V, E> graph, V source) {
    return filteredEdges(graph, source)
        .stream()
        .map(graph::getEdgeTarget)
        .collect(Collectors.toList());
  }

  protected void buildTree(LayoutModel<V> layoutModel, V vertex, int x) {
    Graph<V, E> graph = (Graph<V, E>) layoutModel.getGraph();
    if (alreadyDone.add(vertex)) {
      //go one level further down
      double newY = this.currentY + this.verticalVertexSpacing;
      this.currentX = x;
      this.currentY = newY;
      log.debug("Set vertex {} to {}", vertex, Point.of(currentX, currentY));
      layoutModel.set(vertex, currentX, currentY);

      int sizeXofCurrent = basePositions.getUnchecked(vertex);

      int lastX = x - sizeXofCurrent / 2;

      int sizeXofChild;
      int startXofChild;

      for (E edgeElement : graph.outgoingEdgesOf(vertex)) {
        V element = graph.getEdgeTarget(edgeElement);
        log.trace("get base position of {} from {}", element, basePositions);
        sizeXofChild = this.basePositions.getUnchecked(element);
        startXofChild = lastX + sizeXofChild / 2;
        buildTree(layoutModel, element, startXofChild);

        lastX =
            lastX + sizeXofChild + (edgePredicate.test(edgeElement) ? horizontalVertexSpacing : 0);
      }
      this.currentY -= this.verticalVertexSpacing;
    }
  }

  private int calculateWidth(LayoutModel<V> layoutModel, V vertex, Set<V> seen) {
    Graph<V, E> graph = (Graph<V, E>) layoutModel.getGraph();
    log.trace("graph is {}", graph);
    List<V> successors = Graphs.successorListOf(graph, vertex);
    log.trace("successors of {} are {}", vertex, successors);
    successors.removeIf(seen::contains);
    log.trace("filtered successors of {} are {}", vertex, successors);
    seen.addAll(successors);

    int size =
        graph
            .outgoingEdgesOf(vertex)
            .stream()
            .filter(e -> edgePredicate.test(e)) // retain if the edgePredicate tests true
            .map(graph::getEdgeTarget) // get the edge target vertices
            .filter(
                successors
                    ::contains) // retain if the successors (filtered above) contain the vertex
            .mapToInt(
                element -> calculateWidth(layoutModel, element, seen) + horizontalVertexSpacing)
            .sum();
    size = Math.max(0, size - horizontalVertexSpacing);
    log.trace("calcWidth baseWidths put {} {}", vertex, size);
    basePositions.put(vertex, size);

    return size;
  }

  private int calculateWidth(LayoutModel<V> layoutModel, Collection<V> roots, Set<V> seen) {
    return roots.stream().mapToInt(vertex -> calculateWidth(layoutModel, vertex, seen)).sum();
  }

  private int calculateHeight(LayoutModel<V> layoutModel, V vertex, Set<V> seen) {
    Graph<V, E> graph = (Graph<V, E>) layoutModel.getGraph();
    List<V> successors = Graphs.successorListOf(graph, vertex);
    log.trace("graph is {}", graph);
    log.trace("h successors of {} are {}", vertex, successors);
    successors.removeIf(seen::contains);
    log.trace("filtered h successors of {} are {}", vertex, successors);
    seen.addAll(successors);

    return graph
        .outgoingEdgesOf(vertex)
        .stream()
        .filter(e -> edgePredicate.test(e)) // retain if the edgePredicate tests true
        .map(graph::getEdgeTarget) // get the edge target vertices
        .filter(
            successors::contains) // retain if the successors (filtered above) contain the vertex
        .mapToInt(element -> calculateHeight(layoutModel, element, seen) + verticalVertexSpacing)
        .max()
        .orElse(0);
  }

  private int calculateHeight(LayoutModel<V> layoutModel, Collection<V> roots) {

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
