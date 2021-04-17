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

    String[] infiles = {"marvel-movie-graph.graphml", "ghidra.json"};
    Stream.of(infiles)
        .forEach(
            f -> {
              // load from json
              ASAILoader.load(f, graph);
              // write to a format
              String location = "generated";
              String name = f.substring(0, f.indexOf('.'));
              String[] suffix = {"graphml", "gml", "csv", "col", "json", "visio"};
              Stream.of(suffix)
                  .forEach(s -> ASAILoader.export(location + "/" + name + "." + s, graph));
            });
  }
}
