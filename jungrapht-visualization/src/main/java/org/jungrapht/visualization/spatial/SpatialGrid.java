package org.jungrapht.visualization.spatial;

import java.awt.*;
import java.awt.geom.Area;
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
 * A Spatial Data Structure to optimize rendering performance. The SpatialGrid is used to determine
 * which graph vertices are actually visible for a given rendering situation. Only the visible
 * vertices are passed to the rendering pipeline. When used with Edges, only Edges with at least one
 * visible endpoint are passed to the rendering pipeline.
 *
 * <p>See SimpleGraphSpatialTest (jung-samples) for a rendering that exposes the internals of the
 * SpatialGrid.
 *
 * @author Tom Nelson
 */
public class SpatialGrid<V> extends AbstractSpatial<V, V>
    implements Spatial<V>, TreeNode, LayoutVertexPositionChange.Listener<V> {

  private static final Logger log = LoggerFactory.getLogger(SpatialGrid.class);

  /** the number of grid cells across the width */
  private int horizontalCount;

  /** the number of grid cells across the height */
  private int verticalCount;

  /** the overall size of the area to be divided into a grid */
  private Dimension size;

  /** A mapping of grid cell identified to a collection of contained vertices */
  private final Map<Integer, List<V>> map = Collections.synchronizedMap(new HashMap<>());
  //      Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

  /** the width of a grid cell */
  private double boxWidth;
  /** the height of a grid cell */
  private double boxHeight;

  /** the overall area of the layout (x,y,width,height) */
  private Rectangle2D layoutArea;

  /** a cache of grid cell rectangles for performance */
  private List<Shape> gridCache;

  /**
   * Create an instance
   *
   * @param layoutModel
   */
  public SpatialGrid(LayoutModel<V> layoutModel) {
    super(layoutModel);
    this.horizontalCount = 10;
    this.verticalCount = 10;
    this.setBounds(new Rectangle2D.Double(0, 0, layoutModel.getWidth(), layoutModel.getHeight()));
  }

  /**
   * Create an instance
   *
   * @param bounds the area of the grid
   * @param horizontalCount how many tiles in a row
   * @param verticalCount how many tiles in a column
   */
  public SpatialGrid(
      LayoutModel<V> layoutModel, Rectangle2D bounds, int horizontalCount, int verticalCount) {
    super(layoutModel);
    this.horizontalCount = horizontalCount;
    this.verticalCount = verticalCount;
    this.setBounds(bounds);
  }

  /**
   * Set the layoutSize of the spatial grid and recompute the box widths and heights. null out the
   * obsolete grid cache
   *
   * @param bounds recalculate the size of the spatial area
   */
  public void setBounds(Rectangle2D bounds) {
    this.size = bounds.getBounds().getSize();
    this.layoutArea = bounds;
    this.boxWidth = size.getWidth() / horizontalCount;
    this.boxHeight = size.getHeight() / verticalCount;
    this.gridCache = null;
  }

  @Override
  public Set<TreeNode> getContainingLeafs(Point2D p) {
    int boxNumber = this.getBoxNumberFromLocation(p.getX(), p.getY());
    Rectangle2D r = (Rectangle2D) this.gridCache.get(boxNumber);
    SpatialGrid grid = new SpatialGrid(layoutModel, r, 1, 1);
    return Collections.singleton(grid);
  }

  @Override
  public Set<TreeNode> getContainingLeafs(double x, double y) {
    return getContainingLeafs(new Point2D.Double(x, y));
  }

  @Override
  public TreeNode getContainingLeaf(Object element) {
    for (Map.Entry<Integer, ? extends Collection<V>> entry : map.entrySet()) {
      if (entry.getValue().contains(element)) {
        int index = entry.getKey();
        Rectangle2D r = (Rectangle2D) this.gridCache.get(index);
        return new SpatialGrid<>(layoutModel, r, 1, 1);
      }
    }
    return null;
  }

  public static <V> List<Shape> getGrid(List<Shape> list, SpatialGrid<V> grid) {
    list.addAll(grid.getGrid());
    return list;
  }

  /**
   * Lazily compute the gridCache if needed. The gridCache is a list of rectangles overlaying the
   * layout area. They are numbered from 0 to horizontalCount*verticalCount-1
   *
   * @return the boxes in the grid
   */
  @Override
  public List<Shape> getGrid() {
    if (gridCache == null) {
      gridCache = new ArrayList<>();
      for (int j = 0; j < verticalCount; j++) {
        for (int i = 0; i < horizontalCount; i++) {
          gridCache.add(
              new Rectangle2D.Double(
                  layoutArea.getX() + i * boxWidth,
                  layoutArea.getY() + j * boxHeight,
                  boxWidth,
                  boxHeight));
        }
      }
    }
    return gridCache;
  }

  /**
   * A Multimap of box number to Lists of vertices in that box
   *
   * @return the map of box numbers to contained vertices
   */
  public Map<Integer, ? extends Collection<V>> getMap() {
    return map;
  }

  /**
   * given the box x,y coordinates (not the coordinate system) return the box number (0,0) has box 0
   * (horizontalCount,horizontalCount) has box horizontalCount*verticalCount - 1
   *
   * @param boxX the x value in the box grid
   * @param boxY the y value in the box grid
   * @return the box number for boxX,boxY
   */
  protected int getBoxNumber(int boxX, int boxY) {
    if (log.isTraceEnabled()) {
      if (log.isTraceEnabled()) {
        log.trace(
            "{},{} clamped to {},{}",
            boxX,
            boxY,
            Math.max(0, Math.min(boxX, this.horizontalCount - 1)),
            Math.max(0, Math.min(boxY, this.verticalCount - 1)));
      }
    }
    boxX = Math.max(0, Math.min(boxX, this.horizontalCount - 1));
    boxY = Math.max(0, Math.min(boxY, this.verticalCount - 1));
    if (log.isTraceEnabled()) {
      log.trace("getBoxNumber({},{}):{}", boxX, boxY, (boxY * this.horizontalCount + boxX));
    }
    return boxY * this.horizontalCount + boxX;
  }

  /**
   * @param boxXY the (x,y) in the grid coordinate system
   * @return the box number for that (x,y)
   */
  protected int getBoxNumber(int[] boxXY) {
    return getBoxNumber(boxXY[0], boxXY[1]);
  }

  /**
   * give a Point in the coordinate system, return the box number that contains it
   *
   * @param p a location in the coordinate system
   * @return the box number that contains the passed location
   */
  protected int getBoxNumberFromLocation(org.jungrapht.visualization.layout.model.Point p) {
    int count = 0;
    for (Shape shape : getGrid()) {
      Rectangle2D r = shape.getBounds2D();
      if (r.contains(p.x, p.y) || r.intersects(p.x, p.y, 1, 1)) {
        return count;
      } else {
        count++;
      }
    }
    log.trace("no box for  {}", p);
    return -1;
  }

  /**
   * give a Point in the coordinate system, return the box number that contains it
   *
   * @param x, y a location in the coordinate system
   * @return the box number that contains the passed location
   */
  protected int getBoxNumberFromLocation(double x, double y) {
    int count = 0;
    for (Shape shape : getGrid()) {
      Rectangle2D r = shape.getBounds2D();
      if (r.contains(x, y) || r.intersects(x, y, 1, 1)) {
        return count;
      } else {
        count++;
      }
    }
    log.trace("no box for {},{}", x, y);
    return -1;
  }

  /**
   * given (x,y) in the coordinate system, get the boxX,boxY for the box that it is in
   *
   * @param x a location in the coordinate system
   * @param y a location in the coordinate system
   * @return a 2 dimensional array of int containing the box x and y coordinates
   */
  protected int[] getBoxIndex(double x, double y) {

    // clamp the x and y to be within the bounds of the layout grid
    int[] boxIndex = new int[2];
    int hcount = 0;
    int vcount = 0;
    for (Shape r : getGrid()) {
      if (r.contains(new Point2D.Double(x, y))) {
        boxIndex = new int[] {hcount, vcount};
        break;
      }
      hcount++;
      if (hcount >= this.horizontalCount) {
        hcount = 0;
        vcount++;
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("boxIndex for ({},{}) is {}", x, y, Arrays.toString(boxIndex));
    }
    return boxIndex;
  }

  @Override
  public void recalculate() {
    if (isActive()) {
      recalculate(layoutModel.getGraph().vertexSet());
    }
  }

  @Override
  public void clear() {
    this.map.clear();
  }

  /**
   * Recalculate the contents of the Map of box number to contained Vertices
   *
   * @param vertices the collection of vertices to update in the structure
   */
  public void recalculate(Collection<V> vertices) {
    clear();
    while (true) {
      try {
        for (V vertex : vertices) {
          int box = this.getBoxNumberFromLocation(layoutModel.apply(vertex));
          if (this.map.containsKey(box)) {
            this.map.get(box).add(vertex);
          } else {
            List<V> list = new ArrayList<>();
            list.add(vertex);
            this.map.put(box, list);
          }
          //          this.map.put(this.getBoxNumberFromLocation(layoutModel.apply(vertex)), vertex);
        }
        break;
      } catch (ConcurrentModificationException ex) {
        // ignore
      }
    }
  }

  /**
   * update the location of a vertex in the map of box number to vertex lists
   *
   * @param vertex the vertex to update in the structure
   */
  @Override
  public void update(V vertex, Point location) {
    if (isActive()) {
      if (!this.getLayoutArea().contains(location.x, location.y)) {
        log.trace(location + " outside of spatial " + this.getLayoutArea());
        this.setBounds(this.getUnion(this.getLayoutArea(), location.x, location.y));
        recalculate(layoutModel.getGraph().vertexSet());
      }

      int rightBox = this.getBoxNumberFromLocation(layoutModel.apply(vertex));
      // vertex should end up in box 'rightBox'
      // check to see if it is already there
      if (map.get(rightBox).size() > 0 && map.get(rightBox).contains(vertex)) {
        // nothing to do here, just return
        return;
      }
      // remove vertex from the first (and only) wrong box it is found in
      Integer wrongBox = null;
      synchronized (map) {
        for (Integer box : map.keySet()) {
          if (map.get(box).size() > 0 && map.get(box).contains(vertex)) {
            // remove it and stop, because vertex can be in only one box
            wrongBox = box;
            break;
          }
        }
      }
      if (wrongBox != null) {
        map.remove(wrongBox, vertex);
      }
      if (map.get(rightBox).size() > 0) {
        map.get(rightBox).add(vertex);
      } else {
        List<V> list = new ArrayList<>();
        list.add(vertex);
        map.put(rightBox, list);
      }
      //      map.put(rightBox, vertex);
    }
  }

  @Override
  public V getClosestElement(Point2D p) {
    return getClosestElement(p.getX(), p.getY());
  }

  @Override
  public V getClosestElement(double x, double y) {
    if (!isActive()) {
      return fallback.getVertex(layoutModel, x, y);
    }
    Collection<TreeNode> leafs = getContainingLeafs(x, y);
    if (leafs.size() != 0) {
      TreeNode leaf = leafs.iterator().next();

      Rectangle2D area = leaf.getBounds();
      double radius = area.getWidth();
      V closest = null;
      while (closest == null) {

        double diameter = radius * 2;

        Ellipse2D searchArea = new Ellipse2D.Double(x - radius, y - radius, diameter, diameter);

        Collection<V> vertices = getVisibleElements(searchArea);
        closest = getClosest(vertices, x, y, radius);

        // if I have already considered all of the vertices in the graph
        // (in the spatialquadtree) there is no reason to enlarge the
        // area and try again
        if (vertices.size() >= layoutModel.getGraph().vertexSet().size()) {
          break;
        }
        // double the search area size and try again
        radius *= 2;
      }
      return closest;
    }

    return null;
  }
  /**
   * given a rectangular area and an offset, return the tile numbers that are contained in it
   *
   * @param visibleArea the (possibly) non-rectangular area of interest
   * @return the tile numbers that intersect with the visibleArea
   */
  protected Collection<Integer> getVisibleTiles(Shape visibleArea) {
    Set<Integer> visibleTiles = new HashSet<>();
    List<Shape> grid = getGrid();
    for (int i = 0; i < this.horizontalCount * this.verticalCount; i++) {
      if (visibleArea.intersects(grid.get(i).getBounds2D())) {
        visibleTiles.add(i);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("visible boxes:{}", visibleTiles);
    }
    return visibleTiles;
  }

  /**
   * Given an area, return a collection of the vertices that are contained in it (the vertices that
   * are contained in the boxes that intersect with the area)
   *
   * @param visibleArea a shape projected on the grid
   * @return the vertices that should be visible
   */
  @Override
  public Set<V> getVisibleElements(Shape visibleArea) {
    if (!isActive()) {
      log.trace("not active so getting from the graph");
      return layoutModel.getGraph().vertexSet();
    }

    pickShapes.add(visibleArea);
    Area area = new Area(visibleArea);
    area.intersect(new Area(this.layoutArea));
    if (log.isTraceEnabled()) {
      log.trace("map is {}", map);
    }
    Set<V> visibleVertices = new HashSet<>();
    Collection<Integer> tiles = getVisibleTiles(area);
    for (Integer index : tiles) {
      Collection<V> toAdd = this.map.get(index);
      if (toAdd.size() > 0) {
        visibleVertices.addAll(toAdd);
        if (log.isTraceEnabled()) {
          log.trace("added all of: {} from index {} to visibleVertices", toAdd, index);
        }
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("visibleVertices:{}", visibleVertices);
    }
    return visibleVertices;
  }

  /** @return the layout area rectangle for this grid */
  @Override
  public Rectangle2D getLayoutArea() {
    return layoutArea;
  }

  @Override
  public Rectangle2D getBounds() {
    return layoutArea;
  }

  @Override
  public Collection<? extends TreeNode> getChildren() {
    return null;
  }

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.Event<V> evt) {
    update(evt.vertex, evt.location);
  }

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.GraphEvent<V> evt) {
    update(evt.vertex, evt.location);
  }
}
