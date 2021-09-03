/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 * Created on Mar 11, 2005
 *
 */
package org.jungrapht.visualization.selection;

import static org.jungrapht.visualization.MultiLayerTransformer.Layer;
import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.PropertyLoader;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.control.GraphElementAccessor;
import org.jungrapht.visualization.decorators.ExpandXY;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.spatial.Spatial;
import org.jungrapht.visualization.spatial.SpatialRTree;
import org.jungrapht.visualization.spatial.rtree.LeafNode;
import org.jungrapht.visualization.spatial.rtree.TreeNode;
import org.jungrapht.visualization.transform.LensTransformer;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.jungrapht.visualization.util.AWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>GraphElementAccessor</code> that returns elements whose <code>Shape</code> contains the
 * specified pick point or region.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class ShapePickSupport<V, E> implements GraphElementAccessor<V, E> {

  static {
    PropertyLoader.load();
  }

  private static final String PICK_AREA_SIZE = PREFIX + "pickAreaSize";
  private static final String PICKING_STYLE = PREFIX + "pickingStyle";

  private static final Logger log = LoggerFactory.getLogger(ShapePickSupport.class);

  /**
   * The available picking heuristics:
   *
   * <ul>
   *   <li><code>Style.CENTERED</code>: returns the element whose center is closest to the pick
   *       point.
   *   <li><code>Style.LOWEST</code>: returns the first such element encountered. (If the element
   *       collection has a consistent ordering, this will also be the element "on the bottom", that
   *       is, the one which is rendered first.)
   *   <li><code>Style.HIGHEST</code>: returns the last such element encountered. (If the element
   *       collection has a consistent ordering, this will also be the element "on the top", that
   *       is, the one which is rendered last.)
   * </ul>
   */
  public enum Style {
    LOWEST,
    CENTERED,
    HIGHEST
  }

  protected int pickSize = Integer.getInteger(PICK_AREA_SIZE, 4);

  /**
   * The <code>VisualizationServer</code> in which the this instance is being used for picking. Used
   * to retrieve properties such as the layout, renderer, vertex and edge shapes, and coordinate
   * transformations.
   */
  protected VisualizationServer<V, E> vv;

  /** The current picking heuristic for this instance. Defaults to <code>CENTERED</code>. */
  protected Style style = Style.valueOf(System.getProperty(PICKING_STYLE, "CENTERED"));

  /**
   * Creates a <code>ShapePickSupport</code> for the <code>vv</code> VisualizationServer, with the
   * specified pick footprint and the default pick style. The <code>VisualizationServer</code> is
   * used to access properties of the current visualization (layout, renderer, coordinate
   * transformations, vertex/edge shapes, etc.).
   *
   * @param vv source of the current <code>Layout</code>.
   * @param pickSize the layoutSize of the pick footprint for line edges
   */
  public ShapePickSupport(VisualizationServer<V, E> vv, int pickSize) {
    this(vv);
    this.pickSize = pickSize;
  }

  /**
   * Create a <code>ShapePickSupport</code> for the specified <code>VisualizationServer</code> with
   * a default pick footprint. of layoutSize 2.
   *
   * @param vv the visualization server used for rendering
   */
  public ShapePickSupport(VisualizationServer<V, E> vv) {
    this.vv = vv;
  }

  /**
   * Returns the style of picking used by this instance. This specifies which of the elements, among
   * those whose shapes contain the pick point, is returned. The available styles are:
   *
   * <ul>
   *   <li><code>Style.CENTERED</code>: returns the element whose center is closest to the pick
   *       point.
   *   <li><code>Style.LOWEST</code>: returns the first such element encountered. (If the element
   *       collection has a consistent ordering, this will also be the element "on the bottom", that
   *       is, the one which is rendered first.)
   *   <li><code>Style.HIGHEST</code>: returns the last such element encountered. (If the element
   *       collection has a consistent ordering, this will also be the element "on the top", that
   *       is, the one which is rendered last.)
   * </ul>
   *
   * @return the style of picking used by this instance
   */
  public Style getStyle() {
    return style;
  }

  /**
   * Specifies the style of picking to be used by this instance. This specifies which of the
   * elements, among those whose shapes contain the pick point, will be returned. The available
   * styles are:
   *
   * <ul>
   *   <li><code>Style.CENTERED</code>: returns the element whose center is closest to the pick
   *       point.
   *   <li><code>Style.LOWEST</code>: returns the first such element encountered. (If the element
   *       collection has a consistent ordering, this will also be the element "on the bottom", that
   *       is, the one which is rendered first.)
   *   <li><code>Style.HIGHEST</code>: returns the last such element encountered. (If the element
   *       collection has a consistent ordering, this will also be the element "on the top", that
   *       is, the one which is rendered last.)
   * </ul>
   *
   * @param style the style to set
   */
  public void setStyle(Style style) {
    this.style = style;
  }

  /**
   * Returns the vertex, if any, whose shape intersects the supplied pickingFootprint. The
   * pickingFootprint is in the view coordinate system. To test each vertex, it will have its shape
   * transformed to the view coordinate system (what the user sees rendered on the screen).
   *
   * @param layoutModel
   * @param pickingFootprint a rectangle in the view coordinate system
   * @return the vertex whos shape intersects the pickingFootprint in the view.
   */
  public V getVertex(LayoutModel<V> layoutModel, Rectangle2D pickingFootprint) {
    if (log.isTraceEnabled()) {
      log.trace("look for vertex intersecting {}", pickingFootprint);
    }
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();

    MutableTransformer viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW);

    // if there is a spatial data structure active, use it
    Spatial<V, V> vertexSpatial = vv.getVertexSpatial();
    if (!(viewTransformer instanceof LensTransformer) && vertexSpatial.isActive()) {
      return getVertex(vertexSpatial, layoutModel, pickingFootprint);
    }

    V closest = null;
    double minDistance = Double.MAX_VALUE;
    // draw the pick area in the view
    if (log.isTraceEnabled()) {
      vv.addPreRenderPaintable(new FootprintPaintable(Color.magenta, pickingFootprint));
    }
    // pick area is in layout coordinates
    while (true) {
      try {
        for (V v : getFilteredVertices()) {

          // get the shape for the vertex (it is at the origin)
          Shape shape = vv.getRenderContext().getVertexShapeFunction().apply(v);
          // get the vertex location in layout coordinate system
          org.jungrapht.visualization.layout.model.Point p = layoutModel.apply(v);
          if (p == null) {
            continue;
          }

          Point2D p2d =
              multiLayerTransformer.transform(MultiLayerTransformer.Layer.LAYOUT, p.x, p.y);
          // now p is in view coordinates, ready to be further transformed by any transform in the
          // graphics context
          float x = (float) p2d.getX();
          float y = (float) p2d.getY();
          // create a transform that translates to the location of
          // the vertex to be rendered
          AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
          // return the transformed vertex shape
          shape = xform.createTransformedShape(shape);

          // account for the graphics transform, or perhaps the lens transform
          shape = viewTransformer.transform(shape);

          if (viewTransformer instanceof LensTransformer) {
            LensTransformer lensTransformer = (LensTransformer) viewTransformer;
            shape = lensTransformer.getDelegate().transform(shape);
          }
          if (log.isTraceEnabled()) {
            vv.addPreRenderPaintable(new FootprintPaintable(Color.pink, shape));
          }

          if (shape.intersects(pickingFootprint)) {

            if (log.isTraceEnabled()) {
              vv.addPreRenderPaintable(new FootprintPaintable(Color.green, shape));
            }

            if (style == Style.LOWEST) {
              // return the first match
              closest = v;
              break; // to return the first
            } else if (style == Style.HIGHEST) {
              // will return the last match
              closest = v;
              // don't break and get the last one
            } else {

              // return the vertex closest to the
              // center of a vertex shape
              Rectangle2D bounds = shape.getBounds2D();
              double dx = bounds.getCenterX() - pickingFootprint.getCenterX();
              double dy = bounds.getCenterY() - pickingFootprint.getCenterY();
              double dist = dx * dx + dy * dy;
              if (dist < minDistance) {
                minDistance = dist;
                closest = v;
              }
            }
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return closest;
  }

  /**
   * uses the spatialRTree to find the closest vertex to the points
   *
   * @param spatial
   * @param layoutModel
   * @param pickingFootprint
   * @return the selected vertex
   */
  protected V getVertex(
      Spatial<V, V> spatial, LayoutModel<V> layoutModel, Rectangle2D pickingFootprint) {

    MultiLayerTransformer mlt = vv.getRenderContext().getMultiLayerTransformer();

    // find the leaf vertex that would contain a point at x,y
    Point2D pickingCenter =
        new Point2D.Double(pickingFootprint.getCenterX(), pickingFootprint.getCenterY());

    // transform the pickingCenter to the layout coordinates
    pickingCenter = mlt.inverseTransform(pickingCenter);

    Collection<? extends TreeNode> containingLeafs = spatial.getContainingLeafs(pickingCenter);

    if (containingLeafs == null || containingLeafs.size() == 0) return null;
    // make a target circle the same size as the leaf vertex
    // leaf vertices are small when vertices are close and large when they are sparse
    // union up all the leafs then make a target
    Rectangle2D union = null;
    for (TreeNode r : containingLeafs) {
      if (union == null) {
        union = r.getBounds();
      } else {
        union = union.createUnion(r.getBounds());
      }
    }
    double width = union.getWidth();
    double height = union.getHeight();
    double radiusx = width / 2;
    double radiusy = height / 2;
    Ellipse2D target =
        new Ellipse2D.Double(
            pickingCenter.getX() - radiusx, pickingCenter.getY() - radiusy, width, height);
    if (log.isTraceEnabled()) {
      log.trace("target is {}", target);
    }

    double minDistance = Double.MAX_VALUE;

    // will be the selected vertex
    V closest = null;

    // get the all vertices from any leafs that intersect the target
    Collection<V> vertices = spatial.getVisibleElements(target);
    if (log.isTraceEnabled()) {
      log.trace("instead of checking all vertices: {}", getFilteredVertices());
      log.trace("out of these candidates: {}...", vertices);
    }

    // Check the (smaller) set of eligible vertices
    // to return the one that contains the (x,y)
    for (V v : vertices) {

      // get the shape for the vertex (it is at the origin)
      Shape shape = vv.getRenderContext().getVertexShapeFunction().apply(v);
      // get the vertex location in layout coordinate system
      org.jungrapht.visualization.layout.model.Point p = layoutModel.apply(v);
      if (p == null) {
        continue;
      }

      Point2D p2d = mlt.transform(MultiLayerTransformer.Layer.LAYOUT, p.x, p.y);
      // now p is in view coordinates, ready to be further transformed by any transform in the
      // graphics context
      float x = (float) p2d.getX();
      float y = (float) p2d.getY();
      // create a transform that translates to the location of
      // the vertex to be rendered
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      // return the transformed vertex shape
      shape = xform.createTransformedShape(shape);

      MutableTransformer viewTransformer = mlt.getTransformer(Layer.VIEW);
      shape = viewTransformer.transform(shape);
      if (viewTransformer instanceof LensTransformer) {
        LensTransformer lensTransformer = (LensTransformer) viewTransformer;
        shape = lensTransformer.getDelegate().transform(shape);
      }

      if (shape.intersects(pickingFootprint)) {
        if (style == Style.LOWEST) {
          // return the first match
          return v;
        } else if (style == Style.HIGHEST) {
          // will return the last match
          closest = v;
        } else {

          // return the vertex closest to the
          // center of a vertex shape
          Rectangle2D bounds = shape.getBounds2D();
          double dx = bounds.getCenterX() - pickingCenter.getX();
          double dy = bounds.getCenterY() - pickingCenter.getY();
          double dist = dx * dx + dy * dy;
          if (dist < minDistance) {
            minDistance = dist;
            closest = v;
          }
        }
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("selected {} with spatial quadtree", closest);
    }
    return closest;
  }

  @Override
  public V getVertex(LayoutModel<V> layoutModel, org.jungrapht.visualization.layout.model.Point p) {
    return getVertex(layoutModel, p.x, p.y);
  }

  /**
   * Returns the vertex, if any, whose shape contains (x, y). If (x,y) is contained in more than one
   * vertex's shape, returns the vertex whose center is closest to the pick point.
   *
   * @param x the x coordinate of the pick point
   * @param y the y coordinate of the pick point
   * @return the vertex whose shape contains (x,y), and whose center is closest to the pick point
   */
  @Override
  public V getVertex(LayoutModel<V> layoutModel, double x, double y) {

    MultiLayerTransformer mlt = vv.getRenderContext().getMultiLayerTransformer();

    // x and y are in layout coordinates. Translate to view and make a footprint
    Point2D layoutPoint = new Point2D.Double(x, y);
    Point2D viewPoint = mlt.transform(layoutPoint);
    Rectangle2D pickFootprint =
        new Rectangle2D.Double(
            viewPoint.getX() - pickSize / 2, viewPoint.getY() - pickSize / 2, pickSize, pickSize);

    return getVertex(layoutModel, pickFootprint);
  }

  @Override
  public V getClosestVertex(LayoutModel<V> layoutModel, V v) {
    return null;
  }

  /**
   * Returns the vertices whose layout coordinates are contained in <code>Shape</code>. The shape is
   * in screen coordinates, and the graph vertices are transformed to screen coordinates before they
   * are tested for inclusion.
   *
   * @return the <code>Collection</code> of vertices whose <code>layout</code> coordinates are
   *     contained in <code>shape</code>.
   */
  @Override
  public Collection<V> getVertices(LayoutModel<V> layoutModel, Shape shape) {
    Set<V> pickedVertices = new HashSet<>();

    // the pick target shape is in layout coordinate system.

    Spatial spatial = vv.getVertexSpatial();
    if (spatial != null) {
      return getContained(spatial, layoutModel, shape);
    }

    // fall back on checking every vertex
    while (true) {
      try {
        for (V v : getFilteredVertices()) {
          org.jungrapht.visualization.layout.model.Point p = layoutModel.apply(v);
          if (p == null) {
            continue;
          }
          if (shape.contains(p.x, p.y)) {
            pickedVertices.add(v);
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return pickedVertices;
  }

  /**
   * use the spatial structure to find vertices inside the passed shape
   *
   * @param spatial
   * @param layoutModel
   * @param shape a target shape in layout coordinates
   * @return the vertices contained in the target shape
   */
  protected Collection<V> getContained(Spatial spatial, LayoutModel<V> layoutModel, Shape shape) {

    Collection<V> visible = new HashSet<>(spatial.getVisibleElements(shape));
    if (log.isTraceEnabled()) {
      log.trace("your shape intersects tree cells with these vertices: {}", visible);
    }

    // some of the vertices that the spatial tree considers visible may be outside
    // of the pick target shape. Check this smaller set of vertices and return only
    // those that are inside the shape
    for (Iterator<V> iterator = visible.iterator(); iterator.hasNext(); ) {
      V vertex = iterator.next();
      org.jungrapht.visualization.layout.model.Point p = layoutModel.apply(vertex);
      if (p == null) {
        continue;
      }
      if (!shape.contains(p.x, p.y)) {
        iterator.remove();
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("these were actually selected: {}", visible);
    }
    return visible;
  }
  /**
   * use the spatial R tree to find edges inside the passed shape
   *
   * @param spatial
   * @param layoutModel
   * @param shape a target shape in layout coordinates
   * @return the vertices contained in the target shape
   */
  protected Collection<E> getContained(
      SpatialRTree.Edges<E, V> spatial, LayoutModel<V> layoutModel, Shape shape) {

    Collection<E> visible = spatial.getVisibleElements(shape);
    if (log.isTraceEnabled()) {
      log.trace("your shape intersects tree cells with these vertices: {}", visible);
    }
    // some of the vertices that the spatial tree considers visible may be outside
    // of the pick target shape. Check this smaller set of vertices and return only
    // those that intersect the shape
    for (Iterator<E> iterator = visible.iterator(); iterator.hasNext(); ) {
      E edge = iterator.next();
      Shape edgeShape = getTransformedEdgeShape(edge);
      if (!edgeShape.intersects(shape.getBounds())) {
        iterator.remove();
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("these were actually selected: {}", visible);
    }
    return visible;
  }

  //// Edge selection

  protected E getEdge(
      SpatialRTree.Edges<E, V> spatial, LayoutModel<V> layoutModel, Rectangle2D pickingFootprint) {
    MultiLayerTransformer mlt = vv.getRenderContext().getMultiLayerTransformer();

    // find the leaf vertex that would contain a point at x,y
    Point2D pickingCenter =
        new Point2D.Double(pickingFootprint.getCenterX(), pickingFootprint.getCenterY());

    // transform the pickingCenter to the layout coordinates
    pickingCenter = mlt.inverseTransform(pickingCenter);

    // find the leaf vertices that would contain a point at x,y
    Collection<LeafNode<E>> containingLeafs = spatial.getContainingLeafs(pickingCenter);

    if (containingLeafs == null || containingLeafs.size() == 0) return null;
    // make a target circle the same size as the leaf vertex area union
    // leaf vertices are small when vertices are close and large when they are sparse
    // union up all the leafs then make a target
    Rectangle2D union = null;
    for (LeafNode<E> r : containingLeafs) {
      if (union == null) {
        union = r.getBounds();
      } else {
        union = union.createUnion(r.getBounds());
      }
    }
    double width = union.getWidth();
    double height = union.getHeight();
    double radiusx = width / 2;
    double radiusy = height / 2;
    Ellipse2D target =
        new Ellipse2D.Double(
            pickingCenter.getX() - radiusx, pickingCenter.getY() - radiusy, width, height);

    if (log.isTraceEnabled()) {
      log.trace("target is {}", target);
    }

    // will be the selected edge
    E closest = null;

    // get the all vertices from any leafs that intersect the target
    Collection<E> edges = spatial.getVisibleElements(target);
    if (log.isTraceEnabled()) {
      log.trace(
          "instead of checking all {} edges: {}", getFilteredEdges().size(), getFilteredEdges());
      log.trace("out of these {} candidates: {}...", edges.size(), edges);
    }

    // Check the (smaller) set of eligible edges
    // to return the one that contains the (x,y)
    for (E edge : edges) {

      // make sure that edgeShape is in view coordinates
      Shape edgeShape = getTransformedEdgeShape(edge);
      if (edgeShape == null) {
        continue;
      }
      MutableTransformer viewTransformer = mlt.getTransformer(Layer.VIEW);
      edgeShape = viewTransformer.transform(edgeShape);
      if (viewTransformer instanceof LensTransformer) {
        LensTransformer lensTransformer = (LensTransformer) viewTransformer;
        edgeShape = lensTransformer.getDelegate().transform(edgeShape);
      }

      Line2D endToEnd = getLineFromShape(edgeShape);
      // for articulated edges, the edge 'shape' is an area bounded by the zig-zag edge and the
      // (invisible) line from source to target vertex. The pick footprint is not inside the shape
      // and is not intersecting the invisible line, but does intersect the zig zag line
      if (!edgeShape.contains(pickingFootprint)
          && edgeShape.intersects(pickingFootprint)
          && !endToEnd.intersects(pickingFootprint)) {
        closest = edge;
        break;
      }
    }
    return closest;
  }

  public E getEdge(LayoutModel<V> layoutModel, Rectangle2D pickFootprint) {
    E closest = null;
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW);

    while (true) {
      try {
        // this checks every edge.
        for (E edge : getFilteredEdges()) {
          Shape edgeShape = prepareFinalEdgeShape(vv.getRenderContext(), layoutModel, edge);
          if (edgeShape == null) {
            continue;
          }

          edgeShape = viewTransformer.transform(edgeShape);
          if (viewTransformer instanceof LensTransformer) {
            LensTransformer lensTransformer = (LensTransformer) viewTransformer;
            edgeShape = lensTransformer.getDelegate().transform(edgeShape);
          }

          Line2D endToEnd = getLineFromShape(edgeShape);
          // for articulated edges, the edge 'shape' is an area bounded by the zig-zag edge and the
          // (invisible) line from source to target vertex. The pick footprint is not inside the shape
          // and is not intersecting the invisible line, but does intersect the zig zag line
          if (!edgeShape.contains(pickFootprint)
              && edgeShape.intersects(pickFootprint)
              && !endToEnd.intersects(pickFootprint)) {
            closest = edge;
            break;
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return closest;
  }

  /**
   * Returns an edge whose shape intersects the 'pickArea' footprint of the passed x,y, coordinates.
   *
   * @param x the x coordinate of the location (layout coordinate system)
   * @param y the y coordinate of the location (layout coordinate system)
   * @return an edge whose shape intersects the pick area centered on the location {@code (x,y)}
   */
  @Override
  public E getEdge(LayoutModel<V> layoutModel, double x, double y) {

    MultiLayerTransformer mlt = vv.getRenderContext().getMultiLayerTransformer();

    // x and y are in layout coordinates. Translate to view and make a footprint
    Point2D layoutPoint = new Point2D.Double(x, y);
    Point2D viewPoint = mlt.transform(layoutPoint);
    Rectangle2D pickFootprint =
        new Rectangle2D.Double(
            viewPoint.getX() - pickSize / 2, viewPoint.getY() - pickSize / 2, pickSize, pickSize);

    return getEdge(layoutModel, pickFootprint);
  }

  @Override
  public E getEdge(LayoutModel<V> layoutModel, Point p) {
    return getEdge(layoutModel, p.x, p.y);
  }

  /**
   * uses the spatialRTree to find the closest edge to the point coords
   *
   * @param spatial
   * @param layoutModel
   * @param x in the layout coordinate system
   * @param y in the layout coordinate system
   * @return the selected vertex
   */
  protected E getEdge(
      SpatialRTree.Edges<E, V> spatial, LayoutModel<V> layoutModel, double x, double y) {

    // find the leaf vertices that would contain a point at x,y
    Collection<LeafNode<E>> containingLeafs = spatial.getContainingLeafs(new Point2D.Double(x, y));
    if (log.isTraceEnabled()) {
      log.trace("leaf for {},{} is {}", x, y, containingLeafs);
    }
    if (containingLeafs == null || containingLeafs.size() == 0) return null;
    // make a target circle the same size as the leaf vertex area union
    // leaf vertices are small when vertices are close and large when they are sparse
    // union up all the leafs then make a target
    Rectangle2D union = null;
    for (LeafNode<E> r : containingLeafs) {
      if (union == null) {
        union = r.getBounds();
      } else {
        union = union.createUnion(r.getBounds());
      }
    }
    double width = union.getWidth();
    double height = union.getHeight();
    double radiusx = width / 2;
    double radiusy = height / 2;
    Ellipse2D target = new Ellipse2D.Double(x - radiusx, y - radiusy, width, height);
    if (log.isTraceEnabled()) {
      log.trace("target is {}", target);
    }

    // will be the selected edge
    E closest = null;

    // get the all vertices from any leafs that intersect the target
    Collection<E> edges = spatial.getVisibleElements(target);
    if (log.isTraceEnabled()) {
      log.trace(
          "instead of checking all {} edges: {}", getFilteredEdges().size(), getFilteredEdges());
      log.trace("out of these {} candidates: {}...", edges.size(), edges);
    }

    Rectangle2D pickArea =
        new Rectangle2D.Float(
            (float) x - pickSize / 2, (float) y - pickSize / 2, pickSize, pickSize);

    // Check the (smaller) set of eligible edges
    // to return the one that contains the (x,y)
    for (E edge : edges) {

      Shape edgeShape = getTransformedEdgeShape(edge);
      if (edgeShape == null) {
        continue;
      }
      Line2D endToEnd = getLineFromShape(edgeShape);
      // for articulated edges, the edge 'shape' is an area bounded by the zig-zag edge and the
      // (invisible) line from source to target vertex. The pick footprint is not inside the shape
      // and is not intersecting the invisible line, but does intersect the zig zag line
      if (!edgeShape.contains(pickArea)
          && edgeShape.intersects(pickArea)
          && !endToEnd.intersects(pickArea)) {
        closest = edge;
        break;
      }
    }
    return closest;
  }

  /**
   * for articulated edges, I want the line from source to target vertex for any other shape, return
   * a 0 length line
   *
   * @param shape
   * @return
   */
  private Line2D getLineFromShape(Shape shape) {
    float[] coords = new float[6];
    float startx = 0;
    float starty = 0;
    float endx = 0;
    float endy = 0;
    int segmentCount = 0;
    PathIterator pathIterator = shape.getPathIterator(new AffineTransform());
    while (!pathIterator.isDone()) {
      switch (pathIterator.currentSegment(coords)) {
        case PathIterator.SEG_MOVETO:
          startx = coords[0];
          starty = coords[1];
          break;
        case PathIterator.SEG_LINETO:
          segmentCount++;
          endx = coords[0];
          endy = coords[1];
          break;
        case PathIterator.SEG_QUADTO:
          segmentCount += 2;
          endx = coords[2];
          endy = coords[3];
          break;
        case PathIterator.SEG_CUBICTO:
          segmentCount += 2;
          endx = coords[4];
          endy = coords[5];
          break;
        case PathIterator.SEG_CLOSE:
          break;
      }
      pathIterator.next();
    }
    if (segmentCount > 1) {
      return new Line2D.Float(startx, starty, endx, endy);
    } else {
      return new Line2D.Float();
    }
  }

  /**
   * Retrieves the shape template for <code>e</code> and transforms it according to the positions of
   * its endpoints in <code>layout</code>.
   *
   * @param e the edge whose shape is to be returned
   * @return the transformed shape
   */
  private Shape getTransformedEdgeShape(E e) {
    V v1 = vv.getVisualizationModel().getGraph().getEdgeSource(e);
    V v2 = vv.getVisualizationModel().getGraph().getEdgeTarget(e);
    boolean isLoop = v1.equals(v2);
    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
    org.jungrapht.visualization.layout.model.Point p1 = layoutModel.apply(v1);
    Point p2 = layoutModel.apply(v2);
    if (p1 == null || p2 == null) {
      return null;
    }
    float x1 = (float) p1.x;
    float y1 = (float) p1.y;
    float x2 = (float) p2.x;
    float y2 = (float) p2.y;

    // translate the edge to the starting vertex
    AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

    Shape edgeShape =
        vv.getRenderContext()
            .getEdgeShapeFunction()
            .apply(vv.getVisualizationModel().getGraph(), e);
    if (isLoop) {
      // make the loops proportional to the layoutSize of the vertex
      Shape s2 = vv.getRenderContext().getVertexShapeFunction().apply(v2);
      Rectangle2D s2Bounds = s2.getBounds2D();
      xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
      // move the loop so that the nadir is centered in the vertex
      xform.translate(0, -edgeShape.getBounds2D().getHeight() / 2);
    } else {
      float dx = x2 - x1;
      float dy = y2 - y1;
      // rotate the edge to the angle between the vertices
      double theta = Math.atan2(dy, dx);
      xform.rotate(theta);
      // stretch the edge to span the distance between the vertices
      float dist = (float) Math.sqrt(dx * dx + dy * dy);
      if (edgeShape instanceof ExpandXY) {
        xform.scale(dist, dist);
      } else {
        xform.scale(dist, 1.0);
      }
    }

    // transform the edge to its location and dimensions
    edgeShape = xform.createTransformedShape(edgeShape);
    return edgeShape;
  }

  protected Collection<V> getFilteredVertices() {
    Set<V> vertices = vv.getVisualizationModel().getGraph().vertexSet();
    return verticesAreFiltered()
        ? vertices
            .stream()
            .filter(vv.getRenderContext().getVertexIncludePredicate()::test)
            .collect(Collectors.toSet())
        //            Sets.filter(vertices, vv.getRenderContext().getVertexIncludePredicate()::test)
        : vertices;
  }

  protected Collection<E> getFilteredEdges() {
    Set<E> edges = vv.getVisualizationModel().getGraph().edgeSet();
    return edgesAreFiltered()
        ? edges
            .stream()
            .filter(vv.getRenderContext().getEdgeIncludePredicate()::test)
            .collect(Collectors.toSet())
        : edges;
  }

  /**
   * Quick test to allow optimization of <code>getFilteredVertices()</code>.
   *
   * @return <code>true</code> if there is an relaxing vertex filtering mechanism for this
   *     visualization, <code>false</code> otherwise
   */
  protected boolean verticesAreFiltered() {
    Predicate<V> vertexIncludePredicate = vv.getRenderContext().getVertexIncludePredicate();
    return vertexIncludePredicate != null
        && !vertexIncludePredicate.equals((Predicate<V>) (n -> true));
  }

  /**
   * Quick test to allow optimization of <code>getFilteredEdges()</code>.
   *
   * @return <code>true</code> if there is an relaxing edge filtering mechanism for this
   *     visualization, <code>false</code> otherwise
   */
  protected boolean edgesAreFiltered() {
    Predicate<E> edgeIncludePredicate = vv.getRenderContext().getEdgeIncludePredicate();
    return edgeIncludePredicate != null && !edgeIncludePredicate.equals((Predicate<V>) (n -> true));
  }

  /**
   * Returns <code>true</code> if this vertex in this graph is included in the collections of
   * elements to be rendered, and <code>false</code> otherwise.
   *
   * @return <code>true</code> if this vertex is included in the collections of elements to be
   *     rendered, <code>false</code> otherwise.
   */
  protected boolean isVertexRendered(V vertex) {
    Predicate<V> vertexIncludePredicate = vv.getRenderContext().getVertexIncludePredicate();
    return vertexIncludePredicate == null || vertexIncludePredicate.test(vertex);
  }

  /**
   * Returns <code>true</code> if this edge and its endpoints in this graph are all included in the
   * collections of elements to be rendered, and <code>false</code> otherwise.
   *
   * @return <code>true</code> if this edge and its endpoints are all included in the collections of
   *     elements to be rendered, <code>false</code> otherwise.
   */
  protected boolean isEdgeRendered(E edge) {
    Predicate<V> vertexIncludePredicate = vv.getRenderContext().getVertexIncludePredicate();
    Predicate<E> edgeIncludePredicate = vv.getRenderContext().getEdgeIncludePredicate();
    Graph<V, E> g = vv.getVisualizationModel().getGraph();
    if (edgeIncludePredicate != null && !edgeIncludePredicate.test(edge)) {
      return false;
    }
    V v1 = g.getEdgeSource(edge);
    V v2 = g.getEdgeTarget(edge);

    return vertexIncludePredicate == null
        || (vertexIncludePredicate.test(v1) && vertexIncludePredicate.test(v2));
  }

  /**
   * Returns the layoutSize of the edge picking area. The picking area is square; the layoutSize is
   * specified as the length of one side, in view coordinates.
   *
   * @return the layoutSize of the edge picking area
   */
  public float getPickSize() {
    return pickSize;
  }

  /**
   * Sets the layoutSize of the edge picking area.
   *
   * @param pickSize the length of one side of the (square) picking area, in view coordinates
   */
  public void setPickSize(int pickSize) {
    this.pickSize = pickSize;
  }

  class FootprintPaintable implements VisualizationServer.Paintable {
    Shape footPrint;
    Color color;

    public FootprintPaintable(Color color, Shape footPrint) {
      this.color = color;
      this.footPrint = footPrint;
    }

    public void paint(Graphics g) {
      Color oldColor = g.getColor();
      g.setColor(color);
      ((Graphics2D) g).draw(footPrint);
      g.setColor(oldColor);
    }

    public boolean useTransform() {
      return false;
    }
  }

  protected Shape prepareFinalEdgeShape(
      RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e) {
    V source = layoutModel.getGraph().getEdgeSource(e);
    V target = layoutModel.getGraph().getEdgeTarget(e);

    Point sourcePoint = layoutModel.apply(source);
    Point targetPoint = layoutModel.apply(target);
    Point2D sourcePoint2D =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, AWT.convert(sourcePoint));
    Point2D targetPoint2D =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, AWT.convert(targetPoint));
    float sourcePoint2DX = (float) sourcePoint2D.getX();
    float sourcePoint2DY = (float) sourcePoint2D.getY();
    float targetPoint2DX = (float) targetPoint2D.getX();
    float targetPoint2DY = (float) targetPoint2D.getY();

    boolean isLoop = source.equals(target);
    Shape targetShape = renderContext.getVertexShapeFunction().apply(target);
    Shape edgeShape = vv.getRenderContext().getEdgeShapeFunction().apply(layoutModel.getGraph(), e);

    AffineTransform xform = AffineTransform.getTranslateInstance(sourcePoint2DX, sourcePoint2DY);

    if (isLoop) {
      // this is a self-loop. scale it is larger than the vertex
      // it decorates and translate it so that its nadir is
      // at the center of the vertex.
      Rectangle2D targetShapeBounds2D = targetShape.getBounds2D();
      xform.scale(targetShapeBounds2D.getWidth(), targetShapeBounds2D.getHeight());
      xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
    } else {
      // this is a normal edge. Rotate it to the angle between
      // vertex endpoints, then scale it to the distance between
      // the vertices
      float dx = targetPoint2DX - sourcePoint2DX;
      float dy = targetPoint2DY - sourcePoint2DY;
      float thetaRadians = (float) Math.atan2(dy, dx);
      xform.rotate(thetaRadians);
      double dist = Math.sqrt(dx * dx + dy * dy);
      if (edgeShape instanceof ExpandXY) {
        xform.scale(dist, dist);
      } else {
        xform.scale(dist, 1.0);
      }
    }
    edgeShape = xform.createTransformedShape(edgeShape);

    return edgeShape;
  }
}
