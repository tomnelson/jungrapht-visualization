package org.jungrapht.visualization.layout.algorithms.util;

import java.util.Objects;
import org.jungrapht.visualization.layout.model.Rectangle;

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
