/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.visualization.transform.shape;

import java.awt.*;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.LensTransformSupport;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.TransformSupport;
import org.jungrapht.visualization.layout.NetworkElementAccessor;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.transform.AbstractLensSupport;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.LensTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses a LensTransformer to use in the view transform. This one will distort Node shapes.
 *
 * @author Tom Nelson
 */
public class ViewLensSupport<N, E> extends AbstractLensSupport<N, E> implements LensSupport {

  private static final Logger log = LoggerFactory.getLogger(ViewLensSupport.class);

  protected RenderContext<N, E> renderContext;
  protected GraphicsDecorator lensGraphicsDecorator;
  protected GraphicsDecorator savedGraphicsDecorator;
  protected NetworkElementAccessor<N, E> pickSupport;
  protected Renderer.Edge<N, E> savedEdgeRenderer;
  protected Renderer.Edge<N, E> reshapingEdgeRenderer;

  public ViewLensSupport(
      VisualizationViewer<N, E> vv,
      LensTransformer lensTransformer,
      ModalGraphMouse lensGraphMouse) {
    super(vv, lensGraphMouse);
    this.renderContext = vv.getRenderContext();
    this.pickSupport = renderContext.getPickSupport();
    this.savedGraphicsDecorator = renderContext.getGraphicsContext();
    this.lensTransformer = lensTransformer;
    LayoutModel layoutModel = vv.getModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
    lensTransformer.getLens().setSize(d);

    this.lensGraphicsDecorator = new TransformingFlatnessGraphics(lensTransformer);
    this.savedEdgeRenderer = vv.getRenderer().getEdgeRenderer();
  }

  public void activate() {
    lensTransformer.setDelegate(
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.VIEW));
    if (lensPaintable == null) {
      lensPaintable = new LensPaintable(lensTransformer);
    }
    if (lensControls == null) {
      lensControls = new LensControls(lensTransformer);
    }

    Point2D viewCenter = vv.getCenter();
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    log.trace("raw view center is {}", viewCenter);
    log.trace("transformed view center is {}", multiLayerTransformer.transform(viewCenter));
    log.trace(
        "inverseTransformed view center is {}", multiLayerTransformer.inverseTransform(viewCenter));
    log.trace(
        "view transformed view center is {}",
        multiLayerTransformer
            .getTransformer(MultiLayerTransformer.Layer.VIEW)
            .transform(viewCenter));
    log.trace(
        "view inverseTransformed view center is {}",
        multiLayerTransformer
            .getTransformer(MultiLayerTransformer.Layer.VIEW)
            .inverseTransform(viewCenter));
    log.trace(
        "layout transformed view center is {}",
        multiLayerTransformer
            .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
            .transform(viewCenter));
    log.trace(
        "layout inverseTransformed view center is {}",
        multiLayerTransformer
            .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
            .inverseTransform(viewCenter));
    lensTransformer
        .getLens()
        .setCenter(
            multiLayerTransformer
                .getTransformer(MultiLayerTransformer.Layer.VIEW)
                .inverseTransform(viewCenter));

    double scale =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.VIEW).getScale();
    log.trace("view scale is {}", scale);
    lensTransformer.getLens().setRadius(Math.min(vv.getWidth(), vv.getHeight()) / scale / 2.2);

    vv.getRenderContext()
        .getMultiLayerTransformer()
        .setTransformer(MultiLayerTransformer.Layer.VIEW, lensTransformer);
    this.renderContext.setGraphicsContext(lensGraphicsDecorator);
    vv.prependPreRenderPaintable(lensPaintable);
    vv.addPostRenderPaintable(lensControls);
    vv.setGraphMouse(lensGraphMouse);
    vv.setToolTipText(instructions);
    vv.setTransformSupport(new LensTransformSupport());
    vv.repaint();
  }

  public void deactivate() {
    vv.getRenderContext()
        .getMultiLayerTransformer()
        .setTransformer(MultiLayerTransformer.Layer.VIEW, lensTransformer.getDelegate());
    vv.removePreRenderPaintable(lensPaintable);
    vv.removePostRenderPaintable(lensControls);
    this.renderContext.setGraphicsContext(savedGraphicsDecorator);
    vv.setRenderContext(renderContext);
    vv.setToolTipText(defaultToolTipText);
    vv.setGraphMouse(graphMouse);
    vv.setTransformSupport(new TransformSupport());
    vv.repaint();
  }
}
