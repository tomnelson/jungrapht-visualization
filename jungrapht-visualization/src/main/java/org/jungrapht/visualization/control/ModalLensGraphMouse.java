/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Aug 26, 2005
 */

package org.jungrapht.visualization.control;

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * an implementation of the AbstractModalGraphMouse that includes plugins for manipulating a view
 * that is using a LensTransformer.
 *
 * @author Tom Nelson
 */
public class ModalLensGraphMouse<V, E> extends DefaultModalGraphMouse<V, E>
    implements ModalGraphMouse, LensGraphMouse {

  /**
   * Build an instance of a ModalLensGraphMouse
   *
   * @param <T>
   * @param <B>
   */
  public static class Builder<
          V, E, T extends ModalLensGraphMouse<V, E>, B extends Builder<V, E, T, B>>
      extends DefaultModalGraphMouse.Builder<V, E, T, B> {

    protected float magnificationDelta = 0.05f;
    // values for hyperbolic transform lens
    protected float magnificationFloor = 0.45f;
    protected float magnificationCeiling = 1.0f;
    // values for magnify transform lens
    //    protected float magnificationFloor = 1.0f;
    //    protected float magnificationCeiling = 4.0f;

    // translation mask
    protected int lensTranslatingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "lensTranslatingMask", "MB1"));

    public B lensTranslatingMask(int lensTranslatingMask) {
      this.lensTranslatingMask = lensTranslatingMask;
      return self();
    }

    public B magnificationFloor(float magnificationFloor) {
      this.magnificationFloor = magnificationFloor;
      return self();
    }

    public B magnificationCeiling(float magnificationCeiling) {
      this.magnificationCeiling = magnificationCeiling;
      return self();
    }

    public B magnificationDelta(float magnificationDelta) {
      this.magnificationDelta = magnificationDelta;
      return self();
    }

    public T build() {
      return (T) new ModalLensGraphMouse(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  /** not included in the base class */
  protected LensMagnificationGraphMousePlugin magnificationPlugin;

  protected LensSelectingGraphMousePlugin lensSelectingGraphMousePlugin;
  protected LensKillingGraphMousePlugin lensKillingGraphMousePlugin;
  protected LensTranslatingGraphMousePlugin lensTranslatingGraphMousePlugin;

  public ModalLensGraphMouse() {
    this(new Builder<>());
  }

  ModalLensGraphMouse(Builder<V, E, ?, ?> builder) {
    this(
        builder.mode,
        builder.in,
        builder.out,
        builder.vertexSelectionOnly,
        builder.singleSelectionMask,
        builder.toggleSingleSelectionMask,
        builder.regionSelectionMask,
        builder.toggleRegionSelectionMask,
        builder.regionSelectionCompleteMask,
        builder.toggleRegionSelectionCompleteMask,
        builder.translatingMask,
        builder.lensTranslatingMask,
        builder.scalingMask,
        builder.xAxisScalingMask,
        builder.yAxisScalingMask,
        builder.magnificationFloor,
        builder.magnificationCeiling,
        builder.magnificationDelta);
  }

  public ModalLensGraphMouse(
      Mode mode,
      float in,
      float out,
      boolean vertexSelectionOnly,
      int singleSelectionMask,
      int toggleSingleSelectionMask,
      int regionSelectionMask,
      int toggleRegionSelectionMask,
      int regionSelectionCompleteMask,
      int toggleRegionSelectionCompleteMask,
      int translatingMask,
      int lensTranslatingMask,
      int scalingMask,
      int xAxisScalingMask,
      int yAxisScalingMask,
      float magnificationFloor,
      float magnificationCeiling,
      float magnificationDelta) {
    super(
        mode,
        in,
        out,
        vertexSelectionOnly,
        singleSelectionMask,
        toggleSingleSelectionMask,
        regionSelectionMask,
        toggleRegionSelectionMask,
        regionSelectionCompleteMask,
        toggleRegionSelectionCompleteMask,
        translatingMask,
        scalingMask,
        xAxisScalingMask,
        yAxisScalingMask);
    this.magnificationPlugin =
        new LensMagnificationGraphMousePlugin(
            magnificationFloor, magnificationCeiling, magnificationDelta);
    this.lensTranslatingGraphMousePlugin = new LensTranslatingGraphMousePlugin(lensTranslatingMask);
    this.lensKillingGraphMousePlugin = new LensKillingGraphMousePlugin();
    this.translatingPlugin =
        TranslatingGraphMousePlugin.builder().translatingMask(translatingMask).build();
  }

  public void setKillSwitch(Runnable killSwitch) {
    this.lensKillingGraphMousePlugin.setKillSwitch(killSwitch);
  }

  public void loadPlugins() {
    super.loadPlugins();
    setMode(this.mode);
  }

  public static class ModeKeyAdapter extends KeyAdapter {
    private char t = 't';
    private char p = 'p';
    protected ModalGraphMouse graphMouse;

    public ModeKeyAdapter(ModalGraphMouse graphMouse) {
      this.graphMouse = graphMouse;
    }

    public ModeKeyAdapter(char t, char p, ModalGraphMouse graphMouse) {
      this.t = t;
      this.p = p;
      this.graphMouse = graphMouse;
    }

    public void keyTyped(KeyEvent event) {
      char keyChar = event.getKeyChar();
      if (keyChar == t) {
        ((Component) event.getSource())
            .setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        graphMouse.setMode(Mode.TRANSFORMING);
      } else if (keyChar == p) {
        ((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        graphMouse.setMode(Mode.PICKING);
      }
    }
  }

  /* (non-Javadoc)
   * @see ModalGraphMouse#setPickingMode()
   */
  protected void setPickingMode() {
    clear();
    add(lensKillingGraphMousePlugin);
    add(magnificationPlugin);
    add(scalingPlugin);
    add(selectingPlugin);
    add(regionSelectingPlugin);
  }

  /* (non-Javadoc)
   * @see ModalGraphMouse#setTransformingMode()
   */
  protected void setTransformingMode() {
    clear();
    add(lensKillingGraphMousePlugin);
    add(magnificationPlugin);
    add(scalingPlugin);
    add(lensTranslatingGraphMousePlugin);
    add(translatingPlugin);
    add(selectingPlugin);
    add(rotatingPlugin);
    add(shearingPlugin);
  }
}
