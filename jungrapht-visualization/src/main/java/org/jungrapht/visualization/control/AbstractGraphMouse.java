/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 * Created on August 3, 2019
 *
 */
package org.jungrapht.visualization.control;

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.awt.event.MouseEvent;
import org.jungrapht.visualization.PropertyLoader;

/**
 * AbstractGraphMouse is a PluggableGraphMouse class that manages a collection of plugins for
 * picking and transforming the graph.
 *
 * <p>Users must implement the loadPlugins() method to create and install the GraphMousePlugins. The
 * order of the plugins is important, as they are evaluated against the mask parameters in the order
 * that they are added.
 *
 * @author Tom Nelson
 */
public abstract class AbstractGraphMouse extends PluggableGraphMouse {

  static {
    PropertyLoader.load();
  }

  private static final String VERTEX_SELECTION_ONLY = PREFIX + "vertexSelectionOnly";

  /**
   * Configure an instance of an AbstractGraphMouse
   *
   * @param <T> GraphMouse type
   * @param <B> Builder type
   */
  public abstract static class Builder<T extends AbstractGraphMouse, B extends Builder<T, B>> {

    protected boolean vertexSelectionOnly =
        Boolean.parseBoolean(System.getProperty(VERTEX_SELECTION_ONLY, "false"));
    protected float in = 1.1f;
    protected float out = 1 / 1.1f;
    protected int selectionMask = MouseEvent.BUTTON1_DOWN_MASK;
    protected int addToSelectionMask = MouseEvent.SHIFT_DOWN_MASK;

    public B self() {
      return (B) this;
    }

    public B in(float in) {
      this.in = in;
      return self();
    }

    public B out(float out) {
      this.out = out;
      return self();
    }

    public B vertexSelectionOnly(boolean vertexSelectionOnly) {
      this.vertexSelectionOnly = vertexSelectionOnly;
      return self();
    }

    public abstract T build();
  }

  /** used by the scaling plugins for zoom in */
  protected float in;
  /** used by the scaling plugins for zoom out */
  protected float out;

  protected boolean vertexSelectionOnly;

  protected GraphMousePlugin scalingPlugin;

  protected AbstractGraphMousePlugin selectingPlugin;

  protected AbstractGraphMousePlugin regionSelectingPlugin;

  protected AbstractGraphMousePlugin translatingPlugin;

  protected AbstractGraphMouse(float in, float out, boolean vertexSelectionOnly) {
    this.in = in;
    this.out = out;
    this.vertexSelectionOnly = vertexSelectionOnly;
  }

  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
  }
}
