/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 *
 */
package org.jungrapht.visualization.annotations;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.control.*;

/**
 * a graph mouse that supplies an annotations mode
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class AnnotatingModalGraphMouse<V, E> extends AbstractModalGraphMouse
    implements ModalGraphMouse, ItemSelectable {

  /**
   * Build an instance of a EditingModalGraphMouse
   *
   * @param <V>
   * @param <E>
   * @param <T>
   * @param <B>
   */
  public static class Builder<
          V, E, T extends AnnotatingModalGraphMouse, B extends Builder<V, E, T, B>>
      extends AbstractModalGraphMouse.Builder<T, B> {

    protected Supplier<MultiLayerTransformer> multiLayerTransformerSupplier;
    protected AnnotatingGraphMousePlugin<V, E> annotatingPlugin;

    public B multiLayerTransformerSupplier(
        Supplier<MultiLayerTransformer> multiLayerTransformerSupplier) {
      this.multiLayerTransformerSupplier = multiLayerTransformerSupplier;
      return self();
    }

    public B annotatingPlugin(AnnotatingGraphMousePlugin<V, E> annotatingGraphMousePlugin) {
      this.annotatingPlugin = annotatingGraphMousePlugin;
      return self();
    }

    public T build() {
      return (T) new AnnotatingModalGraphMouse<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected AnnotatingGraphMousePlugin<V, E> annotatingPlugin;
  protected MultiLayerTransformer basicTransformer;

  public AnnotatingModalGraphMouse() {
    this(new Builder<>());
  }

  protected AnnotatingModalGraphMouse(Builder<V, E, ?, ?> builder) {
    super(builder);
    this.basicTransformer = builder.multiLayerTransformerSupplier.get();
    this.annotatingPlugin = builder.annotatingPlugin;
  }

  /**
   * Create an instance with default values for scale in (1.1) and scale out (1/1.1).
   *
   * @param annotatingPlugin the plugin used by this class for annotating
   */
  //  AnnotatingModalGraphMouse(
  //      Supplier<MultiLayerTransformer> multiLayerTransformerSupplier,
  //      AnnotatingGraphMousePlugin<V, E> annotatingPlugin) {
  //    this(multiLayerTransformerSupplier, annotatingPlugin, 1.1f, 1 / 1.1f, false);
  //  }

  /**
   * Create an instance with the specified scale in and scale out values.
   *
   * @param annotatingPlugin the plugin used by this class for annotating
   * @param in override value for scale in
   * @param out override value for scale out
   */
  //  AnnotatingModalGraphMouse(
  //      Supplier<MultiLayerTransformer> multiLayerTransformerSupplier,
  //      AnnotatingGraphMousePlugin<V, E> annotatingPlugin,
  //      float in,
  //      float out,
  //      boolean vertexSelectionOnly) {
  //    super(in, out, vertexSelectionOnly);
  //    this.basicTransformer = multiLayerTransformerSupplier.get();
  //    this.annotatingPlugin = annotatingPlugin;
  //    this.mode = Mode.ANNOTATING;
  //    setModeKeyListener(new ModeKeyAdapter(this));
  //  }

  /** create the plugins, and load the plugins for TRANSFORMING mode */
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

  /** setter for the Mode. */
  @Override
  public void setMode(Mode mode) {
    this.mode = mode;
    if (mode == Mode.TRANSFORMING) {
      setTransformingMode();
    } else if (mode == Mode.PICKING) {
      setPickingMode();
    } else if (mode == Mode.ANNOTATING) {
      setAnnotatingMode();
    }
  }

  @Override
  protected void setPickingMode() {
    clear();
    add(scalingPlugin);
    add(selectingPlugin);
    add(animatedPickingPlugin);
  }

  @Override
  protected void setTransformingMode() {
    clear();
    add(scalingPlugin);
    add(translatingPlugin);
    add(rotatingPlugin);
    add(shearingPlugin);
  }

  //  protected void setEditingMode() {
  //    clear();
  //    add(scalingPlugin);
  ////    remove(selectingPlugin);
  ////    remove(animatedPickingPlugin);
  ////    remove(translatingPlugin);
  ////    remove(rotatingPlugin);
  ////    remove(shearingPlugin);
  ////    remove(annotatingPlugin);
  //  }

  protected void setAnnotatingMode() {
    clear();
    add(scalingPlugin);
    add(annotatingPlugin);
  }

  public static class ModeKeyAdapter extends KeyAdapter {
    private char t = 't';
    private char p = 'p';
    private char a = 'a';
    protected ModalGraphMouse graphMouse;

    public ModeKeyAdapter(ModalGraphMouse graphMouse) {
      this.graphMouse = graphMouse;
    }

    public ModeKeyAdapter(char t, char p, char a, ModalGraphMouse graphMouse) {
      this.t = t;
      this.p = p;
      this.a = a;
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
      } else if (keyChar == a) {
        ((Component) event.getSource())
            .setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        graphMouse.setMode(Mode.ANNOTATING);
      }
    }
  }
}
