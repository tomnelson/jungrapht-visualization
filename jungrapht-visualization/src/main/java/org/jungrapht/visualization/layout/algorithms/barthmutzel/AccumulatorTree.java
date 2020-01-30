package org.jungrapht.visualization.layout.algorithms.barthmutzel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Comparators;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accumulator tree for counting edge crossing
 *
 * @see "W. Barth, M. Jünger, and P. Mutzel. Simple and efficient bilayer cross counting"
 * @see "V. Waddle and A. Malhotra, An E log E line crossing algorithm for levelled graphs"
 */
public class AccumulatorTree<V, E> {

  private static final Logger log = LoggerFactory.getLogger(AccumulatorTree.class);

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
      pos = parentIndex(pos); // for 1-based array: pos/2. for 0-based: (pos+1)/2 - 1
    }
    return tree[0] - sum;
  }

  public int crossingCount(List<LE<V, E>> edges) {
    Comparator<LE<V, E>> biLevelEdgeComparator = Comparators.biLevelEdgeComparator();
    edges.sort(biLevelEdgeComparator);
    List<Integer> targetIndices = new ArrayList<>();
    int weight = 1;
    for (LE<V, E> edge : edges) {
      targetIndices.add(edge.getTarget().getIndex());
    }
    int[] southSequence = new int[targetIndices.size()];
    targetIndices.forEach(i -> southSequence[i] = i);
    return crossCount(southSequence, southSequence.length);
    //    return weight * InsertionSortCounter.insertionSortCounter(targetIndices);
  }

  public int crossCount(int[] southSequence, int r) {
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
      }
    }
    Arrays.stream(tree).forEach(i -> tree[i] = 0);
    return crossCount;
  }

  public int crossWeight(int[] southSequence, int r, Function<Integer, Integer> weight) {
    int crossWeight = 0;
    for (int k = 0; k < r; k++) {
      int index = southSequence[k] + firstIndex;
      tree[index] += weight.apply(k);
      int weightSum = 0;
      while (index > 0) {
        if (index % 2 != 0) {
          weightSum += tree[index + 1];
        }
        index = (index - 1) / 2;
        tree[index] += weightSum;
      }
      crossWeight += weight.apply(k) * weightSum;
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
}
