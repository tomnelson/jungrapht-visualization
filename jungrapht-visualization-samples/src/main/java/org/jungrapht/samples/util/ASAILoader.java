package org.jungrapht.samples.util;

import static org.jgrapht.nio.graphml.GraphMLExporter.*;
import static org.jungrapht.visualization.util.Attributed.*;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.nio.*;
import org.jgrapht.nio.csv.CSVExporter;
import org.jgrapht.nio.csv.CSVImporter;
import org.jgrapht.nio.dimacs.DIMACSExporter;
import org.jgrapht.nio.dimacs.DIMACSImporter;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.dot.DOTImporter;
import org.jgrapht.nio.gml.GmlExporter;
import org.jgrapht.nio.gml.GmlImporter;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.nio.graphml.GraphMLImporter;
import org.jgrapht.nio.json.JSONExporter;
import org.jgrapht.nio.json.JSONImporter;
import org.jungrapht.visualization.util.Attributed;

public final class ASAILoader {

  private ASAILoader() {}

  public static boolean load(File file, Graph<AS, AI> graph) {
    GraphImporter importer = setupImporter(file.getName());
    if (importer != null) {
      clear(graph);
      try (InputStreamReader inputStreamReader = new FileReader(file)) {
        importer.importGraph(graph, inputStreamReader);
      } catch (Exception ex) {
        ex.printStackTrace();
        return false;
      }
    }
    return true;
  }

  public static boolean load(String fileName, Graph<AS, AI> graph) {
    GraphImporter importer = setupImporter(fileName);
    if (importer != null) {
      try (InputStreamReader inputStreamReader =
          new InputStreamReader(ASAILoader.class.getResourceAsStream("/" + fileName))) {
        importer.importGraph(graph, inputStreamReader);
      } catch (Exception ex) {
        ex.printStackTrace();
        return false;
      }
    }
    return true;
  }

  /**
   * from a collection of Attributed (vertices or edges) gather and return all the attribute keys
   * from all of the vertex or edge attribute maps
   *
   * @param in
   * @return
   */
  private static Set<String> collectKeys(Collection<? extends Attributed<?>> in) {
    return in.stream()
        .map(a -> a.getAttributeMap())
        .flatMap(m -> m.entrySet().stream())
        .map(e -> e.getKey())
        .collect(Collectors.toSet());
  }

  public static boolean export(String fileName, Graph<AS, AI> graph) {
    BaseExporter<AS, AI> exporter = setupExporter(fileName);
    if (exporter instanceof GraphMLExporter) {
      collectKeys(graph.vertexSet())
          .forEach(
              key ->
                  ((GraphMLExporter) exporter)
                      .registerAttribute(key, AttributeCategory.NODE, AttributeType.STRING));

      collectKeys(graph.edgeSet())
          .forEach(
              key ->
                  ((GraphMLExporter) exporter)
                      .registerAttribute(key, AttributeCategory.EDGE, AttributeType.STRING));
    }
    if (exporter != null) {
      try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName))) {
        ((GraphExporter) exporter).exportGraph(graph, writer);
      } catch (Exception ex) {
        ex.printStackTrace();
        return false;
      }
    }
    return true;
  }

  private static BaseExporter<AS, AI> setupExporter(String fileName) {
    String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    BaseExporter<AS, AI> exporter;
    switch (suffix) {
      case "graphml":
        exporter = new GraphMLExporter<>();
        break;
      case "gml":
        exporter = new GmlExporter<>();
        break;
      case "dot":
      case "gv":
        exporter = new DOTExporter<>();
        break;
      case "csv":
        exporter = new CSVExporter<>();
        break;
      case "col":
        exporter = new DIMACSExporter<>();
        break;
      case "json":
        exporter = new JSONExporter<>();
        break;
      default:
        return null;
    }
    exporter.setVertexIdProvider(v -> v.getValue());
    exporter.setVertexAttributeProvider(
        as ->
            as.entrySet()
                .stream()
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new DefaultAttribute(entry.getValue(), AttributeType.STRING))));
    exporter.setEdgeIdProvider(edge -> edge.getValue().toString());
    exporter.setEdgeAttributeProvider(
        ai ->
            ai.entrySet()
                .stream()
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new DefaultAttribute<>(entry.getValue(), AttributeType.STRING))));
    return exporter;
  }

  protected Function<Attributed<Object>, Map<String, Attribute>> attributeProvider =
      ai ->
          ai.entrySet()
              .stream()
              .filter(entry -> null != entry.getValue() && !entry.getValue().isEmpty())
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey,
                      entry -> new DefaultAttribute<>(entry.getValue(), AttributeType.STRING)));

  private static GraphImporter setupImporter(String fileName) {
    String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    GraphImporter<AS, AI> importer;
    switch (suffix) {
      case "graphml":
        importer = new GraphMLImporter<>();
        ((GraphMLImporter<AS, AI>) importer).setSchemaValidation(false);
        ((GraphMLImporter<AS, AI>) importer).setVertexFactory(AS_FUNCTION);
        break;
      case "gml":
        importer = new GmlImporter<>();
        break;
      case "dot":
      case "gv":
        importer = new DOTImporter<>();
        break;
      case "csv":
        importer = new CSVImporter<>();
        break;
      case "col":
        importer = new DIMACSImporter<>();
        break;
      case "json":
        importer = new JSONImporter<>();
        ((JSONImporter<AS, AI>) importer).setVertexFactory(AS_FUNCTION);
        break;
      default:
        return null;
    }
    if (importer instanceof BaseEventDrivenImporter) {
      BaseEventDrivenImporter<AS, AI> baseEventDrivenImporter =
          (BaseEventDrivenImporter<AS, AI>) importer;
      baseEventDrivenImporter.addVertexAttributeConsumer(
          (pair, attribute) -> {
            AS vertex = pair.getFirst();
            String key = pair.getSecond();
            if (!attribute.getValue().isEmpty()) {
              vertex.put(key, attribute.getValue());
            }
          });
      baseEventDrivenImporter.addEdgeAttributeConsumer(
          (pair, attribute) -> {
            AI edge = pair.getFirst();
            String key = pair.getSecond();
            if (!attribute.getValue().isEmpty()) {
              edge.put(key, attribute.getValue());
            }
          });
    }
    return importer;
  }

  public static void clear(Graph graph) {
    Set edges = new HashSet(graph.edgeSet());
    Set vertices = new HashSet(graph.vertexSet());
    edges.forEach(graph::removeEdge);
    vertices.forEach(graph::removeVertex);
  }
}
