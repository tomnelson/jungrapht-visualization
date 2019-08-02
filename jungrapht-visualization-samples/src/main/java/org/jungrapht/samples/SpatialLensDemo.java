/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.geom.AffineTransform;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicLabelUI;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.DefaultVisualizationModel;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.LensMagnificationGraphMousePlugin;
import org.jungrapht.visualization.control.ModalLensGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.transform.HyperbolicTransformer;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.MagnifyTransformer;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.MagnifyShapeTransformer;
import org.jungrapht.visualization.transform.shape.ViewLensSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates the use of <code>HyperbolicTransform</code> and <code>MagnifyTransform</code>
 * applied to either the model (graph layout) or the view (VisualizationViewer) The hyperbolic
 * transform is applied in an elliptical lens that affects that part of the visualization.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class SpatialLensDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(SpatialLensDemo.class);
  /** the graph */
  Graph<String, Number> graph;

  LayoutAlgorithm<String> graphLayoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Number> vv;

  /** provides a Hyperbolic lens for the view */
  LensSupport hyperbolicViewSupport;
  /** provides a magnification lens for the view */
  LensSupport magnifyViewSupport;

  /** provides a Hyperbolic lens for the model */
  LensSupport hyperbolicLayoutSupport;
  /** provides a magnification lens for the model */
  LensSupport magnifyLayoutSupport;

  ScalingControl scaler;

  /** create an instance of a simple graph with controls to demo the zoomand hyperbolic features. */
  public SpatialLensDemo() {
    setLayout(new BorderLayout());
    graph = //buildOneVertex();
        TestGraphs.getOneComponentGraph();

    graphLayoutAlgorithm = FRLayoutAlgorithm.<String>builder().build();

    Dimension preferredSize = new Dimension(600, 600);
    //    Map<String, Point2D> map = new HashMap<>();

    final VisualizationModel<String, Number> visualizationModel =
        DefaultVisualizationModel.builder(graph)
            .layoutAlgorithm(graphLayoutAlgorithm)
            .layoutSize(preferredSize)
            .build();
    vv = VisualizationViewer.builder(visualizationModel).viewSize(preferredSize).build();
    vv.getRenderContext().setVertexLabelFunction(Object::toString);
    vv.setBackground(Color.white);

    vv.getRenderContext().setVertexLabelFunction(Object::toString);

    VisualizationScrollPane gzsp = new VisualizationScrollPane(vv);
    add(gzsp);

    // the regular graph mouse for the normal view
    final DefaultModalGraphMouse<String, Number> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    // create a lens to share between the two hyperbolic transformers
    LayoutModel<String> layoutModel = vv.getModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
    Lens lens = new Lens(d);
    hyperbolicViewSupport =
        new ViewLensSupport<>(
            vv,
            new HyperbolicShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)),
            new ModalLensGraphMouse());
    hyperbolicLayoutSupport =
        new LayoutLensSupport<>(
            vv,
            new HyperbolicTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)),
            new ModalLensGraphMouse());

    // the magnification lens uses a different magnification than the hyperbolic lens
    // create a new one to share between the two magnigy transformers
    lens = new Lens(d);
    lens.setMagnification(3.f);
    magnifyViewSupport =
        new ViewLensSupport<>(
            vv,
            new MagnifyShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)),
            new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)));
    magnifyLayoutSupport =
        new LayoutLensSupport<>(
            vv,
            new MagnifyTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)),
            new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)));
    hyperbolicLayoutSupport
        .getLensTransformer()
        .getLens()
        .setLensShape(hyperbolicViewSupport.getLensTransformer().getLens().getLensShape());
    magnifyViewSupport
        .getLensTransformer()
        .getLens()
        .setLensShape(hyperbolicLayoutSupport.getLensTransformer().getLens().getLensShape());
    magnifyLayoutSupport
        .getLensTransformer()
        .getLens()
        .setLensShape(magnifyViewSupport.getLensTransformer().getLens().getLensShape());

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    ButtonGroup radio = new ButtonGroup();
    JRadioButton normal = new JRadioButton("None");
    normal.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (hyperbolicViewSupport != null) {
              hyperbolicViewSupport.deactivate();
            }
            if (hyperbolicLayoutSupport != null) {
              hyperbolicLayoutSupport.deactivate();
            }
            if (magnifyViewSupport != null) {
              magnifyViewSupport.deactivate();
            }
            if (magnifyLayoutSupport != null) {
              magnifyLayoutSupport.deactivate();
            }
          }
        });

    final JRadioButton hyperView = new JRadioButton("Hyperbolic View");
    hyperView.addItemListener(
        e -> hyperbolicViewSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    final JRadioButton hyperModel = new JRadioButton("Hyperbolic Layout");
    hyperModel.addItemListener(
        e -> hyperbolicLayoutSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    final JRadioButton magnifyView = new JRadioButton("Magnified View");
    magnifyView.addItemListener(
        e -> magnifyViewSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    final JRadioButton magnifyModel = new JRadioButton("Magnified Layout");
    magnifyModel.addItemListener(
        e -> magnifyLayoutSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    JLabel modeLabel = new JLabel("     Mode Menu >>");
    modeLabel.setUI(new VerticalLabelUI(false));
    radio.add(normal);
    radio.add(hyperModel);
    radio.add(hyperView);
    radio.add(magnifyModel);
    radio.add(magnifyView);
    normal.setSelected(true);

    graphMouse.addItemListener(hyperbolicLayoutSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(magnifyLayoutSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(magnifyViewSupport.getGraphMouse().getModeListener());

    JMenuBar menubar = new JMenuBar();
    menubar.add(graphMouse.getModeMenu());
    gzsp.setCorner(menubar);

    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(
        ((DefaultModalGraphMouse<Integer, Number>) vv.getGraphMouse()).getModeListener());

    JRadioButton showSpatialEffects = new JRadioButton("Spatial Structure");
    showSpatialEffects.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            System.err.println("TURNED ON LOGGING");
            // turn on the logging
            // programmatically set the log level so that the spatial grid is drawn for this demo and the SpatialGrid logging is output
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            ctx.getLogger("org.jungrapht.visualization.layout.spatial").setLevel(Level.DEBUG);
            ctx.getLogger("org.jungrapht.visualization.DefaultVisualizationServer")
                .setLevel(Level.TRACE);
            ctx.getLogger("org.jungrapht.visualization.picking").setLevel(Level.TRACE);
            repaint();

          } else if (e.getStateChange() == ItemEvent.DESELECTED) {
            System.err.println("TURNED OFF LOGGING");
            // turn off the logging
            // programmatically set the log level so that the spatial grid is drawn for this demo and the SpatialGrid logging is output
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            ctx.getLogger("org.jungrapht.visualization.layout.spatial").setLevel(Level.INFO);
            ctx.getLogger("org.jungrapht.visualization.DefaultVisualizationServer")
                .setLevel(Level.INFO);
            ctx.getLogger("org.jungrapht.visualization.picking").setLevel(Level.INFO);
            repaint();
          }
        });

    JButton showRTree = new JButton("Show RTree");
    showRTree.addActionListener(e -> RTreeVisualization.showRTree(vv));

    Box controls = Box.createHorizontalBox();
    JPanel zoomControls = new JPanel(new GridLayout(2, 1));
    JPanel modeControls = new JPanel(new GridLayout(3, 1));
    JPanel leftControls = new JPanel();
    JPanel hyperControls = new JPanel(new GridLayout(3, 2));
    hyperControls.setBorder(BorderFactory.createTitledBorder("Examiner Lens"));
    zoomControls.add(plus);
    zoomControls.add(minus);
    modeControls.add(showSpatialEffects);
    modeControls.add(modeBox);
    modeControls.add(showRTree);
    leftControls.add(zoomControls);
    leftControls.add(modeControls);

    hyperControls.add(normal);
    hyperControls.add(new JLabel());

    hyperControls.add(hyperModel);
    hyperControls.add(magnifyModel);

    hyperControls.add(hyperView);
    hyperControls.add(magnifyView);

    controls.add(leftControls);
    controls.add(hyperControls);
    controls.add(modeLabel);

    add(controls, BorderLayout.SOUTH);
  }

  static class VerticalLabelUI extends BasicLabelUI {
    static {
      labelUI = new VerticalLabelUI(false);
    }

    protected boolean clockwise;

    VerticalLabelUI(boolean clockwise) {
      super();
      this.clockwise = clockwise;
    }

    public Dimension getPreferredSize(JComponent c) {
      Dimension dim = super.getPreferredSize(c);
      return new Dimension(dim.height, dim.width);
    }

    private static Rectangle paintIconR = new Rectangle();
    private static Rectangle paintTextR = new Rectangle();
    private static Rectangle paintViewR = new Rectangle();
    private static Insets paintViewInsets = new Insets(0, 0, 0, 0);

    public void paint(Graphics g, JComponent c) {

      JLabel label = (JLabel) c;
      String text = label.getText();
      Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

      if ((icon == null) && (text == null)) {
        return;
      }

      FontMetrics fm = g.getFontMetrics();
      paintViewInsets = c.getInsets(paintViewInsets);

      paintViewR.x = paintViewInsets.left;
      paintViewR.y = paintViewInsets.top;

      // Use inverted height & width
      paintViewR.height = c.getWidth() - (paintViewInsets.left + paintViewInsets.right);
      paintViewR.width = c.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);

      paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
      paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

      String clippedText = layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR);

      Graphics2D g2 = (Graphics2D) g;
      AffineTransform tr = g2.getTransform();
      if (clockwise) {
        g2.rotate(Math.PI / 2);
        g2.translate(0, -c.getWidth());
      } else {
        g2.rotate(-Math.PI / 2);
        g2.translate(-c.getHeight(), 0);
      }

      if (icon != null) {
        icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
      }

      if (text != null) {
        int textX = paintTextR.x;
        int textY = paintTextR.y + fm.getAscent();

        if (label.isEnabled()) {
          paintEnabledText(label, g, clippedText, textX, textY);
        } else {
          paintDisabledText(label, g, clippedText, textX, textY);
        }
      }

      g2.setTransform(tr);
    }
  }

  Graph<String, Number> buildOneVertex() {
    Graph<String, Number> graph =
        GraphTypeBuilder.<String, Number>forGraphType(DefaultGraphType.directedMultigraph())
            .buildGraph();
    graph.addVertex("A");
    return graph;
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new SpatialLensDemo());
    f.pack();
    f.setVisible(true);
  }
}
