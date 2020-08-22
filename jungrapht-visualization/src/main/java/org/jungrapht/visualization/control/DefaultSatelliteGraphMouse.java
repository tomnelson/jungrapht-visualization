package org.jungrapht.visualization.control;

import java.awt.event.InputEvent;

/**
 * A Satellite version of the {@link DefaultGraphMouse}. Non-modal, Use CTRL-mouse to translate the
 * graph visualization
 *
 * @author Tom Nelson
 */
public class DefaultSatelliteGraphMouse extends AbstractGraphMouse {

  public static class Builder<
          V, E, T extends DefaultSatelliteGraphMouse, B extends Builder<V, E, T, B>>
      extends AbstractGraphMouse.Builder<T, B> {

    public T build() {
      return (T) new DefaultSatelliteGraphMouse(in, out, vertexSelectionOnly);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  /** create an instance with default values */
  public DefaultSatelliteGraphMouse() {
    this(1.1f, 1 / 1.1f, false);
  }

  /**
   * create an instance with passed values
   *
   * @param in override value for scale in
   * @param out override value for scale out
   */
  public DefaultSatelliteGraphMouse(float in, float out, boolean vertexSelectionOnly) {
    super(in, out, vertexSelectionOnly);
  }

  /** create the plugins, and load them */
  public void loadPlugins() {
    super.loadPlugins();
    scalingPlugin = new SatelliteScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
    add(new SatelliteTranslatingGraphMousePlugin(InputEvent.BUTTON1_DOWN_MASK));
    pickingPlugin = new SelectingGraphMousePlugin<>();
    add(pickingPlugin);
    add(scalingPlugin);
  }

  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
  }
}
