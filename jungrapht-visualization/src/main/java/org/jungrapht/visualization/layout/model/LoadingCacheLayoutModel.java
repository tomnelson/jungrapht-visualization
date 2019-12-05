package org.jungrapht.visualization.layout.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

  protected ConcurrentMap<V, Point> locations = new ConcurrentHashMap<>();
  Function<V, Point> initializer = v -> Point.ORIGIN;
  /**
   * a builder for LoadingCache instances
   *
   * @param <V> the vertex type
   * @param <T> the type of the superclass of the LayoutModel to be built
   */
  public static class Builder<V, T extends LoadingCacheLayoutModel<V>, B extends Builder<V, T, B>>
      extends AbstractLayoutModel.Builder<V, T, B> {

    Function<V, Point> initializer = v -> Point.ORIGIN;

    //    protected ConcurrentMap<V, Point> locations  = new ConcurrentHashMap<>();
    //        CacheBuilder.newBuilder().build(CacheLoader.from(() -> Point.ORIGIN));

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
      this.initializer = initializer;
      //      this.locations = new ConcurrentHashMap<>() {
      //        @Override
      //        public Point computeIfAbsent(V key, Function<? super V, ? extends Point> mappingFunction) {
      //          return initializer.apply(key);
      //        }
      //      };
      //              CacheBuilder.newBuilder().build(CacheLoader.from(initializer::apply));
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
    this.initializer = builder.initializer;
  }

  private LoadingCacheLayoutModel(LoadingCacheLayoutModel<V> other) {
    super(other.graph, other.width, other.height);
    this.initializer = other.initializer;
  }

  public void setInitializer(Function<V, Point> initializer) {
    this.initializer = initializer;
    //    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(initializer::apply));
  }

  @Override
  public void set(V vertex, Point location) {
    if (location == null) {
      log.error("whoa");
    }
    if (vertex == null) {
      log.error("whoa");
    }
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
    //    if (log.isTraceEnabled()) {
    //      log.trace(this.locations.get(vertex) + " gotten for " + vertex);
    //    }
    return this.locations.computeIfAbsent(vertex, initializer);
    //    return this.locations.get(vertex);
  }

  @Override
  public Point apply(V vertex) {
    return this.get(vertex);
  }

  @Override
  public void clear() {

    this.locations.clear();
    this.initializer = v -> Point.ORIGIN;
  }
}
