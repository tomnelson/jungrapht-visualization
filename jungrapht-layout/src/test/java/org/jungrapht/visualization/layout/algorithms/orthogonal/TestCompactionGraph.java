package org.jungrapht.visualization.layout.algorithms.orthogonal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.junit.Assert;
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

  @Test
  public void testThreeVerticesInRow() {
    Mappings<String> mappings = new Mappings<>();
    List<Cell<String>> cells = new ArrayList<>();
    cells.add(Cell.of("V1", 1, 1, 1, 1));
    cells.add(Cell.of("V2", 3, 1, 1, 1));
    cells.add(Cell.of("V3", 6, 1, 1, 1));
    Compaction.of(cells, Compaction.Direction.HORIZONTAL, 1, mappings::accept);
    Assert.assertTrue(
            mappings.vertices().containsAll(cells.stream()
                    .map(c -> c.occupant).collect(Collectors.toList())));
    Assert.assertTrue(
            mappings.rectangles().containsAll(
                    List.of(Rectangle.of(1,1,1,1),
                            Rectangle.of(3,1,1,1),
                            Rectangle.of(5,1,1,1))));
    log.info("mappings: {}", mappings);
  }

  @Test
  public void testThreeSizedVerticesInRow() {
    Mappings<String> mappings = new Mappings<>();
    List<Cell<String>> cells = new ArrayList<>();
    cells.add(Cell.of("V1", 1, 1, 1, 1));
    cells.add(Cell.of("V2", 3, 1, 1, 1));
    cells.add(Cell.of("V3", 6, 1, 1, 1));
    Compaction.of(cells, Compaction.Direction.HORIZONTAL, 1,
            v -> Rectangle.of(0,0,5,5), mappings::accept);
    Assert.assertTrue(
            mappings.vertices().containsAll(cells.stream()
                    .map(c -> c.occupant).collect(Collectors.toList())));
    Assert.assertTrue(
            mappings.rectangles().containsAll(
                    List.of(Rectangle.of(1,1,1,1),
                            Rectangle.of(7,1,1,1),
                            Rectangle.of(13,1,1,1))));
    log.info("mappings: {}", mappings);
  }


  @Test
  public void testThreeVerticesInColumn() {
    Mappings<String> mappings = new Mappings<>();
    List<Cell<String>> cells = new ArrayList<>();
    cells.add(Cell.of("V1", 1, 1, 1, 1));
    cells.add(Cell.of("V2", 1, 4, 1, 1));
    cells.add(Cell.of("V3", 1, 6, 1, 1));
    Compaction.of(cells, Compaction.Direction.VERTICAL, 1, mappings::accept);
    Assert.assertTrue(
            mappings.vertices().containsAll(cells.stream()
                    .map(c -> c.occupant).collect(Collectors.toList())));
    Assert.assertTrue(
            mappings.rectangles().containsAll(
                    List.of(Rectangle.of(1,1,1,1),
                            Rectangle.of(1,3,1,1),
                            Rectangle.of(1,5,1,1))));
    log.info("mappings: {}", mappings);
  }


  boolean obstructs(Rectangle one, Rectangle two) {
    return two.y + two.height > one.y && two.y < one.y + one.height;
  }
}
