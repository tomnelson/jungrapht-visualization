package org.jungrapht.visualization.spatial.rtree;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * R-Tree or R*-Tree implementation, depending on the type of Splitters passed in the
 * SplitterContext
 *
 * <p>Based on <a href="http://dbs.mathematik.uni-marburg.de/publications/myPapers/1990/BKSS90.pdf">
 * The R*-tree: An Efficient and Robust Access Method for Points and Rectangles+</a> Norbert
 * Beckmann, Hans-Peter begel, Ralf Schneider, Bernhard Seeger Praktuche Informatlk, Umversltaet
 * Bremen, D-2800 Bremen 33, West Germany
 *
 * @author Tom Nelson
 */
public class RTree<T> {

  private static final Logger log = LoggerFactory.getLogger(RTree.class);

  /** the root of the R-Tree */
  private final Optional<Node<T>> root;

  /** @return the root of the R-Tree */
  public Optional<Node<T>> getRoot() {
    return root;
  }

  /** create an empty R-Tree */
  private RTree() {
    root = Optional.empty();
  }

  /**
   * create an R-Tree with the passed Node as the root
   *
   * @param node the node that will be the root
   */
  private RTree(Node<T> node) {
    if (!node.getParent().isEmpty())
      throw new RuntimeException("Error creating R-Tree with root that has parent");
    root = Optional.of(node);
  }

  /**
   * create and return an empty R-Tree
   *
   * @param <T>
   * @return an empty R-Tree
   */
  public static <T> RTree<T> create() {
    return new RTree();
  }

  public static <T> RTree<T> addAll(
      RTree<T> rtree,
      SplitterContext<T> splitterContext,
      Collection<Map.Entry<T, Rectangle2D>> entries) {
    for (Map.Entry<T, Rectangle2D> entry : entries) {
      rtree = add(rtree, splitterContext, entry);
    }
    return rtree;
  }

  public static <T> RTree<T> add(
      RTree<T> rtree, SplitterContext<T> splitterContext, Map.Entry<T, Rectangle2D> entry) {
    return add(rtree, splitterContext, entry.getKey(), entry.getValue());
  }

  /**
   * add one element to the RTree. Adding to the tree can result in a new
   *
   * @param splitterContext the R*Tree or R-Tree rules
   * @param element to add to the tree
   * @param bounds for the element to add
   * @return a new RTree containing the added element
   */
  public static <T> RTree<T> add(
      RTree<T> rtree, SplitterContext<T> splitterContext, T element, Rectangle2D bounds) {
    // see if the root is not present (i.e. the RTree is empty
    if (rtree.root.isEmpty()) {
      // The first element addded to an empty RTree
      // Return a new RTree with the new LeafVertex as its root
      return new RTree(LeafNode.create(element, bounds));
    }
    // otherwise...
    Node<T> node = rtree.root.get();
    if (node instanceof LeafNode) {
      // the root is a LeafVertex
      LeafNode<T> leafVertex = (LeafNode) node;

      Node<T> got = leafVertex.add(splitterContext, element, bounds);
      if (!got.getParent().isEmpty())
        throw new RuntimeException("return from LeafVertex add has a parent");
      return new RTree(got);

    } else {

      InnerNode<T> innerVertex = (InnerNode) node;
      Node<T> got = innerVertex.add(splitterContext, element, bounds);
      if (got == null) {
        log.error("add did not work");
        throw new RuntimeException("add did not work");
      } else if (!got.getParent().isEmpty()) {
        throw new RuntimeException("return from InnerVertex add has a parent");
      }
      return new RTree(got);
    }
  }

  public static <T> RTree<T> bulkAdd(
      RTree<T> rtree,
      SplitterContext<T> splitterContext,
      Collection<Map.Entry<T, Rectangle2D>> items) {
    // sort the items
    List<Map.Entry<T, Rectangle2D>> sortedList = new ArrayList<>(items);
    sortedList.sort(new HorizontalCenterNodeComparator<>());
    for (Map.Entry<T, Rectangle2D> entry : sortedList) {
      rtree = RTree.add(rtree, splitterContext, entry.getKey(), entry.getValue());
    }
    return rtree;
  }

  public static <T> RTree removeForReinsert(
      RTree<T> rtree, Collection<Map.Entry<T, Rectangle2D>> removed) {
    if (rtree.root.isEmpty()) return rtree;
    Node<T> root = rtree.root.get();
    log.trace(
        "average leaf count {}", rtree.averageLeafCount(root, new double[] {0}, new int[] {0}));

    // find all nodes that have leaf children
    List<LeafNode> leafVertices = rtree.collectLeafVertices(root, new ArrayList<>());
    // are there dupes?
    // for each leaf node, sort the children max to min, according to how far they are from the center
    List<Map.Entry<T, Rectangle2D>> goners = new ArrayList<>();
    int averageSize = (int) rtree.averageLeafCount(root, new double[] {0}, new int[] {0});

    for (TreeNode node : leafVertices) {
      if (node instanceof LeafNode) {
        LeafNode leafVertex = (LeafNode) node;
        NodeMap<T> nodeMap = leafVertex.map;
        // will be sorted at the end
        List<Map.Entry<T, Rectangle2D>> entryList = new ArrayList<>(nodeMap.entrySet());
        entryList.sort(new DistanceComparator(leafVertex.centerOfGravity()));

        // now take 30% from the beginning of the sortedList, remove them all from the tree, then re-insert them all

        int size = entryList.size();
        if (size >= averageSize) {
          size *= 0.3;
        }
        for (int i = 0; i < size; i++) {
          Map.Entry<T, Rectangle2D> entry = entryList.get(i);
          goners.add(entry);
        }
      }
    }
    for (Map.Entry<T, Rectangle2D> goner : goners) {
      rtree = RTree.remove(rtree, goner.getKey());
      log.trace("removed one, tree size now {}", rtree.count());
    }
    removed.addAll(goners);
    return rtree;
  }

  public static <T> RTree reinsert(RTree<T> rtree, SplitterContext<T> splitterContext) {

    if (rtree.root.isEmpty()) return rtree;
    Node<T> root = rtree.root.get();
    log.trace(
        "average leaf count {}", rtree.averageLeafCount(root, new double[] {0}, new int[] {0}));

    // find all nodes that have leaf children
    List<LeafNode> leafVertices = rtree.collectLeafVertices(root, new ArrayList<>());
    // for each leaf node, sort the children max to min, according to how far they are from the center
    List<Map.Entry<T, Rectangle2D>> goners = new ArrayList<>();
    int averageSize = (int) rtree.averageLeafCount(root, new double[] {0}, new int[] {0});

    for (TreeNode node : leafVertices) {
      if (node instanceof LeafNode) {
        Rectangle2D boundsOfLeafVertex = node.getBounds();
        Point2D centerOfLeafVertex =
            new Point2D.Double(boundsOfLeafVertex.getCenterX(), boundsOfLeafVertex.getCenterY());
        LeafNode leafVertex = (LeafNode) node;
        NodeMap<T> nodeMap = leafVertex.map;
        // will be sorted at the end
        List<Map.Entry<T, Rectangle2D>> entryList = new ArrayList<>(nodeMap.entrySet());
        entryList.sort(new DistanceComparator(centerOfLeafVertex));

        // now take 30% from the beginning of the sortedList, remove them all from the tree, then re-insert them all

        int size = entryList.size();
        if (size >= averageSize) {
          size *= 0.3;
        }
        for (int i = 0; i < size; i++) {
          Map.Entry<T, Rectangle2D> entry = entryList.get(i);
          goners.add(entry);
        }
      }
    }
    for (Map.Entry<T, Rectangle2D> goner : goners) {
      rtree = RTree.remove(rtree, goner.getKey());
      log.trace("removed one, tree size now {}", rtree.count());
    }
    log.trace("removed {} goners", goners.size());
    log.trace("removed goners, tree size is {}", rtree.count());
    for (Map.Entry<T, Rectangle2D> goner : goners) {
      rtree = RTree.add(rtree, splitterContext, goner.getKey(), goner.getValue());
    }
    log.trace("after adding back {} goners, rtree size is {}", goners.size(), rtree.count());
    return rtree;
  }

  static class DistanceComparator<T> implements Comparator<Map.Entry<T, Rectangle2D>> {
    Point2D center;

    public DistanceComparator(Point2D center) {
      this.center = center;
    }

    @Override
    public int compare(Map.Entry<T, Rectangle2D> o1, Map.Entry<T, Rectangle2D> o2) {
      Point2D centerOfO1 =
          new Point2D.Double(o1.getValue().getCenterX(), o1.getValue().getCenterY());
      Point2D centerOfO2 =
          new Point2D.Double(o2.getValue().getCenterX(), o2.getValue().getCenterY());
      return Double.compare(center.distanceSq(centerOfO2), center.distanceSq(centerOfO1));
    }
  }

  private List<LeafNode> collectLeafVertices(TreeNode parent, List<LeafNode> leafVertices) {
    if (parent instanceof Node) {
      Node<T> node = (Node<T>) parent;
      if (node instanceof LeafNode) {
        leafVertices.add((LeafNode) node);
      } else {
        for (TreeNode kid : parent.getChildren()) {
          collectLeafVertices(kid, leafVertices);
        }
      }
    }
    return leafVertices;
  }

  /**
   * remove an element from the tree
   *
   * @param element
   * @return
   */
  public static <T> RTree<T> remove(RTree<T> rtree, T element) {
    log.trace("want to remove {} from tree size {}", element, rtree.count());
    if (rtree.root.isEmpty()) {
      // this tree is empty
      return new RTree();
    }
    Node<T> rootVertex = rtree.root.get();
    Node<T> newRoot = rootVertex.remove(element);

    // if the newRoot is empty, return a new empty RTree, otherwise, return this
    if (newRoot.count() == 0) {
      return RTree.create();
    } else {
      return rtree;
    }
  }

  /**
   * return an object at point p
   *
   * @param p point to search
   * @return an element that contains p or null
   */
  public T getPickedObject(Point2D p) {
    Node<T> root = this.root.get();
    if (root instanceof LeafNode) {
      LeafNode<T> leafVertex = (LeafNode) root;
      return leafVertex.getPickedObject(p);
    } else if (root instanceof InnerNode) {
      InnerNode<T> innerVertex = (InnerNode) root;
      return innerVertex.getPickedObject(p);
    } else {
      return null;
    }
  }

  /** @return a collection of rectangular bounds of the R-Tree nodes */
  public Set<Shape> getGrid() {
    Set<Shape> areas = new HashSet<>();
    if (root.isPresent()) {
      Node<T> node = root.get();
      node.collectGrids(areas);
    }
    return areas;
  }

  /**
   * get the R-Tree leaf nodes that would contain the passed point
   *
   * @param p the point to search
   * @return a Collection of R-Tree nodes that would contain p
   */
  public Collection<TreeNode> getContainingLeafs(Point2D p) {
    if (root.isPresent()) {
      Node<T> theRoot = root.get();

      if (theRoot instanceof LeafNode) {
        return Collections.singleton(theRoot);
      } else if (theRoot instanceof InnerNode) {
        return ((InnerNode) theRoot).getContainingLeafs(new HashSet<>(), p);
      }
    }
    return Collections.emptySet();
  }

  /**
   * count all the elements in the R-Tree
   *
   * @return the count
   */
  public int count() {
    int count = 0;
    if (root.isPresent()) {
      Node<T> node = root.get();
      count += node.count();
    }
    return count;
  }

  private String asString() {
    if (root.isPresent()) {
      return root.get().asString("");
    } else {
      return "Empty RTree";
    }
  }

  private static final String marginIncrement = "   ";

  private static String asString(Rectangle2D r) {
    return "["
        + (int) r.getX()
        + ","
        + (int) r.getY()
        + ","
        + (int) r.getWidth()
        + ","
        + (int) r.getHeight()
        + "]";
  }

  private double averageLeafCount(TreeNode parent, double[] average, int[] leafCount) {

    if (parent instanceof LeafNode) {
      int childSum = ((LeafNode) parent).map.size();
      average[0] = (average[0] * leafCount[0] + childSum) / (leafCount[0] + 1);
      leafCount[0]++;
    } else {
      for (TreeNode node : parent.getChildren()) {
        average[0] = averageLeafCount(node, average, leafCount);
      }
    }
    return average[0];
  }

  private static <T> String asString(Collection<RTree<T>> trees) {
    StringBuilder sb = new StringBuilder();
    for (RTree<T> tree : trees) {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append(tree.asString());
    }
    return sb.toString();
  }

  private static <T> String asString(Map<T, Rectangle2D> map) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<T, Rectangle2D> entry : map.entrySet()) {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append(asString(entry));
    }
    return sb.toString();
  }

  private static <T> String asString(Map.Entry<T, Rectangle2D> entry) {
    return entry.getKey() + "->" + asString(entry.getValue());
  }

  @Override
  public String toString() {
    return this.asString();
  }
}
