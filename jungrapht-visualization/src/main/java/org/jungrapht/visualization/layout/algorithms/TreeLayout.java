package org.jungrapht.visualization.layout.algorithms;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.Shape;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jungrapht.visualization.layout.model.Rectangle;

/**
 * an interface for {@code LayoutAlgorithm} that place their vertices in a tree inke heirarchy
 *
 * @param <V>
 */
public interface TreeLayout<V> extends LayoutAlgorithm<V> {

  int TREE_LAYOUT_HORIZONTAL_SPACING =
      Integer.getInteger(PREFIX + "treeLayoutHorizontalSpacing", 50);
  int TREE_LAYOUT_VERTICAL_SPACING = Integer.getInteger(PREFIX + "treeLayoutVerticalSpacing", 50);

  Map<V, Rectangle> getBaseBounds();

  void setRootPredicate(Predicate<V> rootPredicate);

  void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction);
}
