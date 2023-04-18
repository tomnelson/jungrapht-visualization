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
import org.jungrapht.visualization.selection.VertexEndpointsSelectedEdgeSelectedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VertexSelectingGraphMousePlugin supports the selecting of graph vertices with the mouse.
 * MouseButtonOne selects a single vertex. If a Vertex is selected and the mouse is dragged while on
 * the selected Vertex, then that Vertex will be repositioned to follow the mouse until the button
 * is released.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class VertexSelectingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  private static final Logger log = LoggerFactory.getLogger(VertexSelectingGraphMousePlugin.class);

  static {
    PropertyLoader.load();
  }

  public static class Builder<
      V, E, T extends VertexSelectingGraphMousePlugin, B extends Builder<V, E, T, B>> {
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
      return (T) new VertexSelectingGraphMousePlugin(this);
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

  protected V deselectedVertex;

  /** controls whether the Vertices may be moved with the mouse */
  protected boolean locked;

  protected Rectangle2D footprintRectangle = new Rectangle2D.Double();
  protected VisualizationViewer.Paintable pickFootprintPaintable;

  /** color for the picking rectangle */
  protected Color lensColor = Color.cyan;

  protected Point2D deltaDown;

  protected int singleSelectionMask;

  protected int toggleSingleSelectionMask;

  protected boolean showFootprint;

  public VertexSelectingGraphMousePlugin(Builder<V, E, ?, ?> builder) {
    this(builder.singleSelectionMask, builder.toggleSingleSelectionMask, builder.showFootprint);
  }

  public VertexSelectingGraphMousePlugin() {
    this(VertexSelectingGraphMousePlugin.builder());
  }

  /** create an instance with overrides */
  public VertexSelectingGraphMousePlugin(
      int singleSelectionMask, int toggleSingleSelectionMask, boolean showFootprint) {
    this.singleSelectionMask = singleSelectionMask;
    this.toggleSingleSelectionMask = toggleSingleSelectionMask;
    this.showFootprint = showFootprint;
    this.pickFootprintPaintable = new FootprintPaintable();
    this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  }

  /**
   * Configure the passed VisualizationViewer to use vertex selection only. No edge selection with
   * the mouse. Edges will be 'selected' only when both endpoint vertices are selected.
   *
   * @param visualizationViewer to configure
   * @param <V> vertex type
   * @param <E> edge type
   */
  public static <V, E> void configure(VisualizationViewer<V, E> visualizationViewer) {
    visualizationViewer.setSelectedEdgeState(
        new VertexEndpointsSelectedEdgeSelectedState<>(
            visualizationViewer.getVisualizationModel()::getGraph,
            visualizationViewer.getSelectedVertexState()));
    visualizationViewer.setGraphMouse(
        DefaultGraphMouse.<V, E>builder().vertexSelectionOnly(true).build());
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
   * mouse pointer. For additional selection (default Shift+MouseButton1): Add to the selection, a
   * single Vertex or Edge that is under the mouse pointer. If a previously selected Vertex or Edge
   * is under the pointer, it is un-selected.
   *
   * @param e the event
   */
  public void mousePressed(MouseEvent e) {
    down = e.getPoint();
    log.trace("mouse pick at screen coords {}", e.getPoint());
    deltaDown = down;
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    TransformSupport<V, E> transformSupport = vv.getTransformSupport();
    //    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
    //    GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
    //    MutableSelectedState<V> selectedVertexState = vv.getSelectedVertexState();

    //    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();

    // a rectangle in the view coordinate system.
    this.footprintRectangle =
        new Rectangle2D.Double(
            e.getPoint().x - pickSize / 2, e.getPoint().y - pickSize / 2, pickSize, pickSize);

    if (showFootprint) {
      vv.addPostRenderPaintable(pickFootprintPaintable);
    }
    vv.repaint();

    // subclass can override to account for view distortion effects
    // layoutPoint is the mouse event point projected on the layout coordinate system
    Point2D layoutPoint = transformSupport.inverseTransform(vv, down);
    log.trace("layout coords of mouse click {}", layoutPoint);

    boolean vertexWasSelected = false;
    if (e.getModifiersEx() == singleSelectionMask) {
      vertexWasSelected = this.singleVertexSelection(e, layoutPoint, false);
    } else if (e.getModifiersEx() == toggleSingleSelectionMask) {
      vertexWasSelected = this.singleVertexSelection(e, layoutPoint, true);
    }
    if (vertexWasSelected) {
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

  /**
   * If the mouse is dragging a rectangle, pick the Vertices contained in that rectangle
   *
   * <p>clean up settings from mousePressed
   */
  @SuppressWarnings("unchecked")
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
}
