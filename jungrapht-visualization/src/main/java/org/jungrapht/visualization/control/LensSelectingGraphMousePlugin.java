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
public class LensSelectingGraphMousePlugin<V, E> extends SelectingGraphMousePlugin<V, E> {

  private static final Logger log = LoggerFactory.getLogger(LensSelectingGraphMousePlugin.class);

  public static class Builder<
          V, E, T extends LensSelectingGraphMousePlugin, B extends Builder<V, E, T, B>>
      extends SelectingGraphMousePlugin.Builder<V, E, T, B> {

    public T build() {
      return (T) new LensSelectingGraphMousePlugin(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected TransformSupport transformSupport = new LensTransformSupport();

  public LensSelectingGraphMousePlugin() {
    this(LensSelectingGraphMousePlugin.builder());
  }

  public LensSelectingGraphMousePlugin(Builder<V, E, ?, ?> builder) {
    this(builder.singleSelectionMask, builder.addSingleSelectionMask, builder.showFootprint);
  }

  /** create an instance with overides */
  LensSelectingGraphMousePlugin(
      int singleSelectionMask, int addSingleSelectionMask, boolean showFootprint) {
    super(singleSelectionMask, addSingleSelectionMask, showFootprint);
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
