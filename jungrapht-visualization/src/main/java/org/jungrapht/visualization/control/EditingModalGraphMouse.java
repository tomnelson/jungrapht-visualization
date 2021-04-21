package org.jungrapht.visualization.control;

import java.awt.Component;
import java.awt.Cursor;
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
public class EditingModalGraphMouse<V, E> extends DefaultModalGraphMouse<V, E>
    implements ModalGraphMouse {

  private static final Logger log = LoggerFactory.getLogger(EditingModalGraphMouse.class);
  /**
   * Build an instance of a EditingModalGraphMouse
   *
   * @param <V>
   * @param <E>
   * @param <T>
   * @param <B>
   */
  public static class Builder<
          V, E, T extends EditingModalGraphMouse<V, E>, B extends Builder<V, E, T, B>>
      extends DefaultModalGraphMouse.Builder<V, E, T, B> {

    protected Supplier<V> vertexFactory;
    protected Supplier<E> edgeFactory;
    protected Supplier<Map<V, String>> vertexLabelMapSupplier = HashMap::new;
    protected Supplier<Map<E, String>> edgeLabelMapSupplier = HashMap::new;
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
    super(builder);
    this.vertexFactory = builder.vertexFactory;
    this.edgeFactory = builder.edgeFactory;
    this.multiLayerTransformerSupplier = builder.multiLayerTransformerSupplier;
    this.vertexLabelMap = builder.vertexLabelMapSupplier.get();
    this.edgeLabelMap = builder.edgeLabelMapSupplier.get();
    this.basicTransformer = builder.multiLayerTransformerSupplier.get();
    this.rc = builder.renderContextSupplier.get();
  }

  /** create the plugins, and load the plugins for TRANSFORMING mode */
  @Override
  public void loadPlugins() {
    selectingPlugin =
        SelectingGraphMousePlugin.builder()
            .singleSelectionMask(InputEvent.BUTTON1_DOWN_MASK)
            .toggleSingleSelectionMask(InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)
            .build();
    regionSelectingPlugin =
        RegionSelectingGraphMousePlugin.builder()
            .regionSelectionMask(InputEvent.BUTTON1_DOWN_MASK)
            .toggleRegionSelectionMask(InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)
            .regionSelectionCompleteMask(0)
            .toggleRegionSelectionCompleteMask(InputEvent.SHIFT_DOWN_MASK)
            .build();
    translatingPlugin =
        TranslatingGraphMousePlugin.builder().translatingMask(translatingMask).build();
    scalingPlugin =
        ScalingGraphMousePlugin.builder().scalingControl(new CrossoverScalingControl()).build();
    rotatingPlugin = new RotatingGraphMousePlugin();
    shearingPlugin = new ShearingGraphMousePlugin();
    editingPlugin = EditingGraphMousePlugin.builder(vertexFactory, edgeFactory).build();
    labelEditingPlugin = new LabelEditingGraphMousePlugin<>(vertexLabelMap::put, edgeLabelMap::put);
    annotatingPlugin = new AnnotatingGraphMousePlugin<>(rc);
    popupEditingPlugin = new EditingPopupGraphMousePlugin<>(vertexFactory, edgeFactory);
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
    } else if (mode == Mode.EDITING) {
      setEditingMode();
    } else if (mode == Mode.ANNOTATING) {
      setAnnotatingMode();
    }
  }

  @Override
  protected void setPickingMode() {
    clear();
    add(scalingPlugin);
    add(selectingPlugin);
    add(regionSelectingPlugin);
    add(animatedPickingPlugin);
    add(labelEditingPlugin);
    add(popupEditingPlugin);
  }

  @Override
  protected void setTransformingMode() {
    clear();
    add(scalingPlugin);
    add(translatingPlugin);
    add(rotatingPlugin);
    add(shearingPlugin);
    add(labelEditingPlugin);
    add(popupEditingPlugin);
  }

  protected void setEditingMode() {
    clear();
    add(scalingPlugin);
    add(labelEditingPlugin);
    add(editingPlugin);
    add(popupEditingPlugin);
  }

  protected void setAnnotatingMode() {
    clear();
    add(scalingPlugin);
    add(annotatingPlugin);
  }

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
