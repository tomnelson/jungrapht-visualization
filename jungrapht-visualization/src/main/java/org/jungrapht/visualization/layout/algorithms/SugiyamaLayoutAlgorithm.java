package org.jungrapht.visualization.layout.algorithms;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.layout.algorithms.sugiyama.RenderContextAware;
import org.jungrapht.visualization.layout.algorithms.util.sugiyama.*;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SugiyamaLayoutAlgorithm<V, E>
    implements LayoutAlgorithm<V>,
        EdgeAwareLayoutAlgorithm<V, E>,
        EdgeSorting<E>,
        EdgePredicated<E>,
        VertexSorting<V>,
        VertexPredicated<V>,
        RenderContextAware<V, E>,
        ShapeFunctionAware<V>,
        Future {

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

  /**
   * a Builder to create a configured instance
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  public static class Builder<
          V, E, T extends SugiyamaLayoutAlgorithm<V, E>, B extends Builder<V, E, T, B>>
      implements LayoutAlgorithm.Builder<V, T, B>, EdgeAwareLayoutAlgorithm.Builder<V, E, T, B> {
    protected Predicate<V> rootPredicate;
    protected Function<V, Shape> vertexShapeFunction = v -> IDENTITY_SHAPE;
    protected Predicate<V> vertexPredicate = v -> false;
    protected Predicate<E> edgePredicate = e -> false;
    protected Comparator<V> vertexComparator = (v1, v2) -> 0;
    protected Comparator<E> edgeComparator = (e1, e2) -> 0;
    protected boolean expandLayout = true;

    /** {@inheritDoc} */
    protected B self() {
      return (B) this;
    }

    public B vertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
      this.vertexShapeFunction = vertexShapeFunction;
      return self();
    }
    /** {@inheritDoc} */
    public B rootPredicate(Predicate<V> rootPredicate) {
      this.rootPredicate = rootPredicate;
      return self();
    }

    /**
     * @param vertexPredicate {@link Predicate} to apply to vertices
     * @return this Builder
     */
    public B vertexPredicate(Predicate<V> vertexPredicate) {
      this.vertexPredicate = vertexPredicate;
      return self();
    }

    /**
     * @param edgePredicate {@link Predicate} to apply to edges
     * @return this Builder
     */
    public B edgePredicate(Predicate<E> edgePredicate) {
      this.edgePredicate = edgePredicate;
      return self();
    }

    /**
     * @param vertexComparator {@link Comparator} to sort vertices
     * @return this Builder
     */
    public B vertexComparator(Comparator<V> vertexComparator) {
      this.vertexComparator = vertexComparator;
      return self();
    }

    /**
     * @param edgeComparator {@link Comparator} to sort edges
     * @return this Builder
     */
    public B edgeComparator(Comparator<E> edgeComparator) {
      this.edgeComparator = edgeComparator;
      return self();
    }

    /** {@inheritDoc} */
    public B expandLayout(boolean expandLayout) {
      this.expandLayout = expandLayout;
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

  protected Rectangle bounds = Rectangle.IDENTITY;
  //  protected List<Integer> heights = new ArrayList<>();
  protected List<V> roots;

  protected Predicate<V> rootPredicate;
  protected Function<V, Shape> vertexShapeFunction;
  protected Predicate<V> vertexPredicate;
  protected Predicate<E> edgePredicate;
  protected Comparator<V> vertexComparator;
  protected Comparator<E> edgeComparator;
  protected boolean expandLayout;
  protected RenderContext<V, E> renderContext;
  CompletableFuture theFuture;

  private SugiyamaLayoutAlgorithm(Builder builder) {
    this(
        builder.rootPredicate,
        builder.vertexShapeFunction,
        builder.vertexPredicate,
        builder.vertexComparator,
        builder.edgePredicate,
        builder.edgeComparator,
        builder.expandLayout);
  }

  private SugiyamaLayoutAlgorithm(
      Predicate<V> rootPredicate,
      Function<V, Shape> vertexShapeFunction,
      Predicate<V> vertexPredicate,
      Comparator<V> vertexComparator,
      Predicate<E> edgePredicate,
      Comparator<E> edgeComparator,
      boolean expandLayout) {
    this.rootPredicate = rootPredicate;
    this.vertexShapeFunction = vertexShapeFunction;
    this.vertexPredicate = vertexPredicate;
    this.vertexComparator = vertexComparator;
    this.edgePredicate = edgePredicate;
    this.edgeComparator = edgeComparator;
    this.expandLayout = expandLayout;
  }

  private static final Logger log = LoggerFactory.getLogger(SugiyamaLayoutAlgorithm.class);

  private static final Shape IDENTITY_SHAPE = new Ellipse2D.Double();

  //  private Graph<V, E> graph;
  //  private Graph<SV<V>, SE<V, E>> svGraph;

  @Override
  public void setRenderContext(RenderContext<V, E> renderContext) {
    this.renderContext = renderContext;
  }

  @Override
  public void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
    this.vertexShapeFunction = vertexShapeFunction;
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {

    Runnable runnable = new SugiyamaRunnable(layoutModel, renderContext);
    theFuture =
        CompletableFuture.runAsync(runnable)
            .thenRun(
                () -> {
                  log.trace("We're done");
                  layoutModel.getViewChangeSupport().fireViewChanged();
                  // fire an event to say that the layout relax is done
                  layoutModel
                      .getLayoutStateChangeSupport()
                      .fireLayoutStateChanged(layoutModel, false);
                });
  }

  public void cancel() {
    if (theFuture != null) {
      theFuture.cancel(true);
    }
  }

  @Override
  public void setEdgePredicate(Predicate<E> edgePredicate) {
    this.edgePredicate = edgePredicate;
  }

  @Override
  public void setEdgeComparator(Comparator<E> comparator) {
    this.edgeComparator = edgeComparator;
  }

  @Override
  public void setVertexPredicate(Predicate<V> vertexPredicate) {
    this.vertexPredicate = vertexPredicate;
  }

  @Override
  public void setVertexComparator(Comparator<V> comparator) {
    this.vertexComparator = vertexComparator;
  }
}
