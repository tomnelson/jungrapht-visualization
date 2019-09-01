package org.jungrapht.visualization.renderers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Renderer} that delegates to either a {@link HeavyweightRenderer} or a {@link
 * LightweightRenderer} depending on the results of a count predicate and a scale predicate
 *
 * <p>The count predicate defaults to a comparison of the vertex count with the
 * lightweightCountThreshold
 *
 * <p>The scale predicate defauls to a comparison of the VIEW transform scale with the
 * lightweightScaleThreshold
 *
 * <p>if the scale threshold is less than 0.5, then the graph is always drawn with the lightweight
 * renderer
 *
 * <p>if the vertex count is less than (for example) 20 the the graph is always drawn with the
 * default renderer
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class DefaultModalRenderer<V, E> extends BiModalRenderer<V, E>
    implements ModalRenderer<V, E> {

  private static final Logger log = LoggerFactory.getLogger(DefaultModalRenderer.class);

  public static class Builder<
          V,
          E,
          M extends Enum<M>,
          T extends DefaultModalRenderer<V, E>,
          B extends Builder<V, E, M, T, B>>
      extends BiModalRenderer.Builder<V, E, T, B> {
    public T build() {
      return (T) new DefaultModalRenderer<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?, ?> builder() {
    return new Builder();
  }

  public DefaultModalRenderer(Builder<V, E, ?, ?, ?> builder) {
    super(builder);
  }
}
