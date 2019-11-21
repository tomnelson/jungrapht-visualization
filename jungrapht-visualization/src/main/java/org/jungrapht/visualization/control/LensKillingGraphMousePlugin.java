package org.jungrapht.visualization.control;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensTransformer;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A subclass of SelectingGraphMousePlugin that will deactivate the lens
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LensKillingGraphMousePlugin<V, E> extends SelectingGraphMousePlugin<V, E> {

  private static final Logger log = LoggerFactory.getLogger(LensKillingGraphMousePlugin.class);

  Runnable killSwitch;

  protected TransformSupport transformSupport = new LensTransformSupport();

  public void setKillSwitch(Runnable killSwitch) {
    this.killSwitch = killSwitch;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    //    super.mousePressed(e);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    //    super.mouseDragged(e);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    //    super.mouseClicked(e);
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    super.mouseEntered(e);
  }

  @Override
  public void mouseExited(MouseEvent e) {
    //    super.mouseExited(e);
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    //    super.mouseMoved(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.VIEW);
    MutableTransformer layoutTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.LAYOUT);
    Point2D p = e.getPoint();
    if (viewTransformer instanceof LensTransformer) {
      //        viewTransformer = ((LensTransformer) viewTransformer).getDelegate();
      p = ((LensTransformer) viewTransformer).getDelegate().inverseTransform(p);
    } else {
      p = viewTransformer.inverseTransform(p);
    }
    boolean accepted = true;
    if (accepted) {
      vv.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      if (layoutTransformer instanceof LensTransformer) {
        Lens lens = ((LensTransformer) layoutTransformer).getLens();
        testLensExit(lens, p);
      }
      if (viewTransformer instanceof LensTransformer) {
        Lens lens = ((LensTransformer) viewTransformer).getLens();
        testLensExit(lens, p);
      }
      vv.repaint();
    }
  }

  private void testLensExit(Lens lens, Point2D point) {
    RectangularShape lensShape = lens.getLensShape();
    Ellipse2D killTarget =
        new Ellipse2D.Double(
            lensShape.getMinX() + lensShape.getWidth(),
            lensShape.getMinY(),
            lensShape.getWidth() / 20,
            lensShape.getHeight() / 20);
    if (killTarget.contains(point) && killSwitch != null) {
      killSwitch.run();
    }
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
    multiSelectionStrategy.updateShape(down, down);
    layoutTargetShape = transformSupport.inverseTransform(vv, viewRectangle);
  }
}
