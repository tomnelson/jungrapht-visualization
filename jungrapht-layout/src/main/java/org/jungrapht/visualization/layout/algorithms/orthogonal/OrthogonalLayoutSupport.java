package org.jungrapht.visualization.layout.algorithms.orthogonal;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.util.DimensionSummaryStatistics;
import org.jungrapht.visualization.layout.model.Dimension;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;

public class OrthogonalLayoutSupport<V, E> {

  Graph<V, E> graph;
  Function<V, Rectangle> vertexDimensionFunction;
  int delta;
  LayoutModel<V> layoutModel;
  double TMin = 0.2;
  Function<V, Point> upperLeftCornerFunction =
      v ->
          layoutModel
              .apply(v)
              .add(
                  -vertexDimensionFunction.apply(v).width / 2,
                  -vertexDimensionFunction.apply(v).height / 2);

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
    Point p1 = layoutModel.apply(v1).add(-w1 / 2, -h1 / 2);
    Point p2 = layoutModel.apply(v2).add(-w2 / 2, -h2 / 2);
    Rectangle r1 = Rectangle.of(p1.x, p1.y, w1, h1);
    Rectangle r2 = Rectangle.of(p2.x, p2.y, w2, h2);
    return euclideanDistance(r1, r2);
  }

  // dsube(v(i), v(j))
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

  int xci(V v) {
    return (int) layoutModel.apply(v).x; // the center x of v's rectangle
  }

  int yci(V v) {
    return (int) layoutModel.apply(v).y; // the center y of v's rectangle
  }

  int distance(V vi, V vj) {
    int c = computeGridCell();
    double lhn = Math.abs(xci(vi) - xci(vj)); // left-hand numerator
    double rhn = Math.abs(yci(vi) - yci(vj)); // right-hand numerator
    double lhd = widthInGrid(vi, c) + widthInGrid(vj, c);
    double rhd = heightInGrid(vi, c) + heightInGrid(vj, c);

    double distance = euclideanDistance(vi, vj) + Math.min(lhn / lhd, rhn / rhd) / 20.0;
    return (int) distance;
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

  Point neighborsMedianXY(V v) {
    List<Point> points =
        Graphs.neighborSetOf(graph, v)
            .stream()
            .map(layoutModel::apply)
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
}
