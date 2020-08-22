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
public class ModalSatelliteGraphMouse<V, E> extends DefaultModalGraphMouse<V, E>
    implements ModalGraphMouse {

  /**
   * Build an instance of a DefaultGraphMouse
   *
   * @param <V>
   * @param <E>
   * @param <T>
   * @param <B>
   */
  public static class Builder<
          V, E, T extends ModalSatelliteGraphMouse<V, E>, B extends Builder<V, E, T, B>>
      extends DefaultModalGraphMouse.Builder<V, E, T, B> {

    public T build() {
      return (T) new ModalSatelliteGraphMouse(in, out, vertexSelectionOnly);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  public ModalSatelliteGraphMouse() {
    this(new Builder<>());
  }

  ModalSatelliteGraphMouse(Builder<V, E, ?, ?> builder) {
    this(builder.in, builder.out, builder.vertexSelectionOnly);
  }

  ModalSatelliteGraphMouse(float in, float out) {
    super(in, out);
  }

  ModalSatelliteGraphMouse(float in, float out, boolean vertexSelectionOnly) {
    super(in, out, vertexSelectionOnly);
  }

  public void loadPlugins() {
    super.loadPlugins();
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
