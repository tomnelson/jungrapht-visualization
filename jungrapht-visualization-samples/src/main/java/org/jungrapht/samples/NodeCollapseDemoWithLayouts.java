/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LayoutHelper;
import org.jungrapht.samples.util.SpanningTreeAdapter;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.BaseVisualizationModel;
import org.jungrapht.visualization.GraphZoomScrollPane;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.decorators.EllipseNodeShapeFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.LoadingCacheLayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.subLayout.Collapsable;
import org.jungrapht.visualization.subLayout.GraphCollapser;
import org.jungrapht.visualization.util.PredicatedParallelEdgeIndexFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A demo that shows how collections of nodes can be collapsed into a single node. In this demo, the
 * nodes that are collapsed are those mouse-picked by the user. Any criteria could be used to form
 * the node collections to be collapsed, perhaps some common characteristic of those node objects.
 *
 * <p>Note that the collection types don't use generics in this demo, because the nodes are of two
 * types: String for plain nodes, and {@code Network<String, Number>} for the collapsed nodes.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class NodeCollapseDemoWithLayouts extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(NodeCollapseDemoWithLayouts.class);

  String instructions =
      "<html>Use the mouse to select multiple nodes"
          + "<p>either by dragging a region, or by shift-clicking"
          + "<p>on multiple nodes."
          + "<p>After you select nodes, use the Collapse button"
          + "<p>to combine them into a single node."
          + "<p>Select a 'collapsed' node and use the Expand button"
          + "<p>to restore the collapsed nodes."
          + "<p>The Restore button will restore the original graph."
          + "<p>If you select 2 (and only 2) nodes, then press"
          + "<p>the Compress Edges button, parallel edges between"
          + "<p>those two nodes will no longer be expanded."
          + "<p>If you select 2 (and only 2) nodes, then press"
          + "<p>the Expand Edges button, parallel edges between"
          + "<p>those two nodes will be expanded."
          + "<p>You can drag the nodes with the mouse."
          + "<p>Use the 'Picking'/'Transforming' combo-box to switch"
          + "<p>between picking and transforming mode.</html>";
  /** the graph */
  Graph<Collapsable<?>, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Collapsable<?>, Number> vv;

  LayoutAlgorithm<Collapsable<?>> layoutAlgorithm;

  GraphCollapser collapser;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public NodeCollapseDemoWithLayouts() {
    setLayout(new BorderLayout());
    // create a simple graph for the demo
    Graph<String, Number> generatedGraph = TestGraphs.getOneComponentGraph();

    // make a graph of the same type but with Collapsable node types
    this.graph =
        GraphTypeBuilder.<Collapsable<?>, Number>forGraphType(generatedGraph.getType())
            .buildGraph();

    for (Number edge : generatedGraph.edgeSet()) {
      Collapsable<?> source = Collapsable.of(generatedGraph.getEdgeSource(edge));
      Collapsable<?> target = Collapsable.of(generatedGraph.getEdgeTarget(edge));
      this.graph.addVertex(source);
      this.graph.addVertex(target);
      this.graph.addEdge(source, target, edge);
    }

    collapser = new GraphCollapser<>(graph);

    layoutAlgorithm = FRLayoutAlgorithm.<Collapsable<?>>builder().build();

    Dimension preferredSize = new Dimension(400, 400);

    final VisualizationModel<Collapsable<?>, Number> visualizationModel =
        new BaseVisualizationModel<>(graph, layoutAlgorithm, preferredSize);
    vv = new VisualizationViewer<>(visualizationModel, preferredSize);

    vv.getRenderContext().setNodeShapeFunction(new ClusterNodeShapeFunction());

    final Set exclusions = new HashSet();
    final PredicatedParallelEdgeIndexFunction eif =
        new PredicatedParallelEdgeIndexFunction(exclusions::contains);
    vv.getRenderContext().setParallelEdgeIndexFunction(eif);

    vv.setBackground(Color.white);

    // add a listener for ToolTips
    vv.setNodeToolTipFunction(Object::toString);

    // the regular graph mouse for the normal view
    final DefaultModalGraphMouse<Collapsable<?>, Number> graphMouse = new DefaultModalGraphMouse();

    vv.setGraphMouse(graphMouse);

    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    add(gzsp);

    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

    LayoutHelper.Layouts[] combos = LayoutHelper.getCombos();
    final JRadioButton animateLayoutTransition = new JRadioButton("Animate Layout Transition");

    final JComboBox jcb = new JComboBox(combos);
    jcb.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  LayoutHelper.Layouts layoutType = (LayoutHelper.Layouts) jcb.getSelectedItem();
                  LayoutAlgorithm layoutAlgorithm = layoutType.getLayoutAlgorithm();
                  log.trace("got a {}", layoutAlgorithm);
                  if ((layoutAlgorithm instanceof TreeLayoutAlgorithm)
                      && vv.getModel().getNetwork().getType().isUndirected()) {
                    Graph tree = SpanningTreeAdapter.getSpanningTree(vv.getModel().getNetwork());
                    LayoutModel positionModel = this.getTreeLayoutPositions(tree, layoutAlgorithm);
                    vv.getModel().getLayoutModel().setInitializer(positionModel);
                    layoutAlgorithm = new StaticLayoutAlgorithm();
                  }
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
                  }
                }));

    jcb.setSelectedItem(LayoutHelper.Layouts.FR);

    jcb.setSelectedItem(LayoutHelper.Layouts.FR);

    JButton collapse = new JButton("Collapse");
    collapse.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  Collection<Collapsable<?>> picked =
                      new HashSet(vv.getSelectedNodeState().getSelected());
                  if (picked.size() > 1) {
                    Graph<Collapsable<?>, Number> inGraph = vv.getModel().getNetwork();
                    LayoutModel<Collapsable<?>> layoutModel = vv.getModel().getLayoutModel();
                    Graph<Collapsable<?>, Number> clusterGraph =
                        collapser.getClusterGraph(inGraph, picked);
                    log.trace("clusterGraph:" + clusterGraph);
                    Graph<Collapsable<?>, Number> g = collapser.collapse(inGraph, clusterGraph);
                    log.trace("g:" + g);

                    double sumx = 0;
                    double sumy = 0;
                    for (Collapsable<?> v : picked) {
                      Point p = layoutModel.apply(v);
                      sumx += p.x;
                      sumy += p.y;
                    }
                    Point cp = Point.of(sumx / picked.size(), sumy / picked.size());
                    layoutModel
                        .getLayoutStateChangeSupport()
                        .fireLayoutStateChanged(layoutModel, true);
                    layoutModel.lock(false);
                    layoutModel.set(Collapsable.of(clusterGraph), cp);
                    log.trace("put the cluster at " + cp);
                    layoutModel.lock(Collapsable.of(clusterGraph), true);
                    layoutModel.lock(true);
                    vv.getModel().setNetwork(g);

                    vv.getRenderContext().getParallelEdgeIndexFunction().reset();
                    layoutModel.accept(vv.getModel().getLayoutAlgorithm());
                    vv.getSelectedNodeState().clear();

                    vv.repaint();
                  }
                }));

    JButton expand = new JButton("Expand");
    expand.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  Collection<Collapsable<?>> picked =
                      new HashSet(vv.getSelectedNodeState().getSelected());
                  for (Collapsable<?> v : picked) {
                    if (v.get() instanceof Graph) {
                      Graph<Collapsable<?>, Number> inGraph = vv.getModel().getNetwork();
                      LayoutModel<Collapsable<?>> layoutModel = vv.getModel().getLayoutModel();
                      Graph<Collapsable<?>, Number> g =
                          collapser.expand(
                              graph, inGraph, (Collapsable<Graph<Collapsable<?>, Number>>) v);

                      layoutModel.lock(false);
                      vv.getModel().setNetwork(g);

                      vv.getRenderContext().getParallelEdgeIndexFunction().reset();
                    }
                    vv.getSelectedNodeState().clear();
                    vv.repaint();
                  }
                }));

    JButton compressEdges = new JButton("Compress Edges");
    compressEdges.addActionListener(
        e -> {
          Set picked = vv.getSelectedNodeState().getSelected();
          if (picked.size() == 2) {
            Iterator pickedIter = picked.iterator();
            Object nodeU = pickedIter.next();
            Object nodeV = pickedIter.next();
            Graph graph = vv.getModel().getNetwork();
            Collection edges = new HashSet(graph.edgesOf(nodeU));
            edges.retainAll(graph.edgesOf(nodeV));
            exclusions.addAll(edges);
            vv.repaint();
          }
        });

    JButton expandEdges = new JButton("Expand Edges");
    expandEdges.addActionListener(
        e -> {
          Set picked = vv.getSelectedNodeState().getSelected();
          if (picked.size() == 2) {
            Iterator pickedIter = picked.iterator();
            Object nodeU = pickedIter.next();
            Object nodeV = pickedIter.next();
            Graph graph = vv.getModel().getNetwork();
            Collection edges = new HashSet(graph.edgesOf(nodeU));
            edges.retainAll(graph.edgesOf(nodeV));
            exclusions.removeAll(edges);
            vv.repaint();
          }
        });

    JButton reset = new JButton("Reset");
    reset.addActionListener(
        e -> {
          vv.getModel().setNetwork(graph);
          exclusions.clear();
          vv.repaint();
        });

    JButton help = new JButton("Help");
    help.addActionListener(
        e ->
            JOptionPane.showMessageDialog(
                (JComponent) e.getSource(), instructions, "Help", JOptionPane.PLAIN_MESSAGE));

    JPanel controls = new JPanel();
    controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));
    JPanel collapseControls = new JPanel(new GridLayout(0, 1));
    collapseControls.setBorder(BorderFactory.createTitledBorder("Picked"));
    collapseControls.add(collapse);
    collapseControls.add(expand);
    collapseControls.add(compressEdges);
    collapseControls.add(expandEdges);
    collapseControls.add(reset);
    controls.add(collapseControls);
    JPanel modePanel = new JPanel();
    modePanel.add(modeBox);
    controls.add(modePanel);
    JPanel jcbPanel = new JPanel();
    jcbPanel.add(jcb);
    jcbPanel.add(animateLayoutTransition);
    controls.add(jcbPanel);
    controls.add(help);
    add(controls, BorderLayout.EAST);
  }

  LayoutModel getTreeLayoutPositions(Graph tree, LayoutAlgorithm treeLayout) {
    LayoutModel model = LoadingCacheLayoutModel.builder().size(600, 600).graph(tree).build();
    model.accept(treeLayout);
    return model;
  }

  /**
   * a demo class that will create a node shape that is either a polygon or star. The number of
   * sides corresponds to the number of nodes that were collapsed into the node represented by this
   * shape.
   */
  class ClusterNodeShapeFunction extends EllipseNodeShapeFunction<Collapsable<?>> {

    ClusterNodeShapeFunction() {
      setSizeTransformer(new ClusterNodeSizeFunction(20));
    }

    @Override
    public Shape apply(Collapsable<?> v) {
      if (v.get() instanceof Graph) {
        int size = ((Graph) v.get()).vertexSet().size();
        if (size < 8) {
          int sides = Math.max(size, 3);
          return factory.getRegularPolygon(v, sides);
        } else {
          return factory.getRegularStar(v, size);
        }
      }
      return super.apply(v);
    }
  }

  /**
   * A demo class that will make nodes larger if they represent a collapsed collection of original
   * nodes
   */
  class ClusterNodeSizeFunction implements Function<Collapsable<?>, Integer> {
    int size;

    public ClusterNodeSizeFunction(Integer size) {
      this.size = size;
    }

    public Integer apply(Collapsable<?> v) {
      if (v.get() instanceof Graph) {
        return 30;
      }
      return size;
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new NodeCollapseDemoWithLayouts());
    f.pack();
    f.setVisible(true);
  }
}
