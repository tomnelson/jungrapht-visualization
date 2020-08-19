package org.jungrapht.visualization.control;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.event.InputEvent;

/**
 * The DefaultGraphMouse does not have 'transforming/selecting' modes. It has 3 plugins that are
 * always active:
 *
 * <ul>
 *   <li>{@link ScalingGraphMousePlugin} operates via mouse wheel
 *   <li>{@link TranslatingGraphMousePlugin} operated via mouse-drag
 *   <li>{@link SelectingGraphMousePlugin} operates via CTRL+mouse gestures
 * </ul>
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class DefaultGraphMouse<V, E> extends AbstractGraphMouse {

  private static final String VERTEX_SELECTION_ONLY = PREFIX + "vertexSelectionOnly";

  public static class Builder<V, E, T extends DefaultGraphMouse, B extends Builder<V, E, T, B>> {
    private boolean vertexSelectionOnly =
        Boolean.parseBoolean(System.getProperty(VERTEX_SELECTION_ONLY, "false"));
    private float in = 1.1f;
    private float out = 1.1f;

    public B self() {
      return (B) this;
    }

    public B in(float in) {
      this.in = in;
      return self();
    }

    public B out(float out) {
      this.out = out;
      return self();
    }

    public T build() {
      return (T) new DefaultGraphMouse(in, out, vertexSelectionOnly);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  private boolean vertexSelectionOnly;

  /** create an instance with default values */
  DefaultGraphMouse(Builder<V, E, ?, ?> builder) {
    this(builder.in, builder.out, builder.vertexSelectionOnly);
  }

  /** create an instance with default values */
  public DefaultGraphMouse() {
    this(new Builder<>());
  }

  /**
   * create an instance with passed values
   *
   * @param in override value for scale in
   * @param out override value for scale out
   */
  DefaultGraphMouse(float in, float out, boolean vertexSelectionOnly) {
    super(in, out);
    this.vertexSelectionOnly = vertexSelectionOnly;
  }

  /** create the plugins, and load them */
  @Override
  public void loadPlugins() {
    scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
    add(new TranslatingGraphMousePlugin(InputEvent.BUTTON1_DOWN_MASK));
    pickingPlugin =
        vertexSelectionOnly
            ? new VertexSelectingGraphMousePlugin<>()
            : new SelectingGraphMousePlugin<>();
    add(pickingPlugin);
    add(scalingPlugin);
  }

  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
  }
}
