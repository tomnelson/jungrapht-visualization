package org.jungrapht.visualization.layout.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.function.Function;
import org.jungrapht.visualization.layout.util.Caching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutModel that uses a lazy cache for node locations (LoadingCache)
 *
 * @param <N> the node type
 * @author Tom Nelson
 */
public class LoadingCacheLayoutModel<N> extends AbstractLayoutModel<N>
    implements LayoutModel<N>, Caching {

  private static final Logger log = LoggerFactory.getLogger(LoadingCacheLayoutModel.class);

  protected LoadingCache<N, Point> locations =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> Point.ORIGIN));

  /**
   * a builder for LoadingCache instances
   *
   * @param <N> the node type
   * @param <T> the type of the superclass of the LayoutModel to be built
   */
  public static class Builder<N, T extends LoadingCacheLayoutModel<N>, B extends Builder<N, T, B>>
      extends AbstractLayoutModel.Builder<N, T, B> {

    protected LoadingCache<N, Point> locations =
        CacheBuilder.newBuilder().build(CacheLoader.from(() -> Point.ORIGIN));

    /**
     * set the LayoutModel to copy with this builder
     *
     * @param layoutModel
     * @return this builder for further use
     */
    public B layoutModel(LayoutModel<N> layoutModel) {
      this.width = layoutModel.getWidth();
      this.height = layoutModel.getHeight();
      return (B) this;
    }

    /**
     * sets the initializer to use for new nodes
     *
     * @param initializer
     * @return the builder
     */
    public B initializer(Function<N, Point> initializer) {
      this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(initializer::apply));
      return (B) this;
    }

    /**
     * build an instance of the requested LayoutModel of type T
     *
     * @return
     */
    public T build() {
      return (T) new LoadingCacheLayoutModel<>(this);
    }
  }

  public static <N> Builder<N, ?, ?> builder() {
    return new Builder<>();
  }

  public static <N> LoadingCacheLayoutModel<N> from(LoadingCacheLayoutModel<N> other) {
    return new LoadingCacheLayoutModel<>(other);
  }

  protected LoadingCacheLayoutModel(LoadingCacheLayoutModel.Builder builder) {
    super(builder);
    this.locations = builder.locations;
  }

  private LoadingCacheLayoutModel(LoadingCacheLayoutModel<N> other) {
    super(other.graph, other.width, other.height);
  }

  public void setInitializer(Function<N, Point> initializer) {
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(initializer::apply));
  }

  @Override
  public void set(N node, Point location) {
    if (!locked) {
      this.locations.put(node, location);
      super.set(node, location); // will fire events
    }
  }

  @Override
  public void set(N node, double x, double y) {
    this.set(node, Point.of(x, y));
  }

  @Override
  public Point get(N node) {
    if (log.isTraceEnabled()) {
      log.trace(this.locations.getUnchecked(node) + " gotten for " + node);
    }
    return this.locations.getUnchecked(node);
  }

  @Override
  public Point apply(N node) {
    return this.get(node);
  }

  @Override
  public void clear() {
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(() -> Point.ORIGIN));
  }
}
