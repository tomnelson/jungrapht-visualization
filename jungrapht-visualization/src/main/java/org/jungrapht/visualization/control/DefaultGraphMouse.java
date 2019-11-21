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

  /** create an instance with default values */
  public DefaultGraphMouse() {
    this(1.1f, 1 / 1.1f);
  }

  /**
   * create an instance with passed values
   *
   * @param in override value for scale in
   * @param out override value for scale out
   */
  public DefaultGraphMouse(float in, float out) {
    super(in, out);
    loadPlugins();
  }

  /** create the plugins, and load them */
  protected void loadPlugins() {
    scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
    add(new TranslatingGraphMousePlugin(InputEvent.BUTTON1_DOWN_MASK));
    pickingPlugin = new SelectingGraphMousePlugin<>();
    add(pickingPlugin);
    add(scalingPlugin);
  }

  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
  }
}
