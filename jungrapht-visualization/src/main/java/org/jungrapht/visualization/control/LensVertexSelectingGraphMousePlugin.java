package org.jungrapht.visualization.control;

import java.awt.Shape;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.VisualizationViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A subclass of SelectingGraphMousePlugin that contains methods that are overridden to account for
 * the Lens effects that are in the view projection
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LensVertexSelectingGraphMousePlugin<V, E>
    extends VertexSelectingGraphMousePlugin<V, E> {

  private static final Logger log =
      LoggerFactory.getLogger(LensVertexSelectingGraphMousePlugin.class);

  public static class Builder<
          V, E, T extends LensVertexSelectingGraphMousePlugin, B extends Builder<V, E, T, B>>
      extends VertexSelectingGraphMousePlugin.Builder<V, E, T, B> {

    public T build() {
      return (T) new LensVertexSelectingGraphMousePlugin(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected TransformSupport transformSupport = new LensTransformSupport();

  public LensVertexSelectingGraphMousePlugin() {
    this(LensVertexSelectingGraphMousePlugin.builder());
  }

  public LensVertexSelectingGraphMousePlugin(Builder<V, E, ?, ?> builder) {
    this(builder.singleSelectionMask, builder.addSingleSelectionMask);
  }

  /** create an instance with overides */
  public LensVertexSelectingGraphMousePlugin(int singleSelectionMask, int addSingleSelectionMask) {
    super(singleSelectionMask, addSingleSelectionMask);
  }

  /**
   * Overridden to apply lens effects to the transformation from view to layout coordinates
   *
   * @param vv
   * @param p
   * @return
   */
  @Override
  protected Point2D inverseTransform(VisualizationViewer<V, E> vv, Point2D p) {
    return transformSupport.inverseTransform(vv, p);
  }

  /**
   * Overridden to perform lens effects when transforming from Layout to view. Used when projecting
   * the selection Lens (the rectangular area drawn with the mouse) back into the view.
   *
   * @param vv
   * @param shape
   * @return
   */
  @Override
  protected Shape transform(VisualizationViewer<V, E> vv, Shape shape) {
    return transformSupport.transform(vv, shape);
  }
}
