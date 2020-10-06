/*
 * Created on Mar 3, 2007
 *
 * Copyright (c) 2007, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeUtils {

  private static Logger log = LoggerFactory.getLogger(TreeUtils.class);

  public static <V> Set<V> roots(Graph<V, ?> graph) {
    Objects.requireNonNull(graph, "graph");
    return graph
        .vertexSet()
        .stream()
        .filter(vertex -> graph.incomingEdgesOf(vertex).isEmpty())
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /**
   * A graph is "forest-shaped" if it is directed, acyclic, and each vertex has at most one
   * predecessor.
   */
  public static <V> boolean isForestShaped(Graph<V, ?> graph) {
    Objects.requireNonNull(graph, "graph");
    return graph.getType().isDirected()
        && !graph.getType().isAllowingCycles()
        && graph.vertexSet().stream().allMatch(vertex -> graph.incomingEdgesOf(vertex).size() <= 1);
  }

  /**
   * Returns a copy of the subtree of <code>tree</code> which is rooted at <code>root</code>.
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param tree the tree whose subtree is to be extracted
   * @param root the root of the subtree to be extracted
   */
  public static <V, E> Graph<V, E> getSubTree(Graph<V, E> tree, V root) {
    Objects.requireNonNull(tree, "tree");
    Objects.requireNonNull(root, "root");
    Objects.requireNonNull(
        tree.vertexSet().contains(root), "Input tree does not contain the input subtree root");
    // subtree must allow parallel and loop edges
    DirectedPseudograph<V, E> subtree =
        (DirectedPseudograph)
            GraphTypeBuilder.<V, E>forGraphType(DefaultGraphType.directedPseudograph())
                .buildGraph();
    growSubTree(tree, subtree, root);

    return subtree;
  }

  /**
   * Populates <code>subtree</code> with the subtree of <code>tree</code> which is rooted at <code>
   * root</code>.
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param tree the tree whose subtree is to be extracted
   * @param subTree the tree instance which is to be populated with the subtree of <code>tree</code>
   * @param root the root of the subtree to be extracted
   */
  public static <V, E> void growSubTree(Graph<V, E> tree, Graph<V, E> subTree, V root) {
    Objects.requireNonNull(tree, "tree");
    Objects.requireNonNull(subTree, "subTree");
    Objects.requireNonNull(root, "root");
    for (E edge : tree.outgoingEdgesOf(root)) {
      V kid = tree.getEdgeTarget(edge);
      subTree.addVertex(root);
      subTree.addVertex(kid);
      subTree.addEdge(root, kid, edge);
      growSubTree(tree, subTree, kid);
    }
  }

  /**
   * Connects {@code subTree} to {@code tree} by attaching it as a child of {@code subTreeParent}
   * with edge {@code connectingEdge}.
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param tree the tree to which {@code subTree} is to be added
   * @param subTree the tree which is to be grafted on to {@code tree}
   * @param subTreeParent the parent of the root of {@code subTree} in its new position in {@code
   *     tree}
   * @param connectingEdge the edge used to connect {@code subTreeParent} to {@code subtree}'s root
   */
  public static <V, E> void addSubTree(
      Graph<V, E> tree, Graph<V, E> subTree, V subTreeParent, E connectingEdge) {
    Objects.requireNonNull(tree, "tree");
    Objects.requireNonNull(subTree, "subTree");
    Objects.requireNonNull(subTreeParent, "subTreeParent");
    Objects.requireNonNull(connectingEdge, "connectingEdge");
    Objects.requireNonNull(
        tree.vertexSet().contains(subTreeParent), "'tree' does not contain 'subTreeParent'");

    Set<V> roots = TreeUtils.roots(subTree);
    log.trace("ast roots of {} is {}", subTree, roots);
    if (roots.isEmpty()) {
      // empty subtree; nothing to do
      return;
    }

    for (V subTreeRoot : roots) {
      log.trace("ast add {} to \n{}", subTreeParent, tree);
      tree.addVertex(subTreeParent);
      log.trace("ast add {} to \n{}", subTreeRoot, tree);
      tree.addVertex(subTreeRoot);
      log.trace("ast addEdge {} {} {} to \n{}", subTreeParent, subTreeRoot, connectingEdge, tree);
      tree.addEdge(subTreeParent, subTreeRoot, connectingEdge);
      addFromSubTree(tree, subTree, subTreeRoot);
    }
  }

  private static <V, E> void addFromSubTree(Graph<V, E> tree, Graph<V, E> subTree, V subTreeRoot) {
    Objects.requireNonNull(tree, "tree");
    Objects.requireNonNull(subTree, "subTree");
    Objects.requireNonNull(subTreeRoot, "subTreeRoot");
    for (E edge : subTree.outgoingEdgesOf(subTreeRoot)) {
      V child = subTree.getEdgeTarget(edge);
      log.trace("addVertex {} to \n{}", subTreeRoot, tree);
      tree.addVertex(subTreeRoot);
      log.trace("addVertex {} to \n{}", child, tree);
      tree.addVertex(child);
      log.trace("addEdge {} {} {} \n to {}", subTreeRoot, child, edge, tree);
      tree.addEdge(subTreeRoot, child, edge);
      addFromSubTree(tree, subTree, child);
    }
  }

  /**
   * removes a vertex and all descendants from a tree
   *
   * @param tree the tree to mutate
   * @param subRoot the vertex to remove
   * @param <V> the vertex type
   * @param <E> the edge type
   */
  public static <V, E> void removeTreeVertex(Graph<V, E> tree, V subRoot) {
    Iterator<V> iterator = new BreadthFirstIterator<>(tree, subRoot);
    // remove all the children
    while (iterator.hasNext()) {
      V loser = iterator.next();
      tree.removeVertex(loser);
    }
    // remove the subRoot vertex
    tree.removeVertex(subRoot);
  }
}
