package org.jungrapht.visualization.spatial.rtree;

import java.awt.geom.Rectangle2D;

/**
 * @author Tom Nelson
 * @param <T>
 */
public interface BoundedMap<T> extends java.util.Map<T, Rectangle2D> {

  Rectangle2D getBounds();

  void recalculateBounds();
}
