/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Jul 2, 2003
 *
 */
package org.jungrapht.samples.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;

/** Provides generators for several different test graphs. */
public class TestGraphs {

  /**
   * A series of pairs that may be useful for generating graphs. The miniature graph consists of 8
   * edges, 10 nodes, and is formed of two connected components, one of 8 nodes, the other of 2.
   */
  public static String[][] pairs = {
    {"a", "b", "3"},
    {"a", "c", "4"},
    {"a", "d", "5"},
    {"d", "c", "6"},
    {"d", "e", "7"},
    {"e", "f", "8"},
    {"f", "g", "9"},
    {"h", "i", "1"}
  };

  /**
   * Creates a small sample graph that can be used for testing purposes. The graph is as described
   * in the section on {@link #pairs pairs}.
   *
   * @param directed true iff the graph created is to have directed edges
   * @return a graph consisting of eight edges and ten nodes.
   */
  public static Graph<String, Number> createTestGraph(boolean directed) {
    Graph<String, Number> graph;
    if (directed) {
      graph =
          GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.directedMultigraph())
              .buildGraph();
    } else {
      graph =
          GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.multigraph()).buildGraph();
    }

    for (int i = 0; i < pairs.length; i++) {
      String[] pair = pairs[i];
      graph.addVertex(pair[0]);
      graph.addVertex(pair[1]);
      graph.addEdge(pair[0], pair[1], Integer.parseInt(pair[2]));
    }
    return graph;
  }

  /**
   * @param chain_length the length of the chain of nodes to add to the returned graph
   * @param isolate_count the number of isolated nodes to add to the returned graph
   * @return a graph consisting of a chain of {@code chain_length} nodes and {@code isolate_count}
   *     isolated nodes.
   */
  public static Graph<String, Number> createChainPlusIsolates(int chain_length, int isolate_count) {
    Graph<String, Number> graph =
        GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.multigraph()).buildGraph();

    if (chain_length > 0) {
      String[] v = new String[chain_length];
      v[0] = "v" + 0;
      graph.addVertex(v[0]);
      for (int i = 1; i < chain_length; i++) {
        v[i] = "v" + i;
        graph.addVertex(v[i]);
        graph.addEdge(v[i], v[i - 1], new Double(Math.random()));
      }
    }
    for (int i = 0; i < isolate_count; i++) {
      String v = "v" + (chain_length + i);
      graph.addVertex(v);
    }
    return graph;
  }

  /**
   * Creates a sample directed acyclic graph by generating several "layers", and connecting nodes
   * (randomly) to nodes in earlier (but never later) layers. The number of nodes in each layer is a
   * random value in the range [1, maxNodesPerLayer].
   *
   * @param layers the number of layers of nodes to create in the graph
   * @param maxNodesPerLayer the maximum number of nodes to put in any layer
   * @param linkprob the probability that this method will add an edge from a node in layer <i>k</i>
   *     to a node in layer <i>k+1</i>
   * @return the created graph
   */
  public static Graph<String, Number> createDirectedAcyclicGraph(
      int layers, int maxNodesPerLayer, double linkprob) {

    Graph<String, Number> graph =
        GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.directedMultigraph())
            .buildGraph();

    Set<String> previousLayers = new HashSet<>();
    Set<String> inThisLayer = new HashSet<>();
    for (int i = 0; i < layers; i++) {

      int nodesThisLayer = (int) (Math.random() * maxNodesPerLayer) + 1;
      for (int j = 0; j < nodesThisLayer; j++) {
        String v = i + ":" + j;
        graph.addVertex(v);
        inThisLayer.add(v);
        // for each previous node...
        for (String v2 : previousLayers) {
          if (Math.random() < linkprob) {
            Double de = new Double(Math.random());
            graph.addEdge(v, v2, de);
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
   * one series of edges running from the first node to the last.
   *
   * @return the testgraph
   */
  public static Graph<String, Number> getOneComponentGraph() {

    Graph<String, Number> graph =
        GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.multigraph()).buildGraph();

    // let's throw in a clique, too
    for (int i = 1; i <= 10; i++) {
      for (int j = i + 1; j <= 10; j++) {
        String i1 = "" + i;
        String i2 = "" + j;
        graph.addVertex(i1);
        graph.addVertex(i2);
        graph.addEdge(i1, i2, Math.pow(i + 2, j));
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
        graph.addVertex(i1);
        graph.addVertex(i2);
        graph.addEdge(i1, i2, Math.pow(i + 2, j));
      }
    }
    Iterator<String> nodeIt = graph.vertexSet().iterator();
    String current = nodeIt.next();
    int i = 0;
    while (nodeIt.hasNext()) {
      String next = nodeIt.next();
      graph.addVertex(current);
      graph.addVertex(next);
      graph.addEdge(current, next, new Integer(i++));
    }

    return graph;
  }

  /**
   * Returns a bigger test graph with a clique, several components, and other parts.
   *
   * @return a demonstration graph of type <tt>UndirectedSparseMultiNetwork</tt> with 28 nodes.
   */
  public static Graph<String, Number> getDemoGraph() {
    Graph<String, Number> graph =
        GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.multigraph()).buildGraph();

    for (int i = 0; i < pairs.length; i++) {
      String[] pair = pairs[i];
      graph.addVertex(pair[0]);
      graph.addVertex(pair[1]);
      graph.addEdge(pair[0], pair[1], Integer.parseInt(pair[2]));
      //      createEdge(builder, pair[0], pair[1], Integer.parseInt(pair[2]));
    }

    // let's throw in a clique, too
    for (int i = 1; i <= 10; i++) {
      for (int j = i + 1; j <= 10; j++) {
        String i1 = "c" + i;
        String i2 = "c" + j;
        graph.addVertex(i1);
        graph.addVertex(i2);
        graph.addEdge(i1, i2, Math.pow(i + 2, j));
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
        graph.addVertex(i1);
        graph.addVertex(i2);
        graph.addEdge(i1, i2, Math.pow(i + 2, j));
      }
    }
    return graph;
  }

  /** @return the network for this demo */
  public static Graph<String, Number> getGeneratedNetwork() {

    Graph<String, Number> graph =
        GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.directedPseudograph())
            .vertexSupplier(new NodeSupplier())
            .edgeSupplier(new EdgeSupplier())
            .buildGraph();
    BarabasiAlbertGraphGenerator<String, Number> gen = new BarabasiAlbertGraphGenerator<>(4, 3, 20);
    gen.generateGraph(graph, null);
    return graph;
  }

  static class NodeSupplier implements Supplier<String> {
    char a = 'a';

    public String get() {
      return Character.toString(a++);
    }
  }

  static class EdgeSupplier implements Supplier<Number> {
    int count;

    public Number get() {
      return count++;
    }
  }
}
