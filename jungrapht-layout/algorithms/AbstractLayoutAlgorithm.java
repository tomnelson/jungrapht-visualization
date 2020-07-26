package org.jungrapht.visualization.layout.algorithms;

import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.util.DimensionSummaryStatistics;
import org.jungrapht.visualization.layout.algorithms.util.PointSummaryStatistics;
import org.jungrapht.visualization.layout.model.Dimension;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;

/**
 * For Layout algorithms that can run an 'after' function
 *
 * @author Tom Nelson
 */
public abstract class AbstractLayoutAlgorithm<V> implements LayoutAlgorithm<V> {

  public abstract static class Builder<
          V, T extends AbstractLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected Runnable after = () -> {};

    public B after(Runnable after) {
      this.after = after;
      return self();
    }

    protected B self() {
      return (B) this;
    }

    public abstract T build();
  }

  protected AbstractLayoutAlgorithm(Builder builder) {
    this.after = builder.after;
  }

  public void setAfter(Runnable after) {
    this.after = after;
  }

  public void runAfter() {
    if (after != null) after.run();
  }

  protected Runnable after;

  protected <E> Dimension computeAverageVertexDimension(
      Graph<V, E> graph, Function<V, Rectangle> shapeFunction) {
    DimensionSummaryStatistics dss = new DimensionSummaryStatistics();
    graph.vertexSet().stream().map(vertex -> shapeFunction.apply(vertex)).forEach(dss::accept);
    return dss.getAverage();
  }

  /**
   * Compute the smallest Rectangle in the layout area containing vertices
   *
   * @param layoutModel
   * @return
   */
  protected Rectangle computeLayoutExtent(LayoutModel<V> layoutModel) {
    // find the dimensions of the layout
    PointSummaryStatistics pss = new PointSummaryStatistics();
    layoutModel.getLocations().values().forEach(pss::accept);
    return Rectangle.from(pss.getMin(), pss.getMax());
  }

  /**
   * @param layoutModel
   * @param occupiedRegion
   */
  protected void expandToFill(LayoutModel<V> layoutModel, Rectangle occupiedRegion) {
    int regionWidth = (int) occupiedRegion.width;
    int regionHeight = (int) occupiedRegion.height;
    if (regionWidth > regionHeight) {
      double expansion = (double) regionWidth / regionHeight;
      Graph<V, ?> graph = layoutModel.getGraph();
      graph
          .vertexSet()
          .forEach(
              v -> {
                Point p = layoutModel.get(v);
                p = Point.of(p.x, expansion * p.y);
                layoutModel.set(v, p);
              });
    } else if (regionWidth < regionHeight) {
      double expansion = (double) regionHeight / regionWidth;
      Graph<V, ?> graph = layoutModel.getGraph();
      graph
          .vertexSet()
          .forEach(
              v -> {
                Point p = layoutModel.get(v);
                p = Point.of(expansion * p.x, p.y);
                layoutModel.set(v, p);
              });
    }
  }
}
