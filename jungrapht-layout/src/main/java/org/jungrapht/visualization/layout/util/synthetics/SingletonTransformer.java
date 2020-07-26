package org.jungrapht.visualization.layout.util.synthetics;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Transform the key of type S to a value of type T. If the transform has been accessed before,
 * return that cached result.
 *
 * @param <S> the key type
 * @param <T> the value type
 */
public class SingletonTransformer<S, T> implements Function<S, T> {

  Map<S, T> transformedMap = new HashMap<>();
  Function<S, T> transformFunction;

  public SingletonTransformer(Function<S, T> transformFunction) {
    this.transformFunction = transformFunction;
  }

  @Override
  public T apply(S s) {
    return transformedMap.computeIfAbsent(s, transformFunction);
  }

  public Map<S, T> getTransformedMap() {
    return transformedMap;
  }
}
