package org.jungrapht.visualization.layout.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.function.Function;
import org.jungrapht.visualization.layout.util.Caching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutModel that uses a lazy cache for vertex locations (LoadingCache)
 *
 * @param <V> the vertex type
 * @author Tom Nelson
 */
public class LoadingCacheLayoutModel<V> extends AbstractLayoutModel<V>
    implements LayoutModel<V>, Caching {

  private static final Logger log = LoggerFactory.getLogger(LoadingCacheLayoutModel.class);

  protected LoadingCache<V, Point> locations;
  /**
   * a builder for LoadingCache instances
   *
   * @param <V> the vertex type
   * @param <T> the type of the superclass of the LayoutModel to be built
   */
  public static class Builder<V, T extends LoadingCacheLayoutModel<V>, B extends Builder<V, T, B>>
      extends AbstractLayoutModel.Builder<V, T, B> {

    protected LoadingCache<V, Point> locations =
        CacheBuilder.newBuilder().build(CacheLoader.from(() -> Point.ORIGIN));

    /**
     * set the LayoutModel to copy with this builder
     *
     * @param layoutModel
     * @return this builder for further use
     */
    public B layoutModel(LayoutModel<V> layoutModel) {
      this.width = layoutModel.getWidth();
      this.height = layoutModel.getHeight();
      return (B) this;
    }

    /**
     * sets the initializer to use for new vertices
     *
     * @param initializer
     * @return the builder
     */
    public B initializer(Function<V, Point> initializer) {
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

  public static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  public static <V> LoadingCacheLayoutModel<V> from(LoadingCacheLayoutModel<V> other) {
    return new LoadingCacheLayoutModel<>(other);
  }

  protected LoadingCacheLayoutModel(LoadingCacheLayoutModel.Builder builder) {
    super(builder);
    this.locations = builder.locations;
  }

  private LoadingCacheLayoutModel(LoadingCacheLayoutModel<V> other) {
    super(other.graph, other.width, other.height);
    this.locations = other.locations;
  }

  public void setInitializer(Function<V, Point> initializer) {
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(initializer::apply));
  }

  @Override
  public void set(V vertex, Point location) {
    if (!locked) {
      this.locations.put(vertex, location);
      super.set(vertex, location); // will fire events
    }
  }

  @Override
  public void set(V vertex, double x, double y) {
    this.set(vertex, Point.of(x, y));
  }

  @Override
  public Point get(V vertex) {
    if (log.isTraceEnabled()) {
      log.trace(this.locations.getUnchecked(vertex) + " gotten for " + vertex);
    }
    return this.locations.getUnchecked(vertex);
  }

  @Override
  public Point apply(V vertex) {
    return this.get(vertex);
  }

  @Override
  public void clear() {
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(() -> Point.ORIGIN));
  }
}
