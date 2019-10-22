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
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.util.helpers.SpanningTreeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class DemoTreeSupplier {

  private static final Logger log = LoggerFactory.getLogger(DemoTreeSupplier.class);

  public static Graph<String, Integer> createSmallTree() {
    GraphBuilder<String, Integer, ?> treeBuilder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedSimple())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraphBuilder();

    treeBuilder.addVertex("root");

    Integer edgeId = 0;
    // root gets 3 children
    treeBuilder.addEdge("root", "V0");
    treeBuilder.addEdge("root", "V1");
    treeBuilder.addEdge("root", "V2");

    // V2 gets 2 children
    treeBuilder.addEdge("V2", "C0");
    treeBuilder.addEdge("V2", "C1");
    return treeBuilder.build();
  }

  public static Graph<String, Integer> createGenericTreeOne() {
    GraphBuilder<String, Integer, ?> tree =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedMultigraph())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraphBuilder();

    tree.addVertex("root");

    tree.addEdge("root", "V0");
    tree.addEdge("V0", "V1");
    tree.addEdge("V0", "V2");
    tree.addEdge("V1", "V4");
    tree.addEdge("V2", "V3");
    tree.addEdge("V2", "V5");
    tree.addEdge("V4", "V6");
    tree.addEdge("V4", "V7");
    tree.addEdge("V3", "V8");
    tree.addEdge("V6", "V9");
    tree.addEdge("V4", "V10");

    tree.addEdge("root", "A0");
    tree.addEdge("A0", "A1");
    tree.addEdge("A0", "A2");
    tree.addEdge("A0", "A3");

    tree.addEdge("root", "B0");
    tree.addEdge("B0", "B1");
    tree.addEdge("B0", "B2");
    tree.addEdge("B1", "B4");
    tree.addEdge("B2", "B3");
    tree.addEdge("B2", "B5");
    tree.addEdge("B4", "B6");
    tree.addEdge("B4", "B7");
    tree.addEdge("B3", "B8");
    tree.addEdge("B6", "B9");

    return tree.build();
  }

  /** */
  public static Graph<String, Integer> createTreeTwo() {
    GraphBuilder<String, Integer, Graph<String, Integer>> builder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraphBuilder();

    builder.addVertex("A0");
    builder.addEdge("A0", "B0");
    builder.addEdge("A0", "B1");
    builder.addEdge("A0", "B2");

    builder.addEdge("B0", "C0");
    builder.addEdge("B0", "C1");
    builder.addEdge("B0", "C2");
    builder.addEdge("B0", "C3");

    builder.addEdge("C2", "H0");
    builder.addEdge("C2", "H1");

    builder.addEdge("H1", "H2");
    builder.addEdge("H1", "H3");

    builder.addEdge("H3", "H4");
    builder.addEdge("H3", "H5");
    builder.addEdge("H5", "H6");
    builder.addEdge("H5", "H7");

    builder.addEdge("B1", "D0");
    builder.addEdge("B1", "D1");
    builder.addEdge("B1", "D2");

    builder.addEdge("B2", "E0");
    builder.addEdge("B2", "E1");
    builder.addEdge("B2", "E2");

    builder.addEdge("D0", "F0");
    builder.addEdge("D0", "F1");
    builder.addEdge("D0", "F2");

    builder.addEdge("D1", "G0");
    builder.addEdge("D1", "G1");
    builder.addEdge("D1", "G2");
    builder.addEdge("D1", "G3");
    builder.addEdge("D1", "G4");
    builder.addEdge("D1", "G5");
    builder.addEdge("D1", "G6");
    builder.addEdge("D1", "G7");

    return builder.build();
  }

  public static Graph<String, Integer> createTreeOne() {
    GraphBuilder<String, Integer, ?> tree =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraphBuilder();

    tree.addVertex("root");

    tree.addEdge("root", "V0");
    tree.addEdge("V0", "V1");
    tree.addEdge("V0", "V2");
    tree.addEdge("V1", "V4");
    tree.addEdge("V2", "V3");
    tree.addEdge("V2", "V5");
    tree.addEdge("V4", "V6");
    tree.addEdge("V4", "V7");
    tree.addEdge("V3", "V8");
    tree.addEdge("V6", "V9");
    tree.addEdge("V4", "V10");

    tree.addEdge("root", "A0");
    tree.addEdge("A0", "A1");
    tree.addEdge("A0", "A2");
    tree.addEdge("A0", "A3");

    tree.addEdge("root", "B0");
    tree.addEdge("B0", "B1");
    tree.addEdge("B0", "B2");
    tree.addEdge("B1", "B4");
    tree.addEdge("B2", "B3");
    tree.addEdge("B2", "B5");
    tree.addEdge("B4", "B6");
    tree.addEdge("B4", "B7");
    tree.addEdge("B3", "B8");
    tree.addEdge("B6", "B9");

    return tree.build();
  }

  public static Graph<String, Integer> createForestForCompactTreeLayout() {
    GraphBuilder<String, Integer, Graph<String, Integer>> tree =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraphBuilder();

    tree.addEdge("R1", "A1");
    tree.addEdge("R1", "A2");
    tree.addEdge("A2", "A3");
    tree.addEdge("A2", "A4");
    tree.addEdge("A4", "A5");
    tree.addEdge("A4", "A6");

    tree.addEdge("R2", "B1");
    tree.addEdge("R2", "B2");

    tree.addEdge("R3", "C1");
    tree.addEdge("C1", "C2");
    tree.addEdge("C1", "C3");
    tree.addEdge("R3", "C4");

    return tree.build();
  }

  public static Graph<String, Integer> createForest() {
    GraphBuilder<String, Integer, Graph<String, Integer>> tree =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraphBuilder();

    //    tree.addEdge("R", "V0");
    //    tree.addEdge("R", "A0");
    //    tree.addEdge("R", "B0");
    tree.addEdge("V0", "V1");
    tree.addEdge("V0", "V2");
    tree.addEdge("V1", "V4");
    tree.addEdge("V2", "V3");
    tree.addEdge("V2", "V5");
    tree.addEdge("V4", "V6");
    tree.addEdge("V4", "V7");
    tree.addEdge("V3", "V8");
    tree.addEdge("V6", "V9");
    tree.addEdge("V4", "V10");

    tree.addEdge("V5", "V11");
    tree.addEdge("V5", "V12");

    tree.addEdge("A0", "A1");
    tree.addEdge("A0", "A2");
    tree.addEdge("A0", "A3");

    tree.addEdge("B0", "B1");
    tree.addEdge("B0", "B2");
    tree.addEdge("B1", "B4");
    tree.addEdge("B2", "B3");
    tree.addEdge("B2", "B5");
    tree.addEdge("B4", "B6");
    tree.addEdge("B4", "B7");
    tree.addEdge("B4", "B8");
    tree.addEdge("B3", "B9");
    tree.addEdge("B6", "B10");

    tree.addEdge("B6", "B11");

    return tree.build();
  }

  public static Graph<String, Integer> createForest2() {
    GraphBuilder<String, Integer, Graph<String, Integer>> builder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.simple())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraphBuilder();

    Integer edgeId = 0;
    builder.addEdge("A0", "A1");
    builder.addEdge("A0", "A2");
    //    builder.addEdge("A1", "A4");
    //    builder.addEdge("A2", "A3");
    //    builder.addEdge("A2", "A5");
    //    builder.addEdge("A4", "A6");
    //    builder.addEdge("A4", "A7");
    //    builder.addEdge("A3", "A8");
    //    builder.addEdge("A6", "A9");
    //    builder.addEdge("A4", "A10");

    builder.addEdge("G0", "G1");
    builder.addEdge("G0", "G2");
    //    builder.addEdge("G1", "G4");
    //    builder.addEdge("G2", "G3");
    //    builder.addEdge("G2", "G5");
    //    builder.addEdge("G4", "G6");
    //    builder.addEdge("G4", "G7");
    //    builder.addEdge("G3", "G8");
    //    builder.addEdge("G6", "G9");

    builder.addEdge("B0", "B1");
    builder.addEdge("B0", "B2");
    builder.addEdge("B0", "B3");

    builder.addEdge("C0", "C1");
    builder.addEdge("C0", "C2");
    builder.addEdge("C0", "C3");

    builder.addEdge("D0", "D1");
    builder.addEdge("D0", "D2");

    builder.addEdge("E0", "E1");

    builder.addEdge("F0", "F1");

    builder.addEdge("E10", "E11");

    builder.addEdge("F10", "F11");

    builder.addEdge("E20", "E21");

    builder.addEdge("F20", "F21");
    int i = 0;
    char c = (char) ('H' + i);
    for (; i < 8; i++) {
      builder.addEdge(c + "0", c + "1");
      builder.addEdge(c + "0", c + "2");
      builder.addEdge(c + "1", c + "4");
      builder.addEdge(c + "2", c + "3");
      builder.addEdge(c + "2", c + "5");
      builder.addEdge(c + "4", c + "6");
      builder.addEdge(c + "4", c + "7");
      builder.addEdge(c + "3", c + "8");
      builder.addEdge(c + "6", c + "9");
      builder.addEdge(c + "6", c + "10");
      builder.addEdge(c + "9", c + "11");
      builder.addEdge(c + "9", c + "12");
      c++;
    }

    for (; i < 14; i++) {
      builder.addEdge(c + "0", c + "1");
      builder.addEdge(c + "0", c + "2");
      builder.addEdge(c + "0", c + "3");
      c++;
    }

    return SpanningTreeAdapter.getSpanningTree(builder.build());
  }

  public static Graph<String, Integer> generateProgramGraph() {
    Integer edgeCounter = 100;
    GraphBuilder<String, Integer, Graph<String, Integer>> builder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraphBuilder();
    builder.addEdge("A0", "A1", (Integer) 0);
    builder.addEdge("A1", "A2", (Integer) 1);
    builder.addEdge("A2", "A3", (Integer) 3);
    builder.addEdge("A3", "A4", (Integer) 4);
    builder.addEdge("A3", "A5", edgeCounter++);
    builder.addEdge("A1", "A6", edgeCounter++);
    builder.addEdge("A6", "A7", edgeCounter++);
    builder.addEdge("A6", "A8", edgeCounter++);
    builder.addEdge("A0", "A9", edgeCounter++);
    builder.addEdge("A9", "A10", edgeCounter++);
    builder.addEdge("A9", "A11", edgeCounter++);
    builder.addEdge("A10", "A12", edgeCounter++);
    builder.addEdge("A7", "A1", edgeCounter++);
    builder.addEdge("A12", "A2", edgeCounter++);
    builder.addEdge("A11", "A13", edgeCounter++);
    builder.addEdge("A11", "A14", edgeCounter++);
    return builder.build();
  }

  public static Graph<String, Integer> generateProgramGraph2() {
    Integer edgeCounter = 100;
    Integer zeroCounter = 0;
    GraphBuilder<String, Integer, Graph<String, Integer>> builder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag()).buildGraphBuilder();
    builder.addEdge("A0", "A1", zeroCounter++);
    builder.addEdge("A1", "A2", zeroCounter++);
    builder.addEdge("A2", "A3", zeroCounter++);
    builder.addEdge("A3", "A4", zeroCounter++);
    builder.addEdge("A3", "A5", edgeCounter++);
    builder.addEdge("A1", "A6", edgeCounter++);
    builder.addEdge("A0", "A6", edgeCounter++);
    builder.addEdge("A6", "A7", edgeCounter++);
    builder.addEdge("A6", "A8", edgeCounter++);
    builder.addEdge("A0", "A9", edgeCounter++);
    builder.addEdge("A9", "A10", edgeCounter++);
    builder.addEdge("A9", "A11", edgeCounter++);
    builder.addEdge("A10", "A12", edgeCounter++);
    builder.addEdge("A7", "A1", edgeCounter++);
    builder.addEdge("A12", "A2", edgeCounter++);
    builder.addEdge("A11", "A13", edgeCounter++);
    builder.addEdge("A11", "A14", edgeCounter++);

    builder.addEdge("B0", "B1", zeroCounter++);
    builder.addEdge("B1", "B2", zeroCounter++);
    builder.addEdge("B2", "B3", zeroCounter++);
    builder.addEdge("B3", "B4", zeroCounter++);
    builder.addEdge("B3", "B5", edgeCounter++);
    builder.addEdge("B1", "B6", edgeCounter++);
    builder.addEdge("B6", "B7", edgeCounter++);
    builder.addEdge("B6", "B8", edgeCounter++);
    builder.addEdge("B0", "B8", edgeCounter++);
    builder.addEdge("B0", "B9", edgeCounter++);
    builder.addEdge("B9", "B10", edgeCounter++);
    builder.addEdge("B9", "B11", edgeCounter++);
    builder.addEdge("B10", "B12", edgeCounter++);
    builder.addEdge("B7", "B1", edgeCounter++);
    builder.addEdge("B12", "B2", edgeCounter++);
    builder.addEdge("B11", "B13", edgeCounter++);
    builder.addEdge("B11", "B14", edgeCounter++);

    // edge to prior tree
    builder.addEdge("B14", "A1", edgeCounter++);
    //     edge to next tree
    builder.addEdge("A14", "B1", edgeCounter++);
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
              //              builder.addEdge(c + "7", c + "1", (Integer) (100 + edge + 5));
            });
    return builder.build();
  }

  public static Graph<String, Integer> generatePicture() {
    GraphBuilder<String, Integer, Graph<String, Integer>> builder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraphBuilder();
    builder.addEdge("SimpleDigraphAdapter<V>", "SimpleDigraph<V>");
    builder.addEdge("SimpleDigraphAdapter<V>", "Digraph<V,E>");
    builder.addEdge("SimpleDigraphAdapter<V>", "WeightedDigraph<V>");
    builder.addEdge("SimpleDigraphAdapter<V>", "WeightedDigraphAdapter<V>");
    builder.addEdge("WeightedDigraphAdapter<V>", "WeightedDigraph<V>");
    builder.addEdge("WeightedDigraphAdapter<V>", "Digraph<V,E>");
    builder.addEdge("WeightedDigraphAdapter<V>", "DigraphAdapter<V,E>");
    builder.addEdge("WeightedDigraphAdapter<V>", "Digraphs");
    builder.addEdge("WeightedDigraphAdapter<V>", "MapDigraph<V,E>");
    builder.addEdge("SimpleDigraphAdapter<V>", "Digraphs");
    builder.addEdge("SimpleDigraphAdapter<V>", "DigraphAdapter<V,E>");
    builder.addEdge("SimpleDigraphAdapter<V>", "MapDigraph<V,E>");
    builder.addEdge("DoubledDigraphAdapter<V,E>", "MapDigraph<V,E>");
    builder.addEdge("DoubledDigraphAdapter<V,E>", "DigraphAdapter<V,E>");
    builder.addEdge("DoubledDigraphAdapter<V,E>", "DoubledDigraph<V,E>");
    builder.addEdge("DoubledDigraphAdapter<V,E>", "Digraph<V,E>");
    builder.addEdge("MapDigraph<V,E>", "Digraphs");
    builder.addEdge("MapDigraph<V,E>", "Digraph<V,E>");
    builder.addEdge("Digraphs", "Digraph<V,E>");
    builder.addEdge("Digraphs", "DigraphAdapter<V,E>");
    builder.addEdge("Digraphs", "DoubledDigraph<V,E>");
    builder.addEdge("DigraphAdapter<V,E>", "Digraph<V,E>");
    builder.addEdge("DoubledDigraph<V,E>", "Digraph<V,E>");
    builder.addEdge("WeightedDigraph<V>", "Digraph<V,E>");
    builder.addEdge("SimpleDigraph<V>", "Digraph<V,E>");

    return builder.build();
  }

  public static Graph<String, Integer> generateDag() {
    GraphBuilder<String, Integer, Graph<String, Integer>> builder =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraphBuilder();
    builder.addEdge("A0", "A1");
    builder.addEdge("A1", "A2");
    builder.addEdge("A2", "A3");
    builder.addEdge("A3", "A4");
    builder.addEdge("A3", "A5");
    builder.addEdge("A1", "A6");
    builder.addEdge("A6", "A7");
    builder.addEdge("A6", "A8");
    builder.addEdge("A0", "A9");
    builder.addEdge("A9", "A10");
    builder.addEdge("A9", "A11");
    builder.addEdge("A10", "A12");
    builder.addEdge("A7", "A1");
    builder.addEdge("A9", "A7");
    //    builder.addEdge("A12", "A2");
    builder.addEdge("A11", "A13");
    builder.addEdge("A11", "A14");

    builder.addEdge("B0", "B1");
    builder.addEdge("B1", "B2");
    builder.addEdge("B2", "B3");
    builder.addEdge("B3", "B4");
    builder.addEdge("B3", "B5");
    builder.addEdge("B1", "B6");
    builder.addEdge("B6", "B7");
    builder.addEdge("B6", "B8");
    builder.addEdge("B0", "B9");
    builder.addEdge("B9", "B10");
    builder.addEdge("B9", "B11");
    builder.addEdge("B10", "B12");
    builder.addEdge("B7", "B1");
    //    builder.addEdge("B12", "B2");
    builder.addEdge("B11", "B13");
    builder.addEdge("B11", "B14");

    // edge to prior tree
    //    builder.addEdge("B14", "A1");
    //     edge to next tree
    //    builder.addEdge("A14", "B1");

    builder.addEdge("A0", "A6");
    builder.addEdge("A9", "A12");
    builder.addEdge("B1", "B5");
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
    Graph<String, Integer> directedGraph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedSimple())
            .buildGraph();
    graph.vertexSet().forEach(directedGraph::addVertex);
    graph
        .edgeSet()
        .forEach(e -> directedGraph.addEdge(graph.getEdgeTarget(e), graph.getEdgeSource(e), e));
    log.trace("graph is {}, directedGraph is {}", graph, directedGraph);
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
