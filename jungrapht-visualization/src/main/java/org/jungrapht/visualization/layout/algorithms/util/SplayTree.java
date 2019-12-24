package org.jungrapht.visualization.layout.algorithms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplayTree<T extends Comparable<T>> {

  private static final Logger log = LoggerFactory.getLogger(SplayTree.class);

  static class Node<T extends Comparable<T>> implements Comparable<Node<T>> {
    T key;
    Node<T> parent;
    Node<T> left;
    Node<T> right;

    public Node(T key) {
      this.key = key;
    }

    @Override
    public int compareTo(Node<T> other) {
      return key.compareTo(other.key);
    }
  }

  Node<T> root;

  int size;

  public static <T extends Comparable<T>> SplayTree<T> create() {
    return new SplayTree<>();
  }

  public static <T extends Comparable<T>> SplayTree<T> create(Node<T> root) {
    return new SplayTree<>(root);
  }

  private SplayTree() {}

  private SplayTree(Node<T> root) {
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
    x.parent = y;
  }

  public void splay(T element) {
    Node<T> node = find(element);
    if (node != null) {
      splay(node);
    }
  }

  public void splay(Node<T> x) {
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
    return subtree_maximum(root);
  }

  public Node<T> min() {
    return subtree_minimum(root);
  }

  public void insert(T key) {
    Node<T> z = root;
    Node<T> p = null;

    while (z != null) {
      p = z;
      if (comp(z.key, key)) z = z.right;
      else z = z.left;
    }

    z = new Node<>(key);
    z.parent = p;

    if (null == p) root = z;
    else if (comp(p.key, z.key)) p.right = z;
    else p.left = z;

    splay(z);
    size++;
  }

  public static <T extends Comparable<T>> void join(Pair<SplayTree<T>> trees) {
    // find my largest item
    // assert that the max of left is smaller than the min of right
    if (!comp(trees.first.max(), trees.second.min())) {
      throw new IllegalArgumentException();
    }
    Node<T> largest = trees.first.max();
    trees.first.splay(largest);
    trees.first.root.right = trees.second.root;
  }

  public void join(SplayTree<T> joiner) {
    log.info("max:{}, joiner min: {}", max().key, joiner.min().key);
    log.info("comp is {}", comp(max(), joiner.min()));
    if (!comp(max(), joiner.min())) {
      throw new IllegalArgumentException();
    }
    Node<T> largest = max();
    splay(largest);
    root.right = joiner.root;
  }

  public SplayTree<T> split(T key) {
    // split off the right side of key
    Node<T> node = find(key);
    SplayTree<T> splitter = SplayTree.create(node.right);
    node.right = null;
    return splitter;
  }

  public static <T extends Comparable<T>> Pair<SplayTree<T>> split(SplayTree<T> tree, T key) {
    // assume we find key
    Node<T> node = tree.find(key);
    return Pair.of(SplayTree.create(node.left), SplayTree.create(node.right));
  }

  public Node<T> find(T key) {
    Node<T> z = root;
    while (z != null) {
      if (comp(z.key, key)) z = z.right;
      else if (comp(key, z.key)) z = z.left;
      else return z;
    }
    return null;
  }

  public Node<T> findSplay(T key) {
    Node<T> z = root;
    while (z != null) {
      if (comp(z.key, key)) z = z.right;
      else if (comp(key, z.key)) z = z.left;
      else {
        splay(z);
        return z;
      }
    }
    return null;
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

    size--;
  }

  public int size() {
    return size;
  }

  public int height() {
    return height(root);
  }

  public static <T extends Comparable<T>> int height(Node<T> node) {
    return node != null ? 1 + Math.max(height(node.left), height(node.right)) : 0;
  }

  static <T extends Comparable> boolean comp(T one, T two) {
    return one.compareTo(two) < 0;
  }
}
