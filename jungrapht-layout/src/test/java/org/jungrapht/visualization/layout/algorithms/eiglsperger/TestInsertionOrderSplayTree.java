package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestInsertionOrderSplayTree {

  private static final Logger log = LoggerFactory.getLogger(TestInsertionOrderSplayTree.class);

  InsertionOrderSplayTree<String> tree;

  String[] values = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

  @Test
  public void testSmallContainer() {
    tree = InsertionOrderSplayTree.create();
    tree.append("A");
    System.err.println(tree.printTree("Appended A"));

    tree.append("B");
    System.err.println(tree.printTree("Appended B"));

    tree.append("C");
    System.err.println(tree.printTree("Appended C"));

    tree.append("E");
    System.err.println(tree.printTree("Appended E"));

    tree.append("F");
    System.err.println(tree.printTree("Appended F"));

    tree.splay("D");
    System.err.println(tree.printTree("Splayed D"));

    tree.splay("C");
    System.err.println(tree.printTree("Splayed C"));

    tree.splay("B");
    System.err.println(tree.printTree("Splayed B"));

    tree.splay("A");
    System.err.println(tree.printTree("Splayed A"));

    tree.splay("C");
    System.err.println(tree.printTree("Splayed C"));

    tree.append("D");
    System.err.println(tree.printTree("Appended D"));

    tree.append("H");
    System.err.println(tree.printTree("Appended H"));

    tree.append("G");
    System.err.println(tree.printTree("Appended G"));

    InsertionOrderSplayTree.Node<String> found = tree.find(3);
    System.err.println(tree.printTree("found " + found.key + " at " + 3));

    found = tree.find(5);
    System.err.println(tree.printTree("found " + found.key + " at " + 5));

    for (int i = 0; i < tree.size(); i++) {
      found = tree.find(i);
      System.err.println("found " + found.key + " at " + i);
    }

    tree.splay("C");
    System.err.println(tree.printTree("Splayed C"));

    for (int i = 0; i < tree.size(); i++) {
      found = tree.find(i);
      System.err.println("found " + found.key + " at " + i);
    }
    tree.validate();
  }

  @Test
  public void testSplitToPair() {
    tree = InsertionOrderSplayTree.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());

    Pair<InsertionOrderSplayTree<String>> pair = InsertionOrderSplayTree.split(tree, "M");
    System.err.println(pair.first.printTree());

    pair.first.validate();

    System.err.println(pair.second.printTree());

    pair.second.validate();
  }

  @Test
  public void testSplit() {
    tree = InsertionOrderSplayTree.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree("starting tree"));

    InsertionOrderSplayTree<String> newTree = tree.split("M");
    System.err.println(newTree.printTree("split off tree"));

    newTree.validate();

    System.err.println(tree.printTree("tree after split"));
    tree.validate();

    tree.join(newTree);
    System.err.println(tree.printTree("Joined Tree"));
    tree.validate();
  }

  @Test
  public void testSplits() {
    tree = InsertionOrderSplayTree.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());

    for (int splitPoint : shuffledInts(0, tree.size())) {
      InsertionOrderSplayTree<String> splitter = tree.split(splitPoint);
      System.err.println("split at " + splitPoint);
      System.err.println("newTree size: " + splitter.size());
      System.err.println(splitter.printTree("split off tree"));

      splitter.validate();
      System.err.println("tree size: " + tree.size());
      System.err.println(tree.printTree("tree after split"));
      System.err.println(
          "__________________________________________________________________________");

      tree.validate();

      tree.join(splitter);
      System.err.println(tree.printTree("Joined Tree"));
      tree.validate();
    }
  }

  @Test
  public void testStaticSplits() {
    tree = InsertionOrderSplayTree.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());
    tree.validate();

    for (int splitPoint : shuffledInts(0, tree.size())) {
      tree.validate();
      Pair<InsertionOrderSplayTree<String>> pair = InsertionOrderSplayTree.split(tree, splitPoint);
      System.err.println("split at " + splitPoint);

      InsertionOrderSplayTree<String> left = pair.first;
      InsertionOrderSplayTree<String> right = pair.second;
      System.err.println("left size: " + left.size());
      System.err.println("right size: " + right.size());
      System.err.println(left.printTree("split off tree"));
      System.err.println(right.printTree("split off tree"));

      left.validate();
      right.validate();
      System.err.println(
          "__________________________________________________________________________");

      InsertionOrderSplayTree<String> joined = InsertionOrderSplayTree.join(pair);
      System.err.println(joined.printTree("Joined Tree"));
      joined.validate();
      tree = joined;
    }
  }

  @Test
  public void testStaticSplitAtZero() {
    tree = InsertionOrderSplayTree.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());

    Pair<InsertionOrderSplayTree<String>> pair = InsertionOrderSplayTree.split(tree, 0);
    System.err.println("split at " + 0);

    InsertionOrderSplayTree<String> left = pair.first;
    InsertionOrderSplayTree<String> right = pair.second;
    System.err.println("left size: " + left.size());
    System.err.println("right size: " + right.size());
    System.err.println(left.printTree("split off tree"));
    System.err.println(right.printTree("split off tree"));

    left.validate();
    right.validate();
    System.err.println(
        "__________________________________________________________________________");

    InsertionOrderSplayTree<String> joined = InsertionOrderSplayTree.join(pair);
    System.err.println(joined.printTree("Joined Tree"));
    joined.validate();
  }

  @Test
  public void testStaticSplitAtTop() {
    tree = InsertionOrderSplayTree.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());

    Pair<InsertionOrderSplayTree<String>> pair = InsertionOrderSplayTree.split(tree, tree.size());
    System.err.println("split at " + tree.size());

    InsertionOrderSplayTree<String> left = pair.first;
    InsertionOrderSplayTree<String> right = pair.second;
    System.err.println("left size: " + left.size());
    System.err.println("right size: " + right.size());
    System.err.println(left.printTree("split off tree"));
    System.err.println(right.printTree("split off tree"));

    left.validate();
    right.validate();
    System.err.println(
        "__________________________________________________________________________");

    InsertionOrderSplayTree<String> joined = InsertionOrderSplayTree.join(pair);
    System.err.println(joined.printTree("Joined Tree"));
    joined.validate();
  }

  @Test
  public void testStaticSplitAtOverTop() {
    tree = InsertionOrderSplayTree.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());

    Pair<InsertionOrderSplayTree<String>> pair =
        InsertionOrderSplayTree.split(tree, tree.size() + 10);
    System.err.println("split at " + (tree.size() + 10));

    InsertionOrderSplayTree<String> left = pair.first;
    InsertionOrderSplayTree<String> right = pair.second;
    System.err.println("left size: " + left.size());
    System.err.println("right size: " + right.size());
    System.err.println(left.printTree("split off tree"));
    System.err.println(right.printTree("split off tree"));

    left.validate();
    right.validate();
    System.err.println(
        "__________________________________________________________________________");

    InsertionOrderSplayTree<String> joined = InsertionOrderSplayTree.join(pair);
    System.err.println(joined.printTree("Joined Tree"));
    joined.validate();
  }

  @Test
  public void testStaticSplitAtNegative() {
    tree = InsertionOrderSplayTree.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());

    Pair<InsertionOrderSplayTree<String>> pair = InsertionOrderSplayTree.split(tree, -10);
    System.err.println("split at " + -10);

    InsertionOrderSplayTree<String> left = pair.first;
    InsertionOrderSplayTree<String> right = pair.second;
    System.err.println("left size: " + left.size());
    System.err.println("right size: " + right.size());
    System.err.println(left.printTree("split off tree"));
    System.err.println(right.printTree("split off tree"));

    left.validate();
    right.validate();
    System.err.println(
        "__________________________________________________________________________");

    InsertionOrderSplayTree<String> joined = InsertionOrderSplayTree.join(pair);
    System.err.println(joined.printTree("Joined Tree"));
    joined.validate();
  }

  @Test
  public void testSplitsAtKeys() {
    List<String> chars = new ArrayList<>();
    for (char c = 'A'; c <= 'Z'; c++) {
      String s = Character.toString(c);
      chars.add(s);
    }
    for (String key : new String[] {"V", "A", "G", "Z"}) {
      tree = InsertionOrderSplayTree.create();
      chars.forEach(tree::append);
      System.err.println(tree.printTree("starting tree"));

      Pair<InsertionOrderSplayTree<String>> pair = InsertionOrderSplayTree.split(tree, key);

      System.err.println(pair.first.printTree("first split off tree"));
      System.err.println(pair.second.printTree("second split off tree"));
      pair.first.validate();
      pair.second.validate();
    }
  }

  @Test
  public void joinWithEmptyTree() {
    tree = InsertionOrderSplayTree.create();

    InsertionOrderSplayTree<String> joiner =
        InsertionOrderSplayTree.create(new InsertionOrderSplayTree.Node("Z"));

    tree.join(joiner);

    System.err.println(joiner.printTree());

    InsertionOrderSplayTree<String> next =
        InsertionOrderSplayTree.create(new InsertionOrderSplayTree.Node<>("A"));
    InsertionOrderSplayTree.join(Pair.of(tree, next));
    System.err.println(tree.printTree());
    tree.splay("A");
    System.err.println(tree.printTree());
  }

  @Test
  public void testSizeAfterRotations() {
    tree = InsertionOrderSplayTree.create();
    tree.append("A");
    tree.append("B");
    tree.append("C");
    tree.splay("B");
    assertEquals(3, tree.size(), "Tree size should be 3 after appends and splay");
    assertEquals(3, tree.root.count(), "Root count should match size");
    InsertionOrderSplayTree.Node<String> node = tree.find("B");
    assertEquals(3, node.size, "Node B size should be 3 after splay");
    tree.validate();
  }

  @Test
  public void testAppendSizeConsistency() {
    tree = InsertionOrderSplayTree.create();
    for (char c = 'A'; c <= 'E'; c++) {
      tree.append("" + c);
      assertEquals(c - 'A' + 1, tree.size(), "Tree size should be " + (c - 'A' + 1));
      assertEquals(tree.size(), tree.root.count(), "Root count should match size");
    }
    tree.validate();
  }

  @Test
  public void testSplitNonExistentKey() {
    tree = InsertionOrderSplayTree.create();
    for (char c = 'A'; c <= 'E'; c++) {
      tree.append("" + c);
    }
    int originalSize = tree.size();
    Pair<InsertionOrderSplayTree<String>> pair = InsertionOrderSplayTree.split(tree, "X");
    assertEquals(originalSize, tree.size(), "Original tree size should remain unchanged");
    assertEquals(0, pair.second.size(), "Right tree should be empty");
    assertNull(pair.second.root, "Right tree root should be null");
    tree.validate();
    pair.second.validate();
  }

  @Test
  public void testEmptyTreeOperations() {
    tree = InsertionOrderSplayTree.create();
    assertEquals(0, tree.size(), "Empty tree should have size 0");
    assertNull(tree.root, "Empty tree root should be null");

    tree.splay("A");
    assertEquals(0, tree.size(), "Splay on empty tree should not change size");
    tree.validate();

    Pair<InsertionOrderSplayTree<String>> pair = InsertionOrderSplayTree.split(tree, "A");
    assertEquals(0, pair.first.size(), "Left tree should be empty after split");
    assertEquals(0, pair.second.size(), "Right tree should be empty after split");
    pair.first.validate();
    pair.second.validate();

    InsertionOrderSplayTree<String> joiner = InsertionOrderSplayTree.create();
    tree.join(joiner);
    assertEquals(0, tree.size(), "Joining empty trees should result in empty tree");
    tree.validate();
  }

  // Implementing Fisher–Yates shuffle
  static void shuffleArray(int[] ar) {
    Random rnd = ThreadLocalRandom.current();
    for (int i = ar.length - 1; i > 0; i--) {
      int index = rnd.nextInt(i + 1);
      int a = ar[index];
      ar[index] = ar[i];
      ar[i] = a;
    }
  }

  static int[] shuffledInts(int from, int to) {
    int[] array = IntStream.rangeClosed(from, to).toArray();
    shuffleArray(array);
    return array;
  }
}
