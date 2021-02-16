package org.jungrapht.visualization.control;

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static Logger log = LoggerFactory.getLogger(DefaultGraphMouse.class);
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

    // selection masks
    protected int singleSelectionMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "singleSelectionMask", "MB1_MENU"));
    protected int toggleSingleSelectionMask =
        Modifiers.masks.get(
            System.getProperty(PREFIX + "toggleSingleSelectionMask", "MB1_SHIFT_MENU"));
    protected int regionSelectionMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "regionSelectionMask", "MB1_MENU"));
    protected int toggleRegionSelectionMask =
        Modifiers.masks.get(
            System.getProperty(PREFIX + "addregionSelectionMask", "MB1_SHIFT_MENU"));
    protected int regionSelectionCompleteMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "regionSelectionCompleteMask", "MENU"));
    protected int toggleRegionSelectionCompleteMask =
        Modifiers.masks.get(
            System.getProperty(PREFIX + "toggleRegionSelectionCompleteMask", "SHIFT_MENU"));

    // translation mask
    protected int translatingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "translatingMask", "MB1"));

    // scaling masks
    protected int xAxisScalingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "xAxisScalingMask", "MENU"));
    protected int yAxisScalingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "yAxisScalingMask", "ALT"));
    protected int scalingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "scalingMask", "NONE"));

    public B singleSelectionMask(int singleSelectionMask) {
      this.singleSelectionMask = singleSelectionMask;
      return self();
    }

    public B toggleSingleSelectionMask(int toggleSingleSelectionMask) {
      this.toggleSingleSelectionMask = toggleSingleSelectionMask;
      return self();
    }

    public B regionSelectionMask(int regionSelectionMask) {
      this.regionSelectionMask = regionSelectionMask;
      return self();
    }

    public B toggleRegionSelectionMask(int toggleRegionSelectionMask) {
      this.toggleRegionSelectionMask = toggleRegionSelectionMask;
      return self();
    }

    public B regionSelectionCompleteMask(int regionSelectionCompleteMask) {
      this.regionSelectionCompleteMask = regionSelectionCompleteMask;
      return self();
    }

    public B toggleRegionSelectionCompleteMask(int toggleRegionSelectionCompleteMask) {
      this.toggleRegionSelectionCompleteMask = toggleRegionSelectionCompleteMask;
      return self();
    }

    public B translatingMask(int translatingMask) {
      this.translatingMask = translatingMask;
      return self();
    }

    public B scalingMask(int scalingMask) {
      this.scalingMask = scalingMask;
      return self();
    }

    public B xAxisScalingMask(int xAxisScalingMask) {
      this.xAxisScalingMask = xAxisScalingMask;
      return self();
    }

    public B yAxisScalingMask(int yAxisScalingMask) {
      this.yAxisScalingMask = yAxisScalingMask;
      return self();
    }

    public T build() {
      return (T) new DefaultGraphMouse<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected int singleSelectionMask;
  protected int toggleSingleSelectionMask;
  protected int regionSelectionMask;
  protected int toggleRegionSelectionMask;
  protected int regionSelectionCompleteMask;
  protected int toggleRegionSelectionCompleteMask;
  protected int translatingMask;
  protected int scalingMask;
  protected int xAxisScalingMask;
  protected int yAxisScalingMask;

  /** create an instance with default values */
  protected DefaultGraphMouse(Builder<V, E, ?, ?> builder) {
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
        builder.scalingMask,
        builder.xAxisScalingMask,
        builder.yAxisScalingMask);
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
  DefaultGraphMouse(
      float in,
      float out,
      boolean vertexSelectionOnly,
      int singleSelectionMask,
      int toggleSingleSelectionMask,
      int regionSelectionMask,
      int toggleRegionSelectionMask,
      int regionSelectionCompleteMask,
      int toggleRegionSelectionCompleteMask,
      int translatingMask,
      int scalingMask,
      int xAxisScalingMask,
      int yAxisScalingMask) {
    super(in, out, vertexSelectionOnly);
    this.singleSelectionMask = singleSelectionMask;
    this.toggleSingleSelectionMask = toggleSingleSelectionMask;
    this.regionSelectionMask = regionSelectionMask;
    this.toggleRegionSelectionMask = toggleRegionSelectionMask;
    this.regionSelectionCompleteMask = regionSelectionCompleteMask;
    this.toggleRegionSelectionCompleteMask = toggleRegionSelectionCompleteMask;
    this.translatingMask = translatingMask;
    this.scalingMask = scalingMask;
    this.xAxisScalingMask = xAxisScalingMask;
    this.yAxisScalingMask = yAxisScalingMask;
  }

  /** create the plugins, and load them */
  @Override
  public void loadPlugins() {
    scalingPlugin =
        new ScalingGraphMousePlugin(
            new CrossoverScalingControl(),
            scalingMask,
            xAxisScalingMask,
            yAxisScalingMask,
            in,
            out);
    selectingPlugin =
        vertexSelectionOnly
            ? new VertexSelectingGraphMousePlugin<>(
                singleSelectionMask, toggleSingleSelectionMask, false)
            : SelectingGraphMousePlugin.builder()
                .singleSelectionMask(singleSelectionMask)
                .toggleSingleSelectionMask(toggleSingleSelectionMask)
                .build();
    regionSelectingPlugin =
        RegionSelectingGraphMousePlugin.builder()
            .regionSelectionMask(regionSelectionMask)
            .toggleRegionSelectionMask(toggleRegionSelectionMask)
            .regionSelectionCompleteMask(regionSelectionCompleteMask)
            .toggleRegionSelectionCompleteMask(toggleRegionSelectionCompleteMask)
            .build();
    translatingPlugin =
        TranslatingGraphMousePlugin.builder().translatingMask(translatingMask).build();

    add(selectingPlugin);
    add(regionSelectingPlugin);
    add(translatingPlugin);
    add(scalingPlugin);
  }

  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
  }
}
