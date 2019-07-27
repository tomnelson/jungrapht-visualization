/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package org.jungrapht.visualization.subLayout;

import com.google.common.collect.Iterables;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.util.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class TreeCollapser {

  private static Logger log = LoggerFactory.getLogger(TreeCollapser.class);
  /**
   * Replaces the subtree of {@code tree} rooted at {@code subRoot} with a node representing that
   * subtree.
   *
   * @param tree the tree whose subtree is to be collapsed
   * @param subRoot the root of the subtree to be collapsed
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <E> Graph<Collapsable<?>, E> collapse(
      Graph<Collapsable<?>, E> tree, Collapsable<?> subRoot) {
    // get the subtree rooted at subRoot
    Graph<Collapsable<?>, E> subTree = TreeUtils.getSubTree(tree, subRoot);
    log.trace("subTree of {} is {}", subRoot, subTree);
    if (tree.incomingEdgesOf(subRoot).isEmpty()) {
      TreeUtils.removeTreeNode(tree, subRoot);
      tree.addVertex(Collapsable.of(subTree));
    } else {
      log.trace("collapse at subroot {}", subRoot);
      for (Collapsable<?> parent : Graphs.predecessorListOf(tree, subRoot)) {
        // subRoot has a parent, so attach its parent to subTree in its place
        E parentEdge =
            Iterables.getOnlyElement(tree.incomingEdgesOf(subRoot)); // THERE CAN BE ONLY ONE

        TreeUtils.removeTreeNode(tree, subRoot);

        tree.addVertex(parent);
        tree.addVertex(Collapsable.of(subTree));
        tree.addEdge(parent, Collapsable.of(subTree), parentEdge);
      }
    }
    log.trace("made this subtree {}", subTree); // correct
    return subTree;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <E> Graph<Collapsable<?>, E> expand(
      Graph<Collapsable<?>, E> tree, Collapsable<Graph> subTree) {

    Set<E> incomingEdges = tree.incomingEdgesOf(subTree);
    log.trace("incoming edges are {}", incomingEdges);

    if (incomingEdges.isEmpty()) {
      return subTree.get();
    }
    if (TreeUtils.roots(subTree.get()).isEmpty()) {
      return tree;
    }
    E parentEdge = incomingEdges.iterator().next();
    Collapsable<?> parent = tree.getEdgeSource(parentEdge);
    tree.removeVertex(subTree);
    log.trace("parentEdge {}", parentEdge);
    log.trace("tree contains edge {} is {}", parentEdge, tree.containsEdge(parentEdge));
    if (parent != null) {
      TreeUtils.addSubTree(tree, subTree.get(), parent, parentEdge);
      return tree;
    } else {
      return subTree.get();
    }
  }
}
