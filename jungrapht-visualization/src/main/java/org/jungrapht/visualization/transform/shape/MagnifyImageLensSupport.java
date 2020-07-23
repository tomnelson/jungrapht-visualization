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
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.transform.AbstractLensSupport;
import org.jungrapht.visualization.transform.LensTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes various visualization settings to activate or deactivate an examining lens for a graph
 * visualization.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 * @param <M> LensGraphMouse type
 */
public class MagnifyImageLensSupport<V, E, M extends LensGraphMouse>
    extends AbstractLensSupport<V, E, M> {

  public static class Builder<
          V,
          E,
          M extends LensGraphMouse,
          T extends MagnifyImageLensSupport<V, E, M>,
          B extends Builder<V, E, M, T, B>>
      extends AbstractLensSupport.Builder<V, E, M, T, B> {

    protected Builder(VisualizationViewer<V, E> vv) {
      super(vv);
    }

    public T build() {
      return (T) new MagnifyImageLensSupport(this);
    }
  }

  public static <V, E, M extends LensGraphMouse> Builder<V, E, M, ?, ?> builder(
      VisualizationViewer<V, E> vv) {
    return new Builder<>(vv);
  }

  protected MagnifyImageLensSupport(Builder<V, E, M, ?, ?> builder) {
    super(builder);
    this.renderContext = vv.getRenderContext();
    this.pickSupport = renderContext.getPickSupport();
    this.renderer = vv.getRenderer();
    this.transformingRenderer = Renderer.<V, E>builder().build();
    this.savedGraphicsDecorator = renderContext.getGraphicsContext();
    this.lensGraphicsDecorator = new MagnifyIconGraphics(lensTransformer);
  }

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

  protected MagnifyImageLensSupport(
      VisualizationViewer<V, E> vv, LensTransformer lensTransformer, M lensGraphMouse) {
    super(vv, lensGraphMouse);
    this.renderContext = vv.getRenderContext();
    this.pickSupport = renderContext.getPickSupport();
    this.renderer = vv.getRenderer();
    this.transformingRenderer = Renderer.<V, E>builder().build();
    this.savedGraphicsDecorator = renderContext.getGraphicsContext();
    this.lensTransformer = lensTransformer;

    this.lensGraphicsDecorator = new MagnifyIconGraphics(lensTransformer);
  }

  public void activate() {
    if (allowed()) {
      super.activate();
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
      MultiLayerTransformer multiLayerTransformer =
          vv.getRenderContext().getMultiLayerTransformer();
      lensTransformer
          .getLens()
          .setCenter(
              multiLayerTransformer
                  .getTransformer(MultiLayerTransformer.Layer.VIEW)
                  .inverseTransform(viewCenter));

      double scale =
          multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.VIEW).getScale();
      log.trace("view scale is {}", scale);
      lensTransformer.getLens().setRadius(Math.min(vv.getWidth(), vv.getHeight()) / scale / 3);

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
  }

  public void deactivate() {
    super.deactivate();
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
