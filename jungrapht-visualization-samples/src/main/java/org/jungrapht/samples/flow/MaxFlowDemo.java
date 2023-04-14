package org.jungrapht.samples.flow;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.graph.AsWeightedGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;

/**
 * Demo of the EdmondsKarp Max Flow algorithm applied to a graph with a capacity property as edge
 * weight.
 */
public class MaxFlowDemo {

  public MaxFlowDemo() {
    Graph<MyNode, MyLink> graph = constructGraph();
    VisualizationViewer<MyNode, MyLink> vv = VisualizationViewer.builder(graph).build();

    SugiyamaLayoutAlgorithm<MyNode, MyLink> layoutAlgorithm =
        SugiyamaLayoutAlgorithm.<MyNode, MyLink>edgeAwareBuilder().build();

    // the SugiyamaLayoutAlgorithm needs the Vertex sizes
    layoutAlgorithm.setVertexBoundsFunction(vv.getRenderContext().getVertexBoundsFunction());
    // The SugiyamaLayoutAlgorithm needs to be able to set the EdgeShape Function for Articulated edges.
    vv.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm);

    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelDrawPaintFunction(v -> Color.white);
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    // even though the edge shape is set here, it will be changed to EdgeShape.ArticulatedLine by the LayoutAlgorithm
    vv.getRenderContext().setEdgeShapeFunction(new EdgeShape.QuadCurve<>());

    Map<MyLink, Double> edgeFlowMap = new HashMap<>();
    Map<MyLink, MaximumFlowAlgorithm.MaximumFlow<MyLink>> maxFlowMap = new HashMap<>();
    EdmondsKarpMFImpl<MyNode, MyLink> alg = new EdmondsKarpMFImpl(graph);
    graph
        .edgeSet()
        .forEach(
            e -> {
              MyNode source = graph.getEdgeSource(e);
              MyNode target = graph.getEdgeTarget(e);
              edgeFlowMap.put(e, alg.getMaximumFlowValue(source, target));
              maxFlowMap.put(e, alg.getMaximumFlow(source, target));
            });

    vv.getRenderContext()
        .setEdgeLabelFunction(e -> edgeFlowMap.get(e).intValue() + "/" + (int) e.capacity);
    vv.setEdgeToolTipFunction(
        e -> "Flow:" + edgeFlowMap.get(e).intValue() + " / Capacity:" + (int) e.capacity);

    // create a frame to hold the graph visualization
    final JFrame frame = new JFrame();
    frame.getContentPane().add(vv.getComponent());
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

  public Graph<MyNode, MyLink> constructGraph() {
    Graph<MyNode, MyLink> graph =
        new AsWeightedGraph<>(
            GraphTypeBuilder.<MyNode, MyLink>directed().weighted(true).buildGraph(),
            MyLink::capacity,
            true,
            true);

    MyNode n1 = new MyNode(1);
    MyNode n2 = new MyNode(2);
    MyNode n3 = new MyNode(3);
    MyNode n4 = new MyNode(4);
    MyNode n5 = new MyNode(5);
    MyNode n6 = new MyNode(6);
    graph.addVertex(n1);
    graph.addVertex(n2);
    graph.addVertex(n3);
    graph.addVertex(n4);
    graph.addVertex(n5);
    graph.addVertex(n6);

    graph.addEdge(n1, n2, new MyLink(10));
    graph.addEdge(n1, n3, new MyLink(10));
    graph.addEdge(n2, n3, new MyLink(2));
    graph.addEdge(n2, n4, new MyLink(4));
    graph.addEdge(n2, n5, new MyLink(8));
    graph.addEdge(n3, n5, new MyLink(9));
    graph.addEdge(n5, n4, new MyLink(6));
    graph.addEdge(n4, n6, new MyLink(10));
    graph.addEdge(n5, n6, new MyLink(10));
    return graph;
  }

  public static void main(String[] args) {
    new MaxFlowDemo();
  }

  static class MyNode {
    int id;

    public MyNode(int id) {
      this.id = id;
    }

    public String toString() {
      return "V" + id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MyNode myNode = (MyNode) o;
      return id == myNode.id;
    }

    @Override
    public int hashCode() {
      return Objects.hash(id);
    }
  }

  static class MyLink {
    static int edgeCount;
    double capacity;
    int id;

    public MyLink(double capacity) {
      this.id = edgeCount++;
      this.capacity = capacity;
    }

    public double capacity() {
      return this.capacity;
    }

    public String toString() {
      return "E" + id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MyLink myLink = (MyLink) o;
      return id == myLink.id;
    }

    @Override
    public int hashCode() {
      return Objects.hash(id);
    }
  }
}
