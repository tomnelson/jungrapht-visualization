package org.jungrapht.visualization.layout.algorithms.repulsion;

import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.quadtree.BarnesHutQuadTree;
import org.jungrapht.visualization.layout.quadtree.ForceObject;
import org.jungrapht.visualization.layout.quadtree.Node;

/**
 * Implementation of repulsion based on Barnes-Hut approximation. Recommended for large Graphs.
 * Complexity is O(N*log(N)).
 *
 * @see "https://en.wikipedia.org/wiki/Barnes%E2%80%93Hut_simulation"
 * @see "ForceAtlas2, a Continuous Graph Layout Algorithm for Handy Network Visualization Designed
 *     for the Gephi Software"
 * @see "https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0098679"
 * @param <V>
 */
public class BarnesHutFA2Repulsion<V>
    extends StandardFA2Repulsion<V, BarnesHutFA2Repulsion<V>, BarnesHutFA2Repulsion.Builder<V>>
    implements BarnesHutRepulsion<V, BarnesHutFA2Repulsion<V>, BarnesHutFA2Repulsion.Builder<V>> {
  public static class Builder<V>
      extends StandardFA2Repulsion.Builder<
          V, BarnesHutFA2Repulsion<V>, BarnesHutFA2Repulsion.Builder<V>>
      implements BarnesHutRepulsion.Builder<
          V, BarnesHutFA2Repulsion<V>, BarnesHutFA2Repulsion.Builder<V>> {
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

    @Override
    public BarnesHutFA2Repulsion build() {
      return new BarnesHutFA2Repulsion<>(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @Deprecated
  public static Builder barnesHutBuilder() {
    return builder();
  }

  private BarnesHutQuadTree<V> tree;

  protected BarnesHutFA2Repulsion(Builder<V> builder) {
    super(builder);
    this.tree = builder.tree;
  }

  @Override
  public void step() {
    tree.rebuild(layoutModel.getGraph().vertexSet(), nodeMasses::get, layoutModel);
  }

  @Override
  public void calculateRepulsion() {
    for (V vertex : vertexSet) {
      Point forcePoint = layoutModel.apply(vertex);
      double vertexOneSize = nodeSizes.apply(vertex);

      ForceObject<V> forceObject =
          new ForceObject<V>(vertex, forcePoint, nodeMasses.get(vertex)) {
            @Override
            protected void addForceFrom(ForceObject other) {
              double dx = this.p.x - other.p.x;
              double dy = this.p.y - other.p.y;

              double otherMass = other.getMass();

              double dist = Math.max(epsilon, Math.sqrt((dx * dx) + (dy * dy)));
              dist -= vertexOneSize + nodeSizes.apply(vertex);
              double force;

              if (Double.compare(dist, 0) == 0) {
                force = 0.;
              } else if (dist > 0) {
                force = kr * mass * otherMass / dist / dist;
              } else {
                force = kr * mass * otherMass / dist;
              }

              if (Double.isNaN(force)) {
                throw new RuntimeException(
                    "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
              }

              f = f.add(force * dx, force * dy);
            }
          };
      tree.applyForcesTo(forceObject);
      frVertexData.put(vertex, Point.of(forceObject.f.x, forceObject.f.y));
    }
  }
}
