package org.jungrapht.visualization.layout.algorithms.util;

import java.awt.*;
import java.util.Objects;

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
