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

  /** used by the scaling plugins for zoom in */
  protected float in;
  /** used by the scaling plugins for zoom out */
  protected float out;

  protected GraphMousePlugin scalingPlugin;

  protected SelectingGraphMousePlugin pickingPlugin;

  protected AbstractGraphMouse(float in, float out) {
    this.in = in;
    this.out = out;
  }

  public void setMultiSelectionStrategy(MultiSelectionStrategy multiSelectionStrategy) {
    this.pickingPlugin.setMultiSelectionStrategy(multiSelectionStrategy);
  }

  /** create the plugins, and load the plugins for TRANSFORMING mode */
  public abstract void loadPlugins();

  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
  }
}
