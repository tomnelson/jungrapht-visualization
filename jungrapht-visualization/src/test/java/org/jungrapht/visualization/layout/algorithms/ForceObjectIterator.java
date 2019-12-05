package org.jungrapht.visualization.layout.algorithms;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jungrapht.visualization.layout.quadtree.BarnesHutQuadTree;
import org.jungrapht.visualization.layout.quadtree.ForceObject;
import org.jungrapht.visualization.layout.quadtree.Node;

/**
 * An iterator over the (logn) force objects to apply to the passed target This approach is slower
 * than visiting the BarnesHutOctTree, but the results should be identical for each approach. This
 * exists for testing and verification only.
 *
 * <p>package level access for the associated test algorithm implementations only
 *
 * @author Tom Nelson
 */
class ForceObjectIterator<T> implements Iterator<ForceObject<T>> {

  private BarnesHutQuadTree<T> tree;
  private Iterator<ForceObject<T>> iterator;

  public ForceObjectIterator(BarnesHutQuadTree<T> tree, ForceObject<T> target) {
    this.tree = tree;
    Set<ForceObject<T>> forceObjects = getForceObjectsFor(new LinkedHashSet<>(), target);
    this.iterator = forceObjects.iterator();
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public ForceObject<T> next() {
    return this.iterator.next();
  }

  private Set<ForceObject<T>> getForceObjectsFor(
      Set<ForceObject<T>> forceObjects, ForceObject<T> target) {
    Node<T> root = tree.getRoot();
    if (root != null && root.getForceObject() != target) {
      return getForceObjectsFor(forceObjects, target, root);
    } else {
      return Collections.emptySet();
    }
  }

  private Set<ForceObject<T>> getForceObjectsFor(
      Set<ForceObject<T>> forceObjects, ForceObject<T> target, Node<T> from) {

    double THETA = 0.5;

    if (from.getForceObject() == null || target.equals(from.getForceObject())) {
      forceObjects.add(target);
    }

    if (from.isLeaf()) {
      forceObjects.add(from.getForceObject());
    } else {
      // not a leaf
      //  this node is an internal node
      //  calculate s/d
      double s = from.getArea().width;
      //      distance between the incoming node's position and
      //      the center of mass for this node
      double d = from.getForceObject().p.distance(target.p);
      if (s / d < THETA) {
        // this node is sufficiently far away
        // just use this node's forces
        forceObjects.add(from.getForceObject());
      } else {
        // down the tree we go
        getForceObjectsFor(forceObjects, target, from.getNW());
        getForceObjectsFor(forceObjects, target, from.getNE());
        getForceObjectsFor(forceObjects, target, from.getSW());
        getForceObjectsFor(forceObjects, target, from.getSE());
      }
    }
    return forceObjects;
  }
}
