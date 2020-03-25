package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.List;
import java.util.stream.Collectors;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.jungrapht.visualization.layout.model.Point;

/**
 * Extension of the {@link InsertionOrderSplayTree} to hold Nodes of {@link Segment<V>}
 *
 * @param <V> the vertex type
 */
class Container<V> extends InsertionOrderSplayTree<Segment<V>> implements LV<V> {

  double measure = -1;
  int pos = -1;
  int index;

  public static <V> Container<V> createSubContainer() {
    return new Container<>();
  }

  public static <V> Container<V> createSubContainer(Node<Segment<V>> root) {
    Container<V> tree = new Container<>(root);
    tree.validate();
    return tree;
  }

  protected Container() {}

  protected Container(Node<Segment<V>> root) {
    this.root = root;
  }

  public static <V> Pair<Container<V>> split(Container<V> tree, Segment<V> key) {
    Container<V> right = tree.split(key);
    return Pair.of(tree, right);
  }

  public Container<V> split(Segment<V> key) {
    // split off the right side of key
    Node<Segment<V>> node = find(key);
    if (node != null) {
      splay(node); // so node will be root
      node.size -= size(node.right);
      // root should be the found node
      if (node.right != null) node.right.parent = null;

      if (node.left != null) {
        node.left.parent = null;
      }
      root = node.left;

      Container<V> splitter = Container.createSubContainer(node.right);

      // found should not be in either tree
      splitter.validate();
      validate();
      return splitter;
    } else {
      return this;
    }
  }

  public static <V> Pair<Container<V>> split(Container<V> tree, int position) {

    Container<V> right = tree.split(position);

    return Pair.of(tree, right);
  }

  public Container<V> split(int position) {
    Node<Segment<V>> found = find(position);
    if (found != null) {
      splay(found);
      // split off the right side of key
      if (found.right != null) {
        found.right.parent = null;
        found.size -= found.right.size;
      }
      Container<V> splitter = Container.createSubContainer(found.right);
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
  public <T extends LV<V>> T copy() {
    throw new RuntimeException("Copy of Container not implemented");
  }

  @Override
  public void setRank(int rank) {}

  @Override
  public int getRank() {
    return 0;
  }

  @Override
  public void setIndex(int index) {
    this.index = index;
  }

  @Override
  public int getIndex() {
    return index;
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
    if (measure == -1) {
      measure = pos;
    }
  }

  @Override
  public double getMeasure() {
    return measure;
  }

  @Override
  public void setMeasure(double measure) {
    this.measure = measure;
    this.pos = (int) measure;
  }

  @Override
  public V getVertex() {
    return null;
  }

  public List<Segment<V>> segments() {
    return super.nodes().stream().map(n -> n.key).collect(Collectors.toList());
  }

  public String toString() {
    StringBuilder buf = new StringBuilder("Container");
    buf.append(" size:").append(this.size());
    buf.append(" index:").append(this.getIndex());
    buf.append(" pos:").append(this.getPos());
    buf.append(" measure:").append(this.getMeasure());
    buf.append(" {");
    boolean first = true;
    for (Iterator<Segment<V>> iterator = new Iterator<>(root); iterator.hasNext(); ) {
      Node<Segment<V>> node = iterator.next();
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
