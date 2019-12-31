package org.jungrapht.visualization.layout.algorithms.util;

import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.util.synthetics.SE;
import org.jungrapht.visualization.layout.util.synthetics.SV;
import org.jungrapht.visualization.layout.util.synthetics.SVTransformedGraphSupplier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCircles {

  private static final Logger log = LoggerFactory.getLogger(TestCircles.class);

  Graph<Integer, Integer> graph;
  Graph<SV<Integer>, SE<Integer>> svGraph;

  @Before
  public void before() {
    graph =
        GraphTypeBuilder.<Integer, Integer>undirected()
            .vertexSupplier(SupplierUtil.createIntegerSupplier(1))
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    graph.addVertex();
    graph.addVertex();
    graph.addVertex();
    graph.addVertex();
    graph.addVertex();
    graph.addVertex();
    graph.addVertex();
    graph.addVertex();
    graph.addVertex();
    graph.addVertex(); //10

    graph.addEdge(1, 2);
    graph.addEdge(1, 10);
    graph.addEdge(2, 3);
    graph.addEdge(2, 10);
    graph.addEdge(3, 4);
    graph.addEdge(3, 10);
    graph.addEdge(4, 8);
    graph.addEdge(4, 5);
    graph.addEdge(4, 10);
    graph.addEdge(5, 6);
    graph.addEdge(5, 7);
    graph.addEdge(6, 7);
    graph.addEdge(7, 8);
    graph.addEdge(8, 9);
    graph.addEdge(9, 10);

    log.info("graph: {}", graph);
    svGraph = new SVTransformedGraphSupplier(graph).get();
    log.info("svGraph: {}", svGraph);
  }

  @Test
  public void testCount() {
    CircleLayoutReduceEdgeCrossing cl = new CircleLayoutReduceEdgeCrossing(graph);
    List<Integer> vertexList = List.of(1, 10, 9, 7, 8, 6, 5, 4, 2, 3);
    int count =
        CircleLayoutReduceEdgeCrossing.countCrossings(graph, vertexList.toArray(new Integer[0]));
    Assert.assertEquals(7, count);
  }

  @Test
  public void testThis() {
    CircleLayoutReduceEdgeCrossing<Integer, Integer> circleLayouts =
        new CircleLayoutReduceEdgeCrossing<>(graph);
    int count =
        CircleLayoutReduceEdgeCrossing.countCrossings(
            graph, circleLayouts.getVertexOrderedList().toArray(new Integer[0]));
    Assert.assertEquals(0, count);

    /*

    13:22:26.372 [main] INFO  o.j.v.l.a.util.CircleLayouts - put SV{vertex=1} -> [SV{vertex=2}, SV{vertex=10}]
    13:22:26.372 [main] INFO  o.j.v.l.a.util.CircleLayouts - put SV{vertex=2} -> [SV{vertex=1}, SV{vertex=3}, SV{vertex=10}]
    13:22:26.372 [main] INFO  o.j.v.l.a.util.CircleLayouts - put SV{vertex=3} -> [SV{vertex=2}, SV{vertex=4}, SV{vertex=10}]
    13:22:26.373 [main] INFO  o.j.v.l.a.util.CircleLayouts - put SV{vertex=4} -> [SV{vertex=3}, SV{vertex=8}, SV{vertex=5}, SV{vertex=10}]
    13:22:26.373 [main] INFO  o.j.v.l.a.util.CircleLayouts - put SV{vertex=5} -> [SV{vertex=6}, SV{vertex=7}, SV{vertex=4}]
    13:22:26.373 [main] INFO  o.j.v.l.a.util.CircleLayouts - put SV{vertex=6} -> [SV{vertex=5}, SV{vertex=7}]
    13:22:26.374 [main] INFO  o.j.v.l.a.util.CircleLayouts - put SV{vertex=7} -> [SV{vertex=6}, SV{vertex=5}, SV{vertex=8}]
    13:22:26.374 [main] INFO  o.j.v.l.a.util.CircleLayouts - put SV{vertex=8} -> [SV{vertex=9}, SV{vertex=7}, SV{vertex=4}]
    13:22:26.375 [main] INFO  o.j.v.l.a.util.CircleLayouts - put SV{vertex=9} -> [SV{vertex=8}, SV{vertex=10}]
    13:22:26.375 [main] INFO  o.j.v.l.a.util.CircleLayouts - put SV{vertex=10} -> [SV{vertex=1}, SV{vertex=9}, SV{vertex=2}, SV{vertex=3}, SV{vertex=4}]
    13:22:26.375 [main] INFO  o.j.v.l.a.util.CircleLayouts - table is {
    SV{vertex=1}=[SV{vertex=2}, SV{vertex=10}],
    SV{vertex=2}=[SV{vertex=1}, SV{vertex=3}, SV{vertex=10}],
    SV{vertex=3}=[SV{vertex=2}, SV{vertex=4}, SV{vertex=10}],
    SV{vertex=4}=[SV{vertex=3}, SV{vertex=8}, SV{vertex=5}, SV{vertex=10}],
    SV{vertex=5}=[SV{vertex=6}, SV{vertex=7}, SV{vertex=4}],
    SV{vertex=6}=[SV{vertex=5}, SV{vertex=7}],
    SV{vertex=7}=[SV{vertex=6}, SV{vertex=5}, SV{vertex=8}],
    SV{vertex=8}=[SV{vertex=9}, SV{vertex=7}, SV{vertex=4}],
    SV{vertex=9}=[SV{vertex=8}, SV{vertex=10}],
    SV{vertex=10}=[SV{vertex=1}, SV{vertex=9}, SV{vertex=2}, SV{vertex=3}, SV{vertex=4}]}


    13:30:21.498 [main] INFO  o.j.v.l.a.util.CircleLayouts - table is {
    SV{vertex=1}=[SV{vertex=8}, SV{vertex=10}],
    SV{vertex=2}=[SV{vertex=9}, SV{vertex=7}, SV{vertex=4}],
    SV{vertex=4}=[SV{vertex=3}, SV{vertex=8}, SV{vertex=5}, SV{vertex=10}],
    SV{vertex=10}=[SV{vertex=1}, SV{vertex=9}, SV{vertex=2}, SV{vertex=3}, SV{vertex=4}]}



    13:53:02.110 [main] INFO  o.j.v.l.a.util.CircleLayouts - SV{vertex=1} -> [SV{vertex=2}, SV{vertex=10}]
    13:53:02.110 [main] INFO  o.j.v.l.a.util.CircleLayouts - SV{vertex=6} -> [SV{vertex=5}, SV{vertex=7}]
    13:53:02.110 [main] INFO  o.j.v.l.a.util.CircleLayouts - SV{vertex=9} -> [SV{vertex=8}, SV{vertex=10}]
    13:53:02.110 [main] INFO  o.j.v.l.a.util.CircleLayouts - SV{vertex=2} -> [SV{vertex=1}, SV{vertex=3}, SV{vertex=10}]
    13:53:02.110 [main] INFO  o.j.v.l.a.util.CircleLayouts - SV{vertex=3} -> [SV{vertex=2}, SV{vertex=4}, SV{vertex=10}]
    13:53:02.110 [main] INFO  o.j.v.l.a.util.CircleLayouts - SV{vertex=5} -> [SV{vertex=6}, SV{vertex=7}, SV{vertex=4}]
    13:53:02.110 [main] INFO  o.j.v.l.a.util.CircleLayouts - SV{vertex=7} -> [SV{vertex=6}, SV{vertex=5}, SV{vertex=8}]
    13:53:02.110 [main] INFO  o.j.v.l.a.util.CircleLayouts - SV{vertex=8} -> [SV{vertex=9}, SV{vertex=7}, SV{vertex=4}]
    13:53:02.110 [main] INFO  o.j.v.l.a.util.CircleLayouts - SV{vertex=4} -> [SV{vertex=3}, SV{vertex=8}, SV{vertex=5}, SV{vertex=10}]
    13:53:02.111 [main] INFO  o.j.v.l.a.util.CircleLayouts - SV{vertex=10} -> [SV{vertex=1}, SV{vertex=9}, SV{vertex=2}, SV{vertex=3}, SV{vertex=4}]

             */
  }
}
/*
with undirected as graph type
09:14:02.106 [main] INFO  o.j.v.l.algorithms.util.TestCircles - svGraph: ([SV{vertex=1}, SV{vertex=2}, SV{vertex=3}, SV{vertex=4}, SV{vertex=5}, SV{vertex=6}, SV{vertex=7}, SV{vertex=8}, SV{vertex=9}, SV{vertex=10}], [SE{edge=0}={SV{vertex=1},SV{vertex=2}}, SE{edge=1}={SV{vertex=1},SV{vertex=10}}, SE{edge=2}={SV{vertex=2},SV{vertex=3}}, SE{edge=3}={SV{vertex=2},SV{vertex=10}}, SE{edge=4}={SV{vertex=3},SV{vertex=4}}, SE{edge=5}={SV{vertex=3},SV{vertex=10}}, SE{edge=6}={SV{vertex=4},SV{vertex=8}}, SE{edge=7}={SV{vertex=4},SV{vertex=5}}, SE{edge=8}={SV{vertex=4},SV{vertex=10}}, SE{edge=9}={SV{vertex=5},SV{vertex=6}}, SE{edge=10}={SV{vertex=5},SV{vertex=7}}, SE{edge=11}={SV{vertex=6},SV{vertex=7}}, SE{edge=12}={SV{vertex=7},SV{vertex=8}}, SE{edge=13}={SV{vertex=8},SV{vertex=9}}, SE{edge=14}={SV{vertex=9},SV{vertex=10}}])
09:14:16.461 [main] INFO  o.j.v.l.a.util.CircleLayouts - currentNode: SV{vertex=1} edge v, w: SV{vertex=2},SV{vertex=10} exists
09:14:19.201 [main] INFO  o.j.v.l.a.util.CircleLayouts - currentNode: SV{vertex=2} edge v, w: SV{vertex=3},SV{vertex=10} exists
09:14:19.204 [main] INFO  o.j.v.l.a.util.CircleLayouts - currentNode: SV{vertex=3} edge v, w: SV{vertex=10},SV{vertex=4} exists
09:14:19.207 [main] INFO  o.j.v.l.a.util.CircleLayouts - currentNode: SV{vertex=10} edge v, w: SV{vertex=9},SV{vertex=4} does not exist
09:14:19.209 [main] INFO  o.j.v.l.a.util.CircleLayouts - currentNode: SV{vertex=9} edge v, w: SV{vertex=8},SV{vertex=4} exists
09:14:19.210 [main] INFO  o.j.v.l.a.util.CircleLayouts - currentNode: SV{vertex=8} edge v, w: SV{vertex=4},SV{vertex=7} does not exist
09:14:19.211 [main] INFO  o.j.v.l.a.util.CircleLayouts - currentNode: SV{vertex=4} edge v, w: SV{vertex=5},SV{vertex=7} exists
09:14:19.212 [main] INFO  o.j.v.l.a.util.CircleLayouts - removed losers to get ([SV{vertex=1}, SV{vertex=2}, SV{vertex=3}, SV{vertex=4}, SV{vertex=5}, SV{vertex=6}, SV{vertex=7}, SV{vertex=8}, SV{vertex=9}, SV{vertex=10}], [SE{edge=0}={SV{vertex=1},SV{vertex=2}}, SE{edge=1}={SV{vertex=1},SV{vertex=10}}, SE{edge=2}={SV{vertex=2},SV{vertex=3}}, SE{edge=4}={SV{vertex=3},SV{vertex=4}}, SE{edge=7}={SV{vertex=4},SV{vertex=5}}, SE{edge=9}={SV{vertex=5},SV{vertex=6}}, SE{edge=11}={SV{vertex=6},SV{vertex=7}}, SE{edge=12}={SV{vertex=7},SV{vertex=8}}, SE{edge=13}={SV{vertex=8},SV{vertex=9}}, SE{edge=14}={SV{vertex=9},SV{vertex=10}}])
09:14:19.217 [main] INFO  o.j.v.l.a.util.CircleLayouts - dfi next: SV{vertex=1}
09:14:19.217 [main] INFO  o.j.v.l.a.util.CircleLayouts - dfi next: SV{vertex=10}
09:14:19.217 [main] INFO  o.j.v.l.a.util.CircleLayouts - dfi next: SV{vertex=9}
09:14:19.217 [main] INFO  o.j.v.l.a.util.CircleLayouts - dfi next: SV{vertex=8}
09:14:19.218 [main] INFO  o.j.v.l.a.util.CircleLayouts - dfi next: SV{vertex=7}
09:14:19.218 [main] INFO  o.j.v.l.a.util.CircleLayouts - dfi next: SV{vertex=6}
09:14:19.218 [main] INFO  o.j.v.l.a.util.CircleLayouts - dfi next: SV{vertex=5}
09:14:19.218 [main] INFO  o.j.v.l.a.util.CircleLayouts - dfi next: SV{vertex=4}
09:14:19.218 [main] INFO  o.j.v.l.a.util.CircleLayouts - dfi next: SV{vertex=3}
09:14:19.218 [main] INFO  o.j.v.l.a.util.CircleLayouts - dfi next: SV{vertex=2}





 */
