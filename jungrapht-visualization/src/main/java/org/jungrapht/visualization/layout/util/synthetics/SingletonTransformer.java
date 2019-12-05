package org.jungrapht.visualization.layout.util.synthetics;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SingletonTransformer<S, T> implements Function<S, T> {

  Map<S, T> transformedMap = new HashMap<>();
  Function<S, T> transformFunction;

  public SingletonTransformer(Function<S, T> transformFunction) {
    this.transformFunction = transformFunction;
  }

  @Override
  public T apply(S s) {
    if (!transformedMap.containsKey(s)) {
      transformedMap.put(s, transformFunction.apply(s));
    }
    return transformedMap.get(s);
  }

  public Map<S, T> getTransformedMap() {
    return transformedMap;
  }
}
