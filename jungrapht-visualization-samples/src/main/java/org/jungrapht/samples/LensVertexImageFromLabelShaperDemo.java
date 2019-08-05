/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import static org.jungrapht.visualization.renderers.LightweightModalRenderer.Mode.LIGHTWEIGHT;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.Rectangle2D;
import java.util.function.Function;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.annotations.MultiSelectedVertexPaintable;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.control.DefaultLensGraphMouse;
import org.jungrapht.visualization.control.LensGraphMouse;
import org.jungrapht.visualization.control.LensMagnificationGraphMousePlugin;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.decorators.IconShapeFunction;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.renderers.JLabelEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.JLabelVertexLabelRenderer;
import org.jungrapht.visualization.renderers.LightweightVertexRenderer;
import org.jungrapht.visualization.renderers.ModalRenderer;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.MagnifyTransformer;
import org.jungrapht.visualization.transform.shape.MagnifyImageLensSupport;
import org.jungrapht.visualization.transform.shape.MagnifyShapeTransformer;
import org.jungrapht.visualization.util.IconCache;

/** @author Tom Nelson */
public class LensVertexImageFromLabelShaperDemo extends JPanel {

  /** */
  private static final long serialVersionUID = 5432239991020505763L;

  /** the graph */
  Graph<String, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Number> vv;

  LensSupport<LensGraphMouse> magnifyLayoutSupport;
  LensSupport<LensGraphMouse> magnifyViewSupport;
  /** create an instance of a simple graph with controls to demo the zoom features. */
  public LensVertexImageFromLabelShaperDemo() {

    Dimension layoutSize = new Dimension(1300, 1300);
    Dimension viewSize = new Dimension(600, 600);

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    FRLayoutAlgorithm<String> layoutAlgorithm = FRLayoutAlgorithm.<String>builder().build();
    layoutAlgorithm.setMaxIterations(100);
    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .layoutSize(layoutSize)
            .viewSize(viewSize)
            .build();

    Function<String, Paint> vpf =
        new PickableElementPaintFunction<>(vv.getSelectedVertexState(), Color.white, Color.yellow);
    vv.getRenderContext().setVertexFillPaintFunction(vpf);
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv.getSelectedEdgeState(), Color.black, Color.cyan));

    vv.setBackground(Color.white);
    IconCache<String> iconCache =
        new IconCache(
            n -> "<html>This<br>is a<br>multiline<br>label " + n,
            (Function<String, Paint>)
                n -> {
                  if (graph.incomingEdgesOf(n).isEmpty()) return Color.red;
                  if (graph.outgoingEdgesOf(n).isEmpty()) return Color.green;
                  return Color.black;
                });

    vv.getRenderContext().setVertexLabelRenderer(new JLabelVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new JLabelEdgeLabelRenderer(Color.cyan));

    final IconShapeFunction<String> vertexImageShapeFunction =
        new IconShapeFunction<>(new EllipseShapeFunction<>());
    vertexImageShapeFunction.setIconMap(iconCache);

    vv.getRenderContext().setVertexShapeFunction(vertexImageShapeFunction);
    vv.getRenderContext().setVertexIconFunction(iconCache::get);

    vv.addPostRenderPaintable(MultiSelectedVertexPaintable.builder(vv).build());
    // Get the pickedState and add a listener that will decorate the
    //Vertex images with a checkmark icon when they are selected
    MutableSelectedState<String> ps = vv.getSelectedVertexState();

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    final DefaultGraphMouse<String, Number> graphMouse = new DefaultGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    Renderer<String, Number> renderer = vv.getRenderer();
    if (renderer instanceof ModalRenderer) {
      ModalRenderer modalRenderer = (ModalRenderer) renderer;
      Renderer.Vertex<Number, Number> vertexRenderer = modalRenderer.getVertexRenderer(LIGHTWEIGHT);
      // TODO: refactor interfaces
      if (vertexRenderer instanceof LightweightVertexRenderer) {
        LightweightVertexRenderer lightweightVertexRenderer =
            (LightweightVertexRenderer) vertexRenderer;
        lightweightVertexRenderer.setVertexShapeFunction(
            n -> new Rectangle2D.Double(-10, -10, 20, 20));
      }
    }

    final ScalingControl scaler = new CrossoverScalingControl();
    vv.scaleToLayout(scaler);
    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
    scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
    JPanel controls = new JPanel();
    scaleGrid.add(plus);
    scaleGrid.add(minus);
    controls.add(scaleGrid);

    add(controls, BorderLayout.SOUTH);

    LayoutModel<String> layoutModel = vv.getModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());

    Lens lens = new Lens(d);
    lens.setMagnification(2.f);
    magnifyViewSupport =
        new MagnifyImageLensSupport<>(
            vv,
            new MagnifyShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)),
            new DefaultLensGraphMouse<>(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)));
    lens = new Lens(d);
    lens.setMagnification(2.f);
    magnifyLayoutSupport =
        new LayoutLensSupport<>(
            vv,
            new MagnifyTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)),
            new DefaultLensGraphMouse<>(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)));

    ButtonGroup radio = new ButtonGroup();
    JRadioButton none = new JRadioButton("None");
    none.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (magnifyViewSupport != null) {
              magnifyViewSupport.deactivate();
            }
            if (magnifyLayoutSupport != null) {
              magnifyLayoutSupport.deactivate();
            }
          }
        });

    final JRadioButton magnifyView = new JRadioButton("Magnified View");
    magnifyView.addItemListener(
        e ->
            SwingUtilities.invokeLater(
                () -> magnifyViewSupport.activate(e.getStateChange() == ItemEvent.SELECTED)));

    final JRadioButton magnifyModel = new JRadioButton("Magnified Layout");
    magnifyModel.addItemListener(
        e ->
            SwingUtilities.invokeLater(
                () -> magnifyLayoutSupport.activate(e.getStateChange() == ItemEvent.SELECTED)));

    radio.add(none);
    radio.add(magnifyView);
    radio.add(magnifyModel);

    JPanel lensPanel = new JPanel(new GridLayout(2, 0));
    lensPanel.setBorder(BorderFactory.createTitledBorder("Lens"));
    lensPanel.add(none);
    lensPanel.add(magnifyView);
    lensPanel.add(magnifyModel);
    controls.add(lensPanel);
  }

  Integer n = 0;

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new LensVertexImageFromLabelShaperDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
