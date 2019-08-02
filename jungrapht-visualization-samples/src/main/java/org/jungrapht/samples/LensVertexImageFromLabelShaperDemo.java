/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.LayeredIcon;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.annotations.SelectedVertexPaintable;
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
import org.jungrapht.visualization.renderers.Checkmark;
import org.jungrapht.visualization.renderers.DefaultEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.DefaultVertexLabelRenderer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.MagnifyTransformer;
import org.jungrapht.visualization.transform.shape.MagnifyImageLensSupport;
import org.jungrapht.visualization.transform.shape.MagnifyShapeTransformer;
import org.jungrapht.visualization.util.IconCache;
import org.jungrapht.visualization.util.LightweightRenderingVisitor;

/** @author Tom Nelson */
public class LensVertexImageFromLabelShaperDemo extends JPanel {

  /** */
  private static final long serialVersionUID = 5432239991020505763L;

  /** the graph */
  Graph<Number, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Number, Number> vv;

  LensSupport<LensGraphMouse> magnifyLayoutSupport;
  LensSupport<LensGraphMouse> magnifyViewSupport;
  /** create an instance of a simple graph with controls to demo the zoom features. */
  public LensVertexImageFromLabelShaperDemo() {

    Dimension layoutSize = new Dimension(2000, 2000);
    Dimension viewSize = new Dimension(600, 600);

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = createGraph();

    FRLayoutAlgorithm<Number> layoutAlgorithm = FRLayoutAlgorithm.<Number>builder().build();
    layoutAlgorithm.setMaxIterations(100);
    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .layoutSize(layoutSize)
            .viewSize(viewSize)
            .build();

    Function<Number, Paint> vpf =
        new PickableElementPaintFunction<>(vv.getSelectedVertexState(), Color.white, Color.yellow);
    vv.getRenderContext().setVertexFillPaintFunction(vpf);
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv.getSelectedEdgeState(), Color.black, Color.cyan));

    vv.setBackground(Color.white);
    IconCache<Number> iconCache =
        new IconCache(
            n -> "<html>This<br>is a<br>multiline<br>label " + n,
            (Function<Number, Paint>)
                n -> {
                  if (graph.incomingEdgesOf(n).isEmpty()) return Color.red;
                  if (graph.outgoingEdgesOf(n).isEmpty()) return Color.green;
                  return Color.black;
                });

    vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));

    final IconShapeFunction<Number> vertexImageShapeFunction =
        new IconShapeFunction<>(new EllipseShapeFunction<>());
    vertexImageShapeFunction.setIconMap(iconCache);

    vv.getRenderContext().setVertexShapeFunction(vertexImageShapeFunction);
    vv.getRenderContext().setVertexIconFunction(iconCache::get);

    vv.addPostRenderPaintable(SelectedVertexPaintable.builder(vv).build());
    // Get the pickedState and add a listener that will decorate the
    //Vertex images with a checkmark icon when they are selected
    MutableSelectedState<Number> ps = vv.getSelectedVertexState();
    ps.addItemListener(new PickWithIconListener(iconCache::get));

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    final DefaultGraphMouse<Number, Number> graphMouse = new DefaultGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    final ScalingControl scaler = new CrossoverScalingControl();
    vv.scaleToLayout(scaler);
    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    LightweightRenderingVisitor.visit(vv);

    JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
    scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
    JPanel controls = new JPanel();
    scaleGrid.add(plus);
    scaleGrid.add(minus);
    controls.add(scaleGrid);

    add(controls, BorderLayout.SOUTH);

    LayoutModel<Number> layoutModel = vv.getModel().getLayoutModel();
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

  Graph<Number, Number> createGraph() {
    Graph<Number, Number> graph =
        GraphTypeBuilder.<Number, Number>forGraphType(DefaultGraphType.dag())
            .edgeSupplier((Supplier<Number>) () -> n++)
            .buildGraph();

    IntStream.rangeClosed(0, 10).forEach(graph::addVertex);
    graph.addEdge(0, 1);
    graph.addEdge(3, 0);
    graph.addEdge(0, 4);
    graph.addEdge(4, 5);
    graph.addEdge(5, 3);
    graph.addEdge(2, 1);
    graph.addEdge(4, 1);
    graph.addEdge(8, 2);
    graph.addEdge(3, 8);
    graph.addEdge(6, 7);
    graph.addEdge(7, 5);
    graph.addEdge(0, 9);
    graph.addEdge(9, 8);
    graph.addEdge(7, 6);
    graph.addEdge(6, 5);
    graph.addEdge(4, 2);
    graph.addEdge(5, 4);
    graph.addEdge(4, 10);
    graph.addEdge(10, 4);

    return graph;
  }

  public static class PickWithIconListener implements ItemListener {
    Function<Number, Icon> imager;
    Icon checked;

    public PickWithIconListener(Function<Number, Icon> imager) {
      this.imager = imager;
      checked = new Checkmark(Color.red);
    }

    public void itemStateChanged(ItemEvent e) {
      if (e.getItem() instanceof Collection) {
        ((Collection<Number>) e.getItem()).forEach(n -> updatePickIcon(n, e.getStateChange()));
      } else {
        updatePickIcon((Number) e.getItem(), e.getStateChange());
      }
    }

    private void updatePickIcon(Number n, int stateChange) {
      Icon icon = imager.apply(n);
      if (icon instanceof LayeredIcon) {
        if (stateChange == ItemEvent.SELECTED) {
          ((LayeredIcon) icon).add(checked);
        } else {
          ((LayeredIcon) icon).remove(checked);
        }
      }
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new LensVertexImageFromLabelShaperDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
