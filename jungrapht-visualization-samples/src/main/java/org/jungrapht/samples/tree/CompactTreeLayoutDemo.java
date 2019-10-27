/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples.tree;

import java.awt.*;
import java.util.function.Function;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.DemoTreeSupplier;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse.Mode;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.TidierTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.LayoutPaintable;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.util.helpers.ControlHelpers;
import org.jungrapht.visualization.util.helpers.TreeLayoutSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonsrates TreeLayout and RadialTreeLayout.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class CompactTreeLayoutDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(CompactTreeLayoutDemo.class);
  Graph<String, Integer> graph;

  VisualizationViewer<String, Integer> vv;

  public CompactTreeLayoutDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph =
        //            DemoTreeSupplier.createForestForCompactTreeLayout();
        DemoTreeSupplier.createForest();
    //                DemoTreeSupplier.createTreeTwo();
    vv = VisualizationViewer.builder(graph).viewSize(new Dimension(600, 600)).build();
    Function<String, Shape> vertexShapeFunction = vv.getRenderContext().getVertexShapeFunction();
    TidierTreeLayoutAlgorithm<String, Integer> layoutAlgorithm =
        TidierTreeLayoutAlgorithm.<String, Integer>edgeAwareBuilder()
            .vertexShapeFunction(vertexShapeFunction)
            .build();

    //    layoutAlgorithm.setVertexShapeFunction(vertexShapeFunction);
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelDrawPaintFunction(n -> Color.white);
    vv.getRenderContext().setVertexShapeFunction(vertexShapeFunction);
    vv.setVertexToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);
    if (log.isTraceEnabled()) {
      vv.addPreRenderPaintable(
          new LayoutPaintable.LayoutBounds(
              vv.getVisualizationModel(), vv.getRenderContext().getMultiLayerTransformer()));
    }
    vv.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm);
    vv.scaleToLayout();
    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);
    // temporary feature to draw the layout bounds in the viewer
    vv.addPreRenderPaintable(
        new LayoutPaintable.LayoutBounds(
            vv.getVisualizationModel(), vv.getRenderContext().getMultiLayerTransformer()));

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(Mode.TRANSFORMING);

    JPanel layoutPanel = new JPanel(new GridLayout(0, 1));
    layoutPanel.add(
        TreeLayoutSelector.<String, Integer>builder(vv)
            .vertexShapeFunction(vertexShapeFunction)
            .after(vv::scaleToLayout)
            .build());
    JPanel controls = new JPanel();
    controls.add(layoutPanel);
    controls.add(ControlHelpers.getZoomControls(vv));
    controls.add(modeBox);

    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    JPanel demo = new CompactTreeLayoutDemo();
    content.add(demo);
    frame.pack();
    frame.setVisible(true);
    log.trace("frame width {}", frame.getWidth());
    log.trace("demo width {}", demo.getWidth());
  }
}
