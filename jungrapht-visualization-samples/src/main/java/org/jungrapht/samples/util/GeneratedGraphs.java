package org.jungrapht.samples.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.generate.*;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;

public class GeneratedGraphs {

  public static Map<String, Supplier<Graph<String, Integer>>> map =
      Map.of(
          "BarbarasiAlbert",
          GeneratedGraphs::getBarbarasiGeneratedGraph,
          "PlantedPartition",
          GeneratedGraphs::getPlantedPartitionGeneratedGraph,
          "StarGraph",
          GeneratedGraphs::getStarGraphGeneratedGraph,
          "WheelGraph",
          GeneratedGraphs::getWheelGraphGeneratedGraph,
          "Windmill",
          GeneratedGraphs::getWindmillGraphGeneratedGraph,
          "DutchWindmill",
          GeneratedGraphs::getDutchWindmillGraphGeneratedGraph,
          "WattsStrogatz",
          GeneratedGraphs::getWattsStrogatzGraphGeneratedGraph,
          "PruferTree",
          GeneratedGraphs::getPruferTreeGeneratedGraph,
          //          "Grid",
          //          GeneratedGraphs::getGridGeneratedGraph,
          "Partial Grid",
          GeneratedGraphs::getPartialGridGeneratedGraph,
          "Complete",
          GeneratedGraphs::getCompleteGraphGeneratedGraph);

  private static Graph<String, Integer> getGraph(boolean directed) {
    return GraphTypeBuilder.<String, Integer>forGraphType(
            directed ? DefaultGraphType.directedPseudograph() : DefaultGraphType.pseudograph())
        .vertexSupplier(SupplierUtil.createStringSupplier())
        .edgeSupplier(SupplierUtil.createIntegerSupplier())
        .buildGraph();
  }

  private static Graph<String, Integer> getGraph() {
    return getGraph(false);
  }

  private static Function<GraphGenerator<String, Integer, String>, Graph<String, Integer>>
      directed =
          gen -> {
            Graph<String, Integer> graph = getGraph();
            gen.generateGraph(graph, null);
            return graph;
          };
  private static Function<GraphGenerator<String, Integer, String>, Graph<String, Integer>>
      undirected =
          gen -> {
            Graph<String, Integer> graph = getGraph(false);
            gen.generateGraph(graph, null);
            return graph;
          };

  public static Graph<String, Integer> getBarbarasiGeneratedGraph() {
    return directed.apply(new BarabasiAlbertGraphGenerator<>(4, 3, 20));
  }

  public static Graph<String, Integer> getPlantedPartitionGeneratedGraph() {
    return directed.apply(new PlantedPartitionGraphGenerator<>(4, 10, .7, .2));
  }

  public static Graph<String, Integer> getGridGeneratedGraph() {
    return directed.apply(new GridGraphGenerator<>(10, 10));
  }

  public static Graph<String, Integer> getPartialGridGeneratedGraph() {
    Graph<String, Integer> graph = directed.apply(new GridGraphGenerator<>(10, 10));
    java.util.List<String> vertices = new ArrayList<>(graph.vertexSet());
    Collections.shuffle(vertices);
    IntStream.range(0, 10).mapToObj(vertices::get).forEach(graph::removeVertex);
    return graph;
  }

  public static Graph<String, Integer> getStarGraphGeneratedGraph() {
    return directed.apply(new StarGraphGenerator<>(21));
  }

  public static Graph<String, Integer> getWheelGraphGeneratedGraph() {
    return directed.apply(new WheelGraphGenerator<>(21));
  }

  public static Graph<String, Integer> getWindmillGraphGeneratedGraph() {
    return directed.apply(
        new WindmillGraphsGenerator<>(WindmillGraphsGenerator.Mode.WINDMILL, 10, 20));
  }

  public static Graph<String, Integer> getDutchWindmillGraphGeneratedGraph() {
    return directed.apply(
        new WindmillGraphsGenerator<>(WindmillGraphsGenerator.Mode.DUTCHWINDMILL, 10, 20));
  }

  public static Graph<String, Integer> getWattsStrogatzGraphGeneratedGraph() {
    return directed.apply(new WattsStrogatzGraphGenerator<>(21, 4, .3));
  }

  public static Graph<String, Integer> getPruferTreeGeneratedGraph() {
    return undirected.apply(new PruferTreeGenerator<>(new int[] {1, 2, 3, 4, 5, 6, 7}));
  }

  public static Graph<String, Integer> getCompleteGraphGeneratedGraph() {
    return directed.apply(new CompleteGraphGenerator<>(21));
  }
}
