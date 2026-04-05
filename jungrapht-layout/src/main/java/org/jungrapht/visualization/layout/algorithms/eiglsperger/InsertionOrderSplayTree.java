package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A splay tree that maintains insertion order for unique keys of type T. Duplicate keys are not
 * supported and may lead to undefined behavior. New items are appended to the right side of the
 * tree by finding the maximum node, splaying it to the root, and adding the new node as its right
 * child.
 *
 * <p>Fixed in this version: - Proper subtree size maintenance using updateSizes() in append(),
 * join(), split, etc. - Removed dead leftSize/rightSize code in splay() - Consistent size updates
 * after structural changes
 */
public class InsertionOrderSplayTree<T> {

  private static final Logger log = LoggerFactory.getLogger(InsertionOrderSplayTree.class);

  static <T> int nodeSize(Node<T> node) {
    return node == null ? 0 : node.size;
  }

  public static class Node<T> {
    T key;
    Node<T> parent;
    Node<T> left;
    Node<T> right;
    int size = 1;

    public Node(T key) {
      this.key = key;
    }

    public int size() {
      return this.size;
    }

    int count() {
      int leftCount = left != null ? left.count() : 0;
      int rightCount = right != null ? right.count() : 0;
      return 1 + leftCount + rightCount;
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

  protected Node<T> root;

  public static <T> InsertionOrderSplayTree<T> create() {
    return new InsertionOrderSplayTree<>();
  }

  public static <T> InsertionOrderSplayTree<T> create(Node<T> root) {
    InsertionOrderSplayTree<T> tree = new InsertionOrderSplayTree<>(root);
    tree.validate();
    return tree;
  }

  protected InsertionOrderSplayTree() {}

  protected InsertionOrderSplayTree(Node<T> root) {
    this.root = root;
  }

  // ====================== Size Maintenance ======================
  private void updateSizes(Node<T> node) {
    while (node != null) {
      node.size = nodeSize(node.left) + nodeSize(node.right) + 1;
      node = node.parent;
    }
  }

  // ====================== Rotations ======================
  void leftRotate(Node<T> x) {
    Node<T> y = x.right;
    if (y != null) {
      x.right = y.left;
      if (y.left != null) y.left.parent = x;
      y.parent = x.parent;
    }

    if (x.parent == null) root = y;
    else if (x == x.parent.left) x.parent.left = y;
    else x.parent.right = y;
    if (y != null) y.left = x;

    x.parent = y;

    x.size = nodeSize(x.left) + nodeSize(x.right) + 1;
    if (y != null) y.size = nodeSize(y.left) + nodeSize(y.right) + 1;
  }

  void rightRotate(Node<T> x) {
    Node<T> y = x.left;
    if (y != null) {
      x.left = y.right;
      if (y.right != null) y.right.parent = x;
      y.parent = x.parent;
    }
    if (null == x.parent) root = y;
    else if (x == x.parent.left) x.parent.left = y;
    else x.parent.right = y;
    if (y != null) y.right = x;

    x.parent = y;

    x.size = nodeSize(x.left) + nodeSize(x.right) + 1;
    if (y != null) y.size = nodeSize(y.left) + nodeSize(y.right) + 1;
  }

  // ====================== Splay ======================
  public void splay(T element) {
    Node<T> node = find(element);
    if (node != null) {
      splay(node);
    }
  }

  public void splay(Node<T> x) {
    if (x == null) {
      return;
    }

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

    validate();
  }

  // ====================== Helpers ======================
  static <T> Node<T> p(Node<T> node) {
    return node != null ? node.parent : node;
  }

  static <T> int size(Node<T> node) {
    return node != null ? node.size() : 0;
  }

  static <T> Node<T> l(Node<T> node) {
    return node != null ? node.left : node;
  }

  static <T> Node<T> r(Node<T> node) {
    return node != null ? node.right : node;
  }

  public int pos(Node<T> node) {
    if (node == root) {
      return size(l(node));
    } else if (r(p(node)) == node) {
      return pos(p(node)) + size(l(node)) + 1;
    } else if (l(p(node)) == node) {
      return pos(p(node)) - size(r(node)) - 1;
    } else {
      return -1;
    }
  }

  void replace(Node<T> u, Node<T> v) {
    if (null == u.parent) root = v;
    else if (u == u.parent.left) u.parent.left = v;
    else u.parent.right = v;
    if (v != null) v.parent = u.parent;
  }

  Node<T> subtree_minimum(Node<T> u) {
    while (u.left != null) u = u.left;
    return u;
  }

  Node<T> subtree_maximum(Node<T> u) {
    while (u.right != null) u = u.right;
    return u;
  }

  public Node<T> max() {
    if (root != null) {
      return subtree_maximum(root);
    } else {
      return null;
    }
  }

  public Node<T> min() {
    if (root != null) {
      return subtree_minimum(root);
    } else {
      return null;
    }
  }

  // ====================== Core Operations ======================
  public void append(T key) {
    Node<T> z = new Node<>(key);
    z.size = 1;
    if (root == null) {
      root = z;
      return;
    }
    Node<T> max = max();
    splay(max);

    max.right = z;
    z.parent = max;

    updateSizes(max); // reliable size propagation
  }

  public static <T> InsertionOrderSplayTree<T> join(Pair<InsertionOrderSplayTree<T>> trees) {
    trees.first.join(trees.second);
    return trees.first;
  }

  public void join(InsertionOrderSplayTree<T> joiner) {
    if (joiner == null || joiner.root == null) return;
    if (root == null) {
      root = joiner.root;
      return;
    }

    Node<T> largest = max();
    splay(largest);

    root.right = joiner.root;
    if (joiner.root != null) {
      joiner.root.parent = root;
    }

    updateSizes(root); // reliable size propagation
  }

  // ====================== Find ======================
  public Node<T> find(int k) {
    return find(root, k);
  }

  Node<T> find(Node<T> node, int k) {
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

  public Node<T> find(T key) {
    return find(root, key);
  }

  private Node<T> find(Node<T> from, T key) {
    if (from == null) return null;
    if (from.key.equals(key)) return from;
    Node<T> found = find(from.left, key);
    if (found != null) return found;
    return find(from.right, key);
  }

  public Node<T> find(Node<T> node) {
    return find(root, node);
  }

  private Node<T> find(Node<T> from, Node<T> node) {
    if (from == null) return null;
    if (from == node) return from;
    Node<T> found = find(from.left, node);
    if (found != null) return found;
    return find(from.right, node);
  }

  // ====================== Split ======================
  public static <T> Pair<InsertionOrderSplayTree<T>> split(InsertionOrderSplayTree<T> tree, T key) {
    InsertionOrderSplayTree<T> right = tree.split(key);
    return Pair.of(tree, right);
  }

  public InsertionOrderSplayTree<T> split(T key) {
    Node<T> node = find(key);
    if (node == null) {
      return InsertionOrderSplayTree.create();
    }
    splay(node);

    InsertionOrderSplayTree<T> rightTree = InsertionOrderSplayTree.create(node.right);
    if (node.right != null) node.right.parent = null;

    node.right = null;
    updateSizes(node);

    root = node.left;
    if (root != null) root.parent = null;

    validate();
    rightTree.validate();
    return rightTree;
  }

  public static <T> Pair<InsertionOrderSplayTree<T>> split(
      InsertionOrderSplayTree<T> tree, int position) {
    InsertionOrderSplayTree<T> right = tree.split(position);
    return Pair.of(tree, right);
  }

  public InsertionOrderSplayTree<T> split(int position) {
    Node<T> found = find(position);
    if (found == null) {
      return InsertionOrderSplayTree.create();
    }
    splay(found);

    InsertionOrderSplayTree<T> rightTree = InsertionOrderSplayTree.create(found.right);
    if (found.right != null) found.right.parent = null;

    found.right = null;
    updateSizes(found);

    validate();
    rightTree.validate();
    return rightTree;
  }

  // ====================== Erase ======================
  public void erase(T key) {
    Node<T> z = find(key);
    if (null == z) return;
    splay(z);

    if (null == z.left) {
      replace(z, z.right);
    } else if (null == z.right) {
      replace(z, z.left);
    } else {
      Node<T> y = subtree_minimum(z.right);
      if (y.parent != z) {
        replace(y, y.right);
        y.right = z.right;
        y.right.parent = y;
      }
      replace(z, y);
      y.left = z.left;
      y.left.parent = y;
    }
    if (root != null) updateSizes(root);
    validate();
  }

  // ====================== Other methods (unchanged) ======================
  public int size() {
    return root != null ? root.size : 0;
  }

  public int height() {
    return height(root);
  }

  public static <T> int height(Node<T> node) {
    return node != null ? 1 + Math.max(height(node.left), height(node.right)) : 0;
  }

  public boolean contains(Node<T> element) {
    return contains(root, element);
  }

  private boolean contains(Node<T> from, Node<T> segment) {
    if (from == null) return false;
    if (from == segment) return true;
    return contains(from.left, segment) || contains(from.right, segment);
  }

  public boolean contains(T value) {
    return contains(root, value);
  }

  private boolean contains(Node<T> from, T value) {
    if (from == null) return false;
    if (from.key.equals(value)) return true; // changed to equals()
    return contains(from.left, value) || contains(from.right, value);
  }

  public String printTree() {
    return printTree(root, 0);
  }

  public String printTree(Node<T> node, int d) {
    StringBuilder builder = new StringBuilder();
    if (node == null) return "";
    builder.append(printTree(node.right, d + 1));
    for (int i = 0; i < d; i++) builder.append("  ");
    builder.append(node.key + "(" + node.size + ")\n");
    builder.append(printTree(node.left, d + 1));
    return builder.toString();
  }

  public String printTree(String note) {
    return note + "\n" + printTree(root, 0);
  }

  public void validate() {
    if (log.isTraceEnabled()) {
      if (root != null) {
        if (root.parent != null) {
          throw new RuntimeException("root parent is not null");
        }
        root.validate();
        validateChild(root.left);
        validateChild(root.right);
      }
    }
  }

  private void validateChild(Node<T> node) {
    if (node != null) {
      node.validate();
      if (node.parent == null) {
        throw new RuntimeException("child " + node.key + " has null parent");
      }
      if (node.size != node.count()) {
        throw new RuntimeException("size of " + node.key + " does not match count");
      }
      validateChild(node.left);
      validateChild(node.right);
    }
  }

  public static class Iterator<V> implements java.util.Iterator<Node<V>> {
    private Node<V> next;

    public Iterator(Node<V> root) {
      this.next = root;
      if (next == null) return;
      while (next.left != null) next = next.left;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public Node<V> next() {
      if (!hasNext()) throw new NoSuchElementException();
      Node<V> r = next;
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

  protected List<Node<T>> nodes() {
    List<Node<T>> list = new ArrayList<>();
    Iterator<T> iterator = new Iterator<>(root);
    while (iterator.hasNext()) {
      list.add(iterator.next());
    }
    return list;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    for (Iterator<T> iterator = new Iterator(root); iterator.hasNext(); ) {
      Node<T> node = iterator.next();
      buf.append(node.toString()).append("\n");
    }
    return buf.toString();
  }
}
