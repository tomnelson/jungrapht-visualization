package org.jungrapht.samples.sugiyama;

import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.util.helpers.ControlHelpers;
import org.jungrapht.visualization.util.helpers.TreeLayoutSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a test graph from the BrandesKopf paper, layers the graph, then manually sets all the
 * synthetic vertices and edges to make a good starting point for the BrandesKopf Horizontal
 * Coordinate Assignment algorithm
 *
 * @author Tom Nelson
 */
public class BrandesKopfSugiyamaGraphExampleWithLayouts extends JFrame {

  private static final Logger log =
      LoggerFactory.getLogger(BrandesKopfSugiyamaGraphExampleWithLayouts.class);

  public BrandesKopfSugiyamaGraphExampleWithLayouts() {

    JPanel container = new JPanel(new BorderLayout());

    Graph<Integer, Integer> graph = createInitialGraph();

    VisualizationViewer<Integer, Integer> vv = configureVisualizationViewer(graph);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());

    TreeLayoutSelector<String, Integer> treeLayoutSelector =
        TreeLayoutSelector.<String, Integer>builder(vv)
            .initialSelection(2)
            .vertexShapeFunction(vv.getRenderContext().getVertexShapeFunction())
            .alignFavoredEdges(false)
            .after(vv::scaleToLayout)
            .build();

    SugiyamaLayoutAlgorithm<Integer, Integer> layoutAlgorithm =
        SugiyamaLayoutAlgorithm.<Integer, Integer>edgeAwareBuilder()
            //                .straightenEdges(false)
            //                .postStraighten(false)
            .after(vv::scaleToLayout)
            .build();
    layoutAlgorithm.setRenderContext(vv.getRenderContext());
    vv.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm);

    container.add(vv.getComponent());

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    add(container);

    Box controls = Box.createHorizontalBox();
    controls.add(ControlHelpers.getCenteredContainer("Layout Controls", treeLayoutSelector));
    add(controls, BorderLayout.SOUTH);

    pack();
    setVisible(true);
  }

  private VisualizationViewer<Integer, Integer> configureVisualizationViewer(
      Graph<Integer, Integer> graph) {
    VisualizationViewer<Integer, Integer> vv =
        VisualizationViewer.builder(graph)
            .layoutSize(new Dimension(600, 600))
            .viewSize(new Dimension(600, 600))
            .build();

    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);

    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelDrawPaintFunction(c -> Color.white);
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    return vv;
  }

  /**
   * creates a graph to look like the one in the paper
   *
   * @return
   */
  Graph<Integer, Integer> createInitialGraph() {

    Graph<Integer, Integer> graph =
        GraphTypeBuilder.<Integer, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .vertexSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    IntStream.rangeClosed(1, 23).forEach(graph::addVertex);
    graph.addEdge(1, 3);
    graph.addEdge(1, 4);
    graph.addEdge(1, 13);
    graph.addEdge(1, 21);

    graph.addEdge(2, 3);
    graph.addEdge(2, 20);

    graph.addEdge(3, 4);
    graph.addEdge(3, 5);
    graph.addEdge(3, 23);

    graph.addEdge(4, 6);

    graph.addEdge(5, 7);

    graph.addEdge(6, 8);
    graph.addEdge(6, 16);
    graph.addEdge(6, 23);

    graph.addEdge(7, 9);

    graph.addEdge(8, 10);
    graph.addEdge(8, 11);

    graph.addEdge(9, 12);

    graph.addEdge(10, 13);
    graph.addEdge(10, 14);
    graph.addEdge(10, 15);

    graph.addEdge(11, 15);
    graph.addEdge(11, 16);

    graph.addEdge(12, 20);

    graph.addEdge(13, 17);

    graph.addEdge(14, 17);
    graph.addEdge(14, 18);
    // no 15 targets

    graph.addEdge(16, 18);
    graph.addEdge(16, 19);
    graph.addEdge(16, 20);

    graph.addEdge(18, 21);

    graph.addEdge(19, 22);

    graph.addEdge(21, 23);

    graph.addEdge(22, 23);
    return graph;
  }

  public static void main(String[] args) {
    new BrandesKopfSugiyamaGraphExampleWithLayouts();
  }
}
