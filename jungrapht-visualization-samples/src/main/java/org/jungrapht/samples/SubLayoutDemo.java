/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import static org.jungrapht.samples.util.SubLayoutHelper.Layouts;
import static org.jungrapht.samples.util.SubLayoutHelper.getCombos;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.SpanningTreeAdapter;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.DefaultVisualizationModel;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.AggregateLayoutModel;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.LoadingCacheLayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.RandomLocationTransformer;
import org.jungrapht.visualization.selection.MutableSelectedState;

/**
 * Demonstrates the AggregateLayout class. In this demo, vertices are visually clustered as they are
 * selected. The cluster is formed in a new Layout centered at the middle locations of the selected
 * vertices. The layoutSize and layout algorithm for each new cluster is selectable.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class SubLayoutDemo extends JPanel {

  String instructions =
      "<html>"
          + "Use the Layout combobox to select the "
          + "<p>underlying layout."
          + "<p>Use the SubLayout combobox to select "
          + "<p>the type of layout for any clusters you create."
          + "<p>To create clusters, use the mouse to select "
          + "<p>multiple vertices, either by dragging a region, "
          + "<p>or by shift-clicking on multiple vertices."
          + "<p>After you select vertices, use the "
          + "<p>Cluster Picked button to cluster them using the "
          + "<p>layout and layoutSize specified in the Sublayout comboboxen."
          + "<p>Use the Uncluster All button to remove all"
          + "<p>clusters."
          + "<p>You can drag the cluster with the mouse."
          + "<p>Use the 'Picking'/'Transforming' combo-box to switch"
          + "<p>between picking and transforming mode.</html>";
  /** the graph */
  Graph<String, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Number> vv;

  AggregateLayoutModel<String> clusteringLayoutModel;

  Dimension subLayoutSize;

  MutableSelectedState<String> ps;

  @SuppressWarnings("rawtypes")
  LayoutAlgorithm<String> subLayoutType = CircleLayoutAlgorithm.builder().build();

  public SubLayoutDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    // ClusteringLayout is a decorator class that delegates
    // to another layout, but can also separately manage the
    // layout of sub-sets of vertices in circular clusters.
    Dimension preferredSize = new Dimension(600, 600);

    LayoutAlgorithm<String> layoutAlgorithm = FRLayoutAlgorithm.<String>builder().build();
    clusteringLayoutModel =
        new AggregateLayoutModel<>(
            LoadingCacheLayoutModel.<String>builder()
                .graph(graph)
                .size(preferredSize.width, preferredSize.height)
                .build());

    clusteringLayoutModel.accept(layoutAlgorithm);

    final VisualizationModel<String, Number> visualizationModel =
        DefaultVisualizationModel.builder(graph)
            .layoutModel(clusteringLayoutModel)
            .layoutAlgorithm(layoutAlgorithm)
            .build();

    vv = VisualizationViewer.builder(visualizationModel).viewSize(preferredSize).build();

    ps = vv.getSelectedVertexState();
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv.getSelectedEdgeState(), Color.black, Color.red));
    vv.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(
                vv.getSelectedVertexState(), Color.red, Color.yellow));
    vv.setBackground(Color.white);

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);

    // the regular graph mouse for the normal view
    final DefaultModalGraphMouse<?, ?> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);

    VisualizationScrollPane gzsp = new VisualizationScrollPane(vv);
    add(gzsp);

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

    JButton cluster = new JButton("Cluster Picked");
    cluster.addActionListener(e -> clusterPicked());

    JButton uncluster = new JButton("UnCluster All");
    uncluster.addActionListener(e -> uncluster());

    JComboBox<Layouts> layoutTypeComboBox = new JComboBox<>(getCombos());
    layoutTypeComboBox.setRenderer(
        new DefaultListCellRenderer() {
          public Component getListCellRendererComponent(
              JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String valueString = value.toString();
            valueString = valueString.substring(valueString.lastIndexOf('.') + 1);
            return super.getListCellRendererComponent(
                list, valueString, index, isSelected, cellHasFocus);
          }
        });
    layoutTypeComboBox.setSelectedItem(Layouts.FR);
    layoutTypeComboBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            vv.getModel().getLayoutModel().accept(((Layouts) e.getItem()).getLayoutAlgorithm());
          }
        });

    JComboBox<Layouts> subLayoutTypeComboBox = new JComboBox<>(getCombos());

    subLayoutTypeComboBox.setRenderer(
        new DefaultListCellRenderer() {
          public Component getListCellRendererComponent(
              JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String valueString = value.toString();
            valueString = valueString.substring(valueString.lastIndexOf('.') + 1);
            return super.getListCellRendererComponent(
                list, valueString, index, isSelected, cellHasFocus);
          }
        });
    subLayoutTypeComboBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            subLayoutType = (((Layouts) e.getItem()).getLayoutAlgorithm());
            uncluster();
            clusterPicked();
          }
        });

    JComboBox<?> subLayoutDimensionComboBox =
        new JComboBox<Object>(
            new Dimension[] {
              new Dimension(75, 75),
              new Dimension(100, 100),
              new Dimension(150, 150),
              new Dimension(200, 200),
              new Dimension(250, 250),
              new Dimension(300, 300)
            });
    subLayoutDimensionComboBox.setRenderer(
        new DefaultListCellRenderer() {
          public Component getListCellRendererComponent(
              JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String valueString = value.toString();
            valueString = valueString.substring(valueString.lastIndexOf('['));
            valueString = valueString.replaceAll("idth", "");
            valueString = valueString.replaceAll("eight", "");
            return super.getListCellRendererComponent(
                list, valueString, index, isSelected, cellHasFocus);
          }
        });
    subLayoutDimensionComboBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            subLayoutSize = (Dimension) e.getItem();
            uncluster();
            clusterPicked();
          }
        });

    subLayoutDimensionComboBox.setSelectedIndex(1);

    JButton help = new JButton("Help");
    help.addActionListener(
        e ->
            JOptionPane.showMessageDialog(
                (JComponent) e.getSource(), instructions, "Help", JOptionPane.PLAIN_MESSAGE));

    Dimension space = new Dimension(20, 20);
    Box controls = Box.createHorizontalBox();
    controls.add(Box.createRigidArea(space));

    JComponent zoomControls = ControlHelpers.getZoomControls(vv, "Zoom", new GridLayout(0, 1));
    heightConstrain(zoomControls);
    controls.add(zoomControls);
    controls.add(Box.createRigidArea(space));

    JPanel clusterControls = new JPanel(new GridLayout(0, 1));
    clusterControls.setBorder(BorderFactory.createTitledBorder("Clustering"));
    clusterControls.add(cluster);
    clusterControls.add(uncluster);
    heightConstrain(clusterControls);
    controls.add(clusterControls);
    controls.add(Box.createRigidArea(space));

    JPanel layoutControls = new JPanel(new GridLayout(0, 1));
    layoutControls.setBorder(BorderFactory.createTitledBorder("Layout"));
    layoutControls.add(layoutTypeComboBox);
    heightConstrain(layoutControls);
    controls.add(layoutControls);

    JPanel subLayoutControls = new JPanel(new GridLayout(0, 1));
    subLayoutControls.setBorder(BorderFactory.createTitledBorder("SubLayout"));
    subLayoutControls.add(subLayoutTypeComboBox);
    subLayoutControls.add(subLayoutDimensionComboBox);
    heightConstrain(subLayoutControls);
    controls.add(subLayoutControls);
    controls.add(Box.createRigidArea(space));

    JPanel modePanel = new JPanel(new GridLayout(1, 1));
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(modeBox);
    heightConstrain(modePanel);
    controls.add(modePanel);
    controls.add(Box.createRigidArea(space));

    controls.add(help);
    controls.add(Box.createVerticalGlue());
    add(controls, BorderLayout.SOUTH);
  }

  private void heightConstrain(Component component) {
    Dimension d =
        new Dimension(component.getMaximumSize().width, component.getMinimumSize().height);
    component.setMaximumSize(d);
  }

  private void clusterPicked() {
    cluster(true);
  }

  private void uncluster() {
    cluster(false);
  }

  private void cluster(boolean state) {
    if (state) {
      // put the selected vertices into a new sublayout
      Collection<String> picked = ps.getSelected();
      if (picked.size() > 1) {
        Point2D center = new Point2D.Double();
        double x = 0;
        double y = 0;
        for (String vertex : picked) {
          Point p = clusteringLayoutModel.apply(vertex);
          x += p.x;
          y += p.y;
        }
        x /= picked.size();
        y /= picked.size();
        center.setLocation(x, y);

        Graph<String, Number> subGraph;
        try {
          subGraph = GraphTypeBuilder.forGraph(graph).buildGraph();
          for (String vertex : picked) {
            subGraph.addVertex(vertex);
            for (Number edge : graph.edgesOf(vertex)) {
              String vertexU = graph.getEdgeSource(edge);
              String vertexV = graph.getEdgeTarget(edge);
              if (picked.contains(vertexU) && picked.contains(vertexV)) {
                subGraph.addVertex(vertexU);
                subGraph.addVertex(vertexV);
                // put this edge into the subgraph
                subGraph.addEdge(vertexU, vertexV, edge);
              }
            }
          }

          LayoutAlgorithm<String> subLayoutAlgorithm = subLayoutType;

          LayoutModel<String> newLayoutModel =
              LoadingCacheLayoutModel.<String>builder()
                  .graph(subGraph)
                  .size(subLayoutSize.width, subLayoutSize.height)
                  .initializer(
                      new RandomLocationTransformer<>(subLayoutSize.width, subLayoutSize.height, 0))
                  .build();

          if (subLayoutAlgorithm instanceof TreeLayoutAlgorithm) {
            LayoutModel positionModel =
                this.getTreeLayoutPositions(
                    SpanningTreeAdapter.getSpanningTree(vv.getModel().getGraph()),
                    subLayoutAlgorithm);
            newLayoutModel.setInitializer(positionModel);
            subLayoutAlgorithm = new StaticLayoutAlgorithm();
          }

          clusteringLayoutModel.put(newLayoutModel, Point.of(center.getX(), center.getY()));
          newLayoutModel.accept(subLayoutAlgorithm);
          vv.repaint();

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } else {
      // remove all sublayouts
      this.clusteringLayoutModel.removeAll();
      vv.repaint();
    }
  }

  LayoutModel getTreeLayoutPositions(Graph tree, LayoutAlgorithm treeLayout) {
    LayoutModel model = LoadingCacheLayoutModel.builder().size(600, 600).graph(tree).build();
    model.accept(treeLayout);
    return model;
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new SubLayoutDemo());
    f.pack();
    f.setVisible(true);
  }
}
