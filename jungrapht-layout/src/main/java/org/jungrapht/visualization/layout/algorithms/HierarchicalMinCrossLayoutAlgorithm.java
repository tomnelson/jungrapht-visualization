package org.jungrapht.visualization.layout.algorithms;

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.util.concurrent.Executor;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.eiglsperger.EiglspergerRunnable;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaRunnable;
import org.jungrapht.visualization.layout.algorithms.util.AfterRunnable;
import org.jungrapht.visualization.layout.algorithms.util.ExecutorConsumer;
import org.jungrapht.visualization.layout.algorithms.util.LayeredRunnable;
import org.jungrapht.visualization.layout.algorithms.util.Threaded;
import org.jungrapht.visualization.layout.algorithms.util.VertexBoundsFunctionConsumer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.jungrapht.visualization.layout.util.PropertyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Hierarchical Minimum-Cross layout algorithm based on Sugiyama. Uses the Eiglsperger optimations
 * for large graphs. A threshold property may be used to control the decision to switch from the
 * standard Sugiyama algorithm to the faster Eiglsperger algorithm.
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
 * @see "An Efficient Implementation of Sugiyama's Algorithm for Layered Graph Drawing. Markus
 *     Eiglsperger, Martin Siebenhaller, Michael Kaufman"
 * @param <V> vertex type
 * @param <E> edge type
 */
public class HierarchicalMinCrossLayoutAlgorithm<V, E>
    extends AbstractHierarchicalMinCrossLayoutAlgorithm<V, E>
    implements LayoutAlgorithm<V>,
        VertexBoundsFunctionConsumer<V>,
        Layered<V, E>,
        AfterRunnable,
        ExecutorConsumer,
        Threaded {

  private static final Logger log =
      LoggerFactory.getLogger(HierarchicalMinCrossLayoutAlgorithm.class);

  static {
    PropertyLoader.load();
  }

  protected static final String EIGLSPERGER_THRESHOLD = PREFIX + "mincross.eiglspergerThreshold";

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
          T extends HierarchicalMinCrossLayoutAlgorithm<V, E> & EdgeAwareLayoutAlgorithm<V, E>,
          B extends Builder<V, E, T, B>>
      extends AbstractHierarchicalMinCrossLayoutAlgorithm.Builder<V, E, T, B>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected int transposeLimit = Integer.getInteger(TRANSPOSE_LIMIT, 6);
    protected int eiglspergerThreshold = Integer.getInteger(EIGLSPERGER_THRESHOLD, 500);

    public B eiglspergerThreshold(int eiglspergerThreshold) {
      this.eiglspergerThreshold = eiglspergerThreshold;
      return self();
    }

    public B transposeLimit(int transposeLimit) {
      this.transposeLimit = transposeLimit;
      return self();
    }

    /** {@inheritDoc} */
    public T build() {
      return (T) new HierarchicalMinCrossLayoutAlgorithm<>(this);
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

  protected int eiglspergerThreshold;

  protected int transposeLimit;

  public HierarchicalMinCrossLayoutAlgorithm() {
    this(HierarchicalMinCrossLayoutAlgorithm.edgeAwareBuilder());
  }

  private HierarchicalMinCrossLayoutAlgorithm(Builder<V, E, ?, ?> builder) {
    this(
        builder.vertexBoundsFunction,
        builder.eiglspergerThreshold,
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
        builder.after);
  }

  protected HierarchicalMinCrossLayoutAlgorithm(
      Function<V, Rectangle> vertexShapeFunction,
      int eiglspergerThreshold,
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
        threaded,
        executor,
        separateComponents,
        after);
    this.eiglspergerThreshold = eiglspergerThreshold;
    this.transposeLimit = transposeLimit;
  }

  @Override
  protected LayeredRunnable<E> getRunnable(
      int componentCount, LayoutModel<V> componentLayoutModel) {

    Graph<V, E> graph = componentLayoutModel.getGraph();
    if (graph.vertexSet().size() + graph.edgeSet().size() < eiglspergerThreshold) {

      return EiglspergerRunnable.<V, E>builder()
          .layoutModel(componentLayoutModel)
          .vertexShapeFunction(vertexBoundsFunction)
          .straightenEdges(straightenEdges)
          .postStraighten(postStraighten)
          .transpose(transpose)
          .maxLevelCross(maxLevelCross)
          .layering(layering)
          .multiComponent(componentCount > 1)
          .build();
    } else {
      return SugiyamaRunnable.<V, E>builder()
          .layoutModel(componentLayoutModel)
          .vertexShapeFunction(vertexBoundsFunction)
          .straightenEdges(straightenEdges)
          .postStraighten(postStraighten)
          .transpose(transpose)
          .transposeLimit(transposeLimit)
          .maxLevelCross(maxLevelCross)
          .layering(layering)
          .multiComponent(componentCount > 1)
          .build();
    }
  }
}
