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
    // make a graph of the same type but with Collapsable node types
    Graph<Collapsable<?>, Number> network =
        GraphTypeBuilder.<Collapsable<?>, Number>forGraphType(generatedGraph.getType())
            .buildGraph();

    for (Number edge : generatedGraph.edgeSet()) {
      Collapsable<?> source = Collapsable.of(generatedGraph.getEdgeSource(edge));
      Collapsable<?> target = Collapsable.of(generatedGraph.getEdgeTarget(edge));
      network.addVertex(source);
      network.addVertex(target);
      network.addEdge(source, target, edge);
    }

    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("A"), Collapsable.of("B"), Collapsable.of("C")),
        network.vertexSet());
    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("B"), Collapsable.of("A")), endpoints(network, 0));
    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("C"), Collapsable.of("A")), endpoints(network, 1));
    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("B"), Collapsable.of("C")), endpoints(network, 2));

    GraphCollapser<Number> collapser = new GraphCollapser(network);
    MultiMutableSelectedState picker = new MultiMutableSelectedState();
    picker.pick(Collapsable.of("B"), true);
    picker.pick(Collapsable.of("C"), true);

    Graph<Collapsable<?>, Number> clusterGraph =
        collapser.getClusterGraph(network, picker.getSelected());
    Graph<Collapsable<?>, Number> collapsed = collapser.collapse(network, clusterGraph);
    for (Collapsable<?> node : collapsed.vertexSet()) {
      if (node.get() instanceof Graph) {
        Assert.assertEquals(((Graph) node.get()).edgeSet(), Sets.newHashSet(2));
      } else {
        Assert.assertEquals(node, Collapsable.of("A"));
      }
    }

    Assert.assertEquals(collapsed.edgeSet(), Sets.newHashSet(0, 1));
    for (Number edge : collapsed.edgeSet()) {
      Assert.assertEquals(Collapsable.of("A"), collapsed.getEdgeSource(edge));
      Assert.assertTrue(collapsed.getEdgeTarget(edge).get() instanceof Graph);
    }

    Collection<Collapsable<?>> nodes = collapsed.vertexSet();
    picker.clear();
    for (Collapsable<?> node : collapsed.vertexSet()) {
      if (node.get() instanceof Graph) {
        picker.pick(node, true);
      }
    }
    Graph<Collapsable<?>, Number> expanded =
        collapser.expand(network, collapsed, Collapsable.of(clusterGraph));
    Assert.assertEquals(
        Sets.newHashSet(Collapsable.of("A"), Collapsable.of("B"), Collapsable.of("C")),
        network.vertexSet());
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
    // make a graph of the same type but with Collapsable node types
    Graph<Collapsable<?>, Number> originalNetwork =
        GraphTypeBuilder.<Collapsable<?>, Number>forGraphType(generatedGraph.getType())
            .buildGraph();

    for (Number edge : generatedGraph.edgeSet()) {
      Collapsable<?> source = Collapsable.of(generatedGraph.getEdgeSource(edge));
      Collapsable<?> target = Collapsable.of(generatedGraph.getEdgeTarget(edge));
      originalNetwork.addVertex(source);
      originalNetwork.addVertex(target);
      originalNetwork.addEdge(source, target, edge);
    }

    GraphCollapser<Number> collapser = new GraphCollapser(originalNetwork);
    MultiMutableSelectedState picker = new MultiMutableSelectedState();
    picker.pick(Collapsable.of("A"), true);
    picker.pick(Collapsable.of("B"), true);
    picker.pick(Collapsable.of("C"), true);

    log.debug("originalNetwork:" + originalNetwork);

    Graph<Collapsable<?>, Number> clusterNodeOne =
        collapser.getClusterGraph(originalNetwork, picker.getSelected());
    Graph<Collapsable<?>, Number> collapsedGraphOne =
        collapser.collapse(originalNetwork, clusterNodeOne);

    log.debug("collapsedGraphOne:" + collapsedGraphOne);

    picker.clear();
    picker.pick(Collapsable.of("D"), true);
    picker.pick(Collapsable.of("E"), true);
    picker.pick(Collapsable.of("F"), true);

    Graph clusterNodeTwo = collapser.getClusterGraph(collapsedGraphOne, picker.getSelected());
    Graph collapsedGraphTwo = collapser.collapse(collapsedGraphOne, clusterNodeTwo);

    log.debug("collapsedGraphTwo:" + collapsedGraphTwo);

    Graph<Collapsable<?>, Number> expanded =
        collapser.expand(originalNetwork, collapsedGraphTwo, Collapsable.of(clusterNodeTwo));

    Assert.assertEquals(expanded.edgeSet(), collapsedGraphOne.edgeSet());
    Assert.assertEquals(expanded.vertexSet(), collapsedGraphOne.vertexSet());
    //    Assert.assertEquals(expanded, collapsedGraphOne);

    Graph expandedAgain =
        collapser.expand(originalNetwork, expanded, Collapsable.of(clusterNodeOne));

    Assert.assertEquals(expandedAgain.edgeSet(), originalNetwork.edgeSet());
    Assert.assertEquals(expandedAgain.vertexSet(), originalNetwork.vertexSet());
    //    Assert.assertEquals(expandedAgain, originalNetwork);
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

  private static <N, E> Set<N> endpoints(Graph<N, E> graph, E edge) {
    return Sets.newHashSet(graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
  }
}
