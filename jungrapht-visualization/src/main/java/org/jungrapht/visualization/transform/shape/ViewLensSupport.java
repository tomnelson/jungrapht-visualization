/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
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
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.LensTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses a LensTransformer to use in the view transform. This one will distort Vertex shapes.
 *
 * @author Tom Nelson
 */
public class ViewLensSupport<V, E, M extends LensGraphMouse> extends AbstractLensSupport<V, E, M>
    implements LensSupport<M> {

  public static class Builder<
          V,
          E,
          M extends LensGraphMouse,
          T extends ViewLensSupport<V, E, M>,
          B extends Builder<V, E, M, T, B>>
      extends AbstractLensSupport.Builder<V, E, M, T, B> {

    protected Builder(VisualizationViewer<V, E> vv) {
      super(vv);
    }

    public T build() {
      return (T) new ViewLensSupport(this);
    }
  }

  public static <V, E, M extends LensGraphMouse> Builder<V, E, M, ?, ?> builder(
      VisualizationViewer<V, E> vv) {
    return new Builder<>(vv);
  }

  protected ViewLensSupport(Builder<V, E, M, ?, ?> builder) {
    super(builder);
    this.renderContext = vv.getRenderContext();
    this.pickSupport = renderContext.getPickSupport();
    this.savedGraphicsDecorator = renderContext.getGraphicsContext();
    this.lensGraphicsDecorator = new TransformingFlatnessGraphics(lensTransformer);
    this.savedEdgeRenderer = vv.getRenderer().getEdgeRenderer();
  }

  private static final Logger log = LoggerFactory.getLogger(ViewLensSupport.class);

  protected RenderContext<V, E> renderContext;
  protected GraphicsDecorator lensGraphicsDecorator;
  protected GraphicsDecorator savedGraphicsDecorator;
  protected GraphElementAccessor<V, E> pickSupport;
  protected Renderer.Edge<V, E> savedEdgeRenderer;
  protected Renderer.Edge<V, E> reshapingEdgeRenderer;

  protected ViewLensSupport(
      VisualizationViewer<V, E> vv, LensTransformer lensTransformer, M lensGraphMouse) {
    super(vv, lensGraphMouse);
    this.renderContext = vv.getRenderContext();
    this.pickSupport = renderContext.getPickSupport();
    this.savedGraphicsDecorator = renderContext.getGraphicsContext();
    this.lensTransformer = lensTransformer;
    this.lensGraphicsDecorator = new TransformingFlatnessGraphics(lensTransformer);
    this.savedEdgeRenderer = vv.getRenderer().getEdgeRenderer();
  }

  public void activate() {
    if (allowed()) {
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
