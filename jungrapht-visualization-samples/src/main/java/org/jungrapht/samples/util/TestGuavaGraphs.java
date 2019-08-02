/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
/*
 * Created on Aug 2, 2019
 *
 */
package org.jungrapht.samples.util;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** Provides generators for several different test graphs. */
public class TestGuavaGraphs {

  /**
   * A series of pairs that may be useful for generating graphs. The miniature graph consists of 8
   * edges, 10 vertices, and is formed of two connected components, one of 8 vertices, the other of
   * 2.
   */
  public static String[][] pairs = {
    {"a", "b"}, {"a", "c"}, {"a", "d"}, {"d", "c"}, {"d", "e"}, {"e", "f"}, {"f", "g"}, {"h", "i"}
  };

  /**
   * Creates a small sample graph that can be used for testing purposes. The graph is as described
   * in the section on {@link #pairs pairs}.
   *
   * @param directed true iff the graph created is to have directed edges
   * @return a graph consisting of eight edges and ten vertices.
   */
  public static MutableGraph<String> createTestGraph(boolean directed) {
    MutableGraph<String> graph;
    if (directed) {
      graph = GraphBuilder.directed().allowsSelfLoops(true).build();
    } else {
      graph = GraphBuilder.undirected().allowsSelfLoops(true).build();
    }

    for (String[] pair : pairs) {
      graph.putEdge(pair[0], pair[1]);
    }
    return graph;
  }

  /**
   * @param chain_length the length of the chain of vertices to add to the returned graph
   * @param isolate_count the number of isolated vertices to add to the returned graph
   * @return a graph consisting of a chain of {@code chain_length} vertices and {@code
   *     isolate_count} isolated vertices.
   */
  public static MutableGraph<String> createChainPlusIsolates(int chain_length, int isolate_count) {
    MutableGraph<String> graph = GraphBuilder.undirected().allowsSelfLoops(true).build();

    if (chain_length > 0) {
      String[] v = new String[chain_length];
      v[0] = "v" + 0;
      for (int i = 1; i < chain_length; i++) {
        v[i] = "v" + i;
        graph.putEdge(v[i], v[i - 1]);
      }
    }
    for (int i = 0; i < isolate_count; i++) {
      String v = "v" + (chain_length + i);
      graph.addNode(v);
    }
    return graph;
  }

  /**
   * Creates a sample directed acyclic graph by generating several "layers", and connecting vertices
   * (randomly) to vertices in earlier (but never later) layers. The number of vertices in each
   * layer is a random value in the range [1, maxVerticesPerLayer].
   *
   * @param layers the number of layers of vertices to create in the graph
   * @param maxVerticesPerLayer the maximum number of vertices to put in any layer
   * @param linkprob the probability that this method will add an edge from a vertex in layer
   *     <i>k</i> to a vertex in layer <i>k+1</i>
   * @return the created graph
   */
  public static MutableGraph<String> createDirectedAcyclicGraph(
      int layers, int maxVerticesPerLayer, double linkprob) {

    MutableGraph<String> graph = GraphBuilder.directed().build();

    Set<String> previousLayers = new HashSet<>();
    Set<String> inThisLayer = new HashSet<>();
    for (int i = 0; i < layers; i++) {

      int verticesThisLayer = (int) (Math.random() * maxVerticesPerLayer) + 1;
      for (int j = 0; j < verticesThisLayer; j++) {
        String v = i + ":" + j;
        graph.addNode(v);
        inThisLayer.add(v);
        // for each previous vertex...
        for (String v2 : previousLayers) {
          if (Math.random() < linkprob) {
            Double de = Math.random();
            graph.putEdge(v, v2);
          }
        }
      }

      previousLayers.addAll(inThisLayer);
      inThisLayer.clear();
    }
    return graph;
  }

  /**
   * Returns a bigger, undirected test graph with a just one component. This graph consists of a
   * clique of ten edges, a partial clique (randomly generated, with edges of 0.6 probability), and
   * one series of edges running from the first vertex to the last.
   *
   * @return the testgraph
   */
  public static MutableGraph<String> getOneComponentGraph() {

    MutableGraph<String> graph = GraphBuilder.undirected().build();

    // let's throw in a clique, too
    for (int i = 1; i <= 10; i++) {
      for (int j = i + 1; j <= 10; j++) {
        String i1 = "" + i;
        String i2 = "" + j;
        graph.putEdge(i1, i2);
      }
    }

    // and, last, a partial clique
    for (int i = 11; i <= 20; i++) {
      for (int j = i + 1; j <= 20; j++) {
        if (Math.random() > 0.6) {
          continue;
        }
        String i1 = "" + i;
        String i2 = "" + j;
        graph.putEdge(i1, i2);
      }
    }
    Iterator<String> vertexIt = graph.nodes().iterator();
    String current = vertexIt.next();
    while (vertexIt.hasNext()) {
      String next = vertexIt.next();
      graph.putEdge(current, next);
    }

    return graph;
  }

  /**
   * Returns a bigger test graph with a clique, several components, and other parts.
   *
   * @return a demonstration graph of type <tt>UndirectedSparseMultiGraph</tt> with 28 vertices.
   */
  public static MutableGraph<String> getDemoGraph() {
    MutableGraph<String> graph = GraphBuilder.directed().build();

    for (String[] pair : pairs) {
      graph.putEdge(pair[0], pair[1]);
      //      createEdge(builder, pair[0], pair[1], Integer.parseInt(pair[2]));
    }

    // let's throw in a clique, too
    for (int i = 1; i <= 10; i++) {
      for (int j = i + 1; j <= 10; j++) {
        String i1 = "c" + i;
        String i2 = "c" + j;
        graph.putEdge(i1, i2);
      }
    }

    // and, last, a partial clique
    for (int i = 11; i <= 20; i++) {
      for (int j = i + 1; j <= 20; j++) {
        if (Math.random() > 0.6) {
          continue;
        }
        String i1 = "p" + i;
        String i2 = "p" + j;
        graph.putEdge(i1, i2);
      }
    }
    return graph;
  }

  public static MutableGraph<String> createSmallGraph(boolean directed) {
    MutableGraph<String> graph;
    if (directed) {
      graph = GraphBuilder.directed().build();
    } else {
      graph = GraphBuilder.undirected().build();
    }

    graph.putEdge("A", "B");
    graph.putEdge("A", "C");
    graph.putEdge("B", "C");

    return graph;
  }
}
