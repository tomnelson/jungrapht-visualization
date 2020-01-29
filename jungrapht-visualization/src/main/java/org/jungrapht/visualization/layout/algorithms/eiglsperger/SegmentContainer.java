package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LVI;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A splay tree for items that are not Comparable. There is no 'insert' method, any new item is
 * appended to the right side of the SplayTree by first finding the 'max' (farthest right), splaying
 * to it, and adding the new Node as its right child
 *
 * @param <V>
 */
public class SegmentContainer<V> extends LVI<V> {

  private static final Logger log = LoggerFactory.getLogger(SegmentContainer.class);

  static <V> int nodeSize(Node<V> node) {
    return node == null ? 0 : node.size;
  }

  public static class Node<V> {
    Segment<V> key;
    Node<V> parent;
    Node<V> left;
    Node<V> right;
    int size;

    public Node(Segment<V> key) {
      this.key = key;
    }

    public int size() {
      return this.size;
    }

    int count() {
      int leftCount = left != null ? left.count() : 0;
      int rightCount = right != null ? right.count() : 0;
      int count = 1 + leftCount + rightCount;
      return count;
    }

    public void validate() {
      if (this == left) {
        throw new RuntimeException("this == left");
      }
      if (left != null && left == right) {
        throw new RuntimeException("children match");
      }
      if (this == this.parent) {
        throw new RuntimeException("node is its own parent");
      }
    }
  }

  Node<V> root;

  public static <V> SegmentContainer<V> create() {
    return new SegmentContainer<>();
  }

  public static <V> SegmentContainer<V> create(Node<V> root) {
    return new SegmentContainer<>(root);
  }

  protected SegmentContainer() {}

  protected SegmentContainer(Node<V> root) {
    this.root = root;
  }

  void leftRotate(Node<V> x) {
    Node<V> y = x.right;
    if (y != null) {
      x.right = y.left;
      if (y.left != null) y.left.parent = x;
      y.parent = x.parent;
    }

    if (x.parent == null) root = y;
    else if (x == x.parent.left) x.parent.left = y;
    else x.parent.right = y;
    if (y != null) y.left = x;

    x.size = nodeSize(x.left) + nodeSize(x.right) + 1;

    x.parent = y;
  }

  void rightRotate(Node<V> x) {
    Node<V> y = x.left;
    if (y != null) {
      x.left = y.right;
      if (y.right != null) y.right.parent = x;
      y.parent = x.parent;
    }
    if (null == x.parent) root = y;
    else if (x == x.parent.left) x.parent.left = y;
    else x.parent.right = y;
    if (y != null) y.right = x;

    x.size = nodeSize(x.left) + nodeSize(x.right) + 1;

    x.parent = y;
  }

  public void splay(Segment<V> element) {
    Node<V> node = find(element);
    if (node != null) {
      splay(node);
    }
  }

  public void splay(Node<V> x) {
    int rootSize = nodeSize(root);
    int leftSize = 0;
    int rightSize = 0;

    while (x.parent != null) {
      if (null == x.parent.parent) {
        if (x.parent.left == x) rightRotate(x.parent);
        else leftRotate(x.parent);
      } else if (x.parent.left == x && x.parent.parent.left == x.parent) {
        rightRotate(x.parent.parent);
        rightRotate(x.parent);
      } else if (x.parent.right == x && x.parent.parent.right == x.parent) {
        leftRotate(x.parent.parent);
        leftRotate(x.parent);
      } else if (x.parent.left == x && x.parent.parent.right == x.parent) {
        rightRotate(x.parent);
        leftRotate(x.parent);
      } else {
        leftRotate(x.parent);
        rightRotate(x.parent);
      }
    }

    leftSize += nodeSize(root.left); /* Now l_size and r_size are the sizes of */
    rightSize += nodeSize(root.right); /* the left and right trees we just built.*/
    root.size = leftSize + rightSize + 1;
  }

  static <V> Node<V> p(Node<V> node) {
    return node.parent;
  }

  static <V> int size(Node<V> node) {
    return node != null ? node.size() : 0;
  }

  static <V> Node<V> l(Node<V> node) {
    return node.left;
  }

  static <V> Node<V> r(Node<V> node) {
    return node.right;
  }

  public int pos(Node<V> node) {
    if (node == root) {
      return size(l(node));
    } else if (r(p(node)) == node) { // node is a right child
      return pos(p(node)) + size(l(node)) + 1;
    } else if (l(p(node)) == node) { // node is a left child
      return pos(p(node)) - size(r(node)) - 1;
    } else {
      return -1;
    }
  }

  void replace(Node<V> u, Node<V> v) {
    if (null == u.parent) root = v;
    else if (u == u.parent.left) u.parent.left = v;
    else u.parent.right = v;
    if (v != null) v.parent = u.parent;
  }

  Node<V> subtree_minimum(Node<V> u) {
    while (u.left != null) u = u.left;
    return u;
  }

  Node<V> subtree_maximum(Node<V> u) {
    while (u.right != null) u = u.right;
    return u;
  }

  public Node<V> max() {
    return subtree_maximum(root);
  }

  public Node<V> min() {
    return subtree_minimum(root);
  }

  public void append(Segment<V> key) {
    Node<V> z = new Node<>(key);
    z.size = 1;
    if (root == null) {
      root = z;
      return;
    }
    Node<V> max = max();
    splay(max);

    //    System.err.println(printTree("Appending " + key));

    max.right = z;
    max.size += z.size;
    z.parent = max;
    //    System.err.println(printTree("Appended " + key));
    //    size++;
  }

  public static <V> void join(Pair<SegmentContainer<V>> trees) {
    // find my largest item
    Node<V> largest = trees.first.max();
    trees.first.splay(largest);
    trees.first.root.right = trees.second.root;
  }

  public void join(SegmentContainer<V> joiner) {
    Node<V> largest = max();
    splay(largest);
    root.right = joiner.root;
    if (joiner.root != null) {
      joiner.root.parent = root;
      root.size += joiner.root.size;
    } else {
      //      System.err.println("joiner root is null");
    }
  }

  public SegmentContainer<V> split(Segment<V> key) {
    // split off the right side of key
    Node<V> node = find(key);
    splay(node);
    System.err.println(printTree());
    // root should be the found node

    node.right.parent = null;
    SegmentContainer<V> splitter = SegmentContainer.create(node.right);
    node.right = null;
    return splitter;
  }

  public SegmentContainer<V> split(int position) {
    Node<V> found = find(position);
    if (found != null) {
      splay(found);
      // split off the right side of key
      if (found.right != null) {
        found.right.parent = null;
        found.size -= found.right.size;
      }
      SegmentContainer<V> splitter = SegmentContainer.create(found.right);
      found.right = null;

      validate();
      return splitter;
    }
    return SegmentContainer.create(); // return empty 'right' tree and leave tree alone
  }

  public static <T, V> Pair<SegmentContainer<V>> split(SegmentContainer<V> tree, int position) {
    // assume we find key
    Node<V> node = tree.find(position);
    tree.splay(node);
    node.left.parent = null;
    node.right.parent = null;
    return Pair.of(SegmentContainer.create(node.left), SegmentContainer.create(node.right));
  }

  public Node<V> find(int k) {
    return find(root, k);
  }

  Node<V> find(Node<V> node, int k) {
    if (node == null) return null;
    int pos = pos(node);

    if (pos == k) {
      return node;
    }
    if (pos < k) {
      return find(node.right, k);
    } else {
      return find(node.left, k);
    }
  }

  public static <T, V> Pair<SegmentContainer<V>> split(SegmentContainer<V> tree, Segment<V> key) {
    // assume we find key
    Node<V> node = tree.find(key);
    tree.splay(node);
    node.left.parent = null;
    node.right.parent = null;
    return Pair.of(SegmentContainer.create(node.left), SegmentContainer.create(node.right));
  }

  public Node<V> find(Node<V> node) {
    return find(root, node);
  }

  private Node<V> find(Node<V> from, Node<V> node) {
    if (from == null) return null;
    if (from == node) return from;
    Node<V> found = find(from.left, node);
    if (found != null) {
      return found;
    } else {
      found = find(from.right, node);
      if (found != null) {
        return found;
      } else {
        return null;
      }
    }
  }

  public Node<V> find(Segment<V> key) {
    return find(root, key);
  }

  private Node<V> find(Node<V> from, Segment<V> node) {
    if (from == null) return null;
    if (from != null && from.key.equals(node)) return from;
    Node<V> found = find(from.left, node);
    if (found != null) {
      return found;
    } else {
      found = find(from.right, node);
      if (found != null) {
        return found;
      } else {
        return null;
      }
    }
  }

  public void erase(Segment<V> key) {
    Node<V> z = find(key);
    if (null == z) return;

    splay(z);

    if (null == z.left) replace(z, z.right);
    else if (null == z.right) replace(z, z.left);
    else {
      Node<V> y = subtree_minimum(z.right);
      if (y.parent != z) {
        replace(y, y.right);
        y.right = z.right;
        y.right.parent = y;
      }
      replace(z, y);
      y.left = z.left;
      y.left.parent = y;
    }
  }

  public int size() {
    return root != null ? root.size : 0;
  }

  public int height() {
    return height(root);
  }

  public static <V> int height(Node<V> node) {
    return node != null ? 1 + Math.max(height(node.left), height(node.right)) : 0;
  }

  public boolean contains(Node<V> element) {
    return contains(root, element);
  }

  private boolean contains(Node<V> from, Node<V> segment) {
    if (from == null) return false;
    if (from == segment) return true;
    return contains(from.left, segment) || contains(from.right, segment);
  }

  public boolean contains(Segment<V> value) {
    return contains(root, value);
  }

  private boolean contains(Node<V> from, Segment<V> value) {
    if (from == null) return false;
    if (from.key == value) return true;
    return contains(from.left, value) || contains(from.right, value);
  }

  public String printTree() {
    return printTree(root, 0);
  }

  public String printTree(Node<V> node, int d) {
    StringBuilder builder = new StringBuilder();
    int i;
    if (node == null) return "";
    builder.append(printTree(node.right, d + 1));
    for (i = 0; i < d; i++) builder.append("  ");
    builder.append(node.key + "(" + node.size + ")\n");
    builder.append(printTree(node.left, d + 1));
    return builder.toString();
  }

  public String printTree(String note) {
    return note + "\n" + printTree(root, 0);
  }

  public void validate() {
    // root parent is null
    if (root != null) {
      if (root.parent != null) {
        throw new RuntimeException("root parent is not null");
      }
      root.validate();
      validateChild(root.left);
      validateChild(root.right);
    }
  }

  private void validateChild(Node<V> node) {
    if (node != null) {
      node.validate();
      if (node.parent == null) {
        throw new RuntimeException("child has null parent");
      }
      if (node.size != node.count()) {
        throw new RuntimeException("size does not match count");
      }
      validateChild(node.left);
      validateChild(node.right);
    }
  }

  //  void updateSize() {
  //    size = 0;
  //    for (TreeIterator<V> iterator = new TreeIterator<>(root); iterator.hasNext(); iterator.next()) {
  //      size++;
  //    }
  //  }

  public static class TreeIterator<V> implements Iterator<Node<V>> {
    @Override
    public void remove() {}

    private Node<V> next;
    Set<Node<V>> elements = new LinkedHashSet<>();

    public TreeIterator(Node<V> root) {
      this.next = root;
      if (next == null) return;

      while (next.left != null) {
        if (elements.contains(next.left)) {
          throw new RuntimeException("duplicate elements");
        }
        elements.add(next.left);
        next = next.left;
      }
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public Node<V> next() {
      if (!hasNext()) throw new NoSuchElementException();
      Node<V> r = next;

      // If you can walk right, walk right, then fully left.
      // otherwise, walk up until you come from left.
      if (next.right != null) {
        next = next.right;
        while (next.left != null) next = next.left;
        return r;
      }

      while (true) {
        if (next.parent == null) {
          next = null;
          return r;
        }
        if (next.parent.left == next) {
          next = next.parent;
          return r;
        }
        next = next.parent;
      }
    }
  }
}
