/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 21, 2005
 */

package org.jungrapht.visualization.transform.shape;

import java.awt.*;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.LensTransformSupport;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.TransformSupport;
import org.jungrapht.visualization.layout.NetworkElementAccessor;
import org.jungrapht.visualization.renderers.BasicRenderer;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.transform.AbstractLensSupport;
import org.jungrapht.visualization.transform.LensTransformer;

/**
 * Changes various visualization settings to activate or deactivate an examining lens for a jung
 * graph application.
 *
 * @author Tom Nelson
 */
public class MagnifyImageLensSupport<N, E> extends AbstractLensSupport<N, E> {

  protected RenderContext<N, E> renderContext;
  protected GraphicsDecorator lensGraphicsDecorator;
  protected GraphicsDecorator savedGraphicsDecorator;
  protected Renderer<N, E> renderer;
  protected Renderer<N, E> transformingRenderer;
  protected NetworkElementAccessor<N, E> pickSupport;

  static final String instructions =
      "<html><center>Mouse-Drag the Lens center to move it<p>"
          + "Mouse-Drag the Lens edge to resize it<p>"
          + "Ctrl+MouseWheel to change magnification</center></html>";

  public MagnifyImageLensSupport(
      VisualizationViewer<N, E> vv,
      LensTransformer lensTransformer,
      ModalGraphMouse lensGraphMouse) {
    super(vv, lensGraphMouse);
    this.renderContext = vv.getRenderContext();
    this.pickSupport = renderContext.getPickSupport();
    this.renderer = vv.getRenderer();
    this.transformingRenderer = new BasicRenderer<>();
    this.savedGraphicsDecorator = renderContext.getGraphicsContext();
    this.lensTransformer = lensTransformer;

    Dimension d = vv.getSize();
    if (d.width == 0 || d.height == 0) {
      d = vv.getPreferredSize();
    }
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
