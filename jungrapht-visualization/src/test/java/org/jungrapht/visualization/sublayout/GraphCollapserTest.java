package org.jungrapht.visualization.sublayout;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.selection.MultiMutableSelectedState;
import org.jungrapht.visualization.subLayout.Collapsable;
import org.jungrapht.visualization.subLayout.GraphCollapser;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class GraphCollapserTest {

  Logger log = LoggerFactory.getLogger(GraphCollapserTest.class);

  @Test
  public void testCollapser() {

    Graph<String, Number> generatedGraph = getDemoGraph();
    // make a graph of the same type but with Collapsable vertex types
    Graph<Collapsable<?>, Number> graph =
        GraphTypeBuilder.<Collapsable<?>, Number>forGraphType(generatedGraph.getType())
            .buildGraph();

    for (Number edge : generatedGraph.edgeSet()) {
      Collapsable<?> source = Collapsable.of(generatedGraph.getEdgeSource(edge));
      Collapsable<?> target = Collapsable.of(generatedGraph.getEdgeTarget(edge));
      graph.addVertex(source);
      graph.addVertex(target);
      graph.addEdge(source, target, edge);
    }

    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("A"), Collapsable.of("B"), Collapsable.of("C")),
        graph.vertexSet());
    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("B"), Collapsable.of("A")), endpoints(graph, 0));
    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("C"), Collapsable.of("A")), endpoints(graph, 1));
    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("B"), Collapsable.of("C")), endpoints(graph, 2));

    GraphCollapser<Number> collapser = new GraphCollapser(graph);
    MultiMutableSelectedState picker = new MultiMutableSelectedState();
    picker.select(Collapsable.of("B"));
    picker.select(Collapsable.of("C"));

    Graph<Collapsable<?>, Number> clusterGraph =
        collapser.getClusterGraph(graph, picker.getSelected());
    Graph<Collapsable<?>, Number> collapsed = collapser.collapse(graph, clusterGraph);
    for (Collapsable<?> vertex : collapsed.vertexSet()) {
      if (vertex.get() instanceof Graph) {
        Assert.assertEquals(((Graph) vertex.get()).edgeSet(), Sets.newHashSet(2));
      } else {
        Assert.assertEquals(vertex, Collapsable.of("A"));
      }
    }

    Assert.assertEquals(collapsed.edgeSet(), Sets.newHashSet(0, 1));
    for (Number edge : collapsed.edgeSet()) {
      Assert.assertEquals(Collapsable.of("A"), collapsed.getEdgeSource(edge));
      Assert.assertTrue(collapsed.getEdgeTarget(edge).get() instanceof Graph);
    }

    Collection<Collapsable<?>> vertices = collapsed.vertexSet();
    picker.clear();
    for (Collapsable<?> vertex : collapsed.vertexSet()) {
      if (vertex.get() instanceof Graph) {
        picker.select(vertex);
      }
    }
    Graph<Collapsable<?>, Number> expanded =
        collapser.expand(graph, collapsed, Collapsable.of(clusterGraph));
    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("A"), Collapsable.of("B"), Collapsable.of("C")),
        graph.vertexSet());
    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("B"), Collapsable.of("A")), endpoints(expanded, 0));
    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("C"), Collapsable.of("A")), endpoints(expanded, 1));
    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("B"), Collapsable.of("C")), endpoints(expanded, 2));
  }

  @Test
  public void testTwoConnectedClustersExpandOneThenTheOther() {
    Graph<String, Number> generatedGraph = getDemoGraph2();
    // make a graph of the same type but with Collapsable vertex types
    Graph<Collapsable<?>, Number> originalGraph =
        GraphTypeBuilder.<Collapsable<?>, Number>forGraphType(generatedGraph.getType())
            .buildGraph();

    for (Number edge : generatedGraph.edgeSet()) {
      Collapsable<?> source = Collapsable.of(generatedGraph.getEdgeSource(edge));
      Collapsable<?> target = Collapsable.of(generatedGraph.getEdgeTarget(edge));
      originalGraph.addVertex(source);
      originalGraph.addVertex(target);
      originalGraph.addEdge(source, target, edge);
    }

    GraphCollapser<Number> collapser = new GraphCollapser(originalGraph);
    MultiMutableSelectedState picker = new MultiMutableSelectedState();
    picker.select(Collapsable.of("A"));
    picker.select(Collapsable.of("B"));
    picker.select(Collapsable.of("C"));

    log.debug("originalGraph:" + originalGraph);

    Graph<Collapsable<?>, Number> clusterVertexOne =
        collapser.getClusterGraph(originalGraph, picker.getSelected());
    Graph<Collapsable<?>, Number> collapsedGraphOne =
        collapser.collapse(originalGraph, clusterVertexOne);

    log.debug("collapsedGraphOne:" + collapsedGraphOne);

    picker.clear();
    picker.select(Collapsable.of("D"));
    picker.select(Collapsable.of("E"));
    picker.select(Collapsable.of("F"));

    Graph clusterVertexTwo = collapser.getClusterGraph(collapsedGraphOne, picker.getSelected());
    Graph collapsedGraphTwo = collapser.collapse(collapsedGraphOne, clusterVertexTwo);

    log.debug("collapsedGraphTwo:" + collapsedGraphTwo);

    Graph<Collapsable<?>, Number> expanded =
        collapser.expand(originalGraph, collapsedGraphTwo, Collapsable.of(clusterVertexTwo));

    Assert.assertEquals(expanded.edgeSet(), collapsedGraphOne.edgeSet());
    Assert.assertEquals(expanded.vertexSet(), collapsedGraphOne.vertexSet());
    //    Assert.assertEquals(expanded, collapsedGraphOne);

    Graph expandedAgain =
        collapser.expand(originalGraph, expanded, Collapsable.of(clusterVertexOne));

    Assert.assertEquals(expandedAgain.edgeSet(), originalGraph.edgeSet());
    Assert.assertEquals(expandedAgain.vertexSet(), originalGraph.vertexSet());
    //    Assert.assertEquals(expandedAgain, originalGraph);
  }

  private static void createEdge(
      Graph<String, Number> g, String v1Label, String v2Label, int weight) {
    g.addVertex(v1Label);
    g.addVertex(v2Label);
    g.addEdge(v1Label, v2Label, weight);
  }

  public static Graph<String, Number> getDemoGraph() {

    Graph<String, Number> g =
        GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.multigraph()).buildGraph();
    createEdge(g, "A", "B", 0);
    createEdge(g, "A", "C", 1);
    createEdge(g, "B", "C", 2);

    return g;
  }

  public static Graph<String, Number> getDemoGraph2() {
    Graph<String, Number> g =
        GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.multigraph()).buildGraph();

    createEdge(g, "A", "B", 0);
    createEdge(g, "A", "C", 1);
    createEdge(g, "B", "C", 2);

    createEdge(g, "D", "E", 3);
    createEdge(g, "D", "F", 4);
    createEdge(g, "E", "F", 5);

    createEdge(g, "B", "D", 6);

    createEdge(g, "A", "G", 7);

    return g;
  }

  private static <V, E> Set<V> endpoints(Graph<V, E> graph, E edge) {
    return Sets.newHashSet(graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
  }
}
