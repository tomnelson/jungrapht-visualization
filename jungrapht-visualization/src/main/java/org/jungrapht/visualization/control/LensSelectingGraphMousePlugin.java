package org.jungrapht.visualization.control;

import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
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

  protected TransformSupport transformSupport = new LensTransformSupport();

  /** create an instance with default settings */
  public LensSelectingGraphMousePlugin() {
    super(
        InputEvent.BUTTON1_DOWN_MASK,
        // CTRL down on linux, command down on mac
        Toolkit.getDefaultToolkit()
            .getMenuShortcutKeyMaskEx(), // select or drag select in rectangle
        InputEvent.SHIFT_DOWN_MASK);
  }

  /**
   * create an instance with overides
   *
   * @param selectionModifiers for primary selection
   * @param addToSelectionModifiers for additional selection
   */
  public LensSelectingGraphMousePlugin(
      int modifiers, int selectionModifiers, int addToSelectionModifiers) {
    super(modifiers, selectionModifiers, addToSelectionModifiers);
  }

  public void mousePressed(MouseEvent e) {
    super.mousePressed(e);
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
