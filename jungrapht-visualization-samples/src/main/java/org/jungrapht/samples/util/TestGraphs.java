/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
/*
 * Created on Jul 2, 2003
 *
 */
package org.jungrapht.samples.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.generate.CompleteBipartiteGraphGenerator;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;

/** Provides generators for several different test graphs. */
public class TestGraphs {

  /**
   * A series of pairs that may be useful for generating graphs. The miniature graph consists of 8
   * edges, 10 vertices, and is formed of two connected components, one of 8 vertices, the other of
   * 2.
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
   * @return a graph consisting of eight edges and ten vertices.
   */
  public static Graph<String, Integer> createTestGraph(boolean directed) {
    Graph<String, Integer> graph;
    if (directed) {
      graph =
          GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedMultigraph())
              .edgeSupplier(SupplierUtil.createIntegerSupplier())
              .buildGraph();
    } else {
      graph =
          GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.multigraph())
              .buildGraph();
    }

    for (String[] pair : pairs) {
      graph.addVertex(pair[0]);
      graph.addVertex(pair[1]);
      graph.addEdge(pair[0], pair[1], Integer.parseInt(pair[2]));
    }
    return graph;
  }

  /**
   * @param chain_length the length of the chain of vertices to add to the returned graph
   * @param isolate_count the number of isolated vertices to add to the returned graph
   * @return a graph consisting of a chain of {@code chain_length} vertices and {@code
   *     isolate_count} isolated vertices.
   */
  public static Graph<String, Integer> createChainPlusIsolates(
      int chain_length, int isolate_count) {
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.multigraph())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    if (chain_length > 0) {
      String[] v = new String[chain_length];
      v[0] = "v" + 0;
      graph.addVertex(v[0]);
      for (int i = 1; i < chain_length; i++) {
        v[i] = "v" + i;
        graph.addVertex(v[i]);
        graph.addEdge(v[i], v[i - 1]);
      }
    }
    for (int i = 0; i < isolate_count; i++) {
      String v = "v" + (chain_length + i);
      graph.addVertex(v);
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
  public static Graph<String, Integer> createDirectedAcyclicGraph(
      int layers, int maxVerticesPerLayer, double linkprob) {
    return createDirectedAcyclicGraph(
        layers, maxVerticesPerLayer, linkprob, System.currentTimeMillis());
  }

  public static Graph<String, Integer> createDirectedAcyclicGraph(
      int layers, int maxVerticesPerLayer, double linkprob, long randomSeed) {
    Random random = new Random(randomSeed);
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedMultigraph())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    Set<String> previousLayers = new HashSet<>();
    Set<String> inThisLayer = new HashSet<>();
    for (int i = 0; i < layers; i++) {

      int verticesThisLayer = (int) (random.nextDouble() * maxVerticesPerLayer) + 1;
      for (int j = 0; j < verticesThisLayer; j++) {
        String v = i + ":" + j;
        graph.addVertex(v);
        inThisLayer.add(v);
        // for each previous vertex...
        for (String v2 : previousLayers) {
          if (random.nextDouble() < linkprob) {
            graph.addEdge(v2, v);
          }
        }
      }

      previousLayers.addAll(inThisLayer);
      inThisLayer.clear();
    }
    return graph;
  }

  public static Graph<String, Integer> getOneVertexGraph() {

    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.simple())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    graph.addVertex("A");
    return graph;
  }
  /**
   * Returns a bigger, undirected test graph with a just one component. This graph consists of a
   * clique of ten edges, a partial clique (randomly generated, with edges of 0.6 probability), and
   * one series of edges running from the first vertex to the last.
   *
   * @return the testgraph
   */
  public static Graph<String, Integer> getOneComponentGraph() {

    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.simple())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    // let's throw in a clique, too
    for (int i = 1; i <= 10; i++) {
      for (int j = i + 1; j <= 10; j++) {
        String i1 = "" + i;
        String i2 = "" + j;
        graph.addVertex(i1);
        graph.addVertex(i2);
        graph.addEdge(i1, i2);
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
        graph.addEdge(i1, i2);
      }
    }
    Iterator<String> vertexIt = graph.vertexSet().iterator();
    String current = vertexIt.next();
    while (vertexIt.hasNext()) {
      String next = vertexIt.next();
      graph.addEdge(current, next);
    }

    return graph;
  }

  public static <T> Graph<T, Integer> getOneComponentGraph(Supplier<T> factory) {

    Graph<T, Integer> graph =
        GraphTypeBuilder.<T, Integer>forGraphType(DefaultGraphType.pseudograph())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .vertexSupplier(factory)
            .buildGraph();

    List<T> list = IntStream.range(0, 20).mapToObj(i -> factory.get()).collect(Collectors.toList());
    // create a clique
    for (int i = 0; i < 10; i++) {
      for (int j = i + 1; j < 10; j++) {
        T i1 = list.get(i);
        T i2 = list.get(j);
        graph.addVertex(i1);
        graph.addVertex(i2);
        graph.addEdge(i1, i2);
      }
    }

    // create a partial clique
    for (int i = 10; i < 20; i++) {
      for (int j = i + 1; j < 20; j++) {
        if (Math.random() > 0.6) {
          continue;
        }
        T i1 = list.get(i);
        T i2 = list.get(j);
        graph.addVertex(i1);
        graph.addVertex(i2);
        graph.addEdge(i1, i2);
      }
    }
    Iterator<T> vertexIt = graph.vertexSet().iterator();
    T current = vertexIt.next();
    while (vertexIt.hasNext()) {
      T next = vertexIt.next();
      graph.addEdge(current, next);
    }
    return graph;
  }

  /**
   * Returns a bigger test graph with a clique, several components, and other parts.
   *
   * @return a demonstration graph of type <tt>UndirectedSparseMultiGraph</tt> with 28 vertices.
   */
  public static Graph<String, Integer> getDemoGraph() {
    return getDemoGraph(false);
  }

  /**
   * Returns a bigger test graph with a clique, several components, and other parts.
   *
   * @param directed true if the supplied graph should be directed
   * @return a demonstration graph of type <tt>UndirectedSparseMultiGraph</tt> with 28 vertices.
   */
  public static Graph<String, Integer> getDemoGraph(boolean directed) {
    GraphType graphType =
        directed ? DefaultGraphType.directedMultigraph() : DefaultGraphType.multigraph();
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(graphType)
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    for (String[] pair : pairs) {
      graph.addVertex(pair[0]);
      graph.addVertex(pair[1]);
      graph.addEdge(pair[0], pair[1]);
    }

    // let's throw in a clique, too
    for (int i = 1; i <= 10; i++) {
      for (int j = i + 1; j <= 10; j++) {
        String i1 = "c" + i;
        String i2 = "c" + j;
        graph.addVertex(i1);
        graph.addVertex(i2);
        graph.addEdge(i1, i2);
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
        graph.addEdge(i1, i2);
      }
    }
    return graph;
  }

  public static Graph<String, Integer> createSmallGraph(boolean directed) {
    Graph<String, Integer> graph;
    if (directed) {
      graph =
          GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedMultigraph())
              .edgeSupplier(SupplierUtil.createIntegerSupplier())
              .buildGraph();
    } else {
      graph =
          GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.multigraph())
              .buildGraph();
    }

    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B", 1);
    graph.addEdge("A", "C", 2);
    graph.addEdge("B", "C", 3);

    return graph;
  }

  /** @return the graph for this demo */
  public static Graph<String, Integer> getGeneratedGraph() {

    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedPseudograph())
            .vertexSupplier(new VertexSupplier())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    BarabasiAlbertGraphGenerator<String, Integer> gen =
        new BarabasiAlbertGraphGenerator<>(4, 3, 20);
    gen.generateGraph(graph, null);
    return graph;
  }

  public static Graph<String, Integer> getGeneratedGraph2() {

    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.multigraph())
            .vertexSupplier(new VertexSupplier())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    BarabasiAlbertGraphGenerator<String, Integer> gen =
        new BarabasiAlbertGraphGenerator<>(2, 2, 1000);
    gen.generateGraph(graph, null);
    return graph;
  }

  public static Graph<String, Integer> getGeneratedBipartiteGraph() {

    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.multigraph())
            .vertexSupplier(SupplierUtil.createStringSupplier())
            //            .vertexSupplier(new VertexSupplier())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    CompleteBipartiteGraphGenerator<String, Integer> gen =
        new CompleteBipartiteGraphGenerator<>(5, 10);
    gen.generateGraph(graph, null);
    return graph;
  }

  static class VertexSupplier implements Supplier<String> {
    char a = 'a';

    public String get() {
      return Character.toString(a++);
    }
  }

  public static Graph<String, Integer> gridGraph(int size) {
    Graph<String, Integer> graph = GraphTypeBuilder.forGraphType(DefaultGraphType.multigraph())
            .vertexSupplier(SupplierUtil.createStringSupplier())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    String[][] vertexBox = new String[size][size];
    for (int i=0; i<size; i++) {
      for (int j=0; j<size; j++) {
        vertexBox[i][j] = graph.addVertex();
      }
    }
    for (int i=0; i<size; i++) {
      // if we are not on the last row, connect all in row to next row
      if (i < size-1) {
        for (int jj=0; jj<size; jj++) {
          graph.addEdge(vertexBox[i][jj], vertexBox[i+1][jj]);
        }
      }
      for (int j = 0; j < size; j++) {
        // if we are not in the last column, connect to next column
        if (j < size-1) {
          graph.addEdge(vertexBox[i][j], vertexBox[i][j+1]);
        }
      }
    }
    return graph;
  }

}
