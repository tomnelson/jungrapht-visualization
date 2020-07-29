package org.jungrapht.visualization.layout.algorithms;

import java.util.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A radial layout for Tree or Forest graphs. Positions vertices in concentric circles with the root
 * at the center
 *
 * @author Tom Nelson
 */
public class TidierRadialTreeLayoutAlgorithm<V, E> extends TidierTreeLayoutAlgorithm<V, E>
    implements RadialTreeLayout<V> {

  private static final Logger log = LoggerFactory.getLogger(TidierRadialTreeLayoutAlgorithm.class);

  protected Map<V, PolarPoint> polarLocations = new HashMap<>();

  public static class Builder<
          V, E, T extends TidierRadialTreeLayoutAlgorithm<V, E>, B extends Builder<V, E, T, B>>
      extends TidierTreeLayoutAlgorithm.Builder<V, E, T, B>
      implements LayoutAlgorithm.Builder<V, T, B> {

    public T build() {
      return (T) new TidierRadialTreeLayoutAlgorithm<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> edgeAwareBuilder() {
    return (Builder<V, E, ?, ?>) new Builder<>().expandLayout(false);
  }

  public TidierRadialTreeLayoutAlgorithm() {
    this(TidierRadialTreeLayoutAlgorithm.edgeAwareBuilder());
  }

  protected TidierRadialTreeLayoutAlgorithm(Builder<V, E, ?, ?> builder) {
    super(builder);
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    Graph<V, E> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    super.visit(layoutModel);
    log.trace("roots are {}", roots);
    setRadialLocations(super.roots, layoutModel);
    putRadialPointsInModel(layoutModel);
    // set size with the max of the layout rings
    int diameter = diameter(layoutModel);
    int offsetDeltaX = diameter - layoutModel.getWidth();
    int offsetDeltaY = diameter - layoutModel.getHeight();
    offset(layoutModel, offsetDeltaX / 2, offsetDeltaY / 2);
    layoutModel.setSize(diameter, diameter);
  }

  protected void offset(LayoutModel<V> layoutModel, int deltax, int deltay) {
    layoutModel
        .getGraph()
        .vertexSet()
        .forEach(
            v -> {
              Point p = layoutModel.get(v);
              p = p.add(deltax, deltay);
              layoutModel.set(v, p);
            });
  }

  @Override
  public int diameter(LayoutModel<V> layoutModel) {
    return layoutModel
        .getGraph()
        .vertexSet()
        .stream()
        .map(vertex -> polarLocations.get(vertex).radius * 2)
        .mapToInt(Double::intValue)
        .max()
        .orElse(layoutModel.getWidth());
  }

  /**
   * override for to always start at zero
   *
   * @param layoutHeight
   * @param treeHeight
   * @return 0 for any supplied values
   */
  protected int getInitialPosition(int initialPosition, int layoutHeight, int treeHeight) {
    return 0;
  }

  protected void putRadialPointsInModel(LayoutModel<V> layoutModel) {
    polarLocations.forEach((key, value) -> layoutModel.set(key, getCartesian(layoutModel, key)));
  }

  /** @return a map from vertices to their locations in polar coordinates. */
  public Map<V, PolarPoint> getPolarLocations() {
    return polarLocations;
  }

  protected Point getCartesian(LayoutModel<V> layoutModel, V vertex) {
    PolarPoint pp = polarLocations.get(vertex);
    double centerX = layoutModel.getWidth() / 2;
    double centerY = layoutModel.getHeight() / 2;
    Point cartesian = PolarPoint.polarToCartesian(pp);
    cartesian = cartesian.add(centerX, centerY);
    return cartesian;
  }

  protected Point getMaxXY(LayoutModel<V> layoutModel) {
    double maxx = 0;
    double maxy = 0;
    Collection<V> vertices = layoutModel.getGraph().vertexSet();
    for (V vertex : vertices) {
      Point location = layoutModel.apply(vertex);
      maxx = Math.max(maxx, location.x);
      maxy = Math.max(maxy, location.y);
    }
    return Point.of(maxx, maxy);
  }

  protected void setRadialLocations(List<V> roots, LayoutModel<V> layoutModel) {
    int width = layoutModel.getWidth();
    Point max = getMaxXY(layoutModel);
    double maxx = max.x + verticalVertexSpacing;
    maxx = Math.max(maxx, width);
    double theta = 2 * Math.PI / maxx;

    double deltaRadius = 1; //width / 2 / maxy;
    double offset = 0;
    if (roots.size() > 0) {
      offset = verticalVertexSpacing;
    }
    for (V vertex : layoutModel.getGraph().vertexSet()) {
      Point p = layoutModel.get(vertex);

      PolarPoint polarPoint =
          PolarPoint.of(p.x * theta, (offset + p.y - this.verticalVertexSpacing) * deltaRadius);
      polarLocations.put(vertex, polarPoint);
    }
  }

  /** @return the center of this layout's area. */
  public Point getCenter(LayoutModel<V> layoutModel) {
    return Point.of(layoutModel.getWidth() / 2, layoutModel.getHeight() / 2);
  }

  @Override
  public boolean constrained() {
    return true;
  }
}
