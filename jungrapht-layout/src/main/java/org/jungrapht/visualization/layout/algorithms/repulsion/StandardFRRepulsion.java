package org.jungrapht.visualization.layout.algorithms.repulsion;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

/**
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <R> the Repulsion type
 * @param <B> the Repulsion Builder type
 */
public class StandardFRRepulsion<
        V, R extends StandardFRRepulsion<V, R, B>, B extends StandardFRRepulsion.Builder<V, R, B>>
    implements StandardRepulsion<V, R, B> {

  public static class Builder<V, R extends StandardFRRepulsion<V, R, B>, B extends Builder<V, R, B>>
      implements StandardRepulsion.Builder<V, R, B> {

    protected Map<V, Point> frVertexData;
    protected Function<V, Point> initializer = v -> Point.ORIGIN;
    protected double repulsionConstant;
    protected Random random = new Random();
    protected LayoutModel<V> layoutModel;

    public B nodeData(Map<V, Point> frVertexData) {
      this.frVertexData = frVertexData;
      return (B) this;
    }

    public B initializer(Function<V, Point> initializer) {
      this.initializer = initializer;
      return (B) this;
    }

    public B repulsionConstant(double repulstionConstant) {
      this.repulsionConstant = repulstionConstant;
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
      return (R) new StandardFRRepulsion<>(this);
    }
  }

  protected Map<V, Point> frVertexData;
  protected Function<V, Point> initializer;
  protected double repulsionConstant;
  protected double EPSILON = 0.000001D;
  protected Random random = new Random();
  protected LayoutModel<V> layoutModel;
  protected Set<V> vertexSet;
  double repulsionSquared;

  public static Builder builder() {
    return new Builder();
  }

  @Deprecated
  public static Builder standardBuilder() {
    return builder();
  }

  protected StandardFRRepulsion(Builder<V, R, B> builder) {
    this.layoutModel = builder.layoutModel;
    this.vertexSet = layoutModel.getGraph().vertexSet();
    this.random = builder.random;
    this.frVertexData = builder.frVertexData;
    this.initializer = builder.initializer;
    this.repulsionConstant = builder.repulsionConstant;
    this.repulsionSquared = repulsionConstant * repulsionConstant;
  }

  public void step() {}

  public Random getRandom() {
    return random;
  }

  @Override
  public void calculateRepulsion() {
    for (V vertex1 : vertexSet) {
      Point fvd1 = Point.ORIGIN;
      Point p1 = layoutModel.apply(vertex1);
      try {
        for (V vertex2 : vertexSet) {

          if (vertex1 != vertex2) {
            Point p2 = layoutModel.apply(vertex2);
            if (p1 == null || p2 == null) {
              continue;
            }
            double dx = p1.x - p2.x;
            double dy = p1.y - p2.y;

            double dist = Math.max(EPSILON, Math.sqrt((dx * dx) + (dy * dy)));

            double force = repulsionSquared / dist;

            if (Double.isNaN(force)) {
              throw new RuntimeException(
                  "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
            }
            fvd1 = fvd1.add((dx / dist) * force, (dy / dist) * force);
          }
        }
        frVertexData.put(vertex1, fvd1);
      } catch (ConcurrentModificationException cme) {
        calculateRepulsion();
      }
    }
  }
}
