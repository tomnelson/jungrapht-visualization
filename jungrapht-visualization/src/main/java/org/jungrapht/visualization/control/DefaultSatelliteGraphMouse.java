package org.jungrapht.visualization.control;

import java.awt.event.InputEvent;

/**
 * A Satellite version of the {@link DefaultGraphMouse}. Non-modal, Use CTRL-mouse to translate the
 * graph visualization
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class DefaultSatelliteGraphMouse<V, E> extends AbstractGraphMouse {

  /** create an instance with default values */
  public DefaultSatelliteGraphMouse() {
    this(1.1f, 1 / 1.1f);
  }

  /**
   * create an instance with passed values
   *
   * @param in override value for scale in
   * @param out override value for scale out
   */
  public DefaultSatelliteGraphMouse(float in, float out) {
    super(in, out);
    loadPlugins();
  }

  /** create the plugins, and load them */
  public void loadPlugins() {
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
