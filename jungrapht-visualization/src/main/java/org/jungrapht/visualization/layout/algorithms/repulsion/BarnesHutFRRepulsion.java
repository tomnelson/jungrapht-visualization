package org.jungrapht.visualization.layout.algorithms.repulsion;

import java.util.Map;
import java.util.Random;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.quadtree.BarnesHutQuadTree;
import org.jungrapht.visualization.layout.quadtree.ForceObject;
import org.jungrapht.visualization.layout.quadtree.Node;

/**
 * @author Tom Nelson
 * @param <V> the vertex type
 */
public class BarnesHutFRRepulsion<V>
    extends StandardFRRepulsion<V, BarnesHutFRRepulsion<V>, BarnesHutFRRepulsion.Builder<V>>
    implements BarnesHutRepulsion<V, BarnesHutFRRepulsion<V>, BarnesHutFRRepulsion.Builder<V>> {

  public static class Builder<V>
      extends StandardFRRepulsion.Builder<
          V, BarnesHutFRRepulsion<V>, BarnesHutFRRepulsion.Builder<V>>
      implements BarnesHutRepulsion.Builder<
          V, BarnesHutFRRepulsion<V>, BarnesHutFRRepulsion.Builder<V>> {

    private double theta = Node.DEFAULT_THETA;
    private BarnesHutQuadTree<V> tree;

    public Builder<V> layoutModel(LayoutModel<V> layoutModel) {
      this.layoutModel = layoutModel;
      this.tree =
          BarnesHutQuadTree.<V>builder()
              .bounds(layoutModel.getWidth(), layoutModel.getHeight())
              .theta(theta)
              .build();
      return this;
    }

    public Builder<V> theta(double theta) {
      this.theta = theta;
      return this;
    }

    public Builder<V> nodeData(Map<V, Point> frVertexData) {
      this.frVertexData = frVertexData;
      return this;
    }

    public Builder<V> repulsionConstant(double repulstionConstant) {
      this.repulsionConstant = repulstionConstant;
      return this;
    }

    @Override
    public Builder<V> random(Random random) {
      this.random = random;
      return this;
    }

    public BarnesHutFRRepulsion<V> build() {
      return new BarnesHutFRRepulsion(this);
    }
  }

  protected double EPSILON = 0.000001D;
  private BarnesHutQuadTree<V> tree;

  public static Builder builder() {
    return new Builder();
  }

  @Deprecated
  public static Builder barnesHutBuilder() {
    return builder();
  }

  protected BarnesHutFRRepulsion(Builder<V> builder) {
    super(builder);
    this.tree = builder.tree;
  }

  public void step() {
    tree.rebuild(vertexSet, layoutModel);
  }

  @Override
  public void calculateRepulsion() {
    for (V vertex : vertexSet) {
      Point forcePoint = layoutModel.apply(vertex);
      ForceObject<V> nodeForceObject =
          new ForceObject(vertex, forcePoint.x, forcePoint.y) {
            @Override
            protected void addForceFrom(ForceObject other) {
              double dx = this.p.x - other.p.x;
              double dy = this.p.y - other.p.y;
              double dist = Math.sqrt(dx * dx + dy * dy);
              dist = Math.max(EPSILON, dist);
              double force = repulsionSquared / dist;
              f = f.add(force * (dx / dist), force * (dy / dist));
            }
          };
      tree.applyForcesTo(nodeForceObject);
      frVertexData.put(vertex, Point.of(nodeForceObject.f.x, nodeForceObject.f.y));
    }
  }
}
