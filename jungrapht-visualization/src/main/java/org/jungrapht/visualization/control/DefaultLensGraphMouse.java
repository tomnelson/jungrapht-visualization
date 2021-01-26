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

    protected LensMagnificationGraphMousePlugin magnificationPlugin =
        new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f);

    public B magnificationPlugin(LensMagnificationGraphMousePlugin magnificationPlugin) {
      this.magnificationPlugin = magnificationPlugin;
      return self();
    }

    public B lensTranslatingMask(int lensTranslatingMask) {
      this.lensTranslatingMask = lensTranslatingMask;
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
    this.magnificationPlugin = new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f);
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
        builder.lensTranslatingMask,
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
      int lensTranslatingMask,
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
                .addSingleSelectionMask(addSingleSelectionMask)
                .build()
            : LensSelectingGraphMousePlugin.builder()
                .singleSelectionMask(singleSelectionMask)
                .addSingleSelectionMask(addSingleSelectionMask)
                .build();
    this.regionSelectingPlugin =
        new LensRegionSelectingGraphMousePlugin<>(
            regionSelectionMask,
            addRegionSelectionMask,
            regionSelectionCompleteMask,
            addRegionSelectionCompleteMask);
    this.translatingPlugin = new TranslatingGraphMousePlugin(translatingMask);

    add(lensKillingGraphMousePlugin);
    add(lensTranslatingGraphMousePlugin);
    add(selectingPlugin);
    add(translatingPlugin);
    add(regionSelectingPlugin);
    add(magnificationPlugin);
    add(scalingPlugin);
  }
}
