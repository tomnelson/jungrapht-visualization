package org.jungrapht.visualization.layout.algorithms.orthogonal;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
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

public class OrthogonalLayoutAlgorithmThreaded<V, E> extends AbstractLayoutAlgorithm<V>
    implements LayoutAlgorithm<V>, VertexBoundsFunctionConsumer<V> {

  private static final Logger log =
      LoggerFactory.getLogger(OrthogonalLayoutAlgorithmThreaded.class);
  protected static final Rectangle IDENTITY_SHAPE = Rectangle.IDENTITY;

  public static class Builder<
          V, E, T extends OrthogonalLayoutAlgorithmThreaded<V, E>, B extends Builder<V, E, T, B>>
      extends AbstractLayoutAlgorithm.Builder<V, T, B> implements LayoutAlgorithm.Builder<V, T, B> {
    protected Function<V, Rectangle> vertexBoundsFunction = v -> IDENTITY_SHAPE;

    public B vertexBoundsFunction(Function<V, Rectangle> vertexBoundsFunction) {
      this.vertexBoundsFunction = vertexBoundsFunction;
      return self();
    }

    public T build() {
      return (T) new OrthogonalLayoutAlgorithmThreaded(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected OrthogonalLayoutAlgorithmThreaded(Builder builder) {
    super(builder);
    this.realVertexDimensionFunction = builder.vertexBoundsFunction;
  }

  Mappings<V> mappings;

  Function<V, Rectangle> identityVertexDimensionFunction = v -> Rectangle.IDENTITY;
  Function<V, Rectangle> realVertexDimensionFunction;
  Function<V, Rectangle> vertexDimensionFunction;
  int cellSize = 1;
  int iteration;
  int iterationCount;
  V v;
  boolean firstLoop = true;
  boolean secondLoop = false;
  boolean horizontalCompaction = true;
  boolean verticalCompaction = false;
  boolean done = false;

  List<V> vertexList;
  int vertexIndex = 0; // index into vertexList
  Compaction.Direction compactionDirection = Compaction.Direction.HORIZONTAL;
  double temperature;
  double k;

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    this.mappings = new Mappings<>();
    this.layoutModel = layoutModel;
    this.graph = layoutModel.getGraph();
    int gridSize = this.initialGridSize();
    log.info("initialGridSize: {}", gridSize);
    layoutModel.setSize(600, 600);
    int vertexCount = graph.vertexSet().size();
    this.vertexList = new ArrayList<>(graph.vertexSet());
    this.placeVerticesRandomlyInGridSpace(graph, gridSize);
    //    printGrid();
    Compaction.Direction compactionDirection = Compaction.Direction.HORIZONTAL;
    double sqrtVertexCount = Math.sqrt(vertexCount);
    this.iterationCount = (int) (90 * sqrtVertexCount);
    this.temperature = 2 * sqrtVertexCount;
    //        k=(0.2/T)1/iterationCount ;
    this.k = Math.pow(0.2 / temperature, 1.0 / iterationCount);

    vertexDimensionFunction = identityVertexDimensionFunction;

    this.iteration = 0;

    Thread thread =
        new Thread() {
          public void run() {
            //            try {
            //              Thread.sleep(5000);
            //            } catch (InterruptedException ex) {
            //            }
            layoutModel.getLayoutStateChangeSupport().fireLayoutStateChanged(layoutModel, true);
            while (!done) {
              //              log.info("step");
              step();
              setPoints();
              //          layoutModel.getModelChangeSupport().fireModelChanged();
              //              try {
              //                Thread.sleep(2);
              //              } catch (InterruptedException ex) {
              //              }
            }
            layoutModel.getLayoutStateChangeSupport().fireLayoutStateChanged(layoutModel, false);
          }
        };
    thread.start();
  }

  private void setPoints() {
    for (V v : graph.vertexSet()) {
      Rectangle r = mappings.get(v);
      layoutModel.set(v, r.min());
    }
    centerIt(layoutModel, Collections.emptyList());
  }

  public void step() {
    // first loop is until iteration >= iterationCount/2
    if (done) {
      for (V v : graph.vertexSet()) {
        Rectangle r = mappings.get(v);
        layoutModel.set(v, r.min());
      }
      centerIt(layoutModel, Collections.emptyList());
      return;
    }
    if (this.firstLoop && iteration < iterationCount / 2) {
      if (vertexIndex < vertexList.size()) {
        this.v = vertexList.get(vertexIndex);
        this.insideLoopOne();
        vertexIndex++;
        return;
      } else {
        if (iteration % 9 == 0) {
          compact(compactionDirection, 3, false);
          compactionDirection = compactionDirection.toggle();
        }
        vertexIndex = 0;
        iteration++;
        this.temperature *= k;
        return;
      }

    } else if (horizontalCompaction) {
      // iteration has reached iterationCount/2

      // do the compactions
      compact(Compaction.Direction.HORIZONTAL, 3, true);
      horizontalCompaction = false;
      verticalCompaction = true;
      return;
    } else if (verticalCompaction) {

      compact(Compaction.Direction.VERTICAL, 3, true);
      this.verticalCompaction = false;
      // ready for secondLoop
      this.firstLoop = false;
      this.secondLoop = true;
      this.vertexDimensionFunction = this.realVertexDimensionFunction;

      // vertexIndex should be 0
      // iteration should be iterationCount / 2
      return;
    }

    if (this.secondLoop && iteration < iterationCount) {
      if (vertexIndex < vertexList.size()) {
        this.v = vertexList.get(vertexIndex);
        this.insideLoopTwo();
        vertexIndex++;
        return;
      } else {
        if (iterationCount % 9 == 0) {
          compact(
              compactionDirection,
              Math.max(1, 1 + 2 * (iterationCount - iteration - 30) / 0.5 * iterationCount),
              false);
          this.compactionDirection = compactionDirection.toggle();
        }
        vertexIndex = 0;
        iteration++;
        this.temperature *= k;
        return;
      }

    } else {
      // done with iteration loop
      this.secondLoop = false;
      this.done = true;
    }
  }

  private void insideLoopOne() {
    Point neighborsMedian =
        neighborsMedianPoint(v)
            .add(randomTemp(-temperature, temperature), randomTemp(-temperature, temperature));
    int x = (int) neighborsMedian.x;
    int y = (int) neighborsMedian.y;
    // see if the cell at x,y is free
    Rectangle rectangle = Rectangle.of(x, y, cellSize, cellSize);
    Set<Rectangle> occupiedRectangles = occupiedRectangles();
    if (occupiedRectangles.contains(rectangle)) {
      // need a different cell
      Collection<Rectangle> potentialCells =
          this.findNearbyEmptyCells(rectangle, cellSize);
      // find which one has the least edge len to neighbors
      double min = Double.MAX_VALUE;
      Rectangle winner = null;
      for (Rectangle potential : potentialCells) {
        double sum = sumOfDistToNeighbors(v, potential);
        if (sum < min) {
          min = sum;
          winner = potential;
        }
      }
      if (winner == rectangle) {
        // try to swap with nearby cell
        Rectangle closest = closestTo(rectangle, occupiedRectangles);
        // the vertex in the closest cell
        V closestCellVertex = mappings.get(closest);
        mappings.accept(closestCellVertex, rectangle);
        mappings.accept(v, closest);

      } else if (winner != null) {
        mappings.accept(v, winner);
      } else {
        log.error("no winner");
      }
    } else {
      mappings.accept(v, rectangle);
    }
  }

  private void insideLoopTwo() {
    Rectangle vd = vertexDimensionFunction.apply(v);
    Point neighborsMedian =
        neighborsMedianPoint(v)
            .add(
                randomTemp(-temperature * vd.width, temperature * vd.width),
                randomTemp(-temperature * vd.height, temperature * vd.height));
    int x = (int) neighborsMedian.x;
    int y = (int) neighborsMedian.y;
    // see if the cell at x,y is free
    Rectangle rectangle = Rectangle.of(x, y, cellSize, cellSize);
    if (!mappings.empty(rectangle)) {
//        // need a different cell
      Collection<Rectangle> potentialCells =
          this.findNearbyEmptyCells(rectangle, cellSize);
      // find which one has the least edge len to neighbors
      double min = Double.MAX_VALUE;
      Rectangle winner = null;
      for (Rectangle potential : potentialCells) {
        double sum = sumOfDistToNeighbors(v, potential);
        if (sum < min) {
          min = sum;
          winner = potential;
        }
      }
      if (winner == rectangle) {
        // try to swap with nearby cell
        Rectangle closest = closestTo(rectangle, potentialCells);
        // the vertex in the closest cell
        V closestCellVertex = mappings.get(closest);
        mappings.accept(closestCellVertex, rectangle);
        mappings.accept(v, closest);
      } else if (winner != null) {
        mappings.accept(v, winner);
      } else {
        log.error("no winner");
      }
    } else {
      mappings.accept(v, rectangle);
    }
  }

  private void printGrid() {
    System.err.println("-------");
    // imagine a grid that is width / height
    PointSummaryStatistics ps = new PointSummaryStatistics();
    Collection<Point> locations =
        graph.vertexSet().stream().map(v -> mappings.get(v).min()).collect(Collectors.toSet());
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

  protected void centerIt(LayoutModel<V> layoutModel, Collection<List<Point>> articulations) {
    Rectangle extent = Expansion.computeLayoutExtent2(layoutModel, articulations);
    // width of extent
    double widthOfExtent = extent.width;
    double heightOfExtent = extent.height;
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

  protected Rectangle computeLayoutExtent(Collection<Point> points) {
    // find the dimensions of the layout
    PointSummaryStatistics pss = new PointSummaryStatistics();
    points.forEach(pss::accept);
    return Rectangle.from(pss.getMin(), pss.getMax());
  }

  //  void updateMaps(V v, Rectangle r) {
  //    this.rectangleToVertexMap.remove(vertexToRectangleMap.get(v));
  //    this.vertexToRectangleMap.put(v, r);
  //    this.rectangleToVertexMap.put(r, v);
  //  }

  private void compact(Compaction.Direction direction, double gamma, boolean expand) {
    //return;
    log.info("compact with gamma:{}", gamma);
    List<Cell<V>> cells = new ArrayList<>();
    graph
        .vertexSet()
        .forEach(
            v -> {
              Rectangle r = mappings.get(v);
              cells.add(Cell.of(v, r.x, r.y, r.width, r.height));
            });
    Compaction.of(cells, direction, gamma, mappings::accept);

    log.info("mappings are {}", mappings.getVertexToRectangleMap());
    log.info("cells are {}", cells);
    if (expand) {
      Expansion.expandToFillBothAxes(
              Rectangle.of(0, 0, layoutModel.getWidth(), layoutModel.getHeight()), mappings);
    }
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

  private double randomTemp(double lower, double upper) {
    return Math.random() * (upper - lower) + lower;
  }

  private void placeVerticesRandomlyInGridSpace(Graph<V, E> graph, int gridSize) {
    //        Map<V, Rectangle> cells = new HashSet<>();
    // populate grid with random vertices
    graph
        .vertexSet()
        .forEach(
            v -> {
              // random choice of grid cell
              int x = (int) (Math.random() * gridSize);
              int y = (int) (Math.random() * gridSize);

              Rectangle rectangle = Rectangle.of(x, y, 1, 1);
              // no dupes
              while (mappings.rectangles().contains(rectangle)) {
                x = (int) (Math.random() * gridSize);
                y = (int) (Math.random() * gridSize);
                rectangle = Rectangle.of(x, y, 1, 1);
              }
              mappings.accept(v, rectangle);
              //            cells.put(v, cell);
              //            cell.setOccupant(v);
              //            cells.add(cell);
              //            whereAreTheNodes.put(v, cell);
            });
    //        return cells;
  }

  List<Rectangle> neighborsOf(Rectangle cell, int dist) {
    int x = (int) cell.x;
    int y = (int) cell.y;
    List<Rectangle> list = new ArrayList<>();
    // start above, CW around all neighbors
    list.add(Rectangle.of(x, y - dist, 1, 1));
    list.add(Rectangle.of(x + dist, y - dist, 1, 1));
    list.add(Rectangle.of(x + dist, y, 1, 1));
    list.add(Rectangle.of(x + dist, y + dist, 1, 1));
    list.add(Rectangle.of(x, y + dist, 1, 1));
    list.add(Rectangle.of(x - dist, y + dist, 1, 1));
    list.add(Rectangle.of(x - dist, y, 1, 1));
    list.add(Rectangle.of(x - dist, y - dist, 1, 1));
    return list;
  }

  Collection<Rectangle> findNearbyEmptyCells(
      Rectangle cell, int dist) {
    // remove any neighbors whose Rectangles are already occupied
    List<Rectangle> neighbors = // neighbors that are empty
        neighborsOf(cell, dist)
            .stream()
                .filter(c -> !mappings.empty(c))
//            .filter(c -> !occupied.contains(c))
            .collect(Collectors.toList());
    if (!neighbors.isEmpty()) {
      return neighbors;
    } else {
      return findNearbyEmptyCells(cell, ++dist);
    }
  }

  List<Rectangle> neighborsOf(Rectangle r) {
    return neighborsOf(r, 1);
  }

  Set<Rectangle> occupiedRectangles() {
    return mappings.rectangles().stream().collect(Collectors.toSet());
  }

  @Override
  public void setVertexBoundsFunction(Function<V, Rectangle> vertexBoundsFunction) {
    this.vertexDimensionFunction = vertexBoundsFunction;
  }

  Graph<V, E> graph;
  int delta;
  LayoutModel<V> layoutModel;
  double TMin = 0.2;
  //  Grid grid;
  //    Function<V, Point> upperLeftCornerFunction =
  //            v ->
  //                    layoutModel
  //                            .apply(v)
  //                            .add(
  //                                    -vertexDimensionFunction.apply(v).width / 2,
  //                                    -vertexDimensionFunction.apply(v).height / 2);

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
  int computeGridCell() {
    DimensionSummaryStatistics dss = prepare();
    int elMax = computeElMax(dss);
    int elMin = computeElMin(dss);
    if (elMax < 3 * elMin) {
      return elMax;
    } else if (elMax <= 15 * elMin) {
      return (3 * elMin) / 2;
    } else {
      return elMax / 30;
    }
  }

  // w'(i)
  int widthInGrid(V v, int c) {
    return (int) Math.ceil((vertexDimensionFunction.apply(v).width + delta) / c);
  }

  // h'(i)
  int heightInGrid(V v, int c) {
    return (int) Math.ceil((vertexDimensionFunction.apply(v).height + delta) / c);
  }

  int euclideanDistance(V v1, V v2) {
    double w1 = vertexDimensionFunction.apply(v1).width;
    double w2 = vertexDimensionFunction.apply(v2).width;
    double h1 = vertexDimensionFunction.apply(v1).height;
    double h2 = vertexDimensionFunction.apply(v2).height;
    Point p1 = locationModel.apply(v1).add(-w1 / 2, -h1 / 2);
    Point p2 = locationModel.apply(v2).add(-w2 / 2, -h2 / 2);
    Rectangle r1 = Rectangle.of(p1.x, p1.y, w1, h1);
    Rectangle r2 = Rectangle.of(p2.x, p2.y, w2, h2);
    return euclideanDistance(r1, r2);
  }

  Function<V, Point> locationModel =
      v -> {
        Rectangle cell = mappings.get(v);
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

  Point vcp(V v) {
    // find the upper-left for the Rectangle containing v

    Rectangle r = mappings.get(v);
    return Point.of(r.x, r.y);
    //        Optional<Cell<V>> opt = occupiedCells.stream()
    //                .filter(c -> c.getOccupant() == v).findAny();
    ////                grid.getRectangleContaining(v);
    //        if (opt.isPresent() && opt.get() != null) {
    //            Cell<V> cell = opt.get();
    //            return Point.of(cell.getRectangle().x, cell.getRectangle().y);
    //        }
    //        return Point.ORIGIN;
  }

  /**
   * return the center of the Shape for v
   *
   * @param v
   * @return
   */
  int xci(V v) {
    // find the upper-left for the Rectangle containing v
    return (int) vcp(v).x;
  }

  /**
   * return the center y of the Shape for v
   *
   * @param v
   * @return
   */
  int yci(V v) {
    return (int) vcp(v).y;
  }

  //                           1          |xci - xcj|   |yci - ycj|
  // d(vi, vj) = de(vi, vj) + ----  min ( ----------- , ----------- )
  //                           20          w'i + w'j     h'i + h'j

  int distance(V vi, V vj) {
    int c = computeGridCell();
    Point vcpi = vcp(vi);
    Point vcpj = vcp(vj);
    double lhn = Math.abs(vcpi.x - vcpj.x); // left-hand numerator
    double rhn = Math.abs(vcpi.y - vcpj.y); // right-hand numerator
    double lhd = widthInGrid(vi, c) + widthInGrid(vj, c);
    double rhd = heightInGrid(vi, c) + heightInGrid(vj, c);

    double distance = euclideanDistance(vi, vj) + Math.min(lhn / lhd, rhn / rhd) / 20.0;
    return (int) distance;
  }

  int distance(Rectangle cell, V vi, V vj) {
    int c = computeGridCell();
    double lhn = Math.abs(cell.x - xci(vj)); // left-hand numerator
    double rhn = Math.abs(cell.y - yci(vj)); // right-hand numerator
    double lhd = widthInGrid(vi, c) + widthInGrid(vj, c);
    double rhd = heightInGrid(vi, c) + heightInGrid(vj, c);

    double distance = euclideanDistance(vi, vj) + Math.min(lhn / lhd, rhn / rhd) / 20.0;
    return (int) distance;
  }

  int distance(Rectangle cell, Rectangle neighborCell) {
    return (int) (Math.abs(cell.x - neighborCell.x) + Math.abs(cell.y - neighborCell.y));
  }

  double sumOfDistToNeighbors(V v) {
    return Graphs.neighborSetOf(graph, v)
        .stream()
        .flatMapToDouble(n -> DoubleStream.of(distance(v, n)))
        .sum();
  }

  double sumOfDistToNeighbors(V v, Rectangle cell) {
    return Graphs.neighborSetOf(graph, v)
        .stream()
        .flatMapToDouble(n -> DoubleStream.of(distance(cell, mappings.get(n))))
        .sum();
  }

  int initialGridSize() {
    int vertexCount = graph.vertexSet().size();
    return (int) (5 * Math.sqrt(vertexCount));
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
                .map(ce -> Point.of(ce.x, ce.y))
                .collect(Collectors.toList())
            :
            // all other vertices
            graph
                .vertexSet()
                .stream()
                .filter(vertex -> vertex != v)
                .map(w -> mappings.get(w))
                .map(ce -> Point.of(ce.x, ce.y))
                .collect(Collectors.toList());

    int count = points.size();
    int medianIndex = (count - 1) / 2;
    // sort the list by x, get median, sort by y, get median
    points.sort(Comparator.comparingDouble(p -> p.x));
    int xMedian = (int) points.get(medianIndex).x;
    points.sort(Comparator.comparingDouble(p -> p.y));
    int yMedian = (int) points.get(medianIndex).y;
    return Point.of(xMedian, yMedian);
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
