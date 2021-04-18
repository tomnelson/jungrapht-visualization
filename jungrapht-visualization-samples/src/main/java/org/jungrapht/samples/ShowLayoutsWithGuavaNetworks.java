package org.jungrapht.samples;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.guava.MutableNetworkAdapter;
import org.jungrapht.samples.spatial.RTreeVisualization;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LayoutFunction;
import org.jungrapht.samples.util.LayoutHelper;
import org.jungrapht.samples.util.TestGuavaNetworks;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.modal.ModeControls;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.util.LayoutAlgorithmTransition;
import org.jungrapht.visualization.util.LayoutPaintable;

/**
 * Demo that visualizes {@link com.google.common.graph.Network}s. Demonstrates several of the graph
 * layout algorithms. Allows the user to interactively select one of several Networks, and one of
 * several layouts, and visualizes the combination.
 *
 * @author Tom Nelson
 */
public class ShowLayoutsWithGuavaNetworks extends JPanel {

  protected static Graph<String, Integer>[] graphArray;
  protected static int graphIndex;
  protected static String[] graphNames = {
    "Two component network",
    "Random mixed-mode network",
    "One component network",
    "Chain+isolate network",
    "Trivial (disconnected) network",
    "Little Network",
    "Generated Network"
  };

  LayoutPaintable.BalloonRings balloonLayoutRings;
  LayoutPaintable.RadialRings radialLayoutRings;

  public ShowLayoutsWithGuavaNetworks() {

    graphArray = new Graph[graphNames.length];

    graphArray[0] = new MutableNetworkAdapter(TestGuavaNetworks.createTestNetwork(false));
    graphArray[1] = new MutableNetworkAdapter(TestGuavaNetworks.getDemoNetwork());
    graphArray[2] = new MutableNetworkAdapter(TestGuavaNetworks.getOneComponentNetwork());
    graphArray[3] = new MutableNetworkAdapter(TestGuavaNetworks.createChainPlusIsolates(18, 5));
    graphArray[4] = new MutableNetworkAdapter(TestGuavaNetworks.createChainPlusIsolates(0, 20));
    MutableNetwork<String, Integer> graph = NetworkBuilder.directed().build();
    int edge = 0;
    graph.addEdge("A", "B", edge++);
    graph.addEdge("A", "C", edge++);

    graphArray[5] = new MutableNetworkAdapter(graph);
    graphArray[6] = new MutableNetworkAdapter(TestGuavaNetworks.getGeneratedNetwork());

    Graph<String, Integer> initialGraph = graphArray[2]; // initial graph

    final DefaultModalGraphMouse<Integer, Number> graphMouse = new DefaultModalGraphMouse<>();

    final VisualizationViewer<String, Integer> vv =
        VisualizationViewer.builder(initialGraph)
            .layoutAlgorithm(new KKLayoutAlgorithm<>())
            .graphMouse(graphMouse)
            .build();

    vv.getRenderContext().setVertexLabelFunction(Object::toString);

    vv.setVertexToolTipFunction(
        vertex ->
            vertex
                + ". with neighbors:"
                + Graphs.neighborListOf(vv.getVisualizationModel().getGraph(), vertex));

    JComboBox modeBox = ModeControls.getStandardModeComboBox(graphMouse);

    setLayout(new BorderLayout());
    add(vv.getComponent(), BorderLayout.CENTER);

    LayoutFunction<String> layoutFunction = new LayoutFunction.FullLayoutFunction<>();

    final JComboBox jcb = new JComboBox(layoutFunction.getNames().toArray());

    final JRadioButton animateLayoutTransition = new JRadioButton("Animate Layout Transition");

    jcb.setSelectedItem(LayoutHelper.Layouts.KK);

    jcb.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  LayoutAlgorithm.Builder<String, ?, ?> builder =
                      layoutFunction.apply((String) jcb.getSelectedItem());
                  LayoutAlgorithm<String> layoutAlgorithm = builder.build();
                  vv.removePreRenderPaintable(balloonLayoutRings);
                  vv.removePreRenderPaintable(radialLayoutRings);
                  if (animateLayoutTransition.isSelected()) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
                  } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
                  }
                  if (layoutAlgorithm instanceof BalloonLayoutAlgorithm) {
                    balloonLayoutRings =
                        new LayoutPaintable.BalloonRings(
                            vv, (BalloonLayoutAlgorithm) layoutAlgorithm);
                    vv.addPreRenderPaintable(balloonLayoutRings);
                  }
                  if (layoutAlgorithm instanceof RadialTreeLayoutAlgorithm) {
                    radialLayoutRings =
                        new LayoutPaintable.RadialRings(
                            vv, (RadialTreeLayoutAlgorithm) layoutAlgorithm);
                    vv.addPreRenderPaintable(radialLayoutRings);
                  }
                }));

    JPanel control_panel = new JPanel(new GridLayout(2, 1));
    JPanel topControls = new JPanel();
    JPanel bottomControls = new JPanel();
    control_panel.add(topControls);
    control_panel.add(bottomControls);
    add(control_panel, BorderLayout.NORTH);

    final JComboBox graphChooser = new JComboBox(graphNames);
    // do this before adding the listener so there is no event fired
    graphChooser.setSelectedIndex(2);

    graphChooser.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  graphIndex = graphChooser.getSelectedIndex();
                  vv.getVertexSpatial().clear();
                  vv.getEdgeSpatial().clear();
                  vv.getVisualizationModel().setGraph(graphArray[graphIndex]);
                }));

    JButton showRTree = new JButton("Show RTree");
    showRTree.addActionListener(e -> RTreeVisualization.showRTree(vv));

    topControls.add(jcb);
    topControls.add(graphChooser);
    bottomControls.add(animateLayoutTransition);
    bottomControls.add(ControlHelpers.getZoomControls("Zoom", vv));
    bottomControls.add(modeBox);
    bottomControls.add(showRTree);
  }

  LayoutModel getTreeLayoutPositions(Graph tree, LayoutAlgorithm treeLayout) {
    LayoutModel model = LayoutModel.builder().size(600, 600).graph(tree).build();
    model.accept(treeLayout);
    return model;
  }

  public static void main(String[] args) {
    JPanel jp = new ShowLayoutsWithGuavaNetworks();

    JFrame jf = new JFrame();
    jf.setTitle(jp.getClass().getSimpleName());
    jf.getContentPane().add(jp);
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
