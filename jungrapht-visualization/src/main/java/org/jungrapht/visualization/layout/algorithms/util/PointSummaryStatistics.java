package org.jungrapht.visualization.layout.algorithms.util;

import java.util.DoubleSummaryStatistics;
import org.jungrapht.visualization.layout.model.Point;

public class PointSummaryStatistics implements PointConsumer {

  private DoubleSummaryStatistics xValues;
  private DoubleSummaryStatistics yValues;

  public PointSummaryStatistics() {
    xValues = new DoubleSummaryStatistics();
    yValues = new DoubleSummaryStatistics();
  }

  public PointSummaryStatistics(int count, int min, int max, int sum)
      throws IllegalArgumentException {
    xValues = new DoubleSummaryStatistics(count, min, max, sum);
    yValues = new DoubleSummaryStatistics(count, min, max, sum);
  }

  public void accept(Point p) {
    xValues.accept(p.x);
    yValues.accept(p.y);
  }

  public void combine(PointSummaryStatistics other) {
    xValues.combine(other.xValues);
    yValues.combine(other.yValues);
  }

  public final long getCount() {
    return this.xValues.getCount();
  }

  public final Point getSum() {
    return Point.of((int) xValues.getSum(), (int) yValues.getSum());
  }

  public final Point getMin() {
    return Point.of(xValues.getMin(), yValues.getMin());
  }

  public final Point getMax() {
    return Point.of(xValues.getMax(), yValues.getMax());
  }

  public final Point getAverage() {
    return Point.of((int) xValues.getAverage(), (int) yValues.getAverage());
  }

  @Override
  public String toString() {
    return "DimensionSummaryStatistics{" + "xValues=" + xValues + ", heights=" + yValues + '}';
  }
}
