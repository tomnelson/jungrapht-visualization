package org.jungrapht.visualization.util;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * wraps a delegate class and provides Map semantics for attributes
 *
 * @param <T>
 */
public interface Attributed<T> extends Map<String, String> {

  T getValue();

  Map<String, String> getAttributeMap();

  void set(String key, String value);

  /** convenience class to shorten generic declarations */
  class AS extends DefaultAttributed<String> {
    public AS(String value) {
      super(value);
    }
  }

  /** convenience class to shorten generic declarations */
  class AI extends DefaultAttributed<Integer> {
    public AI(Integer value) {
      super(value);
    }
  }

  class AISupplier implements Supplier<AI> {
    private static int i = 1;

    @Override
    public AI get() {
      return new AI(i++);
    }
  }

  class ASSupplier implements Supplier<AS> {
    private static int i = 1;

    @Override
    public AS get() {
      return new AS("" + i++);
    }
  }

  Function<String, AS> AS_FUNCTION = t -> new AS(t);

  Function<Integer, AI> AI_FUNCTION = t -> new AI(t);
}
