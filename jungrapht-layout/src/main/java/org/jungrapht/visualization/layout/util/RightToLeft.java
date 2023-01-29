package org.jungrapht.visualization.layout.util;

import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

/**
 * When passed as the after function to a LayoutAlgorithm, will place the points from right to left
 * instead of top to bottom
 *
 * @param <V>
 */
public class RightToLeft<V> implements Runnable {
  LayoutModel<V> layoutModel;

  public RightToLeft(LayoutModel layoutModel) {
    this.layoutModel = layoutModel;
  }

  @Override
  public void run() {
    layoutModel
        .getLocations()
        .forEach((v, p) -> layoutModel.set(v, Point.of(layoutModel.getHeight() - p.y, p.x)));
  }
}
