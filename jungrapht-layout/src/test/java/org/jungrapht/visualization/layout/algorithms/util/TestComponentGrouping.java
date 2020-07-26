package org.jungrapht.visualization.layout.algorithms.util;

import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestComponentGrouping {

  private static final Logger log = LoggerFactory.getLogger(TestComponentGrouping.class);

  Graph<String, Integer> graph;

  @Before
  public void setup() {
    this.graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.multigraph())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    String[][] pairs = {
      {"a", "b", "3"},
      {"a", "c", "4"},
      {"a", "d", "5"},
      {"d", "c", "6"},
      {"d", "e", "7"},
      {"e", "f", "8"},
      {"f", "g", "9"},
      {"h", "i", "1"}
    };

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
  }

  @Test
  public void testGetAllEdges() {
    List<Graph<String, Integer>> componentList = ComponentGrouping.getComponentGraphs(graph);
    log.info("components are");
    for (Graph<String, Integer> comp : componentList) {
      log.info("{}", comp);
    }
  }
}
