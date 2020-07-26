package org.jungrapht.visualization.layout.algorithms;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.Dimension;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.util.Caching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code TreeLayoutAlgorithm} that adheres to either an original width or to the width of the
 * widest subtree in the forest by creating new rows of tree roots
 *
 * @author Tom Nelson
 */
public class MultiRowTreeLayoutAlgorithm<V> extends TreeLayoutAlgorithm<V>
    implements LayoutAlgorithm<V> {

  private static final Logger log = LoggerFactory.getLogger(MultiRowTreeLayoutAlgorithm.class);

  public static class Builder<
          V, T extends MultiRowTreeLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      extends TreeLayoutAlgorithm.Builder<V, T, B> implements LayoutAlgorithm.Builder<V, T, B> {

    public T build() {
      return (T) new MultiRowTreeLayoutAlgorithm<>(this);
    }
  }

  public static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  public MultiRowTreeLayoutAlgorithm() {
    this(MultiRowTreeLayoutAlgorithm.builder());
  }

  protected MultiRowTreeLayoutAlgorithm(Builder<V, ?, ?> builder) {
    super(builder);
  }

  /** keeps track of how many rows have been created */
  protected int rowCount = 1;

  /**
   * visit a {@link LayoutModel} to set all of the graph vertex positions according to the
   * LayoutAlgorithm logic.
   *
   * @param layoutModel the mediator between the container for vertices (the Graph) and the mapping
   */
  @Override
  public void visit(LayoutModel<V> layoutModel) {
    super.visit(layoutModel);
  }

  /**
   * Build the entire forest, first measuring the width and height, then possibly expanding the
   * layout area, then placing the vertices under rows of tree roots
   *
   * @param layoutModel the model to hold vertex positions
   * @return the roots vertices of the tree
   */
  @Override
  protected Set<V> buildTree(LayoutModel<V> layoutModel) {
    rowCount = 1;
    Graph<V, ?> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return Collections.emptySet();
    }
    if (layoutModel instanceof Caching) {
      ((Caching) layoutModel).clear();
    }

    this.defaultRootPredicate =
        v -> graph.incomingEdgesOf(v).isEmpty() || TreeLayout.isIsolatedVertex(graph, v);
    if (vertexBoundsFunction != null) {
      Dimension averageVertexSize = computeAverageVertexDimension(graph, vertexBoundsFunction);
      this.horizontalVertexSpacing = averageVertexSize.width * 2;
      this.verticalVertexSpacing = averageVertexSize.height * 2;
    }
    if (this.rootPredicate == null) {
      this.rootPredicate = this.defaultRootPredicate;
    } else {
      this.rootPredicate = this.rootPredicate.or(this.defaultRootPredicate);
    }
    Set<V> roots =
        graph
            .vertexSet()
            .stream()
            .filter(rootPredicate)
            .sorted(Comparator.comparingInt(v -> TreeLayout.vertexIsolationScore(graph, v)))
            .collect(Collectors.toCollection(LinkedHashSet::new));

    if (roots.size() == 0) {
      Graph<V, ?> tree = TreeLayoutAlgorithm.getSpanningTree(graph);
      layoutModel.setGraph(tree);
      Set<V> treeRoots = buildTree(layoutModel);
      return treeRoots;
    }
    int overallWidth = calculateWidth(layoutModel, roots, new HashSet<>());
    int overallHeight = calculateOverallHeight(layoutModel, roots, overallWidth);

    int cursor =
        horizontalVertexSpacing; //(horizontalVertexSpacing, layoutModel.getWidth(), overallWidth);

    int y = 0; //getInitialPosition(0, layoutModel.getHeight(), overallHeight);

    Set<V> rootsInRow = new HashSet<>();
    Set<V> seen = new HashSet<>();
    Set<V> seenForHeight = new HashSet<>();
    for (V vertex : roots) {
      if (!seen.contains(vertex)) {
        int w = (int) this.baseBounds.get(vertex).width;
        cursor += w;
        cursor += horizontalVertexSpacing;

        if (cursor > layoutModel.getWidth()) {
          cursor =
              getInitialPosition(horizontalVertexSpacing, layoutModel.getWidth(), overallWidth);
          cursor += w;
          cursor += horizontalVertexSpacing;
          int rowHeight = calculateHeight(layoutModel, rootsInRow, seenForHeight);
          y += rowHeight;
          rootsInRow.clear();
        }
        rootsInRow.add(vertex);
        int x = cursor - horizontalVertexSpacing - w / 2;
        buildTree(layoutModel, vertex, x, y, seen);
        merge(layoutModel, vertex);
      }
    }
    // last row
    int rowHeight = calculateHeight(layoutModel, rootsInRow, seenForHeight);
    if (expandLayout) {
      expandToFill(layoutModel);
    }
    return roots;
  }

  /**
   * Calculate the width of the entire forest
   *
   * @param layoutModel the source of the graph and its vertices
   * @param roots the root vertices of the forest
   * @param seen a set of vertices that were already placed
   * @return
   */
  @Override
  protected int calculateWidth(LayoutModel<V> layoutModel, Collection<V> roots, Set<V> seen) {
    int overallWidth = 0;
    int cursor = horizontalVertexSpacing;
    for (V root : roots) {
      if (!seen.contains(root)) {
        int w = calculateWidth(layoutModel, root, seen);
        cursor += w;
        cursor += horizontalVertexSpacing;
        log.trace("width of {} is {}", root, w);
        if (cursor > layoutModel.getWidth()) {
          cursor = horizontalVertexSpacing;
          cursor += w;
          cursor += horizontalVertexSpacing;
          rowCount++;
          log.trace("row count now {}", rowCount);
        }
        overallWidth = Math.max(cursor, overallWidth);
      }
    }
    log.trace("entire width from {} is {}", roots, overallWidth);
    return overallWidth;
  }

  /**
   * Calculate the overall height of the entire forest
   *
   * @param layoutModel the source of the graph and its vertices
   * @param roots the roots of the trees in the forest
   * @param overallWidth the previously measured overall width of the forest
   * @return the overall height
   */
  protected int calculateOverallHeight(
      LayoutModel<V> layoutModel, Collection<V> roots, int overallWidth) {

    int overallHeight = 0;
    int cursor = horizontalVertexSpacing;
    Set<V> rootsInRow = new HashSet<>();
    Set<V> seenForWidth = new HashSet<>();
    Set<V> seenForHeight = new HashSet<>();
    for (V root : roots) {
      if (!seenForHeight.contains(root) && !seenForWidth.contains(root)) {
        int w = calculateWidth(layoutModel, root, seenForWidth);
        cursor += w;
        cursor += horizontalVertexSpacing;
        log.trace("width of {} is {}", root, w);
        if (cursor > overallWidth) {
          cursor = horizontalVertexSpacing;
          cursor += w;
          cursor += horizontalVertexSpacing;
          overallHeight += super.calculateHeight(layoutModel, rootsInRow, seenForHeight);
          rootsInRow.clear();
        }
        rootsInRow.add(root);
      }
    }
    // last row
    overallHeight += super.calculateHeight(layoutModel, rootsInRow, seenForHeight);
    return overallHeight;
  }

  public boolean constrained() {
    return false;
  }
}
