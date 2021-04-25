package org.jungrapht.samples.experimental;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Triple;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.GraphImporter;
import org.jgrapht.nio.ImportException;
import org.jgrapht.nio.json.JSONEventDrivenImporter;
import org.jgrapht.nio.json.JSONImporter;
import org.jungrapht.visualization.util.Attributed;
import org.jungrapht.visualization.util.DefaultAttributed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributedJSONImporter extends JSONImporter<Attributed<String>, Attributed<Integer>> {

  private static final Logger log = LoggerFactory.getLogger(AttributedJSONImporter.class);

  Triple<String, String, Double> lastTriple;
  Attributed<Integer> lastEdge;

  @Override
  public void importGraph(Graph<Attributed<String>, Attributed<Integer>> graph, Reader input) {
    GraphType graphType = graph.getType();
    Map<String, Attributed<String>> map = new HashMap<>();
    Function<String, Attributed<String>> vertexFactory = v -> new DefaultAttributed<>(v);

    JSONEventDrivenImporter importer = new JSONEventDrivenImporter();
    importer.addVertexConsumer(
        (t) -> {
          if (map.containsKey(t)) {
            throw new ImportException("Node " + t + " already exists");
          }

          Attributed<String> v;
          if (vertexFactory != null) {
            v = vertexFactory.apply(t);
            graph.addVertex(v);
          } else {
            v = graph.addVertex();
          }
          map.put(t, v);
        });
    importer.addVertexAttributeConsumer(
        (p, a) -> {
          String vertex = p.getFirst();
          if (!map.containsKey(vertex)) {
            throw new ImportException("Node " + vertex + " does not exist");
          }
          map.get(vertex).put(p.getSecond(), a.getValue());
        });

    importer.addEdgeConsumer(
        (t) -> {
          String source = t.getFirst();
          Attributed<String> from = map.get(t.getFirst());
          if (from == null) {
            throw new ImportException("Node " + source + " does not exist");
          }

          String target = t.getSecond();
          Attributed<String> to = map.get(target);
          if (to == null) {
            throw new ImportException("Node " + target + " does not exist");
          }

          Attributed<Integer> e = graph.addEdge(from, to);
          if (graphType.isWeighted() && t.getThird() != null) {
            graph.setEdgeWeight(e, t.getThird());
          }
          lastTriple = t;
          lastEdge = e;
        });

    importer.addEdgeAttributeConsumer(
        (p, a) -> {
          Triple<String, String, Double> t = p.getFirst();
          if (t == lastTriple) {
            lastEdge.put(p.getSecond(), a.getValue());
          }
        });
    importer.importInput(input);
  }

  public static void main(String[] args) {
    class EdgeSupplier implements Supplier<Attributed<Integer>> {
      int counter = 0;

      @Override
      public Attributed<Integer> get() {
        return new DefaultAttributed<>(counter++);
      }
    }
    Graph<Attributed<String>, Attributed<Integer>> graph =
        GraphTypeBuilder.<Attributed<String>, Attributed<Integer>>directed()
            .allowingSelfLoops(true)
            .allowingMultipleEdges(true)
            .edgeSupplier(new EdgeSupplier())
            .buildGraph();
    String fileName = "graph.json";
    GraphImporter importer = new AttributedJSONImporter();
    try (InputStreamReader inputStreamReader =
        new InputStreamReader(AttributedJSONImporter.class.getResourceAsStream("/" + fileName))) {
      importer.importGraph(graph, inputStreamReader);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    log.info("graph: {}", graph);
  }
}
