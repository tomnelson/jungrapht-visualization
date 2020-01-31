package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.jungrapht.visualization.layout.model.Point;

/**
 * @param <V> the vertex type
 * @param <NT> the type for the Container node keys
 */
class Container<V, NT extends LV<V>> extends InsertionOrderSplayTreeWithSize<NT> implements LV<V> {

  double measure = -1;
  int pos = -1;

  public static <V, NT extends LV<V>> Container<V, NT> createSubContainer() {
    return new Container<>();
  }

  public static <V, NT extends LV<V>> Container<V, NT> createSubContainer(Node<NT> root) {
    Container<V, NT> tree = new Container<>(root);
    tree.validate();
    return tree;
  }

  protected Container() {}

  protected Container(Node<NT> root) {
    this.root = root;
  }

  public static <V, NT extends LV<V>> Pair<Container<V, NT>> split(Container<V, NT> tree, NT key) {
    Container<V, NT> right = tree.split(key);
    return Pair.of(tree, right);
  }

  public Container<V, NT> split(NT key) {
    // split off the right side of key
    Node<NT> node = find(key);
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

      Container<V, NT> splitter = Container.createSubContainer(node.right);

      // found should not be in either tree
      splitter.validate();
      validate();
      return splitter;
    } else {
      return this;
    }
  }

  public static <V, NT extends LV<V>> Pair<Container<V, NT>> split(
      Container<V, NT> tree, int position) {

    Container<V, NT> right = tree.split(position);

    return Pair.of(tree, right);
  }

  public Container<V, NT> split(int position) {
    Node<NT> found = find(position);
    if (found != null) {
      splay(found);
      // split off the right side of key
      if (found.right != null) {
        found.right.parent = null;
        found.size -= found.right.size;
      }
      Container<V, NT> splitter = Container.createSubContainer(found.right);
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
    return Container.createSubContainer();
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
  public int getPos() {
    return pos;
  }

  @Override
  public void setPos(int pos) {
    this.pos = pos;
  }

  @Override
  public double getMeasure() {
    return measure;
  }

  @Override
  public void setMeasure(double measure) {
    this.measure = measure;
  }

  @Override
  public V getVertex() {
    return null;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder("Container");
    buf.append(" size:" + this.size());
    buf.append(" index:" + this.getIndex());
    buf.append(" pos:" + this.getPos());
    buf.append(" measure:" + this.getMeasure());
    buf.append(" {");
    boolean first = true;
    for (Iterator<V> iterator = new Iterator(root); iterator.hasNext(); ) {
      Node<V> node = iterator.next();
      if (!first) {
        buf.append(", ");
      }
      first = false;
      if (node != null) buf.append(node.key.toString());
    }
    buf.append('}');
    return buf.toString();
  }
}
