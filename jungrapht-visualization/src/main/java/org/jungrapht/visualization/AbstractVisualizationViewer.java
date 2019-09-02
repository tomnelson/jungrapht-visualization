package org.jungrapht.visualization;

/**
 * Provided as a base for extending the DefaultVisualizationViewer
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public abstract class AbstractVisualizationViewer<V, E> extends DefaultVisualizationViewer<V, E> {

  /**
   * Constructor for extending classes
   *
   * @param builder to set properties
   */
  protected AbstractVisualizationViewer(VisualizationViewer.Builder<V, E, ?, ?> builder) {
    super(builder);
  }
}
