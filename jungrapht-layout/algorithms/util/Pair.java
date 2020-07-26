package org.jungrapht.visualization.layout.algorithms.util;

import java.util.Objects;

public class Pair<V> {
  public final V first;
  public final V second;

  public static <V> Pair<V> of(V first, V second) {
    return new Pair(first, second);
  }

  private Pair(V first, V second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Pair<?> pair = (Pair<?>) o;
    return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }

  @Override
  public String toString() {
    return "Pair{" + first + "," + second + '}';
  }
}
