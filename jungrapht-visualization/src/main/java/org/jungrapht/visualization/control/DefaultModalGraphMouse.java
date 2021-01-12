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

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * DefaultModalGraphMouse is a GraphMouse class that pre-installs a large collection of plugins for
 * picking and transforming the graph. Additionally, it carries the notion of a Mode: Picking or
 * Translating. Switching between modes allows for a more natural choice of mouse modifiers to be
 * used for the various plugins. The default modifiers are intended to mimick those of mainstream
 * software applications in order to be intuitive to users.
 *
 * <p>To change between modes, two different controls are offered, a combo box and a menu system.
 * These controls are lazily created in their respective 'getter' methods so they don't impact code
 * that does not intend to use them. The menu control can be placed in an unused corner of the
 * VisualizationScrollPane, which is a common location for mouse mode selection menus in mainstream
 * applications.
 *
 * @author Tom Nelson
 */
public class DefaultModalGraphMouse<V, E> extends AbstractModalGraphMouse
    implements ModalGraphMouse {

  /**
   * Build an instance of a DefaultGraphMouse
   *
   * @param <V>
   * @param <E>
   * @param <T>
   * @param <B>
   */
  public static class Builder<V, E, T extends DefaultModalGraphMouse, B extends Builder<V, E, T, B>>
      extends AbstractModalGraphMouse.Builder<T, B> {
    // selection masks
    protected int singleSelectionMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "singleSelectionMask", "MB1_MENU"));
    protected int addSingleSelectionMask =
        Modifiers.masks.get(
            System.getProperty(PREFIX + "addSingleSelectionMask", "MB1_SHIFT_MENU"));
    protected int regionSelectionMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "regionSelectionMask", "MB1_MENU"));
    protected int addRegionSelectionMask =
        Modifiers.masks.get(
            System.getProperty(PREFIX + "addregionSelectionMask", "MB1_SHIFT_MENU"));
    protected int regionSelectionCompleteMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "regionSelectionCompleteMask", "MENU"));
    protected int addRegionSelectionCompleteMask =
        Modifiers.masks.get(
            System.getProperty(PREFIX + "addRegionSelectionCompleteMask", "SHIFT_MENU"));

    // translation mask
    protected int translatingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "translatingMask", "MB1"));

    // scaling masks
    protected int xAxisScalingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "xAxisScalingMask", "MENU"));
    protected int yAxisScalingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "yAxisScalingMask", "ALT"));
    protected int scalingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "scalingMask", "NONE"));

    public B singleSelectionMask(int singleSelectionMask) {
      this.singleSelectionMask = singleSelectionMask;
      return self();
    }

    public B addSingleSelectionMask(int addSingleSelectionMask) {
      this.addSingleSelectionMask = addSingleSelectionMask;
      return self();
    }

    public B regionSelectionMask(int regionSelectionMask) {
      this.regionSelectionMask = regionSelectionMask;
      return self();
    }

    public B addRegionSelectionMask(int addRegionSelectionMask) {
      this.addRegionSelectionMask = addRegionSelectionMask;
      return self();
    }

    public B regionSelectionCompleteMask(int regionSelectionCompleteMask) {
      this.regionSelectionCompleteMask = regionSelectionCompleteMask;
      return self();
    }

    public B addRegionSelectionCompleteMask(int addRegionSelectionCompleteMask) {
      this.addRegionSelectionCompleteMask = addRegionSelectionCompleteMask;
      return self();
    }

    public B translatingMask(int translatingMask) {
      this.translatingMask = translatingMask;
      return self();
    }

    public B scalingMask(int scalingMask) {
      this.scalingMask = scalingMask;
      return self();
    }

    public B xAxisScalingMask(int xAxisScalingMask) {
      this.xAxisScalingMask = xAxisScalingMask;
      return self();
    }

    public B yAxisScalingMask(int yAxisScalingMask) {
      this.yAxisScalingMask = yAxisScalingMask;
      return self();
    }

    public T build() {
      return (T) new DefaultModalGraphMouse(in, out, vertexSelectionOnly);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected int singleSelectionMask;
  protected int addSingleSelectionMask;
  protected int regionSelectionMask;
  protected int addRegionSelectionMask;
  protected int regionSelectionCompleteMask;
  protected int addRegionSelectionCompleteMask;
  protected int translatingMask;
  protected int scalingMask;
  protected int xAxisScalingMask;
  protected int yAxisScalingMask;

  /** create an instance with default values */
  public DefaultModalGraphMouse() {
    this(new Builder<>());
  }

  /**
   * create an instance with passed values
   *
   * @param in override value for scale in
   * @param out override value for scale out
   */
  DefaultModalGraphMouse(float in, float out, boolean vertexSelectionOnly) {
    super(Mode.TRANSFORMING, in, out, vertexSelectionOnly);
    setModeKeyListener(new ModeKeyAdapter(this));
  }

  /** create an instance with default values */
  protected DefaultModalGraphMouse(Builder<V, E, ?, ?> builder) {
    this(
        builder.mode,
        builder.in,
        builder.out,
        builder.vertexSelectionOnly,
        builder.singleSelectionMask,
        builder.addSingleSelectionMask,
        builder.regionSelectionMask,
        builder.addRegionSelectionMask,
        builder.regionSelectionCompleteMask,
        builder.addRegionSelectionCompleteMask,
        builder.translatingMask,
        builder.scalingMask,
        builder.xAxisScalingMask,
        builder.yAxisScalingMask);
    //    this(builder.in, builder.out, builder.vertexSelectionOnly);
  }

  public DefaultModalGraphMouse(
      Mode mode,
      float in,
      float out,
      boolean vertexSelectionOnly,
      int singleSelectionMask,
      int addSingleSelectionMask,
      int regionSelectionMask,
      int addRegionSelectionMask,
      int regionSelectionCompleteMask,
      int addRegionSelectionCompleteMask,
      int translatingMask,
      int scalingMask,
      int xAxisScalingMask,
      int yAxisScalingMask) {
    super(mode, in, out, vertexSelectionOnly);
    this.singleSelectionMask = singleSelectionMask;
    this.addSingleSelectionMask = addSingleSelectionMask;
    this.regionSelectionMask = regionSelectionMask;
    this.addRegionSelectionMask = addRegionSelectionMask;
    this.regionSelectionCompleteMask = regionSelectionCompleteMask;
    this.addRegionSelectionCompleteMask = addRegionSelectionCompleteMask;
    this.translatingMask = translatingMask;
    this.scalingMask = scalingMask;
    this.xAxisScalingMask = xAxisScalingMask;
    this.yAxisScalingMask = yAxisScalingMask;
    setModeKeyListener(new ModeKeyAdapter(this));
  }
  /** create the plugins, and load the plugins for set mode */
  @Override
  public void loadPlugins() {
    selectingPlugin =
        SelectingGraphMousePlugin.builder()
            .singleSelectionMask(InputEvent.BUTTON1_DOWN_MASK)
            .addSingleSelectionMask(InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)
            .build();
    regionSelectingPlugin =
        RegionSelectingGraphMousePlugin.builder()
            .regionSelectionMask(InputEvent.BUTTON1_DOWN_MASK)
            .addRegionSelectionMask(InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)
            .regionSelectionCompleteMask(0)
            .addRegionSelectionCompleteMask(InputEvent.SHIFT_DOWN_MASK)
            .build();
    translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_DOWN_MASK);
    scalingPlugin =
        ScalingGraphMousePlugin.builder().scalingControl(new CrossoverScalingControl()).build();
    rotatingPlugin = new RotatingGraphMousePlugin();
    shearingPlugin = new ShearingGraphMousePlugin();
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

    @Override
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
