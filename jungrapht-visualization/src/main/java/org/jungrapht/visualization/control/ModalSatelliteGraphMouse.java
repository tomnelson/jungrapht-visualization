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

import java.awt.event.InputEvent;
/** @author Tom Nelson */
public class ModalSatelliteGraphMouse extends DefaultModalGraphMouse implements ModalGraphMouse {

  public ModalSatelliteGraphMouse() {
    this(1.1f, 1 / 1.1f);
  }

  public ModalSatelliteGraphMouse(float in, float out) {
    super(in, out);
  }

  public void loadPlugins() {
    pickingPlugin = new SelectingGraphMousePlugin();
    animatedPickingPlugin = new SatelliteAnimatedPickingGraphMousePlugin();
    translatingPlugin = new SatelliteTranslatingGraphMousePlugin(InputEvent.BUTTON1_DOWN_MASK);
    scalingPlugin = new SatelliteScalingGraphMousePlugin(new CrossoverScalingControl(), 0);
    rotatingPlugin = new SatelliteRotatingGraphMousePlugin();
    shearingPlugin = new SatelliteShearingGraphMousePlugin();

    add(scalingPlugin);

    setMode(Mode.TRANSFORMING);
  }
}
