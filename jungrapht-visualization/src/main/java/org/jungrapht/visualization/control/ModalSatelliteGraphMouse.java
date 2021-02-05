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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class ModalSatelliteGraphMouse<V, E> extends DefaultModalGraphMouse<V, E>
    implements ModalGraphMouse {

  private static final Logger log = LoggerFactory.getLogger(ModalSatelliteGraphMouse.class);
  /**
   * Build an instance of a ModalSatelliteGraphMouse
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
      return (T) new ModalSatelliteGraphMouse(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected int singleSelectionMask;
  protected int toggleSingleSelectionMask;
  protected int regionSelectionMask;
  protected int toggleRegionSelectionMask;
  protected int regionSelectionCompleteMask;
  protected int toggleRegionSelectionCompleteMask;
  protected int translatingMask;
  protected int scalingMask;
  protected int xAxisScalingMask;
  protected int yAxisScalingMask;

  public ModalSatelliteGraphMouse() {
    this(new Builder<>());
  }

  ModalSatelliteGraphMouse(Builder<V, E, ?, ?> builder) {
    this(
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
        builder.scalingMask,
        builder.xAxisScalingMask,
        builder.yAxisScalingMask);
  }

  ModalSatelliteGraphMouse(
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
      int scalingMask,
      int xAxisScalingMask,
      int yAxisScalingMask) {
    super(in, out, vertexSelectionOnly);
    this.singleSelectionMask = singleSelectionMask;
    this.toggleSingleSelectionMask = toggleSingleSelectionMask;
    this.regionSelectionMask = regionSelectionMask;
    this.toggleRegionSelectionMask = toggleRegionSelectionMask;
    this.regionSelectionCompleteMask = regionSelectionCompleteMask;
    this.toggleRegionSelectionCompleteMask = toggleRegionSelectionCompleteMask;
    this.translatingMask = translatingMask;
    this.scalingMask = scalingMask;
    this.xAxisScalingMask = xAxisScalingMask;
    this.yAxisScalingMask = yAxisScalingMask;
  }

  public void loadPlugins() {
    selectingPlugin =
        SelectingGraphMousePlugin.builder()
            .singleSelectionMask(InputEvent.BUTTON1_DOWN_MASK)
            .toggleSingleSelectionMask(InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)
            .build();
    regionSelectingPlugin =
        RegionSelectingGraphMousePlugin.builder()
            .regionSelectionMask(InputEvent.BUTTON1_DOWN_MASK)
            .toggleRegionSelectionMask(0)
            .regionSelectionCompleteMask(0)
            .toggleRegionSelectionCompleteMask(InputEvent.SHIFT_DOWN_MASK)
            .build();

    //    animatedPickingPlugin = new SatelliteAnimatedPickingGraphMousePlugin();
    translatingPlugin = new SatelliteTranslatingGraphMousePlugin(InputEvent.BUTTON1_DOWN_MASK);
    scalingPlugin =
        new SatelliteScalingGraphMousePlugin(
            new CrossoverScalingControl(),
            scalingMask,
            xAxisScalingMask,
            yAxisScalingMask,
            in,
            out);
    rotatingPlugin = new SatelliteRotatingGraphMousePlugin();
    shearingPlugin = new SatelliteShearingGraphMousePlugin();

    add(scalingPlugin);
  }
}
