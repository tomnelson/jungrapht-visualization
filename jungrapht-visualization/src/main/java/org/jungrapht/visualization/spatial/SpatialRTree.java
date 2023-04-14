package org.jungrapht.visualization.spatial;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.control.GraphElementAccessor;
import org.jungrapht.visualization.layout.event.LayoutVertexPositionChange;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.spatial.rtree.LeafNode;
import org.jungrapht.visualization.spatial.rtree.Node;
import org.jungrapht.visualization.spatial.rtree.RTree;
import org.jungrapht.visualization.spatial.rtree.SplitterContext;
import org.jungrapht.visualization.util.BoundingRectangleCollector;
import org.jungrapht.visualization.util.RadiusGraphElementAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T> The Type for the elements managed by the RTree, Vertices or Edges
 * @param <NT> The Type for the Vertices of the graph. May be the same as T
 * @author Tom Nelson
 */
public abstract class SpatialRTree<T, NT> extends AbstractSpatial<T, NT> implements Spatial<T, NT> {

  private static final Logger log = LoggerFactory.getLogger(SpatialRTree.class);

  public abstract static class Builder<T, NT> {
    protected LayoutModel<NT> layoutModel;
    protected BoundingRectangleCollector<T> boundingRectangleCollector;
    protected SplitterContext<T> splitterContext;
    protected boolean reinsert;

    public Builder<T, NT> boundingRectangleCollector(
        BoundingRectangleCollector<T> boundingRectangleCollector) {
      this.boundingRectangleCollector = boundingRectangleCollector;
      return this;
    }

    public Builder<T, NT> layoutModel(LayoutModel<NT> layoutModel) {
      this.layoutModel = layoutModel;
      return this;
    }

    public Builder<T, NT> splitterContext(SplitterContext<T> splitterContext) {
      this.splitterContext = splitterContext;
      return this;
    }

    public Builder<T, NT> reinsert(boolean reinsert) {
      this.reinsert = reinsert;
      return this;
    }

    public abstract SpatialRTree<T, NT> build();
  }

  /** a container for the splitter functions to use, quadratic or R*Tree */
  protected SplitterContext<T> splitterContext;

  /** the RTree to use. Add/Remove methods may change this to a new immutable RTree reference */
  protected RTree<T> rtree;

  /** gathers the bounding rectangles of the elements managed by the RTree. Node or Edge shapes */
  protected BoundingRectangleCollector<T> boundingRectangleCollector;

  protected boolean reinsert;

  /**
   * create a new instance with the a LayoutModel and a style of splitter to use
   *
   * @param layoutModel
   * @param splitterContext
   */
  protected SpatialRTree(
      LayoutModel<NT> layoutModel, SplitterContext<T> splitterContext, boolean reinsert) {
    super(layoutModel);
    this.splitterContext = splitterContext;
    this.reinsert = reinsert;
  }

  public boolean isReinsert() {
    return reinsert;
  }

  public void setReinsert(boolean reinsert) {
    this.reinsert = reinsert;
  }

  /**
   * gather the RTree nodes into a list for display as Paintables
   *
   * @param list
   * @param tree
   * @return
   */
  protected abstract List<Shape> collectGrids(List<Shape> list, RTree<T> tree);

  /** @return the 2 dimensional area of interest for this class */
  @Override
  public Rectangle2D getLayoutArea() {
    return rectangle;
  }

  /** @param bounds the new bounds for the data struture */
  @Override
  public void setBounds(Rectangle2D bounds) {
    this.rectangle = bounds;
  }

  @Override
  public List<Shape> getGrid() {
    if (gridCache == null) {
      if (log.isTraceEnabled()) {
        log.trace("getting Grid from tree size {}", rtree.count());
      }
      if (!isActive()) {
        // just return the entire layout area
        return Collections.singletonList(getLayoutArea());
      }
      List<Shape> areas = new ArrayList<>();

      gridCache = collectGrids(areas, rtree);
      if (log.isTraceEnabled()) {
        log.trace("getGrid got {} and {}", areas.size(), gridCache.size());
      }
      return gridCache;
    } else {
      return gridCache;
    }
  }

  @Override
  public void clear() {
    rtree = RTree.create();
  }

  /**
   * @param x a point to search in the spatial structure
   * @param y a point to search in the spatial structure
   * @return a collection of the RTree LeafVertices that would contain the passed point
   */
  @Override
  public Set<LeafNode<T>> getContainingLeafs(double x, double y) {
    if (!isActive() || rtree.getRoot().isEmpty()) {
      return Collections.emptySet();
    }

    Node<T> theRoot = rtree.getRoot().get();
    return theRoot.getContainingLeafs(new HashSet<>(), x, y);
  }

  @Override
  public Set<LeafNode<T>> getContainingLeafs(Point2D p) {
    return getContainingLeafs(p.getX(), p.getY());
  }

  @Override
  public LeafNode<T> getContainingLeaf(Object element) {
    if (rtree.getRoot().isEmpty()) {
      return null; // nothing in this tree
    }
    Node<T> theRoot = rtree.getRoot().get();
    return theRoot.getContainingLeaf((T) element);
  }

  protected void recalculate(Collection<T> elements) {
    if (!SwingUtilities.isEventDispatchThread()) {
      log.warn("NOT AWT Thread");
    }
    try {
      log.trace("start recalculate");
      clear();
      if (boundingRectangleCollector != null) {
        for (T element : elements) {
          rtree =
              RTree.add(
                  rtree,
                  splitterContext,
                  element,
                  boundingRectangleCollector.getForElement(element));
          if (log.isTraceEnabled()) {
            log.trace("added {} got {} nodes in {}", element, rtree.count(), rtree);
          }
        }
        if (reinsert) {
          if (log.isTraceEnabled()) {
            log.trace("before reinsert node count: {}", rtree.count());
          }

          rtree = RTree.reinsert(rtree, splitterContext);
          if (log.isTraceEnabled()) {
            log.trace("after reinsert node count: {}", rtree.count());
          }
        }
      } else {
        log.trace("got no rectangles");
      }
      log.trace("end recalculate");

    } catch (Exception ex) {
      log.debug("unstable RTree got exception: {}", ex);
    }
  }

  protected void bulkInsert(Collection<T> elements) {
    log.trace("start bulk insert");
    clear();
    if (boundingRectangleCollector != null) {
      List<Map.Entry<T, Rectangle2D>> entryList = new ArrayList<>();

      for (T element : elements) {
        entryList.add(
            new AbstractMap.SimpleEntry(
                element, boundingRectangleCollector.getForElement(element)));
      }
      rtree = RTree.bulkAdd(rtree, splitterContext, entryList);

    } else {
      log.trace("got no rectangles");
    }
    log.trace("end recalculate");
  }

  public static class Vertices<V> extends SpatialRTree<V, V>
      implements Spatial<V, V>, LayoutVertexPositionChange.Listener<V> {

    private static final Logger log = LoggerFactory.getLogger(Vertices.class);

    public static class Builder<V> extends SpatialRTree.Builder<V, V> {

      public Vertices<V> build() {
        return new Vertices(this);
      }
    }

    public static Builder builder() {
      return new Vertices.Builder<>();
    }

    Vertices(Builder<V> builder) {
      this(
          builder.layoutModel,
          builder.boundingRectangleCollector,
          builder.splitterContext,
          builder.reinsert);
    }

    Vertices(
        LayoutModel<V> layoutModel,
        BoundingRectangleCollector<V> boundingRectangleCollector,
        SplitterContext<V> splitterContext,
        boolean reinsert) {
      super(layoutModel, splitterContext, reinsert);
      this.boundingRectangleCollector = boundingRectangleCollector;
      rtree = RTree.create();
    }

    /**
     * @param shape the possibly non-rectangular area of interest
     * @return all nodes that are contained within the passed Shape
     */
    @Override
    public Set<V> getVisibleElements(Shape shape) {
      if (!isActive() || rtree.getRoot().isEmpty()) {
        return layoutModel.getGraph().vertexSet();
      }
      pickShapes.add(shape);

      Node<V> root = rtree.getRoot().get();
      if (log.isTraceEnabled()) {
        log.trace("out of nodes {}", layoutModel.getGraph().vertexSet());
      }
      Set<V> visibleElements = new HashSet<>();
      return root.getVisibleElements(visibleElements, shape);
    }

    /**
     * update the position of the passed node
     *
     * @param element the element to update in the structure
     * @param location the new location for the element
     */
    @Override
    public void update(V element, org.jungrapht.visualization.layout.model.Point location) {
      try {
        gridCache = null;
        // do nothing if we are not active
        if (isActive() && rtree.getRoot().isPresent()) {
          //        TreeNode root = rtree.getRoot().get();

          LeafNode<V> containingLeaf = getContainingLeaf(element);
          Rectangle2D itsShape = boundingRectangleCollector.getForElement(element, location);
          // if the shape does not enlarge the containingRTree, then only update what is in elements
          // otherwise, remove this node and re-insert it
          if (containingLeaf != null) {
            if (containingLeaf.getBounds().contains(itsShape)) {
              // the element did not move out of its LeafVertex
              // no RTree update required, just re-add the element to the map
              // with the new Bounds value
              // remove it from the map (so there is no overflow when it is added
              containingLeaf.remove(element);
              containingLeaf.add(splitterContext, element, itsShape);
            } else {
              // the element is outside of the previous containing LeafVertex
              // remmove the element from the tree and add it again so it will
              // go to the correct LeafVertex
              rtree = RTree.remove(rtree, element);
              rtree = RTree.add(rtree, splitterContext, element, itsShape);
            }
          } else {
            // the element is new to the RTree
            // just add it
            rtree = RTree.add(rtree, splitterContext, element, itsShape);
          }
        }
      } catch (ConcurrentModificationException cme) {
        log.debug("ignoring CME");
      }
    }

    @Override
    public V getClosestElement(Point2D p) {
      return getClosestElement(p.getX(), p.getY());
    }

    /**
     * get the node that is closest to the passed (x,y)
     *
     * @param x
     * @param y
     * @return the node closest to x,y
     */
    @Override
    public V getClosestElement(double x, double y) {
      if (!isActive() || rtree.getRoot().isEmpty()) {
        // use the fallback VertexAccessor
        return fallback.getVertex(layoutModel, x, y);
      }

      double radius = layoutModel.getWidth() / 20;

      V closest = null;
      while (closest == null) {

        double diameter = radius * 2;

        Ellipse2D searchArea = new Ellipse2D.Double(x - radius, y - radius, diameter, diameter);

        Collection<V> nodes = getVisibleElements(searchArea);
        closest = getClosest(nodes, x, y, radius);

        // if i found a winner or
        // if I have already considered all of the nodes in the graph
        // (in the spatialtree) there is no reason to enlarge the
        // area and try again
        if (closest != null || nodes.size() >= layoutModel.getGraph().vertexSet().size()) {
          break;
        }
        // double the search area size and try again
        radius *= 2;
      }
      return closest;
    }

    @Override
    public V getClosestElement(V v) {
      if (!isActive() || rtree.getRoot().isEmpty()) {
        // use the fallback VertexAccessor
        return fallback.getClosestVertex(layoutModel, v);
      }

      Point vp = layoutModel.get(v);
      double radius = layoutModel.getWidth() / 20;

      V closest = null;
      while (closest == null) {

        double diameter = radius * 2;

        Ellipse2D searchArea =
            new Ellipse2D.Double(vp.x - radius, vp.y - radius, diameter, diameter);

        Collection<V> nodes =
            getVisibleElements(searchArea)
                .stream()
                .filter(vertex -> vertex != v)
                .collect(Collectors.toSet());
        closest = getClosest(nodes, vp.x, vp.y, radius);

        // if i found a winner or
        // if I have already considered all of the nodes in the graph
        // (in the spatialtree) there is no reason to enlarge the
        // area and try again
        if (closest != null || nodes.size() >= layoutModel.getGraph().vertexSet().size()) {
          break;
        }
        // double the search area size and try again
        radius *= 2;
      }
      return closest;
    }

    protected List<Shape> collectGrids(List<Shape> list, RTree<V> tree) {
      if (tree.getRoot().isPresent()) {
        Node<V> root = tree.getRoot().get();
        root.collectGrids(list);
      }
      return list;
    }

    /**
     * rebuild the data structure // * // * @param elements the elements to insert into the data
     * structure
     */
    @Override
    public void recalculate() {
      try {
        gridCache = null;
        log.trace(
            "called recalculate while active:{} layout model relaxing:{}",
            isActive(),
            layoutModel.isRelaxing());
        if (isActive()) {
          if (log.isTraceEnabled()) {
            log.trace("recalculate for nodes: {}", layoutModel.getGraph().vertexSet());
          }
          recalculate(layoutModel.getGraph().vertexSet());
          //        bulkInsert(layoutModel.getGraph().vertexSet());
        } else {
          log.trace("no recalculate when active: {}", isActive());
        }
      } catch (ConcurrentModificationException ex) {
        recalculate();
      }
      log.trace("recalculate tries");
    }

    @Override
    public void layoutVertexPositionChanged(LayoutVertexPositionChange.GraphEvent<V> evt) {
      update(evt.vertex, evt.location);
    }

    @Override
    public void layoutVertexPositionChanged(LayoutVertexPositionChange.Event<V> evt) {
      update(evt.vertex, evt.location);
    }
  }

  public static class Edges<E, V> extends SpatialRTree<E, V>
      implements Spatial<E, V>, LayoutVertexPositionChange.Listener<V> {

    private static final Logger log = LoggerFactory.getLogger(Edges.class);

    public static class Builder<E, V> extends SpatialRTree.Builder<E, V> {

      public Builder<E, V> layoutModel(LayoutModel<V> layoutModel) {
        this.layoutModel = layoutModel;
        return this;
      }

      public Edges<E, V> build() {
        return new Edges(this);
      }
    }

    public static Builder builder() {
      return new Builder<>();
    }

    Edges(Builder<E, V> builder) {
      this(
          builder.layoutModel,
          builder.boundingRectangleCollector,
          builder.splitterContext,
          builder.reinsert);
    }

    GraphElementAccessor<V, E> graphElementAccessor;

    // Edges gets a VisualizationModel reference to access the Graph and work with edges
    //    VisualizationModel<V, E> visualizationModel;

    Edges(
        LayoutModel<V> layoutModel,
        BoundingRectangleCollector<E> boundingRectangleCollector,
        SplitterContext<E> splitterContext,
        boolean reinsert) {
      super(layoutModel, splitterContext, reinsert);
      //      this.layoutModel = layoutModel;
      this.boundingRectangleCollector = boundingRectangleCollector;
      graphElementAccessor = new RadiusGraphElementAccessor();
      rtree = RTree.create();
      recalculate();
    }

    /**
     * @param shape the possibly non-rectangular area of interest
     * @return all nodes that are contained within the passed Shape
     */
    @Override
    public Set<E> getVisibleElements(Shape shape) {
      if (!isActive() || rtree.getRoot().isEmpty()) {
        log.trace("not relaxing so getting from the graph");
        return (Set<E>) layoutModel.getGraph().edgeSet();
      }
      pickShapes.add(shape);
      Node<E> root = rtree.getRoot().get();
      Set<E> visibleElements = new HashSet<>();
      return root.getVisibleElements(visibleElements, shape);
    }

    /**
     * update the position of the passed node
     *
     * @param element the element to update in the structure
     * @param location the new location for the element
     */
    @Override
    public void update(E element, org.jungrapht.visualization.layout.model.Point location) {
      try {
        gridCache = null;
        if (isActive()) {

          // get the endpoints for this edge
          // there should be 2
          V n1 = layoutModel.getGraph().getEdgeSource(element);
          V n2 = layoutModel.getGraph().getEdgeTarget(element);

          if (n2 == null) {
            n2 = n1;
          }
          if (n1 != null && n2 != null) {
            Rectangle2D itsShape =
                boundingRectangleCollector.getForElement(
                    element, layoutModel.apply(n1), layoutModel.apply(n2));
            LeafNode<E> containingLeaf = getContainingLeaf(element);
            // if the shape does not enlarge the containingRTree, then only update what is in elements map
            // otherwise, remove this node and re-insert it
            if (containingLeaf != null) {
              if (containingLeaf.getBounds().contains(itsShape)) {
                containingLeaf.remove(element);
                containingLeaf.add(splitterContext, element, itsShape);
                log.trace("{} changed in place", element);
              } else {
                containingLeaf.remove(element);
                if (log.isTraceEnabled()) {
                  log.trace("rtree now size {}", rtree.count());
                }
                rtree = RTree.add(rtree, splitterContext, element, itsShape);
                if (log.isTraceEnabled()) {
                  log.trace(
                      "added back {} with {} into rtree size {}", element, itsShape, rtree.count());
                }
              }
            } else {
              rtree = RTree.add(rtree, splitterContext, element, itsShape);
            }
          }
        }
      } catch (ConcurrentModificationException cme) {
        log.debug("ignoring CME");
      }
    }

    @Override
    public void layoutVertexPositionChanged(LayoutVertexPositionChange.Event<V> evt) {
      V vertex = evt.vertex;
      org.jungrapht.visualization.layout.model.Point p = evt.location;
      if (layoutModel.getGraph().containsVertex(vertex)) {
        Graph<V, E> graph = layoutModel.getGraph();
        Set<E> edges = graph.edgesOf(vertex);

        for (E edge : edges) {
          update(edge, p);
        }
      }
    }

    @Override
    public void layoutVertexPositionChanged(LayoutVertexPositionChange.GraphEvent<V> evt) {
      V vertex = evt.vertex;
      org.jungrapht.visualization.layout.model.Point p = evt.location;
      if (layoutModel.getGraph().containsVertex(vertex)) {
        Graph<V, E> graph = layoutModel.getGraph();
        Set<E> edges = graph.edgesOf(vertex);
        for (E edge : edges) {
          update(edge, p);
        }
      }
    }

    /**
     * get the element that is closest to the passed point
     *
     * @param p a point to search in the spatial structure
     * @return the closest element
     */
    @Override
    public E getClosestElement(Point2D p) {
      return getClosestElement(p.getX(), p.getY());
    }

    /**
     * get the element that is closest to the passed (x,y)
     *
     * @param x coordinate to search for
     * @param y coordinate to search for
     * @return the element closest to x,y
     */
    @Override
    public E getClosestElement(double x, double y) {

      if (!isActive() || rtree.getRoot().isEmpty()) {
        // not active or empty
        // use the fallback VertexAccessor
        return graphElementAccessor.getEdge(layoutModel, x, y);
      }
      //      Node<E> root = rtree.getRoot().get();
      double radius = layoutModel.getWidth() / 20;

      E closest = null;
      while (closest == null) {

        double diameter = radius * 2;

        Ellipse2D searchArea = new Ellipse2D.Double(x - radius, y - radius, diameter, diameter);

        Collection<E> edges = getVisibleElements(searchArea);
        closest = getClosestEdge(edges, x, y, radius);

        // If i found a winner, break. also
        // if I have already considered all of the nodes in the graph
        // (in the spatialquadtree) there is no reason to enlarge the
        // area and try again
        if (closest != null || edges.size() >= layoutModel.getGraph().edgeSet().size()) {
          break;
        }
        // double the search area size and try again
        radius *= 2;
      }
      return closest;
    }

    @Override
    public E getClosestElement(E element) {
      // cop-out to just use the midpoint
      Graph<V, E> graph = layoutModel.getGraph();
      Point sp = layoutModel.get(graph.getEdgeSource(element));
      Point tp = layoutModel.get(graph.getEdgeSource(element));
      Point p = Point.centroidOf(sp, tp);
      if (!isActive() || rtree.getRoot().isEmpty()) {
        // not active or empty
        // use the fallback VertexAccessor
        return graphElementAccessor.getEdge(layoutModel, p);
      }
      //      Node<E> root = rtree.getRoot().get();
      double radius = layoutModel.getWidth() / 20;

      E closest = null;
      while (closest == null) {

        double diameter = radius * 2;

        Ellipse2D searchArea = new Ellipse2D.Double(p.x - radius, p.y - radius, diameter, diameter);

        Collection<E> edges = getVisibleElements(searchArea);
        closest = getClosestEdge(edges, p.x, p.y, radius);

        // If i found a winner, break. also
        // if I have already considered all of the nodes in the graph
        // (in the spatialquadtree) there is no reason to enlarge the
        // area and try again
        if (closest != null || edges.size() >= layoutModel.getGraph().edgeSet().size()) {
          break;
        }
        // double the search area size and try again
        radius *= 2;
      }
      return closest;
    }

    protected E getClosestEdge(Collection<E> edges, double x, double y, double radius) {

      // since I am comparing with distance squared, i need to square the radius
      double radiusSq = radius * radius;
      if (edges.size() > 0) {
        double closestSoFar = Double.MAX_VALUE;
        E winner = null;
        double winningDistance = -1;
        for (E edge : edges) {
          // get the 2 endpoints
          Graph<V, E> graph = layoutModel.getGraph();
          V u = graph.getEdgeSource(edge);
          V v = graph.getEdgeTarget(edge);
          org.jungrapht.visualization.layout.model.Point up = layoutModel.apply(u);
          Point vp = layoutModel.apply(v);
          // compute the distance between my point and a Line connecting u and v
          Line2D line = new Line2D.Double(up.x, up.y, vp.x, vp.y);
          double dist = line.ptSegDist(x, y);

          // consider only edges that cross inside the search radius
          // and are closer than previously found nodes
          if (dist < radiusSq && dist < closestSoFar) {
            closestSoFar = dist;
            winner = edge;
            winningDistance = dist;
          }
        }
        if (log.isTraceEnabled()) {
          log.trace("closest winner is {} at distance {}", winner, winningDistance);
        }
        return winner;
      } else {
        return null;
      }
    }

    protected List<Shape> collectGrids(List<Shape> list, RTree<E> tree) {
      if (tree.getRoot().isPresent()) {
        Node<E> root = tree.getRoot().get();
        root.collectGrids(list);
      }
      return list;
    }

    /** rebuild the data structure */
    @Override
    public void recalculate() {
      gridCache = null;
      log.trace(
          "called recalculate while active:{} layout model relaxing:{}",
          isActive(),
          layoutModel.isRelaxing());
      if (isActive()) {
        if (log.isTraceEnabled()) {
          log.trace("recalculate for edges: {}", layoutModel.getGraph().edgeSet());
        }
        Graph<V, E> graph = layoutModel.getGraph();
        recalculate(graph.edgeSet());
      }
    }
  }

  public String toString() {
    return rtree.toString();
  }
}
