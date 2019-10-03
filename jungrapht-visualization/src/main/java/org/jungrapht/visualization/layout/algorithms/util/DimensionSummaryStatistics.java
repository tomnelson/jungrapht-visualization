package org.jungrapht.visualization.layout.algorithms.util;

import java.awt.*;
import java.util.IntSummaryStatistics;

public class DimensionSummaryStatistics implements DimensionConsumer, RectangleConsumer {

  private IntSummaryStatistics widths;
  private IntSummaryStatistics heights;

  public DimensionSummaryStatistics() {
    widths = new IntSummaryStatistics();
    heights = new IntSummaryStatistics();
  }

  public DimensionSummaryStatistics(int count, int min, int max, int sum)
      throws IllegalArgumentException {
    widths = new IntSummaryStatistics(count, min, max, sum);
    heights = new IntSummaryStatistics(count, min, max, sum);
  }

  public void accept(Dimension dimension) {
    widths.accept(dimension.width);
    heights.accept(dimension.height);
  }

  public void accept(Rectangle rectangle) {
    widths.accept(rectangle.width);
    heights.accept(rectangle.height);
  }

  public void combine(DimensionSummaryStatistics other) {
    widths.combine(other.widths);
    heights.combine(other.heights);
  }

  public final long getCount() {
    return this.widths.getCount();
  }

  public final Dimension getSum() {
    return new Dimension((int) widths.getSum(), (int) heights.getSum());
  }

  public final Dimension getMin() {
    return new Dimension(widths.getMin(), heights.getMin());
  }

  public final Dimension getMax() {
    return new Dimension(widths.getMax(), heights.getMax());
  }

  public final Dimension getAverage() {
    return new Dimension((int) widths.getAverage(), (int) heights.getAverage());
  }

  @Override
  public String toString() {
    return "DimensionSummaryStatistics{" + "widths=" + widths + ", heights=" + heights + '}';
  }
}
