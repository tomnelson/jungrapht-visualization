package org.jungrapht.visualization.layout.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.orthogonal.Mappings;
import org.jungrapht.visualization.layout.algorithms.util.PointSummaryStatistics;
import org.jungrapht.visualization.layout.algorithms.util.RectangleSummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Expansion {

  private static Logger log = LoggerFactory.getLogger(Expansion.class);
  /**
   * Uses all of the points in the {@code LayoutModel} plus any additional points (for example,
   * articulated edge points) in the locations Collection
   *
   * @param layoutModel - holds vertex locations
   * @param locations - holds additional locations to consider
   * @param <V> vertex type
   * @return a {@code Rectanele} holding all provided {@code Point}s
   */
  public static <V> Rectangle computeLayoutExtent(
      LayoutModel<V> layoutModel, Collection<Point> locations) {
    if (layoutModel.getLocations().size() == 0) {
      return Rectangle.from(Point.ORIGIN, Point.ORIGIN);
    }
    // find the dimensions of the layout
    PointSummaryStatistics pss = new PointSummaryStatistics();
    layoutModel.getLocations().values().forEach(pss::accept);
    locations.forEach(pss::accept);
    return Rectangle.from(pss.getMin(), pss.getMax());
  }

  public static <V> Rectangle computeRectanglesExtent(Collection<Rectangle> rectangles) {
    if (rectangles.size() == 0) {
      return Rectangle.from(Point.ORIGIN, Point.ORIGIN);
    }
    // find the dimensions of the layout
    RectangleSummaryStatistics pss = new RectangleSummaryStatistics();
    rectangles.forEach(pss::accept);
    return Rectangle.from(pss.getMin(), pss.getMax());
  }

  public static <V> Rectangle computePointsExtent(Collection<Point> points) {
    if (points.size() == 0) {
      return Rectangle.from(Point.ORIGIN, Point.ORIGIN);
    }
    // find the dimensions of the layout
    PointSummaryStatistics pss = new PointSummaryStatistics();
    points.forEach(pss::accept);
    return Rectangle.from(pss.getMin(), pss.getMax());
  }

  public static <V> Rectangle computeLayoutExtent2(
      LayoutModel<V> layoutModel, Collection<List<Point>> locations) {
    return computeLayoutExtent(
        layoutModel, locations.stream().flatMap(List::stream).collect(Collectors.toList()));
  }

  public static <V> Rectangle computeLayoutExtent(LayoutModel<V> layoutModel) {
    return computeLayoutExtent(layoutModel, Collections.emptySet());
  }

  public static <V> void expandToFill(LayoutModel<V> layoutModel) {
    expandToFill(layoutModel, Collections.emptySet());
  }

  /** @param layoutModel */
  public static <V> void expandToFill(LayoutModel<V> layoutModel, Collection<Point> locations) {

    // find the dimensions of the layout's occupied area
    Rectangle vertexContainingRectangle = computeLayoutExtent(layoutModel, locations);

    int maxDimension =
        Math.max((int) vertexContainingRectangle.width, (int) vertexContainingRectangle.height);
    layoutModel.setSize(maxDimension, maxDimension);

    expandToFill(layoutModel, vertexContainingRectangle);
  }

  /**
   * @param layoutModel
   * @param occupiedRegion
   */
  public static <V, E> void expandToFill(LayoutModel<V> layoutModel, Rectangle occupiedRegion) {
    int regionX = (int) occupiedRegion.x;
    int regionY = (int) occupiedRegion.y;
    int regionWidth = (int) occupiedRegion.width;
    int regionHeight = (int) occupiedRegion.height;
    if (regionWidth > regionHeight) {
      double expansion = (double) regionWidth / regionHeight;
      Graph<V, E> graph = layoutModel.getGraph();
      graph
          .vertexSet()
          .stream()
          .filter(v -> !layoutModel.isLocked(v))
          .forEach(
              v -> {
                Point p = layoutModel.get(v);
                p = Point.of(p.x, expansion * (p.y - regionY));
                layoutModel.set(v, p);
              });
    } else if (regionWidth < regionHeight) {
      double expansion = (double) regionHeight / regionWidth;
      Graph<V, ?> graph = layoutModel.getGraph();
      graph
          .vertexSet()
          .stream()
          .filter(v -> !layoutModel.isLocked(v))
          .forEach(
              v -> {
                Point p = layoutModel.get(v);
                p = Point.of(expansion * (p.x - regionX), p.y);
                layoutModel.set(v, p);
              });
    }
  }

  public static <V, E> void expandToFillBothAxes(Rectangle layoutRectangle, Mappings<V> mappings) {

    // find the Rectangle that contains all mapping Rectangles
    Rectangle mappingRectangle = Expansion.computeRectanglesExtent(mappings.rectangles());
    if (mappingRectangle.width > layoutRectangle.width
        || mappingRectangle.height > layoutRectangle.height) {
      layoutRectangle =
          Rectangle.of(
              0,
              0,
              mappingRectangle.width > layoutRectangle.width
                  ? mappingRectangle.width
                  : layoutRectangle.width,
              mappingRectangle.height > layoutRectangle.height
                  ? mappingRectangle.height
                  : layoutRectangle.height);
    }

    double horizontalExpansion = layoutRectangle.width / mappingRectangle.width;
    double verticalExpansion = layoutRectangle.height / mappingRectangle.height;
    double horizontalDelta = layoutRectangle.x - mappingRectangle.x;
    double verticalDelta = layoutRectangle.y - mappingRectangle.y;

    // move each mapping
    for (V v : mappings.vertices().stream().collect(Collectors.toList())) {
      Rectangle r = mappings.get(v);
      Rectangle newr =
          Rectangle.of(
              (r.x + horizontalDelta) * horizontalExpansion,
              (r.y + verticalDelta) * verticalExpansion,
              r.width,
              r.height);
      mappings.accept(v, newr);
      log.info("updated for {} from {} to {}", v, r.min(), newr.min());
    }

    //    Expansion.expandToFillBothAxes(layoutModel,
    //            mappings.rectangles().stream().map(Rectangle::min).collect(Collectors.toSet()));
    //    Graph<V, E> graph = layoutModel.getGraph();
    //    graph
    //            .vertexSet()
    //            .stream()
    //            .filter(v -> !layoutModel.isLocked(v))
    //            .forEach(
    //                    v -> {
    //                      Point p = layoutModel.get(v);
    //                       mappings.update(v, Rectangle.of(p.x, p.y, 1, 1));
    //                    });

  }

  public static <V> void expandToFillBothAxes(
      LayoutModel<V> layoutModel, Collection<Point> locations) {

    // find the dimensions of the layout's occupied area
    Rectangle vertexContainingRectangle = computeLayoutExtent(layoutModel, locations);
    log.info("vertexContainingRectangle: {}", vertexContainingRectangle);

    expandToFillBothAxes(layoutModel, vertexContainingRectangle);
    log.info("filled locations:\n{}", layoutModel.getLocations());
  }

  public static <V, E> void expandToFillBothAxes(
      LayoutModel<V> layoutModel, Rectangle occupiedRegion) {
    int regionX = (int) occupiedRegion.x;
    int regionY = (int) occupiedRegion.y;
    int regionWidth = (int) occupiedRegion.width;
    int regionHeight = (int) occupiedRegion.height;
    double horizontalExpansion = (double) layoutModel.getWidth() / regionWidth;
    double verticalExpansion = (double) layoutModel.getHeight() / regionHeight;
    log.info("horizontalExpansion: {}", horizontalExpansion);
    log.info("verticalExpansion: {}", verticalExpansion);
    Graph<V, E> graph = layoutModel.getGraph();
    graph
        .vertexSet()
        .stream()
        .filter(v -> !layoutModel.isLocked(v))
        .forEach(
            v -> {
              Point p = layoutModel.get(v);
              p =
                  Point.of(
                      horizontalExpansion * (p.x - regionX), verticalExpansion * (p.y - regionY));
              layoutModel.set(v, p);
            });
  }
}
