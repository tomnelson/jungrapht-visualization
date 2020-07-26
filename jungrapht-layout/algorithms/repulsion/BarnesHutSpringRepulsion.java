package org.jungrapht.visualization.layout.algorithms.repulsion;

import static org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm.*;

import java.util.ConcurrentModificationException;
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
public class BarnesHutSpringRepulsion<V>
    extends StandardSpringRepulsion<
        V, BarnesHutSpringRepulsion<V>, BarnesHutSpringRepulsion.Builder<V>>
    implements BarnesHutRepulsion<
        V, BarnesHutSpringRepulsion<V>, BarnesHutSpringRepulsion.Builder<V>> {

  public static class Builder<V>
      extends StandardSpringRepulsion.Builder<
          V, BarnesHutSpringRepulsion<V>, BarnesHutSpringRepulsion.Builder<V>>
      implements BarnesHutRepulsion.Builder<
          V, BarnesHutSpringRepulsion<V>, BarnesHutSpringRepulsion.Builder<V>> {

    private double theta = Node.DEFAULT_THETA;
    private BarnesHutQuadTree<V> tree = new BarnesHutQuadTree<>();

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

    public Builder<V> nodeData(Map<V, SpringVertexData> springVertexData) {
      this.springVertexData = springVertexData;
      return this;
    }

    public Builder<V> repulsionRangeSquared(int repulsionRangeSquared) {
      this.repulsionRangeSquared = repulsionRangeSquared;
      return this;
    }

    @Override
    public Builder<V> random(Random random) {
      this.random = random;
      return this;
    }

    public BarnesHutSpringRepulsion<V> build() {
      return new BarnesHutSpringRepulsion(this);
    }
  }

  protected BarnesHutQuadTree<V> tree;

  public static Builder builder() {
    return new Builder();
  }

  @Deprecated
  public static Builder barnesHutBuilder() {
    return builder();
  }

  protected BarnesHutSpringRepulsion(Builder<V> builder) {
    super(builder);
    this.tree = builder.tree;
  }

  public void step() {
    tree.rebuild(layoutModel.getGraph().vertexSet(), layoutModel);
  }

  public void calculateRepulsion() {
    try {
      for (V vertex : vertexSet) {

        if (layoutModel.isLocked(vertex)) {
          continue;
        }

        SpringVertexData svd = springVertexData.getOrDefault(vertex, new SpringVertexData());
        Point forcePoint = layoutModel.apply(vertex);
        ForceObject<V> nodeForceObject =
            new ForceObject(vertex, forcePoint.x, forcePoint.y) {
              @Override
              protected void addForceFrom(ForceObject other) {

                if (other == null || vertex == other.getElement()) {
                  return;
                }
                Point p = this.p;
                Point p2 = other.p;
                if (p == null || p2 == null) {
                  return;
                }
                double vx = p.x - p2.x;
                double vy = p.y - p2.y;
                double distanceSq = p.distanceSquared(p2);
                if (distanceSq == 0) {
                  f = f.add(random.nextDouble(), random.nextDouble());
                } else if (distanceSq < repulsionRangeSquared) {
                  double factor = 1;
                  f = f.add(factor * vx / distanceSq, factor * vy / distanceSq);
                }
              }
            };
        tree.applyForcesTo(nodeForceObject);
        Point f = Point.of(nodeForceObject.f.x, nodeForceObject.f.y);
        double dlen = f.x * f.x + f.y * f.y;
        if (dlen > 0) {
          dlen = Math.sqrt(dlen) / 2;
          svd.repulsiondx += f.x / dlen;
          svd.repulsiondy += f.y / dlen;
        }
      }
    } catch (ConcurrentModificationException cme) {
      calculateRepulsion();
    }
  }
}
