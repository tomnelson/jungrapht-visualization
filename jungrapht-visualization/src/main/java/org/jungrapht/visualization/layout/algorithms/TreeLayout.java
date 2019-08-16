package org.jungrapht.visualization.layout.algorithms;

import java.util.Map;
import org.jungrapht.visualization.layout.model.Dimension;

public interface TreeLayout<V> {

  Map<V, Dimension> getBaseBounds();
}
