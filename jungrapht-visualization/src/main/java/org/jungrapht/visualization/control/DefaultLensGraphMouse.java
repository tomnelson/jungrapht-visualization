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
public class DefaultLensGraphMouse<V, E> extends DefaultGraphMouse<V, E> implements LensGraphMouse {

  /**
   * Build an instance of a RefactoredDefaultLEnsGraphMouse
   *
   * @param <T>
   * @param <B>
   */
  public static class Builder<V, E, T extends DefaultLensGraphMouse, B extends Builder<V, E, T, B>>
      extends DefaultGraphMouse.Builder<V, E, T, B> {

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
  protected LensRegionSelectingGraphMousePlugin<V, E> lensRegionSelectingGraphMousePlugin;
  protected LensKillingGraphMousePlugin lensKillingGraphMousePlugin;

  public DefaultLensGraphMouse() {
    this(new Builder<>(new LensMagnificationGraphMousePlugin()));
  }

  DefaultLensGraphMouse(Builder<V, E, ?, ?> builder) {
    this(
        builder.in,
        builder.out,
        builder.vertexSelectionOnly,
        builder.singleSelectionMask,
        builder.addSingleSelectionMask,
        builder.regionSelectionMask,
        builder.addRegionSelectionMask,
        builder.regionSelectionCompleteMask,
        builder.addRegionSelectionCompleteMask,
        builder.translatingMask,
        builder.scalingMask,
        builder.xAxisScalingMask,
        builder.yAxisScalingMask,
        builder.magnificationPlugin);
  }

  DefaultLensGraphMouse(
      float in,
      float out,
      boolean vertexSelectionOnly,
      int singleSelectionMask,
      int addSingleSelectionMask,
      int regionSelectionMask,
      int addRegionSelectionMask,
      int regionSelectionCompleteMask,
      int addRegionSelectionCompleteMask,
      int translatingMask,
      int scalingMask,
      int xAxisScalingMask,
      int yAxisScalingMask,
      LensMagnificationGraphMousePlugin magnificationPlugin) {
    super(
        in,
        out,
        vertexSelectionOnly,
        singleSelectionMask,
        addSingleSelectionMask,
        regionSelectionMask,
        addRegionSelectionMask,
        regionSelectionCompleteMask,
        addRegionSelectionCompleteMask,
        translatingMask,
        scalingMask,
        xAxisScalingMask,
        yAxisScalingMask);
    this.magnificationPlugin = magnificationPlugin;
    //    this.scalingPlugin = new PrevScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
    this.lensSelectingGraphMousePlugin =
        new LensSelectingGraphMousePlugin<>(singleSelectionMask, addSingleSelectionMask);
    this.lensRegionSelectingGraphMousePlugin =
        new LensRegionSelectingGraphMousePlugin<>(
            regionSelectionMask,
            addRegionSelectionMask,
            regionSelectionCompleteMask,
            addRegionSelectionCompleteMask);
    this.lensKillingGraphMousePlugin = new LensKillingGraphMousePlugin();
  }

  public void setKillSwitch(Runnable killSwitch) {
    this.lensKillingGraphMousePlugin.setKillSwitch(killSwitch);
  }

  public void loadPlugins() {
    add(selectingPlugin);
    add(regionSelectingPlugin);
    add(new TranslatingGraphMousePlugin(translatingMask));
    add(lensKillingGraphMousePlugin);
    add(lensSelectingGraphMousePlugin);
    add(lensRegionSelectingGraphMousePlugin);
    add(new LensTranslatingGraphMousePlugin(InputEvent.BUTTON1_DOWN_MASK));
    add(magnificationPlugin);
    add(scalingPlugin);
    setPluginsLoaded();
  }
}
