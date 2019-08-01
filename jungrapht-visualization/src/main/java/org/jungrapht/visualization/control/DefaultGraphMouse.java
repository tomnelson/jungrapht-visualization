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

import java.awt.event.InputEvent;

/** @author Tom Nelson */
public class DefaultGraphMouse<V, E> extends PluggableGraphMouse {

  /** used by the scaling plugins for zoom in */
  protected float in;
  /** used by the scaling plugins for zoom out */
  protected float out;

  protected GraphMousePlugin scalingPlugin;

  /** create an instance with default values */
  public DefaultGraphMouse() {
    this(1.1f, 1 / 1.1f);
  }

  /**
   * create an instance with passed values
   *
   * @param in override value for scale in
   * @param out override value for scale out
   */
  public DefaultGraphMouse(float in, float out) {
    this.in = in;
    this.out = out;
    loadPlugins();
  }

  /** create the plugins, and load the plugins for TRANSFORMING mode */
  protected void loadPlugins() {
    scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
    add(new TranslatingGraphMousePlugin(InputEvent.BUTTON1_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
    add(new SelectingGraphMousePlugin<V, E>());
    add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out));
  }

  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
  }
}
