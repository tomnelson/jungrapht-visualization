package org.jungrapht.visualization.control;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A subclass of SelectingGraphMousePlugin that contains methods that are overridden to account for
 * the Lens effects that are in the view projection
 *
 * @author Tom Nelson
 */
public class LensSelectingGraphMousePlugin<V, E> extends SelectingGraphMousePlugin<V, E> {

  private static final Logger log = LoggerFactory.getLogger(LensSelectingGraphMousePlugin.class);

  protected TransformSupport transformSupport = new LensTransformSupport();

  /** create an instance with default settings */
  public LensSelectingGraphMousePlugin() {
    super(
        InputEvent.BUTTON1_DOWN_MASK | InputEvent.CTRL_DOWN_MASK,
        InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK);
  }

  /**
   * create an instance with overides
   *
   * @param selectionModifiers for primary selection
   * @param addToSelectionModifiers for additional selection
   */
  public LensSelectingGraphMousePlugin(int selectionModifiers, int addToSelectionModifiers) {
    super(selectionModifiers, addToSelectionModifiers);
  }

  /**
   * Overriden to apply lens effects to the transformation from view to layout coordinates
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
   * Overriden to perform lens effects when transforming from Layout to view. Used when projecting
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

  /**
   * Overriden to perform Lens effects when managing the picking Lens target shape (drawn with the
   * mouse) in both the layout and view coordinate systems
   *
   * @param vv
   * @param multiLayerTransformer
   * @param down
   * @param out
   */
  @Override
  protected void updatePickingTargets(
      VisualizationViewer vv,
      MultiLayerTransformer multiLayerTransformer,
      Point2D down,
      Point2D out) {
    viewRectangle.setFrameFromDiagonal(down, out);

    layoutTargetShape = transformSupport.inverseTransform(vv, viewRectangle);
  }
}
