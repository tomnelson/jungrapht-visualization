/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Aug 3, 2019
 */

package org.jungrapht.visualization.control;

import java.awt.event.InputEvent;

/**
 * A Satellite version of the {@link DefaultGraphMouse}. Non-modal, Use CTRL-mouse to translate the
 * graph visualization
 *
 * @author Tom Nelson
 */
public class DefaultSatelliteGraphMouse<V, E> extends AbstractGraphMouse {

  /** create an instance with default values */
  public DefaultSatelliteGraphMouse() {
    this(1.1f, 1 / 1.1f);
  }

  /**
   * create an instance with passed values
   *
   * @param in override value for scale in
   * @param out override value for scale out
   */
  public DefaultSatelliteGraphMouse(float in, float out) {
    super(in, out);
    loadPlugins();
  }

  /** create the plugins, and load the plugins for TRANSFORMING mode */
  protected void loadPlugins() {
    scalingPlugin = new SatelliteScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
    add(
        new SatelliteTranslatingGraphMousePlugin(
            InputEvent.BUTTON1_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
    add(new SelectingGraphMousePlugin<V, E>());
    add(scalingPlugin);
  }

  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
  }
}
