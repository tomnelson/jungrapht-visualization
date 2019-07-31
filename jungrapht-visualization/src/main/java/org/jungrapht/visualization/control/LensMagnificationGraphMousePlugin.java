/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 * Created on Mar 8, 2005
 *
 */
package org.jungrapht.visualization.control;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensTransformer;
import org.jungrapht.visualization.transform.MutableTransformer;

/**
 * HyperbolicMagnificationGraphMousePlugin changes the magnification within the Hyperbolic
 * projection of the HyperbolicTransformer.
 *
 * @author Tom Nelson
 */
public class LensMagnificationGraphMousePlugin extends AbstractGraphMousePlugin
    implements MouseWheelListener {

  protected final float floor;
  protected final float ceiling;
  protected final float delta;

  /**
   * Creates an instance with modifier of CTRL_DOWN_MASK, and default min/max/delta zoom values of
   * 1/4/0.2.
   */
  public LensMagnificationGraphMousePlugin() {
    this(MouseEvent.CTRL_DOWN_MASK);
  }

  /**
   * Creates an instance with modifier of CTRL_DOWN_MASK, and the specified zoom parameters.
   *
   * @param floor the minimum zoom value
   * @param ceiling the maximum zoom value
   * @param delta the change in zoom value caused by each mouse event
   */
  public LensMagnificationGraphMousePlugin(float floor, float ceiling, float delta) {
    this(MouseEvent.CTRL_DOWN_MASK, floor, ceiling, delta);
  }

  /**
   * Creates an instance with the specified modifiers and the default min/max/delta zoom values of
   * 1/4/0.2.
   *
   * @param modifiers the mouse event modifiers to specify
   */
  public LensMagnificationGraphMousePlugin(int modifiers) {
    this(modifiers, 0.5f, 4.0f, .2f);
  }

  /**
   * Creates an instance with the specified mouse event modifiers and zoom parameters.
   *
   * @param modifiers the mouse event modifiers to specify
   * @param floor the minimum zoom value
   * @param ceiling the maximum zoom value
   * @param delta the change in zoom value caused by each mouse event
   */
  public LensMagnificationGraphMousePlugin(int modifiers, float floor, float ceiling, float delta) {
    super(modifiers);
    this.floor = floor;
    this.ceiling = ceiling;
    this.delta = delta;
  }

  /** override to check equality with a mask */
  public boolean checkModifiers(MouseEvent e) {
    return (e.getModifiersEx() & modifiers) != 0;
  }

  private void changeMagnification(Lens lens, float delta) {
    float magnification = lens.getMagnification() + delta;
    magnification = Math.max(floor, magnification);
    magnification = Math.min(magnification, ceiling);
    lens.setMagnification(magnification);
  }

  /** change magnification of the lens, depending on the direction of the mouse wheel motion. */
  public void mouseWheelMoved(MouseWheelEvent e) {
    boolean accepted = checkModifiers(e);
    float delta = this.delta;
    if (accepted) {
      VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
      MutableTransformer layoutTransformer =
          vv.getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(MultiLayerTransformer.Layer.LAYOUT);
      MutableTransformer viewTransformer =
          vv.getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(MultiLayerTransformer.Layer.VIEW);
      int amount = e.getWheelRotation();
      if (amount < 0) {
        delta = -delta;
      }
      Lens lens =
          (layoutTransformer instanceof LensTransformer)
              ? ((LensTransformer) layoutTransformer).getLens()
              : (viewTransformer instanceof LensTransformer)
                  ? ((LensTransformer) viewTransformer).getLens()
                  : null;
      if (lens != null) {
        changeMagnification(lens, delta);
      }
      vv.repaint();
      e.consume();
    }
  }
}
