package org.jungrapht.visualization.layout.algorithms;

import java.util.Map;
import org.jungrapht.visualization.layout.model.Rectangle;

/**
 * an interface for {@code LayoutAlgorithm} that place their vertices in a tree inke heirarchy
 *
 * @param <V>
 */
public interface TreeLayout<V> extends LayoutAlgorithm<V> {

  Map<V, Rectangle> getBaseBounds();
}