package org.jungrapht.visualization.control;

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.function.Supplier;
import javax.swing.JComponent;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A plugin that can create vertices, undirected edges, and directed edges using mouse gestures.
 *
 * <p>vertexSupport and edgeSupport member classes are responsible for actually creating the new
 * graph elements, and for repainting the view when changes were made.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class EditingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  private static final Logger log = LoggerFactory.getLogger(EditingGraphMousePlugin.class);

  public static class Builder<
      V, E, T extends EditingGraphMousePlugin, B extends Builder<V, E, T, B>> {
    protected int vertexEditingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "vertexEditingMask", "MB1"));
    protected Supplier<V> vertexFactory;
    protected Supplier<E> edgeFactory;
    protected VertexSupport<V, E> vertexSupport;
    protected EdgeSupport<V, E> edgeSupport;

    protected Builder(Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
      this.vertexFactory = vertexFactory;
      this.edgeFactory = edgeFactory;
      this.vertexSupport = new SimpleVertexSupport<>(vertexFactory);
      this.edgeSupport = new SimpleEdgeSupport<>(edgeFactory);
    }

    public B self() {
      return (B) this;
    }

    public B vertexEditingMask(int vertexEditingMask) {
      this.vertexEditingMask = vertexEditingMask;
      return self();
    }

    public B vertexSupport(VertexSupport<V, E> vertexSupport) {
      this.vertexSupport = vertexSupport;
      return self();
    }

    public B edgeSupport(EdgeSupport<V, E> edgeSupport) {
      this.edgeSupport = edgeSupport;
      return self();
    }

    public T build() {
      return (T) new EditingGraphMousePlugin<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder(
      Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
    return new Builder<>(vertexFactory, edgeFactory);
  }

  protected int vertexEditingMask;

  protected VertexSupport<V, E> vertexSupport;
  protected EdgeSupport<V, E> edgeSupport;
  private Creating createMode = Creating.UNDETERMINED;

  private enum Creating {
    EDGE,
    VERTEX,
    UNDETERMINED
  }

  /**
   * Creates an instance and prepares shapes for visual effects, using the default modifiers of
   * BUTTON1_DOWN_MASK.
   */
  public EditingGraphMousePlugin(Builder<V, E, ?, ?> builder) {
    this.vertexEditingMask = builder.vertexEditingMask;
    this.vertexSupport = builder.vertexSupport;
    this.edgeSupport = builder.edgeSupport;
  }

  /**
   * Overridden to be more flexible, and pass events with key combinations. The default responds to
   * both ButtonOne and ButtonOne+Shift
   */
  @Override
  public boolean checkModifiers(MouseEvent e) {
    return e.getModifiersEx() == vertexEditingMask;
  }

  /**
   * If the mouse is pressed in an empty area, create a new vertex there. If the mouse is pressed on
   * an existing vertex, prepare to create an edge from that vertex to another
   */
  public void mousePressed(MouseEvent e) {
    log.trace("mousePressed in {}", this.getClass().getName());
    if (e.getModifiersEx() == vertexEditingMask) {
      final VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
      final LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();

      Point2D down = e.getPoint();
      TransformSupport<V, E> transformSupport = vv.getTransformSupport();
      Point2D layoutPoint = transformSupport.inverseTransform(vv, down);

      GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
      if (pickSupport != null) {
        final V vertex = pickSupport.getVertex(layoutModel, layoutPoint.getX(), layoutPoint.getY());
        if (vertex != null) { // get ready to make an edge
          this.createMode = Creating.EDGE;
          edgeSupport.startEdgeCreate(vv, vertex, e.getPoint());
        } else { // make a new vertex
          this.createMode = Creating.VERTEX;
          vertexSupport.startVertexCreate(vv, e.getPoint());
        }
      }
    }
  }

  /**
   * If startVertex is non-null, and the mouse is released over an existing vertex, create an edge
   * from startVertex to the vertex under the mouse pointer.
   */
  @SuppressWarnings("unchecked")
  public void mouseReleased(MouseEvent e) {
    log.trace("mousePressed in {}", this.getClass().getName());
    final VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    final LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();

    Point2D down = e.getPoint();
    TransformSupport<V, E> transformSupport = vv.getTransformSupport();
    Point2D layoutPoint = transformSupport.inverseTransform(vv, down);

    if (createMode == Creating.EDGE) {
      GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
      V vertex = pickSupport.getVertex(layoutModel, layoutPoint.getX(), layoutPoint.getY());
      if (vertex != null) {
        edgeSupport.endEdgeCreate(vv, vertex);
        vv.getEdgeSpatial().recalculate();

      } else {
        edgeSupport.abort(vv);
      }
    } else if (createMode == Creating.VERTEX) {
      vertexSupport.endVertexCreate(vv, e.getPoint());
      vv.getVertexSpatial().recalculate();
    }
    createMode = Creating.UNDETERMINED;
  }

  /**
   * If startVertex is non-null, stretch an edge shape between startVertex and the mouse pointer to
   * simulate edge creation
   */
  @SuppressWarnings("unchecked")
  public void mouseDragged(MouseEvent e) {
    if (e.getModifiersEx() == vertexEditingMask) {
      VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
      if (createMode == Creating.EDGE) {
        edgeSupport.midEdgeCreate(vv, e.getPoint());
      } else if (createMode == Creating.VERTEX) {
        vertexSupport.midVertexCreate(vv, e.getPoint());
      }
    }
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

  public VertexSupport<V, E> getVertexSupport() {
    return vertexSupport;
  }

  public void setVertexSupport(VertexSupport<V, E> vertexSupport) {
    this.vertexSupport = vertexSupport;
  }

  public EdgeSupport<V, E> edgesupport() {
    return edgeSupport;
  }

  public void setEdgeSupport(EdgeSupport<V, E> edgeSupport) {
    this.edgeSupport = edgeSupport;
  }
}
