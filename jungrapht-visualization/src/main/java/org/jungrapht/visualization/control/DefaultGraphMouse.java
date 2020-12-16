package org.jungrapht.visualization.control;

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.awt.Toolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PrevDefaultGraphMouse does not have 'transforming/selecting' modes. It has 3 plugins that are
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
   * Build an instance of a PrevDefaultGraphMouse
   *
   * @param <V>
   * @param <E>
   * @param <T>
   * @param <B>
   */
  public static class Builder<V, E, T extends DefaultGraphMouse, B extends Builder<V, E, T, B>>
      extends AbstractGraphMouse.Builder<T, B> {

    private static int MENU = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    // selection masks
    protected int singleSelectionMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "singleSelectionMask", "MB1_MENU"));
    protected int addSingleSelectionMask =
        Modifiers.masks.get(
            System.getProperty(PREFIX + "addSingleSelectionMask", "MB1_SHIFT_MENU"));
    protected int regionSelectionMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "regionSelectionMask", "MB1_MENU"));
    protected int addRegionSelectionMask =
        Modifiers.masks.get(
            System.getProperty(PREFIX + "addregionSelectionMask", "MB1_SHIFT_MENU"));
    protected int regionSelectionCompleteMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "regionSelectionCompleteMask", "MENU"));
    protected int addRegionSelectionCompleteMask =
        Modifiers.masks.get(
            System.getProperty(PREFIX + "addRegionSelectionCompleteMask", "SHIFT_MENU"));

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

    public B addSingleSelectionMask(int addSingleSelectionMask) {
      this.addSingleSelectionMask = addSingleSelectionMask;
      return self();
    }

    public B regionSelectionMask(int regionSelectionMask) {
      this.regionSelectionMask = regionSelectionMask;
      return self();
    }

    public B addRegionSelectionMask(int addRegionSelectionMask) {
      this.addRegionSelectionMask = addRegionSelectionMask;
      return self();
    }

    public B regionSelectionCompleteMask(int regionSelectionCompleteMask) {
      this.regionSelectionCompleteMask = regionSelectionCompleteMask;
      return self();
    }

    public B addRegionSelectionCompleteMask(int addRegionSelectionCompleteMask) {
      this.addRegionSelectionCompleteMask = addRegionSelectionCompleteMask;
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
  protected int addSingleSelectionMask;
  protected int regionSelectionMask;
  protected int addRegionSelectionMask;
  protected int regionSelectionCompleteMask;
  protected int addRegionSelectionCompleteMask;
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
        builder.addSingleSelectionMask,
        builder.regionSelectionMask,
        builder.addRegionSelectionMask,
        builder.regionSelectionCompleteMask,
        builder.addRegionSelectionCompleteMask,
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
      int addSingleSelectionMask,
      int regionSelectionMask,
      int addRegionSelectionMask,
      int regionSelectionCompleteMask,
      int addRegionSelectionCompleteMask,
      int translatingMask,
      int scalingMask,
      int xAxisScalingMask,
      int yAxisScalingMask) {
    super(in, out, vertexSelectionOnly);
    this.singleSelectionMask = singleSelectionMask;
    this.addSingleSelectionMask = addSingleSelectionMask;
    this.regionSelectionMask = regionSelectionMask;
    this.addRegionSelectionMask = addRegionSelectionMask;
    this.regionSelectionCompleteMask = regionSelectionCompleteMask;
    this.addRegionSelectionCompleteMask = addRegionSelectionCompleteMask;
    this.translatingMask = translatingMask;
    this.scalingMask = scalingMask;
    this.xAxisScalingMask = xAxisScalingMask;
    this.yAxisScalingMask = yAxisScalingMask;
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
            ? new VertexSelectingGraphMousePlugin<>(singleSelectionMask, addSingleSelectionMask)
            : new SelectingGraphMousePlugin<>(singleSelectionMask, addSingleSelectionMask);
    regionSelectingPlugin =
        RegionSelectingGraphMousePlugin.builder()
            .regionSelectionMask(regionSelectionMask)
            .addRegionSelectionMask(addRegionSelectionMask)
            .regionSelectionCompleteMask(regionSelectionCompleteMask)
            .addRegionSelectionCompleteMask(addRegionSelectionCompleteMask)
            .build();
  }

  /** create the plugins, and load them */
  @Override
  public void loadPlugins() {
    add(selectingPlugin);
    log.info("added " + selectingPlugin);
    add(regionSelectingPlugin);
    log.info("added " + regionSelectingPlugin);
    add(new TranslatingGraphMousePlugin(translatingMask));
    add(scalingPlugin);
    setPluginsLoaded();
  }

  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
  }
}
