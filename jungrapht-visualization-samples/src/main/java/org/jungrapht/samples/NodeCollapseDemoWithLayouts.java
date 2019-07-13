/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
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
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LayoutHelper;
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
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
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
  @SuppressWarnings("rawtypes")
  Graph graph;

  //  enum Layouts {
  //    KAMADA_KAWAI,
  //    FRUCHTERMAN_REINGOLD,
  //    CIRCLE,
  //    SPRING,
  //    SELF_ORGANIZING_MAP
  //  };

  /** the visual component and renderer for the graph */
  @SuppressWarnings("rawtypes")
  VisualizationViewer vv;

  @SuppressWarnings("rawtypes")
  LayoutAlgorithm layoutAlgorithm;

  GraphCollapser collapser;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public NodeCollapseDemoWithLayouts() {
    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    collapser = new GraphCollapser(graph);

    layoutAlgorithm = FRLayoutAlgorithm.builder().build();

    Dimension preferredSize = new Dimension(600, 600);
    final VisualizationModel visualizationModel =
        new BaseVisualizationModel(graph, layoutAlgorithm, preferredSize);
    vv = new VisualizationViewer(visualizationModel, preferredSize);

    vv.getRenderContext().setNodeShapeFunction(new ClusterNodeShapeFunction());

    final Set exclusions = new HashSet();
    final PredicatedParallelEdgeIndexFunction eif =
        new PredicatedParallelEdgeIndexFunction(exclusions::contains);
    vv.getRenderContext().setParallelEdgeIndexFunction(eif);

    vv.setBackground(Color.white);

    // add a listener for ToolTips
    vv.setNodeToolTipFunction(
        v -> {
          if (v instanceof Graph) {
            return ((Graph) v).vertexSet().toString();
          }
          return v;
        });

    /** the regular graph mouse for the normal view */
    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();

    vv.setGraphMouse(graphMouse);

    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    add(gzsp);

    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

    LayoutHelper.Layouts[] combos = LayoutHelper.getCombos();
    final JComboBox jcb = new JComboBox(combos);
    // use a renderer to shorten the layout name presentation
    //        jcb.setRenderer(new DefaultListCellRenderer() {
    //            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    //                String valueString = value.toString();
    //                valueString = valueString.substring(valueString.lastIndexOf('.')+1);
    //                return super.getListCellRendererComponent(list, valueString, index, isSelected,
    //                        cellHasFocus);
    //            }
    //        });
    jcb.addActionListener(
        e -> {
          LayoutHelper.Layouts layoutType = (LayoutHelper.Layouts) jcb.getSelectedItem();
          LayoutAlgorithm layoutAlgorithm = LayoutHelper.createLayout(layoutType);
          //              if (animateLayoutTransition.isSelected()) {
          LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
          //              } else {
          //                LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
          //              }
        });

    jcb.setSelectedItem(LayoutHelper.Layouts.FR);

    //    jcb.addActionListener(new LayoutChooser(jcb, vv));

    jcb.setSelectedItem(LayoutHelper.Layouts.FR);

    JButton collapse = new JButton("Collapse");
    collapse.addActionListener(
        e -> {
          Collection picked = new HashSet(vv.getSelectedNodeState().getSelected());
          if (picked.size() > 1) {
            LayoutModel layoutModel = vv.getModel().getLayoutModel();
            Graph inGraph = vv.getModel().getNetwork();
            Graph clusterGraph = collapser.getClusterGraph(inGraph, picked);
            Graph g = collapser.collapse(inGraph, clusterGraph);
            double sumx = 0;
            double sumy = 0;
            for (Object v : picked) {
              Point p = (Point) layoutModel.apply(v);
              sumx += p.x;
              sumy += p.y;
            }
            Point cp = Point.of(sumx / picked.size(), sumy / picked.size());
            layoutModel.lock(false);
            layoutModel.set(clusterGraph, cp);
            log.trace("put the cluster at " + cp);
            layoutModel.lock(clusterGraph, true);
            vv.getModel().setNetwork(g);
            layoutModel.lock(clusterGraph, false);

            vv.getRenderContext().getParallelEdgeIndexFunction().reset();
            vv.repaint();
          }
        });

    JButton expand = new JButton("Expand");
    expand.addActionListener(
        e -> {
          Collection picked = new HashSet(vv.getSelectedNodeState().getSelected());
          for (Object v : picked) {
            if (v instanceof Graph) {
              Graph inGraph = vv.getModel().getNetwork();
              LayoutModel layoutModel = vv.getModel().getLayoutModel();

              Graph g = collapser.expand(graph, inGraph, (Graph) v);

              vv.getModel().setNetwork(g);

              vv.getRenderContext().getParallelEdgeIndexFunction().reset();
            }
            vv.repaint();
          }
        });

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
          layoutAlgorithm = LayoutHelper.createLayout((LayoutHelper.Layouts) jcb.getSelectedItem());
          LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
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
    controls.add(jcbPanel);
    controls.add(help);
    add(controls, BorderLayout.EAST);
  }

  /**
   * a demo class that will create a node shape that is either a polygon or star. The number of
   * sides corresponds to the number of nodes that were collapsed into the node represented by this
   * shape.
   *
   * @author Tom Nelson
   * @param <N> the node type
   */
  class ClusterNodeShapeFunction<N> extends EllipseNodeShapeFunction<N> {

    ClusterNodeShapeFunction() {
      setSizeTransformer(new ClusterNodeSizeFunction<>(20));
    }

    @Override
    public Shape apply(N v) {
      if (v instanceof Graph) {
        @SuppressWarnings("rawtypes")
        int size = ((Graph) v).vertexSet().size();
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
   *
   * @author Tom Nelson
   * @param <N> the node type
   */
  class ClusterNodeSizeFunction<N> implements Function<N, Integer> {
    int size;

    public ClusterNodeSizeFunction(Integer size) {
      this.size = size;
    }

    public Integer apply(N v) {
      if (v instanceof Graph) {
        return 30;
      }
      return size;
    }
  }

  //  private class LayoutChooser implements ActionListener {
  //    private final JComboBox<?> jcb;
  //
  //    @SuppressWarnings("rawtypes")
  //    private final VisualizationViewer vv;
  //
  //    private LayoutChooser(JComboBox<?> jcb, VisualizationViewer<Object, ?> vv) {
  //      super();
  //      this.jcb = jcb;
  //      this.vv = vv;
  //    }
  //
  //    @SuppressWarnings({"unchecked", "rawtypes"})
  //    public void actionPerformed(ActionEvent arg0) {
  //      Layouts layoutType = (Layouts) jcb.getSelectedItem();
  //
  //      try {
  //        layoutAlgorithm = createLayout(layoutType);
  //        LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
  //        vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
  //        vv.repaint();
  //
  //      } catch (Exception e) {
  //        e.printStackTrace();
  //      }
  //    }
  //  }

  //  private Layouts[] getCombos() {
  //    return Layouts.values();
  //  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new NodeCollapseDemoWithLayouts());
    f.pack();
    f.setVisible(true);
  }
}
