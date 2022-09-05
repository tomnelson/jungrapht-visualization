package org.jungrapht.visualization.layout.model;

/** Utility to use where mutable points were required in older code */
public class LocationUtil {

  public static <V> void set(LayoutModel<V> layoutModel, V v, Point p) {
    layoutModel.set(v, p);
  }

  public static <V> void offset(LayoutModel<V> layoutModel, V v, Point p) {
    if (layoutModel.get(v) != null) {
      layoutModel.set(v, layoutModel.get(v).add(p));
    } else {
      layoutModel.set(v, p);
    }
  }
}
