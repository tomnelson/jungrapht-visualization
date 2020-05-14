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

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.graph.guava.MutableNetworkAdapter;
import org.jgrapht.util.SupplierUtil;

/** Provides generators for several different test graphs. */
public class TestGuavaNetworks {

  /**
   * A series of pairs that may be useful for generating graphs. The miniature graph consists of 8
   * edges, 10 vertices, and is formed of two connected components, one of 8 vertices, the other of
   * 2.
   */
  public static String[][] pairs = {
    {"a", "b"}, {"a", "c"}, {"a", "d"}, {"d", "c"}, {"d", "e"}, {"e", "f"}, {"f", "g"}, {"h", "i"}
  };

  /**
   * Creates a small sample network that can be used for testing purposes. The network is as
   * described in the section on {@link #pairs pairs}.
   *
   * @param directed true iff the network created is to have directed edges
   * @return a network consisting of eight edges and ten vertices.
   */
  public static MutableNetwork<String, Integer> createTestNetwork(boolean directed) {
    MutableNetwork<String, Integer> network;
    if (directed) {
      network = NetworkBuilder.directed().allowsSelfLoops(true).build();
    } else {
      network = NetworkBuilder.undirected().allowsSelfLoops(true).build();
    }
    int i = 0;
    for (String[] pair : pairs) {
      network.addEdge(pair[0], pair[1], i++);
    }
    return network;
  }

  /**
   * @param chain_length the length of the chain of vertices to add to the returned graph
   * @param isolate_count the number of isolated vertices to add to the returned graph
   * @return a network consisting of a chain of {@code chain_length} vertices and {@code
   *     isolate_count} isolated vertices.
   */
  public static MutableNetwork<String, Integer> createChainPlusIsolates(
      int chain_length, int isolate_count) {
    MutableNetwork<String, Integer> network =
        NetworkBuilder.undirected().allowsSelfLoops(true).build();

    if (chain_length > 0) {
      String[] v = new String[chain_length];
      v[0] = "v" + 0;
      for (int i = 1; i < chain_length; i++) {
        v[i] = "v" + i;
        network.addEdge(v[i], v[i - 1], i);
      }
    }
    for (int i = 0; i < isolate_count; i++) {
      String v = "v" + (chain_length + i);
      network.addNode(v);
    }
    return network;
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
  public static MutableNetwork<String, Integer> createDirectedAcyclicNetwork(
      int layers, int maxVerticesPerLayer, double linkprob) {

    MutableNetwork<String, Integer> network = NetworkBuilder.directed().build();

    Set<String> previousLayers = new HashSet<>();
    Set<String> inThisLayer = new HashSet<>();
    int edge = 0;
    for (int i = 0; i < layers; i++) {

      int verticesThisLayer = (int) (Math.random() * maxVerticesPerLayer) + 1;
      for (int j = 0; j < verticesThisLayer; j++) {
        String v = i + ":" + j;
        network.addNode(v);
        inThisLayer.add(v);
        // for each previous vertex...
        for (String v2 : previousLayers) {
          if (Math.random() < linkprob) {
            Double de = Math.random();
            network.addEdge(v, v2, edge++);
          }
        }
      }

      previousLayers.addAll(inThisLayer);
      inThisLayer.clear();
    }
    return network;
  }

  /**
   * Returns a bigger, undirected test network with a just one component. This network consists of a
   * clique of ten edges, a partial clique (randomly generated, with edges of 0.6 probability), and
   * one series of edges running from the first vertex to the last.
   *
   * @return the testgraph
   */
  public static MutableNetwork<String, Integer> getOneComponentNetwork() {

    MutableNetwork<String, Integer> network =
        NetworkBuilder.undirected().allowsParallelEdges(true).build();

    // let's throw in a clique, too
    int edge = 0;
    for (int i = 1; i <= 10; i++) {
      for (int j = i + 1; j <= 10; j++) {
        String i1 = "" + i;
        String i2 = "" + j;
        network.addEdge(i1, i2, edge++);
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
        network.addEdge(i1, i2, edge++);
      }
    }
    Iterator<String> vertexIt = network.nodes().iterator();
    String current = vertexIt.next();
    while (vertexIt.hasNext()) {
      String next = vertexIt.next();
      network.addEdge(current, next, edge++);
    }

    return network;
  }

  /**
   * Returns a bigger test graph with a clique, several components, and other parts.
   *
   * @return a demonstration graph of type <tt>UndirectedSparseMultiGraph</tt> with 28 vertices.
   */
  public static MutableNetwork<String, Integer> getDemoNetwork() {
    MutableNetwork<String, Integer> graph = NetworkBuilder.directed().build();
    int edge = 0;
    for (String[] pair : pairs) {
      graph.addEdge(pair[0], pair[1], edge++);
    }

    // let's throw in a clique, too
    for (int i = 1; i <= 10; i++) {
      for (int j = i + 1; j <= 10; j++) {
        String i1 = "c" + i;
        String i2 = "c" + j;
        graph.addEdge(i1, i2, edge++);
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
        graph.addEdge(i1, i2, edge++);
      }
    }
    return graph;
  }

  public static MutableNetwork<String, Integer> createSmallNetwork(boolean directed) {
    MutableNetwork<String, Integer> network;
    if (directed) {
      network = NetworkBuilder.directed().build();
    } else {
      network = NetworkBuilder.undirected().build();
    }
    int edge = 0;
    network.addEdge("A", "B", edge++);
    network.addEdge("A", "C", edge++);
    network.addEdge("B", "C", edge++);

    return network;
  }

  /** @return the graph for this demo */
  public static MutableNetwork<String, Integer> getGeneratedNetwork() {

    MutableNetwork<String, Integer> network = NetworkBuilder.directed().build();
    MutableNetworkAdapter<String, Integer> adapter = new MutableNetworkAdapter<>(network);
    adapter.setVertexSupplier(new VertexSupplier());
    adapter.setEdgeSupplier(SupplierUtil.createIntegerSupplier());
    BarabasiAlbertGraphGenerator<String, Integer> gen =
        new BarabasiAlbertGraphGenerator<>(4, 3, 20);
    gen.generateGraph(adapter, null);
    return network;
  }

  static class VertexSupplier implements Supplier<String> {
    char a = 'a';

    public String get() {
      return Character.toString(a++);
    }
  }
}
