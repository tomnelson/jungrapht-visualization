package org.jungrapht.visualization.layout.algorithms;

import static org.jungrapht.visualization.layout.model.LayoutModel.PREFIX;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.Rectangle;

/**
 * an interface for {@code LayoutAlgorithm} that place their vertices in a tree like heirarchy
 *
 * @param <V>
 */
public interface TreeLayout<V> extends LayoutAlgorithm<V> {

  int TREE_LAYOUT_HORIZONTAL_SPACING =
      Integer.getInteger(PREFIX + "treeLayoutHorizontalSpacing", 50);
  int TREE_LAYOUT_VERTICAL_SPACING = Integer.getInteger(PREFIX + "treeLayoutVerticalSpacing", 50);

  Map<V, Rectangle> getBaseBounds();

  void setRootPredicate(Predicate<V> rootPredicate);

  void setRootComparator(Comparator<V> rootComparator);

  void setVertexShapeFunction(Function<V, Rectangle> vertexShapeFunction);

  static <V, E> boolean isLoopVertex(Graph<V, E> graph, V v) {
    return false; //graph.outgoingEdgesOf(v).equals(graph.incomingEdgesOf(v));
  }

  static <V, E> boolean isZeroDegreeVertex(Graph<V, E> graph, V v) {
    return graph.degreeOf(v) == 0;
  }

  static <V, E> boolean isIsolatedVertex(Graph<V, E> graph, V v) {
    return isLoopVertex(graph, v) || isZeroDegreeVertex(graph, v);
  }

  /**
   * to set vertex order to normal -> loop -> zeroDegree
   *
   * @param graph
   * @param v
   * @param <V>
   * @param <E>
   * @return
   */
  static <V, E> int vertexIsolationScore(Graph<V, E> graph, V v) {
    if (isZeroDegreeVertex(graph, v)) return 2;
    if (isLoopVertex(graph, v)) return 1;
    return 0;
  }
}
