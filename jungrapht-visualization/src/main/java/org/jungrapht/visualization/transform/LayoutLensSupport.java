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
