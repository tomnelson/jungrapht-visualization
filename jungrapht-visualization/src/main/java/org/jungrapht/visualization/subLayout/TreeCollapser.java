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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.util.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will collapse and expand a subtree from its root vertex
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class TreeCollapser<V, E> {

  private static Logger log = LoggerFactory.getLogger(TreeCollapser.class);

  Graph<V, E> tree;
  Supplier<V> vertexFactory;
  protected final Map<V, Graph<V, E>> vertexToClusterMap = new HashMap<>();

  public Function<V, Graph<V, E>> collapsedGraphFunction() {
    return v -> vertexToClusterMap.get(v);
  }

  public TreeCollapser(Graph<V, E> graph, Supplier<V> vertexFactory) {
    this.tree = graph;
    this.vertexFactory = vertexFactory;
  }

  public Collection<V> collapse(Collection<V> roots) {
    Set<V> set = roots.stream().filter(r -> tree.outDegreeOf(r) != 0).collect(Collectors.toSet());
    for (V v : roots) {
      set.add(collapse(v));
    }
    return set;
  }

  /**
   * Replaces the subtree of {@code tree} rooted at {@code subRoot} with a vertex representing that
   * subtree.
   *
   * @param subRoot the root of the subtree to be collapsed
   */
  public V collapse(V subRoot) {
    if (tree.containsVertex(subRoot) && tree.outDegreeOf(subRoot) != 0) {
      // get the subtree rooted at subRoot
      Graph<V, E> subTree = TreeUtils.getSubTree(tree, subRoot);
      V collapseVertex = vertexFactory.get();
      vertexToClusterMap.put(collapseVertex, subTree);
      log.trace("subTree of {} is {}", subRoot, subTree);
      if (tree.incomingEdgesOf(subRoot).isEmpty()) {
        TreeUtils.removeTreeVertex(tree, subRoot);
        tree.addVertex(collapseVertex);
      } else {
        log.trace("collapse at subroot {}", subRoot);
        for (V parent : Graphs.predecessorListOf(tree, subRoot)) {
          // subRoot has a parent, so attach its parent to subTree in its place
          E parentEdge = tree.incomingEdgesOf(subRoot).stream().findFirst().get();

          TreeUtils.removeTreeVertex(tree, subRoot);

          tree.addVertex(parent);
          tree.addVertex(collapseVertex);
          tree.addEdge(parent, collapseVertex, parentEdge);
        }
      }
      log.trace("made this subtree {}", subTree); // correct
      return collapseVertex;
    }
    return null;
  }

  public void expand(Collection<V> collapsedRoots) {
    collapsedRoots.forEach(this::expand);
  }

  public void expand(V collapseVertex) {
    Graph<V, E> subTree = vertexToClusterMap.get(collapseVertex);
    if (subTree != null) {
      Set<E> incomingEdges = tree.incomingEdgesOf(collapseVertex);
      log.trace("incoming edges are {}", incomingEdges);

      vertexToClusterMap.remove(collapseVertex);
      if (incomingEdges.isEmpty()) {
        // this is the root of the original tree
        // make its root the root of the tree
        Set<V> roots = TreeUtils.roots(subTree);
        for (V root : roots) {
          TreeUtils.growSubTree(subTree, tree, root);
        }
        tree.removeVertex(collapseVertex);
        return;
      }
      if (TreeUtils.roots(subTree).isEmpty()) {
        return;
      }
      E parentEdge = incomingEdges.stream().findFirst().get();
      V parent = tree.getEdgeSource(parentEdge);
      log.trace("parentEdge {}", parentEdge);
      log.trace("tree contains edge {} is {}", parentEdge, tree.containsEdge(parentEdge));
      if (parent != null) {
        tree.removeVertex(collapseVertex);
        TreeUtils.addSubTree(tree, subTree, parent, parentEdge);
      } else {
        TreeUtils.growSubTree(subTree, tree, parent);
      }
      tree.removeVertex(collapseVertex);
    }
  }

  protected void setGraph(Graph<V, E> graph) {
    this.tree = graph;
  }
}
