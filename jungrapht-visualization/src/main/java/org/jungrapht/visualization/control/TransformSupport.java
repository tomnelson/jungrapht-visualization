package org.jungrapht.visualization.control;

import java.awt.*;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.transform.MutableAffineTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class TransformSupport<V, E> extends MutableAffineTransformer {

  private static final Logger log = LoggerFactory.getLogger(TransformSupport.class);

  /**
   * Overriden to apply lens effects to the transformation from view to layout coordinates
   *
   * @param vv
   * @param p
   * @return
   */
  public Point2D inverseTransform(VisualizationServer<V, E> vv, Point2D p) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    return multiLayerTransformer.inverseTransform(p);
  }

  /**
   * Overriden to perform lens effects when transforming from Layout to view. Used when projecting
   * the selection Lens (the rectangular area drawn with the mouse) back into the view.
   *
   * @param vv
   * @param p
   * @return
   */
  public Point2D transform(VisualizationServer<V, E> vv, Point2D p) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    return multiLayerTransformer.transform(p);
  }

  /**
   * Overriden to perform lens effects when transforming from Layout to view. Used when projecting
   * the selection Lens (the rectangular area drawn with the mouse) back into the view.
   *
   * @param vv
   * @param p
   * @return
   */
  public Point2D transform(
      VisualizationServer<V, E> vv, Point2D p, MultiLayerTransformer.Layer layer) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    return multiLayerTransformer.transform(layer, p);
  }

  /**
   * Overridden to perform lens effects when transforming from Layout to view. Used when projecting
   * the selection Lens (the rectangular area drawn with the mouse) back into the view.
   *
   * @param vv
   * @param shape
   * @return
   */
  public Shape transform(VisualizationServer<V, E> vv, Shape shape) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    return multiLayerTransformer.transform(shape);
  }

  /**
   * Overridden to perform lens effects when transforming from Layout to view. Used when projecting
   * the selection Lens (the rectangular area drawn with the mouse) back into the view.
   *
   * @param vv
   * @param shape
   * @return
   */
  public Shape transform(
      VisualizationServer<V, E> vv, Shape shape, MultiLayerTransformer.Layer layer) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    return multiLayerTransformer.transform(layer, shape);
  }

  /**
   * Overriden to perform lens effects when inverse transforming from view to layout.
   *
   * @param vv
   * @param shape
   * @return
   */
  public Shape inverseTransform(VisualizationServer<V, E> vv, Shape shape) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    return multiLayerTransformer.inverseTransform(shape);
  }
}
