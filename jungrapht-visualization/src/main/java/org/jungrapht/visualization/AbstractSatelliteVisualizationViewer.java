package org.jungrapht.visualization;

/**
 * Provided as a public base class for extending the DefaultVisualizationViewer
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public abstract class AbstractSatelliteVisualizationViewer<V, E>
    extends DefaultSatelliteVisualizationViewer<V, E> {

  /**
   * Constructor for extending classes
   *
   * @param builder to set properties
   */
  protected AbstractSatelliteVisualizationViewer(
      SatelliteVisualizationViewer.Builder<V, E, ?, ?> builder) {
    super(builder);
  }
}
