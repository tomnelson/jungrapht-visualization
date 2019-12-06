package org.jungrapht.visualization.layout.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.jungrapht.visualization.layout.util.Caching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutModel that uses a lazy loading
 *
 * @param <V> the vertex type
 * @author Tom Nelson
 */
public class DefaultLayoutModel<V> extends AbstractLayoutModel<V>
    implements LayoutModel<V>, Caching {

  private static final Logger log = LoggerFactory.getLogger(DefaultLayoutModel.class);

  protected Map<V, Point> locations = new HashMap<>();

  Function<V, Point> initializer;

  /**
   * a builder for LoadingCache instances
   *
   * @param <V> the vertex type
   * @param <T> the type of the superclass of the LayoutModel to be built
   */
  public static class Builder<V, T extends DefaultLayoutModel<V>, B extends Builder<V, T, B>>
      extends AbstractLayoutModel.Builder<V, T, B> {

    Function<V, Point> initializer = v -> Point.ORIGIN;

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
      return (B) this;
    }

    /**
     * build an instance of the requested LayoutModel of type T
     *
     * @return
     */
    public T build() {
      return (T) new DefaultLayoutModel<>(this);
    }
  }

  public static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  public static <V> DefaultLayoutModel<V> from(DefaultLayoutModel<V> other) {
    return new DefaultLayoutModel<>(other);
  }

  protected DefaultLayoutModel(DefaultLayoutModel.Builder builder) {
    super(builder);
    this.initializer = builder.initializer;
  }

  private DefaultLayoutModel(DefaultLayoutModel<V> other) {
    super(other.graph, other.width, other.height);
    this.initializer = other.initializer;
  }

  public void setInitializer(Function<V, Point> initializer) {
    this.initializer = initializer;
  }

  @Override
  public void set(V vertex, Point location) {
    assert location != null : "Location cannot be null";
    assert vertex != null : "vertex cannot be null";
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
    return this.locations.computeIfAbsent(vertex, initializer);
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
