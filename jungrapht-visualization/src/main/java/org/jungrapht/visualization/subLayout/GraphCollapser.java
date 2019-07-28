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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GraphCollapser<E> {

  private static final Logger log = LoggerFactory.getLogger(GraphCollapser.class);

  private static final Logger logger = LoggerFactory.getLogger(GraphCollapser.class);
  private Graph<Collapsable<?>, E> originalGraph;
  private GraphTypeBuilder<Collapsable<?>, E> graphBuilder;

  public GraphCollapser(Graph<Collapsable<?>, E> originalGraph) {
    this.originalGraph = originalGraph;
    this.graphBuilder =
        GraphTypeBuilder.forGraphType(DefaultGraphType.<Collapsable<?>, E>pseudograph());
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
    for (Collapsable<?> v : inGraph.vertexSet()) {
      if (!cluster.contains(v)) {
        graph.addVertex(v);
      }
    }
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
    log.trace("collapsed graph is {}" + graph);
    return graph;
  }

  public Graph<Collapsable<?>, E> expand(
      Graph<Collapsable<?>, E> originalGraph,
      Graph<Collapsable<?>, E> inGraph,
      Collapsable<Graph<Collapsable<?>, E>> clusterGraphVertex) {

    GraphTypeBuilder<Collapsable<?>, E> graphBuilder =
        GraphTypeBuilder.forGraphType(originalGraph.getType());
    // build a new empty graph
    Graph<Collapsable<?>, E> newGraph = graphBuilder.buildGraph();
    // add all the vertices from inGraph that are not clusterGraphVertex and are not in clusterGraphVertex

    for (Collapsable<?> vertex : inGraph.vertexSet()) {
      if (!vertex.equals(clusterGraphVertex) && !this.contains(clusterGraphVertex, vertex)) {
        newGraph.addVertex(vertex);
      }
    }

    // add all edges that don't have an endpoint that either is clusterGraphVertex or is in clusterGraphVertex
    for (E edge : inGraph.edgeSet()) {
      Collapsable<?> fromVertex = inGraph.getEdgeSource(edge);
      Collapsable<?> toVertex = inGraph.getEdgeTarget(edge);
      boolean dontWantThis = false;
      dontWantThis |=
          fromVertex.equals(clusterGraphVertex) || this.contains(clusterGraphVertex, fromVertex);
      dontWantThis |=
          toVertex.equals(clusterGraphVertex) || this.contains(clusterGraphVertex, toVertex);
      if (!dontWantThis) {
        newGraph.addEdge(fromVertex, toVertex, edge);
      }
    }

    // add all the vertices from the clusterGraphVertex
    for (Collapsable<?> vertex : clusterGraphVertex.get().vertexSet()) {
      newGraph.addVertex(vertex);
    }

    // add all the edges that are in the clusterGraphVertex
    for (E edge : clusterGraphVertex.get().edgeSet()) {
      newGraph.addEdge(
          clusterGraphVertex.get().getEdgeSource(edge),
          clusterGraphVertex.get().getEdgeTarget(edge),
          edge);
    }

    // add edges from ingraph where one endpoint is the clusterGraphVertex
    // it will now be connected to the vertex that was expanded from clusterGraphVertex
    for (E edge : inGraph.edgeSet()) {
      Set<Collapsable<?>> endpointsFromCollapsedGraph =
          Sets.newHashSet(inGraph.getEdgeSource(edge), inGraph.getEdgeTarget(edge));

      Set<Collapsable<?>> endpoints =
          Sets.newHashSet(inGraph.getEdgeSource(edge), inGraph.getEdgeTarget(edge));
      for (Collapsable<?> endpoint : endpoints) {

        if (endpoint.equals(clusterGraphVertex)) {
          // get the endpoints for this edge from the original graph
          Set<Collapsable<?>> endpointsFromOriginalGraph =
              Sets.newHashSet(originalGraph.getEdgeSource(edge), originalGraph.getEdgeTarget(edge));
          // remove the endpoint that is the cluster i am expanding
          endpointsFromCollapsedGraph.remove(endpoint);
          // put in the one that is in the collapsedGraphVertex i am expanding
          for (Collapsable<?> originalEndpoint : endpointsFromOriginalGraph) {
            if (this.contains(clusterGraphVertex, originalEndpoint)) {
              endpointsFromCollapsedGraph.add(originalEndpoint);
              break;
            }
          }
          List<Collapsable<?>> list = Lists.newArrayList(endpointsFromCollapsedGraph);
          newGraph.addEdge(list.get(0), list.get(1), edge);
        }
      }
    }
    return newGraph;
  }

  public boolean contains(Collapsable<Graph<Collapsable<?>, E>> inGraph, Collapsable<?> inVertex) {
    log.trace("inGraph is {}", inGraph);
    log.trace("looking for {}", inVertex);
    boolean contained = false;
    log.trace(
        "check inGraph.vertexSet {} contains {} is {}",
        inGraph.get().vertexSet(),
        inVertex,
        inGraph.get().vertexSet().contains(inVertex));

    if (inGraph.get().vertexSet().contains(inVertex)) {
      // inVertex is one of the vertices in inGraph
      return true;
    }

    for (Collapsable<?> vertex : inGraph.get().vertexSet()) {
      log.trace(
          "vertex.get() {} instanceof Graph is {}", vertex.get(), vertex.get() instanceof Graph);
      contained |=
          (vertex.get() instanceof Graph)
              && contains((Collapsable<Graph<Collapsable<?>, E>>) vertex, inVertex);
    }
    return contained;
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
