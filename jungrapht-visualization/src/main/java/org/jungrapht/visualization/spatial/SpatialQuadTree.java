package org.jungrapht.visualization.spatial;

import static org.jungrapht.visualization.spatial.SpatialQuadTree.Quadrant.*;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import org.jungrapht.visualization.layout.event.LayoutVertexPositionChange;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.spatial.rtree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A spatial data structure that uses a quadtree.
 *
 * @author Tom Nelson
 * @param <V> the node type
 */
public class SpatialQuadTree<V> extends AbstractSpatial<V, V>
    implements TreeNode, Spatial<V, V>, LayoutVertexPositionChange.Listener<V> {

  private static final Logger log = LoggerFactory.getLogger(SpatialQuadTree.class);

  private final Object lock = new Object();

  @Override
  public Rectangle2D getBounds() {
    return rectangle;
  }

  @Override
  public Collection<? extends TreeNode> getChildren() {
    return children.values();
  }

  /** the four quadrant keys for the child cells */
  enum Quadrant {
    NE,
    NW,
    SW,
    SE
  }

  /** how many nodes per cell */
  private int MAX_OBJECTS = 1;
  /** max tree height */
  private int MAX_LEVELS = 12;

  /** the level of this cell in the tree */
  private int level;
  /** the nodes contains in this cell, assuming this cell is a leaf */
  private Set<V> nodes;
  /** the area for this cell */
  private Rectangle2D area;
  /** a collection of child nodes, assuming this is not a leaf */
  private Map<Quadrant, SpatialQuadTree<V>> children;

  /** a cache of grid cell rectangles for performance */
  private List<Spatial> gridCache;

  //  private Collection<Shape> pickShapes = EvictingQueue.create(4);

  /** @param layoutModel */
  public SpatialQuadTree(LayoutModel<V> layoutModel) {
    this(layoutModel, 0, 0, 0, layoutModel.getWidth(), layoutModel.getHeight());
  }

  /**
   * @param layoutModel
   * @param width
   * @param height
   */
  public SpatialQuadTree(LayoutModel<V> layoutModel, double width, double height) {
    this(layoutModel, 0, 0, 0, width, height);
  }

  /**
   * @param level level to start at. 0 is the root
   * @param x
   * @param y
   * @param width
   * @param height
   */
  public SpatialQuadTree(
      LayoutModel<V> layoutModel, int level, double x, double y, double width, double height) {
    this(layoutModel, level, new Rectangle2D.Double(x, y, width, height));
  }

  public SpatialQuadTree(LayoutModel<V> layoutModel, int pLevel, Rectangle2D area) {
    super(layoutModel);
    level = pLevel;
    nodes = Collections.synchronizedSet(new HashSet<>());
    this.area = area;
  }

  /**
   * @param o max number of objects allowed
   * @return this QuadTree
   */
  public SpatialQuadTree<V> setMaxObjects(int o) {
    MAX_OBJECTS = o;
    return this;
  }

  /**
   * @param l max levels allowed
   * @return
   */
  public SpatialQuadTree<V> setMaxLevels(int l) {
    MAX_LEVELS = l;
    return this;
  }

  /** @return the level of this cell */
  protected int getLevel() {
    return level;
  }

  /** @return the nodes in this cell, assuming it is a leaf */
  public Set<V> getVertices() {
    return nodes;
  }

  /*
   * Clears the quadtree
   */
  @Override
  public void clear() {
    nodes.clear();
    synchronized (lock) {
      children = null;
      gridCache = null;
    }
  }

  /*
   * Splits the Quadtree into 4 sub-QuadTrees
   */
  protected void split() {
    log.trace("splitting {}", this);
    double width = (area.getWidth() / 2);
    double height = (area.getHeight() / 2);
    double x = area.getX();
    double y = area.getY();

    int childLevel = level + 1;
    SpatialQuadTree<V> ne =
        new SpatialQuadTree(layoutModel, childLevel, x + width, y, width, height);
    SpatialQuadTree<V> nw = new SpatialQuadTree(layoutModel, childLevel, x, y, width, height);
    SpatialQuadTree<V> sw =
        new SpatialQuadTree(layoutModel, childLevel, x, y + height, width, height);
    SpatialQuadTree<V> se =
        new SpatialQuadTree(layoutModel, childLevel, x + width, y + height, width, height);
    synchronized (lock) {
      children = Map.of(NE, ne, NW, nw, SW, sw, SE, se);
    }
  }

  /**
   * find the quadrant that the point would be in
   *
   * @param p the point of interest
   * @return the quadrant that would contain the point
   */
  protected Quadrant getQuadrant(Point p) {
    return getQuadrant(p.x, p.y);
  }

  /**
   * find the quadrant that the point would be in
   *
   * @param x, y the point of interest
   * @return the quadrant that would contain the point
   */
  protected Quadrant getQuadrant(double x, double y) {

    double centerX = area.getCenterX();
    double centerY = area.getCenterY();

    boolean inNorth = y < centerY;

    boolean inSouth = y >= centerY;

    boolean inWest = x < centerX;

    if (inNorth && inWest) {
      return Quadrant.NW;
    }
    if (inSouth && inWest) {
      return Quadrant.SW;
    }
    boolean inEast = x >= centerX;
    if (inNorth && inEast) {
      return Quadrant.NE;
    }
    if (inSouth && inEast) {
      return Quadrant.SE;
    }
    return null;
  }

  /*
   * Insert the object into the quadtree. If the node exceeds the capacity, it
   * will split and add all objects to their corresponding nodes.
   */
  protected void insert(V p) {
    gridCache = null;
    log.trace("{} inserting {} at {}", this, p, layoutModel.apply(p));
    if (children != null) {
      // there are child QuadTrees available
      Quadrant quadrant = getQuadrant(layoutModel.apply(p));
      if (quadrant != null && children.get(quadrant) != null) {
        // insert into the child QuadTree
        children.get(quadrant).insert(p);
        return;
      }
    }
    // insert into this QuadTree unless capacity is exceeded
    nodes.add(p);
    // if capacity is exceeded, split and put all objects into child QuadTrees
    if (nodes.size() > MAX_OBJECTS && level < MAX_LEVELS) {
      split();
      // now this QuadTree has child QuadTrees

      for (Iterator<V> iterator = nodes.iterator(); iterator.hasNext(); ) {
        V node = iterator.next();
        Quadrant quadrant = getQuadrant(layoutModel.apply(node));
        children.get(quadrant).insert(node);
        iterator.remove();
      }
    }
  }

  /*
   * Return all objects that are within the passed rectangle
   */
  protected Set<V> retrieve(Set<V> returnObjects, Rectangle2D r) {
    if (children == null) {
      // i am a leaf, add any nodes i have
      returnObjects.addAll(nodes);
    } else {

      for (Map.Entry<Quadrant, SpatialQuadTree<V>> entry : children.entrySet()) {
        if (entry.getValue().area.intersects(r)) {
          children.get(entry.getKey()).retrieve(returnObjects, r);
        }
      }
    }
    return returnObjects;
  }

  /**
   * Return all objects that are within the passed shape This is needed when the layout is
   * rotated/skewed and the shape edges are no longer parallel to the grid edges.
   */
  protected Set<V> retrieve(Set<V> returnObjects, Shape shape) {
    if (children == null) {
      // i am a leaf, add any nodes i have
      returnObjects.addAll(nodes);
    } else {

      synchronized (lock) {
        for (Map.Entry<Quadrant, SpatialQuadTree<V>> entry : children.entrySet()) {
          if (shape.intersects(entry.getValue().area)) {
            children.get(entry.getKey()).retrieve(returnObjects, shape);
          }
        }
      }
    }
    return returnObjects;
  }

  public List<Spatial> getVertices(List<Spatial> list) {
    if (gridCache == null) {
      list.addAll(this.collectVertices(list, this));
      gridCache = list;
    }
    return gridCache;
  }

  @Override
  public List<Shape> getGrid() {
    List<Shape> areas = new ArrayList<>();

    return collectGrids(areas, this);
  }

  private List<Shape> collectGrids(List<Shape> list, SpatialQuadTree<V> tree) {
    list.add(tree.area);
    if (tree.children != null) {
      for (Map.Entry<Quadrant, SpatialQuadTree<V>> entry : tree.children.entrySet()) {
        collectGrids(list, entry.getValue());
      }
    }
    return list;
  }

  private List<Spatial> collectVertices(List<Spatial> list, SpatialQuadTree<V> tree) {
    list.add(tree);
    if (tree.children != null) {
      for (Map.Entry<Quadrant, SpatialQuadTree<V>> entry : tree.children.entrySet()) {
        collectVertices(list, entry.getValue());
      }
    }
    return list;
  }

  /**
   * @param shape the possibly non-rectangular area of interest
   * @return the nodes that are in the quadtree cells that intersect with the passed shape
   */
  @Override
  public Set<V> getVisibleElements(Shape shape) {
    if (!isActive()) {
      log.trace("not active so getting from the graph");
      return layoutModel.getGraph().vertexSet();
    }

    pickShapes.add(shape);
    Set<V> set = new HashSet<>();
    Set<V> visibleVertices = this.retrieve(set, shape);
    if (log.isDebugEnabled()) {
      log.debug("visibleVertices:{}", visibleVertices);
    }

    return visibleVertices;
  }

  /**
   * @param r
   * @return the nodes that are in the quadtree cells that intersect with the passed rectangle
   */
  public Set<V> getVisibleVertices(Rectangle2D r) {
    if (!isActive()) {
      log.trace("not active so getting from the graph");
      return layoutModel.getGraph().vertexSet();
    }

    Set<V> set = new HashSet<>();
    Set<V> visibleVertices = this.retrieve(set, r);
    if (log.isDebugEnabled()) {
      log.debug("visibleVertices:{}", visibleVertices);
    }
    return visibleVertices;
  }

  /**
   * tha layout area that this tree cell operates over
   *
   * @return
   */
  @Override
  public Rectangle2D getLayoutArea() {
    return area;
  }

  @Override
  public void recalculate() {
    if (isActive()) {
      recalculate(layoutModel.getGraph().vertexSet());
    }
  }

  private void recalculate(Collection<V> nodes) {

    this.clear();
    while (true) {
      try {
        for (V node : nodes) {
          this.insert(node);
        }
        break;
      } catch (ConcurrentModificationException ex) {
        // ignore
      }
    }
  }

  /**
   * @param node the node to search for
   * @return the quadtree leaf that contains the passed node
   */
  public TreeNode getContainingQuadTreeLeaf(V node) {
    // find where it is now, not where the layoutModel will put it
    if (this.nodes.contains(node)) {
      if (log.isTraceEnabled()) {
        log.trace("nodes {} in {} does contain {}", nodes, this, node);
      }
      return this;
    }
    if (children != null) {
      for (Map.Entry<Quadrant, SpatialQuadTree<V>> entry : children.entrySet()) {
        SpatialQuadTree<V> child = entry.getValue();
        TreeNode leaf = child.getContainingQuadTreeLeaf(node);
        if (leaf != null) {
          return leaf;
        }
      }
    }
    return null;
  }

  public Set<SpatialQuadTree<V>> getContainingLeafs(Point2D p) {
    return Collections.singleton(getContainingQuadTreeLeaf(p));
  }

  public Set<SpatialQuadTree<V>> getContainingLeafs(double x, double y) {
    return Collections.singleton(getContainingQuadTreeLeaf(x, y));
  }

  @Override
  public TreeNode getContainingLeaf(Object element) {

    return getContainingQuadTreeLeaf((V) element);
  }

  /**
   * find the cell that would contain the passed point
   *
   * @param p the point of interest
   * @return the cell that would contain p
   */
  public SpatialQuadTree<V> getContainingQuadTreeLeaf(Point2D p) {
    return getContainingQuadTreeLeaf(p.getX(), p.getY());
  }

  /**
   * @param x location of interest
   * @param y location of interest
   * @return the cell that would contain (x, y)
   */
  public SpatialQuadTree<V> getContainingQuadTreeLeaf(double x, double y) {
    if (this.area.contains(x, y)) {
      if (this.children != null) {
        for (Map.Entry<Quadrant, SpatialQuadTree<V>> entry : this.children.entrySet()) {
          if (entry.getValue().area.contains(x, y)) {
            return entry.getValue().getContainingQuadTreeLeaf(x, y);
          }
        }
      } else {
        // i am a leaf. return myself
        return this;
      }
    }
    return null;
  }

  @Override
  public V getClosestElement(Point2D p) {
    return getClosestElement(p.getY(), p.getY());
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
    if (!isActive()) {
      return fallback.getVertex(layoutModel, x, y);
    }
    Spatial leaf = getContainingQuadTreeLeaf(x, y);
    Rectangle2D area = leaf.getLayoutArea();
    double radius = area.getWidth();
    V closest = null;
    while (closest == null) {

      double diameter = radius * 2;

      Ellipse2D searchArea = new Ellipse2D.Double(x - radius, y - radius, diameter, diameter);

      Collection<V> nodes = getVisibleElements(searchArea);
      closest = getClosest(nodes, x, y, radius);

      // if I have already considered all of the nodes in the graph
      // (in the spatialquadtree) there is no reason to enlarge the
      // area and try again
      if (nodes.size() >= layoutModel.getGraph().vertexSet().size()) {
        break;
      }
      // double the search area size and try again
      radius *= 2;
    }
    return closest;
  }

  /**
   * reset the side of this structure
   *
   * @param bounds the new bounds for the data struture
   */
  @Override
  public void setBounds(Rectangle2D bounds) {
    gridCache = null;
    this.area = bounds;
  }

  /**
   * Update the structure for the passed node. If the node is still in the same cell, don't rebuild
   * the structure. If it moved to a new cell, rebuild the structure
   *
   * @param node
   */
  @Override
  public void update(V node, Point location) {
    if (isActive()) {
      gridCache = null;
      if (!this.getLayoutArea().contains(location.x, location.y)) {
        log.trace(location + " outside of spatial " + this.getLayoutArea());
        this.setBounds(this.getUnion(this.getLayoutArea(), location.x, location.y));
        this.recalculate(layoutModel.getGraph().vertexSet());
      }
      Spatial locationContainingLeaf = getContainingQuadTreeLeaf(location.x, location.y);
      log.trace("leaf {} contains {}", locationContainingLeaf, location);
      TreeNode nodeContainingLeaf = getContainingQuadTreeLeaf(node);
      log.trace("leaf {} contains node {}", nodeContainingLeaf, node);
      if (locationContainingLeaf == null) {
        log.trace("got null for leaf containing {}", location);
      }
      if (nodeContainingLeaf == null) {
        log.trace("got null for leaf containing {}", node);
      }
      if (locationContainingLeaf != null && !locationContainingLeaf.equals(nodeContainingLeaf)) {
        log.trace("time to recalculate");
        this.recalculate(layoutModel.getGraph().vertexSet());
      }
      this.insert(node);
    }
  }

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.Event<V> evt) {
    this.update(evt.vertex, evt.location);
  }

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.GraphEvent<V> evt) {
    this.update(evt.vertex, evt.location);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SpatialQuadTree<?> that = (SpatialQuadTree<?>) o;

    if (level != that.level) return false;
    if (!nodes.equals(that.nodes)) return false;
    if (!area.equals(that.area)) return false;
    return layoutModel.equals(that.layoutModel);
  }

  @Override
  public int hashCode() {
    int result = level;
    result = 31 * result + nodes.hashCode();
    result = 31 * result + area.hashCode();
    result = 31 * result + layoutModel.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "SpatialQuadTree{"
        + "level="
        + level
        + ", nodes="
        + nodes
        + ", area="
        + area
        + ", children="
        + children
        + '}';
  }
}
