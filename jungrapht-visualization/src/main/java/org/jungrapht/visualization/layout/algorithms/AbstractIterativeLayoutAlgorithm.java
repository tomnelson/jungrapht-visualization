package org.jungrapht.visualization.layout.algorithms;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For Iterative algorithms that perform delayed operations on a Thread, save off the layoutModel so
 * that it can be accessed by the threaded code. The layoutModel could be removed and instead passed
 * via all of the iterative methods (for example step(layoutModel) instead of step() )
 *
 * @author Tom Nelson
 */
public abstract class AbstractIterativeLayoutAlgorithm<V> extends AbstractLayoutAlgorithm<V>
    implements IterativeLayoutAlgorithm<V>, Future {

  private static final Logger log = LoggerFactory.getLogger(AbstractIterativeLayoutAlgorithm.class);

  public abstract static class Builder<
          V, T extends AbstractIterativeLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      extends AbstractLayoutAlgorithm.Builder<V, T, B> implements LayoutAlgorithm.Builder<V, T, B> {
    protected Executor executor;
    protected Random random = new Random();
    protected boolean shouldPrerelax = true;
    protected int preRelaxDurationMs = 500;

    public B randomSeed(long randomSeed) {
      this.random = new Random(randomSeed);
      return self();
    }

    public B prerelax(boolean shouldPrerelax) {
      this.shouldPrerelax = shouldPrerelax;
      return self();
    }

    public B preRelaxDuration(int preRelaxDurationMs) {
      this.preRelaxDurationMs = preRelaxDurationMs;
      return self();
    }

    public B executor(Executor executor) {
      this.executor = executor;
      return self();
    }

    protected B self() {
      return (B) this;
    }

    public abstract T build();
  }

  protected AbstractIterativeLayoutAlgorithm(Builder builder) {
    super(builder);
    this.executor = builder.executor;
    this.random = builder.random;
    this.shouldPreRelax = builder.shouldPrerelax;
    this.preRelaxDurationMs = builder.preRelaxDurationMs;
  }
  /**
   * because the IterativeLayoutAlgorithms use multithreading to continuously update vertex
   * positions, the layoutModel state is saved (during the visit method) so that it can be used
   * continuously
   */
  protected LayoutModel<V> layoutModel;

  protected Executor executor;

  // both of these can be set at instance creation time
  protected boolean shouldPreRelax;
  protected int preRelaxDurationMs; // how long should the prerelax phase last?

  protected Random random;

  protected Future future;

  public void setRandomSeed(long randomSeed) {
    this.random = new Random(randomSeed);
  }

  // returns true iff prerelaxing happened
  public final boolean preRelax() {
    if (!shouldPreRelax) {
      return false;
    }
    long timeNow = System.currentTimeMillis();
    while (System.currentTimeMillis() - timeNow < preRelaxDurationMs && !done()) {
      step();
    }
    return true;
  }

  @Override
  public void setExecutor(Executor executor) {
    this.executor = executor;
  }

  @Override
  public Executor getExecutor() {
    return this.executor;
  }

  /**
   * because the IterativeLayoutAlgorithms use multithreading to continuously update vertex
   * positions, the layoutModel state is saved (during the visit method) so that it can be used
   * continuously
   */
  public void visit(LayoutModel<V> layoutModel) {
    this.future = layoutModel.getTheFuture();
    log.trace("visiting " + layoutModel);
    this.layoutModel = layoutModel;
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
    if (this.future != null) {
      return this.future.cancel(mayInterruptIfRunning);
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
    if (this.future != null) {
      return this.future.isCancelled();
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
    return false;
  }

  @Override
  public Object get() throws InterruptedException, ExecutionException {
    if (this.future != null) {
      return this.future.get();
    }
    return null;
  }

  @Override
  public Object get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    if (this.future != null) {
      return this.future.get(timeout, unit);
    }
    return null;
  }
}
