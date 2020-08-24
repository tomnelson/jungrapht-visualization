package org.jungrapht.visualization.control;

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

  /**
   * Build an instance of a DefaultGraphMouse
   *
   * @param <V>
   * @param <E>
   * @param <T>
   * @param <B>
   */
  public static class Builder<V, E, T extends DefaultGraphMouse, B extends Builder<V, E, T, B>>
      extends AbstractGraphMouse.Builder<T, B> {

    public T build() {
      return (T) new DefaultGraphMouse(in, out, vertexSelectionOnly);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  /** create an instance with default values */
  protected DefaultGraphMouse(Builder<V, E, ?, ?> builder) {
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
    super(in, out, vertexSelectionOnly);
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
    setPluginsLoaded();
  }

  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
  }
}
