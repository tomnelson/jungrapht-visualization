package org.jungrapht.visualization.spatial;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.RadiusGraphElementAccessor;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.RadiusVertexAccessor;
import org.jungrapht.visualization.spatial.rtree.TreeNode;

/**
 * Basic interface for Spatial data
 *
 * @author Tom Nelson
 */
public interface Spatial<T> extends LayoutStateChange.Listener {

  /**
   * a flag to suggest whether or not the spatial structure should be used
   *
   * @param active
   */
  void setActive(boolean active);

  /** @return a hint about whether the spatial structure should be used */
  boolean isActive();

  /** @return a geometic representation of the spatial structure */
  List<Shape> getGrid();

  /**
   * a short-lived collection of recent pick target areas
   *
   * @return
   */
  Collection<Shape> getPickShapes();

  /** destroy the current spatial structure */
  void clear();

  /** rebuild the data structure */
  void recalculate();

  /** @return the 2 dimensional area of interest for this class */
  Rectangle2D getLayoutArea();

  /** @param bounds the new bounds for the data struture */
  void setBounds(Rectangle2D bounds);

  /**
   * expands the passed rectangle so that it includes the passed point
   *
   * @param rect the area to consider
   * @param p the point that may be outside of the area
   * @return a new rectangle
   */
  default Rectangle2D getUnion(Rectangle2D rect, Point2D p) {
    rect.add(p);
    return rect;
  }

  default Rectangle2D getUnion(Rectangle2D rect, double x, double y) {
    rect.add(x, y);
    return rect;
  }

  /**
   * update the spatial structure with the (possibly new) location of the passed element
   *
   * @param element the element to consider
   * @param location the location of the element
   */
  void update(T element, org.jungrapht.visualization.layout.model.Point location);

  /**
   * @param p a point to search in the spatial structure
   * @return all leaf nodes that contain the passed point
   */
  Set<? extends TreeNode> getContainingLeafs(Point2D p);

  /**
   * @param x the x location to search for
   * @param y the y location to search for
   * @return all leaf nodes that contain the passed coordinates
   */
  Set<? extends TreeNode> getContainingLeafs(double x, double y);

  /**
   * @param element element to search for
   * @return the leaf node that currently contains the element (not a spatial search)
   */
  TreeNode getContainingLeaf(Object element);

  /**
   * @param shape a shape to filter the spatial structure's elements
   * @return all elements that are contained in the passed shape
   */
  Set<T> getVisibleElements(Shape shape);

  /**
   * @param p a point to search in the spatial structure
   * @return the closest element to the passed point
   */
  T getClosestElement(Point2D p);

  /**
   * @param x coordinate of a point to search in the spatial structure
   * @param y coordinate of a point to search in the spatial structure
   * @return the closest element to the passed coordinates
   */
  T getClosestElement(double x, double y);

  /**
   * a special case Spatial that does no filtering
   *
   * @param <T> the type for elements in the spatial
   * @param <NT> the type for the Vertices in a LayoutModel
   */
  abstract class NoOp<T, NT> extends AbstractSpatial<T, NT> {

    private TreeNode treeVertex;

    public NoOp(LayoutModel<NT> layoutModel) {
      super(layoutModel);
      this.treeVertex = new DegenerateTreeVertex(layoutModel);
    }

    /**
     * return the entire area
     *
     * @return
     */
    @Override
    public List<Shape> getGrid() {
      return Collections.singletonList(getLayoutArea());
    }

    /** nothing to clear */
    @Override
    public void clear() {
      // no op
    }

    /** nothing to recalculate */
    @Override
    public void recalculate() {
      // no op
    }

    /**
     * return the entire area
     *
     * @return
     */
    @Override
    public Rectangle2D getLayoutArea() {
      return new Rectangle2D.Double(0, 0, layoutModel.getWidth(), layoutModel.getHeight());
    }

    /**
     * nothing to change
     *
     * @param bounds the new bounds for the data struture
     */
    @Override
    public void setBounds(Rectangle2D bounds) {
      // no op
    }

    /**
     * nothing to update
     *
     * @param element the element to consider
     * @param location the location of the element
     */
    @Override
    public void update(T element, Point location) {
      // no op
    }

    /**
     * @param p a point to search in the spatial structure
     * @return the single element that contains everything
     */
    @Override
    public Set<? extends TreeNode> getContainingLeafs(Point2D p) {
      return Collections.singleton(this.treeVertex);
    }

    /**
     * @param x the x location to search for
     * @param y the y location to search for
     * @return the single element that contains everything
     */
    @Override
    public Set<? extends TreeNode> getContainingLeafs(double x, double y) {
      return Collections.singleton(this.treeVertex);
    }

    /**
     * @param element element to search for
     * @return the single leaf that contains everything
     */
    @Override
    public TreeNode getContainingLeaf(Object element) {
      return this.treeVertex;
    }

    /**
     * a TreeNode that is immutable and covers the entire layout area
     *
     * @param <V>
     */
    public static class DegenerateTreeVertex<V> implements TreeNode {
      LayoutModel<V> layoutModel;

      public DegenerateTreeVertex(LayoutModel<V> layoutModel) {
        this.layoutModel = layoutModel;
      }

      @Override
      public Rectangle2D getBounds() {
        return new Rectangle2D.Double(0, 0, layoutModel.getWidth(), layoutModel.getHeight());
      }

      /**
       * contains no children
       *
       * @return
       */
      @Override
      public List<? extends TreeNode> getChildren() {
        return Collections.emptyList();
      }
    }

    public static class Vertex<V> extends NoOp<V, V> {

      RadiusVertexAccessor<V> accessor;

      public Vertex(LayoutModel<V> layoutModel) {
        super(layoutModel);
        this.accessor = new RadiusVertexAccessor<>();
      }

      @Override
      public Set<V> getVisibleElements(Shape shape) {
        return layoutModel.getGraph().vertexSet();
      }

      @Override
      public void setActive(boolean active) {
        // noop
      }

      @Override
      public V getClosestElement(Point2D p) {
        return getClosestElement(p.getX(), p.getY());
      }

      @Override
      public V getClosestElement(double x, double y) {
        return null; //use radius node accessor
      }
    }

    public static class Edge<E, V> extends NoOp<E, V> {

      private VisualizationModel<V, E> visualizationModel;
      RadiusGraphElementAccessor<V, E> accessor;

      public Edge(VisualizationModel<V, E> visualizationModel) {
        super(visualizationModel.getLayoutModel());
        this.visualizationModel = visualizationModel;
        this.accessor = new RadiusGraphElementAccessor<>(visualizationModel.getGraph());
      }

      @Override
      public Set<E> getVisibleElements(Shape shape) {
        return visualizationModel.getGraph().edgeSet();
      }

      @Override
      public void setActive(boolean active) {
        // noop
      }

      @Override
      public E getClosestElement(Point2D p) {
        return getClosestElement(p.getX(), p.getY());
      }

      @Override
      public E getClosestElement(double x, double y) {
        return accessor.getEdge(layoutModel, x, y);
      }
    }
  }
}
