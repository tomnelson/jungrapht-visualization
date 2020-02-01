package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Arrays;
import java.util.Comparator;
import org.jungrapht.visualization.layout.model.Point;

public class AverageMedian<V> {

  private static int comparePoints(Point p1, Point p2) {
    return Double.compare(p1.x, p2.x);
  }

  public static Point averageMedianPoint(Point... points) {
    Arrays.sort(points, Comparator.comparingDouble(p -> p.x));
    int n = points.length;
    int floor = (int) Math.floor((n - 1) / 2.0);
    int ceil = (int) Math.ceil((n - 1) / 2.0);
    double avgx = (points[floor].x + points[ceil].x) / 2.0;
    double avgy = (points[floor].y + points[ceil].y) / 2.0;
    return Point.of(avgx, avgy);
  }

  public static Point medianPoint(Point... points) {
    Arrays.sort(points, Comparator.comparingDouble(p -> p.x));
    int n = points.length;
    int floor = (int) Math.floor((n - 1) / 2.0);
    double avgx = points[floor].x;
    double avgy = points[floor].y;
    return Point.of(avgx, avgy);
  }

  public static Point averagePoint(Point... points) {
    double avgx = Arrays.stream(points).mapToDouble(p -> p.x).average().getAsDouble();
    double avgy = Arrays.stream(points).mapToDouble(p -> p.y).average().getAsDouble();
    return Point.of(avgx, avgy);
  }
}
