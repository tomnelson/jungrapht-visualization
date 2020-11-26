package org.jungrapht.visualization.layout.algorithms;

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.layout.algorithms.util.AfterRunnable;
import org.jungrapht.visualization.layout.algorithms.util.ComponentGrouping;
import org.jungrapht.visualization.layout.algorithms.util.EdgeArticulationFunctionSupplier;
import org.jungrapht.visualization.layout.algorithms.util.ExecutorConsumer;
import org.jungrapht.visualization.layout.algorithms.util.LayeredRunnable;
import org.jungrapht.visualization.layout.algorithms.util.Threaded;
import org.jungrapht.visualization.layout.algorithms.util.VertexBoundsFunctionConsumer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.jungrapht.visualization.layout.util.PropertyLoader;
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
public abstract class AbstractHierarchicalMinCrossLayoutAlgorithm<V, E>
    implements LayoutAlgorithm<V>,
        VertexBoundsFunctionConsumer<V>,
        EdgeArticulationFunctionSupplier<E>,
        Layered<V, E>,
        AfterRunnable,
        Threaded,
        ExecutorConsumer {

  private static final Logger log =
      LoggerFactory.getLogger(AbstractHierarchicalMinCrossLayoutAlgorithm.class);

  static {
    PropertyLoader.load();
  }

  protected static final Rectangle IDENTITY_SHAPE = Rectangle.IDENTITY;
  protected static final String MINCROSS_STRAIGHTEN_EDGES = PREFIX + "mincross.straightenEdges";
  protected static final String MINCROSS_POST_STRAIGHTEN = PREFIX + "mincross.postStraighten";
  protected static final String MINCROSS_THREADED = PREFIX + "mincross.threaded";
  protected static final String TRANSPOSE_LIMIT = PREFIX + "mincross.transposeLimit";
  protected static final String MAX_LEVEL_CROSS = PREFIX + "mincross.maxLevelCross";

  /**
   * a Builder to create a configured instance
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  public abstract static class Builder<
          V,
          E,
          T extends
              AbstractHierarchicalMinCrossLayoutAlgorithm<V, E> & EdgeAwareLayoutAlgorithm<V, E>,
          B extends Builder<V, E, T, B>>
      implements LayoutAlgorithm.Builder<V, T, B> {
    static {
      PropertyLoader.load();
    }

    protected Executor executor;
    protected Function<V, Rectangle> vertexBoundsFunction = v -> IDENTITY_SHAPE;
    protected boolean straightenEdges =
        Boolean.parseBoolean(System.getProperty(MINCROSS_STRAIGHTEN_EDGES, "true"));
    protected boolean postStraighten =
        Boolean.parseBoolean(System.getProperty(MINCROSS_POST_STRAIGHTEN, "true"));;
    protected boolean transpose = true;
    protected int maxLevelCross = Integer.getInteger(MAX_LEVEL_CROSS, 23);
    protected Function<Graph<V, E>, Integer> maxLevelCrossFunction = g -> maxLevelCross;
    protected boolean expandLayout = true;
    protected Layering layering = Layering.TOP_DOWN;
    protected Runnable after = () -> {};
    protected boolean threaded =
        Boolean.parseBoolean(System.getProperty(MINCROSS_THREADED, "true"));
    protected boolean separateComponents = true;

    /** {@inheritDoc} */
    protected B self() {
      return (B) this;
    }

    public B vertexBoundsFunction(Function<V, Rectangle> vertexBoundsFunction) {
      this.vertexBoundsFunction = vertexBoundsFunction;
      return self();
    }

    public B straightenEdges(boolean straightenEdges) {
      this.straightenEdges = straightenEdges;
      return self();
    }

    public B postStraighten(boolean postStraighten) {
      this.postStraighten = postStraighten;
      return self();
    }

    public B transpose(boolean transpose) {
      this.transpose = transpose;
      return self();
    }

    public B maxLevelCross(int maxLevelCross) {
      this.maxLevelCross = maxLevelCross;
      return self();
    }

    public B maxLevelCrossFunction(Function<Graph<V, E>, Integer> maxLevelCrossFunction) {
      this.maxLevelCrossFunction = maxLevelCrossFunction;
      return self();
    }

    /** {@inheritDoc} */
    public B expandLayout(boolean expandLayout) {
      this.expandLayout = expandLayout;
      return self();
    }

    public B layering(Layering layering) {
      this.layering = layering;
      return self();
    }

    public B threaded(boolean threaded) {
      this.threaded = threaded;
      return self();
    }

    public B executor(Executor executor) {
      this.executor = executor;
      return self();
    }

    public B after(Runnable after) {
      this.after = after;
      return self();
    }

    public B separateComponents(boolean separateComponents) {
      this.separateComponents = separateComponents;
      return self();
    }
  }

  protected Rectangle bounds = Rectangle.IDENTITY;

  protected List<V> roots;

  protected Function<V, Rectangle> vertexBoundsFunction;
  protected boolean straightenEdges;
  protected boolean postStraighten;
  protected boolean transpose;
  protected int maxLevelCross;
  protected Function<Graph<V, E>, Integer> maxLevelCrossFunction;
  protected boolean expandLayout;
  protected boolean threaded;
  protected Layering layering;
  protected Executor executor;
  protected Runnable after;
  protected boolean separateComponents;
  protected Map<E, List<Point>> edgePointMap = new HashMap<>();
  protected AtomicInteger completionCounter = new AtomicInteger();
  protected Set<LayeredRunnable<E>> runnables = new HashSet<>();
  protected LayoutModel<V> layoutModel; // ref used to switch events back on after cancel
  protected boolean cancelled;

  protected AbstractHierarchicalMinCrossLayoutAlgorithm(Builder builder) {
    this(
        builder.vertexBoundsFunction,
        builder.straightenEdges,
        builder.postStraighten,
        builder.transpose,
        builder.maxLevelCross,
        builder.maxLevelCrossFunction,
        builder.expandLayout,
        builder.layering,
        builder.threaded,
        builder.executor,
        builder.separateComponents,
        builder.after);
  }

  protected AbstractHierarchicalMinCrossLayoutAlgorithm(
      Function<V, Rectangle> vertexBoundsFunction,
      boolean straightenEdges,
      boolean postStraighten,
      boolean transpose,
      int maxLevelCross,
      Function<Graph<V, E>, Integer> maxLevelCrossFunction,
      boolean expandLayout,
      Layering layering,
      boolean threaded,
      Executor executor,
      boolean separateComponents,
      Runnable after) {
    this.vertexBoundsFunction = vertexBoundsFunction;
    this.straightenEdges = straightenEdges;
    this.postStraighten = postStraighten;
    this.transpose = transpose;
    this.maxLevelCross = maxLevelCross;
    this.maxLevelCrossFunction = maxLevelCrossFunction;
    this.expandLayout = expandLayout;
    this.layering = layering;
    this.threaded = threaded;
    this.executor = executor;
    this.separateComponents = separateComponents;
    this.after = after;
  }

  @Override
  public void setVertexBoundsFunction(Function<V, Rectangle> vertexBoundsFunction) {
    this.vertexBoundsFunction = vertexBoundsFunction;
  }

  @Override
  public Function<E, List<Point>> getEdgeArticulationFunction() {
    return e -> edgePointMap.getOrDefault(e, Collections.emptyList());
  }

  @Override
  public void setLayering(Layering layering) {
    this.edgePointMap.clear();
    this.layering = layering;
  }

  @Override
  public void setMaxLevelCrossFunction(Function<Graph<V, E>, Integer> maxLevelCrossFunction) {
    this.maxLevelCrossFunction = maxLevelCrossFunction;
  }

  @Override
  public boolean isThreaded() {
    return this.threaded;
  }

  @Override
  public void setThreaded(boolean threaded) {
    this.threaded = threaded;
  }

  @Override
  public void cancel() {
    cancelled = true;
    runnables.forEach(LayeredRunnable::cancel);
    if (layoutModel != null) {
      layoutModel.setFireEvents(true);
    }
  }

  protected boolean isComplete(int expected) {
    boolean isComplete = completionCounter.incrementAndGet() >= expected;
    if (log.isTraceEnabled()) {
      log.trace(
          " completionCounter:{}, expected: {} isComplete:{}",
          completionCounter.get(),
          expected,
          isComplete);
    }
    return isComplete;
  }

  @Override
  public void setExecutor(Executor executor) {
    this.executor = executor;
  }

  @Override
  public Executor getExecutor() {
    return this.executor;
  }

  protected abstract LayeredRunnable<E> getRunnable(
      int componentCount, LayoutModel<V> componentLayoutModel);

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    this.layoutModel = layoutModel;
    this.completionCounter.set(0);
    this.edgePointMap.clear();

    Graph<V, E> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    List<Graph<V, E>> graphs;
    List<LayoutModel<V>> layoutModels = new ArrayList<>();

    if (separateComponents) {
      // if this is a multicomponent graph, discover components and create a temp
      // LayoutModel for each to visit. Afterwards, append all the layoutModels
      // to the one visited above.
      graphs = ComponentGrouping.getComponentGraphs(graph);
      layoutModel.setFireEvents(false);

      for (int i = 0; i < graphs.size(); i++) {
        LayoutModel<V> componentLayoutModel =
            LayoutModel.<V>builder()
                .graph(graphs.get(i))
                .width(layoutModel.getWidth())
                .height(layoutModel.getHeight())
                .build();
        layoutModels.add(componentLayoutModel);
      }
    } else {
      graphs = Collections.singletonList(graph);
      layoutModels.add(layoutModel);
    }

    for (LayoutModel<V> componentLayoutModel : layoutModels) {

      LayeredRunnable<E> runnable = getRunnable(graphs.size(), componentLayoutModel);
      runnables.add(runnable);
      if (threaded) {
        if (executor != null) {
          CompletableFuture.runAsync(runnable, executor)
              .thenRun(
                  () -> {
                    log.trace("MinCross layout done");
                    this.edgePointMap.putAll(runnable.getEdgePointMap());
                    if (!cancelled && isComplete(graphs.size())) {
                      after.run();
                      layoutModel.setFireEvents(true);
                      appendAll(layoutModel, layoutModels);
                    }
                  });
        } else {
          CompletableFuture.runAsync(runnable)
              .thenRun(
                  () -> {
                    log.trace("MinCross layout done");
                    this.edgePointMap.putAll(runnable.getEdgePointMap());
                    if (!cancelled && isComplete(graphs.size())) {
                      after.run();
                      layoutModel.setFireEvents(true);
                      appendAll(layoutModel, layoutModels);
                    }
                  });
        }
      } else {
        runnable.run();
        log.trace("MinCross layout done");
        this.edgePointMap.putAll(runnable.getEdgePointMap());
        if (!cancelled && isComplete(graphs.size())) {
          after.run();
          layoutModel.setFireEvents(true);
          appendAll(layoutModel, layoutModels);
        }
      }
    }
  }

  private void appendAll(
      LayoutModel<V> parentLayoutModel, Collection<LayoutModel<V>> childLayoutModels) {
    log.trace("appendAll, cancelled: {}", cancelled);
    if (!cancelled) {
      log.trace("appending: {} child layout models", childLayoutModels.size());
      childLayoutModels.forEach(parentLayoutModel::appendLayoutModel);
      parentLayoutModel
          .getLayoutStateChangeSupport()
          .fireLayoutStateChanged(parentLayoutModel, false);
    }
  }

  @Override
  public void runAfter() {
    if (after != null) {
      after.run();
    }
  }

  @Override
  public void setAfter(Runnable after) {
    this.after = after;
  }

  @Override
  public boolean constrained() {
    return false;
  }
}
