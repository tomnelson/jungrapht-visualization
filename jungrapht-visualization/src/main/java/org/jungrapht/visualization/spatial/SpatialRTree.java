package org.jungrapht.visualization.spatial;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.BoundingRectangleCollector;
import org.jungrapht.visualization.layout.NetworkElementAccessor;
import org.jungrapht.visualization.layout.RadiusNetworkElementAccessor;
import org.jungrapht.visualization.layout.event.LayoutNodePositionChange;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.spatial.rtree.LeafNode;
import org.jungrapht.visualization.spatial.rtree.Node;
import org.jungrapht.visualization.spatial.rtree.RTree;
import org.jungrapht.visualization.spatial.rtree.SplitterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T> The Type for the elements managed by the RTree, Nodes or Edges
 * @param <NT> The Type for the Nodes of the graph. May be the same as T
 * @author Tom Nelson
 */
public abstract class SpatialRTree<T, NT> extends AbstractSpatial<T, NT> implements Spatial<T> {

  private static final Logger log = LoggerFactory.getLogger(SpatialRTree.class);

  public abstract static class Builder<T, NT> {
    protected VisualizationModel visualizationModel;
    protected LayoutModel<NT> layoutModel;
    protected BoundingRectangleCollector<T> boundingRectangleCollector;
    protected SplitterContext<T> splitterContext;
    protected boolean reinsert;

    public Builder<T, NT> boundingRectangleCollector(
        BoundingRectangleCollector<T> boundingRectangleCollector) {
      this.boundingRectangleCollector = boundingRectangleCollector;
      return this;
    }

    public Builder<T, NT> visualizationModel(VisualizationModel visualizationModel) {
      this.visualizationModel = visualizationModel;
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
      log.trace("getting Grid from tree size {}", rtree.count());
      if (!isActive()) {
        // just return the entire layout area
        return Collections.singletonList(getLayoutArea());
      }
      List<Shape> areas = Lists.newArrayList();

      gridCache = collectGrids(areas, rtree);
      log.trace("getGrid got {} and {}", areas.size(), gridCache.size());
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
   * @return a collection of the RTree LeafNodes that would contain the passed point
   */
  @Override
  public Set<LeafNode<T>> getContainingLeafs(double x, double y) {
    if (!isActive() || !rtree.getRoot().isPresent()) {
      return Collections.emptySet();
    }

    Node<T> theRoot = rtree.getRoot().get();
    return theRoot.getContainingLeafs(Sets.newHashSet(), x, y);
  }

  @Override
  public Set<LeafNode<T>> getContainingLeafs(Point2D p) {
    return getContainingLeafs(p.getX(), p.getY());
  }

  @Override
  public LeafNode<T> getContainingLeaf(Object element) {
    if (!rtree.getRoot().isPresent()) {
      return null; // nothing in this tree
    }
    Node<T> theRoot = rtree.getRoot().get();
    return theRoot.getContainingLeaf((T) element);
  }

  protected void recalculate(Collection<T> elements) {
    log.trace("start recalculate");
    clear();
    if (boundingRectangleCollector != null) {
      for (T element : elements) {
        rtree =
            RTree.add(
                rtree, splitterContext, element, boundingRectangleCollector.getForElement(element));
        log.trace("added {} got {} nodes in {}", element, rtree.count(), rtree);
      }
      if (reinsert) {
        log.trace("before reinsert node count: {}, tree: {}", rtree.count()); //, rtree);

        rtree = RTree.reinsert(rtree, splitterContext);
        log.trace("after reinsert node count: {}, tree: {}", rtree.count()); //, rtree);
      }
    } else {
      log.trace("got no rectangles");
    }
    log.trace("end recalculate");
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

  public static class Nodes<N> extends SpatialRTree<N, N>
      implements Spatial<N>, LayoutNodePositionChange.Listener<N> {

    private static final Logger log = LoggerFactory.getLogger(Nodes.class);

    public static class Builder<N> extends SpatialRTree.Builder<N, N> {

      public Nodes<N> build() {
        return new Nodes(this);
      }
    }

    public static Builder builder() {
      return new Nodes.Builder<>();
    }

    Nodes(Builder<N> builder) {
      this(
          builder.visualizationModel,
          builder.boundingRectangleCollector,
          builder.splitterContext,
          builder.reinsert);
    }

    Nodes(
        VisualizationModel visualizationModel,
        BoundingRectangleCollector<N> boundingRectangleCollector,
        SplitterContext<N> splitterContext,
        boolean reinsert) {
      super(visualizationModel.getLayoutModel(), splitterContext, reinsert);
      this.boundingRectangleCollector = boundingRectangleCollector;
      rtree = RTree.create();
    }

    /**
     * @param shape the possibly non-rectangular area of interest
     * @return all nodes that are contained within the passed Shape
     */
    @Override
    public Set<N> getVisibleElements(Shape shape) {
      if (!isActive() || !rtree.getRoot().isPresent()) {
        return layoutModel.getGraph().vertexSet();
      }
      pickShapes.add(shape);

      Node<N> root = rtree.getRoot().get();
      log.trace("out of nodes {}", layoutModel.getGraph().vertexSet());
      Set<N> visibleElements = Sets.newHashSet();
      return root.getVisibleElements(visibleElements, shape);
    }

    /**
     * update the position of the passed node
     *
     * @param element the element to update in the structure
     * @param location the new location for the element
     */
    @Override
    public void update(N element, org.jungrapht.visualization.layout.model.Point location) {
      gridCache = null;
      // do nothing if we are not active
      if (isActive() && rtree.getRoot().isPresent()) {
        //        TreeNode root = rtree.getRoot().get();

        LeafNode<N> containingLeaf = getContainingLeaf(element);
        Rectangle2D itsShape = boundingRectangleCollector.getForElement(element, location);
        // if the shape does not enlarge the containingRTree, then only update what is in elements
        // otherwise, remove this node and re-insert it
        if (containingLeaf != null) {
          if (containingLeaf.getBounds().contains(itsShape)) {
            // the element did not move out of its LeafNode
            // no RTree update required, just re-add the element to the map
            // with the new Bounds value
            // remove it from the map (so there is no overflow when it is added
            containingLeaf.remove(element);
            containingLeaf.add(splitterContext, element, itsShape);
          } else {
            // the element is outside of the previous containing LeafNode
            // remmove the element from the tree and add it again so it will
            // go to the correct LeafNode
            rtree = RTree.remove(rtree, element);
            rtree = RTree.add(rtree, splitterContext, element, itsShape);
          }
        } else {
          // the element is new to the RTree
          // just add it
          rtree = RTree.add(rtree, splitterContext, element, itsShape);
        }
      }
    }

    @Override
    public N getClosestElement(Point2D p) {
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
    public N getClosestElement(double x, double y) {
      if (!isActive() || !rtree.getRoot().isPresent()) {
        // use the fallback NetworkNodeAccessor
        return fallback.getNode(layoutModel, x, y);
      }

      double radius = layoutModel.getWidth() / 20;

      N closest = null;
      while (closest == null) {

        double diameter = radius * 2;

        Ellipse2D searchArea = new Ellipse2D.Double(x - radius, y - radius, diameter, diameter);

        Collection<N> nodes = getVisibleElements(searchArea);
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

    protected List<Shape> collectGrids(List<Shape> list, RTree<N> tree) {
      if (tree.getRoot().isPresent()) {
        Node<N> root = tree.getRoot().get();
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
      gridCache = null;
      log.trace(
          "called recalculate while active:{} layout model relaxing:{}",
          isActive(),
          layoutModel.isRelaxing());
      if (isActive()) {
        log.trace("recalculate for nodes: {}", layoutModel.getGraph().vertexSet());
        recalculate(layoutModel.getGraph().vertexSet());
        //        bulkInsert(layoutModel.getGraph().vertexSet());
      } else {
        log.trace("no recalculate when active: {}", isActive());
      }
    }

    @Override
    public void layoutNodePositionChanged(LayoutNodePositionChange.NetworkEvent<N> evt) {
      update(evt.node, evt.location);
    }

    @Override
    public void layoutNodePositionChanged(LayoutNodePositionChange.Event<N> evt) {
      update(evt.node, evt.location);
    }
  }

  public static class Edges<E, N> extends SpatialRTree<E, N>
      implements Spatial<E>, LayoutNodePositionChange.Listener<N> {

    private static final Logger log = LoggerFactory.getLogger(Edges.class);

    public static class Builder<E, N> extends SpatialRTree.Builder<E, N> {

      public Builder<E, N> visualizationModel(VisualizationModel visualizationModel) {
        this.visualizationModel = visualizationModel;
        return this;
      }

      public Edges<E, N> build() {
        return new Edges(this);
      }
    }

    public static Builder builder() {
      return new Builder<>();
    }

    Edges(Builder<E, N> builder) {
      this(
          builder.visualizationModel,
          builder.boundingRectangleCollector,
          builder.splitterContext,
          builder.reinsert);
    }

    NetworkElementAccessor<N, E> networkElementAccessor;

    // Edges gets a VisualizationModel reference to access the Network and work with edges
    VisualizationModel<N, E> visualizationModel;

    Edges(
        VisualizationModel<N, E> visualizationModel,
        BoundingRectangleCollector<E> boundingRectangleCollector,
        SplitterContext<E> splitterContext,
        boolean reinsert) {
      super(visualizationModel.getLayoutModel(), splitterContext, reinsert);
      this.visualizationModel = visualizationModel;
      this.boundingRectangleCollector = boundingRectangleCollector;
      networkElementAccessor = new RadiusNetworkElementAccessor(visualizationModel.getNetwork());
      rtree = RTree.create();
      recalculate();
    }

    /**
     * @param shape the possibly non-rectangular area of interest
     * @return all nodes that are contained within the passed Shape
     */
    @Override
    public Set<E> getVisibleElements(Shape shape) {
      if (!isActive() || !rtree.getRoot().isPresent()) {
        log.trace("not relaxing so getting from the network");
        return visualizationModel.getNetwork().edgeSet();
      }
      pickShapes.add(shape);
      Node<E> root = rtree.getRoot().get();
      Set<E> visibleElements = Sets.newHashSet();
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
      gridCache = null;
      if (isActive()) {

        // get the endpoints for this edge
        // there should be 2
        N n1 = visualizationModel.getNetwork().getEdgeSource(element);
        N n2 = visualizationModel.getNetwork().getEdgeTarget(element);

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
              log.trace("rtree now size {}", rtree.count());
              rtree = RTree.add(rtree, splitterContext, element, itsShape);
              log.trace(
                  "added back {} with {} into rtree size {}", element, itsShape, rtree.count());
            }
          } else {
            rtree = RTree.add(rtree, splitterContext, element, itsShape);
          }
        }
      }
    }

    @Override
    public void layoutNodePositionChanged(LayoutNodePositionChange.Event<N> evt) {
      N node = evt.node;
      org.jungrapht.visualization.layout.model.Point p = evt.location;
      if (visualizationModel.getNetwork().vertexSet().contains(node)) {
        Set<E> edges = visualizationModel.getNetwork().edgesOf(node);

        for (E edge : edges) {
          update(edge, p);
        }
      }
    }

    @Override
    public void layoutNodePositionChanged(LayoutNodePositionChange.NetworkEvent<N> evt) {
      N node = evt.node;
      org.jungrapht.visualization.layout.model.Point p = evt.location;
      if (visualizationModel.getNetwork().vertexSet().contains(node)) {
        Set<E> edges = visualizationModel.getNetwork().edgesOf(node);
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

      if (!isActive() || !rtree.getRoot().isPresent()) {
        // not active or empty
        // use the fallback NetworkNodeAccessor
        return networkElementAccessor.getEdge(layoutModel, x, y);
      }
      Node<E> root = rtree.getRoot().get();
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

    protected E getClosestEdge(Collection<E> edges, double x, double y, double radius) {

      // since I am comparing with distance squared, i need to square the radius
      double radiusSq = radius * radius;
      if (edges.size() > 0) {
        double closestSoFar = Double.MAX_VALUE;
        E winner = null;
        double winningDistance = -1;
        for (E edge : edges) {
          // get the 2 endpoints
          Graph<N, E> network = visualizationModel.getNetwork();
          N u = network.getEdgeSource(edge);
          N v = network.getEdgeTarget(edge);
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
        log.trace("recalculate for edges: {}", visualizationModel.getNetwork().edgeSet());
        recalculate(visualizationModel.getNetwork().edgeSet());
      }
    }
  }

  public String toString() {
    return rtree.toString();
  }
}
