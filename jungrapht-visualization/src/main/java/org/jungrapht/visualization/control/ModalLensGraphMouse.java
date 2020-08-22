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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * an implementation of the AbstractModalGraphMouse that includes plugins for manipulating a view
 * that is using a LensTransformer.
 *
 * @author Tom Nelson
 */
public class ModalLensGraphMouse extends AbstractModalGraphMouse
    implements ModalGraphMouse, LensGraphMouse {

  /**
   * Build an instance of a DefaultGraphMouse
   *
   * @param <T>
   * @param <B>
   */
  public static class Builder<T extends ModalLensGraphMouse, B extends Builder<T, B>>
      extends AbstractModalGraphMouse.Builder<T, B> {

    protected LensMagnificationGraphMousePlugin magnificationPlugin;

    public B magnificationPlugin(LensMagnificationGraphMousePlugin magnificationPlugin) {
      this.magnificationPlugin = magnificationPlugin;
      return self();
    }

    public T build() {
      return (T) new ModalLensGraphMouse(in, out, vertexSelectionOnly, magnificationPlugin);
    }
  }

  public static <V, E> Builder<?, ?> builder() {
    return new Builder<>();
  }

  /** not included in the base class */
  protected LensMagnificationGraphMousePlugin magnificationPlugin;

  protected LensSelectingGraphMousePlugin lensSelectingGraphMousePlugin;
  protected LensKillingGraphMousePlugin lensKillingGraphMousePlugin;

  public ModalLensGraphMouse() {
    this(1.1f, 1 / 1.1f);
  }

  public ModalLensGraphMouse(float in, float out) {
    this(in, out, false, new LensMagnificationGraphMousePlugin());
  }

  public ModalLensGraphMouse(LensMagnificationGraphMousePlugin magnificationPlugin) {
    this(1.1f, 1 / 1.1f, false, magnificationPlugin);
  }

  public ModalLensGraphMouse(
      float in,
      float out,
      boolean vertexSelectionOnly,
      LensMagnificationGraphMousePlugin magnificationPlugin) {
    super(in, out, vertexSelectionOnly);
    this.in = in;
    this.out = out;
    this.magnificationPlugin = magnificationPlugin;
    this.lensSelectingGraphMousePlugin =
        new LensSelectingGraphMousePlugin<>(
            InputEvent.BUTTON1_DOWN_MASK, 0, InputEvent.SHIFT_DOWN_MASK);
    this.lensKillingGraphMousePlugin = new LensKillingGraphMousePlugin();
    setModeKeyListener(new ModeKeyAdapter(this));
  }

  public void setKillSwitch(Runnable killSwitch) {
    this.lensKillingGraphMousePlugin.setKillSwitch(killSwitch);
  }

  public void loadPlugins() {
    super.loadPlugins();
    add(lensKillingGraphMousePlugin);
    pickingPlugin = lensSelectingGraphMousePlugin;
    //    animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<>();
    translatingPlugin = new LensTranslatingGraphMousePlugin(InputEvent.BUTTON1_DOWN_MASK);
    scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
    rotatingPlugin = new RotatingGraphMousePlugin();
    shearingPlugin = new ShearingGraphMousePlugin();

    add(magnificationPlugin);
    add(scalingPlugin);

    setMode(Mode.TRANSFORMING);
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
}
