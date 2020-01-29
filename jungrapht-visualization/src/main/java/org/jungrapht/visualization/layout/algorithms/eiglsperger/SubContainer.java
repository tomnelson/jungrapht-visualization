package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.jungrapht.visualization.layout.model.Point;

public class SubContainer<V, NT extends LV<V>> extends InsertionOrderSplayTreeWithSize<NT>
    implements LV<V> {

  public static <V, NT extends LV<V>> SubContainer<V, NT> createSubContainer() {
    return new SubContainer<>();
  }

  public static <V, NT extends LV<V>> SubContainer<V, NT> createSubContainer(Node<NT> root) {
    SubContainer<V, NT> tree = new SubContainer<>(root);
    tree.validate();
    return tree;
  }

  protected SubContainer() {}

  protected SubContainer(Node<NT> root) {
    this.root = root;
  }

  public static <V, NT extends LV<V>> Pair<SubContainer<V, NT>> split(
      SubContainer<V, NT> tree, NT key) {
    // assume we find key
    Node<NT> node = tree.find(key);
    if (node != null) {
      tree.splay(node);
      if (node.left != null) node.left.parent = null;
      if (node.right != null) node.right.parent = null;
      return Pair.of(
          SubContainer.createSubContainer(node.left), SubContainer.createSubContainer(node.right));
    } else {
      return Pair.of(tree, SubContainer.createSubContainer());
    }
  }

  public static <V, NT extends LV<V>> Pair<SubContainer<V, NT>> split(
      SubContainer<V, NT> tree, int position) {
    // assume we find key
    Node<NT> node = tree.find(position);
    if (node != null) {
      tree.splay(node);
      System.err.println(tree.printTree("after splay at " + node));
      if (node.left != null) node.left.parent = null;
      if (node.right != null) node.right.parent = null;
      SubContainer<V, NT> left = SubContainer.createSubContainer(node.left);
      left.validate();
      SubContainer<V, NT> right = SubContainer.createSubContainer(node.right);
      right.validate();
      return Pair.of(left, right);
    } else {
      tree.validate();
      SubContainer<V, NT> empty = SubContainer.createSubContainer();
      empty.validate();
      return Pair.of(tree, empty);
    }
  }

  @Override
  public LV copy() {
    return null;
  }

  @Override
  public void setRank(int rank) {}

  @Override
  public int getRank() {
    return 0;
  }

  @Override
  public void setIndex(int index) {}

  @Override
  public int getIndex() {
    return 0;
  }

  @Override
  public Point getPoint() {
    return null;
  }

  @Override
  public void setPoint(Point p) {}

  @Override
  public V getVertex() {
    return null;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    for (Iterator<V> iterator = new Iterator(root); iterator.hasNext(); ) {
      Node<V> node = iterator.next();
      if (buf.length() > 0) {
        buf.append(", ");
      }
      if (node != null) buf.append(node.key.toString());
    }
    return buf.toString();
  }
}
