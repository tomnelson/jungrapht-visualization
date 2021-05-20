package org.jungrapht.visualization.layout.algorithms.orthogonal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCompactionGraph {

  public static Logger log = LoggerFactory.getLogger(TestCompactionGraph.class);

  @Test
  public void testCreateEdges() {
    Graph<Rectangle, Integer> compactionGraph =
        GraphTypeBuilder.<Rectangle, Integer>directed()
            .allowingMultipleEdges(true)
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    // add the vertices like in the paper
    compactionGraph.addVertex(Rectangle.of(10, 10, 20, 60));
    compactionGraph.addVertex(Rectangle.of(40, 50, 20, 20));
    compactionGraph.addVertex(Rectangle.of(90, 40, 40, 40));
    compactionGraph.addVertex(Rectangle.of(150, 0, 60, 60));
    List<Rectangle> list = new ArrayList<>(compactionGraph.vertexSet());
    list.sort(Comparator.comparingDouble(r -> r.x));
    for (int i = 0; i < list.size(); i++) {
      for (int j = i + 1; j < list.size(); j++) {
        Rectangle ri = list.get(i);
        Rectangle rj = list.get(j);
        if (obstructs(ri, rj)) {
          compactionGraph.addEdge(ri, rj);
        }
      }
    }
    log.info("compactionGraph: {}", compactionGraph);
  }

  boolean obstructs(Rectangle one, Rectangle two) {
    return two.y + two.height > one.y && two.y < one.y + one.height;
  }
}
