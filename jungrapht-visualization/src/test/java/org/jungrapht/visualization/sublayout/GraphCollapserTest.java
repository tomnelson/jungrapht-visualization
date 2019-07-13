package org.jungrapht.visualization.sublayout;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.selection.MultiMutableSelectedState;
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
    Graph network = getDemoGraph();

    Assert.assertEquals(network.vertexSet(), Sets.newHashSet("A", "B", "C"));
    Assert.assertEquals(endpoints(network, 0), Sets.newHashSet("B", "A"));
    Assert.assertEquals(endpoints(network, 1), Sets.newHashSet("C", "A"));
    Assert.assertEquals(endpoints(network, 2), Sets.newHashSet("B", "C"));

    GraphCollapser collapser = new GraphCollapser(network);
    MultiMutableSelectedState picker = new MultiMutableSelectedState();
    picker.pick("B", true);
    picker.pick("C", true);

    Graph clusterGraph = collapser.getClusterGraph(network, picker.getSelected());
    Graph collapsed = collapser.collapse(network, clusterGraph);
    for (Object node : collapsed.vertexSet()) {
      if (node instanceof Graph) {
        Assert.assertEquals(((Graph) node).edgeSet(), Sets.newHashSet(2));
      } else {
        Assert.assertEquals(node, "A");
      }
    }

    Assert.assertEquals(collapsed.edgeSet(), Sets.newHashSet(0, 1));
    for (Object edge : collapsed.edgeSet()) {
      Assert.assertEquals(collapsed.getEdgeSource(edge), "A");
      Assert.assertTrue(collapsed.getEdgeTarget(edge) instanceof Graph);
    }

    Collection nodes = collapsed.vertexSet();
    picker.clear();
    for (Object node : collapsed.vertexSet()) {
      if (node instanceof Graph) {
        picker.pick(node, true);
      }
    }
    Graph expanded = collapser.expand(network, collapsed, clusterGraph);
    Assert.assertEquals(network.vertexSet(), Sets.newHashSet("A", "B", "C"));
    Assert.assertEquals(endpoints(expanded, 0), Sets.newHashSet("B", "A"));
    Assert.assertEquals(endpoints(expanded, 1), Sets.newHashSet("C", "A"));
    Assert.assertEquals(endpoints(expanded, 2), Sets.newHashSet("B", "C"));
  }

  @Test
  public void testTwoConnectedClustersExpandOneThenTheOther() {
    Graph originalNetwork = getDemoGraph2();
    GraphCollapser collapser = new GraphCollapser(originalNetwork);
    MultiMutableSelectedState picker = new MultiMutableSelectedState();
    picker.pick("A", true);
    picker.pick("B", true);
    picker.pick("C", true);

    log.debug("originalNetwork:" + originalNetwork);

    Graph clusterNodeOne = collapser.getClusterGraph(originalNetwork, picker.getSelected());
    Graph collapsedGraphOne = collapser.collapse(originalNetwork, clusterNodeOne);

    log.debug("collapsedGraphOne:" + collapsedGraphOne);

    picker.clear();
    picker.pick("D", true);
    picker.pick("E", true);
    picker.pick("F", true);

    Graph clusterNodeTwo = collapser.getClusterGraph(collapsedGraphOne, picker.getSelected());
    Graph collapsedGraphTwo = collapser.collapse(collapsedGraphOne, clusterNodeTwo);

    log.debug("collapsedGraphTwo:" + collapsedGraphTwo);

    Graph expanded = collapser.expand(originalNetwork, collapsedGraphTwo, clusterNodeTwo);

    Assert.assertEquals(expanded.edgeSet(), collapsedGraphOne.edgeSet());
    Assert.assertEquals(expanded.vertexSet(), collapsedGraphOne.vertexSet());
    //    Assert.assertEquals(expanded, collapsedGraphOne);

    Graph expandedAgain = collapser.expand(originalNetwork, expanded, clusterNodeOne);

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
