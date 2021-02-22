
package org.jungrapht.visualization.layout.algorithms;

import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.eiglsperger.EiglspergerRunnable;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
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
 * The Sugiyama Hierarchical Minimum-Cross layout algorithm with Eiglsperger optimizations
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
public class EiglspergerLayoutAlgorithm<V, E>
    extends AbstractHierarchicalMinCrossLayoutAlgorithm<V, E>
    implements LayoutAlgorithm<V>,
        VertexBoundsFunctionConsumer<V>,
        Layered<V, E>,
        AfterRunnable,
        Threaded,
        ExecutorConsumer {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerLayoutAlgorithm.class);

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
          T extends EiglspergerLayoutAlgorithm<V, E> & EdgeAwareLayoutAlgorithm<V, E>,
          B extends Builder<V, E, T, B>>
      extends AbstractHierarchicalMinCrossLayoutAlgorithm.Builder<V, E, T, B>
      implements LayoutAlgorithm.Builder<V, T, B> {
    /** {@inheritDoc} */
    public T build() {
      return (T) new EiglspergerLayoutAlgorithm<>(this);
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

  public EiglspergerLayoutAlgorithm() {
    this(EiglspergerLayoutAlgorithm.edgeAwareBuilder());
  }

  protected EiglspergerLayoutAlgorithm(Builder<V, E, ?, ?> builder) {
    this(
        builder.vertexBoundsFunction,
        builder.straightenEdges,
        builder.postStraighten,
        builder.transpose,
        builder.maxLevelCross,
        builder.maxLevelCrossFunction,
        builder.expandLayout,
        builder.layering,
        builder.edgeComparator,
        builder.threaded,
        builder.executor,
        builder.separateComponents,
        builder.favoredEdgePredicate,
        builder.after);
  }

  protected EiglspergerLayoutAlgorithm(
      Function<V, Rectangle> vertexShapeFunction,
      boolean straightenEdges,
      boolean postStraighten,
      boolean transpose,
      int maxLevelCross,
      Function<Graph<V, E>, Integer> maxLevelCrossFunction,
      boolean expandLayout,
      Layering layering,
      Comparator<E> edgeComparator,
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
        edgeComparator,
        threaded,
        executor,
        separateComponents,
        favoredEdgePredicate,
        after);
  }

  @Override
  protected LayeredRunnable<E> getRunnable(
      int componentCount, LayoutModel<V> componentLayoutModel) {
    return EiglspergerRunnable.<V, E>builder()
        .layoutModel(componentLayoutModel)
        .vertexShapeFunction(vertexBoundsFunction)
        .straightenEdges(straightenEdges)
        .postStraighten(postStraighten)
        .transpose(transpose)
        .maxLevelCross(maxLevelCrossFunction.apply(layoutModel.getGraph()))
        .layering(layering)
        .edgeComparator(edgeComparator)
        .favoredEdgePredicate(favoredEdgePredicate)
        .multiComponent(componentCount > 1)
        .build();
  }
}
