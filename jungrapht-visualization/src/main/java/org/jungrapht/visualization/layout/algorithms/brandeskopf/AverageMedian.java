package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaVertex;
import org.jungrapht.visualization.layout.model.Point;

public class AverageMedian<V> {

  List<List<SugiyamaVertex<V>>> ur;
  List<List<SugiyamaVertex<V>>> lr;
  List<List<SugiyamaVertex<V>>> ul;
  List<List<SugiyamaVertex<V>>> ll;

  List<List<SugiyamaVertex<V>>> balanced;

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

  public void foo() {
    for (int i = 0; i < ur.size(); i++) { // rank counter
      List<SugiyamaVertex<V>> rank = ur.get(i);
      balanced.add(new ArrayList<>());
      for (int j = 0; j < rank.size(); j++) {
        List<Point> list =
            List.of(
                ur.get(i).get(j).getPoint(),
                lr.get(i).get(j).getPoint(),
                ul.get(i).get(j).getPoint(),
                ll.get(i).get(j).getPoint());
        list.sort(AverageMedian::comparePoints);
        double y = list.get(0).y;
        int n = list.size();
        int floor = (int) Math.floor((n - 1) / 2.0);
        int ceil = (int) Math.ceil((n - 1) / 2.0);
        double aveMedian = (list.get(floor).x + list.get(ceil).x) / 2.0;
        Point p = Point.of(aveMedian, y);
        ur.get(i).get(j).setPoint(p);
        lr.get(i).get(j).setPoint(p);
        ul.get(i).get(j).setPoint(p);
        ll.get(i).get(j).setPoint(p);
      }
    }
  }
}
