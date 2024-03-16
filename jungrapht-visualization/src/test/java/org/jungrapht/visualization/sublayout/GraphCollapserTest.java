package org.jungrapht.visualization.sublayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.selection.MultiMutableSelectedState;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class GraphCollapserTest {

  Logger log = LoggerFactory.getLogger(GraphCollapserTest.class);

  List<String> collapsedVertices = new ArrayList<>(List.of("AA", "BB", "CC", "DD", "EE"));

  Supplier<String> collapsedVertexFactory = () -> collapsedVertices.remove(0);

  @Test
  public void testCollapser() {

    Graph<String, Integer> graph = getDemoGraph();

    // graph: ([A, B, C], [0={A,B}, 1={A,C}, 2={B,C}])

    Assert.assertEquals(Set.of("A", "B", "C"), graph.vertexSet());
    Assert.assertEquals(Set.of("B", "A"), endpoints(graph, 0));
    Assert.assertEquals(Set.of("C", "A"), endpoints(graph, 1));
    Assert.assertEquals(Set.of("B", "C"), endpoints(graph, 2));

    GraphCollapser<String, Integer> collapser = new GraphCollapser(graph);
    MultiMutableSelectedState<String> picker = new MultiMutableSelectedState<>();
    picker.select("B");
    picker.select("C");

    Graph<String, Integer> clusterGraph = collapser.getClusterGraph(picker.getSelected());
    Assert.assertEquals(Set.of("B", "C"), clusterGraph.vertexSet());

    String collapsedVertex =
        collapser.collapse(picker.getSelected(), s -> collapsedVertexFactory.get());

    for (String vertex : graph.vertexSet()) {
      Graph<String, Integer> collapsedGraph = collapser.collapsedGraphFunction().apply(vertex);

      if (collapsedGraph != null) {
        Assert.assertEquals(Set.of(2), collapsedGraph.edgeSet());
      } else {
        Assert.assertEquals("A", vertex);
      }
    }

    Assert.assertEquals(Set.of(0, 1), graph.edgeSet());
    for (Integer edge : graph.edgeSet()) {
      Assert.assertEquals("A", graph.getEdgeSource(edge));
      String target = graph.getEdgeTarget(edge);
      Assert.assertEquals(collapsedVertex, target);
      Assert.assertTrue(collapser.collapsedGraphFunction().apply(target) instanceof Graph);
    }

    log.info("collapsed graph is now: {}", graph);
    log.info("collapsedVertex has: {}", clusterGraph);

    picker.clear();
    for (String vertex : graph.vertexSet()) {
      Graph<String, Integer> collapsedGraph = collapser.collapsedGraphFunction().apply(vertex);
      if (collapsedGraph != null) {
        picker.select(vertex);
      }
    }

    // AA is selected to expand
    collapser.expand(picker.getSelected());

    // graph should be the original graph
    //
    Assert.assertEquals(Set.of("A", "B", "C"), graph.vertexSet());
    Assert.assertEquals(Set.of("B", "A"), endpoints(graph, 0));
    Assert.assertEquals(Set.of("C", "A"), endpoints(graph, 1));
    Assert.assertEquals(Set.of("B", "C"), endpoints(graph, 2));
    log.info("expanded graph is now: {}", graph);
    Assert.assertEquals(getDemoGraph(), graph);
  }

  @Test
  public void testTwoConnectedClustersExpandOneThenTheOther() {
    Graph<String, Integer> graph = getDemoGraph2();
    // make a graph of the same type but with Collapsable vertex type

    // graph is: ([A, B, C, D, E, F, G], [0={A,B}, 1={A,C}, 2={B,C}, 3={D,E}, 4={D,F}, 5={E,F}, 6={B,D}, 7={A,G}])
    GraphCollapser<String, Integer> collapser = new GraphCollapser<>(graph);
    MultiMutableSelectedState<String> picker = new MultiMutableSelectedState<>();
    picker.select("A");
    picker.select("B");
    picker.select("C");

    log.debug("graph:" + graph);

    Graph<String, Integer> clusterGraphOne = collapser.getClusterGraph(picker.getSelected());
    // clusterGraphOne: AA -> ([A, B, C], [0={A,B}, 1={A,C}, 2={B,C}])
    String clusterVertexOne =
        collapser.collapse(clusterGraphOne, s -> collapsedVertexFactory.get());

    // graph now: ([D, E, F, G, AA], [3={D,E}, 4={D,F}, 5={E,F}, 6={AA,D}, 7={AA,G}])

    log.debug("clusterVertexOne:" + clusterGraphOne);

    picker.clear();
    picker.select("D");
    picker.select("E");
    picker.select("F");

    Graph<String, Integer> clusterGraphTwo = collapser.getClusterGraph(picker.getSelected());
    // clusterGraphTwo:  BB -> ([D, E, F], [3={D,E}, 4={D,F}, 5={E,F}])
    String clusterVertexTwo =
        collapser.collapse(clusterGraphTwo, s -> collapsedVertexFactory.get());

    // graph now: ([G, AA, BB], [7={AA,G}])
    log.debug("clusterVertexTwo:" + clusterVertexTwo);

    collapser.expand(clusterVertexTwo);

    collapser.expand(clusterVertexOne);
    log.debug("graph now {}", graph);

    Assert.assertEquals(getDemoGraph2(), graph);
  }

  @Test
  public void testMore() {
    Graph<String, Integer> graph = getDemoGraph2();
    // graph is: ([A, B, C, D, E, F, G], [0={A,B}, 1={A,C}, 2={B,C}, 3={D,E}, 4={D,F}, 5={E,F}, 6={B,D}, 7={A,G}])
    GraphCollapser<String, Integer> collapser = new GraphCollapser<>(graph);
    MultiMutableSelectedState<String> picker = new MultiMutableSelectedState<>();
    picker.select("B");
    picker.select("C");
    picker.select("D");

    Graph<String, Integer> clusterGraphOne = collapser.getClusterGraph(picker.getSelected());
    // clusterGraphOne: AA -> ([A, B, C], [0={A,B}, 1={A,C}, 2={B,C}])
    String clusterVertexOne =
        collapser.collapse(clusterGraphOne, s -> collapsedVertexFactory.get());

    picker.clear();
    picker.select(List.of("AA", "E", "F"));

    Graph<String, Integer> clusterGraphTwo = collapser.getClusterGraph(picker.getSelected());
    // clusterGraphOne: AA -> ([A, B, C], [0={A,B}, 1={A,C}, 2={B,C}])
    String clusterVertexTwo =
        collapser.collapse(clusterGraphTwo, s -> collapsedVertexFactory.get());

    picker.clear();
    picker.select(List.of("BB", "A", "G"));

    Graph<String, Integer> clusterGraphThree = collapser.getClusterGraph(picker.getSelected());
    // clusterGraphOne: AA -> ([A, B, C], [0={A,B}, 1={A,C}, 2={B,C}])
    String clusterVertexThree =
        collapser.collapse(clusterGraphThree, s -> collapsedVertexFactory.get());

    log.info("graph is {}", graph);

    collapser.expand(clusterVertexThree);
    log.info("graph is {}", graph);

    collapser.expand(clusterVertexTwo);
    log.info("graph is {}", graph);
    collapser.expand(clusterVertexOne);
    log.info("graph is {}", graph);

    Assert.assertEquals(getDemoGraph2(), graph);
  }

  private static void createEdge(Graph<String, Integer> g, String v1Label, String v2Label) {
    g.addVertex(v1Label);
    g.addVertex(v2Label);
    g.addEdge(v1Label, v2Label);
  }

  public static Graph<String, Integer> getDemoGraph() {

    Graph<String, Integer> g =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.multigraph())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .allowingMultipleEdges(true)
            .allowingSelfLoops(true)
            .buildGraph();
    createEdge(g, "A", "B");
    createEdge(g, "A", "C");
    createEdge(g, "B", "C");

    return g;
  }

  public static Graph<String, Integer> getDemoGraph2() {
    Graph<String, Integer> g =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.multigraph())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .allowingSelfLoops(true)
            .allowingMultipleEdges(true)
            .buildGraph();

    createEdge(g, "A", "B");
    createEdge(g, "A", "C");
    createEdge(g, "B", "C");

    createEdge(g, "D", "E");
    createEdge(g, "D", "F");
    createEdge(g, "E", "F");

    createEdge(g, "B", "D");

    createEdge(g, "A", "G");

    return g;
  }

  private static <V, E> Set<V> endpoints(Graph<V, E> graph, E edge) {
    return Set.of(graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
  }
}
