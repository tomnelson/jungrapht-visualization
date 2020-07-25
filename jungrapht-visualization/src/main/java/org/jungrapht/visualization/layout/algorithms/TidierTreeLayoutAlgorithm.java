package org.jungrapht.visualization.layout.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.util.ComponentGrouping;
import org.jungrapht.visualization.layout.algorithms.util.TreeView;
import org.jungrapht.visualization.layout.algorithms.util.VertexBoundsFunctionConsumer;
import org.jungrapht.visualization.layout.model.Dimension;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.jungrapht.visualization.layout.util.Caching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A more compact tree layout algorithm. This implementation has been extended to allow multiple
 * roots and to allow drawing of any directed graph for which a root or roots can be discerned.
 *
 * @see "Tidier Drawings of Trees, EDWARD M. REINGOLD AND JOHN S. TILFORD"
 * @param <V> vertex type
 * @param <E> edge type
 */
public class TidierTreeLayoutAlgorithm<V, E> extends AbstractTreeLayoutAlgorithm<V>
    implements LayoutAlgorithm<V>,
        TreeLayout<V>,
        VertexBoundsFunctionConsumer<V>,
        EdgeAwareLayoutAlgorithm<V, E>,
        EdgeSorting<E>,
        EdgePredicated<E>,
        VertexSorting<V>,
        VertexPredicated<V> {

  private static final Logger log = LoggerFactory.getLogger(TidierTreeLayoutAlgorithm.class);

  private static final Rectangle IDENTITY_SHAPE = Rectangle.of(-5, -5, 10, 10);

  /**
   * a Builder to create a configured instance of an AnotherTreeLayoutAlgorithm
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  public static class Builder<
          V, E, T extends TidierTreeLayoutAlgorithm<V, E>, B extends Builder<V, E, T, B>>
      extends AbstractTreeLayoutAlgorithm.Builder<V, T, B>
      implements LayoutAlgorithm.Builder<V, T, B>, EdgeAwareLayoutAlgorithm.Builder<V, E, T, B> {
    protected Predicate<V> vertexPredicate = v -> false;
    protected Predicate<E> edgePredicate = e -> false;
    protected Comparator<V> vertexComparator = (v1, v2) -> 0;
    protected Comparator<E> edgeComparator = (e1, e2) -> 0;

    /**
     * @param vertexPredicate {@link Predicate} to apply to vertices
     * @return this Builder
     */
    public B vertexPredicate(Predicate<V> vertexPredicate) {
      this.vertexPredicate = vertexPredicate;
      return self();
    }

    /**
     * @param edgePredicate {@link Predicate} to apply to edges
     * @return this Builder
     */
    public B edgePredicate(Predicate<E> edgePredicate) {
      this.edgePredicate = edgePredicate;
      return self();
    }

    /**
     * @param vertexComparator {@link Comparator} to sort vertices
     * @return this Builder
     */
    public B vertexComparator(Comparator<V> vertexComparator) {
      this.vertexComparator = vertexComparator;
      return self();
    }

    /**
     * @param edgeComparator {@link Comparator} to sort edges
     * @return this Builder
     */
    public B edgeComparator(Comparator<E> edgeComparator) {
      this.edgeComparator = edgeComparator;
      return self();
    }

    /** {@inheritDoc} */
    public T build() {
      return (T) new TidierTreeLayoutAlgorithm<>(this);
    }
  }

  /**
   * @param <V> vertex type
   * @param <E> edge type
   * @return a Builder ready to configure
   */
  public static <V, E> Builder<V, E, ?, ?> edgeAwareBuilder() {
    return new Builder<>();
  }

  protected Rectangle bounds = Rectangle.IDENTITY;
  protected List<Integer> heights = new ArrayList<>();
  protected List<V> roots;
  protected Predicate<V> builderRootPredicate;
  protected Predicate<V> vertexPredicate;
  protected Predicate<E> edgePredicate;
  protected Comparator<V> vertexComparator;
  protected Comparator<E> edgeComparator;
  protected Graph<V, E> tree;
  protected LayoutModel<V> layoutModel;

  private static class VertexData<V> {
    private int mod;
    private V thread;
    private int shift;
    private V ancestor;
    private int x;
    private int change;
    private int childCount;
  }

  public TidierTreeLayoutAlgorithm() {
    this(TidierTreeLayoutAlgorithm.edgeAwareBuilder());
  }

  protected TidierTreeLayoutAlgorithm(Builder builder) {
    super(builder);
    this.vertexPredicate = builder.vertexPredicate;
    this.vertexComparator = builder.vertexComparator;
    this.edgePredicate = builder.edgePredicate;
    this.edgeComparator = builder.edgeComparator;
  }

  private final Map<V, VertexData<V>> vertexData = new HashMap<>();

  @Override
  public void setEdgePredicate(Predicate<E> edgePredicate) {
    this.edgePredicate = edgePredicate;
  }

  @Override
  public void setEdgeComparator(Comparator<E> edgeComparator) {
    this.edgeComparator = edgeComparator;
  }

  @Override
  public void setVertexPredicate(Predicate<V> vertexPredicate) {
    this.vertexPredicate = vertexPredicate;
  }

  @Override
  public void setVertexComparator(Comparator<V> vertexComparator) {
    this.vertexComparator = vertexComparator;
  }

  @Override
  public Map<V, Rectangle> getBaseBounds() {
    if (this.baseBounds.isEmpty()) {
      for (V root : roots) {
        Point p = layoutModel.apply(root);
        // get the union of all kids
        Rectangle r = vertexShapeFunction.apply(root);
        r = Rectangle.of(r.x - r.width / 2 + p.x, r.y - r.height / 2 + p.y, r.width, r.height);
        baseBounds.put(root, union(r, Graphs.successorListOf(tree, root)));
      }
    }
    return baseBounds;
  }

  private Rectangle union(Rectangle r, Collection<V> in) {
    for (V v : in) {
      Point p = layoutModel.apply(v);
      Rectangle vr = vertexShapeFunction.apply(v);
      r = Rectangle.of(vr.x - vr.width / 2 + p.x, vr.y - vr.height / 2 + p.y, vr.width, vr.height);
      r = r.union(vr);
      r = union(r, successors(v));
      baseBounds.put(v, union(r, Graphs.successorListOf(tree, v)));
    }
    return r;
  }

  private VertexData<V> vertexData(V v) {
    if (!vertexData.containsKey(v)) {
      vertexData.put(v, new VertexData<>());
    }
    return vertexData.get(v);
  }

  private void firstWalk(V v, V leftSibling) {
    log.trace("firstWalk({}, {})", v, leftSibling);
    if (this.successors(v).isEmpty()) {

      if (leftSibling != null) {
        vertexData(v).x = vertexData(leftSibling).x + getDistance(v, leftSibling);
      }

    } else {
      V defaultAncestor = firstChild(v).get();
      V previousChild = null;
      for (V w : successors(v)) {
        firstWalk(w, previousChild);
        defaultAncestor = apportion(w, defaultAncestor, previousChild, v);
        previousChild = w;
      }
      shift(v);

      int midpoint = (vertexData(firstChild(v).get()).x + vertexData(lastChild(v).get()).x) / 2;

      log.trace("midpoint for {} is {}", v, midpoint);

      if (leftSibling != null) {
        vertexData(v).x = vertexData(leftSibling).x + getDistance(v, leftSibling);
        vertexData(v).mod = vertexData(v).x - midpoint;
      } else {
        vertexData(v).x = midpoint;
      }
    }
  }

  private void secondWalk(V v, int m, int depth, int yOffset) {

    log.trace("secondWalk({}, {}, {}, {})", v, m, depth, yOffset);
    int levelHeight = this.heights.get(depth);
    int x = vertexData(v).x + m;
    int y = yOffset + levelHeight / 2;

    if (v != null) {
      layoutModel.set(v, Point.of(x, y));
    }

    updateBounds(v, x, y);

    if (!successors(v).isEmpty()) {
      yOffset += levelHeight + verticalVertexSpacing;
      for (V w : successors(v)) {
        secondWalk(w, m + vertexData(v).mod, depth + 1, yOffset);
      }
    }
  }

  private Rectangle shape(Object in) {
    if (in == null) {
      // special case for forest where 'in' is null so size is 0
      return IDENTITY_SHAPE;
    }
    return vertexShapeFunction.apply((V) in);
  }

  private void updateBounds(V vertex, int centerX, int centerY) {
    log.trace("updateBounds({}, {}, {})", vertex, centerX, centerY);
    Rectangle shape = shape(vertex);
    int width = (int) shape.width;
    int height = (int) shape.height;
    int left = centerX - width / 2;
    int right = centerX + width / 2;
    int top = centerY - height / 2;
    int bottom = centerY + height / 2;

    Rectangle bounds = Rectangle.of(left, top, right - left, bottom - top);
    this.bounds = this.bounds.union(bounds);
    log.trace(
        "updated bounds to {} ({}, {}, {}, {})",
        bounds,
        bounds.x,
        bounds.maxX,
        bounds.y,
        bounds.maxY);
  }

  private Point normalize(Point p) {
    return Point.of(p.x - bounds.x, p.y - bounds.y);
  }

  private void computeMaxHeights(V vertex, int depth) {
    log.trace("computeMaxHeights({}, {})", vertex, depth);
    int previous;
    if (heights.size() > depth) {
      previous = heights.get(depth);
    } else {
      heights.add(0);
      previous = 0;
    }
    int height = (int) shape(vertex).height;
    heights.set(depth, Math.max(height, previous));

    successors(vertex).forEach(v -> computeMaxHeights(v, depth + 1));
  }

  private void moveSubtree(V leftVertex, V rightVertex, V parentVertex, int shift) {
    log.trace("moveSubtree({}, {}, {}, {})", leftVertex, rightVertex, parentVertex, shift);
    int subtreeCount =
        this.childPosition(rightVertex, parentVertex)
            - this.childPosition(leftVertex, parentVertex);
    if (subtreeCount > 0) {
      VertexData<V> rightData = vertexData(rightVertex);
      VertexData<V> leftData = vertexData(leftVertex);
      rightData.change -= shift / subtreeCount;
      rightData.shift += shift;

      leftData.change += shift / subtreeCount;
      rightData.x = vertexData(rightVertex).x + shift;
      rightData.mod += shift;
    }
  }

  private int childPosition(V vertex, V parentNode) {
    if (parentNode == null) {
      // if the parentNode is null, then the vertex is one of the 'roots'
      log.trace("childPosition({}, {}) = {}", vertex, parentNode, roots.indexOf(vertex) + 1);
      return roots.indexOf(vertex) + 1;
    }
    if (vertexData(vertex).childCount != 0) {
      log.trace("childPosition({}, {}) = {}", vertex, parentNode, vertexData(vertex).childCount);
      return vertexData(vertex).childCount;
    }
    int i = 1;
    for (V v : successors(parentNode)) {
      vertexData(v).childCount = i++;
    }
    log.trace("childPosition({}, {}) = {}", vertex, parentNode, vertexData(vertex).childCount);
    return vertexData(vertex).childCount;
  }

  private V ancestor(V vil, V parentOfV, V defaultAncestor) {
    log.trace("ancestor({}, {}, {})", vil, parentOfV, defaultAncestor);
    V ancestor = Optional.ofNullable(vertexData(vil).ancestor).orElse(vil);

    for (V vp : predecessors(ancestor)) {
      if (vp == parentOfV) {
        log.trace("ancestor({}, {}, {}) = {}", vil, parentOfV, ancestor, ancestor);
        return ancestor;
      }
    }
    log.trace("ancestor({}, {}, {}) = {}", vil, parentOfV, defaultAncestor, defaultAncestor);
    return defaultAncestor;
  }

  private V apportion(V v, V defaultAncestor, V leftSibling, V parentOfV) {
    log.trace("apportion({}, {}, {}, {})", v, defaultAncestor, leftSibling, parentOfV);
    if (leftSibling == null) {
      log.trace("...apportion returning passed Default Ancestor:{}", defaultAncestor);
      return defaultAncestor;
    }

    // i == inner, o == outer, r == right, l == left
    V vor = v;
    V vir = v;
    V vil = leftSibling;
    V vol = successors(parentOfV).get(0);

    int innerRight = vertexData(vir).mod;
    int outerRight = vertexData(vor).mod;
    int innerLeft = vertexData(vil).mod;
    int outerLeft = vertexData(vol).mod;

    V nextRightOfVil = rightChild(vil);
    V nextLeftOfVir = leftChild(vir);

    while (nextRightOfVil != null && nextLeftOfVir != null) {
      vil = nextRightOfVil;
      vir = nextLeftOfVir;
      vol = leftChild(vol);
      vor = rightChild(vor);
      vertexData(vor).ancestor = v;
      int shift =
          (vertexData(vil).x + innerLeft)
              - (vertexData(vir).x + innerRight)
              + getDistance(vil, vir);

      if (shift > 0) {
        moveSubtree(ancestor(vil, parentOfV, defaultAncestor), v, parentOfV, shift);
        innerRight = innerRight + shift;
        outerRight = outerRight + shift;
      }
      innerLeft += vertexData(vil).mod;
      innerRight += vertexData(vir).mod;
      outerLeft += vertexData(vol).mod;
      outerRight += vertexData(vor).mod;

      nextRightOfVil = rightChild(vil);
      nextLeftOfVir = leftChild(vir);
    }

    if (nextRightOfVil != null && rightChild(vor) == null) {
      vertexData(vor).thread = nextRightOfVil;
      vertexData(vor).mod = vertexData(vor).mod + innerLeft - outerRight;
    }

    if (nextLeftOfVir != null && leftChild(vol) == null) {
      vertexData(vol).thread = nextLeftOfVir;
      vertexData(vol).mod += innerRight - outerLeft;
      defaultAncestor = v;
    }
    log.trace("...apportion returning new defaultAncestor:{}", defaultAncestor);
    return defaultAncestor;
  }

  private void shift(V v) {
    log.trace("shift({})", v);
    int shift = 0;
    int change = 0;
    List<V> children = successors(v);
    Collections.reverse(children);

    for (V w : children) {
      vertexData(w).x += shift;
      vertexData(w).mod += shift;
      change += vertexData(w).change;
      shift += vertexData(w).shift + change;
    }
  }

  private List<V> successors(V v) {
    if (v == null) {
      // if v is null, then its successors are the forest roots
      log.trace("successors({}) = {}", v, roots);
      return roots;
    }
    log.trace("successors({}) = {}", v, Graphs.successorListOf(tree, v));
    return Graphs.successorListOf(tree, v);
  }

  private List<V> predecessors(V v) {
    if (roots.contains(v)) {
      // if v is one of the roots, then its predecessor is null (not empty collection)
      log.trace("predecessors({}) = {}", v, Collections.singletonList(null));
      return ((List<V>) Collections.singletonList(null));
    }
    log.trace("predecessors({}) = {}", v, Graphs.predecessorListOf(tree, v));
    return Graphs.predecessorListOf(tree, v);
  }

  private V leftChild(V v) {
    log.trace("leftChild({}) = {}", v, firstChild(v).orElse(vertexData(v).thread));
    return firstChild(v).orElse(vertexData(v).thread);
  }

  private V rightChild(V v) {
    log.trace("rightChild({}) = {}", v, lastChild(v).orElse(vertexData(v).thread));
    return lastChild(v).orElse(vertexData(v).thread);
  }

  private Optional<V> firstChild(V v) {
    log.trace("firstChild({}) = {}", v, successors(v).stream().findFirst());
    return successors(v).stream().findFirst();
  }

  private Optional<V> lastChild(V v) {
    log.trace("lastChild({}) = {}", v, successors(v).stream().reduce((first, second) -> second));
    return successors(v).stream().reduce((first, second) -> second);
  }

  private int getDistance(V v, V w) {
    log.trace("getDistance({}, {})", v, w);
    int sizeOfNodes = (int) shape(v).width + (int) shape(w).width;

    int distance = sizeOfNodes / 2 + horizontalVertexSpacing;
    log.trace("getDistance({}, {}) = {}", v, w, distance);
    return distance;
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    if (layoutModel instanceof Caching) {
      ((Caching) layoutModel).clear();
    }
    this.rootPredicate = this.builderRootPredicate;
    this.layoutModel = layoutModel;
    Graph<V, E> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    this.defaultRootPredicate =
        v ->
            layoutModel.getGraph().incomingEdgesOf(v).isEmpty()
                || TreeLayout.isIsolatedVertex(layoutModel.getGraph(), v);
    this.vertexData.clear();
    this.heights.clear();
    if (this.rootPredicate == null) {
      this.rootPredicate = this.defaultRootPredicate;
    } else {
      this.rootPredicate = this.rootPredicate.or(this.defaultRootPredicate);
    }
    if (vertexShapeFunction != null) {
      Dimension averageVertexSize = computeAverageVertexDimension(graph, vertexShapeFunction);
      this.horizontalVertexSpacing = averageVertexSize.width * 2;
      this.verticalVertexSpacing = averageVertexSize.height * 2;
    }

    TreeView.Builder<V, E, ?, ?> builder =
        TreeView.<V, E>builder()
            .rootPredicate(rootPredicate)
            .edgePredicate(edgePredicate)
            .edgeComparator(edgeComparator)
            .vertexComparator(vertexComparator)
            .vertexPredicate(vertexPredicate);

    this.roots =
        graph
            .vertexSet()
            .stream()
            .filter(rootPredicate)
            .sorted(Comparator.comparingInt(v -> TreeLayout.vertexIsolationScore(graph, v)))
            .collect(Collectors.toList());

    this.roots = ComponentGrouping.groupByComponents(graph, roots);

    if (roots.size() == 0) {
      Graph<V, ?> tree = TreeLayoutAlgorithm.getSpanningTree(graph);
      layoutModel.setGraph(tree);
      visit(layoutModel);
      layoutModel.setGraph(graph);
      return;
    }

    TreeView<V, E> treeView =
        builder
            .rootPredicate(rootPredicate)
            .edgeComparator(edgeComparator)
            .edgePredicate(edgePredicate)
            .vertexComparator(vertexComparator)
            .vertexPredicate(vertexPredicate)
            .build();
    this.tree = treeView.buildTree(graph);
    V root = null;
    if (roots.size() == 1) {
      // this is a tree, otherwise it is a forest (and root stays null)
      root = roots.get(0);
    }
    firstWalk(root, null);
    computeMaxHeights(root, 0);
    secondWalk(root, -vertexData(root).x, 0, 0);

    // normalize all the layoutModel points
    // and center in the layout area
    Rectangle extent = computeLayoutExtent(layoutModel);
    log.trace("extent is {}", extent);
    log.trace("bounds is {}", bounds);
    log.trace("layoutModel bounds: {} {}", layoutModel.getWidth(), layoutModel.getHeight());
    int xoffset = horizontalVertexSpacing;

    int yoffset = (int) -extent.min().y + verticalVertexSpacing;

    for (Map.Entry<V, Point> entry : layoutModel.getLocations().entrySet()) {
      Point p = entry.getValue();
      Point np = normalize(p);
      np = np.add(xoffset, yoffset);
      layoutModel.set(entry.getKey(), np);
    }
    layoutModel.setSize((int) extent.width, (int) extent.height);
    if (expandLayout) {
      expandToFill(layoutModel);
    }
  }
}
