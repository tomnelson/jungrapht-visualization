package org.jungrapht.visualization.layout.algorithms;

import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaRunnable;
import org.jungrapht.visualization.layout.algorithms.util.AfterRunnable;
import org.jungrapht.visualization.layout.algorithms.util.ExecutorConsumer;
import org.jungrapht.visualization.layout.algorithms.util.LayeredRunnable;
import org.jungrapht.visualization.layout.algorithms.util.Threaded;
import org.jungrapht.visualization.layout.algorithms.util.VertexBoundsFunctionConsumer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Sugiyama Hierarchical Minimum-Cross layout algorithm
 *
 * @see "Methods for Visual Understanding Hierarchical System Structures. KOZO SUGIYAMA, MEMBER,
 *     IEEE, SHOJIRO TAGAWA, AND MITSUHIKO TODA, MEMBER, IEEE"
 * @see "An E log E Line Crossing Algorithm for Levelled Graphs. Vance Waddle and Ashok Malhotra IBM
 *     Thomas J. Watson Research Center"
 * @see "Simple and Efficient Bilayer Cross Counting. Wilhelm Barth, Petra Mutzel, Institut für
 *     Computergraphik und Algorithmen Technische Universität Wien, Michael Jünger, Institut für
 *     Informatik Universität zu Köln"
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 * @param <V> vertex type
 * @param <E> edge type
 */
public class SugiyamaLayoutAlgorithm<V, E> extends AbstractHierarchicalMinCrossLayoutAlgorithm<V, E>
    implements LayoutAlgorithm<V>,
        VertexBoundsFunctionConsumer<V>,
        Layered<V, E>,
        AfterRunnable,
        Threaded,
        ExecutorConsumer {

  private static final Logger log = LoggerFactory.getLogger(SugiyamaLayoutAlgorithm.class);

  /**
   * a Builder to create a configured instance
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  public static class Builder<
          V,
          E,
          T extends SugiyamaLayoutAlgorithm<V, E> & EdgeAwareLayoutAlgorithm<V, E>,
          B extends Builder<V, E, T, B>>
      extends AbstractHierarchicalMinCrossLayoutAlgorithm.Builder<V, E, T, B>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected int transposeLimit = Integer.getInteger(TRANSPOSE_LIMIT, 6);

    public B transposeLimit(int transposeLimit) {
      this.transposeLimit = transposeLimit;
      return self();
    }

    /** {@inheritDoc} */
    public T build() {
      return (T) new SugiyamaLayoutAlgorithm<>(this);
    }
  }

  /**
   * @param <V> vertex type
   * @param <E> edge type
   * @return a Builder ready to configure
   */
  public static <V, E> Builder<V, E, ?, ?> edgeAwareBuilder() {
    return new Builder<>();
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected int transposeLimit;

  public SugiyamaLayoutAlgorithm() {
    this(SugiyamaLayoutAlgorithm.edgeAwareBuilder());
  }

  protected SugiyamaLayoutAlgorithm(Builder<V, E, ?, ?> builder) {
    this(
        builder.vertexBoundsFunction,
        builder.straightenEdges,
        builder.postStraighten,
        builder.transpose,
        builder.transposeLimit,
        builder.maxLevelCross,
        builder.maxLevelCrossFunction,
        builder.expandLayout,
        builder.layering,
        builder.threaded,
        builder.executor,
        builder.separateComponents,
        builder.favoredEdgePredicate,
        builder.after);
  }

  private SugiyamaLayoutAlgorithm(
      Function<V, Rectangle> vertexShapeFunction,
      boolean straightenEdges,
      boolean postStraighten,
      boolean transpose,
      int transposeLimit,
      int maxLevelCross,
      Function<Graph<V, E>, Integer> maxLevelCrossFunction,
      boolean expandLayout,
      Layering layering,
      boolean threaded,
      Executor executor,
      boolean separateComponents,
      Predicate<E> favoredEdgePredicate,
      Runnable after) {
    super(
        vertexShapeFunction,
        straightenEdges,
        postStraighten,
        transpose,
        maxLevelCross,
        maxLevelCrossFunction,
        expandLayout,
        layering,
        (v1, v2) -> 0,
        threaded,
        executor,
        separateComponents,
        favoredEdgePredicate,
        after);
    this.transposeLimit = transposeLimit;
  }

  @Override
  protected LayeredRunnable<E> getRunnable(
      int componentCount, LayoutModel<V> componentLayoutModel) {
    return SugiyamaRunnable.<V, E>builder()
        .layoutModel(componentLayoutModel)
        .vertexShapeFunction(vertexBoundsFunction)
        .straightenEdges(straightenEdges)
        .postStraighten(postStraighten)
        .transpose(transpose)
        .transposeLimit(transposeLimit)
        .maxLevelCross(maxLevelCrossFunction.apply(layoutModel.getGraph()))
        .layering(layering)
        .multiComponent(componentCount > 1)
        .build();
  }
}
