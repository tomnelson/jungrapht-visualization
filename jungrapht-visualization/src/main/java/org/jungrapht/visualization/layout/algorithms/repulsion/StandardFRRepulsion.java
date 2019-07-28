package org.jungrapht.visualization.layout.algorithms.repulsion;

import com.google.common.cache.LoadingCache;
import java.util.ConcurrentModificationException;
import java.util.Random;
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

    protected LoadingCache<V, Point> frVertexData;
    protected double repulsionConstant;
    protected Random random = new Random();
    protected LayoutModel<V> layoutModel;

    public B nodeData(LoadingCache<V, Point> frVertexData) {
      this.frVertexData = frVertexData;
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

  protected LoadingCache<V, Point> frVertexData;
  protected double repulsionConstant;
  protected double EPSILON = 0.000001D;
  protected Random random = new Random();
  protected LayoutModel<V> layoutModel;

  public static Builder standardBuilder() {
    return new Builder();
  }

  protected StandardFRRepulsion(Builder<V, R, B> builder) {
    this.layoutModel = builder.layoutModel;
    this.random = builder.random;
    this.frVertexData = builder.frVertexData;
    this.repulsionConstant = builder.repulsionConstant;
  }

  public void step() {}

  public Random getRandom() {
    return random;
  }

  @Override
  public void calculateRepulsion() {
    for (V vertex1 : layoutModel.getGraph().vertexSet()) {
      Point fvd1 = frVertexData.getUnchecked(vertex1);
      if (fvd1 == null) {
        return;
      }
      frVertexData.put(vertex1, Point.ORIGIN);

      try {
        for (V vertex2 : layoutModel.getGraph().vertexSet()) {

          if (vertex1 != vertex2) {
            fvd1 = frVertexData.getUnchecked(vertex1);
            Point p1 = layoutModel.apply(vertex1);
            Point p2 = layoutModel.apply(vertex2);
            if (p1 == null || p2 == null) {
              continue;
            }
            double xDelta = p1.x - p2.x;
            double yDelta = p1.y - p2.y;

            double deltaLength =
                Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));

            double force = (repulsionConstant * repulsionConstant) / deltaLength;

            if (Double.isNaN(force)) {
              throw new RuntimeException(
                  "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
            }
            fvd1 = fvd1.add((xDelta / deltaLength) * force, (yDelta / deltaLength) * force);
            frVertexData.put(vertex1, fvd1);
          }
        }
      } catch (ConcurrentModificationException cme) {
        calculateRepulsion();
      }
    }
  }
}
