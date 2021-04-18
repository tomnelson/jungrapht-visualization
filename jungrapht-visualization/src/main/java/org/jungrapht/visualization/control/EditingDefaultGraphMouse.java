package org.jungrapht.visualization.control;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
public class EditingDefaultGraphMouse<V, E> extends DefaultGraphMouse<V, E>
    implements ModalGraphMouse {

  private static final Logger log = LoggerFactory.getLogger(EditingDefaultGraphMouse.class);
  /**
   * Build an instance of a EditingModalGraphMouse
   *
   * @param <V>
   * @param <E>
   * @param <T>
   * @param <B>
   */
  public static class Builder<
          V, E, T extends EditingDefaultGraphMouse<V, E>, B extends Builder<V, E, T, B>>
      extends DefaultGraphMouse.Builder<V, E, T, B> {

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
      return (T) new EditingDefaultGraphMouse<>(this);
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
  protected Mode mode;

  protected KeyListener modeKeyListener;

  public EditingDefaultGraphMouse(Builder<V, E, ?, ?> builder) {
    super(builder);
    this.vertexFactory = builder.vertexFactory;
    this.edgeFactory = builder.edgeFactory;
    this.multiLayerTransformerSupplier = builder.multiLayerTransformerSupplier;
    this.vertexLabelMap = builder.vertexLabelMapSupplier.get();
    this.edgeLabelMap = builder.edgeLabelMapSupplier.get();
    this.basicTransformer = builder.multiLayerTransformerSupplier.get();
    this.rc = builder.renderContextSupplier.get();
    setModeKeyListener(new ModeKeyAdapter(this));
  }

  /** create the plugins, and load the plugins for TRANSFORMING mode */
  @Override
  public void loadPlugins() {
    super.loadPlugins();

    editingPlugin = EditingGraphMousePlugin.builder(vertexFactory, edgeFactory).build();
    labelEditingPlugin = new LabelEditingGraphMousePlugin<>(vertexLabelMap, edgeLabelMap);
    annotatingPlugin = new AnnotatingGraphMousePlugin<>(rc);
    popupEditingPlugin = new EditingPopupGraphMousePlugin<>(vertexFactory, edgeFactory);
    setMode(this.mode);
  }

  /** setter for the Mode. */
  @Override
  public void setMode(Mode mode) {
    this.mode = mode;
    if (mode == Mode.DEFAULT) {
      setDefaultMode();
      //    } else if (mode == Mode.PICKING) {
      //      setDefaultMode();
    } else if (mode == Mode.EDITING) {
      setEditingMode();
    } else if (mode == Mode.ANNOTATING) {
      setAnnotatingMode();
    }
  }

  protected void setDefaultMode() {
    clear();

    add(selectingPlugin);
    add(regionSelectingPlugin);
    add(translatingPlugin);
    add(scalingPlugin);
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
    private char d = 'd';
    private char e = 'e';
    private char a = 'a';
    protected ModalGraphMouse graphMouse;

    public ModeKeyAdapter(ModalGraphMouse graphMouse) {
      this.graphMouse = graphMouse;
    }

    public ModeKeyAdapter(char d, char e, char a, ModalGraphMouse graphMouse) {
      this.d = d;
      this.e = e;
      this.a = a;
      this.graphMouse = graphMouse;
    }

    @Override
    public void keyTyped(KeyEvent event) {
      char keyChar = event.getKeyChar();
      if (keyChar == d) {
        ((Component) event.getSource())
            .setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        graphMouse.setMode(Mode.DEFAULT);
        //      } else if (keyChar == p) {
        //        ((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        //        graphMouse.setMode(Mode.PICKING);
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

  @Override
  public Mode getMode() {
    return this.mode;
  }

  /** @return the modeKeyListener */
  @Override
  public KeyListener getModeKeyListener() {
    return modeKeyListener;
  }

  /** @param modeKeyListener the modeKeyListener to set */
  public void setModeKeyListener(KeyListener modeKeyListener) {
    this.modeKeyListener = modeKeyListener;
  }
}
