package org.jungrapht.samples;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.samples.spatial.RTreeVisualization;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.GeneratedGraphs;
import org.jungrapht.samples.util.LayoutFunction;
import org.jungrapht.samples.util.LayoutHelper;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayout;
import org.jungrapht.visualization.layout.algorithms.util.InitialDimensionFunction;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.util.GraphImage;
import org.jungrapht.visualization.util.LayoutAlgorithmTransition;
import org.jungrapht.visualization.util.LayoutPaintable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Demonstrates several of the graph layout algorithms with generated graphs.
 * Allows the user to interactively select one
 * of several graphs, and one of several layouts, and visualizes the combination.
 *
 */
public class ShowLayoutsWithGeneratedGraphs extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(ShowLayoutsWithGeneratedGraphs.class);

  protected static Graph<String, Integer>[] graphArray;
  protected static int graphIndex;


  LayoutPaintable.BalloonRings balloonLayoutRings;
  LayoutPaintable.RadialRings radialLayoutRings;
  LayoutPaintable.LayoutBounds layoutBounds;

  public ShowLayoutsWithGeneratedGraphs() {

      graphArray = new Graph[GeneratedGraphs.map.size()];

      int i=0;
      for (Map.Entry<String, Supplier<Graph<String, Integer>>> entry : GeneratedGraphs.map.entrySet()) {
          graphArray[i++] = entry.getValue().get();
      }

    Graph<String, Integer> initialGraph = graphArray[3]; // initial graph

    final VisualizationViewer<String, Integer> vv =
        VisualizationViewer.builder(initialGraph)
            .initialDimensionFunction(new InitialDimensionFunction<>())
            .layoutAlgorithm(new KKLayoutAlgorithm<>())
            .build();

    vv.getRenderContext().setVertexLabelFunction(Object::toString);

    vv.setVertexToolTipFunction(
        vertex ->
            vertex
                + ". with neighbors:"
                + Graphs.neighborListOf(vv.getVisualizationModel().getGraph(), vertex));

    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext()
        .setVertexShapeFunction(
            v -> {
              Graph<String, Integer> g = vv.getVisualizationModel().getGraph();
              if (!g.containsVertex(v)) {
                log.error("shapeFunction {} was not in {}", v, g.vertexSet());
              }
              int size = Math.max(5, 2 * (g.containsVertex(v) ? g.degreeOf(v) : 20));
              return new Ellipse2D.Float(-size / 2.f, -size / 2.f, size, size);
            });

    vv.setInitialDimensionFunction(
        InitialDimensionFunction.builder(vv.getRenderContext().getVertexBoundsFunction()).build());

    layoutBounds = new LayoutPaintable.LayoutBounds(vv);
    vv.addPreRenderPaintable(layoutBounds);
    // for the initial layout
    vv.scaleToLayout();

    setLayout(new BorderLayout());
    add(vv.getComponent(), BorderLayout.CENTER);

    final JRadioButton animateLayoutTransition = new JRadioButton("Animate Layout Transition");

    LayoutFunction<String> layoutFunction = new LayoutFunction.FullLayoutFunction<>();

    final JComboBox jcb = new JComboBox(layoutFunction.getNames().toArray());
    jcb.setSelectedItem(LayoutHelper.Layouts.KK);

    jcb.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  vv.getVisualizationModel().getLayoutModel().setPreferredSize(600, 600);
                  vv.reset();
                  LayoutAlgorithm.Builder<String, ?, ?> builder =
                      layoutFunction.apply((String) jcb.getSelectedItem());
                  LayoutAlgorithm<String> layoutAlgorithm = builder.build();
                  vv.removePreRenderPaintable(balloonLayoutRings);
                  vv.removePreRenderPaintable(radialLayoutRings);
                  vv.removePreRenderPaintable(layoutBounds);
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm, vv::scaleToLayout);
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm, vv::scaleToLayout);
                  }
                  if (layoutAlgorithm instanceof BalloonLayoutAlgorithm) {
                    balloonLayoutRings =
                        new LayoutPaintable.BalloonRings(
                            vv, (BalloonLayoutAlgorithm) layoutAlgorithm);
                    vv.addPreRenderPaintable(balloonLayoutRings);
                  }
                  if (layoutAlgorithm instanceof RadialTreeLayout) {
                    radialLayoutRings =
                        new LayoutPaintable.RadialRings(vv, (RadialTreeLayout) layoutAlgorithm);
                    vv.addPreRenderPaintable(radialLayoutRings);
                  }
                  layoutBounds = new LayoutPaintable.LayoutBounds(vv);
                  vv.addPreRenderPaintable(layoutBounds);
                }));

    JPanel control_panel = new JPanel(new GridLayout(2, 1));
    JPanel topControls = new JPanel();
    JPanel bottomControls = new JPanel();
    control_panel.add(topControls);
    control_panel.add(bottomControls);
    add(control_panel, BorderLayout.NORTH);

    final JComboBox graphChooser = new JComboBox(GeneratedGraphs.map.keySet().toArray(new String[0]));
    graphChooser.setSelectedIndex(3);

    graphChooser.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  graphIndex = graphChooser.getSelectedIndex();
                  vv.getVertexSpatial().clear();
                  vv.getEdgeSpatial().clear();
                  vv.getVisualizationModel().getLayoutModel().setSize(600, 600);
                  vv.reset();
                  vv.getVisualizationModel().setGraph(graphArray[graphIndex]);
                  vv.getRenderContext()
                      .setVertexShapeFunction(
                          v -> {
                            int size =
                                Math.max(5, 2 * vv.getVisualizationModel().getGraph().degreeOf(v));
                            return new Ellipse2D.Float(-size / 2.f, -size / 2.f, size, size);
                          });
                }));

    JButton showRTree = new JButton("Show RTree");
    showRTree.addActionListener(e -> RTreeVisualization.showRTree(vv));

    JButton imageButton = new JButton("Save Image");
    imageButton.addActionListener(e -> GraphImage.capture(vv));

    JButton scaleToLayoutButton = new JButton("ScaleToLayout");
    scaleToLayoutButton.addActionListener(evt -> vv.scaleToLayout());

    topControls.add(jcb);
    topControls.add(graphChooser);
    bottomControls.add(animateLayoutTransition);
    bottomControls.add(ControlHelpers.getZoomControls("Zoom", vv));
    bottomControls.add(showRTree);
    bottomControls.add(scaleToLayoutButton);
  }

  LayoutModel getTreeLayoutPositions(Graph tree, LayoutAlgorithm treeLayout) {
    LayoutModel model = LayoutModel.builder().size(600, 600).graph(tree).build();
    model.accept(treeLayout);
    return model;
  }

  public static void main(String[] args) {
    JPanel jp = new ShowLayoutsWithGeneratedGraphs();

    JFrame jf = new JFrame();
    jf.setTitle(jp.getClass().getSimpleName());
    jf.getContentPane().add(jp);
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
