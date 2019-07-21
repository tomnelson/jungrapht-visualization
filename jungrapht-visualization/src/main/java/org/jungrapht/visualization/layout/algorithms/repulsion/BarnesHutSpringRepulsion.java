package org.jungrapht.visualization.layout.algorithms.repulsion;

import com.google.common.cache.LoadingCache;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.quadtree.BarnesHutQuadTree;
import org.jungrapht.visualization.layout.quadtree.ForceObject;
import org.jungrapht.visualization.layout.quadtree.Node;

/**
 * @author Tom Nelson
 * @param <N> the node type
 */
public class BarnesHutSpringRepulsion<N>
    extends StandardSpringRepulsion<
        N, BarnesHutSpringRepulsion<N>, BarnesHutSpringRepulsion.Builder<N>>
    implements BarnesHutRepulsion<
        N, BarnesHutSpringRepulsion<N>, BarnesHutSpringRepulsion.Builder<N>> {

  public static class Builder<N>
      extends StandardSpringRepulsion.Builder<
          N, BarnesHutSpringRepulsion<N>, BarnesHutSpringRepulsion.Builder<N>>
      implements BarnesHutRepulsion.Builder<
          N, BarnesHutSpringRepulsion<N>, BarnesHutSpringRepulsion.Builder<N>> {

    private double theta = Node.DEFAULT_THETA;
    private BarnesHutQuadTree<N> tree = BarnesHutQuadTree.<N>builder().build();

    public Builder<N> layoutModel(LayoutModel<N> layoutModel) {
      this.layoutModel = layoutModel;
      this.tree =
          BarnesHutQuadTree.<N>builder()
              .bounds(layoutModel.getWidth(), layoutModel.getHeight())
              .theta(theta)
              .build();
      return this;
    }

    public Builder<N> theta(double theta) {
      this.theta = theta;
      return this;
    }

    public Builder<N> nodeData(
        LoadingCache<N, SpringLayoutAlgorithm.SpringNodeData> springNodeData) {
      this.springNodeData = springNodeData;
      return this;
    }

    public Builder<N> repulsionRangeSquared(int repulsionRangeSquared) {
      this.repulsionRangeSquared = repulsionRangeSquared;
      return this;
    }

    @Override
    public Builder<N> random(Random random) {
      this.random = random;
      return this;
    }

    public BarnesHutSpringRepulsion<N> build() {
      return new BarnesHutSpringRepulsion(this);
    }
  }

  protected BarnesHutQuadTree<N> tree;

  public static Builder barnesHutBuilder() {
    return new Builder();
  }

  protected BarnesHutSpringRepulsion(Builder<N> builder) {
    super(builder);
    this.tree = builder.tree;
  }

  public void step() {
    tree.rebuild(
        layoutModel
            .getLocations()
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry ->
                        org.jungrapht.visualization.layout.quadtree.Point.of(
                            entry.getValue().x, entry.getValue().y))));
  }

  public void calculateRepulsion() {
    Graph<N, ?> graph = layoutModel.getGraph();

    try {
      for (N node : graph.vertexSet()) {

        if (layoutModel.isLocked(node)) {
          continue;
        }

        SpringLayoutAlgorithm.SpringNodeData svd = springNodeData.getUnchecked(node);
        if (svd == null) {
          continue;
        }
        Point forcePoint = layoutModel.apply(node);
        ForceObject<N> nodeForceObject =
            new ForceObject(node, forcePoint.x, forcePoint.y) {
              @Override
              protected void addForceFrom(ForceObject other) {

                if (other == null || node == other.getElement()) {
                  return;
                }
                org.jungrapht.visualization.layout.quadtree.Point p = this.p;
                org.jungrapht.visualization.layout.quadtree.Point p2 = other.p;
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
