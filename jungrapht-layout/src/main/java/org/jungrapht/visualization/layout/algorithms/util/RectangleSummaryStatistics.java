package org.jungrapht.visualization.layout.algorithms.util;

import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;

import java.util.DoubleSummaryStatistics;

public class RectangleSummaryStatistics implements RectangleConsumer {

  private DoubleSummaryStatistics xValues;
  private DoubleSummaryStatistics yValues;

  public RectangleSummaryStatistics() {
    xValues = new DoubleSummaryStatistics();
    yValues = new DoubleSummaryStatistics();
  }

  public RectangleSummaryStatistics(int count, int min, int max, int sum)
      throws IllegalArgumentException {
    xValues = new DoubleSummaryStatistics(count, min, max, sum);
    yValues = new DoubleSummaryStatistics(count, min, max, sum);
  }

  public void accept(Rectangle r) {
    xValues.accept(r.x);
    xValues.accept(r.maxX);
    yValues.accept(r.y);
    yValues.accept(r.maxY);
  }

  public void combine(RectangleSummaryStatistics other) {
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
    return "RectangleSummaryStatistics{" + "xValues=" + xValues + ", heights=" + yValues + '}';
  }
}
