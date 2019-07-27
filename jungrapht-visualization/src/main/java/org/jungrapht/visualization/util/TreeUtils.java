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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.collect.ImmutableSet;
import java.util.Iterator;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeUtils {

  private static Logger log = LoggerFactory.getLogger(TreeUtils.class);

  public static <N> ImmutableSet<N> roots(Graph<N, ?> graph) {
    checkNotNull(graph, "graph");
    return graph
        .vertexSet()
        .stream()
        .filter(node -> graph.incomingEdgesOf(node).isEmpty())
        .collect(toImmutableSet());
  }

  /**
   * A graph is "forest-shaped" if it is directed, acyclic, and each node has at most one
   * predecessor.
   */
  public static <N> boolean isForestShaped(Graph<N, ?> graph) {
    checkNotNull(graph, "graph");
    return graph.getType().isDirected()
        && !graph.getType().isAllowingCycles()
        && graph.vertexSet().stream().allMatch(node -> graph.incomingEdgesOf(node).size() <= 1);
  }

  /**
   * Returns a copy of the subtree of <code>tree</code> which is rooted at <code>root</code>.
   *
   * @param <N> the node type
   * @param <E> the edge type
   * @param tree the tree whose subtree is to be extracted
   * @param root the root of the subtree to be extracted
   */
  public static <N, E> Graph<N, E> getSubTree(Graph<N, E> tree, N root) {
    checkNotNull(tree, "tree");
    checkNotNull(root, "root");
    checkArgument(
        tree.vertexSet().contains(root), "Input tree does not contain the input subtree root");
    // subtree must allow parallel and loop edges
    DirectedPseudograph<N, E> subtree =
        (DirectedPseudograph)
            GraphTypeBuilder.<N, E>forGraphType(DefaultGraphType.directedPseudograph())
                .buildGraph();
    growSubTree(tree, subtree, root);

    return subtree;
  }

  /**
   * Populates <code>subtree</code> with the subtree of <code>tree</code> which is rooted at <code>
   * root</code>.
   *
   * @param <N> the node type
   * @param <E> the edge type
   * @param tree the tree whose subtree is to be extracted
   * @param subTree the tree instance which is to be populated with the subtree of <code>tree</code>
   * @param root the root of the subtree to be extracted
   */
  public static <N, E> void growSubTree(
      Graph<N, E> tree, DirectedPseudograph<N, E> subTree, N root) {
    checkNotNull(tree, "tree");
    checkNotNull(subTree, "subTree");
    checkNotNull(root, "root");
    for (E edge : tree.outgoingEdgesOf(root)) {
      N kid = tree.getEdgeTarget(edge);
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
   * @param <N> the node type
   * @param <E> the edge type
   * @param tree the tree to which {@code subTree} is to be added
   * @param subTree the tree which is to be grafted on to {@code tree}
   * @param subTreeParent the parent of the root of {@code subTree} in its new position in {@code
   *     tree}
   * @param connectingEdge the edge used to connect {@code subTreeParent} to {@code subtree}'s root
   */
  public static <N, E> void addSubTree(
      Graph<N, E> tree, Graph<N, E> subTree, N subTreeParent, E connectingEdge) {
    checkNotNull(tree, "tree");
    checkNotNull(subTree, "subTree");
    checkNotNull(subTreeParent, "subTreeParent");
    checkNotNull(connectingEdge, "connectingEdge");
    checkArgument(
        tree.vertexSet().contains(subTreeParent), "'tree' does not contain 'subTreeParent'");

    Set<N> roots = TreeUtils.roots(subTree);
    log.trace("ast roots of {} is {}", subTree, roots);
    if (roots.isEmpty()) {
      // empty subtree; nothing to do
      return;
    }

    for (N subTreeRoot : roots) {
      log.trace("ast add {} to \n{}", subTreeParent, tree);
      tree.addVertex(subTreeParent);
      log.trace("ast add {} to \n{}", subTreeRoot, tree);
      tree.addVertex(subTreeRoot);
      log.trace("ast addEdge {} {} {} to \n{}", subTreeParent, subTreeRoot, connectingEdge, tree);
      tree.addEdge(subTreeParent, subTreeRoot, connectingEdge);
      addFromSubTree(tree, subTree, subTreeRoot);
    }
  }

  private static <N, E> void addFromSubTree(Graph<N, E> tree, Graph<N, E> subTree, N subTreeRoot) {
    checkNotNull(tree, "tree");
    checkNotNull(subTree, "subTree");
    checkNotNull(subTreeRoot, "subTreeRoot");
    for (E edge : subTree.outgoingEdgesOf(subTreeRoot)) {
      N child = subTree.getEdgeTarget(edge);
      log.trace("addVertex {} to \n{}", subTreeRoot, tree);
      tree.addVertex(subTreeRoot);
      log.trace("addVertex {} to \n{}", child, tree);
      tree.addVertex(child);
      log.trace("addEdge {} {} {} \n to {}", subTreeRoot, child, edge, tree);
      tree.addEdge(subTreeRoot, child, edge);
      addFromSubTree(tree, subTree, child);
    }
  }

  public static <N, E> void removeTreeNode(Graph<N, E> tree, N subRoot) {
    Iterator<N> iterator = new BreadthFirstIterator<>(tree, subRoot);
    // remove all the children
    while (iterator.hasNext()) {
      N loser = iterator.next();
      tree.removeVertex(loser);
    }
    // remove the subRoot node
    tree.removeVertex(subRoot);
  }
}
