/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Jul 21, 2005
 */

package org.jungrapht.visualization.transform.shape;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.LensGraphMouse;
import org.jungrapht.visualization.control.LensTransformSupport;
import org.jungrapht.visualization.control.TransformSupport;
import org.jungrapht.visualization.layout.GraphElementAccessor;
import org.jungrapht.visualization.renderers.BasicRenderer;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.transform.AbstractLensSupport;
import org.jungrapht.visualization.transform.LensTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes various visualization settings to activate or deactivate an examining lens for a jung
 * graph application.
 *
 * @author Tom Nelson
 */
public class MagnifyImageLensSupport<V, E, T extends LensGraphMouse>
    extends AbstractLensSupport<V, E, T> {

  private static final Logger log = LoggerFactory.getLogger(MagnifyImageLensSupport.class);

  protected RenderContext<V, E> renderContext;
  protected GraphicsDecorator lensGraphicsDecorator;
  protected GraphicsDecorator savedGraphicsDecorator;
  protected Renderer<V, E> renderer;
  protected Renderer<V, E> transformingRenderer;
  protected GraphElementAccessor<V, E> pickSupport;

  static final String instructions =
      "<html><center>Mouse-Drag the Lens center to move it<p>"
          + "Mouse-Drag the Lens edge to resize it<p>"
          + "Ctrl+MouseWheel to change magnification</center></html>";

  public MagnifyImageLensSupport(
      VisualizationViewer<V, E> vv, LensTransformer lensTransformer, T lensGraphMouse) {
    super(vv, lensGraphMouse);
    this.renderContext = vv.getRenderContext();
    this.pickSupport = renderContext.getPickSupport();
    this.renderer = vv.getRenderer();
    this.transformingRenderer = new BasicRenderer<>();
    this.savedGraphicsDecorator = renderContext.getGraphicsContext();
    this.lensTransformer = lensTransformer;

    this.lensGraphicsDecorator = new MagnifyIconGraphics(lensTransformer);
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

    lensTransformer.setDelegate(
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.VIEW));
    vv.getRenderContext()
        .getMultiLayerTransformer()
        .setTransformer(MultiLayerTransformer.Layer.VIEW, lensTransformer);
    this.renderContext.setGraphicsContext(lensGraphicsDecorator);
    vv.addPreRenderPaintable(lensPaintable);
    vv.addPostRenderPaintable(lensControls);
    vv.setGraphMouse(lensGraphMouse);
    vv.setToolTipText(instructions);
    vv.setTransformSupport(new LensTransformSupport<>());
    vv.repaint();
  }

  public void deactivate() {
    renderContext.setPickSupport(pickSupport);
    vv.getRenderContext()
        .getMultiLayerTransformer()
        .setTransformer(MultiLayerTransformer.Layer.VIEW, lensTransformer.getDelegate());
    vv.removePreRenderPaintable(lensPaintable);
    vv.removePostRenderPaintable(lensControls);
    this.renderContext.setGraphicsContext(savedGraphicsDecorator);
    vv.setToolTipText(defaultToolTipText);
    vv.setGraphMouse(graphMouse);
    vv.setTransformSupport(new TransformSupport<>());
    vv.repaint();
  }
}
