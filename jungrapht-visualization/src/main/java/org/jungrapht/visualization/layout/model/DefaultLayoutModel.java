package org.jungrapht.visualization.layout.model;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.util.Caching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutModel that uses a ConcurrentHashMap. Values not already in the Map are loaded with an
 * initializer and computeIfAbsent.
 *
 * @param <V> the vertex type
 * @author Tom Nelson
 */
public class DefaultLayoutModel<V> extends AbstractLayoutModel<V>
    implements LayoutModel<V>, Caching {

  private static final Logger log = LoggerFactory.getLogger(DefaultLayoutModel.class);

  protected Map<V, Point> locations = new ConcurrentHashMap<>();

  protected Function<V, Point> initializer;

  public static <V> DefaultLayoutModel<V> from(LayoutModel<V> other) {
    return new DefaultLayoutModel<>(other);
  }

  protected DefaultLayoutModel(LayoutModel.Builder builder) {
    super(builder);
    this.initializer = builder.initializer;
  }

  private DefaultLayoutModel(LayoutModel<V> other) {
    super(other.getGraph(), other.getWidth(), other.getHeight());
    if (other instanceof DefaultLayoutModel) {
      this.initializer = ((DefaultLayoutModel) other).initializer;
    }
  }

  @Override
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
    if (location == null) throw new IllegalArgumentException("Location cannot be null");
    if (vertex == null) throw new IllegalArgumentException("vertex cannot be null");
    if (!locked) {
      this.locations.put(vertex, location);
      super.set(vertex, location); // will fire events
    }
  }

  @Override
  public void setGraph(Graph<V, ?> graph) {
    this.locations.clear();
    super.setGraph(graph);
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
