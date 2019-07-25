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
    this.graphBuilder = GraphTypeBuilder.forGraphType(originalGraph.getType());
  }

  public Graph<Collapsable<?>, E> collapse(
      Graph<Collapsable<?>, E> inGraph, Graph<Collapsable<?>, E> clusterGraph) {

    if (clusterGraph.vertexSet().size() < 2) {
      return inGraph;
    }
    Graph<Collapsable<?>, E> graph = graphBuilder.buildGraph();
    Collection<Collapsable<?>> cluster = clusterGraph.vertexSet();

    // add all nodes in the delegate, unless the node is in the
    // cluster.
    for (Collapsable<?> v : inGraph.vertexSet()) {
      if (!cluster.contains(v)) {
        graph.addVertex(v);
      }
    }
    // add the clusterGraph as a node
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
      Graph<Collapsable<?>, E> originalNetwork,
      Graph<Collapsable<?>, E> inGraph,
      Collapsable<Graph<Collapsable<?>, E>> clusterGraphNode) {

    GraphTypeBuilder<Collapsable<?>, E> graphBuilder =
        GraphTypeBuilder.forGraphType(originalGraph.getType());
    // build a new empty network
    Graph<Collapsable<?>, E> newGraph = graphBuilder.buildGraph();
    // add all the nodes from inGraph that are not clusterGraphNode and are not in clusterGraphNode

    for (Collapsable<?> node : inGraph.vertexSet()) {
      if (!node.equals(clusterGraphNode) && !this.contains(clusterGraphNode, node)) {
        newGraph.addVertex(node);
      }
    }

    // add all edges that don't have an endpoint that either is clusterGraphNode or is in clusterGraphNode
    for (E edge : inGraph.edgeSet()) {
      Collapsable<?> fromNode = inGraph.getEdgeSource(edge);
      Collapsable<?> toNode = inGraph.getEdgeTarget(edge);
      boolean dontWantThis = false;
      dontWantThis |=
          fromNode.equals(clusterGraphNode) || this.contains(clusterGraphNode, fromNode);
      dontWantThis |= toNode.equals(clusterGraphNode) || this.contains(clusterGraphNode, toNode);
      if (!dontWantThis) {
        newGraph.addEdge(fromNode, toNode, edge);
      }
    }

    // add all the nodes from the clusterGraphNode
    for (Collapsable<?> node : clusterGraphNode.get().vertexSet()) {
      newGraph.addVertex(node);
    }

    // add all the edges that are in the clusterGraphNode
    for (E edge : clusterGraphNode.get().edgeSet()) {
      newGraph.addEdge(
          clusterGraphNode.get().getEdgeSource(edge),
          clusterGraphNode.get().getEdgeTarget(edge),
          edge);
    }

    // add edges from ingraph where one endpoint is the clusterGraphNode
    // it will now be connected to the node that was expanded from clusterGraphNode
    for (E edge : inGraph.edgeSet()) {
      Set<Collapsable<?>> endpointsFromCollapsedGraph =
          Sets.newHashSet(inGraph.getEdgeSource(edge), inGraph.getEdgeTarget(edge));

      Set<Collapsable<?>> endpoints =
          Sets.newHashSet(inGraph.getEdgeSource(edge), inGraph.getEdgeTarget(edge));
      for (Collapsable<?> endpoint : endpoints) {

        if (endpoint.equals(clusterGraphNode)) {
          // get the endpoints for this edge from the original graph
          Set<Collapsable<?>> endpointsFromOriginalGraph =
              Sets.newHashSet(
                  originalNetwork.getEdgeSource(edge), originalNetwork.getEdgeTarget(edge));
          // remove the endpoint that is the cluster i am expanding
          endpointsFromCollapsedGraph.remove(endpoint);
          // put in the one that is in the collapsedGraphNode i am expanding
          for (Collapsable<?> originalEndpoint : endpointsFromOriginalGraph) {
            if (this.contains(clusterGraphNode, originalEndpoint)) {
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

  public boolean contains(Collapsable<Graph<Collapsable<?>, E>> inGraph, Collapsable<?> inNode) {
    log.trace("inGraph is {}", inGraph);
    log.trace("looking for {}", inNode);
    boolean contained = false;
    log.trace(
        "check inGraph.vertexSet {} contains {} is {}",
        inGraph.get().vertexSet(),
        inNode,
        inGraph.get().vertexSet().contains(inNode));

    if (inGraph.get().vertexSet().contains(inNode)) {
      // inNode is one of the nodes in inGraph
      return true;
    }

    for (Collapsable<?> node : inGraph.get().vertexSet()) {
      log.trace("node.get() {} instanceof Graph is {}", node.get(), node.get() instanceof Graph);
      contained |=
          (node.get() instanceof Graph)
              && contains((Collapsable<Graph<Collapsable<?>, E>>) node, inNode);
    }
    return contained;
  }

  public Graph<Collapsable<?>, E> getClusterGraph(
      Graph<Collapsable<?>, E> inGraph, Collection<Collapsable<?>> picked) {
    Graph<Collapsable<?>, E> clusterGraph = graphBuilder.buildGraph();
    for (Collapsable<?> node : picked) {
      clusterGraph.addVertex(node);
      Set<E> edges = inGraph.edgesOf(node);
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
