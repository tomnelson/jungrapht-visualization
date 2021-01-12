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

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import javax.swing.*;
import javax.swing.event.EventListenerList;

/**
 * AbstractModalGraphMouse is a PluggableGraphMouse class that manages a collection of plugins for
 * picking and transforming the graph. Additionally, it carries the notion of a Mode: Picking or
 * Translating. Switching between modes allows for a more natural choice of mouse modifiers to be
 * used for the various plugins. The default modifiers are intended to mimick those of mainstream
 * software applications in order to be intuitive to users.
 *
 * <p>// *
 *
 * <p>To change between modes, two different controls are offered, a combo box and a menu system. //
 * * These controls are lazily created in their respective 'getter' methods so they don't impact
 * code // * that does not intend to use them. The menu control can be placed in an unused corner of
 * the // * VisualizationScrollPane, which is a common location for mouse mode selection menus in
 * mainstream // * applications.
 *
 * <p>Users must implement the loadPlugins() method to create and install the GraphMousePlugins. The
 * order of the plugins is important, as they are evaluated against the mask parameters in the order
 * that they are added.
 *
 * @author Tom Nelson
 */
public abstract class AbstractModalGraphMouse extends AbstractGraphMouse
    implements ModalGraphMouse {

  /**
   * Configure an instance of an AbstractModalGraphMouse
   *
   * @param <T>
   * @param <B>
   */
  public abstract static class Builder<T extends AbstractModalGraphMouse, B extends Builder<T, B>>
      extends AbstractGraphMouse.Builder<T, B> {
    protected Mode mode = Mode.TRANSFORMING;

    public Builder mode(Mode mode) {
      this.mode = mode;
      return this;
    }

    public abstract T build();
  }

  /** a listener for mode changes */
  protected ItemListener modeListener;
  /** the current mode */
  protected Mode mode;
  /** listeners for mode changes */
  protected EventListenerList listenerList = new EventListenerList();

  protected GraphMousePlugin translatingPlugin;
  protected GraphMousePlugin animatedPickingPlugin;
  protected GraphMousePlugin rotatingPlugin;
  protected GraphMousePlugin shearingPlugin;
  protected KeyListener modeKeyListener;

  protected AbstractModalGraphMouse(Builder<?, ?> builder) {
    this(builder.mode, builder.in, builder.out, builder.vertexSelectionOnly);
  }

  protected AbstractModalGraphMouse(Mode mode, float in, float out, boolean vertexSelectionOnly) {
    super(in, out, vertexSelectionOnly);
    this.mode = mode;
  }

  /** setter for the Mode. */
  @Override
  public void setMode(Mode mode) {
    this.mode = mode;
    if (mode == Mode.TRANSFORMING) {
      setTransformingMode();
    } else if (mode == Mode.PICKING) {
      setPickingMode();
    }
  }

  @Override
  public Mode getMode() {
    return this.mode;
  }

  /* (non-Javadoc)
   * @see ModalGraphMouse#setPickingMode()
   */
  protected void setPickingMode() {
    clear();
    add(scalingPlugin);
    add(selectingPlugin);
    add(regionSelectingPlugin);
  }

  /* (non-Javadoc)
   * @see ModalGraphMouse#setTransformingMode()
   */
  protected void setTransformingMode() {
    clear();
    add(scalingPlugin);
    add(translatingPlugin);
    add(rotatingPlugin);
    add(shearingPlugin);
  }

  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
  }

  /** listener to set the mode from an external event source */
  class ModeListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setMode((Mode) e.getItem());
      }
    }
  }

  /* (non-Javadoc)
   * @see ModalGraphMouse#getModeListener()
   */
  public ItemListener getModeListener() {
    if (modeListener == null) {
      modeListener = new ModeListener();
    }
    return modeListener;
  }

  //  /** @return the modeKeyListener */
  @Override
  public KeyListener getModeKeyListener() {
    return modeKeyListener;
  }
  //
  //  /** @param modeKeyListener the modeKeyListener to set */
  public void setModeKeyListener(KeyListener modeKeyListener) {
    this.modeKeyListener = modeKeyListener;
  }

  /** add a listener for mode changes */
  public void addItemListener(ItemListener aListener) {
    listenerList.add(ItemListener.class, aListener);
  }

  /** remove a listener for mode changes */
  public void removeItemListener(ItemListener aListener) {
    listenerList.remove(ItemListener.class, aListener);
  }

  /**
   * Returns an array of all the <code>ItemListener</code>s added to this JComboBox with
   * addItemListener().
   *
   * @return all of the <code>ItemListener</code>s added or an empty array if no listeners have been
   *     added
   * @since 1.4
   */
  public ItemListener[] getItemListeners() {
    return listenerList.getListeners(ItemListener.class);
  }

  public Object[] getSelectedObjects() {
    if (mode == null) {
      return new Object[0];
    } else {
      Object[] result = new Object[1];
      result[0] = mode;
      return result;
    }
  }
}
