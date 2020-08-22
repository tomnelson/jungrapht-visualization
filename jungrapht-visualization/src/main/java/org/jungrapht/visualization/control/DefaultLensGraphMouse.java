package org.jungrapht.visualization.control;

import java.awt.event.InputEvent;

/**
 * an implementation of the PluggableGraphMouse that includes plugins for manipulating a view that
 * is using a LensTransformer.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class DefaultLensGraphMouse<V, E> extends AbstractGraphMouse implements LensGraphMouse {

  /**
   * Build an instance of a DefaultGraphMouse
   *
   * @param <T>
   * @param <B>
   */
  public static class Builder<V, E, T extends DefaultLensGraphMouse, B extends Builder<V, E, T, B>>
      extends AbstractGraphMouse.Builder<T, B> {

    protected LensMagnificationGraphMousePlugin magnificationPlugin;

    protected Builder(LensMagnificationGraphMousePlugin magnificationPlugin) {
      this.magnificationPlugin = magnificationPlugin;
    }

    public B magnificationPlugin(LensMagnificationGraphMousePlugin magnificationPlugin) {
      this.magnificationPlugin = magnificationPlugin;
      return self();
    }

    public T build() {
      return (T) new DefaultLensGraphMouse(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder(
      LensMagnificationGraphMousePlugin magnificationPlugin) {
    return new Builder<>(magnificationPlugin);
  }

  /** not included in the base class */
  protected LensMagnificationGraphMousePlugin magnificationPlugin;

  protected LensSelectingGraphMousePlugin<V, E> lensSelectingGraphMousePlugin;
  protected LensKillingGraphMousePlugin lensKillingGraphMousePlugin;

  public DefaultLensGraphMouse() {
    this(new Builder<>(new LensMagnificationGraphMousePlugin()));
  }

  DefaultLensGraphMouse(Builder<V, E, ?, ?> builder) {
    this(builder.in, builder.out, builder.vertexSelectionOnly, builder.magnificationPlugin);
  }

  DefaultLensGraphMouse(float in, float out) {
    this(in, out, false, new LensMagnificationGraphMousePlugin());
  }

  public DefaultLensGraphMouse(LensMagnificationGraphMousePlugin magnificationPlugin) {
    this(1.1f, 1 / 1.1f, false, magnificationPlugin);
  }

  DefaultLensGraphMouse(
      float in,
      float out,
      boolean vertexSelectionOnly,
      LensMagnificationGraphMousePlugin magnificationPlugin) {
    super(in, out, vertexSelectionOnly);
    this.magnificationPlugin = magnificationPlugin;
    this.scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
    this.lensSelectingGraphMousePlugin = new LensSelectingGraphMousePlugin<>();
    this.lensKillingGraphMousePlugin = new LensKillingGraphMousePlugin();
  }

  public void setKillSwitch(Runnable killSwitch) {
    this.lensKillingGraphMousePlugin.setKillSwitch(killSwitch);
  }

  public void loadPlugins() {
    super.loadPlugins();
    add(lensKillingGraphMousePlugin);
    add(new LensTranslatingGraphMousePlugin(InputEvent.BUTTON1_DOWN_MASK));
    add(lensSelectingGraphMousePlugin);
    add(magnificationPlugin);
    add(scalingPlugin);
  }
}
