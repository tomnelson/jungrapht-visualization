/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */

package org.jungrapht.visualization.layout.algorithms;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class MultiRowTreeLayoutAlgorithm<V> extends TreeLayoutAlgorithm<V>
    implements LayoutAlgorithm<V> {

  private static final Logger log = LoggerFactory.getLogger(MultiRowTreeLayoutAlgorithm.class);

  public static class Builder<
          V, T extends MultiRowTreeLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      extends TreeLayoutAlgorithm.Builder<V, T, B> {

    public T build() {
      return (T) new MultiRowTreeLayoutAlgorithm<>(this);
    }
  }

  public static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  protected MultiRowTreeLayoutAlgorithm(Builder<V, ?, ?> builder) {
    super(builder);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param horizontalVertexSpacing the horizontal spacing between adjacent siblings
   * @param verticalVertexSpacing the vertical spacing between adjacent siblings
   */
  protected MultiRowTreeLayoutAlgorithm(
      int horizontalVertexSpacing, int verticalVertexSpacing, boolean expandLayout) {
    super(horizontalVertexSpacing, verticalVertexSpacing, expandLayout);
  }

  protected int rowCount = 1;

  /**
   * @param layoutModel the model to hold vertex positions
   * @return the roots vertices of the tree
   */
  @Override
  protected Set<V> buildTree(LayoutModel<V> layoutModel) {
    rowCount = 1;
    alreadyDone = Sets.newHashSet();
    Graph<V, ?> graph = layoutModel.getGraph();
    Set<V> roots =
        graph
            .vertexSet()
            .stream()
            .filter(vertex -> Graphs.predecessorListOf(graph, vertex).isEmpty())
            .collect(toImmutableSet());

    Preconditions.checkArgument(roots.size() > 0);
    // the width of the tree under 'roots'. Includes one 'horizontalVertexSpacing' per child vertex
    int overallWidth = calculateWidth(layoutModel, roots, new HashSet<>());
    log.debug("after calculating overallWidth {}, row count is {}", overallWidth, rowCount);
    int tallestTreeHeight = calculateOverallHeight(layoutModel, roots);
    int overallHeight = tallestTreeHeight; // * rowCount;
    overallHeight += verticalVertexSpacing;

    log.trace("layoutModel.getWidth() {}", layoutModel.getWidth());
    log.trace("overallWidth {}", overallWidth);
    int largerHeight = Math.max(layoutModel.getHeight(), overallHeight);
    if (expandLayout) {
      layoutModel.setSize(layoutModel.getWidth(), largerHeight);
    }
    log.trace("layoutModel.getHeight() {}", layoutModel.getHeight());
    log.trace("overallHeight {}", overallHeight);

    int cursor = horizontalVertexSpacing;
    int y = 0;
    log.trace("got initial y of {}", y);

    Set<V> rootsInRow = new HashSet<>();
    for (V vertex : roots) {

      int w = this.baseBounds.get(vertex).width;
      log.trace("w is {} and baseWidths.get({}) = {}", w, vertex, baseBounds.get(vertex));
      cursor += w;
      cursor += horizontalVertexSpacing;

      if (cursor > layoutModel.getWidth()) {
        cursor = horizontalVertexSpacing;
        cursor += w;
        cursor += horizontalVertexSpacing;
        int rowHeight = calculateHeight(layoutModel, rootsInRow);
        log.trace("height for {} is {}", rootsInRow, rowHeight);
        y += rowHeight;
        rootsInRow.clear();
      }
      rootsInRow.add(vertex);
      int x = cursor - horizontalVertexSpacing - w / 2;
      buildTree(layoutModel, vertex, x, y);
    }
    // last row
    int rowHeight = calculateHeight(layoutModel, rootsInRow);
    log.trace("height for (last) {} is {}", rootsInRow, rowHeight);
    log.debug("rowCount is {}", rowCount);
    return roots;
  }

  @Override
  protected int calculateWidth(LayoutModel<V> layoutModel, Collection<V> roots, Set<V> seen) {
    int overallWidth = 0;
    int cursor = horizontalVertexSpacing;
    for (V root : roots) {
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
    log.trace("entire width from {} is {}", roots, overallWidth);
    return overallWidth;
  }

  protected int calculateOverallHeight(LayoutModel<V> layoutModel, Collection<V> roots) {

    int overallHeight = 0;
    int cursor = horizontalVertexSpacing;
    Set<V> rootsInRow = new HashSet<>();
    for (V root : roots) {
      int w = calculateWidth(layoutModel, root, new HashSet<>());
      cursor += w;
      cursor += horizontalVertexSpacing;
      log.trace("width of {} is {}", root, w);
      if (cursor > layoutModel.getWidth()) {
        cursor = horizontalVertexSpacing;
        cursor += w;
        cursor += horizontalVertexSpacing;
        overallHeight += super.calculateHeight(layoutModel, rootsInRow);
        rootsInRow.clear();
      }
      rootsInRow.add(root);
    }
    // last row
    overallHeight += super.calculateHeight(layoutModel, rootsInRow);
    return overallHeight;
  }
}
