/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 * Created on Mar 8, 2005
 *
 */
package org.jungrapht.visualization.control;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import javax.swing.*;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.GraphElementAccessor;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SelectingGraphMousePlugin supports the selecting of graph elements with the mouse. MouseButtonOne
 * selects a single vertex or edge, and MouseButtonTwo adds to the set of selected Vertices or
 * EdgeType. If a Vertex is selected and the mouse is dragged while on the selected Vertex, then
 * that Vertex will be repositioned to follow the mouse until the button is released.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class SelectingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  private static final Logger log = LoggerFactory.getLogger(SelectingGraphMousePlugin.class);

  private static final String PICK_AREA_SIZE = PREFIX + "pickAreaSize";
  private static final String ARBITRARY_SHAPE_SELECTION = PREFIX + "arbitraryShapeSelection";

  protected int pickSize = Integer.getInteger(PICK_AREA_SIZE, 4);

  /** the selected Vertex, if any */
  protected V vertex;

  /** the selected Edge, if any */
  protected E edge;

  /** controls whether the Vertices may be moved with the mouse */
  protected boolean locked;

  protected int selectionModifiers;

  /** additional modifiers for the action of adding to an existing selection */
  protected int addToSelectionModifiers;

  /** used to draw a rectangle to contain selected vertices */
  protected Shape viewRectangle = new Rectangle2D.Float();
  // viewRectangle projected onto the layout coordinate system
  protected Shape layoutTargetShape = viewRectangle;

  /** the Paintable for the lens picking rectangle */
  protected VisualizationServer.Paintable lensPaintable;

  protected Rectangle2D footprintRectangle = new Rectangle2D.Float();
  protected VisualizationViewer.Paintable pickFootprintPaintable;

  /** color for the picking rectangle */
  protected Color lensColor = Color.cyan;

  protected Point2D deltaDown; // what's that flower you have on...

  /** on mouse press, record the current state of the vertexSpatial.isActive() */
  protected boolean vertexSpatialActiveInitialState;

  /** on mouse press, record the current state of the edgeSpatial.isActive() */
  protected boolean edgeSpatialActiveInitialState;

  protected MultiSelectionStrategy multiSelectionStrategy =
      Boolean.parseBoolean(System.getProperty(ARBITRARY_SHAPE_SELECTION, "false"))
          ? MultiSelectionStrategy.arbitrary()
          : MultiSelectionStrategy.rectangular();

  /** create an instance with default settings */
  public SelectingGraphMousePlugin() {
    this(
        InputEvent.BUTTON1_DOWN_MASK,
        InputEvent.CTRL_DOWN_MASK, // select or drag select in rectangle
        InputEvent.SHIFT_DOWN_MASK // drag select in non-rectangular shape
        );
  }

  /**
   * create an instance with overrides
   *
   * @param selectionModifiers for primary selection
   * @param addToSelectionModifiers for additional selection
   */
  public SelectingGraphMousePlugin(
      int modifiers, int selectionModifiers, int addToSelectionModifiers) {
    super(modifiers);
    this.selectionModifiers = selectionModifiers;
    this.addToSelectionModifiers = addToSelectionModifiers;
    this.lensPaintable = new LensPaintable();
    this.pickFootprintPaintable = new FootprintPaintable();
    this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  }

  public void setMultiSelectionStrategy(MultiSelectionStrategy multiSelectionStrategy) {
    this.multiSelectionStrategy = multiSelectionStrategy;
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

  class FootprintPaintable implements VisualizationServer.Paintable {

    public void paint(Graphics g) {
      Color oldColor = g.getColor();
      g.setColor(lensColor);
      ((Graphics2D) g).draw(footprintRectangle);
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
    down = e.getPoint();
    log.trace("mouse pick at screen coords {}", e.getPoint());
    deltaDown = down;
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    vertexSpatialActiveInitialState = vv.getVertexSpatial().isActive();
    edgeSpatialActiveInitialState = vv.getEdgeSpatial().isActive();
    TransformSupport<V, E> transformSupport = vv.getTransformSupport();
    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
    GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
    MutableSelectedState<V> selectedVertexState = vv.getSelectedVertexState();
    MutableSelectedState<E> selectedEdgeState = vv.getSelectedEdgeState();
    viewRectangle = multiSelectionStrategy.getInitialShape(e.getPoint());

    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();

    this.footprintRectangle =
        new Rectangle2D.Float(
            (float) e.getPoint().x - pickSize / 2,
            (float) e.getPoint().y - pickSize / 2,
            pickSize,
            pickSize);

    vv.addPostRenderPaintable(pickFootprintPaintable);
    vv.repaint();
    // subclass can override to account for view distortion effects
    updatePickingTargets(vv, multiLayerTransformer, down, down);

    // subclass can override to account for view distortion effects
    // layoutPoint is the mouse event point projected on the layout coordinate system
    Point2D layoutPoint = transformSupport.inverseTransform(vv, down);
    log.trace("layout coords of mouse click {}", layoutPoint);
    if (e.getModifiersEx() == (modifiers | selectionModifiers)) { // default button1 down and ctrl

      this.vertex = pickSupport.getVertex(layoutModel, layoutPoint.getX(), layoutPoint.getY());

      if (vertex != null) {

        log.trace("mousePressed set the vertex to {}", vertex);
        if (!selectedVertexState.isSelected(vertex)) {
          selectedVertexState.clear();
          selectedVertexState.select(vertex);
        }
        e.consume();
        return;
      }

      this.edge = pickSupport.getEdge(layoutModel, layoutPoint.getX(), layoutPoint.getY());

      if (edge != null) {

        log.trace("mousePressed set the edge to {}", edge);
        if (!selectedEdgeState.isSelected(edge)) {
          selectedEdgeState.clear();
          selectedEdgeState.select(edge);
        }
        e.consume();
        return;
      }

      // got no vertex and no edge, clear all selections
      selectedEdgeState.clear();
      selectedVertexState.clear();
      vv.addPostRenderPaintable(lensPaintable);
      e.consume();
      return;

    } else if (e.getModifiersEx() == (modifiers | selectionModifiers | addToSelectionModifiers)) {

      vv.addPostRenderPaintable(lensPaintable);

      this.vertex = pickSupport.getVertex(layoutModel, layoutPoint.getX(), layoutPoint.getY());

      if (vertex != null) {
        log.trace("mousePressed set the vertex to {}", vertex);
        if (selectedVertexState.isSelected(vertex)) {
          selectedVertexState.deselect(vertex);
        } else {
          selectedVertexState.select(vertex);
        }
        e.consume();
        return;
      }

      this.edge = pickSupport.getEdge(layoutModel, layoutPoint.getX(), layoutPoint.getY());

      if (edge != null) {
        log.trace("mousePressed set the edge to {}", edge);
        if (selectedEdgeState.isSelected(edge)) {
          selectedEdgeState.deselect(edge);
        } else {
          selectedEdgeState.select(edge);
        }
        e.consume();
        return;
      }
    } else {
      down = null;
    }
    e.consume();
  }

  /**
   * If the mouse is dragging a rectangle, pick the Vertices contained in that rectangle
   *
   * <p>clean up settings from mousePressed
   */
  @SuppressWarnings("unchecked")
  public void mouseReleased(MouseEvent e) {
    Point2D out = e.getPoint();

    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    // if the vertexSpatial was active at mousePress (and deactivated during drag) reactivate it now
    if (vertexSpatialActiveInitialState) {
      vv.getVertexSpatial().setActive(vertexSpatialActiveInitialState);
    }
    // if the edgeSpatial was active at mousePress (and deactivated during drag) reactivate it now
    if (edgeSpatialActiveInitialState) {
      vv.getEdgeSpatial().setActive(vertexSpatialActiveInitialState);
    }
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();

    // mouse is not down, check only for the addToSelectionModifiers (defaults to SHIFT_DOWN_MASK)
    if (e.getModifiersEx() == (selectionModifiers | addToSelectionModifiers)) {
      if (down != null) {
        if (vertex == null && !heyThatsTooClose(down, out, 5)) {
          pickContainedVertices(vv, layoutTargetShape, false);
        }
      }
    } else if (e.getModifiersEx() == selectionModifiers) {
      if (down != null) {
        if (vertex == null && !heyThatsTooClose(down, out, 5)) {
          pickContainedVertices(vv, layoutTargetShape, true);
        }
      }
    }
    log.trace("down:{} out:{}", down, out);
    if (vertex != null && !down.equals(out)) {

      multiSelectionStrategy.closeShape();
      // dragging points and changing their layout locations
      Point2D graphPoint = multiLayerTransformer.inverseTransform(out);
      log.trace("p in graph coords is {}", graphPoint);
      Point2D graphDown = multiLayerTransformer.inverseTransform(deltaDown);
      log.trace("graphDown (down in graph coords) is {}", graphDown);
      VisualizationModel<V, E> visualizationModel = vv.getVisualizationModel();
      LayoutModel<V> layoutModel = visualizationModel.getLayoutModel();
      double dx = graphPoint.getX() - graphDown.getX();
      double dy = graphPoint.getY() - graphDown.getY();
      log.trace("dx, dy: {},{}", dx, dy);
      MutableSelectedState<V> ps = vv.getSelectedVertexState();

      for (V v : ps.getSelected()) {
        org.jungrapht.visualization.layout.model.Point vp = layoutModel.apply(v);
        vp = org.jungrapht.visualization.layout.model.Point.of(vp.x + dx, vp.y + dy);
        layoutModel.set(v, vp);
      }
      deltaDown = out;
    }

    down = null;
    vertex = null;
    edge = null;
    layoutTargetShape = multiLayerTransformer.inverseTransform(viewRectangle);
    vv.removePostRenderPaintable(lensPaintable);
    vv.removePostRenderPaintable(pickFootprintPaintable);
    vv.repaint();
  }

  /**
   * If the mouse is over a selected vertex, drag all selected vertices with the mouse. If the mouse
   * is not over a Vertex, draw the rectangle to select multiple Vertices
   */
  public void mouseDragged(MouseEvent e) {
    log.trace("mouseDragged");
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    // if the vertexSpatial was initially active at mousePress, deactivate it during the drag
    if (vertexSpatialActiveInitialState) {
      vv.getVertexSpatial().setActive(false);
    }
    // if the edgeSpatial was initially active at mousePress, deactivate it during the drag
    if (edgeSpatialActiveInitialState) {
      vv.getEdgeSpatial().setActive(false);
    }
    if (!locked) {

      MultiLayerTransformer multiLayerTransformer =
          vv.getRenderContext().getMultiLayerTransformer();
      Point2D p = e.getPoint();
      log.trace("view p for drag event is {}", p);
      log.trace("down is {}", down);
      if (vertex != null) {
        // dragging points and changing their layout locations
        Point2D graphPoint = multiLayerTransformer.inverseTransform(p);
        log.trace("p in graph coords is {}", graphPoint);
        Point2D graphDown = multiLayerTransformer.inverseTransform(deltaDown);
        log.trace("graphDown (down in graph coords) is {}", graphDown);
        VisualizationModel<V, E> visualizationModel = vv.getVisualizationModel();
        LayoutModel<V> layoutModel = visualizationModel.getLayoutModel();
        double dx = graphPoint.getX() - graphDown.getX();
        double dy = graphPoint.getY() - graphDown.getY();
        log.trace("dx, dy: {},{}", dx, dy);
        MutableSelectedState<V> ps = vv.getSelectedVertexState();

        for (V v : ps.getSelected()) {
          org.jungrapht.visualization.layout.model.Point vp = layoutModel.apply(v);
          vp = Point.of(vp.x + dx, vp.y + dy);
          layoutModel.set(v, vp);
        }
        deltaDown = p;

      } else if (down != null) {
        Point2D out = e.getPoint();
        multiSelectionStrategy.updateShape(down, out);
        layoutTargetShape = multiLayerTransformer.inverseTransform(viewRectangle);
      }
      if (vertex != null) {
        e.consume();
      }
      vv.repaint();
    }
  }

  /**
   * rejects picking if the rectangle is too small, like if the user meant to select one vertex but
   * moved the mouse slightly
   *
   * @param p
   * @param q
   * @param min
   * @return
   */
  private boolean heyThatsTooClose(Point2D p, Point2D q, double min) {
    return Math.abs(p.getX() - q.getX()) < min && Math.abs(p.getY() - q.getY()) < min;
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
    log.trace("updatePickingTargets with {} to {}", down, out);

    multiSelectionStrategy.updateShape(down, down);

    layoutTargetShape = multiLayerTransformer.inverseTransform(viewRectangle);

    if (log.isTraceEnabled()) {
      log.trace("viewRectangle {}", viewRectangle);
      log.trace("layoutTargetShape bounds {}", layoutTargetShape.getBounds());
    }
  }

  /**
   * pick the vertices inside the rectangle created from points 'down' and 'out' (two diagonally
   * opposed corners of the rectangle)
   *
   * @param vv the viewer containing the layout and selected state
   * @param pickTarget - the shape to pick vertices in (layout coordinate system)
   * @param clear whether to reset existing selected state
   */
  protected void pickContainedVertices(
      VisualizationViewer<V, E> vv, Shape pickTarget, boolean clear) {
    MutableSelectedState<V> selectedVertexState = vv.getSelectedVertexState();
    GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
    Collection<V> picked = pickSupport.getVertices(layoutModel, pickTarget);
    if (clear) {
      selectedVertexState.clear();
    }
    selectedVertexState.select(picked);
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
}
