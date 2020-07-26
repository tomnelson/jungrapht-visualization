package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A splay tree for items that are not Comparable. There is no 'insert' method, any new item is
 * appended to the right side of the SplayTree by first finding the 'max' (farthest right), splaying
 * to it, and adding the new Node as its right child
 *
 * @param <T> key type that is stored in the tree
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

    x.size = nodeSize(x.left) + nodeSize(x.right) + 1;

    x.parent = y;
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

    x.size = nodeSize(x.left) + nodeSize(x.right) + 1;

    x.parent = y;
  }

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
    validate();
  }

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
    } else if (r(p(node)) == node) { // node is a right child
      return pos(p(node)) + size(l(node)) + 1;
    } else if (l(p(node)) == node) { // node is a left child
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
    max.size += z.size;
    z.parent = max;
  }

  public static <T> InsertionOrderSplayTree<T> join(Pair<InsertionOrderSplayTree<T>> trees) {
    trees.first.join(trees.second);
    return trees.first;
  }

  public void join(InsertionOrderSplayTree<T> joiner) {
    Node<T> largest = max();
    splay(largest);
    if (root != null) {
      root.right = joiner.root;
      if (joiner.root != null) {
        root.size += joiner.root.size;
        joiner.root.parent = root;
      }
    } else {
      root = joiner.root;
    }
  }

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

  /**
   * find key, make it the root, left children go in first tree, right children go in second tree.
   * key is not in either tree
   *
   * @param tree
   * @param key
   * @param <T>
   * @return
   */
  public static <T> Pair<InsertionOrderSplayTree<T>> split(InsertionOrderSplayTree<T> tree, T key) {
    InsertionOrderSplayTree<T> right = tree.split(key);
    return Pair.of(tree, right);
  }

  public InsertionOrderSplayTree<T> split(T key) {
    // split off the right side of key
    Node<T> node = find(key);
    if (node != null) {
      splay(node); // so node will be root
      //      System.err.println(printTree());
      node.size -= size(node.right);
      // root should be the found node
      if (node.right != null) node.right.parent = null;

      if (node.left != null) {
        node.left.parent = null;
      }
      root = node.left;

      InsertionOrderSplayTree<T> splitter = InsertionOrderSplayTree.create(node.right);

      // found should not be in either tree
      splitter.validate();
      validate();
      return splitter;
    } else {
      return this;
    }
  }

  /**
   * first position elements go in left tree, the rest go in right tree. No elements are missin
   *
   * @param tree
   * @param position
   * @param <T>
   * @return
   */
  public static <T> Pair<InsertionOrderSplayTree<T>> split(
      InsertionOrderSplayTree<T> tree, int position) {

    InsertionOrderSplayTree<T> right = tree.split(position);

    return Pair.of(tree, right);
  }

  public InsertionOrderSplayTree<T> split(int position) {
    Node<T> found = find(position);
    if (found != null) {
      splay(found);
      // split off the right side of key
      if (found.right != null) {
        found.right.parent = null;
        found.size -= found.right.size;
      }
      InsertionOrderSplayTree<T> splitter = InsertionOrderSplayTree.create(found.right);
      found.right = null;
      splitter.validate();
      validate();
      // make sure that 'found' is still in this tree.
      if (find(found) == null) {
        throw new RuntimeException(
            "Node " + found + " at position " + position + " was not still in tree");
      }
      return splitter;
    }
    return InsertionOrderSplayTree.create(); // return empty 'right' tree and leave tree alone
  }

  public Node<T> find(Node<T> node) {
    return find(root, node);
  }

  private Node<T> find(Node<T> from, Node<T> node) {
    if (from == null) return null;
    if (from == node) return from;
    Node<T> found = find(from.left, node);
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

  public Node<T> find(T key) {
    return find(root, key);
  }

  private Node<T> find(Node<T> from, T node) {
    if (from == null) return null;
    if (from != null && from.key.equals(node)) return from;
    Node<T> found = find(from.left, node);
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

  public void erase(T key) {
    Node<T> z = find(key);
    if (null == z) return;

    splay(z);

    if (null == z.left) replace(z, z.right);
    else if (null == z.right) replace(z, z.left);
    else {
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
  }

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
    if (from.key == value) return true;
    return contains(from.left, value) || contains(from.right, value);
  }

  public String printTree() {
    return printTree(root, 0);
  }

  public String printTree(Node<T> node, int d) {
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
    if (log.isTraceEnabled()) {
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
    Set<Node<V>> elements = new LinkedHashSet<>();

    public Iterator(Node<V> root) {
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
      buf.append(node.toString());
      buf.append("\n");
    }
    return buf.toString();
  }
}
