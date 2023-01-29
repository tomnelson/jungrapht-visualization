package org.jungrapht.visualization.layout.util;

import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

/**
 * When passed as the after function to a LayoutAlgorithm, will place the points from left to right
 * instead of top to bottom
 *
 * @param <V>
 */
public class LeftToRight<V> implements Runnable {
  LayoutModel<V> layoutModel;

  public LeftToRight(LayoutModel layoutModel) {
    this.layoutModel = layoutModel;
  }

  @Override
  public void run() {
    layoutModel
        .getLocations()
        .forEach((v, p) -> layoutModel.set(v, Point.of(p.y, layoutModel.getWidth() - p.x)));
  }
}
