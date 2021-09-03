/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import static org.jungrapht.visualization.renderers.BiModalRenderer.LIGHTWEIGHT;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import org.jgrapht.Graph;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LensControlHelper;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.annotations.MultiSelectedVertexPaintable;
import org.jungrapht.visualization.control.*;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.decorators.IconShapeFunction;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.renderers.JLabelEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.JLabelVertexLabelRenderer;
import org.jungrapht.visualization.renderers.LightweightVertexRenderer;
import org.jungrapht.visualization.renderers.ModalRenderer;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.MagnifyTransformer;
import org.jungrapht.visualization.transform.shape.MagnifyImageLensSupport;
import org.jungrapht.visualization.transform.shape.MagnifyShapeTransformer;
import org.jungrapht.visualization.util.IconCache;

/**
 * Displays a graph using multi-line JLabel images as vertices. The Magnifier will magnify the
 * images in the lens. Multiple vertex selection (CTRL mouse button 1 drag) will trace an arbitrary
 * shape (instead of a Rectangle) and vertices within the shape will be selected
 *
 * @author Tom Nelson
 */
public class LensVertexImageFromLabelShaperDemo extends JPanel {

  /** */
  private static final long serialVersionUID = 5432239991020505763L;

  /** the graph */
  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  LensSupport<LensGraphMouse> magnifyLayoutSupport;
  LensSupport<LensGraphMouse> magnifyViewSupport;
  /** create an instance of a simple graph with controls to demo the zoom features. */
  public LensVertexImageFromLabelShaperDemo() {

    Dimension layoutSize = new Dimension(1300, 1300);
    Dimension viewSize = new Dimension(600, 600);

    setLayout(new BorderLayout());

    graph = TestGraphs.getOneComponentGraph();

    FRLayoutAlgorithm<String> layoutAlgorithm =
        FRLayoutAlgorithm.<String>builder().maxIterations(100).build();

    final DefaultGraphMouse<String, Integer> graphMouse = new DefaultGraphMouse();

    vv =
        VisualizationViewer.builder(graph)
            .graphMouse(graphMouse)
            .layoutAlgorithm(layoutAlgorithm)
            .multiSelectionStrategySupplier(() -> MultiSelectionStrategy.arbitrary())
            .layoutSize(layoutSize)
            .viewSize(viewSize)
            .build();

    Function<String, Paint> vpf =
        new PickableElementPaintFunction<>(vv.getSelectedVertexState(), Color.white, Color.yellow);
    vv.getRenderContext().setVertexFillPaintFunction(vpf);
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv.getSelectedEdgeState(), Color.black, Color.cyan));

    IconCache<String> iconCache =
        IconCache.<String>builder(n -> "<html>This<br>is a<br>multiline<br>label " + n + "</html>")
            .vertexShapeFunction(vv.getRenderContext().getVertexShapeFunction())
            .colorFunction(
                n -> {
                  if (graph.degreeOf(n) > 9) return Color.red;
                  if (graph.degreeOf(n) < 7) return Color.green;
                  return Color.lightGray;
                })
            .stylist(
                (label, vertex, colorFunction) -> {
                  label.setFont(new Font("Serif", Font.BOLD, 20));
                  label.setForeground(Color.black);
                  label.setBackground(Color.white);
                  //                  label.setOpaque(true);
                  Border lineBorder =
                      BorderFactory.createEtchedBorder(); //Border(BevelBorder.RAISED);
                  Border marginBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
                  label.setBorder(new CompoundBorder(lineBorder, marginBorder));
                })
            .preDecorator(
                (graphics, vertex, labelBounds, vertexShapeFunction, colorFunction) -> {
                  Color color = (Color) colorFunction.apply(vertex);
                  color =
                      new Color(
                          color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 4);
                  // save off the old color
                  Color oldColor = graphics.getColor();
                  // fill the image background with white
                  graphics.setPaint(Color.white);
                  graphics.fill(labelBounds);

                  Shape shape = vertexShapeFunction.apply(vertex);
                  Rectangle shapeBounds = shape.getBounds();

                  AffineTransform scale =
                      AffineTransform.getScaleInstance(
                          1.3 * labelBounds.width / shapeBounds.getWidth(),
                          1.3 * labelBounds.height / shapeBounds.getHeight());
                  AffineTransform translate =
                      AffineTransform.getTranslateInstance(
                          labelBounds.width / 2, labelBounds.height / 2);
                  translate.concatenate(scale);
                  shape = translate.createTransformedShape(shape);
                  graphics.setColor(Color.pink);
                  graphics.fill(shape);
                  graphics.setColor(oldColor);
                })
            .build();

    vv.getRenderContext().setVertexLabelRenderer(new JLabelVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new JLabelEdgeLabelRenderer(Color.cyan));

    final IconShapeFunction<String> vertexImageShapeFunction =
        new IconShapeFunction<>(new EllipseShapeFunction<>());
    vertexImageShapeFunction.setIconFunction(iconCache);
    vv.getRenderContext().setVertexShapeFunction(vertexImageShapeFunction);
    vv.getRenderContext().setVertexIconFunction(iconCache);

    vv.addPostRenderPaintable(
        MultiSelectedVertexPaintable.builder(vv)
            .selectionPaint(Color.red)
            .selectionStrokeMin(4.f)
            .build());

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    Renderer<String, Integer> renderer = vv.getRenderer();
    if (renderer instanceof ModalRenderer) {
      ModalRenderer modalRenderer = (ModalRenderer) renderer;
      LightweightVertexRenderer lightweightVertexRenderer =
          (LightweightVertexRenderer) modalRenderer.getVertexRenderer(LIGHTWEIGHT);
      lightweightVertexRenderer.setVertexShapeFunction(n -> new Ellipse2D.Double(-10, -10, 20, 20));
    }

    vv.scaleToLayout();

    Box controls = Box.createHorizontalBox();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    add(controls, BorderLayout.SOUTH);

    Lens lens = new Lens();
    lens.setMagnification(2.f);
    magnifyViewSupport =
        MagnifyImageLensSupport.builder(vv)
            .lensTransformer(
                MagnifyShapeTransformer.builder(
                        Lens.builder().lensShape(Lens.Shape.RECTANGLE).magnification(3.f).build())
                    .delegate(
                        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW))
                    .build())
            .lensGraphMouse(
                DefaultLensGraphMouse.builder()
                    .magnificationFloor(1.0f)
                    .magnificationCeiling(4.0f)
                    .build())
            .build();

    lens = new Lens();
    lens.setMagnification(2.f);
    magnifyLayoutSupport =
        LayoutLensSupport.builder(vv)
            .lensTransformer(
                MagnifyTransformer.builder(
                        Lens.builder().lensShape(Lens.Shape.RECTANGLE).magnification(3.f).build())
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(Layer.LAYOUT))
                    .build())
            .lensGraphMouse(
                DefaultLensGraphMouse.builder()
                    .magnificationFloor(1.0f)
                    .magnificationCeiling(4.0f)
                    .build())
            .build();

    controls.add(
        LensControlHelper.builder(
                Map.of(
                    "Magnified View", magnifyViewSupport,
                    "Magnified Layout", magnifyLayoutSupport))
            .title("Lens Controls")
            .build()
            .container());
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
