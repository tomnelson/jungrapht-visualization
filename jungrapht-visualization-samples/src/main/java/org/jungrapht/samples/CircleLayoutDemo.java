package org.jungrapht.samples;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.samples.util.LayoutHelper;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;

/**
 * Demonstrates two circle layout algorithms. One (the orginal) makes no effort to reduce edge
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
            .layoutSize(new Dimension(900, 900))
            .viewSize(new Dimension(600, 600))
            .build();

    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setEdgeLabelFunction(Object::toString);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());

    vv.setVertexToolTipFunction(Object::toString);
    vv.setEdgeToolTipFunction(Object::toString);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    vv.scaleToLayout();

    final JRadioButton animateLayoutTransition = new JRadioButton("Animate Layout Transition");

    final JComboBox<LayoutHelper.Layouts> jcb =
        new JComboBox<>(
            new LayoutHelper.Layouts[] {
              LayoutHelper.Layouts.CIRCLE, LayoutHelper.Layouts.REDUCE_XING_CIRCLE
            });

    jcb.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  LayoutHelper.Layouts selected = (LayoutHelper.Layouts) jcb.getSelectedItem();
                  LayoutAlgorithm<Integer> layoutAlgorithm = selected.getLayoutAlgorithm();
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
                  }
                }));

    // set layout algorithm via above action
    jcb.setSelectedItem(LayoutHelper.Layouts.CIRCLE);

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
