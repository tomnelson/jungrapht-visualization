package org.jungrapht.visualization.control;

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * an implementation of the PluggableGraphMouse that includes plugins for manipulating a view that
 * is using a LensTransformer.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class DefaultLensGraphMouse<V, E> extends DefaultGraphMouse<V, E> implements LensGraphMouse {

  private static final Logger log = LoggerFactory.getLogger(DefaultLensGraphMouse.class);
  /**
   * Build an instance of a RefactoredDefaultLEnsGraphMouse
   *
   * @param <T>
   * @param <B>
   */
  public static class Builder<V, E, T extends DefaultLensGraphMouse, B extends Builder<V, E, T, B>>
      extends DefaultGraphMouse.Builder<V, E, T, B> {

    // translation mask
    protected int lensTranslatingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "lensTranslatingMask", "MB1"));

    protected double magnificationDelta = 0.05f;
    // values for hyperbolic transform lens
    protected double magnificationFloor = 0.45f;
    protected double magnificationCeiling = 1.0f;
    // values for magnify transform lens
    //    protected double magnificationFloor = 1.0f;
    //    protected double magnificationCeiling = 4.0f;

    public B lensTranslatingMask(int lensTranslatingMask) {
      this.lensTranslatingMask = lensTranslatingMask;
      return self();
    }

    public B magnificationFloor(double magnificationFloor) {
      this.magnificationFloor = magnificationFloor;
      return self();
    }

    public B magnificationCeiling(double magnificationCeiling) {
      this.magnificationCeiling = magnificationCeiling;
      return self();
    }

    public B magnificationDelta(double magnificationDelta) {
      this.magnificationDelta = magnificationDelta;
      return self();
    }

    public T build() {
      return (T) new DefaultLensGraphMouse(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  /** not included in the base class */
  protected LensMagnificationGraphMousePlugin magnificationPlugin;

  protected LensKillingGraphMousePlugin lensKillingGraphMousePlugin;

  protected LensTranslatingGraphMousePlugin lensTranslatingGraphMousePlugin;

  public DefaultLensGraphMouse() {
    this(new Builder<>());
  }

  DefaultLensGraphMouse(Builder<V, E, ?, ?> builder) {
    this(
        builder.in,
        builder.out,
        builder.vertexSelectionOnly,
        builder.singleSelectionMask,
        builder.toggleSingleSelectionMask,
        builder.regionSelectionMask,
        builder.toggleRegionSelectionMask,
        builder.regionSelectionCompleteMask,
        builder.toggleRegionSelectionCompleteMask,
        builder.translatingMask,
        builder.lensTranslatingMask,
        builder.scalingMask,
        builder.xAxisScalingMask,
        builder.yAxisScalingMask,
        builder.magnificationFloor,
        builder.magnificationCeiling,
        builder.magnificationDelta);
  }

  DefaultLensGraphMouse(
      double in,
      double out,
      boolean vertexSelectionOnly,
      int singleSelectionMask,
      int toggleSingleSelectionMask,
      int regionSelectionMask,
      int toggleRegionSelectionMask,
      int regionSelectionCompleteMask,
      int toggleRegionSelectionCompleteMask,
      int translatingMask,
      int lensTranslatingMask,
      int scalingMask,
      int xAxisScalingMask,
      int yAxisScalingMask,
      double magnificationFloor,
      double magnificationCeiling,
      double magnificationDelta) {
    super(
        in,
        out,
        vertexSelectionOnly,
        singleSelectionMask,
        toggleSingleSelectionMask,
        regionSelectionMask,
        toggleRegionSelectionMask,
        regionSelectionCompleteMask,
        toggleRegionSelectionCompleteMask,
        translatingMask,
        scalingMask,
        xAxisScalingMask,
        yAxisScalingMask);
    this.magnificationPlugin =
        new LensMagnificationGraphMousePlugin(
            magnificationFloor, magnificationCeiling, magnificationDelta);
    this.lensTranslatingGraphMousePlugin = new LensTranslatingGraphMousePlugin(lensTranslatingMask);
    this.lensKillingGraphMousePlugin = new LensKillingGraphMousePlugin();
    this.lensTranslatingGraphMousePlugin = new LensTranslatingGraphMousePlugin();
  }

  public void setKillSwitch(Runnable killSwitch) {
    this.lensKillingGraphMousePlugin.setKillSwitch(killSwitch);
  }

  public void loadPlugins() {

    scalingPlugin =
        new ScalingGraphMousePlugin(
            new CrossoverScalingControl(),
            scalingMask,
            xAxisScalingMask,
            yAxisScalingMask,
            in,
            out);
    this.selectingPlugin =
        vertexSelectionOnly
            ? LensVertexSelectingGraphMousePlugin.builder()
                .singleSelectionMask(singleSelectionMask)
                .toggleSingleSelectionMask(toggleSingleSelectionMask)
                .build()
            : LensSelectingGraphMousePlugin.builder()
                .singleSelectionMask(singleSelectionMask)
                .toggleSingleSelectionMask(toggleSingleSelectionMask)
                .build();
    this.regionSelectingPlugin =
        new LensRegionSelectingGraphMousePlugin<>(
            regionSelectionMask,
            toggleRegionSelectionMask,
            regionSelectionCompleteMask,
            toggleRegionSelectionCompleteMask);
    this.translatingPlugin =
        TranslatingGraphMousePlugin.builder().translatingMask(translatingMask).build();

    add(lensKillingGraphMousePlugin);
    add(lensTranslatingGraphMousePlugin);
    add(selectingPlugin);
    add(translatingPlugin);
    add(regionSelectingPlugin);
    add(magnificationPlugin);
    add(scalingPlugin);
  }
}
