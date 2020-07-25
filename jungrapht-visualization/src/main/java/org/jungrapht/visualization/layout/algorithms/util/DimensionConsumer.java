package org.jungrapht.visualization.layout.algorithms.util;

import java.util.Objects;
import org.jungrapht.visualization.layout.model.Dimension;

@FunctionalInterface
public interface DimensionConsumer {
  void accept(Dimension dimension);

  default DimensionConsumer andThen(DimensionConsumer after) {
    Objects.requireNonNull(after);
    return (t) -> {
      this.accept(t);
      after.accept(t);
    };
  }
}
