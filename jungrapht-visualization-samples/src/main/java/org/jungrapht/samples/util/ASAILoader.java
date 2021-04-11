package org.jungrapht.samples.util;

import static org.jungrapht.visualization.util.Attributed.*;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.nio.BaseEventDrivenImporter;
import org.jgrapht.nio.GraphImporter;
import org.jgrapht.nio.csv.CSVImporter;
import org.jgrapht.nio.dimacs.DIMACSImporter;
import org.jgrapht.nio.dot.DOTImporter;
import org.jgrapht.nio.gml.GmlImporter;
import org.jgrapht.nio.graphml.GraphMLImporter;
import org.jgrapht.nio.json.JSONImporter;

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
      clear(graph);
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

  private static GraphImporter setupImporter(String fileName) {
    String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    GraphImporter importer;
    switch (suffix) {
      case "graphml":
        importer = new GraphMLImporter<>();
        ((GraphMLImporter) importer).setSchemaValidation(false);
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
            vertex.put(key, attribute.getValue());
          });
      baseEventDrivenImporter.addEdgeAttributeConsumer(
          (pair, attribute) -> {
            AI edge = pair.getFirst();
            String key = pair.getSecond();
            edge.put(key, attribute.getValue());
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
