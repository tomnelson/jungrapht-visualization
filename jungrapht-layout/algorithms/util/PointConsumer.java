package org.jungrapht.visualization.layout.algorithms.util;

import java.util.Objects;
import org.jungrapht.visualization.layout.model.Point;

@FunctionalInterface
public interface PointConsumer {
  void accept(Point p);

  default PointConsumer andThen(PointConsumer after) {
    Objects.requireNonNull(after);
    return (t) -> {
      this.accept(t);
      after.accept(t);
    };
  }
}
