/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.samples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipInputStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.EdgeProvider;
import org.jgrapht.io.GmlImporter;
import org.jgrapht.io.VertexProvider;
import org.jungrapht.samples.util.LayoutHelper;
import org.jungrapht.samples.util.SpanningTreeAdapter;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.LoadingCacheLayoutModel;
import org.jungrapht.visualization.util.BalloonLayoutRings;
import org.jungrapht.visualization.util.RadialLayoutRings;

/**
 * Demonstrates several of the graph layout algorithms. Allows the user to interactively select one
 * of several graphs, and one of several layouts, and visualizes the combination.
 *
 * @author Tom Nelson - extensive modification
 */
@SuppressWarnings("serial")
public class ShowLayoutsWithOneJGraphtIO extends JPanel {

  BalloonLayoutRings balloonLayoutRings;
  RadialLayoutRings radialLayoutRings;

  public ShowLayoutsWithOneJGraphtIO() throws Exception {

    Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

    VertexProvider<String> vp = (label, attributes) -> attributes.toString();
    EdgeProvider<String, DefaultEdge> ep =
        (from, to, label, attributes) -> graph.getEdgeSupplier().get();

    GmlImporter gmlImporter = new GmlImporter(vp, ep);
    URL url = new URL("https://gephi.org/datasets/netscience.gml.zip");
    ZipInputStream zipInputStream = new ZipInputStream(url.openStream());

    if (zipInputStream.getNextEntry() != null) {
      InputStreamReader inputStreamReader = new InputStreamReader(zipInputStream);
      gmlImporter.importGraph(graph, inputStreamReader);
      inputStreamReader.close();
    }

    final VisualizationViewer<String, DefaultEdge> vv =
        VisualizationViewer.builder(graph)
            .layoutSize(new Dimension(1300, 1300))
            .viewSize(new Dimension(600, 600))
            .build();

    vv.getRenderContext().setVertexLabelFunction(Object::toString);

    final DefaultModalGraphMouse<Integer, DefaultEdge> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    vv.setVertexToolTipFunction(
        vertex ->
            vertex.toString()
                + ". with neighbors:"
                + Graphs.neighborListOf(vv.getVisualizationModel().getGraph(), vertex));

    final ScalingControl scaler = new CrossoverScalingControl();
    vv.scaleToLayout(scaler);

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(
        ((DefaultModalGraphMouse<Integer, DefaultEdge>) vv.getGraphMouse()).getModeListener());

    vv.setBackground(Color.WHITE);

    setLayout(new BorderLayout());
    add(vv.getComponent(), BorderLayout.CENTER);
    LayoutHelper.Layouts[] combos = LayoutHelper.getCombos();
    final JRadioButton animateLayoutTransition = new JRadioButton("Animate Layout Transition");

    final JComboBox jcb = new JComboBox(combos);
    jcb.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  LayoutHelper.Layouts layoutType = (LayoutHelper.Layouts) jcb.getSelectedItem();
                  LayoutAlgorithm layoutAlgorithm = layoutType.getLayoutAlgorithm();
                  vv.removePreRenderPaintable(balloonLayoutRings);
                  vv.removePreRenderPaintable(radialLayoutRings);
                  if ((layoutAlgorithm instanceof TreeLayoutAlgorithm)
                      && vv.getVisualizationModel().getGraph().getType().isUndirected()) {
                    Graph tree =
                        SpanningTreeAdapter.getSpanningTree(vv.getVisualizationModel().getGraph());
                    LayoutModel positionModel = this.getTreeLayoutPositions(tree, layoutAlgorithm);
                    vv.getVisualizationModel().getLayoutModel().setInitializer(positionModel);
                    layoutAlgorithm = new StaticLayoutAlgorithm();
                  }
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
                  }
                  if (layoutAlgorithm instanceof BalloonLayoutAlgorithm) {
                    balloonLayoutRings =
                        new BalloonLayoutRings(vv, (BalloonLayoutAlgorithm) layoutAlgorithm);
                    vv.addPreRenderPaintable(balloonLayoutRings);
                  }
                  if (layoutAlgorithm instanceof RadialTreeLayoutAlgorithm) {
                    radialLayoutRings =
                        new RadialLayoutRings(vv, (RadialTreeLayoutAlgorithm) layoutAlgorithm);
                    vv.addPreRenderPaintable(radialLayoutRings);
                  }
                }));

    jcb.setSelectedItem(LayoutHelper.Layouts.FR);

    JPanel control_panel = new JPanel(new GridLayout(2, 1));
    JPanel topControls = new JPanel();
    JPanel bottomControls = new JPanel();
    control_panel.add(topControls);
    control_panel.add(bottomControls);
    add(control_panel, BorderLayout.NORTH);

    JButton showRTree = new JButton("Show RTree");
    showRTree.addActionListener(e -> RTreeVisualization.showRTree(vv));

    topControls.add(jcb);
    bottomControls.add(animateLayoutTransition);
    bottomControls.add(plus);
    bottomControls.add(minus);
    bottomControls.add(modeBox);
    bottomControls.add(showRTree);
  }

  LayoutModel getTreeLayoutPositions(Graph tree, LayoutAlgorithm treeLayout) {
    LayoutModel model = LoadingCacheLayoutModel.builder().size(600, 600).graph(tree).build();
    model.accept(treeLayout);
    return model;
  }

  private Collection getRoots(Graph graph) {
    Set roots = new HashSet<>();
    for (Object v : graph.vertexSet()) {
      if (Graphs.predecessorListOf(graph, v).isEmpty()) {
        roots.add(v);
      }
    }
    return roots;
  }

  public static void main(String[] args) throws Exception {
    JPanel jp = new ShowLayoutsWithOneJGraphtIO();

    JFrame jf = new JFrame();
    jf.setTitle("Guava Graph Visualization");
    jf.getContentPane().add(jp);
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
