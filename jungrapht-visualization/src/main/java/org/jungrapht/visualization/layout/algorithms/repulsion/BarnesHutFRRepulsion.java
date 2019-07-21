package org.jungrapht.visualization.layout.algorithms.repulsion;

import com.google.common.cache.LoadingCache;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.quadtree.BarnesHutQuadTree;
import org.jungrapht.visualization.layout.quadtree.ForceObject;
import org.jungrapht.visualization.layout.quadtree.Node;

/**
 * @author Tom Nelson
 * @param <N> the node type
 */
public class BarnesHutFRRepulsion<N>
    extends StandardFRRepulsion<N, BarnesHutFRRepulsion<N>, BarnesHutFRRepulsion.Builder<N>>
    implements BarnesHutRepulsion<N, BarnesHutFRRepulsion<N>, BarnesHutFRRepulsion.Builder<N>> {

  public static class Builder<N>
      extends StandardFRRepulsion.Builder<
          N, BarnesHutFRRepulsion<N>, BarnesHutFRRepulsion.Builder<N>>
      implements BarnesHutRepulsion.Builder<
          N, BarnesHutFRRepulsion<N>, BarnesHutFRRepulsion.Builder<N>> {

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

    public Builder<N> nodeData(LoadingCache<N, Point> frNodeData) {
      this.frNodeData = frNodeData;
      return this;
    }

    public Builder<N> repulsionConstant(double repulstionConstant) {
      this.repulsionConstant = repulstionConstant;
      return this;
    }

    @Override
    public Builder<N> random(Random random) {
      this.random = random;
      return this;
    }

    public BarnesHutFRRepulsion<N> build() {
      return new BarnesHutFRRepulsion(this);
    }
  }

  protected double EPSILON = 0.000001D;
  private BarnesHutQuadTree<N> tree;

  public static Builder barnesHutBuilder() {
    return new Builder();
  }

  protected BarnesHutFRRepulsion(Builder<N> builder) {
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

  @Override
  public void calculateRepulsion() {
    for (N node : layoutModel.getGraph().vertexSet()) {
      Point fvd = frNodeData.getUnchecked(node);
      if (fvd == null) {
        return;
      }
      frNodeData.put(node, Point.ORIGIN);

      Point forcePoint = layoutModel.apply(node);
      ForceObject<N> nodeForceObject =
          new ForceObject(node, forcePoint.x, forcePoint.y) {
            @Override
            protected void addForceFrom(ForceObject other) {
              double dx = this.p.x - other.p.x;
              double dy = this.p.y - other.p.y;
              double dist = Math.sqrt(dx * dx + dy * dy);
              dist = Math.max(EPSILON, dist);
              double force = (repulsionConstant * repulsionConstant) / dist;
              f = f.add(force * (dx / dist), force * (dy / dist));
            }
          };
      tree.applyForcesTo(nodeForceObject);
      frNodeData.put(node, Point.of(nodeForceObject.f.x, nodeForceObject.f.y));
    }
  }
}
