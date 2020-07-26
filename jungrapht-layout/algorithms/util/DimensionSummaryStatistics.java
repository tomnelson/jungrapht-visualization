package org.jungrapht.visualization.layout.algorithms.util;

import java.util.IntSummaryStatistics;
import org.jungrapht.visualization.layout.model.Dimension;
import org.jungrapht.visualization.layout.model.Rectangle;

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
    widths.accept((int) rectangle.width);
    heights.accept((int) rectangle.height);
  }

  public void combine(DimensionSummaryStatistics other) {
    widths.combine(other.widths);
    heights.combine(other.heights);
  }

  public final long getCount() {
    return this.widths.getCount();
  }

  public final Dimension getSum() {
    return Dimension.of((int) widths.getSum(), (int) heights.getSum());
  }

  public final Dimension getMin() {
    return Dimension.of(widths.getMin(), heights.getMin());
  }

  public final Dimension getMax() {
    return Dimension.of(widths.getMax(), heights.getMax());
  }

  public final Dimension getAverage() {
    return Dimension.of((int) widths.getAverage(), (int) heights.getAverage());
  }

  @Override
  public String toString() {
    return "DimensionSummaryStatistics{" + "widths=" + widths + ", heights=" + heights + '}';
  }
}
