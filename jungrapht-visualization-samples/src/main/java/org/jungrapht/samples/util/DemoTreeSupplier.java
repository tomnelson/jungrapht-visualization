package org.jungrapht.samples.util;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.graph.builder.GraphTypeBuilder;

/** @author Tom Nelson */
public class DemoTreeSupplier {

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
}
