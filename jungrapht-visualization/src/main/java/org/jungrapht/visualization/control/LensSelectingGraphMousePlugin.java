package org.jungrapht.visualization.control;

import java.awt.Shape;
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
        InputEvent.CTRL_DOWN_MASK,
        InputEvent.SHIFT_DOWN_MASK // drag select in non-rectangular shape
        //        InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK
        );
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

  //  public void mousePressed(MouseEvent e) {
  //    down = e.getPoint();
  //    log.trace("mouse pick at screen coords {}", e.getPoint());
  //    deltaDown = down;
  //    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
  //    TransformSupport<V, E> transformSupport = vv.getTransformSupport();
  //    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
  //    GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
  //    MutableSelectedState<V> selectedVertexState = vv.getSelectedVertexState();
  //    MutableSelectedState<E> selectedEdgeState = vv.getSelectedEdgeState();
  //    viewRectangle = multiSelectionStrategy.getInitialShape(e.getPoint());
  //
  //    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
  //
  ////    Point2D pp = e.getPoint();
  ////    pp = inverseTransform(vv, pp);
  //    // a rectangle in the view coordinate system.
  //    this.footprintRectangle =
  //            new Rectangle2D.Float(
  //                    (float) e.getPoint().x - pickSize / 2,
  //                    (float) e.getPoint().y - pickSize / 2,
  //                    pickSize,
  //                    pickSize);
  ////    this.footprintRectangle = transformSupport.transform(vv, footprintRectangle);
  //
  //    vv.addPostRenderPaintable(pickFootprintPaintable);
  //    vv.repaint();
  //    // subclass can override to account for view distortion effects
  //    updatePickingTargets(vv, multiLayerTransformer, down, down);
  //
  //    // subclass can override to account for view distortion effects
  //    // layoutPoint is the mouse event point projected on the layout coordinate system
  //    Point2D layoutPoint = transformSupport.inverseTransform(vv, down);
  //    log.trace("layout coords of mouse click {}", layoutPoint);
  //    if (e.getModifiersEx() == (modifiers | selectionModifiers)) { // default button1 down and ctrl
  //
  //      if (pickSupport instanceof ShapePickSupport) {
  //        ShapePickSupport<V, E> shapePickSupport = (ShapePickSupport<V, E>) pickSupport;
  //        this.vertex = shapePickSupport.getVertex(layoutModel, footprintRectangle);
  //      } else {
  //        this.vertex = pickSupport.getVertex(layoutModel, layoutPoint.getX(), layoutPoint.getY());
  //      }
  //
  //      if (vertex != null) {
  //
  //        log.trace("mousePressed set the vertex to {}", vertex);
  //        if (!selectedVertexState.isSelected(vertex)) {
  //          selectedVertexState.clear();
  //          selectedVertexState.select(vertex);
  //        }
  //        e.consume();
  //        return;
  //      }
  //
  //      this.edge = pickSupport.getEdge(layoutModel, layoutPoint.getX(), layoutPoint.getY());
  //
  //      if (edge != null) {
  //
  //        log.trace("mousePressed set the edge to {}", edge);
  //        if (!selectedEdgeState.isSelected(edge)) {
  //          selectedEdgeState.clear();
  //          selectedEdgeState.select(edge);
  //        }
  //        e.consume();
  //        return;
  //      }
  //
  //      // got no vertex and no edge, clear all selections
  //      selectedEdgeState.clear();
  //      selectedVertexState.clear();
  //      vv.addPostRenderPaintable(lensPaintable);
  //      return;
  //
  //    } else if (e.getModifiersEx() == (modifiers | selectionModifiers | addToSelectionModifiers)) {
  //
  //      vv.addPostRenderPaintable(lensPaintable);
  //
  //      this.vertex = pickSupport.getVertex(layoutModel, layoutPoint.getX(), layoutPoint.getY());
  //
  //      if (vertex != null) {
  //        log.trace("mousePressed set the vertex to {}", vertex);
  //        if (selectedVertexState.isSelected(vertex)) {
  //          selectedVertexState.deselect(vertex);
  //        } else {
  //          selectedVertexState.select(vertex);
  //        }
  //        e.consume();
  //        return;
  //      }
  //
  //      this.edge = pickSupport.getEdge(layoutModel, layoutPoint.getX(), layoutPoint.getY());
  //
  //      if (edge != null) {
  //        log.trace("mousePressed set the edge to {}", edge);
  //        if (selectedEdgeState.isSelected(edge)) {
  //          selectedEdgeState.deselect(edge);
  //        } else {
  //          selectedEdgeState.select(edge);
  //        }
  //        e.consume();
  //        return;
  //      }
  //    } else {
  //      down = null;
  //    }
  //    if (vertex != null) {
  //      e.consume();
  //    }
  //  }

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

  /**
   * Overridden to perform Lens effects when managing the picking Lens target shape (drawn with the
   * mouse) in both the layout and view coordinate systems
   *
   * @param vv
   * @param multiLayerTransformer
   * @param down
   * @param out
   */
  //  @Override
  //  protected void updatePickingTargets(
  //      VisualizationViewer vv,
  //      MultiLayerTransformer multiLayerTransformer,
  //      Point2D down,
  //      Point2D out) {
  //
  //    multiSelectionStrategy.updateShape(down, down);
  //    layoutTargetShape = transformSupport.inverseTransform(vv, viewRectangle);
  //  }
}
