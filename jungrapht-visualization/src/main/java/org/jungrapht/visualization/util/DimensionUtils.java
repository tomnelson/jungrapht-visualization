package org.jungrapht.visualization.util;

import org.jungrapht.visualization.layout.model.Dimension;

public final class DimensionUtils {

  private DimensionUtils() {}

  public static java.awt.Dimension convert(Dimension d) {
    return new java.awt.Dimension(d.width, d.height);
  }

  public static Dimension convert(java.awt.Dimension d) {
    return Dimension.of(d.width, d.height);
  }
}
