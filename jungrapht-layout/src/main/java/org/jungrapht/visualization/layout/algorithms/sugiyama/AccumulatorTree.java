package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accumulator tree for counting edge crossing
 *
 * <p>Modified from Waddle/Malhorta to use a 0 based array instead of 1 based
 *
 * @see "An E log E Line Crossing Algorithm for Levelled Graphs. Vance Waddle and Ashok Malhotra IBM
 *     Thomas J. Watson Research Center"
 * @see "Simple and Efficient Bilayer Cross Counting. Wilhelm Barth, Petra Mutzel, Institut für
 *     Computergraphik und Algorithmen Technische Universität Wien, Michael Jünger, Institut für
 *     Informatik Universität zu Köln"
 */
public class AccumulatorTree<V, E> {

  private static final Logger log = LoggerFactory.getLogger(AccumulatorTree.class);

  List<LE<V, E>> edges;
  int firstIndex = 1;
  int treeSize;
  int[] tree;
  int base;
  int last;

  public AccumulatorTree(int size) {
    while (firstIndex < size) {
      firstIndex *= 2;
    }
    treeSize = 2 * firstIndex - 1; // number of tree nodes
    firstIndex--;
    this.base = size - 1;
    this.last = size - 1;
    tree = new int[treeSize];
  }

  public int countEdges(int n, int last) {
    if (n > last) {
      log.error("position exceeds array size");
    }
    if (n == last) {
      return 0;
    }
    int pos = n + base; // check for zero-base array
    int sum = 0;
    while (pos > 0) {
      if (isEven(pos)) { // even number, right-hand child
        sum += tree[pos - 1];
      }
      pos = parentIndex(pos); // for 1-based array: pos/2. for 0-based: (pos-1)/2
    }
    return tree[0] - sum;
  }

  public int crossCount(int[] southSequence) {
    int r = southSequence.length;
    int crossCount = 0;
    for (int k = 0; k < r; k++) {
      int index = southSequence[k] + firstIndex;
      tree[index]++;
      while (index > 0) {
        if (index % 2 != 0) {
          crossCount += tree[index + 1];
        }
        index = (index - 1) / 2;
        tree[index]++;
        if (log.isTraceEnabled()) {
          log.trace("incremented {} in  tree:{}", index, this.toString());
        }
      }
    }
    return crossCount;
  }

  /**
   * @param southSequence
   * @param weight
   * @return
   */
  public int crossWeight(int[] southSequence, Function<Integer, Integer> weight) {
    int r = southSequence.length;
    int crossWeight = 0;
    for (int k = 0; k < r; k++) {
      int index = southSequence[k] + firstIndex;
      if (log.isTraceEnabled()) {
        log.trace("got a weight value of {}", weight.apply(k));
      }
      tree[index] += weight.apply(k);
      int weightSum = 0;
      while (index > 0) {
        if (index % 2 != 0) {
          weightSum += tree[index + 1];
        }
        index = (index - 1) / 2;
        tree[index] += weight.apply(k);
      }
      crossWeight += (weight.apply(k) * weightSum);
    }
    return crossWeight;
  }

  public int crossWeight(LE<V, E>[] edgeArray, Function<Integer, Integer> weight) {
    int r = edgeArray.length;
    int crossWeight = 0;
    for (int k = 0; k < r; k++) {
      LE<V, E> edge = edgeArray[k];
      int index = edge.getTarget().getIndex() + firstIndex;
      if (log.isTraceEnabled()) {
        log.trace("got a weight value of {}", weight.apply(k));
      }
      tree[index] += weight.apply(k);
      int weightSum = 0;
      while (index > 0) {
        if (index % 2 != 0) {
          weightSum += tree[index + 1];
        }
        index = (index - 1) / 2;
        tree[index] += weight.apply(k);
      }
      crossWeight += (weight.apply(k) * weightSum);
    }
    return crossWeight;
  }

  public int getBase() {
    return base;
  }

  private boolean isEven(int n) {
    return n % 2 == 0;
  }

  private boolean isOdd(int n) {
    return !isEven(n);
  }

  int parentIndex(int childIndex) {
    return (childIndex - 1) / 2;
  }

  public void addEdge(int pos) {
    pos += base;
    while (pos > 0) {
      tree[pos]++;
      pos = parentIndex(pos);
    }
    tree[0]++;
  }

  public void subtractEdge(int pos) {
    pos += base;
    while (pos > 0) {
      tree[pos]--;
      pos = parentIndex(pos);
    }
    tree[0]--;
  }

  public String toString() {
    return "First:"
        + firstIndex
        + ",last:"
        + last
        + ",treeSize:"
        + treeSize
        + ","
        + Arrays.toString(tree);
  }
}
