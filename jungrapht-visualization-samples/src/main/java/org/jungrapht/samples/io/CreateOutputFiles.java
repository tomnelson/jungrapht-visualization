package org.jungrapht.samples.io;

import static org.jungrapht.visualization.util.Attributed.*;

import java.util.stream.Stream;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ASAILoader;

public class CreateOutputFiles {

  public static void main(String[] args) {

    // make a graph
    Graph<AS, AI> graph =
        GraphTypeBuilder.directed()
            .vertexSupplier(new ASSupplier())
            .allowingSelfLoops(true)
            .allowingMultipleEdges(true)
            .edgeSupplier(new AISupplier())
            .buildGraph();

    // load from json
    ASAILoader.load("marvel-movie-graph.graphml", graph);
    // write to a format
    String location = "jungrapht-visualization-samples/src/main/resources";
    String name = "outfile";
    String[] suffix = {"graphml", "gml", "csv", "col", "json"};
    Stream.of(suffix).forEach(s -> ASAILoader.export(location + "/" + name + "." + s, graph));
  }
}
