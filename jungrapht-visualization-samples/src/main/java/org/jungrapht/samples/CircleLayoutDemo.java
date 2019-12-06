package org.jungrapht.samples;

import java.awt.*;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.util.helpers.LayoutFunction;
import org.jungrapht.visualization.util.helpers.LayoutHelper;

/**
 * Demonstrates two circle layout algorithms. One (the orginal) makes no effor to reduce edge
 * crossing. The second applies techniques from
 * (https://www.csd.uoc.gr/~hy583/reviewed_notes/circular.pdf) to reduce edge crossing.
 *
 * @author Tom Nelson
 */
public class CircleLayoutDemo extends JPanel {

  Graph<Integer, Integer> graph;

  VisualizationViewer<Integer, Integer> vv;

  public CircleLayoutDemo() {
    setLayout(new BorderLayout());
    createGraph();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(new CircleLayoutAlgorithm<>())
            .layoutSize(new Dimension(900, 900))
            .viewSize(new Dimension(600, 600))
            .build();

    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setEdgeLabelFunction(Object::toString);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    // add listeners for ToolTips
    vv.setVertexToolTipFunction(Object::toString);
    vv.setEdgeToolTipFunction(Object::toString);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    final ScalingControl scaler = new CrossoverScalingControl();
    vv.scaleToLayout(scaler);

    LayoutFunction<Integer> layoutFunction =
        new LayoutFunction<>(
            LayoutFunction.Layout.of("Random Circle", CircleLayoutAlgorithm.<Integer>builder()),
            LayoutFunction.Layout.of(
                "Reduced Edge Crossing Circle",
                CircleLayoutAlgorithm.<Integer>builder().reduceEdgeCrossing(true)));

    final JRadioButton animateLayoutTransition = new JRadioButton("Animate Layout Transition");

    final JComboBox jcb = new JComboBox(layoutFunction.getNames().toArray());
    jcb.setSelectedItem(LayoutHelper.Layouts.CIRCLE);

    jcb.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  LayoutAlgorithm.Builder<Integer, ?, ?> builder =
                      layoutFunction.apply((String) jcb.getSelectedItem());
                  LayoutAlgorithm layoutAlgorithm = builder.build();
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
                  }
                }));

    Box controls = Box.createHorizontalBox();
    controls.add(jcb);
    controls.add(animateLayoutTransition);
    add(controls, BorderLayout.SOUTH);
  }

  private void createGraph() {
    graph =
        GraphTypeBuilder.<Integer, Integer>undirected()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    // mix up the order
    graph.addVertex(1);
    graph.addVertex(4);
    graph.addVertex(7);
    graph.addVertex(10);
    graph.addVertex(3);
    graph.addVertex(6);
    graph.addVertex(9);
    graph.addVertex(2);
    graph.addVertex(5);
    graph.addVertex(8); //10

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
  }

  public static void main(String[] args) {

    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new CircleLayoutDemo());
    frame.pack();
    frame.setVisible(true);
  }
}