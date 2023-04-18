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
import javax.swing.*;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.PropertyLoader;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.selection.ShapePickSupport;
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

  static {
    PropertyLoader.load();
  }

  public static class Builder<
      V, E, T extends SelectingGraphMousePlugin, B extends Builder<V, E, T, B>> {
    protected int singleSelectionMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "singleSelectionMask", "MB1_MENU"));
    protected int toggleSingleSelectionMask =
        Modifiers.masks.get(
            System.getProperty(PREFIX + "toggleSingleSelectionMask", "MB1_SHIFT_MENU"));

    protected boolean showFootprint =
        Boolean.parseBoolean(System.getProperty(PREFIX + "showFootprint", "false"));

    public B self() {
      return (B) this;
    }

    public B singleSelectionMask(int singleSelectionMask) {
      this.singleSelectionMask = singleSelectionMask;
      return self();
    }

    public B toggleSingleSelectionMask(int toggleSingleSelectionMask) {
      this.toggleSingleSelectionMask = toggleSingleSelectionMask;
      return self();
    }

    public B showFootprint(boolean showFootprint) {
      this.showFootprint = showFootprint;
      return self();
    }

    public T build() {
      return (T) new SelectingGraphMousePlugin(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  private static final int TOO_CLOSE_LIMIT = 5;

  private static final String PICK_AREA_SIZE = PREFIX + "pickAreaSize";

  protected int pickSize = Integer.getInteger(PICK_AREA_SIZE, 4);

  /** the selected Vertex, if any */
  protected V vertex;

  /** the selected Edge, if any */
  protected E edge;

  /** controls whether the Vertices may be moved with the mouse */
  protected boolean locked;

  protected Rectangle2D footprintRectangle = new Rectangle2D.Double();
  protected VisualizationViewer.Paintable pickFootprintPaintable;

  /** color for the picking rectangle */
  protected Color lensColor = Color.cyan;

  protected Point2D deltaDown;

  protected MultiSelectionStrategy multiSelectionStrategy;

  protected int singleSelectionMask;
  protected int toggleSingleSelectionMask;

  protected boolean showFootprint;

  public SelectingGraphMousePlugin(Builder<V, E, ?, ?> builder) {
    this(builder.singleSelectionMask, builder.toggleSingleSelectionMask, builder.showFootprint);
  }

  public SelectingGraphMousePlugin() {
    this(SelectingGraphMousePlugin.builder());
  }
  /**
   * create an instance with overrides
   *
   * @param singleSelectionMask for primary selection of one vertex
   * @param toggleSingleSelectionMask to add another vertex to the current selection
   */
  SelectingGraphMousePlugin(
      int singleSelectionMask, int toggleSingleSelectionMask, boolean showFootprint) {
    this.singleSelectionMask = singleSelectionMask;
    this.toggleSingleSelectionMask = toggleSingleSelectionMask;
    this.showFootprint = showFootprint;
    this.pickFootprintPaintable = new FootprintPaintable();
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
    multiSelectionStrategy = vv.getMultiSelectionStrategySupplier().get();
    TransformSupport<V, E> transformSupport = vv.getTransformSupport();

    // a rectangle in the view coordinate system.
    this.footprintRectangle =
        new Rectangle2D.Double(
            e.getPoint().x - pickSize / 2, e.getPoint().y - pickSize / 2, pickSize, pickSize);

    if (showFootprint) {
      vv.addPostRenderPaintable(pickFootprintPaintable);
    }
    vv.repaint();

    // layoutPoint is the mouse event point projected on the layout coordinate system
    Point2D layoutPoint = transformSupport.inverseTransform(vv, down);
    log.trace("layout coords of mouse click {}", layoutPoint);

    // test for vertex select or vertex add to selection
    boolean vertexWasSelected = false;
    boolean edgeWasSelected = false;
    if (e.getModifiersEx() == singleSelectionMask) {
      vertexWasSelected = this.singleVertexSelection(e, layoutPoint, false);
      if (!vertexWasSelected) {
        edgeWasSelected = this.singleEdgeSelection(e, layoutPoint, false);
      }
    } else if (e.getModifiersEx() == toggleSingleSelectionMask) {
      vertexWasSelected = this.singleVertexSelection(e, layoutPoint, true);
      if (!vertexWasSelected) {
        edgeWasSelected = this.singleEdgeSelection(e, layoutPoint, true);
      }
    }
    if (vertexWasSelected || edgeWasSelected) {
      e.consume();
    }
  }

  protected boolean singleVertexSelection(
      MouseEvent e, Point2D layoutPoint, boolean toggleSelection) {
    VisualizationServer<V, E> vv = (VisualizationServer<V, E>) e.getSource();
    GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
    MutableSelectedState<V> selectedVertexState = vv.getSelectedVertexState();
    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
    if (pickSupport instanceof ShapePickSupport) {
      ShapePickSupport<V, E> shapePickSupport = (ShapePickSupport<V, E>) pickSupport;
      vertex = shapePickSupport.getVertex(layoutModel, footprintRectangle);
    } else {
      vertex = pickSupport.getVertex(layoutModel, layoutPoint.getX(), layoutPoint.getY());
    }

    if (vertex != null) {
      log.trace("mousePressed set the vertex to {}", vertex);
      if (selectedVertexState.isSelected(vertex)) {
        if (toggleSelection) {
          selectedVertexState.deselect(vertex);
          vertex = null;
        }
      } else {
        if (!toggleSelection) {
          selectedVertexState.clear();
        }
        selectedVertexState.select(vertex);
      }
      e.consume();
      return true;
    }
    return false;
  }

  protected boolean singleEdgeSelection(MouseEvent e, Point2D layoutPoint, boolean addToSelection) {
    VisualizationServer<V, E> vv = (VisualizationServer<V, E>) e.getSource();
    GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
    MutableSelectedState<E> selectedEdgeState = vv.getSelectedEdgeState();
    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();

    E edge;
    if (pickSupport instanceof ShapePickSupport) {
      ShapePickSupport<V, E> shapePickSupport = (ShapePickSupport<V, E>) pickSupport;
      edge = shapePickSupport.getEdge(layoutModel, footprintRectangle);
    } else {
      edge = pickSupport.getEdge(layoutModel, layoutPoint.getX(), layoutPoint.getY());
    }

    if (edge != null) {
      log.trace("mousePressed set the edge to {}", edge);
      if (!selectedEdgeState.isSelected(edge)) {
        if (!addToSelection) {
          selectedEdgeState.clear();
        }
        selectedEdgeState.select(edge);
      } else {
        selectedEdgeState.deselect(edge);
      }
      e.consume();
      return true;
    }
    return false;
  }

  /**
   * If the mouse is dragging a rectangle, pick the Vertices contained in that rectangle
   *
   * <p>clean up settings from mousePressed
   */
  public void mouseReleased(MouseEvent e) {
    Point2D out = e.getPoint();

    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    //    MutableSelectedState<V> ps = vv.getSelectedVertexState();

    log.trace("down:{} out:{}", down, out);
    if (vertex != null && !down.equals(out)) {
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

      for (V v : vv.getSelectedVertices()) {
        org.jungrapht.visualization.layout.model.Point vp = layoutModel.apply(v);
        vp = vp.add(dx, dy);
        layoutModel.set(v, vp);
      }
      deltaDown = out;
    }

    down = null;
    vertex = null;
    edge = null;

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
    if (!locked) {
      MutableSelectedState<V> selectedVertexState = vv.getSelectedVertexState();

      MultiLayerTransformer multiLayerTransformer =
          vv.getRenderContext().getMultiLayerTransformer();
      Point2D p = e.getPoint();
      log.trace("view p for drag event is {}", p);
      log.trace("down is {}", down);
      if (vertex != null) {
        selectedVertexState.select(vertex);
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
        //        MutableSelectedState<V> ps = vv.getSelectedVertexState();

        for (V v : vv.getSelectedVertices()) {
          org.jungrapht.visualization.layout.model.Point vp = layoutModel.apply(v);
          vp = vp.add(dx, dy); //Point.of(vp.x + dx, vp.y + dy);
          layoutModel.set(v, vp);
        }
        deltaDown = p;
      }
      if (vertex != null) {
        e.consume();
      }
      vv.repaint();
    }
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
        + "\n singleSelectionMask :"
        + Modifiers.maskStrings.get(singleSelectionMask)
        + "\n toggleSingleSelectionMask:"
        + Modifiers.maskStrings.get(toggleSingleSelectionMask);
  }
}
