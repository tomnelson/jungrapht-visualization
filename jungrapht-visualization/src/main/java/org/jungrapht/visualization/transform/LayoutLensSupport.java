/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 21, 2005
 */

package org.jungrapht.visualization.transform;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.LensTransformSupport;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ModalLensGraphMouse;
import org.jungrapht.visualization.control.TransformSupport;
import org.jungrapht.visualization.layout.NetworkElementAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to make it easy to add an examining lens to a jung graph application. See
 * HyperbolicTransformerDemo for an example of how to use it.
 *
 * @author Tom Nelson
 */
public class LayoutLensSupport<N, E> extends AbstractLensSupport<N, E> implements LensSupport {

  private static final Logger log = LoggerFactory.getLogger(LayoutLensSupport.class);
  protected NetworkElementAccessor<N, E> pickSupport;

  public LayoutLensSupport(VisualizationViewer<N, E> vv) {
    this(
        vv,
        new HyperbolicTransformer(
            new Lens(vv.getModel().getLayoutSize()),
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.LAYOUT)),
        new ModalLensGraphMouse());
  }

  public LayoutLensSupport(VisualizationViewer<N, E> vv, Lens lens) {
    this(
        vv,
        new HyperbolicTransformer(
            lens,
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.LAYOUT)),
        new ModalLensGraphMouse());
  }

  /**
   * Create an instance with the specified parameters.
   *
   * @param vv the visualization viewer used for rendering
   * @param lensTransformer the lens transformer to use
   * @param lensGraphMouse the lens input handler
   */
  public LayoutLensSupport(
      VisualizationViewer<N, E> vv,
      LensTransformer lensTransformer,
      ModalGraphMouse lensGraphMouse) {
    super(vv, lensGraphMouse);
    this.lensTransformer = lensTransformer;
    this.pickSupport = vv.getPickSupport();
  }

  public void activate() {
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
        .setTransformer(MultiLayerTransformer.Layer.LAYOUT, lensTransformer);
    vv.prependPreRenderPaintable(lensPaintable);
    vv.addPostRenderPaintable(lensControls);
    vv.setGraphMouse(lensGraphMouse);
    vv.setToolTipText(instructions);
    vv.setTransformSupport(new LensTransformSupport<>());
    vv.repaint();
  }

  public void deactivate() {
    if (lensTransformer != null) {
      vv.removePreRenderPaintable(lensPaintable);
      vv.removePostRenderPaintable(lensControls);
      vv.getRenderContext()
          .getMultiLayerTransformer()
          .setTransformer(MultiLayerTransformer.Layer.LAYOUT, lensTransformer.getDelegate());
    }
    vv.setToolTipText(defaultToolTipText);
    vv.setGraphMouse(graphMouse);
    vv.setTransformSupport(new TransformSupport<>());
    vv.repaint();
  }
}
