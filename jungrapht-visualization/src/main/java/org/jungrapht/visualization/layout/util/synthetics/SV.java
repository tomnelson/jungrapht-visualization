package org.jungrapht.visualization.layout.util.synthetics;

/**
 * A delegate class for a generic type V
 *
 * @param <V> vertex type
 */
public interface SV<V> {

  static <V> SV<V> of(V vertex) {
    return new SVI(vertex);
  }

  V getVertex();
}
