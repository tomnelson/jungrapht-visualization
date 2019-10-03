package org.jungrapht.visualization.layout.algorithms;

import com.google.common.collect.Lists;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnotherForestLayoutAlgorithm<V> implements LayoutAlgorithm<V> {
  private static final Logger log = LoggerFactory.getLogger(AnotherForestLayoutAlgorithm.class);
  protected Function<V, Shape> vertexShapeFunction;
  protected Rectangle bounds = Rectangle.IDENTITY;
  protected List<Integer> heights = new ArrayList<>();
  List<V> roots;

  private static class VertexData<V> {
    private int mod;
    private V thread;
    private int shift;
    private V ancestor;
    private int pos;
    private int change;
    private int childCount;
  }

  private final Map<V, VertexData<V>> data = new HashMap<>();

  int horizontalSpacing = 20;
  int verticalSpacing = 20;

  public void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
    this.vertexShapeFunction = vertexShapeFunction;
  }

  protected Graph<V, ?> graph;
  protected LayoutModel<V> layoutModel;

  private VertexData<V> vertexData(V v) {
    if (!data.containsKey(v)) {
      data.put(v, new VertexData<>());
    }
    return data.get(v);
  }

  private void firstWalk(V v, List<V> successors, V leftSibling) {
    if (successors.isEmpty()) {

      if (leftSibling != null) {
        vertexData(v).pos = vertexData(leftSibling).pos + getDistance(v, leftSibling);
      }

    } else {
      V defaultAncestor = v == null ? successors.get(0) : firstChild(v).get();

      log.trace("defaultAncestor is {} when v is {}", defaultAncestor, v);
      V previousChild = null;
      for (V w : successors) {
        firstWalk(w, successors(w), previousChild);
        defaultAncestor = apportion(w, defaultAncestor, previousChild, v);
        log.trace("defaultAncestor is {} when v is {} and w is {}", defaultAncestor, v, w);
        previousChild = w;
      }
      shift(v);

      V firstChild = firstChild(v).get();
      V lastChild = lastChild(v).get();
      int midpoint = (vertexData(firstChild).pos + vertexData(lastChild).pos) / 2;

      log.trace("midpoint for {} is {}", v, midpoint);

      if (leftSibling != null) {
        vertexData(v).pos = vertexData(leftSibling).pos + getDistance(v, leftSibling);
        vertexData(v).mod = vertexData(v).pos - midpoint;
      } else {
        vertexData(v).pos = midpoint;
      }
    }
  }

  private void secondWalk(V v, List<V> successors, int m, int level, int yOffset) {

    int levelHeight = this.heights.get(level);
    int x = vertexData(v).pos + m;
    int y = yOffset + levelHeight / 2;

    if (v != null) layoutModel.set(v, Point.of(x, y));

    updateBounds(v, x, y);

    if (!successors.isEmpty()) {
      yOffset += levelHeight + verticalSpacing;
      for (V w : successors) {
        secondWalk(w, successors(w), m + vertexData(v).mod, level + 1, yOffset);
      }
    }
  }

  private void updateBounds(V vertex, int centerX, int centerY) {
    if (vertex != null) {
      Shape shape = vertexShapeFunction.apply(vertex);
      int width = shape.getBounds().width;
      int height = shape.getBounds().height;
      int left = centerX - width / 2;
      int right = centerX + width / 2;
      int top = centerY - height / 2;
      int bottom = centerY + height / 2;

      Rectangle bounds = Rectangle.of(left, top, right - left, bottom - top);
      this.bounds = this.bounds.add(bounds);
      log.trace(
          "updated bounds to {} ({}, {}, {}, {})",
          bounds,
          bounds.x,
          bounds.maxX,
          bounds.y,
          bounds.maxY);
    }
  }

  private Point normalize(Point p) {
    return Point.of(p.x - bounds.x, p.y - bounds.y);
  }

  private void computeMaxHeights(V vertex, List<V> successors, int depth) {
    int previous;
    if (heights.size() > depth) {
      previous = heights.get(depth);
    } else {
      heights.add(0);
      previous = 0;
    }
    int height;
    if (vertex == null) {
      height = 0;
    } else {
      height = vertexShapeFunction.apply(vertex).getBounds().height;
    }
    heights.set(depth, Math.max(height, previous));

    successors.forEach(v -> computeMaxHeights(v, successors(v), depth + 1));
  }

  private void moveSubtree(V leftVertex, V rightVertex, V parentVertex, int shift) {
    int subtreeCount =
        this.childPosition(rightVertex, parentVertex)
            - this.childPosition(leftVertex, parentVertex);
    //    if (subtreeCount == 0) subtreeCount = 1;
    VertexData<V> rightData = vertexData(rightVertex);
    VertexData<V> leftData = vertexData(leftVertex);
    rightData.change -= shift / subtreeCount;
    rightData.shift += shift;

    leftData.change += shift / subtreeCount;
    rightData.pos = vertexData(rightVertex).pos + shift;
    rightData.mod += shift;
  }

  private int childPosition(V vertex, V parentNode) {
    List<V> successors;
    if (parentNode == null) {
      successors = roots;
    } else {
      successors = successors(parentNode);
    }
    if (vertexData(vertex).childCount != 0) {
      return vertexData(vertex).childCount;
    }
    int i = 1;
    for (V v : successors) {
      vertexData(v).childCount = i++;
    }

    return vertexData(vertex).childCount;
  }

  private V ancestor(V leftInsideVertex, V parentOfV, V defaultAncestor) {
    V ancestor =
        Optional.ofNullable(vertexData(leftInsideVertex).ancestor).orElse(leftInsideVertex);

    //   V vp = predecessors(ancestor).stream().filter(p -> p != null).findAny().orElse(null);

    for (V vp : predecessors(ancestor))
      if (vp == parentOfV) {
        return ancestor;
      }
    return defaultAncestor;
  }

  private V apportion(V v, V defaultAncestor, V leftSibling, V parentOfV) {
    if (leftSibling == null) {
      return defaultAncestor;
    }

    V rightOutsideContourVertex = v;
    V rightInsideContourVertex = v;
    V leftInsideContourVertex = leftSibling;
    V leftOutsideContourVertex =
        parentOfV != null ? successors(parentOfV).get(0) : leftInsideContourVertex;

    int insideRight = vertexData(rightInsideContourVertex).mod;
    int outsideRight = vertexData(rightOutsideContourVertex).mod;
    int insideLeft = vertexData(leftInsideContourVertex).mod;
    int outsideLeft = vertexData(leftOutsideContourVertex).mod;

    V nextRightInsideLeftVertex = rightChild(leftInsideContourVertex);
    V nextLeftInsideRightVertex = leftChild(rightInsideContourVertex);

    while (nextRightInsideLeftVertex != null && nextLeftInsideRightVertex != null) {
      leftInsideContourVertex = nextRightInsideLeftVertex;
      rightInsideContourVertex = nextLeftInsideRightVertex;
      leftOutsideContourVertex = leftChild(leftOutsideContourVertex);
      rightOutsideContourVertex = rightChild(rightOutsideContourVertex);
      vertexData(rightOutsideContourVertex).ancestor = v;
      int shift =
          (vertexData(leftInsideContourVertex).pos + insideLeft)
              - (vertexData(rightInsideContourVertex).pos + insideRight)
              + getDistance(leftInsideContourVertex, rightInsideContourVertex);

      if (shift > 0) {
        moveSubtree(
            ancestor(leftInsideContourVertex, parentOfV, defaultAncestor), v, parentOfV, shift);
        insideRight = insideRight + shift;
        outsideRight = outsideRight + shift;
      }
      insideLeft += vertexData(leftInsideContourVertex).mod;
      insideRight += vertexData(rightInsideContourVertex).mod;
      outsideLeft += vertexData(leftOutsideContourVertex).mod;
      outsideRight += vertexData(rightOutsideContourVertex).mod;

      nextRightInsideLeftVertex = rightChild(leftInsideContourVertex);
      nextLeftInsideRightVertex = leftChild(rightInsideContourVertex);
    }

    if (nextRightInsideLeftVertex != null && rightChild(rightOutsideContourVertex) == null) {
      vertexData(rightOutsideContourVertex).thread = nextRightInsideLeftVertex;
      vertexData(rightOutsideContourVertex).mod =
          vertexData(rightOutsideContourVertex).mod + insideLeft - outsideRight;
    }

    if (nextLeftInsideRightVertex != null && leftChild(leftOutsideContourVertex) == null) {
      vertexData(leftOutsideContourVertex).thread = nextLeftInsideRightVertex;
      vertexData(leftOutsideContourVertex).mod += insideRight - outsideLeft;
      defaultAncestor = v;
    }
    return defaultAncestor;
  }

  private void shift(V v) {
    List<V> successors;
    if (v == null) {
      successors = roots;
    } else {
      successors = successors(v);
    }
    int shift = 0;
    int change = 0;
    List<V> children = Lists.reverse(successors);
    //    Collections.reverse(children);

    for (V w : children) {
      change += vertexData(w).change;
      vertexData(w).pos += shift;
      vertexData(w).mod += shift;
      shift += vertexData(w).shift + change;
    }
  }

  private List<V> successors(V v) {
    if (v == null) return roots;
    return Graphs.successorListOf(graph, v);
  }

  private List<V> predecessors(V v) {
    return Graphs.predecessorListOf(graph, v);
  }

  private V leftChild(V v) {
    return firstChild(v).orElse(vertexData(v).thread);
  }

  private V rightChild(V v) {
    return lastChild(v).orElse(vertexData(v).thread);
  }

  private Optional<V> firstChild(V v) {
    return successors(v).stream().findFirst();
  }

  private Optional<V> lastChild(V v) {
    return successors(v).stream().reduce((first, second) -> second);
  }

  private int getDistance(V v, V w) {
    int sizeOfNodes =
        vertexShapeFunction.apply(v).getBounds().width
            + vertexShapeFunction.apply(w).getBounds().width;

    int distance = sizeOfNodes / 2 + horizontalSpacing;
    return distance;
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    this.layoutModel = layoutModel;
    this.graph = layoutModel.getGraph();
    this.roots =
        graph
            .vertexSet()
            .stream()
            .filter(v -> Graphs.predecessorListOf(graph, v).isEmpty())
            .collect(Collectors.toList());
    if (roots.size() == 1) {
      V root = roots.get(0);
      firstWalk(root, successors(root), null);
      computeMaxHeights(root, successors(root), 0);
      secondWalk(root, successors(root), -vertexData(root).pos, 0, 0);
    } else {
      V root = (V) new Object();
      graph.addVertex(root);
      roots.forEach(r -> graph.addEdge(root, r));
      firstWalk(root, roots, null);
      computeMaxHeights(root, roots, 0);
      secondWalk(root, roots, 0, 0, 0);
      graph.removeVertex(root);
    }
    // normalize all the layoutModel points
    // and center in the layout area
    int xoffset = (int) (layoutModel.getWidth() - this.bounds.width) / 2;
    int yoffset = (int) (layoutModel.getHeight() - this.bounds.height) / 2;
    for (Map.Entry<V, Point> entry : layoutModel.getLocations().entrySet()) {
      Point p = entry.getValue();
      Point np = normalize(p);
      np = np.add(xoffset, yoffset);
      layoutModel.set(entry.getKey(), np);
    }
  }
}
