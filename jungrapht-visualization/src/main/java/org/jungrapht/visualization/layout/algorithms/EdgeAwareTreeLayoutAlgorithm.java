package org.jungrapht.visualization.layout.algorithms;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TreeLayoutAlgorithm} that can evaluate {@code Comparator} and {@link Predicate} for both
 * edges and vertices.
 *
 * <p>During graph traversal (for tree layout) the Comparators may be spcified to help order the
 * traversal to favor certain edges or vertices. Similarly, the Predicates may be used to help
 * follow a desired path to a vertex when multiple alternatives appear in the graph.
 *
 * @author Tom Nelson
 */
public class EdgeAwareTreeLayoutAlgorithm<V, E> extends TreeLayoutAlgorithm<V>
    implements TreeLayout<V>,
        EdgeAwareLayoutAlgorithm<V, E>,
        EdgeSorting<E>,
        EdgePredicated<E>,
        VertexSorting<V>,
        VertexPredicated<V> {

  private static final Logger log = LoggerFactory.getLogger(EdgeAwareTreeLayoutAlgorithm.class);

  /**
   * a Builder to create a configured instance of an EdgeAwareTreeLayoutAlgorithm
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  public static class Builder<
          V, E, T extends EdgeAwareTreeLayoutAlgorithm<V, E>, B extends Builder<V, E, T, B>>
      extends TreeLayoutAlgorithm.Builder<V, T, B>
      implements EdgeAwareLayoutAlgorithm.Builder<V, E, T, B> {
    protected Predicate<V> vertexPredicate = v -> false;
    protected Predicate<E> edgePredicate = e -> false;
    protected Comparator<V> vertexComparator = (v1, v2) -> 0;
    protected Comparator<E> edgeComparator = (e1, e2) -> 0;
    protected boolean alignFavoredEdges = true;

    /** {@inheritDoc} */
    protected B self() {
      return (B) this;
    }

    /**
     * @param vertexPredicate {@link Predicate} to apply to vertices
     * @return this Builder
     */
    public B vertexPredicate(Predicate<V> vertexPredicate) {
      this.vertexPredicate = vertexPredicate;
      return self();
    }

    /**
     * @param edgePredicate {@link Predicate} to apply to edges
     * @return this Builder
     */
    public B edgePredicate(Predicate<E> edgePredicate) {
      this.edgePredicate = edgePredicate;
      return self();
    }

    /**
     * @param vertexComparator {@link Comparator} to sort vertices
     * @return this Builder
     */
    public B vertexComparator(Comparator<V> vertexComparator) {
      this.vertexComparator = vertexComparator;
      return self();
    }

    /**
     * @param edgeComparator {@link Comparator} to sort edges
     * @return this Builder
     */
    public B edgeComparator(Comparator<E> edgeComparator) {
      this.edgeComparator = edgeComparator;
      return self();
    }

    public B alignFavoredEdges(boolean alignFavoredEdges) {
      this.alignFavoredEdges = alignFavoredEdges;
      return self();
    }

    /** {@inheritDoc} */
    public T build() {
      return (T) new EdgeAwareTreeLayoutAlgorithm<>(this);
    }
  }

  /**
   * @param <V> vertex type
   * @param <E> edge type
   * @return a Builder ready to configure
   */
  public static <V, E> Builder<V, E, ?, ?> edgeAwareBuilder() {
    return new Builder<>();
  }

  public EdgeAwareTreeLayoutAlgorithm() {
    this(EdgeAwareTreeLayoutAlgorithm.edgeAwareBuilder());
  }

  /**
   * Create an instance with the passed builder parameters
   *
   * @param builder the configuration
   */
  protected EdgeAwareTreeLayoutAlgorithm(Builder<V, E, ?, ?> builder) {
    super(builder);
    this.vertexPredicate = builder.vertexPredicate;
    this.edgePredicate = builder.edgePredicate;
    this.vertexComparator = builder.vertexComparator;
    this.edgeComparator = builder.edgeComparator;
    this.alignFavoredEdges = builder.alignFavoredEdges;
  }

  /** a {@link Predicate} to filter vertices */
  protected Predicate<V> vertexPredicate;

  /** a {@link Predicate} to filter edges */
  protected Predicate<E> edgePredicate;

  /** a {@link Comparator} to sort vertices */
  protected Comparator<V> vertexComparator;

  /** a {@link Comparator} to sort edges */
  protected Comparator<E> edgeComparator;

  protected boolean alignFavoredEdges;

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    Graph<V, ?> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    buildTree(layoutModel);
  }

  /** @param vertexPredicate parameter to set */
  @Override
  public void setVertexPredicate(Predicate<V> vertexPredicate) {
    this.vertexPredicate = vertexPredicate;
  }

  /** @param edgePredicate parameter to set */
  @Override
  public void setEdgePredicate(Predicate<E> edgePredicate) {
    this.edgePredicate = edgePredicate;
  }

  /** @param vertexComparator parameter to set */
  @Override
  public void setVertexComparator(Comparator<V> vertexComparator) {
    if (vertexComparator == null) {
      vertexComparator = (v1, v2) -> 0;
    }
    this.vertexComparator = vertexComparator;
  }

  /** @param edgeComparator parameter to set */
  @Override
  public void setEdgeComparator(Comparator<E> edgeComparator) {
    if (edgeComparator == null) {
      edgeComparator = (e1, e2) -> 0;
    }
    this.edgeComparator = edgeComparator;
  }

  /**
   * @param layoutModel the model to hold vertex positions
   * @return the roots vertices of the tree
   */
  protected Set<V> buildTree(LayoutModel<V> layoutModel) {
    Set<V> roots = super.buildTree(layoutModel);
    if (alignFavoredEdges) {
      roots.addAll(afterBuildTree(layoutModel));
    }
    return roots;
  }

  /**
   * Build the tree (position the vertices) starting at the passed vertex and then descending
   * recursively into its child vertices
   *
   * @param layoutModel the {@link LayoutModel} to hold the vertex positions
   * @param vertex the vertex to place in position
   * @param x the x position
   * @param y the y position
   * @param seen a set of vertices that were already 'seen' (and placed in the layoutModel)
   */
  protected void buildTree(LayoutModel<V> layoutModel, V vertex, int x, int y, Set<V> seen) {
    if (seen.add(vertex)) {
      Graph<V, E> graph = layoutModel.getGraph();
      log.trace("buildTree placing {}", vertex);
      // go one level further down
      y += this.verticalVertexSpacing;
      log.trace("Set vertex {} to {}", vertex, Point.of(x, y));
      layoutModel.set(vertex, x, y);
      merge(layoutModel, vertex);

      double sizeXofCurrent = baseBounds.get(vertex).width;
      x -= sizeXofCurrent / 2;

      for (E edge :
          graph
              .outgoingEdgesOf(vertex)
              .stream()
              .sorted(edgeComparator)
              .collect(Collectors.toCollection(LinkedHashSet::new))) {
        //     log.info("consider edge {}", edge);
        if (edgePredicate.test(edge)
            || graph.incomingEdgesOf(graph.getEdgeTarget(edge)).stream().noneMatch(edgePredicate)) {
          V v = graph.getEdgeTarget(edge);
          if (!rootPredicate.test(v) && !seen.contains(v)) {
            double sizeXofChild = this.baseBounds.getOrDefault(v, Rectangle.IDENTITY).width;
            x += sizeXofChild / 2;

            buildTree(layoutModel, v, x, y, seen);
            merge(layoutModel, v);
            x += sizeXofChild / 2 + horizontalVertexSpacing;
          }
        }
      }
    }
  }

  /**
   * Calculate the overall width of the subtree rooted at the passed vertex. The edgePredicate is
   * used to keep favored edges together. For example, if a non-favored edge provides a vertex
   * sooner than its favored edge, the non-favored edge path will be deferred until the favored edge
   * provides the same vertex. This prevents edges that span from one tree to another from
   * 'stealing' a subtree that we would prefer to keep together on its favored edge path
   *
   * @param layoutModel the source of the Graph and its vertices
   * @param vertex the vertex at the top of the current subtree
   * @param seen a set of vertices that were already counted
   * @return the overall width of the subtree rooted at the passed vertex
   */
  protected int calculateWidth(LayoutModel<V> layoutModel, V vertex, Set<V> seen) {
    if (seen.add(vertex)) {
      Graph<V, E> graph = layoutModel.getGraph();
      int width =
          Math.max(
              0,
              graph
                      .outgoingEdgesOf(vertex)
                      .stream()
                      .sorted(edgeComparator)
                      // skip over any edge that is not in the edgePredicate but also has a target with an
                      // incoming edge that is in the edgePredicate
                      .filter(
                          e ->
                              edgePredicate.test(e)
                                  || graph
                                      .incomingEdgesOf(graph.getEdgeTarget(e))
                                      .stream()
                                      .noneMatch(edgePredicate))
                      .map(graph::getEdgeTarget)
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
   * Calculate the overall height of the subtree rooted at the passed vertex. The edgePredicate is
   * used to keep favored edges together. For example, if a non-favored edge provides a vertex
   * sooner than its favored edge, the non-favored edge path will be deferred until the favored edge
   * provides the same vertex. This prevents edges that span from one tree to another from
   * 'stealing' a subtree that we would prefer to keep together on its favored edge path
   *
   * @param layoutModel the source of the Graph and its vertices
   * @param vertex the vertex at the top of the current subtree
   * @param seen a set of vertices that were already counted
   * @return the overall height of the subtree rooted at the passed vertex
   */
  protected int calculateHeight(LayoutModel<V> layoutModel, V vertex, Set<V> seen) {
    if (seen.add(vertex)) {
      Graph<V, E> graph = layoutModel.getGraph();

      int height =
          graph
              .outgoingEdgesOf(vertex)
              .stream()
              .sorted(edgeComparator)
              // skip over any edge that is not in the edgePredicate but also has a target with an
              // incoming edge that is in the edgePredicate
              .filter(
                  e ->
                      edgePredicate.test(e)
                          || graph
                              .incomingEdgesOf(graph.getEdgeTarget(e))
                              .stream()
                              .noneMatch(edgePredicate))
              .map(graph::getEdgeTarget)
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
   * After the tree is configured, visit all of the vertices that are on favored edges and adjust
   * their position to the left side of their children's bounding box. This helps provide a more
   * linear path for favored edge endpoints
   *
   * @param layoutModel the source of the graph and its vertices
   * @return the Set of root vertices
   */
  protected Set<V> afterBuildTree(LayoutModel<V> layoutModel) {
    Set<V> roots = super.buildTree(layoutModel);
    Graph<V, E> graph = layoutModel.getGraph();
    // move all the predicated vertices or vertices with adjacent predicated edges
    for (V vertex : layoutModel.getGraph().vertexSet()) {
      if (vertexPredicate.test(vertex)
          || graph.outgoingEdgesOf(vertex).stream().anyMatch(edgePredicate)
          || graph.incomingEdgesOf(vertex).stream().anyMatch(edgePredicate)) {
        Rectangle vertexRectangle = baseBounds.getOrDefault(vertex, Rectangle.IDENTITY);
        layoutModel.set(vertex, vertexRectangle.x, vertexRectangle.y);
      }
    }
    return roots;
  }
}
