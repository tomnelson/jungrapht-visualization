package org.jungrapht.visualization.control;

import java.awt.event.InputEvent;

/**
 * an implementation of the PluggableGraphMouse that includes plugins for manipulating a view that
 * is using a LensTransformer.
 *
 * @author Tom Nelson
 */
public class DefaultLensGraphMouse<V, E> extends AbstractGraphMouse implements LensGraphMouse {

  /** not included in the base class */
  protected LensMagnificationGraphMousePlugin magnificationPlugin;

  public DefaultLensGraphMouse() {
    this(1.1f, 1 / 1.1f);
  }

  public DefaultLensGraphMouse(float in, float out) {
    this(in, out, new LensMagnificationGraphMousePlugin());
  }

  public DefaultLensGraphMouse(LensMagnificationGraphMousePlugin magnificationPlugin) {
    this(1.1f, 1 / 1.1f, magnificationPlugin);
  }

  public DefaultLensGraphMouse(
      float in, float out, LensMagnificationGraphMousePlugin magnificationPlugin) {
    super(in, out);
    this.magnificationPlugin = magnificationPlugin;
    scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
    loadPlugins();
  }

  protected void loadPlugins() {
    add(
        new LensTranslatingGraphMousePlugin(
            InputEvent.BUTTON1_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
    add(new LensSelectingGraphMousePlugin<V, E>());
    add(magnificationPlugin);
    add(scalingPlugin);
  }
}
