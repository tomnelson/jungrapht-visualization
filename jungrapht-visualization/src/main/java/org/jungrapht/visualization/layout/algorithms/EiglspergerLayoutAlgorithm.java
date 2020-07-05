
package org.jungrapht.visualization.layout.algorithms;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.eiglsperger.EiglspergerRunnable;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.layout.algorithms.util.AfterRunnable;
import org.jungrapht.visualization.layout.algorithms.util.ComponentGrouping;
import org.jungrapht.visualization.layout.algorithms.util.EdgeShapeFunctionSupplier;
import org.jungrapht.visualization.layout.algorithms.util.ExecutorConsumer;
import org.jungrapht.visualization.layout.algorithms.util.Threaded;
import org.jungrapht.visualization.layout.algorithms.util.VertexShapeAware;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.jungrapht.visualization.util.Context;
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
 * @see "An Efficient Implementation of Sugiyama's Algorithm for Layered Graph Drawing. Markus
 *     Eiglsperger, Martin Siebenhaller, Michael Kaufman"
 * @param <V> vertex type
 * @param <E> edge type
 */
public class EiglspergerLayoutAlgorithm<V, E>
    implements LayoutAlgorithm<V>,
        VertexShapeAware<V>,
        EdgeShapeFunctionSupplier<V, E>,
        Layered,
        AfterRunnable,
        Threaded,
        ExecutorConsumer,
        Future {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerLayoutAlgorithm.class);

  private static final Shape IDENTITY_SHAPE = new Ellipse2D.Double();
  protected static final String MINCROSS_STRAIGHTEN_EDGES = PREFIX + "mincross.straightenEdges";
  protected static final String MINCROSS_POST_STRAIGHTEN = PREFIX + "mincross.postStraighten";
  protected static final String MINCROSS_THREADED = PREFIX + "mincross.threaded";
  protected static final String MAX_LEVEL_CROSS = PREFIX + "mincross.maxLevelCross";

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
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected Executor executor;
    protected Function<V, Shape> vertexShapeFunction = v -> IDENTITY_SHAPE;
    protected Consumer<Function<Context<Graph<V, E>, E>, Shape>> edgeShapeFunctionConsumer =
        i -> {};
    protected boolean straightenEdges =
        Boolean.parseBoolean(System.getProperty(MINCROSS_STRAIGHTEN_EDGES, "true"));
    protected boolean postStraighten =
        Boolean.parseBoolean(System.getProperty(MINCROSS_POST_STRAIGHTEN, "true"));
    protected boolean transpose = true;
    protected int maxLevelCross = Integer.getInteger(MAX_LEVEL_CROSS, 23);
    protected boolean expandLayout = true;
    Layering layering = Layering.TOP_DOWN;
    protected Runnable after = () -> {};
    protected boolean threaded =
        Boolean.parseBoolean(System.getProperty(MINCROSS_THREADED, "true"));

    /** {@inheritDoc} */
    protected B self() {
      return (B) this;
    }

    public B vertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
      this.vertexShapeFunction = vertexShapeFunction;
      return self();
    }

    public B edgeShapeFunctionConsumer(
        Consumer<Function<Context<Graph<V, E>, E>, Shape>> edgeShapeConsumer) {
      this.edgeShapeFunctionConsumer = edgeShapeConsumer;
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

  protected Rectangle bounds = Rectangle.IDENTITY;
  protected List<V> roots;

  protected Function<V, Shape> vertexShapeFunction;
  protected Consumer<Function<Context<Graph<V, E>, E>, Shape>> edgeShapeConsumer;
  protected boolean straightenEdges;
  protected boolean postStraighten;
  protected boolean transpose;
  protected int maxLevelCross;
  protected boolean expandLayout;
  protected RenderContext<V, E> renderContext;
  protected boolean threaded;
  protected Layering layering;
  protected Executor executor;
  protected CompletableFuture theFuture;
  protected Runnable after;
  protected Map<E, List<Point>> edgePointMap = new HashMap<>();
  protected EdgeShape.ArticulatedLine<V, E> edgeShape = new EdgeShape.ArticulatedLine<>();

  public EiglspergerLayoutAlgorithm() {
    this(EiglspergerLayoutAlgorithm.edgeAwareBuilder());
  }

  protected EiglspergerLayoutAlgorithm(Builder builder) {
    this(
        builder.vertexShapeFunction,
        builder.edgeShapeFunctionConsumer,
        builder.straightenEdges,
        builder.postStraighten,
        builder.transpose,
        builder.maxLevelCross,
        builder.expandLayout,
        builder.layering,
        builder.threaded,
        builder.executor,
        builder.after);
  }

  protected EiglspergerLayoutAlgorithm(
      Function<V, Shape> vertexShapeFunction,
      Consumer<Function<Context<Graph<V, E>, E>, Shape>> edgeShapeConsumer,
      boolean straightenEdges,
      boolean postStraighten,
      boolean transpose,
      int maxLevelCross,
      boolean expandLayout,
      Layering layering,
      boolean threaded,
      Executor executor,
      Runnable after) {
    this.vertexShapeFunction = vertexShapeFunction;
    this.edgeShapeConsumer = edgeShapeConsumer;
    this.straightenEdges = straightenEdges;
    this.postStraighten = postStraighten;
    this.transpose = transpose;
    this.maxLevelCross = maxLevelCross;
    this.expandLayout = expandLayout;
    this.layering = layering;
    this.after = after;
    this.executor = executor;
    this.threaded = threaded;

    this.edgeShape.setEdgeArticulationFunction(
        e -> edgePointMap.getOrDefault(e, Collections.emptyList()));
  }

  @Override
  public void setExecutor(Executor executor) {
    this.executor = executor;
  }

  @Override
  public Executor getExecutor() {
    return this.executor;
  }

  @Override
  public void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
    this.vertexShapeFunction = vertexShapeFunction;
  }

  @Override
  public void setEdgeShapeFunctionConsumer(
      Consumer<Function<Context<Graph<V, E>, E>, Shape>> edgeShapeConsumer) {
    this.edgeShapeConsumer = edgeShapeConsumer;
  }

  @Override
  public void setLayering(Layering layering) {
    this.layering = layering;
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
  public void visit(LayoutModel<V> layoutModel) {

    Graph<V, E> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    // if this is a multicomponent graph, discover components and create a temp
    // LayoutModel for each to visit. Afterwards, append all the layoutModels
    // to the one visited above.
    List<Graph<V, E>> graphs = ComponentGrouping.getComponentGraphs(graph);
    List<LayoutModel<V>> layoutModels = new ArrayList<>();
    for (int i = 0; i < graphs.size(); i++) {
      LayoutModel<V> componentLayoutModel =
          LayoutModel.<V>builder()
              .graph(graphs.get(i))
              .width(50)
              .height(layoutModel.getHeight())
              .build();
      layoutModels.add(componentLayoutModel);
    }

    for (LayoutModel<V> componentLayoutModel : layoutModels) {
      EiglspergerRunnable runnable =
          EiglspergerRunnable.<V, E>builder()
              .layoutModel(componentLayoutModel)
              .vertexShapeFunction(vertexShapeFunction)
              .straightenEdges(straightenEdges)
              .transpose(transpose)
              .postStraighten(postStraighten)
              .maxLevelCross(maxLevelCross)
              .layering(layering)
              .build();

      if (threaded) {
        if (executor != null) {
          theFuture =
              CompletableFuture.runAsync(runnable, executor)
                  .thenRun(
                      () -> {
                        log.trace("Eiglsperger layout done");
                        this.edgePointMap.putAll(runnable.getEdgePointMap());
                        layoutModel.appendLayoutModel(componentLayoutModel);
                        this.run(); // run the after function
                        layoutModel.getViewChangeSupport().fireViewChanged();
                        // fire an event to say that the layout is done
                        layoutModel
                            .getLayoutStateChangeSupport()
                            .fireLayoutStateChanged(layoutModel, false);
                      });
        } else {
          theFuture =
              CompletableFuture.runAsync(runnable)
                  .thenRun(
                      () -> {
                        log.trace("Eiglsperger layout done");
                        this.edgePointMap.putAll(runnable.getEdgePointMap());
                        layoutModel.appendLayoutModel(componentLayoutModel);
                        this.run(); // run the after function
                        layoutModel.getViewChangeSupport().fireViewChanged();
                        // fire an event to say that the layout is done
                        layoutModel
                            .getLayoutStateChangeSupport()
                            .fireLayoutStateChanged(layoutModel, false);
                      });
        }
      } else {
        runnable.run();
        this.edgePointMap.putAll(runnable.getEdgePointMap());
        layoutModel.appendLayoutModel(componentLayoutModel);
        after.run();
        layoutModel.getViewChangeSupport().fireViewChanged();
        // fire an event to say that the layout is done
        layoutModel.getLayoutStateChangeSupport().fireLayoutStateChanged(layoutModel, false);
      }

      edgeShapeConsumer.accept(edgeShape);
    }
  }

  public void cancel() {
    if (theFuture != null) {
      theFuture.cancel(true);
    }
  }

  /**
   * Attempts to cancel execution of this task. This attempt will fail if the task has already
   * completed, has already been cancelled, or could not be cancelled for some other reason. If
   * successful, and this task has not started when {@code cancel} is called, this task should never
   * run. If the task has already started, then the {@code mayInterruptIfRunning} parameter
   * determines whether the thread executing this task should be interrupted in an attempt to stop
   * the task.
   *
   * <p>After this method returns, subsequent calls to {@link #isDone} will always return {@code
   * true}. Subsequent calls to {@link #isCancelled} will always return {@code true} if this method
   * returned {@code true}.
   *
   * @param mayInterruptIfRunning {@code true} if the thread executing this task should be
   *     interrupted; otherwise, in-progress tasks are allowed to complete
   * @return {@code false} if the task could not be cancelled, typically because it has already
   *     completed normally; {@code true} otherwise
   */
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (theFuture != null) {
      return theFuture.cancel(mayInterruptIfRunning);
    }
    return false;
  }

  /**
   * Returns {@code true} if this task was cancelled before it completed normally.
   *
   * @return {@code true} if this task was cancelled before it completed
   */
  @Override
  public boolean isCancelled() {
    if (theFuture != null) {
      return theFuture.isCancelled();
    }
    return false;
  }

  /**
   * Returns {@code true} if this task completed.
   *
   * <p>Completion may be due to normal termination, an exception, or cancellation -- in all of
   * these cases, this method will return {@code true}.
   *
   * @return {@code true} if this task completed
   */
  @Override
  public boolean isDone() {
    if (theFuture != null) {
      return theFuture.isDone();
    }
    return false;
  }

  /**
   * Waits if necessary for the computation to complete, and then retrieves its result.
   *
   * @return the computed result
   * @throws CancellationException if the computation was cancelled
   * @throws ExecutionException if the computation threw an exception
   * @throws InterruptedException if the current thread was interrupted while waiting
   */
  @Override
  public Object get() throws InterruptedException, ExecutionException {
    if (theFuture != null) {
      return theFuture.get();
    }
    return null;
  }

  /**
   * Waits if necessary for at most the given time for the computation to complete, and then
   * retrieves its result, if available.
   *
   * @param timeout the maximum time to wait
   * @param unit the time unit of the timeout argument
   * @return the computed result
   * @throws CancellationException if the computation was cancelled
   * @throws ExecutionException if the computation threw an exception
   * @throws InterruptedException if the current thread was interrupted while waiting
   * @throws TimeoutException if the wait timed out
   */
  @Override
  public Object get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    if (theFuture != null) {
      return theFuture.get(timeout, unit);
    }
    return null;
  }

  @Override
  public void run() {
    after.run();
  }

  @Override
  public void setAfter(Runnable after) {
    this.after = after;
  }
}
