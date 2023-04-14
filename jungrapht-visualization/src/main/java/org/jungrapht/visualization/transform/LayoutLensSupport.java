/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Jul 21, 2005
 */

package org.jungrapht.visualization.transform;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to make it easy to add an examining lens to a jungrapht graph application. See
 * HyperbolicTransformerDemo for an example of how to use it.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 * @param <M> LensGraphMouse type
 */
public class LayoutLensSupport<V, E, M extends LensGraphMouse> extends AbstractLensSupport<V, E, M>
    implements LensSupport<M> {

  public static class Builder<
          V,
          E,
          M extends LensGraphMouse,
          T extends LayoutLensSupport<V, E, M>,
          B extends Builder<V, E, M, T, B>>
      extends AbstractLensSupport.Builder<V, E, M, T, B> {
    protected Builder(VisualizationViewer<V, E> vv) {
      super(vv);
    }

    //    protected GraphElementAccessor<V, E> pickSupport;

    @Override
    public B lensGraphMouse(M lensGraphMouse) {
      this.lensGraphMouse = lensGraphMouse;
      return self();
    }

    public T build() {
      return (T) new LayoutLensSupport(this);
    }
  }

  public static <V, E, M extends LensGraphMouse> Builder<V, E, M, ?, ?> builder(
      VisualizationViewer<V, E> vv) {
    return new Builder(vv);
  }

  protected LayoutLensSupport(Builder<V, E, M, ?, ?> builder) {
    super(builder);
    this.lensTransformer = builder.lensTransformer;
    this.pickSupport = vv.getPickSupport();
  }

  private static final Logger log = LoggerFactory.getLogger(LayoutLensSupport.class);
  //  protected GraphElementAccessor<V, E> pickSupport;

  /**
   * Create an instance with the specified parameters.
   *
   * @param vv the visualization viewer used for rendering
   * @param lensTransformer the lens layoutTransformer to use
   * @param lensGraphMouse the lens input handler
   */
  protected LayoutLensSupport(
      VisualizationViewer<V, E> vv, LensTransformer lensTransformer, M lensGraphMouse) {
    super(vv, lensGraphMouse);
    this.lensTransformer = lensTransformer;
    this.pickSupport = vv.getPickSupport();
  }

  public void activate() {
    super.activate();
    if (allowed()) {
      if (lensPaintable == null) {
        lensPaintable = new LensPaintable(lensTransformer, useGradient);
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

      vv.getRenderContext()
          .getMultiLayerTransformer()
          .setTransformer(MultiLayerTransformer.Layer.LAYOUT, lensTransformer);
      vv.prependPreRenderPaintable(lensPaintable);
      vv.addPostRenderPaintable(lensControls);
      vv.setGraphMouse(lensGraphMouse);
      vv.setToolTipText(instructions);
      vv.setTransformSupport(new LensTransformSupport<>());
      vv.repaint();
    }
  }

  public void deactivate() {
    super.deactivate();
    if (lensTransformer != null) {
      vv.removePreRenderPaintable(lensPaintable);
      vv.removePostRenderPaintable(lensControls);
      vv.getRenderContext()
          .getMultiLayerTransformer()
          .setTransformer(MultiLayerTransformer.Layer.LAYOUT, lensTransformer.getDelegate());
    }
    vv.setToolTipText(defaultToolTipText);
    //    vv.setGraphMouse(graphMouse);
    vv.setTransformSupport(new TransformSupport<>());
    vv.repaint();
  }
}
