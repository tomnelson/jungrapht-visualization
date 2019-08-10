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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class EdgeAwareTreeLayoutAlgorithm<V, E>
    implements EdgeAwareLayoutAlgorithm<V, E>, EdgeSorting<E>, EdgePredicated<E> {

  private static final Logger log = LoggerFactory.getLogger(EdgeAwareTreeLayoutAlgorithm.class);

  public static class Builder<
      V, E, T extends EdgeAwareTreeLayoutAlgorithm<V, E>, B extends Builder<V, E, T, B>> {
    protected int horizontalVertexSpacing = DEFAULT_HORIZONTAL_VERTEX_SPACING;
    protected int verticalVertexSpacing = DEFAULT_VERTICAL_VERTEX_SPACING;
    protected Predicate<V> vertexPredicate = v -> false;
    protected Predicate<E> edgePredicate = e -> false;
    protected Comparator<V> vertexComparator = (v1, v2) -> 0;
    protected Comparator<E> edgeComparator = (e1, e2) -> 0;
    protected boolean expandLayout = true;

    protected B self() {
      return (B) this;
    }

    public B horizontalVertexSpacing(int horizontalVertexSpacing) {
      Preconditions.checkArgument(
          horizontalVertexSpacing > 0, "horizontalVertexSpacing must be positive");
      this.horizontalVertexSpacing = horizontalVertexSpacing;
      return self();
    }

    public B vertexPredicate(Predicate<V> vertexPredicate) {
      this.vertexPredicate = vertexPredicate;
      return self();
    }

    public B edgePredicate(Predicate<E> edgePredicate) {
      this.edgePredicate = edgePredicate;
      return self();
    }

    public B vertexComparator(Comparator<V> vertexComparator) {
      this.vertexComparator = vertexComparator;
      return self();
    }

    public B edgeComparator(Comparator<E> edgeComparator) {
      this.edgeComparator = edgeComparator;
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
      return (T) new EdgeAwareTreeLayoutAlgorithm<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected EdgeAwareTreeLayoutAlgorithm(Builder<V, E, ?, ?> builder) {
    this(
        builder.horizontalVertexSpacing,
        builder.verticalVertexSpacing,
        builder.vertexPredicate,
        builder.edgePredicate,
        builder.vertexComparator,
        builder.edgeComparator,
        builder.expandLayout);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param horizontalVertexSpacing the horizontal spacing between adjacent siblings
   * @param verticalVertexSpacing the vertical spacing between adjacent siblings
   */
  protected EdgeAwareTreeLayoutAlgorithm(
      int horizontalVertexSpacing,
      int verticalVertexSpacing,
      Predicate<V> vertexPredicate,
      Predicate<E> edgePredicate,
      Comparator<V> vertexComparator,
      Comparator<E> edgeComparator,
      boolean expandLayout) {
    this.horizontalVertexSpacing = horizontalVertexSpacing;
    this.verticalVertexSpacing = verticalVertexSpacing;
    this.vertexPredicate = vertexPredicate;
    this.edgePredicate = edgePredicate;
    this.vertexComparator = vertexComparator;
    this.edgeComparator = edgeComparator;
    this.expandLayout = expandLayout;
  }

  protected transient Set<V> alreadyDone = new HashSet<>();

  protected Map<V, Dimension> baseBounds = new HashMap<>();

  /** The default horizontal vertex spacing. Initialized to 50. */
  protected static final int DEFAULT_HORIZONTAL_VERTEX_SPACING = 50;

  /** The default vertical vertex spacing. Initialized to 50. */
  protected static final int DEFAULT_VERTICAL_VERTEX_SPACING = 50;

  /** The horizontal vertex spacing. Defaults to {@code DEFAULT_HORIZONTAL_VERTEX_SPACING}. */
  protected int horizontalVertexSpacing;

  /** The vertical vertex spacing. Defaults to {@code DEFAULT_VERTICAL_VERTEX_SPACING}. */
  protected int verticalVertexSpacing;

  protected Predicate<V> vertexPredicate;

  protected Predicate<E> edgePredicate;

  protected Comparator<V> vertexComparator;

  protected Comparator<E> edgeComparator;

  protected boolean expandLayout;

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    buildTree(layoutModel);
  }

  @Override
  public void setVertexPredicate(Predicate<V> vertexPredicate) {
    this.vertexPredicate = vertexPredicate;
  }

  @Override
  public void setEdgePredicate(Predicate<E> edgePredicate) {
    this.edgePredicate = edgePredicate;
  }

  @Override
  public void setVertexComparator(Comparator<V> vertexComparator) {
    this.vertexComparator = vertexComparator;
  }

  @Override
  public void setEdgeComparator(Comparator<E> edgeComparator) {
    this.edgeComparator = edgeComparator;
  }

  public Map<V, Dimension> getBaseBounds() {
    return baseBounds;
  }

  /**
   * @param layoutModel the model to hold vertex positions
   * @return the roots vertices of the tree
   */
  protected Set<V> buildTree(LayoutModel<V> layoutModel) {
    Graph<V, E> graph = layoutModel.getGraph();
    alreadyDone = Sets.newHashSet();

    // discover roots of the tree(s)
    Set<V> roots =
        layoutModel
            .getGraph()
            .vertexSet()
            .stream()
            .filter(vertex -> Graphs.predecessorListOf(layoutModel.getGraph(), vertex).isEmpty())
            .collect(toImmutableSet());

    Preconditions.checkArgument(roots.size() > 0);

    // measure the tree

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

    // position the vertices
    int x = horizontalVertexSpacing;
    int y = getInitialY(layoutModel.getHeight(), overallHeight);
    log.trace("got initial y of {}", y);

    // test each root vertex to see if it is on the filtered path
    for (V vertex : roots) {

      boolean onFilteredPath = false;
      onFilteredPath |= vertexPredicate.test(vertex);
      for (E edge : graph.outgoingEdgesOf(vertex)) {
        onFilteredPath |= edgePredicate.test(edge);
      }

      if (onFilteredPath) {
        int w = this.baseBounds.get(vertex).width;
        log.trace("w is {} and baseWidths.get(vertex) = {}", w, baseBounds.get(vertex).width);
        log.trace("currentX after vertex {} is now {}", vertex, x);
        buildTree(layoutModel, vertex, x, y);
        x += w + horizontalVertexSpacing; //- w / 2;
      } else {
        int w = this.baseBounds.get(vertex).width;
        log.trace("w is {} and baseWidths.get(vertex) = {}", w, baseBounds.get(vertex).width);
        x += w / 2;
        log.trace("currentX after vertex {} is now {}", vertex, x);
        buildTree(layoutModel, vertex, x, y);
        x += w / 2 + horizontalVertexSpacing;
      }
    }
    return roots;
  }

  protected int getInitialY(int layoutHeight, int treeHeight) {
    if (layoutHeight == treeHeight) {
      return this.verticalVertexSpacing;
    }
    return layoutHeight / 2 - treeHeight / 2;
  }

  /**
   * position the vertices. Any vertex on the filtered chain is put on the LHS of its bounding box
   *
   * @param layoutModel
   * @param sourceVertex
   * @param x
   * @param y
   */
  protected void buildTree(LayoutModel<V> layoutModel, V sourceVertex, int x, int y) {

    Graph<V, E> graph = layoutModel.getGraph();
    if (alreadyDone.add(sourceVertex)) {
      //go one level further down
      y += this.verticalVertexSpacing;
      log.trace("Set vertex {} to {}", sourceVertex, Point.of(x, y));
      layoutModel.set(sourceVertex, x, y);

      double sizeXofCurrent = baseBounds.getOrDefault(sourceVertex, Dimension.of(0, 0)).width;
      x -= sizeXofCurrent / 2;

      double sizeXofChild = 0;

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
          log.info("get base position of {} from {}", targetVertex, baseBounds);
          buildTree(layoutModel, targetVertex, x, y);
          x += sizeXofChild + horizontalVertexSpacing;
        } else {
          log.trace("get base position of {} from {}", targetVertex, baseBounds);
          sizeXofChild = this.baseBounds.get(targetVertex).width;
          x += sizeXofChild / 2;
          buildTree(layoutModel, targetVertex, x, y);
          x += sizeXofChild / 2 + horizontalVertexSpacing;
        }
      }
    }
  }

  /**
   * calculate the width(s) of tree parent nodes without filtering
   *
   * @param layoutModel
   * @param sourceVertex
   * @param seen
   * @return
   */
  protected int calculateWidth(LayoutModel<V> layoutModel, V sourceVertex, Set<V> seen) {
    Graph<V, E> graph = layoutModel.getGraph();
    log.trace("graph is {}", graph);
    List<V> successors = Graphs.successorListOf(graph, sourceVertex);
    log.trace("successors of {} are {}", sourceVertex, successors);
    successors.removeIf(seen::contains);
    log.info("filtered successors of {} are {}", sourceVertex, successors);
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
                    .sorted(vertexComparator)
                    .mapToInt(
                        element ->
                            calculateWidth(layoutModel, element, seen) + horizontalVertexSpacing)
                    .sum()
                - horizontalVertexSpacing);

    log.trace("calcWidth baseWidths put {} {}", sourceVertex, size);
    baseBounds.merge(sourceVertex, Dimension.of(size, 0), (r, t) -> Dimension.of(size, r.height));
    return size;
  }

  protected int calculateWidth(LayoutModel<V> layoutModel, Collection<V> roots, Set<V> seen) {
    int width = roots.stream().mapToInt(vertex -> calculateWidth(layoutModel, vertex, seen)).sum();
    log.debug("entire width from {} is {}", roots, width);
    return width;
  }

  /**
   * calculate the height(s) of parent nodes without filtering
   *
   * @param layoutModel
   * @param vertex
   * @param seen
   * @return
   */
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
            .map(graph::getEdgeTarget) // get the edge target vertices
            .filter(
                successors
                    ::contains) // retain if the successors (filtered above) contain the vertex
            .sorted(vertexComparator)
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
