package org.jungrapht.visualization.layout.model;

import java.util.Collections;
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

  public static <V> DefaultLayoutModel<V> from(DefaultLayoutModel<V> other) {
    return new DefaultLayoutModel<>(other);
  }

  protected DefaultLayoutModel(LayoutModel.Builder builder) {
    super(builder);
    this.initializer = builder.initializer;
  }

  private DefaultLayoutModel(DefaultLayoutModel<V> other) {
    super(other.graph, other.width, other.height);
    this.initializer = other.initializer;
  }

  public void setInitializer(Function<V, Point> initializer) {
    this.initializer = initializer;
    this.locations.clear();
  }

  @Override
  public Map<V, Point> getLocations() {
    return Collections.unmodifiableMap(this.locations);
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
