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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.plaf.basic.BasicIconFactory;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.control.AbstractModalGraphMouse;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.RegionSelectingGraphMousePlugin;
import org.jungrapht.visualization.control.RotatingGraphMousePlugin;
import org.jungrapht.visualization.control.ScalingGraphMousePlugin;
import org.jungrapht.visualization.control.SelectingGraphMousePlugin;
import org.jungrapht.visualization.control.ShearingGraphMousePlugin;
import org.jungrapht.visualization.control.TranslatingGraphMousePlugin;

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

  public AnnotatingModalGraphMouse(Builder<V, E, ?, ?> builder) {
    super(builder);
    this.basicTransformer = builder.multiLayerTransformerSupplier.get();
    this.annotatingPlugin = builder.annotatingPlugin;
  }

  /**
   * Create an instance with default values for scale in (1.1) and scale out (1/1.1).
   *
   * @param annotatingPlugin the plugin used by this class for annotating
   */
  AnnotatingModalGraphMouse(
      Supplier<MultiLayerTransformer> multiLayerTransformerSupplier,
      AnnotatingGraphMousePlugin<V, E> annotatingPlugin) {
    this(multiLayerTransformerSupplier, annotatingPlugin, 1.1f, 1 / 1.1f, false);
  }

  /**
   * Create an instance with the specified scale in and scale out values.
   *
   * @param annotatingPlugin the plugin used by this class for annotating
   * @param in override value for scale in
   * @param out override value for scale out
   */
  AnnotatingModalGraphMouse(
      Supplier<MultiLayerTransformer> multiLayerTransformerSupplier,
      AnnotatingGraphMousePlugin<V, E> annotatingPlugin,
      float in,
      float out,
      boolean vertexSelectionOnly) {
    super(in, out, vertexSelectionOnly);
    this.basicTransformer = multiLayerTransformerSupplier.get();
    this.annotatingPlugin = annotatingPlugin;
    setModeKeyListener(new ModeKeyAdapter(this));
  }

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

    //    super.loadPlugins();
    //    this.pickingPlugin =
    //        new PrevSelectingGraphMousePlugin<>(
    //            InputEvent.BUTTON1_DOWN_MASK, 0, InputEvent.SHIFT_DOWN_MASK);
    //    this.animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<V, E>();
    //    this.translatingPlugin = new PrevTranslatingGraphMousePlugin(InputEvent.BUTTON1_DOWN_MASK);
    //    this.scalingPlugin = new PrevScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
    //    this.rotatingPlugin = new RotatingGraphMousePlugin();
    //    this.shearingPlugin = new ShearingGraphMousePlugin();
    add(scalingPlugin);
    setMode(Mode.TRANSFORMING);
  }

  /** setter for the Mode. */
  @Override
  public void setMode(Mode mode) {
    if (this.mode != mode) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this.mode, ItemEvent.DESELECTED));
      this.mode = mode;
      if (mode == Mode.TRANSFORMING) {
        setTransformingMode();
      } else if (mode == Mode.PICKING) {
        setPickingMode();
      } else if (mode == Mode.ANNOTATING) {
        setAnnotatingMode();
      }
      if (modeBox != null) {
        modeBox.setSelectedItem(mode);
      }
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, mode, ItemEvent.SELECTED));
    }
  }

  @Override
  protected void setPickingMode() {
    remove(translatingPlugin);
    remove(rotatingPlugin);
    remove(shearingPlugin);
    remove(annotatingPlugin);
    add(selectingPlugin);
    add(animatedPickingPlugin);
  }

  @Override
  protected void setTransformingMode() {
    remove(selectingPlugin);
    remove(animatedPickingPlugin);
    remove(annotatingPlugin);
    add(translatingPlugin);
    add(rotatingPlugin);
    add(shearingPlugin);
  }

  protected void setEditingMode() {
    remove(selectingPlugin);
    remove(animatedPickingPlugin);
    remove(translatingPlugin);
    remove(rotatingPlugin);
    remove(shearingPlugin);
    remove(annotatingPlugin);
  }

  protected void setAnnotatingMode() {
    remove(selectingPlugin);
    remove(animatedPickingPlugin);
    remove(translatingPlugin);
    remove(rotatingPlugin);
    remove(shearingPlugin);
    add(annotatingPlugin);
  }

  /** @return Returns the modeBox. */
  @Override
  public JComboBox<Mode> getModeComboBox() {
    if (modeBox == null) {
      modeBox = new JComboBox<>(new Mode[] {Mode.TRANSFORMING, Mode.PICKING, Mode.ANNOTATING});
      modeBox.addItemListener(getModeListener());
    }
    modeBox.setSelectedItem(mode);
    return modeBox;
  }

  /**
   * create (if necessary) and return a menu that will change the mode
   *
   * @return the menu
   */
  @Override
  public JMenu getModeMenu() {
    if (modeMenu == null) {
      modeMenu = new JMenu(); // {
      Icon icon = BasicIconFactory.getMenuArrowIcon();
      modeMenu.setIcon(BasicIconFactory.getMenuArrowIcon());
      modeMenu.setPreferredSize(new Dimension(icon.getIconWidth() + 10, icon.getIconHeight() + 10));

      final JRadioButtonMenuItem transformingButton =
          new JRadioButtonMenuItem(Mode.TRANSFORMING.toString());
      transformingButton.addItemListener(
          e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
              setMode(Mode.TRANSFORMING);
            }
          });

      final JRadioButtonMenuItem pickingButton = new JRadioButtonMenuItem(Mode.PICKING.toString());
      pickingButton.addItemListener(
          e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
              setMode(Mode.PICKING);
            }
          });

      ButtonGroup radio = new ButtonGroup();
      radio.add(transformingButton);
      radio.add(pickingButton);
      transformingButton.setSelected(true);
      modeMenu.add(transformingButton);
      modeMenu.add(pickingButton);
      modeMenu.setToolTipText("Menu for setting Mouse Mode");
      addItemListener(
          e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
              if (e.getItem() == Mode.TRANSFORMING) {
                transformingButton.setSelected(true);
              } else if (e.getItem() == Mode.PICKING) {
                pickingButton.setSelected(true);
              }
            }
          });
    }
    return modeMenu;
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
