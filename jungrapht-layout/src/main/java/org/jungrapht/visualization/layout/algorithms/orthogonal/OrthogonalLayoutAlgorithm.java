package org.jungrapht.visualization.layout.algorithms.orthogonal;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.algorithms.AbstractLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.DimensionSummaryStatistics;
import org.jungrapht.visualization.layout.algorithms.util.PointSummaryStatistics;
import org.jungrapht.visualization.layout.algorithms.util.VertexBoundsFunctionConsumer;
import org.jungrapht.visualization.layout.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrthogonalLayoutAlgorithm<V, E> extends AbstractLayoutAlgorithm<V>
    implements LayoutAlgorithm<V>, VertexBoundsFunctionConsumer<V> {

  private static final Logger log = LoggerFactory.getLogger(OrthogonalLayoutAlgorithm.class);
  protected static final Point IDENTITY_SHAPE = Point.ORIGIN;

  public static class Builder<
          V, E, T extends OrthogonalLayoutAlgorithm<V, E>, B extends Builder<V, E, T, B>>
      extends AbstractLayoutAlgorithm.Builder<V, T, B> implements LayoutAlgorithm.Builder<V, T, B> {
    protected Function<V, Rectangle> vertexBoundsFunction = v -> Rectangle.of(0, 0, 1, 1);

    public B vertexBoundsFunction(Function<V, Rectangle> vertexBoundsFunction) {
      this.vertexBoundsFunction = vertexBoundsFunction;
      return self();
    }

    public T build() {
      return (T) new OrthogonalLayoutAlgorithm(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected OrthogonalLayoutAlgorithm(Builder builder) {
    super(builder);
    this.vertexDimensionFunction = builder.vertexBoundsFunction;
  }

  Mappings<V> mappings;
  /**
   * CTOR only for unit tests
   *
   * @param layoutModel
   */
  OrthogonalLayoutAlgorithm(LayoutModel<V> layoutModel) {
    super(OrthogonalLayoutAlgorithm.builder());
    this.graph = layoutModel.getGraph();
    this.layoutModel = layoutModel;
    this.vertexDimensionFunction = v -> Rectangle.of(0, 0, 1, 1);
    this.mappings = new Mappings<>();
  }

  Function<V, Rectangle> identityVertexDimensionFunction = v -> Rectangle.of(0, 0, 1, 1);

  Function<V, Rectangle> realVertexDimensionFunction = identityVertexDimensionFunction;

  Function<V, Rectangle> vertexDimensionFunction = identityVertexDimensionFunction;

  int cellSize = 1;

  int initialGridSize;

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    //    visitLoop(layoutModel);
    Thread thread = new Thread(() -> visitLoop(layoutModel));
    thread.start();
  }

  public void visitLoop(LayoutModel<V> layoutModel) {
    this.layoutModel = layoutModel;
    this.mappings = new Mappings<>();
    this.graph = layoutModel.getGraph();
    int gridSize = this.initialGridSize();
    this.initialGridSize = gridSize;
    log.info("initialGridSize: {}", gridSize);
    layoutModel.setSize(600, 600);
    int vertexCount = graph.vertexSet().size();
    this.placeVerticesRandomlyInGridSpace(graph, gridSize);
    //    printGrid();
    Compaction.Direction compactionDirection = Compaction.Direction.HORIZONTAL;
    double sqrtVertexCount = Math.sqrt(vertexCount);
    int iterationCount = (int) (90 * sqrtVertexCount);
    double temperature = 2 * sqrtVertexCount;
    log.info("temperature: {}", temperature);
    //        k=(0.2/T)1/iterationCount ;
    double k = Math.pow(0.2 / temperature, 1.0 / iterationCount);
    log.info("k: {}", k);
    vertexDimensionFunction = identityVertexDimensionFunction;

    display();
    int iteration = 0;

    for (iteration = 0; iteration <= iterationCount / 2; iteration++) {
      display();
      for (V v : graph.vertexSet()) {
        //        display();
        this.placeNearMedian(v, temperature, temperature);
      }
      if (iteration % 9 == 0) {
        compact(compactionDirection, 3, false);
        compactionDirection = compactionDirection.toggle();
        //        mappings.expand(initialGridSize);
      }
      temperature = temperature * k;
      log.info("temperature: {}", temperature);
    }

    vertexDimensionFunction = realVertexDimensionFunction;
    //    display();
    compactionDirection = Compaction.Direction.HORIZONTAL;
    compact(compactionDirection, 3, true);
    display();
    compactionDirection = Compaction.Direction.VERTICAL;
    compact(compactionDirection, 3, true);
    display();
    //    mappings.expand(initialGridSize);

    vertexDimensionFunction = identityVertexDimensionFunction;

    int c = computeGridCell();
    for (iteration = iterationCount / 2 + 1; iteration <= iterationCount; iteration++) {
      display();
      for (V v : graph.vertexSet()) {
        //        display();
        Rectangle vd = vertexDimensionFunction.apply(v);
        double wprimej = widthInGrid(v, c);
        double hprimej = heightInGrid(v, c);
        double randomX = temperature * vd.width / wprimej;
        double randomY = temperature * vd.height / hprimej;
        this.placeNearMedian(v, randomX, randomY);
      }
      if (iteration % 9 == 0) {
        compact(
            compactionDirection,
            Math.max(1, 1 + 2 * (iterationCount - iteration - 30) / (0.5 * iterationCount)),
            false);
        compactionDirection = compactionDirection.toggle();
        //        mappings.expand(initialGridSize);
      }
      temperature = temperature * k;
      log.info("temperature: {}", temperature);
    }
    vertexDimensionFunction = realVertexDimensionFunction;
    //    display(5000);
    //
    //    compact(Compaction.Direction.HORIZONTAL, 3, true);
    //    display(5000);
    //    compact(Compaction.Direction.VERTICAL, 3, true);
    log.info("done. mappings: {}", mappings);
    display(0);
    //    mappings.confirmIntegrity();
    //    Mappings<V> mappingsCopy = Mappings.copy(mappings);
    //    mappingsToFillLayoutModel(mappingsCopy, layoutModel);
    //    centerIt(layoutModel, Collections.emptyList());
  }

  void display() {
    display(1);
  }

  void display(long sleep) {

    mappings.confirmIntegrity();
    Mappings<V> mappingsCopy = Mappings.copy(mappings);
    mappingsToFillLayoutModel(mappingsCopy, layoutModel);
    //    centerIt(layoutModel, Collections.emptyList());
    try {
      Thread.sleep(sleep);
    } catch (InterruptedException ex) {
    }
  }

  protected void placeNearMedian(V v, double randomX, double randomY) {
    //    randomX = randomY = 0; // hack

    //    Rectangle vd = vertexDimensionFunction.apply(v);
    //    double wprimej = widthInGrid(v, c);
    //    double hprimej = heightInGrid(v, c);
    Point neighborsMedian =
        neighborsCentroid(v)
            //            neighborsMedianPoint(v)
            .add(randomInRange(-randomX, randomX), randomInRange(-randomY, randomY));
    //    log.info("neighborSet of {} is {}", v, Graphs.neighborSetOf(graph, v));
    //    log.info("median of {} is {}",
    //            Graphs.neighborSetOf(graph, v)
    //                    .stream()
    //                    .map(w -> mappings.get(w))
    //                    .collect(Collectors.toList()),neighborsMedian
    //
    //            );

    int x = (int) neighborsMedian.x;
    int y = (int) neighborsMedian.y;
    //    mappings.accept(v, neighborsMedian);
    // closest free rectangle to x, y
    Point closestTo = closestFreeRectangleTo(x, y);

    int manhattanDistance = (int) Math.round(manhattanDistance(closestTo, Point.of(x, y)));
    Collection<Point> mdPlusOne = neighborsWithin(closestTo, manhattanDistance + 1);

    List<Point> winners = this.rectanglesSortedByLeastDistanceToNeighbors(v, mdPlusOne);

    for (Point winner : winners) {
      // if the winner is where v already is, swap around with nearby nodes
      if (mappings.get(v).equals(winner)) {
        swapWithNearby(v);
        break;
      } else if (mappings.empty(winner)) {
        mappings.accept(v, winner);
        break;
      }
    }
  }

  protected void mappingsToFillLayoutModel(Mappings<V> mappings, LayoutModel<V> layoutModel) {
    // do all vertices exist as keys?
    if (graph.vertexSet().size() != mappings.vertices().size()) {
      throw new IllegalArgumentException("graph vertices and mappings mismatch");
    }
    mappings.normalize();
    Rectangle extent = //Rectangle.of(0,0,initialGridSize, initialGridSize);
        mappings.computeExtent();
    //    log.info("mappings extent: {} ", extent);
    layoutModel.setSize((int) extent.width, (int) extent.height);
    mappings
        .entries()
        .stream()
        .forEach(e -> layoutModel.set(e.getKey(), e.getValue().x, e.getValue().y));

    //    double horizontalExpansion = 1;//layoutModel.getWidth() / (extent.width+2);
    //    double verticalExpansion = 1;//layoutModel.getHeight() / (extent.height+2);
    //    mappings.entries().stream()
    //            .forEach(e -> layoutModel.set(e.getKey(),e.getValue().x*horizontalExpansion,
    //                            e.getValue().y*verticalExpansion));
  }

  /**
   * which potential cell for v gives the least distance to all of v's neighbors
   *
   * @param v
   * @param potentialCells
   * @return
   */
  List<Point> rectanglesSortedByLeastDistanceToNeighbors(V v, Collection<Point> potentialCells) {

    return potentialCells
        .stream()
        .sorted(
            (r1, r2) -> Double.compare(sumOfDistToNeighbors(v, r1), sumOfDistToNeighbors(v, r2)))
        .collect(Collectors.toList());
  }

  /**
   * @param mappings
   * @param size
   */
  void expandMappings(Mappings<V> mappings, int size) {}

  Point neighborsMedian(V v, double temperature) {

    Rectangle vd = vertexDimensionFunction.apply(v);
    Point neighborsMedian =
        neighborsCentroid(v)
            .add(
                randomInRange(
                    -temperature * vd.width / vd.width, temperature * vd.width / vd.width),
                randomInRange(
                    -temperature * vd.height / vd.height, temperature * vd.height / vd.height));
    int x = (int) neighborsMedian.x;
    int y = (int) neighborsMedian.y;
    return Point.of(x, y);
  }

  private void printGrid() {
    System.err.println("-------");
    // imagine a grid that is width / height
    PointSummaryStatistics ps = new PointSummaryStatistics();
    Collection<Point> locations =
        graph.vertexSet().stream().map(v -> mappings.get(v)).collect(Collectors.toSet());
    //            vertexToRectangleMap.values()
    //                    .stream().map(r -> Point.of(r.x, r.y)).collect(Collectors.toSet());
    locations.forEach(p -> ps.accept(Point.of(p.x, p.y)));
    Point min = ps.getMin();
    Point max = ps.getMax();
    //    int width = (int) (max.x - min.x);
    //    int height = (int) (max.y - min.y);
    for (int i = (int) min.y; i <= max.y; i++) {
      for (int j = (int) min.x; j <= max.x; j++) {
        if (locations.contains(Point.of(j, i))) {
          System.err.print("x");
        } else {
          System.err.print("-");
        }
      }
      System.err.println();
    }
    System.err.println("-------");
  }

  /** move over so there are no negative values */
  protected void expandInLayoutModel(int gridSize, LayoutModel<V> layoutModel) {
    for (V v : graph.vertexSet()) {
      Point r = mappings.get(v);
      layoutModel.set(v, r.multiply(layoutModel.getWidth() / gridSize));
    }
  }

  Rectangle currentGridSize() {
    return mappings.computeExtent();
  }

  protected void centerIt(LayoutModel<V> layoutModel, Collection<List<Point>> articulations) {
    Rectangle extent = Expansion.computeLayoutExtent2(layoutModel, articulations);
    // width of extent
    double widthOfExtent = extent.width - 2;
    double heightOfExtent = extent.height - 2;
    int widthOfLayoutModel = layoutModel.getWidth();
    int heightOfLayoutModel = layoutModel.getHeight();
    // move everything by widthOfLayoutModel/2 - widthOfExtent/2
    double movexBy = extent.x + widthOfExtent / 2 - widthOfLayoutModel / 2;
    double moveyBy = extent.y + heightOfExtent / 2 - heightOfLayoutModel / 2;
    layoutModel
        .getLocations()
        .forEach(
            (v, p) -> {
              p = Point.of(p.x - movexBy, p.y - moveyBy);
              layoutModel.set(v, p);
            });
  }

  double totalEdgeLength() {
    double totalDistance = 0;
    for (E edge : graph.edgeSet()) {
      V v1 = graph.getEdgeSource(edge);
      V v2 = graph.getEdgeTarget(edge);
      totalDistance += this.distance(v1, v2);
    }
    return totalDistance;
  }

  double totalEdgeLength(V v) {
    double totalDistance = 0;
    for (V neighbor : Graphs.neighborSetOf(graph, v)) {
      //      V v1 = graph.getEdgeSource(edge);
      //      V v2 = graph.getEdgeTarget(edge);
      totalDistance += this.distance(v, neighbor);
    }
    return totalDistance;
  }

  /**
   * compute the total distance to all neighbors of v1 and v2 if they are swapped
   *
   * @param v1
   * @param v2
   * @return
   */
  double totalDistanceSwapped(V v1, V v2) {
    mappings.swap(v1, v2); // swap them
    // measure distance
    double totalEdgeLength = sumOfDistToNeighbors(v1) + sumOfDistToNeighbors(v2);
    //    log.info("mappings  pre-swap: {}", mappings);
    mappings.swap(v1, v2); // put them back
    //    log.info("mappings post-swap: {}", mappings);
    return totalEdgeLength;
  }

  private void compact(Compaction.Direction direction, double gamma, boolean expand) {
    mappings.normalize();

    log.info("will compact {} with gamma:{}", direction, gamma);
    display();
    //    try { Thread.sleep(500); }
    //    catch(InterruptedException ex) {}
    List<Cell<V>> cells = new ArrayList<>();
    graph
        .vertexSet()
        .forEach(
            v -> {
              Point r = mappings.get(v);
              cells.add(
                  Cell.of(
                      v,
                      r.x,
                      r.y, //1, 1));
                      vertexDimensionFunction.apply(v).width,
                      vertexDimensionFunction.apply(v).height));
            });
    Compaction.of(cells, direction, gamma, vertexDimensionFunction, mappings::accept);

    //    log.info("mappings are {}", mappings.getVertexToPointMap());
    //    log.info("cells are {}", cells);
    log.info("{} done", direction);
    display();
    //    try { Thread.sleep(500); }
    //    catch(InterruptedException ex) {}
  }

  Point closestFreeRectangleTo(double x, double y) {
    return closestFreeRectangleTo(x, y, 1);
  }

  Point closestFreeRectangleTo(Point p, int distance) {
    return closestFreeRectangleTo(p.x, p.y, distance);
  }

  Point closestFreeRectangleTo(double x, double y, int i) {
    Point me = Point.of(Math.round(x), Math.round(y));
    if (mappings.empty(me)) {
      return me;
    }
    List<Point> closestEmptyRectangleOrigins = findNearbyEmptyCells(me, i);
    // sort by distance from 'me'
    closestEmptyRectangleOrigins.sort(Comparator.comparingDouble(me::distance));
    return closestEmptyRectangleOrigins.get(0);
  }

  Collection<V> adjacentGridCellVertices(Point r) {
    return adjacentGridCellVertices(r, 1);
  }

  Collection<V> adjacentGridCellVertices(Point r, int dist) {
    Set<V> adjacents = new HashSet<>();
    for (int i = (int) r.x - dist; i <= (int) r.x + dist; i++) {
      for (int j = (int) r.y - dist; j <= (int) r.y + dist; j++) {
        if (i != r.x && j != r.y) {
          Point adj = Point.of(i, j);
          if (!mappings.empty(adj)) {
            adjacents.add(mappings.get(adj));
          }
        }
      }
    }
    if (adjacents.isEmpty()) {
      return adjacentGridCellVertices(r, ++dist);
    }
    return adjacents;
  }

  boolean swapWithNearby(V v) {
    // using distance function, add total distance to all neighbors of v
    double totalEdgeLength = sumOfDistToNeighbors(v);
    Point r = mappings.get(v); // this is the current location of v
    Collection<V> adjacents =
        this.occupiedNeighborsOf(r, 1).stream().map(mappings::get).collect(Collectors.toSet());

    //            this.neighborsOf(r).stream()
    //            .map(mappings::get).collect(Collectors.toSet());
    //            this.adjacentGridCellVertices(r); ////////
    V winner = null;
    for (V adj : adjacents) {
      // do i want the first one or the 'worst' one?
      double totalForAdj = totalDistanceSwapped(v, adj);
      if (totalForAdj > totalEdgeLength) {
        winner = adj;
      }
    }
    if (winner != null) {
      mappings.swap(v, winner);
      return true;
    }
    return false; // no adjacents or none are longer/shorter when swapped
  }

  Comparator<Point> euclideanComparator = (p1, p2) -> (int) p1.distance(p2);
  /**
   * Find the closest free rectangle to the provided (x, y)
   *
   * @param x
   * @param y
   * @param i
   * @return
   */
  Point noClosestFreeRectangleTo(double x, double y, int i) {
    Set<Point> rectangles = new HashSet<>();
    Point r = Point.of((int) x, (int) y);
    if (!mappings.rectangles().contains(r)) {
      rectangles.add(r);
    }
    r = Point.of((int) x, (int) (y + i));
    if (!mappings.rectangles().contains(r)) {
      rectangles.add(r);
    }
    r = Point.of((int) (x + i), (int) y);
    if (!mappings.rectangles().contains(r)) {
      rectangles.add(r);
    }
    r = Point.of((int) (x + i), (int) (y + i));
    if (!mappings.rectangles().contains(r)) {
      rectangles.add(r);
    }

    if (rectangles.isEmpty()) {
      // no free rectangles within 'i', increment i and go again
      return closestFreeRectangleTo(x, y, i + 1);
    }
    Point closestRectangle = null;
    double leastManhattanDistance = Double.MAX_VALUE;
    for (Point rec : rectangles) {
      if (manhattanDistance(rec, Point.of(x, y)) < leastManhattanDistance) {
        closestRectangle = rec;
      }
    }
    return closestRectangle;
  }

  /**
   * @param cell A Rectangle, with a V
   * @param cells all Rectangles that have a V associated
   * @return
   */
  private Rectangle closestTo(Rectangle cell, Collection<Rectangle> cells) {
    Rectangle closest = null;
    double min = Double.MAX_VALUE;
    for (Rectangle potential : cells) {
      if (potential != cell) {
        double dist = euclideanDistance(cell, potential);
        if (dist < min) {
          min = dist;
          closest = potential;
        }
      }
    }
    return closest;
  }

  private double randomInRange(double lower, double upper) {
    return lower + (Math.random() * (upper - lower));
  }

  void placeVerticesRandomlyInGridSpace(Graph<V, E> graph, int gridSize) {
    // populate grid with random vertices
    graph
        .vertexSet()
        .forEach(
            v -> {
              // random choice of grid cell
              int x = (int) (Math.random() * gridSize);
              int y = (int) (Math.random() * gridSize);

              Point rectangle = Point.of(x, y);
              // no dupes
              while (mappings.rectangles().contains(rectangle)) {
                x = (int) (Math.random() * gridSize);
                y = (int) (Math.random() * gridSize);
                rectangle = Point.of(x, y);
              }
              mappings.accept(v, rectangle);
            });
  }

  /**
   * The neighbors within distance d of Point p
   *
   * @param p
   * @param d
   * @return
   */
  List<Point> neighborsOf(Point p, int d) {
    List<Point> list;
    double t = d * 4;
    double rad = 2 * Math.PI / t;
    list =
        IntStream.iterate(0, i -> i < t, i -> i + 1)
            .mapToDouble(i -> rad * i)
            .mapToObj(angle -> Point.of((int) (d * Math.cos(angle)), (int) (d * Math.sin(angle))))
            .map(p::add)
            .collect(Collectors.toList());
    return list;
  }

  List<Point> occupiedNeighborsOf(Point p, int d) {
    List<Point> list =
        neighborsOf(p, d).stream().filter(pt -> !mappings.empty(pt)).collect(Collectors.toList());
    if (list.isEmpty()) {
      return occupiedNeighborsOf(p, ++d);
    } else {
      return list;
    }
  }

  /**
   * The neighbors within distance d of Point p
   *
   * @param p
   * @param d
   * @return
   */
  List<Point> neighborsOf(Point p, double d) {
    List<Point> list;
    double t = (int) d * 4;
    double rad = 2 * Math.PI / t;
    list =
        IntStream.iterate(0, i -> i < t, i -> i + 1)
            .mapToDouble(i -> rad * i)
            .mapToObj(
                angle -> Point.of(Math.round(d * Math.cos(angle)), Math.round(d * Math.sin(angle))))
            .map(p::add)
            .collect(Collectors.toList());
    return list;
  }

  List<Point> neighborsWithin(Point p, int d) {
    List<Point> list = new ArrayList<>();
    for (int i = 0; i <= d; i++) {
      list.addAll(neighborsOf(p, (double) i));
    }
    return list;
  }

  List<Point> neighborsWithin(Rectangle cell, int d) {
    return neighborsWithin(cell.min(), d);
  }

  /*
      counter from 0 to 2*d
      d=2
      0 1 2 3 4
      for x, offset by -d
      -2 -1 0 1 2 1 0 -1

      for y,  x + y = d
                  y = d - x
      2 - (-2), 2 - (-1), 2 - 0, 2 - 1, 2 - 2
            4,        3,      2,     1,     0
            0         -1      -2

                0 1 2 3 4 5 6
       -3 -1 -1 0 1 2 3

      d = 1
      x -> -1 0 1  0
      y ->  0 1 0 -1

      d=2
      x -> -2,-1, 0, 1, 2, 1, 0,-1
      y ->  0,-1,-2,-1, 0, 1, 2, 1

      d=3
      x -> -3, -2, -1,  0,  1,  2, 3, 2, 1, 0, -1, -2
      y ->  0, -1, -2, -3, -2, -1, 0, 1, 2, 3,  2,  1

      0 1 2 3 4 5 6 7 8

      x -> -4 -3 -2 -1  0 1 2 3 4 3 2 1 0 -1 -2 -3
      y ->  0 -1 -2 -3 -4 -3 -2 -1 0
      (1,
  */
  //    return list;
  //  }
  List<Rectangle> neighborsOf(Rectangle cell, int dist) {
    List<Point> points = this.neighborsOf(cell.min(), (double) dist);
    return points
        .stream()
        .map(p -> Rectangle.of(p, (int) cell.width, (int) cell.height))
        .collect(Collectors.toList());
  }

  List<Point> findNearbyEmptyCells(Point cell, int dist) {
    List<Point> neighbors = neighborsWithin(cell, dist);
    // remove any neighbors whose Rectangles are already occupied
    neighbors = neighbors.stream().filter(c -> mappings.empty(c)).collect(Collectors.toList());
    if (!neighbors.isEmpty()) {
      return neighbors;
    } else {
      return findNearbyEmptyCells(cell, ++dist);
    }
  }

  List<Point> neighborsOf(Point r) {
    return neighborsWithin(r, 1);
  }

  Set<Point> occupiedRectangles() {
    return mappings.rectangles().stream().collect(Collectors.toSet());
  }

  @Override
  public void setVertexBoundsFunction(Function<V, Rectangle> vertexBoundsFunction) {
    this.realVertexDimensionFunction = vertexBoundsFunction;
  }

  Graph<V, E> graph;
  int delta;
  LayoutModel<V> layoutModel;
  double TMin = 0.2;

  DimensionSummaryStatistics prepare() {
    DimensionSummaryStatistics dss = new DimensionSummaryStatistics();
    graph
        .vertexSet()
        .forEach(
            v -> {
              Dimension dimensionPlusDelta =
                  Dimension.of(
                      (int) vertexDimensionFunction.apply(v).width + delta,
                      (int) vertexDimensionFunction.apply(v).height + delta);
              dss.accept(dimensionPlusDelta);
            });
    return dss;
  }

  int computeElMin(DimensionSummaryStatistics dss) {
    Dimension min = dss.getMin();
    return Math.min(min.width, min.height);
  }

  int computeElMax(DimensionSummaryStatistics dss) {
    Dimension max = dss.getMax();
    return Math.max(max.width, max.height);
  }

  // 'c' in paper

  /**
   *  Lmax if Lmax < 3Lmin c =  3Lmin / 2 if 3Lmin ≤ Lmax < 15Lmin  Lmax / 30 if 15Lmin ≤ Lmax
   * equation (1) where Lmin = min(min(w(i) + δ), min(h(i) + δ)) and Lmax = max(max(w(i) + δ),
   * max(h(i) + δ)).
   *
   * @return
   */
  int computeGridCell() {
    DimensionSummaryStatistics dss = prepare();
    int elMax = computeElMax(dss);
    int elMin = computeElMin(dss);
    if (elMax < 3 * elMin) {
      return elMax;
    } else if (elMax < 15 * elMin) {
      return (3 * elMin) / 2;
    } else {
      return elMax / 30;
    }
  }

  /**
   * @param v
   * @param x
   * @param y
   */
  void putVertexNearXY(V v, int x, int y) {}

  /**
   * w'(i) = ⌈ w(i)+δ / c ⌉
   *
   * @param v
   * @param c
   * @return
   */
  int widthInGrid(V v, int c) {
    return (int) Math.ceil((vertexDimensionFunction.apply(v).width + delta) / c);
  }

  // h'(i)

  /**
   * h'(i) = ⌈ h(i)+δ / c ⌉
   *
   * @param v
   * @param c
   * @return
   */
  int heightInGrid(V v, int c) {
    return (int) Math.ceil((vertexDimensionFunction.apply(v).height + delta) / c);
  }

  int euclideanDistance(V v1, V v2) {
    double w1 = vertexDimensionFunction.apply(v1).width;
    double w2 = vertexDimensionFunction.apply(v2).width;
    double h1 = vertexDimensionFunction.apply(v1).height;
    double h2 = vertexDimensionFunction.apply(v2).height;
    // gets from the Mappings, not the LayoutModel
    Point p1 = locationModel.apply(v1).add(-w1 / 2, -h1 / 2);
    Point p2 = locationModel.apply(v2).add(-w2 / 2, -h2 / 2);
    Rectangle r1 = Rectangle.of(p1.x, p1.y, w1, h1);
    Rectangle r2 = Rectangle.of(p2.x, p2.y, w2, h2);
    return euclideanDistance(r1, r2);
  }

  Function<V, Point> locationModel =
      v -> {
        Point cell = mappings.get(v);
        return Point.of(cell.x, cell.y);
      };

  // d sub e(v(i), v(j))
  int euclideanDistance(Rectangle r1, Rectangle r2) {
    double x1, x2, y1, y2;
    double w, h;
    if (r1.x > r2.x) {
      x1 = r2.x;
      w = r2.width;
      x2 = r1.x;
    } else {
      x1 = r1.x;
      w = r1.width;
      x2 = r2.x;
    }
    if (r1.y > r2.y) {
      y1 = r2.y;
      h = r2.height;
      y2 = r1.y;
    } else {
      y1 = r1.y;
      h = r1.height;
      y2 = r2.y;
    }
    double a = Math.max(0, x2 - x1 - w);
    double b = Math.max(0, y2 - y1 - h);
    return (int) Math.sqrt(a * a + b * b);
  }

  Point locationOf(V v) {
    return mappings.get(v);
  }

  /**
   * return the center of the Shape for v
   *
   * @param v
   * @return
   */
  int xci(V v) {
    // find the upper-left for the Rectangle containing v
    return (int) locationOf(v).x;
  }

  /**
   * return the center y of the Shape for v
   *
   * @param v
   * @return
   */
  int yci(V v) {
    return (int) locationOf(v).y;
  }

  //                           1          |xci - xcj|   |yci - ycj|
  // d(vi, vj) = de(vi, vj) + ----  min ( ----------- , ----------- )
  //                           20          w'i + w'j     h'i + h'j

  int distance(V vi, V vj) {
    return distance(vi, locationOf(vi), vj, locationOf(vj));
  }

  int distance(V vi, Point vcpi, V vj, Point vcpj) {
    // @TODO: optimize out later
    int c = computeGridCell();

    if (vcpi == null) {
      vcpi = locationOf(vi);
    }
    if (vcpj == null) {
      vcpj = locationOf(vj);
    }
    double lhn = Math.abs(vcpi.x - vcpj.x); // left-hand numerator
    double rhn = Math.abs(vcpi.y - vcpj.y); // right-hand numerator
    double lhd = widthInGrid(vi, c) + widthInGrid(vj, c);
    double rhd = heightInGrid(vi, c) + heightInGrid(vj, c);

    double distance = euclideanDistance(vi, vj) + Math.min(lhn / lhd, rhn / rhd) / 20.0;
    return (int) distance;
  }

  double euclideanDistance(Point p1, Point p2) {
    return p1.distance(p2);
  }

  double manhattanDistance(V vi, V vj) {
    Point vcpi = locationOf(vi);
    Point vcpj = locationOf(vj);
    return Math.abs(vcpi.x - vcpj.x) + Math.abs(vcpi.y - vcpj.y);
  }

  static double manhattanDistance(Point pi, Point pj) {
    return Math.abs(pi.x - pj.x) + Math.abs(pi.y - pj.y);
  }

  int distance(Point cell, V vi, V vj) {
    int c = computeGridCell();
    double lhn = Math.abs(cell.x - xci(vj)); // left-hand numerator
    double rhn = Math.abs(cell.y - yci(vj)); // right-hand numerator
    double lhd = widthInGrid(vi, c) + widthInGrid(vj, c);
    double rhd = heightInGrid(vi, c) + heightInGrid(vj, c);

    double distance = euclideanDistance(vi, vj) + Math.min(lhn / lhd, rhn / rhd) / 20.0;
    return (int) distance;
  }

  double sumOfDistToNeighbors(V v) {
    return Graphs.neighborSetOf(graph, v)
        .stream()
        .flatMapToDouble(n -> DoubleStream.of(distance(v, n)))
        .sum();
  }

  /**
   * for each neighbor of v, what would the distance be if v were placed in cell?
   *
   * @param v
   * @param cell
   * @return
   */
  double sumOfDistToNeighbors(V v, Point cell) {
    return Graphs.neighborSetOf(graph, v)
        .stream()
        .flatMapToDouble(n -> DoubleStream.of(manhattanDistance(cell, mappings.get(n))))
        .sum();
  }

  double sumOfManhattanDistancesFromVertexToVertices(V source, Collection<V> vertices) {
    return sumOfManhattanDistancesFromPointToPoints(
        mappings.get(source),
        vertices.stream().map(v -> mappings.get(v)).collect(Collectors.toSet()));
  }

  double sumOfManhattanDistancesFromPointToPoints(Point source, Collection<Point> points) {
    return points.stream().mapToDouble(p -> manhattanDistance(source, p)).sum();
  }

  double sumOfDistancesFromPointToPoints(V source, Collection<V> vertices) {
    return vertices.stream().mapToDouble(p -> distance(source, p)).sum();
  }

  int initialGridSize() {
    int vertexCount = graph.vertexSet().size();
    return (int) (50 * Math.sqrt(vertexCount));
  }

  int iterationCount() {
    int vertexCount = graph.vertexSet().size();
    return (int) (90 * Math.sqrt(vertexCount));
  }

  double startingTemperature() {
    return 2 * Math.sqrt(graph.vertexSet().size());
  }

  double k() {
    double T = startingTemperature();
    return Math.pow(0.2 / T, 1.0 / iterationCount());
  }

  Point neighborsMedianPoint(V v) {
    List<Point> points =
        graph.degreeOf(v) > 0
            ?
            // all neighbors
            Graphs.neighborSetOf(graph, v)
                .stream()
                .map(w -> mappings.get(w))
                .collect(Collectors.toList())
            : Collections.emptyList();

    int count = points.size();
    if (count == 0) {
      return mappings.get(v);
    }
    //    return averageOf(points);
    return Point.geometricMedian(points);
  }

  Point neighborsCentroid(V v) {
    return Point.centroidOf(
        Graphs.neighborSetOf(graph, v)
            .stream()
            .map(w -> mappings.get(w))
            .collect(Collectors.toSet()));
  }

  Point averageOf(List<Point> points) {
    IntSummaryStatistics xsum = points.stream().mapToInt(p -> (int) p.x).summaryStatistics();
    IntSummaryStatistics ysum = points.stream().mapToInt(p -> (int) p.y).summaryStatistics();
    return Point.of((int) xsum.getAverage(), (int) ysum.getAverage());
  }

  Point medianOf(List<Point> points) {
    if (points.size() % 2 == 1) {
      int medianIndex = (points.size() - 1) / 2;
      // sort the list by x, get median, sort by y, get median
      points.sort(Comparator.comparingDouble(p -> p.x));
      double xMedian = points.get(medianIndex).x;
      points.sort(Comparator.comparingDouble(p -> p.y));
      double yMedian = points.get(medianIndex).y;
      return Point.of(xMedian, yMedian);
    } else { // even, take avg of middle 2
      int medianIndex = (points.size() / 2);
      // sort the list by x, get median, sort by y, get median
      points.sort(Comparator.comparingDouble(p -> p.x));
      double xMedian = (points.get(medianIndex).x + points.get(medianIndex - 1).x) / 2;
      points.sort(Comparator.comparingDouble(p -> p.y));
      double yMedian = (points.get(medianIndex).y + points.get(medianIndex - 1).y) / 2;
      return Point.of(xMedian, yMedian);
    }
  }

  Graph<Rectangle, Integer> makeCompactionGraph(Collection<Rectangle> cells) {
    Graph<Rectangle, Integer> compactionGraph =
        GraphTypeBuilder.<Rectangle, Integer>directed()
            .allowingMultipleEdges(true)
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    cells.forEach(compactionGraph::addVertex);
    // sort cells by y
    List<Rectangle> sortedByY = new ArrayList<>(cells);
    sortedByY.sort(Comparator.comparingDouble(c -> c.y));
    Set<Rectangle> done = new HashSet<>();
    for (Rectangle cell : sortedByY) {
      if (!done.contains(cell)) {
        List<Rectangle> row = sameY(cell.y, sortedByY);
        connectWithEdges(row, compactionGraph);
        done.addAll(row);
      }
    }
    return compactionGraph;
  }

  void connectWithEdges(List<Rectangle> row, Graph<Rectangle, Integer> graph) {
    for (int i = 0; i < row.size() - 1; i++) {
      graph.addEdge(row.get(i), row.get(i + 1));
    }
  }

  /**
   * return any cells with same 'y', sorted by 'x'
   *
   * @param y
   * @param in
   * @return
   */
  List<Rectangle> sameY(double y, List<Rectangle> in) {
    List<Rectangle> sorted = in.stream().filter(c -> c.y == y).collect(Collectors.toList());
    sorted.sort(Comparator.comparingDouble(c -> c.x));
    return sorted;
  }

  List<Rectangle> overlappingY(int minY, int maxY, List<Rectangle> in) {
    List<Rectangle> overlapping = new ArrayList<>();
    in.forEach(
        cell -> {
          int cellMinY = (int) cell.y;
          int cellMaxY = (int) (cell.y + cell.height);
          if (maxY >= cellMinY && minY <= cellMaxY) {
            overlapping.add(cell);
          }
        });
    // sort overlapping by 'x'
    overlapping.sort(Comparator.comparingDouble(c -> c.x));
    return overlapping;
  }

  /**
   * return any cells with same 'x', sorted by 'y'
   *
   * @param x
   * @param in
   * @return
   */
  List<Rectangle> sameX(double x, List<Rectangle> in) {
    List<Rectangle> sorted = in.stream().filter(c -> c.x == x).collect(Collectors.toList());
    sorted.sort(Comparator.comparingDouble(c -> c.y));
    return sorted;
  }

  List<Rectangle> overlappingX(int minX, int maxX, List<Rectangle> in) {
    List<Rectangle> overlapping = new ArrayList<>();
    in.forEach(
        cell -> {
          int cellMinX = (int) cell.x;
          int cellMaxX = (int) (cell.x + cell.width);
          if (maxX >= cellMinX && minX <= cellMaxX) {
            overlapping.add(cell);
          }
        });
    // sort overlapping by 'y'
    overlapping.sort(Comparator.comparingDouble(c -> c.y));
    return overlapping;
  }
}
