package org.jungrapht.visualization.control;

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.swing.*;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.PropertyLoader;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RegionSelectingGraphMousePlugin supports the selecting of graph elements with the mouse.
 * MouseButtonOne selects a single vertex or edge, and MouseButtonTwo adds to the set of selected
 * Vertices or EdgeType. If a Vertex is selected and the mouse is dragged while on the selected
 * Vertex, then that Vertex will be repositioned to follow the mouse until the button is released.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class RegionSelectingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  private static final Logger log = LoggerFactory.getLogger(RegionSelectingGraphMousePlugin.class);

  static {
    PropertyLoader.load();
  }

  public static class Builder<
      V, E, T extends RegionSelectingGraphMousePlugin, B extends Builder<V, E, T, B>> {
    protected int regionSelectionMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "regionSelectionMask", "MB1_MENU"));
    protected int toggleRegionSelectionMask =
        Modifiers.masks.get(
            System.getProperty(PREFIX + "toggleRegionSelectionMask", "MB1_SHIFT_MENU"));
    protected int regionSelectionCompleteMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "regionSelectionCompleteMask", "MENU"));
    protected int toggleRegionSelectionCompleteMask =
        Modifiers.masks.get(
            System.getProperty(PREFIX + "toggleRegionSelectionCompleteMask", "SHIFT_MENU"));

    public B self() {
      return (B) this;
    }

    public B regionSelectionMask(int regionSelectionMask) {
      this.regionSelectionMask = regionSelectionMask;
      return self();
    }

    public B toggleRegionSelectionMask(int toggleRegionSelectionMask) {
      this.toggleRegionSelectionMask = toggleRegionSelectionMask;
      return self();
    }

    public B regionSelectionCompleteMask(int regionSelectionCompleteMask) {
      this.regionSelectionCompleteMask = regionSelectionCompleteMask;
      return self();
    }

    public B toggleRegionSelectionCompleteMask(int toggleRegionSelectionCompleteMask) {
      this.toggleRegionSelectionCompleteMask = toggleRegionSelectionCompleteMask;
      return self();
    }

    public T build() {
      return (T) new RegionSelectingGraphMousePlugin(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  /** controls whether the Vertices may be moved with the mouse */
  protected boolean locked;

  /** used to draw a rectangle to contain selected vertices */
  protected Shape viewRectangle = new Rectangle2D.Double();
  // viewRectangle projected onto the layout coordinate system
  protected Shape layoutTargetShape = viewRectangle;

  /** the Paintable for the lens picking rectangle */
  protected VisualizationServer.Paintable lensPaintable;

  /** color for the picking rectangle */
  protected Color lensColor = Color.cyan;

  protected Point2D deltaDown;

  protected MultiSelectionStrategy multiSelectionStrategy;

  protected int singleSelectionMask;
  protected int toggleSingleSelectionMask;
  protected int regionSelectionMask;
  protected int toggleRegionSelectionMask;
  protected int regionSelectionCompleteMask;
  protected int toggleRegionSelectionCompleteMask;

  public RegionSelectingGraphMousePlugin(Builder<V, E, ?, ?> builder) {
    this(
        builder.regionSelectionMask,
        builder.toggleRegionSelectionMask,
        builder.regionSelectionCompleteMask,
        builder.toggleRegionSelectionCompleteMask);
  }

  public RegionSelectingGraphMousePlugin() {
    this(RegionSelectingGraphMousePlugin.builder());
  }
  /**
   * create an instance with overrides
   *
   * <p>// * @param selectionModifiers for primary selection // * @param addToSelectionModifiers for
   * additional selection
   */
  protected RegionSelectingGraphMousePlugin(
      int regionSelectionMask,
      int toggleRegionSelectionMask,
      int regionSelectionCompleteMask,
      int toggleRegionSelectionCompleteMask) {
    this.regionSelectionMask = regionSelectionMask;
    this.toggleRegionSelectionMask = toggleRegionSelectionMask;
    this.regionSelectionCompleteMask = regionSelectionCompleteMask;
    this.toggleRegionSelectionCompleteMask = toggleRegionSelectionCompleteMask;
    this.lensPaintable = new LensPaintable();
    this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  }

  /** @return Returns the lensColor. */
  public Color getLensColor() {
    return lensColor;
  }

  /** @param lensColor The lensColor to set. */
  public void setLensColor(Color lensColor) {
    this.lensColor = lensColor;
  }

  /**
   * a Paintable to draw the rectangle used to pick multiple Vertices
   *
   * @author Tom Nelson
   */
  class LensPaintable implements VisualizationServer.Paintable {

    public void paint(Graphics g) {
      Color oldColor = g.getColor();
      g.setColor(lensColor);
      ((Graphics2D) g).draw(viewRectangle);
      g.setColor(oldColor);
    }

    public boolean useTransform() {
      return false;
    }
  }

  /**
   * For primary modifiers (default, MouseButton1): pick a single Vertex or Edge that is under the
   * mouse pointer. If no Vertex or edge is under the pointer, unselect all selected Vertices and
   * edges, and set up to draw a rectangle for multiple selection of contained Vertices. For
   * additional selection (default Shift+MouseButton1): Add to the selection, a single Vertex or
   * Edge that is under the mouse pointer. If a previously selected Vertex or Edge is under the
   * pointer, it is un-selected. If no vertex or Edge is under the pointer, set up to draw a
   * multiple selection rectangle (as above) but do not unpick previously selected elements.
   *
   * @param e the event
   */
  public void mousePressed(MouseEvent e) {
    log.trace("pressed");
    if (e.getModifiersEx() == regionSelectionMask
        || e.getModifiersEx() == toggleRegionSelectionMask) {
      down = e.getPoint();
      log.trace("mouse pick at screen coords {}", e.getPoint());
      deltaDown = down;
      VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
      multiSelectionStrategy = vv.getMultiSelectionStrategySupplier().get();
      //      TransformSupport<V, E> transformSupport = vv.getTransformSupport();
      viewRectangle = multiSelectionStrategy.getInitialShape(e.getPoint());

      MultiLayerTransformer multiLayerTransformer =
          vv.getRenderContext().getMultiLayerTransformer();

      // subclass can override to account for view distortion effects
      updatePickingTargets(vv, multiLayerTransformer, down, down);

      vv.addPostRenderPaintable(lensPaintable);
    }
  }

  /**
   * If the mouse is dragging a rectangle, pick the Vertices contained in that rectangle
   *
   * <p>clean up settings from mousePressed
   */
  public void mouseReleased(MouseEvent e) {
    log.trace("released");
    // if the mouse is released in lasso mode with no selection, then clear the vertex
    // and edge selections ?
    Point2D out = e.getPoint();

    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();

    // mouse is not down, check only for the addToSelectionModifiers (defaults to SHIFT_DOWN_MASK)
    if (e.getModifiersEx() == toggleRegionSelectionCompleteMask) {
      // toggle the selected state of everything in the selection shape
      // don't deselect anything else
      if (down != null) {
        if (multiSelectionMayProceed(layoutTargetShape.getBounds2D())) {
          toggleSelectionForContainedVertices(vv, layoutTargetShape);
        }
      }
    } else if (e.getModifiersEx() == regionSelectionCompleteMask) {
      if (down != null) {
        if (multiSelectionMayProceed(layoutTargetShape.getBounds2D())) {
          Collection<V> picked = pickContainedVertices(vv, layoutTargetShape);
          if (picked.isEmpty()) {
            vv.getSelectedVertexState().clear();
            vv.getSelectedEdgeState().clear();
          }
        }
      }
    }
    log.trace("down:{} out:{}", down, out);
    down = null;
    layoutTargetShape = multiLayerTransformer.inverseTransform(viewRectangle);
    vv.removePostRenderPaintable(lensPaintable);
    vv.repaint();
  }

  /**
   * If the mouse is over a selected vertex, drag all selected vertices with the mouse. If the mouse
   * is not over a Vertex, draw the rectangle to select multiple Vertices
   */
  public void mouseDragged(MouseEvent e) {
    log.trace("dragged " + viewRectangle);
    if (e.getModifiersEx() == regionSelectionMask
        || e.getModifiersEx() == toggleRegionSelectionMask) {
      log.trace("mouseDragged");
      VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
      if (!locked) {

        MultiLayerTransformer multiLayerTransformer =
            vv.getRenderContext().getMultiLayerTransformer();
        Point2D p = e.getPoint();
        log.trace("view p for drag event is {}", p);
        log.trace("down is {}", down);
        if (down != null) {
          Point2D out = e.getPoint();
          multiSelectionStrategy.updateShape(down, out);
          layoutTargetShape = multiLayerTransformer.inverseTransform(viewRectangle);
        }
        vv.repaint();
      }
    }
  }

  /**
   * for rectangular shape selection, ensure that the rectangle is not too small and proceed for
   * arbitrary shape selection, proceed
   *
   * @param targetShape test for empty shape
   * @return whether the multiselection may proceed
   */
  private boolean multiSelectionMayProceed(Rectangle2D targetShape) {
    return !targetShape.isEmpty();
  }

  /**
   * override to consider Lens effects
   *
   * @param vv
   * @param p
   * @return
   */
  protected Point2D inverseTransform(VisualizationViewer<V, E> vv, Point2D p) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    return multiLayerTransformer.inverseTransform(p);
  }

  /**
   * override to consider Lens effects
   *
   * @param vv
   * @param shape
   * @return
   */
  protected Shape transform(VisualizationViewer<V, E> vv, Shape shape) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    return multiLayerTransformer.transform(shape);
  }

  /**
   * override to consider Lens effects
   *
   * @param vv
   * @param multiLayerTransformer
   * @param down
   * @param out
   */
  protected void updatePickingTargets(
      VisualizationViewer vv,
      MultiLayerTransformer multiLayerTransformer,
      Point2D down,
      Point2D out) {

    multiSelectionStrategy.updateShape(down, down);
    layoutTargetShape = multiLayerTransformer.inverseTransform(viewRectangle);
  }

  /**
   * pick the vertices inside the rectangle created from points 'down' and 'out' (two diagonally
   * opposed corners of the rectangle)
   *
   * @param vv the viewer containing the layout and selected state
   * @param pickTarget - the shape to pick vertices in (layout coordinate system)
   */
  protected Collection<V> pickContainedVertices(VisualizationViewer<V, E> vv, Shape pickTarget) {
    MutableSelectedState<V> selectedVertexState = vv.getSelectedVertexState();
    GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
    Collection<V> picked = pickSupport.getVertices(layoutModel, pickTarget);
    //    if (clear) {
    selectedVertexState.clear();
    //    }
    selectedVertexState.select(picked);
    return picked;
  }

  protected Collection<V> toggleSelectionForContainedVertices(
      VisualizationViewer<V, E> vv, Shape pickTarget) {
    MutableSelectedState<V> selectedVertexState = vv.getSelectedVertexState();
    GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
    Collection<V> picked = pickSupport.getVertices(layoutModel, pickTarget);
    picked.forEach(
        v -> {
          boolean selected = selectedVertexState.isSelected(v);
          if (selected) {
            selectedVertexState.deselect(v);
          } else {
            selectedVertexState.select(v);
          }
        });
    return picked.stream().filter(selectedVertexState::isSelected).collect(Collectors.toSet());
  }

  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {
    JComponent c = (JComponent) e.getSource();
    c.setCursor(cursor);
  }

  public void mouseExited(MouseEvent e) {
    JComponent c = (JComponent) e.getSource();
    c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  public void mouseMoved(MouseEvent e) {}

  /** @return Returns the locked. */
  public boolean isLocked() {
    return locked;
  }

  /** @param locked The locked to set. */
  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  public String toString() {
    return getClass().getSimpleName()
        + "\n regionSelectionMask :"
        + Modifiers.maskStrings.get(regionSelectionMask)
        + "\n toggleRegionSelectionMask:"
        + Modifiers.maskStrings.get(toggleRegionSelectionMask)
        + "\n regionSelectionCompleteMask:"
        + Modifiers.maskStrings.get(regionSelectionCompleteMask)
        + "\n toggleRegionSelectionCompleteMask:"
        + Modifiers.maskStrings.get(toggleRegionSelectionCompleteMask);
  }
}
