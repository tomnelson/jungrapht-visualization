package org.jungrapht.visualization.layout.algorithms.repulsion;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

/**
 * Standard implementation of repulsion. We can strongly recommend to use this class only for Graphs
 * with hundreds nodes. Complexity is O(N^2).
 *
 * <p>For large Graphs use BarnesHutFA2Repulsion.
 *
 * @see "ForceAtlas2, a Continuous Graph Layout Algorithm for Handy Network Visualization Designed
 *     for the Gephi Software"
 * @see "https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0098679"
 * @param <V> vertex class
 * @param <R> repulsion class
 * @param <B> repulsion builder class
 */
public class StandardFA2Repulsion<
        V, R extends StandardFA2Repulsion<V, R, B>, B extends StandardFA2Repulsion.Builder<V, R, B>>
    implements StandardRepulsion<V, R, B> {
  public static class Builder<
          V, R extends StandardFA2Repulsion<V, R, B>, B extends Builder<V, R, B>>
      implements StandardRepulsion.Builder<V, R, B> {
    protected Map<V, Point> frVertexData;
    protected Function<V, Point> initializer = v -> Point.ORIGIN;
    protected LayoutModel<V> layoutModel;
    protected Random random = new Random();
    protected double kr = 1.0;
    protected Function<V, Double> nodeSizes = null; // Sizes for prevent overlapping
    protected Map<V, Double> nodeMasses = null; // Masses for "Repulsion by Degree"

    public B nodeData(Map<V, Point> frVertexData) {
      this.frVertexData = frVertexData;
      return (B) this;
    }

    public B initializer(Function<V, Point> initializer) {
      this.initializer = initializer;
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

    /**
     * Set node sizes. They may have fixed size or size based on centrality measure or anything
     * else. By default 1.0
     *
     * @param nodeSizes
     * @return
     */
    public B nodeSizes(Function<V, Double> nodeSizes) {
      this.nodeSizes = nodeSizes;
      return (B) this;
    }

    /**
     * Set repulsion K. By default 100.0
     *
     * @param kr
     * @return
     */
    public B repulsionK(double kr) {
      this.kr = kr;
      return (B) this;
    }

    /**
     * Set node masses. This may have fixed masses or masses based on degrees or anything else. By
     * default node degrees plus one
     *
     * @param nodeMasses
     * @return
     */
    public B nodeMasses(Map<V, Double> nodeMasses) {
      this.nodeMasses = nodeMasses;
      return (B) this;
    }

    @Override
    public R build() {
      return (R) new StandardFA2Repulsion<>(this);
    }
  }

  protected Map<V, Point> frVertexData;
  protected Function<V, Point> initializer = v -> Point.ORIGIN;
  protected LayoutModel<V> layoutModel;
  protected Set<V> vertexSet;
  protected Random random = new Random();
  protected double kr = 100.0; // 100.0 is default value in Gephi
  protected Function<V, Double> nodeSizes; // Sizes for prevent overlapping
  protected Map<V, Double> nodeMasses; // Masses for "Repulsion by Degree"
  protected static final double epsilon = 1e-16; // Math stability

  public static Builder builder() {
    return new Builder();
  }

  @Deprecated
  public static Builder standardBuilder() {
    return builder();
  }

  protected StandardFA2Repulsion(Builder<V, R, B> builder) {
    this.frVertexData = builder.frVertexData;
    this.initializer = builder.initializer;
    this.layoutModel = builder.layoutModel;
    this.vertexSet = layoutModel.getGraph().vertexSet();
    this.random = builder.random;
    this.kr = builder.kr;

    if (builder.nodeSizes == null) {
      this.nodeSizes = v -> 1.0;
    } else {
      this.nodeSizes = builder.nodeSizes;
    }

    if (builder.nodeMasses == null) {
      this.nodeMasses =
          layoutModel
              .getGraph()
              .vertexSet()
              .stream()
              .collect(Collectors.toMap(v -> v, v -> layoutModel.getGraph().degreeOf(v) + 1.0));
    } else {
      this.nodeMasses = builder.nodeMasses;
    }
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
      double vertexOneMass = nodeMasses.get(vertex1);
      double vertexOneSize = nodeSizes.apply(vertex1);

      try {
        for (V vertex2 : vertexSet) {

          if (vertex1 != vertex2) {
            Point p2 = layoutModel.apply(vertex2);
            if (p1 == null || p2 == null) {
              continue;
            }
            double dx = p1.x - p2.x;
            double dy = p1.y - p2.y;

            double vertex2Mass = nodeMasses.get(vertex2);

            double dist = Math.max(epsilon, Math.sqrt((dx * dx) + (dy * dy)));
            dist -= vertexOneSize + nodeSizes.apply(vertex2);
            double force;

            if (dist > 0) {
              force = kr * vertexOneMass * vertex2Mass / dist / dist;
            } else if (dist < 0) {
              force = kr * vertexOneMass * vertex2Mass / dist;
            } else {
              force = 0.0;
            }

            if (Double.isNaN(force)) {
              throw new RuntimeException(
                  "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
            }
            fvd1 = fvd1.add(dx * force, dy * force);
          }
        }
        frVertexData.put(vertex1, fvd1);

      } catch (ConcurrentModificationException cme) {
        calculateRepulsion();
      }
    }
  }
}
