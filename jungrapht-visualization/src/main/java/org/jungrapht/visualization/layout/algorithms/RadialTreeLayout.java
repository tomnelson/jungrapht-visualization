package org.jungrapht.visualization.layout.algorithms;

import java.util.Map;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.jungrapht.visualization.layout.model.Rectangle;

/**
 * a marker interface for Tree layouts in a radial pattern
 *
 * @param <V>
 */
public interface RadialTreeLayout<V> {

  Map<V, Rectangle> getBaseBounds();

  Map<V, PolarPoint> getPolarLocations();

  Point getCenter(LayoutModel<V> layoutModel);
}
