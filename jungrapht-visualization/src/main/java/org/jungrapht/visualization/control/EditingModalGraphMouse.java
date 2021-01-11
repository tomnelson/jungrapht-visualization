package org.jungrapht.visualization.control;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.ItemSelectable;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.annotations.AnnotatingGraphMousePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public class EditingModalGraphMouse<V, E> extends AbstractModalGraphMouse
    implements ModalGraphMouse, ItemSelectable {

  private static final Logger log = LoggerFactory.getLogger(EditingModalGraphMouse.class);
  /**
   * Build an instance of a EditingModalGraphMouse
   *
   * @param <V>
   * @param <E>
   * @param <T>
   * @param <B>
   */
  public static class Builder<V, E, T extends EditingModalGraphMouse, B extends Builder<V, E, T, B>>
      extends AbstractModalGraphMouse.Builder<T, B> {

    protected Supplier<V> vertexFactory;
    protected Supplier<E> edgeFactory;
    protected Supplier<Map<V, String>> vertexLabelMapSupplier;
    protected Supplier<Map<E, String>> edgeLabelMapSupplier;
    protected Supplier<MultiLayerTransformer> multiLayerTransformerSupplier;
    protected Supplier<RenderContext<V, E>> renderContextSupplier;

    public B vertexFactory(Supplier<V> vertexFactory) {
      this.vertexFactory = vertexFactory;
      return self();
    }

    public B edgeFactory(Supplier<E> edgeFactory) {
      this.edgeFactory = edgeFactory;
      return self();
    }

    public B vertexLabelMapSupplier(Supplier<Map<V, String>> vertexLabelMapSupplier) {
      this.vertexLabelMapSupplier = vertexLabelMapSupplier;
      return self();
    }

    public B edgeLabelMapSupplier(Supplier<Map<E, String>> edgeLabelMapSupplier) {
      this.edgeLabelMapSupplier = edgeLabelMapSupplier;
      return self();
    }

    public B multiLayerTransformerSupplier(
        Supplier<MultiLayerTransformer> multiLayerTransformerSupplier) {
      this.multiLayerTransformerSupplier = multiLayerTransformerSupplier;
      return self();
    }

    public B renderContextSupplier(Supplier<RenderContext<V, E>> renderContextSupplier) {
      this.renderContextSupplier = renderContextSupplier;
      return self();
    }

    public T build() {
      return (T) new EditingModalGraphMouse<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected Supplier<V> vertexFactory;
  protected Supplier<E> edgeFactory;
  protected Supplier<MultiLayerTransformer> multiLayerTransformerSupplier;
  protected Map<V, String> vertexLabelMap;
  protected Map<E, String> edgeLabelMap;
  protected EditingGraphMousePlugin<V, E> editingPlugin;
  protected LabelEditingGraphMousePlugin<V, E> labelEditingPlugin;
  protected EditingPopupGraphMousePlugin<V, E> popupEditingPlugin;
  protected AnnotatingGraphMousePlugin<V, E> annotatingPlugin;
  protected MultiLayerTransformer basicTransformer;
  protected RenderContext<V, E> rc;

  public EditingModalGraphMouse(Builder<V, E, ?, ?> builder) {
    this(
        builder.renderContextSupplier.get(),
        builder.multiLayerTransformerSupplier,
        builder.vertexFactory,
        builder.edgeFactory,
        builder.vertexLabelMapSupplier,
        builder.edgeLabelMapSupplier,
        1.1f,
        1 / 1.1f,
        false);
  }

  /**
   * Creates an instance with the specified rendering context and vertex/edge factories, and with
   * default zoom in/out values of 1.1 and 1/1.1.
   *
   * @param vertexFactory used to construct vertices
   * @param edgeFactory used to construct edges
   */
  EditingModalGraphMouse(
      RenderContext<V, E> rc,
      Supplier<MultiLayerTransformer> multiLayerTransformerSupplier,
      Supplier<V> vertexFactory,
      Supplier<E> edgeFactory) {
    this(
        rc,
        multiLayerTransformerSupplier,
        vertexFactory,
        edgeFactory,
        HashMap::new,
        HashMap::new,
        1.1f,
        1 / 1.1f,
        false);
  }
  /**
   * Creates an instance with the specified rendering context and vertex/edge factories, and with
   * default zoom in/out values of 1.1 and 1/1.1.
   *
   * @param vertexFactory used to construct vertices
   * @param edgeFactory used to construct edges
   */
  EditingModalGraphMouse(
      RenderContext<V, E> rc,
      Supplier<MultiLayerTransformer> multiLayerTransformerSupplier,
      Supplier<V> vertexFactory,
      Supplier<E> edgeFactory,
      Supplier<Map<V, String>> vertexLabelMapSupplier,
      Supplier<Map<E, String>> edgeLabelMapSupplier) {
    this(
        rc,
        multiLayerTransformerSupplier,
        vertexFactory,
        edgeFactory,
        vertexLabelMapSupplier,
        edgeLabelMapSupplier,
        1.1f,
        1 / 1.1f,
        false);
  }

  /**
   * Creates an instance with the specified rendering context and vertex/edge factories, and with
   * the specified zoom in/out values.
   *
   * @param vertexFactory used to construct vertices
   * @param edgeFactory used to construct edges
   * @param in amount to zoom in by for each action
   * @param out amount to zoom out by for each action
   */
  EditingModalGraphMouse(
      RenderContext<V, E> rc,
      Supplier<MultiLayerTransformer> multiLayerTransformerSupplier,
      Supplier<V> vertexFactory,
      Supplier<E> edgeFactory,
      Supplier<Map<V, String>> vertexLabelMapSupplier,
      Supplier<Map<E, String>> edgeLabelMapSupplier,
      float in,
      float out,
      boolean vertexSelectionOnly) {
    super(in, out, vertexSelectionOnly);
    this.rc = rc;
    this.vertexFactory = vertexFactory;
    this.edgeFactory = edgeFactory;
    this.vertexLabelMap = vertexLabelMapSupplier.get();
    this.edgeLabelMap = edgeLabelMapSupplier.get();
    this.basicTransformer = multiLayerTransformerSupplier.get();
    this.mode = Mode.EDITING;
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
    editingPlugin = new EditingGraphMousePlugin<>(vertexFactory, edgeFactory);
    labelEditingPlugin = new LabelEditingGraphMousePlugin<>(vertexLabelMap, edgeLabelMap);
    annotatingPlugin = new AnnotatingGraphMousePlugin<>(rc);
    popupEditingPlugin = new EditingPopupGraphMousePlugin<>(vertexFactory, edgeFactory);
    add(scalingPlugin);
    setMode(this.mode);
  }

  /** setter for the Mode. */
  @Override
  public void setMode(Mode mode) {
    //    if (this.mode != mode) {
    //      fireItemStateChanged(
    //          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this.mode, ItemEvent.DESELECTED));
    this.mode = mode;
    if (mode == Mode.TRANSFORMING) {
      setTransformingMode();
    } else if (mode == Mode.PICKING) {
      setPickingMode();
    } else if (mode == Mode.EDITING) {
      setEditingMode();
    } else if (mode == Mode.ANNOTATING) {
      setAnnotatingMode();
    }
    //      if (modeBox != null) {
    //        modeBox.setSelectedItem(mode);
    //      }
    //      fireItemStateChanged(
    //          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, mode, ItemEvent.SELECTED));
    //    }
  }

  @Override
  protected void setPickingMode() {
    remove(translatingPlugin);
    remove(rotatingPlugin);
    remove(shearingPlugin);
    remove(editingPlugin);
    remove(annotatingPlugin);
    add(selectingPlugin);
    add(animatedPickingPlugin);
    add(labelEditingPlugin);
    add(popupEditingPlugin);
  }

  @Override
  protected void setTransformingMode() {
    remove(selectingPlugin);
    remove(animatedPickingPlugin);
    remove(editingPlugin);
    remove(annotatingPlugin);
    add(translatingPlugin);
    add(rotatingPlugin);
    add(shearingPlugin);
    add(labelEditingPlugin);
    add(popupEditingPlugin);
  }

  protected void setEditingMode() {
    remove(selectingPlugin);
    remove(animatedPickingPlugin);
    remove(translatingPlugin);
    remove(rotatingPlugin);
    remove(shearingPlugin);
    remove(labelEditingPlugin);
    remove(annotatingPlugin);
    add(labelEditingPlugin);
    add(editingPlugin);
    add(popupEditingPlugin);
  }

  protected void setAnnotatingMode() {
    remove(selectingPlugin);
    remove(animatedPickingPlugin);
    remove(translatingPlugin);
    remove(rotatingPlugin);
    remove(shearingPlugin);
    remove(labelEditingPlugin);
    remove(editingPlugin);
    remove(popupEditingPlugin);
    add(annotatingPlugin);
  }

  //  /** @return the modeBox. */
  //  @Override
  //  public JComboBox<Mode> getModeComboBox() {
  //    if (modeBox == null) {
  //      modeBox =
  //          new JComboBox<>(
  //              new Mode[] {Mode.TRANSFORMING, Mode.PICKING, Mode.EDITING, Mode.ANNOTATING});
  //      modeBox.addItemListener(getModeListener());
  //    }
  //    modeBox.setSelectedItem(mode);
  //    return modeBox;
  //  }

  //  /**
  //   * create (if necessary) and return a menu that will change the mode
  //   *
  //   * @return the menu
  //   */
  //  @Override
  //  public JMenu getModeMenu() {
  //    if (modeMenu == null) {
  //      modeMenu = new JMenu(); // {
  //      Icon icon = BasicIconFactory.getMenuArrowIcon();
  //      modeMenu.setIcon(BasicIconFactory.getMenuArrowIcon());
  //      modeMenu.setPreferredSize(new Dimension(icon.getIconWidth() + 10, icon.getIconHeight() + 10));
  //
  //      final JRadioButtonMenuItem transformingButton =
  //          new JRadioButtonMenuItem(Mode.TRANSFORMING.toString());
  //      transformingButton.addItemListener(
  //          e -> {
  //            if (e.getStateChange() == ItemEvent.SELECTED) {
  //              setMode(Mode.TRANSFORMING);
  //            }
  //          });
  //
  //      final JRadioButtonMenuItem pickingButton = new JRadioButtonMenuItem(Mode.PICKING.toString());
  //      pickingButton.addItemListener(
  //          e -> {
  //            if (e.getStateChange() == ItemEvent.SELECTED) {
  //              setMode(Mode.PICKING);
  //            }
  //          });
  //
  //      final JRadioButtonMenuItem editingButton = new JRadioButtonMenuItem(Mode.EDITING.toString());
  //      editingButton.addItemListener(
  //          e -> {
  //            if (e.getStateChange() == ItemEvent.SELECTED) {
  //              setMode(Mode.EDITING);
  //            }
  //          });
  //
  //      ButtonGroup radio = new ButtonGroup();
  //      radio.add(transformingButton);
  //      radio.add(pickingButton);
  //      radio.add(editingButton);
  //      transformingButton.setSelected(true);
  //      modeMenu.add(transformingButton);
  //      modeMenu.add(pickingButton);
  //      modeMenu.add(editingButton);
  //      modeMenu.setToolTipText("Menu for setting Mouse Mode");
  //      addItemListener(
  //          e -> {
  //            if (e.getStateChange() == ItemEvent.SELECTED) {
  //              if (e.getItem() == Mode.TRANSFORMING) {
  //                transformingButton.setSelected(true);
  //              } else if (e.getItem() == Mode.PICKING) {
  //                pickingButton.setSelected(true);
  //              } else if (e.getItem() == Mode.EDITING) {
  //                editingButton.setSelected(true);
  //              }
  //            }
  //          });
  //    }
  //    return modeMenu;
  //  }

  public static class ModeKeyAdapter extends KeyAdapter {
    private char t = 't';
    private char p = 'p';
    private char e = 'e';
    private char a = 'a';
    protected ModalGraphMouse graphMouse;

    public ModeKeyAdapter(ModalGraphMouse graphMouse) {
      this.graphMouse = graphMouse;
    }

    public ModeKeyAdapter(char t, char p, char e, char a, ModalGraphMouse graphMouse) {
      this.t = t;
      this.p = p;
      this.e = e;
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
      } else if (keyChar == e) {
        ((Component) event.getSource())
            .setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        graphMouse.setMode(Mode.EDITING);
      } else if (keyChar == a) {
        ((Component) event.getSource())
            .setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        graphMouse.setMode(Mode.ANNOTATING);
      }
    }
  }

  /** @return the annotatingPlugin */
  public AnnotatingGraphMousePlugin<V, E> getAnnotatingPlugin() {
    return annotatingPlugin;
  }

  /** @return the editingPlugin */
  public EditingGraphMousePlugin<V, E> getEditingPlugin() {
    return editingPlugin;
  }

  /** @return the labelEditingPlugin */
  public LabelEditingGraphMousePlugin<V, E> getLabelEditingPlugin() {
    return labelEditingPlugin;
  }

  /** @return the popupEditingPlugin */
  public EditingPopupGraphMousePlugin<V, E> getPopupEditingPlugin() {
    return popupEditingPlugin;
  }
}
