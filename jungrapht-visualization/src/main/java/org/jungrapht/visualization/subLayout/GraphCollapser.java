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
public class GraphCollapser {

  private static final Logger logger = LoggerFactory.getLogger(GraphCollapser.class);
  private Graph originalGraph;
  private GraphTypeBuilder graphBuilder;

  public GraphCollapser(Graph originalGraph) {
    this.originalGraph = originalGraph;
    this.graphBuilder = GraphTypeBuilder.forGraphType(originalGraph.getType());
  }

  public Graph collapse(Graph inGraph, Graph clusterGraph) {

    if (clusterGraph.vertexSet().size() < 2) {
      return inGraph;
    }
    Graph graph = graphBuilder.buildGraph();
    Collection cluster = clusterGraph.vertexSet();

    // add all nodes in the delegate, unless the node is in the
    // cluster.
    for (Object v : inGraph.vertexSet()) {
      if (!cluster.contains(v)) {
        graph.addVertex(v);
      }
    }
    // add the clusterGraph as a node
    graph.addVertex(clusterGraph);

    // add all edges from the inGraph, unless both endpoints of
    // the edge are in the cluster
    for (Object e : inGraph.edgeSet()) {

      Object u = inGraph.getEdgeSource(e);
      Object v = inGraph.getEdgeTarget(e);

      // only add edges whose endpoints are not both in the cluster
      if (cluster.contains(u) && cluster.contains(v)) {
        continue;
      }

      if (cluster.contains(u)) {
        graph.addEdge(clusterGraph, v, e);
      } else if (cluster.contains(v)) {
        graph.addEdge(u, clusterGraph, e);
      } else {
        graph.addEdge(u, v, e);
      }
    }
    return graph;
  }

  public Graph expand(Graph originalNetwork, Graph inGraph, Graph clusterGraphNode) {

    GraphTypeBuilder graphBuilder = GraphTypeBuilder.forGraphType(originalGraph.getType());
    // build a new empty network
    Graph newGraph = graphBuilder.buildGraph();
    // add all the nodes from inGraph that are not clusterGraphNode and are not in clusterGraphNode
    for (Object node : inGraph.vertexSet()) {
      if (!node.equals(clusterGraphNode) && !this.contains(clusterGraphNode, node)) {
        newGraph.addVertex(node);
      }
    }

    // add all edges that don't have an endpoint that either is clusterGraphNode or is in clusterGraphNode
    for (Object edge : inGraph.edgeSet()) {
      Object fromNode = inGraph.getEdgeSource(edge);
      Object toNode = inGraph.getEdgeTarget(edge);
      boolean dontWantThis = false;
      dontWantThis |=
          fromNode.equals(clusterGraphNode) || this.contains(clusterGraphNode, fromNode);
      dontWantThis |= toNode.equals(clusterGraphNode) || this.contains(clusterGraphNode, toNode);
      if (!dontWantThis) {
        newGraph.addEdge(fromNode, toNode, edge);
      }
    }

    // add all the nodes from the clusterGraphNode
    for (Object node : clusterGraphNode.vertexSet()) {
      newGraph.addVertex(node);
    }

    // add all the edges that are in the clusterGraphNode
    for (Object edge : clusterGraphNode.edgeSet()) {
      newGraph.addEdge(
          clusterGraphNode.getEdgeSource(edge), clusterGraphNode.getEdgeTarget(edge), edge);
    }

    // add edges from ingraph where one endpoint is the clusterGraphNode
    // it will now be connected to the node that was expanded from clusterGraphNode
    for (Object edge : inGraph.edgeSet()) {
      Set endpointsFromCollapsedGraph =
          Sets.newHashSet(inGraph.getEdgeSource(edge), inGraph.getEdgeTarget(edge));

      Set endpoints = Sets.newHashSet(inGraph.getEdgeSource(edge), inGraph.getEdgeTarget(edge));
      for (Object endpoint : endpoints) {

        if (endpoint.equals(clusterGraphNode)) {
          // get the endpoints for this edge from the original graph
          Set endpointsFromOriginalGraph =
              Sets.newHashSet(
                  originalNetwork.getEdgeSource(edge), originalNetwork.getEdgeTarget(edge));
          // remove the endpoint that is the cluster i am expanding
          endpointsFromCollapsedGraph.remove(endpoint);
          // put in the one that is in the collapsedGraphNode i am expanding
          for (Object originalEndpoint : endpointsFromOriginalGraph) {
            if (this.contains(clusterGraphNode, originalEndpoint)) {
              endpointsFromCollapsedGraph.add(originalEndpoint);
              break;
            }
          }
          List list = Lists.newArrayList(endpointsFromCollapsedGraph);
          newGraph.addEdge(list.get(0), list.get(1), edge);
        }
      }
    }
    return newGraph;
  }

  /**
   * @param inGraph
   * @param inNode
   * @return
   */
  public Object findNode(Graph inGraph, Object inNode) {
    /** if the inNode is in the inGraph, return the inNode */
    if (inGraph.vertexSet().contains(inNode)) {
      return inNode;
    }

    /**
     * if the inNode is part of a node that is a Network, return the Network that contains inNode
     */
    for (Object node : inGraph.vertexSet()) {
      if ((node instanceof Graph) && contains((Graph) node, inNode)) {
        // return the node that is a Network containing inNode
        return node;
      }
    }
    return null;
  }

  Object findOriginalNode(Graph inGraph, Object inNode, Graph clusterGraph) {
    if (inGraph.vertexSet().contains(inNode)) {
      return inNode;
    }

    for (Object node : inGraph.vertexSet()) {
      if ((node instanceof Graph) && !node.equals(clusterGraph)) {
        return node;
      }
      if ((node instanceof Graph) && contains((Graph) node, inNode)) {
        return node;
      }
    }
    return null;
  }

  public boolean contains(Graph inGraph, Object inNode) {
    boolean contained = false;
    if (inGraph.vertexSet().contains(inNode)) {
      // inNode is one of the nodes in inGraph
      return true;
    }

    for (Object node : inGraph.vertexSet()) {
      contained |= (node instanceof Graph) && contains((Graph) node, inNode);
    }
    return contained;
  }

  public Graph getClusterGraph(Graph inGraph, Collection picked) {
    Graph clusterGraph = graphBuilder.buildGraph();
    for (Object node : picked) {
      clusterGraph.addVertex(node);
      Set edges = inGraph.edgesOf(node);
      for (Object edge : edges) {
        Object u = inGraph.getEdgeSource(edge);
        Object v = inGraph.getEdgeTarget(edge);
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
