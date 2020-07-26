package org.jungrapht.visualization.layout.algorithms.repulsion;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm.SpringVertexData;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

/**
 * @author Tom Nelson
 * @param <V> the vertex type
 */
public class StandardSpringRepulsion<
        V,
        R extends StandardSpringRepulsion<V, R, B>,
        B extends StandardSpringRepulsion.Builder<V, R, B>>
    implements StandardRepulsion<V, R, B> {

  public static class Builder<
          V, R extends StandardSpringRepulsion<V, R, B>, B extends Builder<V, R, B>>
      implements StandardRepulsion.Builder<V, R, B> {

    protected Map<V, SpringVertexData> springVertexData;
    protected int repulsionRangeSquared = 100 * 100;
    protected Random random = new Random();
    protected LayoutModel<V> layoutModel;

    public B nodeData(Map<V, SpringVertexData> springVertexData) {
      this.springVertexData = springVertexData;
      return (B) this;
    }

    public B repulsionRangeSquared(int repulsionRangeSquared) {
      this.repulsionRangeSquared = repulsionRangeSquared;
      return (B) this;
    }

    @Override
    public B layoutModel(LayoutModel<V> layoutModel) {
      this.layoutModel = layoutModel;
      return (B) this;
    }

    @Override
    public B random(Random random) {
      this.random = random;
      return (B) this;
    }

    public R build() {
      return (R) new StandardSpringRepulsion(this);
    }
  }

  protected Map<V, SpringVertexData> springVertexData;
  protected int repulsionRangeSquared;
  protected Random random;
  protected LayoutModel<V> layoutModel;
  protected Set<V> vertexSet;

  public static Builder builder() {
    return new Builder();
  }

  @Deprecated
  public static Builder standardBuilder() {
    return builder();
  }

  protected StandardSpringRepulsion(Builder<V, R, B> builder) {
    this.layoutModel = builder.layoutModel;
    this.vertexSet = layoutModel.getGraph().vertexSet();
    this.random = builder.random;
    this.springVertexData = builder.springVertexData;
    this.repulsionRangeSquared = builder.repulsionRangeSquared;
  }

  public void step() {}

  public void calculateRepulsion() {
    try {
      for (V vertex : vertexSet) {
        if (layoutModel.isLocked(vertex)) {
          continue;
        }

        SpringVertexData svd = springVertexData.getOrDefault(vertex, new SpringVertexData());
        double dx = 0, dy = 0;

        for (V vertex2 : vertexSet) {
          if (vertex == vertex2) {
            continue;
          }
          Point p = layoutModel.apply(vertex);
          Point p2 = layoutModel.apply(vertex2);
          if (p == null || p2 == null) {
            continue;
          }
          double vx = p.x - p2.x;
          double vy = p.y - p2.y;
          double distanceSq = p.distanceSquared(p2);
          if (distanceSq == 0) {
            dx += random.nextDouble();
            dy += random.nextDouble();
          } else if (distanceSq < repulsionRangeSquared) {
            double factor = 1;
            dx += factor * vx / distanceSq;
            dy += factor * vy / distanceSq;
          }
        }
        double dlen = dx * dx + dy * dy;
        if (dlen > 0) {
          dlen = Math.sqrt(dlen) / 2;
          svd.repulsiondx += dx / dlen;
          svd.repulsiondy += dy / dlen;
        }
      }
    } catch (ConcurrentModificationException cme) {
      calculateRepulsion();
    }
  }
}
