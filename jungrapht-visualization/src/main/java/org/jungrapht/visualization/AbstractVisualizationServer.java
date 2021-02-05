package org.jungrapht.visualization;

/**
 * Provided as a public base class for extending the DefaultVisualizationServer
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class AbstractVisualizationServer<V, E> extends DefaultVisualizationServer<V, E> {

  /**
   * Constructor for extending classes
   *
   * @param builder to set properties
   */
  protected AbstractVisualizationServer(Builder<V, E, ?, ?> builder) {
    super(builder);
  }
}
