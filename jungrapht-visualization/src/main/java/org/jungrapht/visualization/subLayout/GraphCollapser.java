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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will collapse a selected collection of vertices into one vertex. Uses the vertexSupplier to
 * create the vertex to replace the ones selected.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class GraphCollapser<V, E> implements Collapser<V, E> {

  private static final Logger log = LoggerFactory.getLogger(GraphCollapser.class);

  protected Graph<V, E> graph;
  protected Graph<V, E> originalGraph;
  protected final Supplier<V> vertexSupplier;
  protected GraphTypeBuilder<V, E> graphTypeBuilder;

  protected final Map<V, Graph<V, E>> vertexToClusterMap = new HashMap<>();

  /**
   * create an istance with a {@code Graph} and a {@code Supplier&lt;V&gt;}
   *
   * @param graph the {@code Graph} to operate on.
   * @param vertexSupplier supplies a new vertex to use in place of the collapsed vertices
   */
  public GraphCollapser(Graph<V, E> graph, Supplier<V> vertexSupplier) {
    this.graph = graph;
    this.vertexSupplier = vertexSupplier;
    setGraph(graph);
  }

  /**
   * set the {@code Graph} to a new value
   *
   * @param graph the {@code Graph} to set
   */
  protected void setGraph(Graph<V, E> graph) {
    this.graph = graph;
    this.graphTypeBuilder =
        GraphTypeBuilder.<V, E>forGraphType(graph.getType())
            .allowingMultipleEdges(true)
            .allowingSelfLoops(true);
    this.originalGraph = copyGraph(graph);
  }

  /**
   * Create a copy of the passed graph using the GraphTypeBuilder Used to save a copy of the
   * original graph prior to any collapse/expand actions.
   *
   * @param graph to copy
   * @return a shallow copy of the graph
   */
  protected Graph<V, E> copyGraph(Graph<V, E> graph) {
    Graph<V, E> originalGraphCopy = graphTypeBuilder.buildGraph();
    graph.vertexSet().forEach(originalGraphCopy::addVertex);
    graph
        .edgeSet()
        .forEach(e -> originalGraphCopy.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e), e));
    return originalGraphCopy;
  }

  /** accesses the {@code Map} of vertex to {@code Graph} as a {@code Function} */
  public Function<V, Graph<V, E>> collapsedGraphFunction() {
    return vertexToClusterMap::get;
  }

  /**
   * collapse the passed vertices into one
   *
   * @param selected the vertices to be part of the collapsed vertex
   * @return the new vertex that replaces the selected vertices
   */
  @Override
  public V collapse(Collection<V> selected) {
    return collapse(this.getClusterGraph(selected));
  }

  /**
   * Collapse the passed {@code Graph}
   *
   * @param clusterGraph the {@code Graph} to collapse
   * @return the new vertex that replaces the clusterGraph
   */
  public V collapse(Graph<V, E> clusterGraph) {
    // for a cluster of size < 2, do nothing
    if (clusterGraph.vertexSet().size() < 2) {
      return null;
    }

    // create a new vertex for the cluster
    V clusterVertex = vertexSupplier.get();
    vertexToClusterMap.put(clusterVertex, clusterGraph);

    // loser vertices
    Collection<V> clusterVertices = new HashSet<>(clusterGraph.vertexSet());
    // loser edges
    Collection<E> clusterEdges = new HashSet<>(clusterGraph.edgeSet());

    // remove all edges that are in the cluster
    clusterEdges.stream().forEach(graph::removeEdge);
    // remove all vertices that are in the cluster.
    clusterVertices.stream().forEach(graph::removeVertex);
    // add the clusterGraph as a vertex
    graph.addVertex(clusterVertex);

    // the graph has lost all the clusterEdges, and also any edges that have
    // an endpoint that is in the clusterVertex
    Collection<E> edgesToRestore =
        originalGraph
            .edgeSet()
            .stream()
            .filter(e -> !edgeInCluster(graph, e))
            .filter(e -> !clusterEdges.contains(e))
            .filter(e -> !graph.edgeSet().contains(e))
            .collect(Collectors.toSet());

    if (log.isTraceEnabled()) {
      log.trace("originalGraph: {}", originalGraph);
      log.trace("edgesToRestore: {}", edgesToRestore);
      log.trace(
          "edges not to restore: {}",
          originalGraph
              .edgeSet()
              .stream()
              .filter(clusterEdges::contains)
              .filter(graph.edgeSet()::contains)
              .collect(Collectors.toSet()));
    }
    // for each of the edges to restore, one endpoint is in the graph
    // add every edge with the other endpoint on the cluster vertex
    for (E e : edgesToRestore) {
      V source = originalGraph.getEdgeSource(e);
      V target = originalGraph.getEdgeTarget(e);

      if (graph.vertexSet().contains(source)) {
        if (!graph.vertexSet().contains(target)) {
          target = clusterVertex;
          graph.addEdge(source, target, e);
        } else {
          for (V v : graph.vertexSet()) {
            if (vertexToClusterMap.containsKey(v)) {
              Graph<V, E> clusterGrp = vertexToClusterMap.get(v);
              if (isVertexContainedInClusterGraph(clusterGrp, target)) {
                target = v;
                graph.addEdge(source, target, e);
              }
            }
          }
        }
      } else if (graph.vertexSet().contains(target)) {
        if (!graph.vertexSet().contains(source)) {
          source = clusterVertex;
          graph.addEdge(source, target, e);
        } else {
          for (V v : graph.vertexSet()) {
            if (vertexToClusterMap.containsKey(v)) {
              Graph<V, E> clusterGrp = vertexToClusterMap.get(v);
              if (isVertexContainedInClusterGraph(clusterGrp, source)) {
                source = v;
                graph.addEdge(source, target, e);
              }
            }
          }
        }
      } else {
        // source/target not in graph
        // source and target are not in the graph, find their cluster vertices
        boolean foundSource = false;
        boolean foundTarget = false;
        for (V v : graph.vertexSet()) {
          if (vertexToClusterMap.containsKey(v)) {
            clusterGraph = vertexToClusterMap.get(v);
            if (isVertexContainedInClusterGraph(clusterGraph, source)) {
              source = v;
              foundSource = true;
            }
            if (isVertexContainedInClusterGraph(clusterGraph, target)) {
              target = v;
              foundTarget = true;
            }
          }
        }
        if (foundSource && foundTarget) {
          graph.addEdge(source, target, e);
        }
      }
    }
    return clusterVertex;
  }

  @Override
  public void expand(V clusterVertex) {
    expand(Collections.singleton(clusterVertex));
  }

  @Override
  public void expand(Collection<V> clusterVertices) {

    for (V clusterVertex : clusterVertices) {
      if (vertexToClusterMap.containsKey(clusterVertex)) {
        Graph<V, E> subGraph = vertexToClusterMap.get(clusterVertex);

        // remove the clusterGraphVertex from the graph,
        // then add the subgraph to the graph
        graph.removeVertex(clusterVertex);
        subGraph.vertexSet().forEach(graph::addVertex);

        subGraph
            .edgeSet()
            .forEach(e -> graph.addEdge(subGraph.getEdgeSource(e), subGraph.getEdgeTarget(e), e));

        // add all edges from the original graph, unless the edge is in a the graph already
        Collection<E> edgesIWant =
            originalGraph
                .edgeSet()
                .stream()
                .filter(e -> !graph.edgeSet().contains(e))
                .filter(e -> !edgeInCluster(graph, e))
                .collect(Collectors.toSet());

        for (E e : edgesIWant) {
          V source = originalGraph.getEdgeSource(e);
          V target = originalGraph.getEdgeTarget(e);

          if (graph.vertexSet().contains(source) && graph.vertexSet().contains(target)) {
            graph.addEdge(source, target, e);
          } else if (graph.vertexSet().contains(source)) {
            // find the target
            V originalTarget = originalGraph.getEdgeTarget(e);
            if (graph.vertexSet().contains(originalTarget)) { // source and target are in the graph
              graph.addEdge(source, originalTarget, e);
            } else {
              // target not in graph
              for (V v : graph.vertexSet()) {
                if (vertexToClusterMap.containsKey(v)) {
                  Graph<V, E> clusterGraph = vertexToClusterMap.get(v);
                  if (isVertexContainedInClusterGraph(clusterGraph, originalTarget)) {
                    graph.addEdge(source, v, e);
                    break;
                  }
                }
              }
            }
          } else if (graph.vertexSet().contains(target)) {
            // find the source
            V originalSource = originalGraph.getEdgeSource(e);
            if (graph.vertexSet().contains(originalSource)) { // source and target are in the graph
              graph.addEdge(source, originalSource, e);
            } else {
              // source not in graph
              for (V v : graph.vertexSet()) {
                if (vertexToClusterMap.containsKey(v)) {
                  Graph<V, E> clusterGraph = vertexToClusterMap.get(v);
                  if (isVertexContainedInClusterGraph(clusterGraph, originalSource)) {
                    graph.addEdge(v, target, e);
                    break;
                  }
                }
              }
            }
          } else {
            // source and target are not in the graph, find their cluster vertices
            boolean foundSource = false;
            boolean foundTarget = false;
            for (V v : graph.vertexSet()) {
              if (vertexToClusterMap.containsKey(v)) {
                Graph<V, E> clusterGraph = vertexToClusterMap.get(v);
                if (isVertexContainedInClusterGraph(clusterGraph, source)) {
                  source = v;
                  foundSource = true;
                }
                if (isVertexContainedInClusterGraph(clusterGraph, target)) {
                  target = v;
                  foundTarget = true;
                }
              }
            }
            if (foundSource && foundTarget) {
              graph.addEdge(source, target, e);
            }
          }
        }
      }
      vertexToClusterMap.remove(clusterVertex);
    }
  }

  /**
   * look for edge in the graph. If a vertex is a cluster vertex, recursively look in that cluster
   * graph
   *
   * @param graph to look in, including any vertices that represent collapsed vertex graphs
   * @param edge to look for
   * @return true if the edge is found in a graph or contained cluster
   */
  private boolean edgeInCluster(Graph<V, E> graph, E edge) {
    if (graph.containsEdge(edge)) {
      return true;
    }
    for (V v : graph.vertexSet()) {
      if (vertexToClusterMap.containsKey(v)) { // this is a cluster vertex
        Graph<V, E> collapsedGraph = vertexToClusterMap.get(v);
        if (edgeInCluster(collapsedGraph, edge)) {
          return true;
        }
      }
    }
    return false;
  }

  private String printGraph(StringBuilder buf, Graph<V, E> graph) {
    buf.append(graph.toString());
    buf.append("\n");
    for (V v : graph.vertexSet()) {
      Graph<V, E> sub = vertexToClusterMap.get(v);
      if (sub != null) {
        printGraph(buf, sub);
      }
    }
    return buf.toString();
  }

  /**
   * Determine if the passed vertex is contained in the passed clusterGraph If the clusterGraph
   * contains vertices that are clusterGraphs, check them.
   *
   * @param clusterGraph
   * @param vertex
   * @return
   */
  private boolean isVertexContainedInClusterGraph(Graph<V, E> clusterGraph, V vertex) {

    if (clusterGraph.containsVertex(vertex)) {
      return true;
    }

    for (V v : clusterGraph.vertexSet()) {
      if (vertexToClusterMap.containsKey(v)) {
        Graph<V, E> subGraph = vertexToClusterMap.get(v);
        if (isVertexContainedInClusterGraph(subGraph, vertex)) {
          return true;
        }
      }
    }
    return false;
  }

  public Graph<V, E> getClusterGraph(Collection<V> picked) {
    Graph<V, E> clusterGraph = graphTypeBuilder.buildGraph();
    for (V vertex : picked) {
      clusterGraph.addVertex(vertex);
      Set<E> edges = graph.edgesOf(vertex);
      for (E edge : edges) {
        V u = graph.getEdgeSource(edge);
        V v = graph.getEdgeTarget(edge);
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
