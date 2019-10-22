package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.Arrays;

public class AccumulatorTreeOneBased {

  int[] accumulatorTree;
  int base;
  int last;

  public AccumulatorTreeOneBased(int size) {
    int accumulatorTreeSize = size * 2;
    this.base = size - 1;
    this.last = size;
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
    return childIndex / 2;
  }

  public int countEdges(int n) {
    if (n == last) {
      return 0;
    }
    int pos = n + base + 1;
    int sum = 0;
    while (pos > 1) {
      if (isOdd(pos)) { // even number, right-hand child
        sum += accumulatorTree[pos - 1];
      }
      pos = pos / 2; // for 1-based array: pos/2. for 0-based: (pos+1)/2 - 1
    }
    return accumulatorTree[1] - sum;
  }

  public void addEdge(int pos) {
    pos += base;
    while (pos > 1) {
      accumulatorTree[pos]++;
      pos = parentIndex(pos);
    }
    accumulatorTree[1]++;
  }

  public void subtractEdge(int pos) {
    pos += base;
    while (pos > 1) {
      accumulatorTree[pos]--;
      pos = parentIndex(pos);
    }
    accumulatorTree[1]--;
  }

  @Override
  public String toString() {
    return "AccumulatorTreeOneBased{"
        + "accumulatorTree="
        + Arrays.toString(accumulatorTree)
        + ", base="
        + base
        + ", last="
        + last
        + '}';
  }
}
