package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Accumulator Tree, 0 based instead of 1 based indexing
 *
 * @see "An E log E Line Crossing Algorithm for Levelled Graphs. Vance Waddle and Ashok Malhotra IBM
 *     Thomas J. Watson Research Center"
 */
public class AccumulatorTree {

  private static final Logger log = LoggerFactory.getLogger(AccumulatorTree.class);

  int[] accumulatorTree;
  int base;
  int last;

  public AccumulatorTree(int size) {
    int accumulatorTreeSize = size * 2 - 1;
    this.base = size - 1;
    this.last = size - 1;
    this.accumulatorTree = new int[accumulatorTreeSize];
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

  /**
   * @param n the index of the target node for an edge
   * @return
   */
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
        sum += accumulatorTree[pos - 1];
      }
      pos = parentIndex(pos); // for 1-based array: pos/2. for 0-based: (pos+1)/2 - 1
    }
    return accumulatorTree[0] - sum;
  }

  public void addEdge(int pos) {
    pos += base;
    while (pos > 0) {
      accumulatorTree[pos]++;
      pos = parentIndex(pos);
    }
    accumulatorTree[0]++;
  }

  public void subtractEdge(int pos) {
    pos += base;
    while (pos > 0) {
      accumulatorTree[pos]--;
      pos = parentIndex(pos);
    }
    accumulatorTree[0]--;
  }

  @Override
  public String toString() {
    return "AccumulatorTree{"
        + "accumulatorTree="
        + Arrays.toString(accumulatorTree)
        + ", base="
        + base
        + ", last="
        + last
        + '}';
  }
}
