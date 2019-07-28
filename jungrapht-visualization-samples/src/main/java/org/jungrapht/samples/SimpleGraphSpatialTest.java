/*
 * Copyright (c) 2008, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.samples;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.BaseVisualizationModel;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.util.RandomLocationTransformer;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test program to show the SpatialLayout structure and allow users to manipulate the graph ('p'
 * for pick mode, 't' for transform mode) and watch the Spatial structure update
 *
 * @author Tom Nelson
 */
public class SimpleGraphSpatialTest extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(SimpleGraphSpatialTest.class);

  public SimpleGraphSpatialTest() {
    setLayout(new BorderLayout());

    Graph<String, Number> g = TestGraphs.getOneComponentGraph();

    Dimension viewPreferredSize = new Dimension(600, 600);
    Dimension layoutPreferredSize = new Dimension(600, 600);
    LayoutAlgorithm layoutAlgorithm = FRLayoutAlgorithm.builder().build();

    ScalingControl scaler = new CrossoverScalingControl();
    VisualizationModel<String, Number> model =
        BaseVisualizationModel.builder(g)
            .layoutAlgorithm(layoutAlgorithm)
            .initializer(new RandomLocationTransformer(600, 600, System.currentTimeMillis()))
            .layoutSize(layoutPreferredSize)
            .build();
    VisualizationViewer<String, Number> vv =
        VisualizationViewer.builder(model).viewSize(viewPreferredSize).build();
    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
    vv.setGraphMouse(graphMouse);
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    vv.addKeyListener(graphMouse.getModeKeyListener());
    vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");
    vv.setForeground(Color.white);
    vv.scaleToLayout(scaler);
    this.add(vv);
  }

  public static void main(String[] args) {

    // programmatically set the log level so that the spatial grid is drawn for this demo and the SpatialGrid logging is output
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
    LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
    ctx.getLogger("org.jungrapht.visualization.layout.spatial").setLevel(Level.DEBUG);
    ctx.getLogger("org.jungrapht.visualization.BasicVisualizationServer").setLevel(Level.TRACE);
    ctx.getLogger("org.jungrapht.visualization.picking").setLevel(Level.TRACE);

    JFrame jf = new JFrame();

    jf.getContentPane().add(new SimpleGraphSpatialTest());
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
