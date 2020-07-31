package org.jungrapht.visualization.layout.algorithms;

import java.util.Random;
import java.util.concurrent.Executor;
import org.jungrapht.visualization.layout.algorithms.util.AfterRunnable;
import org.jungrapht.visualization.layout.algorithms.util.Threaded;
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
    implements IterativeLayoutAlgorithm<V>, AfterRunnable, Threaded {

  private static final Logger log = LoggerFactory.getLogger(AbstractIterativeLayoutAlgorithm.class);

  public abstract static class Builder<
          V, T extends AbstractIterativeLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      extends AbstractLayoutAlgorithm.Builder<V, T, B> implements LayoutAlgorithm.Builder<V, T, B> {
    protected Executor executor;
    protected Random random = new Random();
    protected boolean shouldPrerelax = true;
    protected int preRelaxDurationMs = 500;
    protected Runnable afterRunnable = () -> {};
    protected boolean threaded = true;

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

    public B afterRunnable(Runnable afterRunnable) {
      this.afterRunnable = afterRunnable;
      return self();
    }

    public B threaded(boolean threaded) {
      this.threaded = threaded;
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
    this.afterRunnable = builder.afterRunnable;
    this.threaded = builder.threaded;
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

  protected Runnable afterRunnable;

  protected boolean threaded;

  protected boolean cancelled;

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
    log.debug("visiting " + layoutModel);
    this.layoutModel = layoutModel;
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
    if (layoutModel != null) {
      layoutModel.stop();
    }
  }
}
