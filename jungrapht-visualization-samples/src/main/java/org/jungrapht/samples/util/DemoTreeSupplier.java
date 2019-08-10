package org.jungrapht.samples.util;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.generate.BarabasiAlbertForestGenerator;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class DemoTreeSupplier {

  private static final Logger log = LoggerFactory.getLogger(DemoTreeSupplier.class);

  public static Graph<String, Integer> createSmallTree() {
    GraphBuilder<String, Integer, ?> treeBuilder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedSimple())
            .buildGraphBuilder();

    treeBuilder.addVertex("root");

    Integer edgeId = 0;
    // root gets 3 children
    treeBuilder.addEdge("root", "V0", edgeId++);
    treeBuilder.addEdge("root", "V1", edgeId++);
    treeBuilder.addEdge("root", "V2", edgeId++);

    // V2 gets 2 children
    treeBuilder.addEdge("V2", "C0", edgeId++);
    treeBuilder.addEdge("V2", "C1", edgeId++);
    return treeBuilder.build();
  }

  public static Graph<String, Number> createGenericTreeOne() {
    GraphBuilder<String, Number, ?> tree =
        GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.directedMultigraph())
            .buildGraphBuilder();

    tree.addVertex("root");

    Integer edgeId = 0;
    tree.addEdge("root", "V0", edgeId++);
    tree.addEdge("V0", "V1", edgeId++);
    tree.addEdge("V0", "V2", edgeId++);
    tree.addEdge("V1", "V4", edgeId++);
    tree.addEdge("V2", "V3", edgeId++);
    tree.addEdge("V2", "V5", edgeId++);
    tree.addEdge("V4", "V6", edgeId++);
    tree.addEdge("V4", "V7", edgeId++);
    tree.addEdge("V3", "V8", edgeId++);
    tree.addEdge("V6", "V9", edgeId++);
    tree.addEdge("V4", "V10", edgeId++);

    tree.addEdge("root", "A0", edgeId++);
    tree.addEdge("A0", "A1", edgeId++);
    tree.addEdge("A0", "A2", edgeId++);
    tree.addEdge("A0", "A3", edgeId++);

    tree.addEdge("root", "B0", edgeId++);
    tree.addEdge("B0", "B1", edgeId++);
    tree.addEdge("B0", "B2", edgeId++);
    tree.addEdge("B1", "B4", edgeId++);
    tree.addEdge("B2", "B3", edgeId++);
    tree.addEdge("B2", "B5", edgeId++);
    tree.addEdge("B4", "B6", edgeId++);
    tree.addEdge("B4", "B7", edgeId++);
    tree.addEdge("B3", "B8", edgeId++);
    tree.addEdge("B6", "B9", edgeId++);

    return tree.build();
  }

  /** */
  public static Graph<String, Integer> createTreeTwo() {
    GraphBuilder<String, Integer, Graph<String, Integer>> builder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag()).buildGraphBuilder();

    Integer edgeId = 0;
    builder.addVertex("A0");
    builder.addEdge("A0", "B0", edgeId++);
    builder.addEdge("A0", "B1", edgeId++);
    builder.addEdge("A0", "B2", edgeId++);

    builder.addEdge("B0", "C0", edgeId++);
    builder.addEdge("B0", "C1", edgeId++);
    builder.addEdge("B0", "C2", edgeId++);
    builder.addEdge("B0", "C3", edgeId++);

    builder.addEdge("C2", "H0", edgeId++);
    builder.addEdge("C2", "H1", edgeId++);

    builder.addEdge("B1", "D0", edgeId++);
    builder.addEdge("B1", "D1", edgeId++);
    builder.addEdge("B1", "D2", edgeId++);

    builder.addEdge("B2", "E0", edgeId++);
    builder.addEdge("B2", "E1", edgeId++);
    builder.addEdge("B2", "E2", edgeId++);

    builder.addEdge("D0", "F0", edgeId++);
    builder.addEdge("D0", "F1", edgeId++);
    builder.addEdge("D0", "F2", edgeId++);

    builder.addEdge("D1", "G0", edgeId++);
    builder.addEdge("D1", "G1", edgeId++);
    builder.addEdge("D1", "G2", edgeId++);
    builder.addEdge("D1", "G3", edgeId++);
    builder.addEdge("D1", "G4", edgeId++);
    builder.addEdge("D1", "G5", edgeId++);
    builder.addEdge("D1", "G6", edgeId++);
    builder.addEdge("D1", "G7", edgeId++);

    return builder.build();
  }

  public static Graph<String, Integer> createTreeOne() {
    GraphBuilder<String, Integer, ?> tree =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag()).buildGraphBuilder();

    tree.addVertex("root");

    Integer edgeId = 0;
    tree.addEdge("root", "V0", edgeId++);
    tree.addEdge("V0", "V1", edgeId++);
    tree.addEdge("V0", "V2", edgeId++);
    tree.addEdge("V1", "V4", edgeId++);
    tree.addEdge("V2", "V3", edgeId++);
    tree.addEdge("V2", "V5", edgeId++);
    tree.addEdge("V4", "V6", edgeId++);
    tree.addEdge("V4", "V7", edgeId++);
    tree.addEdge("V3", "V8", edgeId++);
    tree.addEdge("V6", "V9", edgeId++);
    tree.addEdge("V4", "V10", edgeId++);

    tree.addEdge("root", "A0", edgeId++);
    tree.addEdge("A0", "A1", edgeId++);
    tree.addEdge("A0", "A2", edgeId++);
    tree.addEdge("A0", "A3", edgeId++);

    tree.addEdge("root", "B0", edgeId++);
    tree.addEdge("B0", "B1", edgeId++);
    tree.addEdge("B0", "B2", edgeId++);
    tree.addEdge("B1", "B4", edgeId++);
    tree.addEdge("B2", "B3", edgeId++);
    tree.addEdge("B2", "B5", edgeId++);
    tree.addEdge("B4", "B6", edgeId++);
    tree.addEdge("B4", "B7", edgeId++);
    tree.addEdge("B3", "B8", edgeId++);
    tree.addEdge("B6", "B9", edgeId++);

    return tree.build();
  }

  public static Graph<String, Integer> createForest() {
    GraphBuilder<String, Integer, Graph<String, Integer>> tree =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag()).buildGraphBuilder();

    Integer edgeId = 0;
    tree.addEdge("V0", "V1", edgeId++);
    tree.addEdge("V0", "V2", edgeId++);
    tree.addEdge("V1", "V4", edgeId++);
    tree.addEdge("V2", "V3", edgeId++);
    tree.addEdge("V2", "V5", edgeId++);
    tree.addEdge("V4", "V6", edgeId++);
    tree.addEdge("V4", "V7", edgeId++);
    tree.addEdge("V3", "V8", edgeId++);
    tree.addEdge("V6", "V9", edgeId++);
    tree.addEdge("V4", "V10", edgeId++);

    tree.addEdge("A0", "A1", edgeId++);
    tree.addEdge("A0", "A2", edgeId++);
    tree.addEdge("A0", "A3", edgeId++);

    tree.addEdge("B0", "B1", edgeId++);
    tree.addEdge("B0", "B2", edgeId++);
    tree.addEdge("B1", "B4", edgeId++);
    tree.addEdge("B2", "B3", edgeId++);
    tree.addEdge("B2", "B5", edgeId++);
    tree.addEdge("B4", "B6", edgeId++);
    tree.addEdge("B4", "B7", edgeId++);
    tree.addEdge("B3", "B8", edgeId++);
    tree.addEdge("B6", "B9", edgeId++);

    return tree.build();
  }

  public static Graph<String, Integer> createForest2() {
    GraphBuilder<String, Integer, Graph<String, Integer>> builder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.simple())
            .buildGraphBuilder();

    Integer edgeId = 0;
    builder.addEdge("A0", "A1", edgeId++);
    builder.addEdge("A0", "A2", edgeId++);
    //    builder.addEdge("A1", "A4", edgeId++);
    //    builder.addEdge("A2", "A3", edgeId++);
    //    builder.addEdge("A2", "A5", edgeId++);
    //    builder.addEdge("A4", "A6", edgeId++);
    //    builder.addEdge("A4", "A7", edgeId++);
    //    builder.addEdge("A3", "A8", edgeId++);
    //    builder.addEdge("A6", "A9", edgeId++);
    //    builder.addEdge("A4", "A10", edgeId++);

    builder.addEdge("G0", "G1", edgeId++);
    builder.addEdge("G0", "G2", edgeId++);
    //    builder.addEdge("G1", "G4", edgeId++);
    //    builder.addEdge("G2", "G3", edgeId++);
    //    builder.addEdge("G2", "G5", edgeId++);
    //    builder.addEdge("G4", "G6", edgeId++);
    //    builder.addEdge("G4", "G7", edgeId++);
    //    builder.addEdge("G3", "G8", edgeId++);
    //    builder.addEdge("G6", "G9", edgeId++);

    builder.addEdge("B0", "B1", edgeId++);
    builder.addEdge("B0", "B2", edgeId++);
    builder.addEdge("B0", "B3", edgeId++);

    builder.addEdge("C0", "C1", edgeId++);
    builder.addEdge("C0", "C2", edgeId++);
    builder.addEdge("C0", "C3", edgeId++);

    builder.addEdge("D0", "D1", edgeId++);
    builder.addEdge("D0", "D2", edgeId++);

    builder.addEdge("E0", "E1", edgeId++);

    builder.addEdge("F0", "F1", edgeId++);

    builder.addEdge("E10", "E11", edgeId++);

    builder.addEdge("F10", "F11", edgeId++);

    builder.addEdge("E20", "E21", edgeId++);

    builder.addEdge("F20", "F21", edgeId++);
    int i = 0;
    char c = (char) ('H' + i);
    for (; i < 8; i++) {
      System.err.println("char is " + c);
      builder.addEdge(c + "0", c + "1", edgeId++);
      builder.addEdge(c + "0", c + "2", edgeId++);
      builder.addEdge(c + "1", c + "4", edgeId++);
      builder.addEdge(c + "2", c + "3", edgeId++);
      builder.addEdge(c + "2", c + "5", edgeId++);
      builder.addEdge(c + "4", c + "6", edgeId++);
      builder.addEdge(c + "4", c + "7", edgeId++);
      builder.addEdge(c + "3", c + "8", edgeId++);
      builder.addEdge(c + "6", c + "9", edgeId++);
      builder.addEdge(c + "6", c + "10", edgeId++);
      builder.addEdge(c + "9", c + "11", edgeId++);
      builder.addEdge(c + "9", c + "12", edgeId++);
      c++;
    }

    for (; i < 14; i++) {
      //      char c = (char) (i);
      System.err.println("char is " + c);
      builder.addEdge(c + "0", c + "1", edgeId++);
      builder.addEdge(c + "0", c + "2", edgeId++);
      builder.addEdge(c + "0", c + "3", edgeId++);
      c++;
    }

    return SpanningTreeAdapter.getSpanningTree(builder.build());
  }

  public static Graph<String, Integer> generateProgramGraph() {
    GraphBuilder<String, Integer, Graph<String, Integer>> builder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag()).buildGraphBuilder();
    builder.addEdge("A0", "A1", (Integer) 0);
    builder.addEdge("A1", "A2", (Integer) 1);
    builder.addEdge("A2", "A3", (Integer) 3);
    builder.addEdge("A3", "A4", (Integer) 4);
    builder.addEdge("A3", "A5", (Integer) 100);
    builder.addEdge("A1", "A6", (Integer) 101);
    builder.addEdge("A6", "A7", (Integer) 102);
    builder.addEdge("A6", "A8", (Integer) 103);
    builder.addEdge("A0", "A9", (Integer) 104);
    return builder.build();
  }

  public static Graph<String, Integer> generateProgramGraph2() {
    GraphBuilder<String, Integer, Graph<String, Integer>> builder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag()).buildGraphBuilder();
    builder.addEdge("A0", "A1", (Integer) 0);
    builder.addEdge("A1", "A2", (Integer) 1);
    builder.addEdge("A2", "A3", (Integer) 3);
    builder.addEdge("A3", "A4", (Integer) 4);
    builder.addEdge("A3", "A5", (Integer) 10);
    builder.addEdge("A1", "A6", (Integer) 11);
    builder.addEdge("A6", "A7", (Integer) 12);
    builder.addEdge("A6", "A8", (Integer) 13);
    builder.addEdge("A0", "A9", (Integer) 14);

    builder.addEdge("B0", "B1", (Integer) 5);
    builder.addEdge("B1", "B2", (Integer) 6);
    builder.addEdge("B2", "B3", (Integer) 7);
    builder.addEdge("B3", "B4", (Integer) 8);
    builder.addEdge("B3", "B5", (Integer) 15);
    builder.addEdge("B1", "B6", (Integer) 16);
    builder.addEdge("B6", "B7", (Integer) 17);
    builder.addEdge("B6", "B8", (Integer) 18);
    builder.addEdge("B0", "B9", (Integer) 19);

    return builder.build();
  }

  public static Graph<String, Integer> generateProgramGraph3() {
    GraphBuilder<String, Integer, Graph<String, Integer>> builder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag()).buildGraphBuilder();

    Integer e = 0;
    IntStream.range(0, 5)
        .forEach(
            i -> {
              Integer edge = e + i * 5;
              char c = (char) ('A' + i);
              builder.addEdge(c + "0", c + "1", (Integer) (edge + 0));
              builder.addEdge(c + "1", c + "2", (Integer) (edge + 1));
              builder.addEdge(c + "2", c + "3", (Integer) (edge + 2));
              builder.addEdge(c + "3", c + "4", (Integer) (edge + 3));
              builder.addEdge(c + "3", c + "5", (Integer) (100 + edge + 0));
              builder.addEdge(c + "1", c + "6", (Integer) (100 + edge + 1));
              builder.addEdge(c + "6", c + "7", (Integer) (100 + edge + 2));
              builder.addEdge(c + "6", c + "8", (Integer) (100 + edge + 3));
              builder.addEdge(c + "0", c + "9", (Integer) (100 + edge + 4));
            });
    return builder.build();
  }

  public static Graph<String, Integer> generateForest(int roots, int nodes) {
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.simple())
            .vertexSupplier(new VertexSupplier())
            .edgeSupplier(new EdgeSupplier())
            .buildGraphBuilder()
            .build();
    BarabasiAlbertForestGenerator gen = new BarabasiAlbertForestGenerator(roots, nodes);

    gen.generateGraph(graph);
    System.err.println("generated " + graph);
    Graph<String, Integer> directedGraph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedSimple())
            .buildGraph();
    graph.vertexSet().stream().forEach(v -> directedGraph.addVertex(v));
    graph
        .edgeSet()
        .stream()
        .forEach(e -> directedGraph.addEdge(graph.getEdgeTarget(e), graph.getEdgeSource(e), e));
    log.info("graph is {}, directedGraph is {}", graph, directedGraph);
    return directedGraph;
  }

  public static class VertexSupplier implements Supplier<String> {
    char a = 'a';

    public String get() {
      return Character.toString(a++);
    }

    public static Set<String> get(int count) {
      VertexSupplier supplier = new VertexSupplier();
      Set<String> set = new HashSet<>();
      IntStream.range(0, count).forEach(s -> set.add(supplier.get()));
      return set;
    }
  }

  public static class EdgeSupplier implements Supplier<Integer> {
    int count;

    public Integer get() {
      return count++;
    }
  }
}
