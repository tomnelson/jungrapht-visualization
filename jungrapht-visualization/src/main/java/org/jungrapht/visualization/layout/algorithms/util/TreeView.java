package org.jungrapht.visualization.layout.algorithms.util;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeView<V, E> {

  public static class Builder<V, E, T extends TreeView<V, E>, B extends Builder<V, E, T, B>> {
    protected Predicate<V> rootPredicate = v -> true;
    protected Comparator<V> rootComparator = (v1, v2) -> 0;
    protected Predicate<V> vertexPredicate = v -> false;
    protected Predicate<E> edgePredicate = e -> false;
    protected Comparator<V> vertexComparator = (v1, v2) -> 0;
    protected Comparator<E> edgeComparator = (e1, e2) -> 0;

    /** {@inheritDoc} */
    protected B self() {
      return (B) this;
    }

    /** {@inheritDoc} */
    public B rootPredicate(Predicate<V> rootPredicate) {
      this.rootPredicate = rootPredicate;
      return self();
    }

    public B rootComparator(Comparator<V> rootComparator) {
      this.rootComparator = rootComparator;
      return self();
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

    /** {@inheritDoc} */
    public T build() {
      return (T) new TreeView<>(this);
    }
  }
  /**
   * @param <V> vertex type
   * @param <E> edge type
   * @return a Builder ready to configure
   */
  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  private static final Logger log = LoggerFactory.getLogger(TreeView.class);
  protected Predicate<V> rootPredicate;
  protected Comparator<V> rootComparator;
  protected Predicate<V> vertexPredicate;
  protected Predicate<E> edgePredicate;
  protected Comparator<V> vertexComparator;
  protected Comparator<E> edgeComparator;

  TreeView(Builder<V, E, ?, ?> builder) {
    this(
        builder.rootPredicate,
        builder.rootComparator,
        builder.vertexPredicate,
        builder.edgePredicate,
        builder.vertexComparator,
        builder.edgeComparator);
  }

  TreeView(
      Predicate<V> rootPredicate,
      Comparator<V> rootComparator,
      Predicate<V> vertexPredicate,
      Predicate<E> edgePredicate,
      Comparator<V> vertexComparator,
      Comparator<E> edgeComparator) {
    this.rootPredicate = rootPredicate;
    this.rootComparator = rootComparator;
    this.vertexPredicate = vertexPredicate;
    this.edgePredicate = edgePredicate;
    this.vertexComparator = vertexComparator;
    this.edgeComparator = edgeComparator;
  }

  public Graph<V, E> buildTree(Graph<V, E> graph) {

    Graph<V, E> tree = GraphTypeBuilder.<V, E>directed().buildGraph();

    Set<V> seen = new HashSet<>();
    List<V> roots =
        graph
            .vertexSet()
            .stream()
            .filter(rootPredicate)
            .sorted(rootComparator)
            .collect(Collectors.toList());
    // add all the graph roots to the tree. some my have no incident edges
    roots.forEach(tree::addVertex);
    for (V root : roots) {
      buildTree(graph, tree, seen, root);
    }
    return tree;
  }

  private void buildTree(Graph<V, E> graph, Graph<V, E> tree, Set<V> seen, V vertex) {
    if (seen.add(vertex)) {
      for (E edge :
          graph
              .outgoingEdgesOf(vertex)
              .stream()
              .sorted(edgeComparator)
              .collect(Collectors.toCollection(LinkedHashSet::new))) {
        if (edgePredicate.test(edge)
            || graph.incomingEdgesOf(graph.getEdgeTarget(edge)).stream().noneMatch(edgePredicate)) {
          V v = graph.getEdgeTarget(edge);
          if (!rootPredicate.test(v) && !seen.contains(v)) {
            tree.addVertex(vertex);
            tree.addVertex(v);
            tree.addEdge(vertex, v, edge);
            buildTree(graph, tree, seen, v);
          }
        }
      }
    }
  }
}
