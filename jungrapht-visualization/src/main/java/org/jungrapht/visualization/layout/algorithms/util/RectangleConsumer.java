package org.jungrapht.visualization.layout.algorithms.util;

import java.awt.*;
import java.util.Objects;

@FunctionalInterface
public interface RectangleConsumer {
  void accept(Rectangle rectangle);

  default RectangleConsumer andThen(RectangleConsumer after) {
    Objects.requireNonNull(after);
    return (t) -> {
      this.accept(t);
      after.accept(t);
    };
  }
}
