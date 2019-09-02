package org.jungrapht.visualization;

/**
 * Provided as a public base class for extending the DefaultVisualizationModel
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public abstract class AbstractVisualizationModel<V, E> extends DefaultVisualizationModel<V, E> {

  /**
   * Constructor for extending classes
   *
   * @param builder to set properties
   */
  protected AbstractVisualizationModel(VisualizationModel.Builder<V, E, ?, ?> builder) {
    super(builder);
  }
}
