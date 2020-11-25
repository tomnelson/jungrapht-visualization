package org.jungrapht.visualization.control;

/**
 * A Satellite version of the {@link DefaultGraphMouse}. Non-modal, Use CTRL-mouse to translate the
 * graph visualization
 *
 * @author Tom Nelson
 */
public class DefaultSatelliteGraphMouse<V, E> extends DefaultGraphMouse<V, E> {

  public static class Builder<
          V, E, T extends DefaultSatelliteGraphMouse, B extends Builder<V, E, T, B>>
      extends DefaultGraphMouse.Builder<V, E, T, B> {

    public T build() {
      return (T) new DefaultSatelliteGraphMouse(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  public DefaultSatelliteGraphMouse() {
    super(DefaultSatelliteGraphMouse.builder());
  }

  /** create an instance with default values */
  public DefaultSatelliteGraphMouse(Builder<V, E, ?, ?> builder) {
    super(builder);
  }
  //
  //  /**
  //   * create an instance with passed values
  //   *
  //   * @param in override value for scale in
  //   * @param out override value for scale out
  //   */
  //  public DefaultSatelliteGraphMouse(float in, float out, boolean vertexSelectionOnly) {
  //    super(in, out, vertexSelectionOnly);
  //  }

  /** create the plugins, and load them */
  public void loadPlugins() {
    scalingPlugin =
        new SatelliteScalingGraphMousePlugin(
            new CrossoverScalingControl(),
            scalingMask,
            xAxisScalingMask,
            yAxisScalingMask,
            in,
            out);
    selectingPlugin = new SelectingGraphMousePlugin<>(singleSelectionMask, addSingleSelectionMask);
    regionSelectingPlugin =
        RegionSelectingGraphMousePlugin.builder()
            .regionSelectionMask(regionSelectionMask)
            .addRegionSelectionMask(addRegionSelectionMask)
            .regionSelectionCompleteMask(regionSelectionCompleteMask)
            .addRegionSelectionCompleteMask(addRegionSelectionCompleteMask)
            .build();
    add(selectingPlugin);
    add(regionSelectingPlugin);
    add(new SatelliteTranslatingGraphMousePlugin(translatingMask));
    add(scalingPlugin);
  }

  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
  }
}
