package org.jungrapht.visualization;

public abstract class AbstractVisualizationViewer<V, E> extends DefaultVisualizationViewer<V, E> {

  protected AbstractVisualizationViewer(VisualizationViewer.Builder<V, E, ?, ?> builder) {
    super(builder);
  }
}
