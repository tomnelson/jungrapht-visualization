/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package org.jungrapht.visualization.subLayout;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphCollapser<E> {

  private static final Logger log = LoggerFactory.getLogger(GraphCollapser.class);

  private static final Logger logger = LoggerFactory.getLogger(GraphCollapser.class);
  private Graph<Collapsable<?>, E> originalGraph;
  private GraphTypeBuilder<Collapsable<?>, E> graphBuilder;

  public GraphCollapser(Graph<Collapsable<?>, E> originalGraph) {
    this.originalGraph = originalGraph;
    this.graphBuilder = GraphTypeBuilder.forGraphType(DefaultGraphType.pseudograph());
  }

  /**
   * make a pseudograph with Collapsable vertex types the graph has to allow self loops and parallel
   * edges in order to be collapsed and expanded without losing edges
   *
   * @param graph input graph
   * @param <V> vertex type
   * @param <E> edge type
   * @return a collapsable verson of the input graph
   */
  public static <V, E> Graph<Collapsable<?>, E> makeCollapsableGraph(Graph<V, E> graph) {
    Graph<Collapsable<?>, E> collapsableGraph =
        GraphTypeBuilder.<Collapsable<?>, E>forGraphType(graph.getType())
            .allowingMultipleEdges(true)
            .allowingSelfLoops(true)
            .buildGraph();
    // add vertices and edges to the new graph
    for (E edge : graph.edgeSet()) {
      Collapsable<?> source = Collapsable.of(graph.getEdgeSource(edge));
      Collapsable<?> target = Collapsable.of(graph.getEdgeTarget(edge));
      collapsableGraph.addVertex(source);
      collapsableGraph.addVertex(target);
      collapsableGraph.addEdge(source, target, edge);
    }
    return collapsableGraph;
  }

  public Graph<Collapsable<?>, E> collapse(
      Graph<Collapsable<?>, E> inGraph, Graph<Collapsable<?>, E> clusterGraph) {

    if (clusterGraph.vertexSet().size() < 2) {
      return inGraph;
    }
    Graph<Collapsable<?>, E> graph = graphBuilder.buildGraph();
    Collection<Collapsable<?>> cluster = clusterGraph.vertexSet();

    // add all vertices in the delegate, unless the vertex is in the
    // cluster.
    inGraph.vertexSet().stream().filter(v -> !cluster.contains(v)).forEach(graph::addVertex);
    // add the clusterGraph as a vertex
    graph.addVertex(Collapsable.of(clusterGraph));

    // add all edges from the inGraph, unless both endpoints of
    // the edge are in the cluster
    log.trace("edgeSet {}", inGraph.edgeSet());
    for (E e : inGraph.edgeSet()) {

      Collapsable<?> u = inGraph.getEdgeSource(e);
      Collapsable<?> v = inGraph.getEdgeTarget(e);

      // only add edges whose endpoints are not both in the cluster
      if (cluster.contains(u) && cluster.contains(v)) {
        log.trace("leaving out {} from {} to {}", e, u, v);
        continue;
      }

      if (cluster.contains(u)) {
        graph.addEdge(Collapsable.of(clusterGraph), v, e);
      } else if (cluster.contains(v)) {
        graph.addEdge(u, Collapsable.of(clusterGraph), e);
      } else {
        graph.addEdge(u, v, e);
      }
    }
    log.trace("collapsed graph is {}", graph);
    return graph;
  }

  public Graph<Collapsable<?>, E> expand(
      Graph<Collapsable<?>, E> originalGraph,
      Graph<Collapsable<?>, E> graphToExpand,
      Collapsable<Graph<Collapsable<?>, E>> clusterGraphVertex) {

    GraphTypeBuilder<Collapsable<?>, E> graphBuilder =
        GraphTypeBuilder.forGraphType(originalGraph.getType());
    // build a new empty graph
    Graph<Collapsable<?>, E> newGraph = graphBuilder.buildGraph();

    // add all vertices of graphToExpand to the new graph, excepting the clusterGraphVertex
    graphToExpand
        .vertexSet()
        .stream()
        .filter(v -> !Objects.equals(v, clusterGraphVertex))
        .forEach(newGraph::addVertex);
    log.trace("newGraph: {}", newGraph);
    // add all the vertices that were in the clusterGraphVertex
    clusterGraphVertex.get().vertexSet().forEach(newGraph::addVertex);
    log.trace("newGraph: {}", newGraph);

    // all required vertices should now be in the newGraph
    for (E edge : originalGraph.edgeSet()) {
      // if this edge already exists in the depths of newGraph, then it must be wholly contained
      // inside a collapsed graph vertex. Do not add it to newGraph
      if (null != findEdge(newGraph, edge)) {
        continue;
      }
      // get the original graph endpoints for edge
      Collapsable<?> originalSource = originalGraph.getEdgeSource(edge);
      Collapsable<?> originalTarget = originalGraph.getEdgeTarget(edge);
      // if both endpoints are in newGraph, just add the edge with the same endpoints
      if (newGraph.containsVertex(originalSource) && newGraph.containsVertex(originalTarget)) {
        newGraph.addEdge(originalSource, originalTarget, edge);
        continue;
      }
      // either the originalSource or the originalTarget is inside a collapsed graph vertex in newGraph.
      // Find them and add this edge with one or both endpoints as collapsed graph vertices in newGraph
      Collapsable<?> foundSource = findContainerOf(newGraph, originalSource);
      Collapsable<?> foundTarget = findContainerOf(newGraph, originalTarget);
      newGraph.addEdge(foundSource, foundTarget, edge);
    }
    return newGraph;
  }

  private E findEdge(Graph<Collapsable<?>, E> graph, E edge) {
    if (graph.edgeSet().contains(edge)) {
      return edge;
    }
    for (Collapsable<?> vertex : graph.vertexSet()) {
      if (vertex.get() instanceof Graph) {
        E found = findEdge((Graph<Collapsable<?>, E>) vertex.get(), edge);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  /**
   * Look in the vertexSet of the passed graph. Find either the passed vertex, or if any vertices
   * are {@code Collapsable<Graph>} instances, look inside to see if it includes the passed vertex.
   * When a {@code Collapsable<Graph>} contains the vertex, return that {@code Collapsable<Graph>}
   *
   * @param graph the graph to seach in
   * @param vertex the vertex to search for
   * @return either the vertex itself if in the passed graph, or the outermost Graph vertex that
   *     contains vertex
   */
  private Collapsable<?> findContainerOf(Graph<Collapsable<?>, E> graph, Collapsable<?> vertex) {
    if (graph.vertexSet().contains(vertex)) {
      return vertex;
    }
    log.trace("look in {} for {}", graph.vertexSet(), vertex);
    for (Collapsable<?> v : graph.vertexSet()) {
      if (v.get() instanceof Graph) {
        Collapsable<Graph<Collapsable<?>, E>> collapsedGraph =
            (Collapsable<Graph<Collapsable<?>, E>>) v;
        Collapsable<?> found = findContainerOf(collapsedGraph.get(), vertex);
        if (found != null) {
          return collapsedGraph;
        }
      }
    }
    return null;
  }

  public Graph<Collapsable<?>, E> getClusterGraph(
      Graph<Collapsable<?>, E> inGraph, Collection<Collapsable<?>> picked) {
    Graph<Collapsable<?>, E> clusterGraph = graphBuilder.buildGraph();
    for (Collapsable<?> vertex : picked) {
      clusterGraph.addVertex(vertex);
      Set<E> edges = inGraph.edgesOf(vertex);
      for (E edge : edges) {
        Collapsable<?> u = inGraph.getEdgeSource(edge);
        Collapsable<?> v = inGraph.getEdgeTarget(edge);
        if (picked.contains(u) && picked.contains(v)) {
          clusterGraph.addVertex(u);
          clusterGraph.addVertex(v);
          clusterGraph.addEdge(u, v, edge);
        }
      }
    }
    return clusterGraph;
  }
}
