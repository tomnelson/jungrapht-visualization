package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestInsertionOrderSplayTreeWithSize {

  private static final Logger log =
      LoggerFactory.getLogger(TestInsertionOrderSplayTreeWithSize.class);

  InsertionOrderSplayTreeWithSize<String> tree;

  String[] values = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

  @Test
  public void testSmallContainer() {
    // make a Container and add all the nodes
    tree = InsertionOrderSplayTreeWithSize.create();
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

    InsertionOrderSplayTreeWithSize.Node<String> found = tree.find(3);
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
    tree = InsertionOrderSplayTreeWithSize.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());

    Pair<InsertionOrderSplayTreeWithSize<String>> pair =
        InsertionOrderSplayTreeWithSize.split(tree, "M");
    System.err.println(pair.first.printTree());

    pair.first.validate();

    System.err.println(pair.second.printTree());

    pair.second.validate();
  }

  @Test
  public void testSplit() {
    tree = InsertionOrderSplayTreeWithSize.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree("starting tree"));

    InsertionOrderSplayTreeWithSize<String> newTree = tree.split("M");
    System.err.println(newTree.printTree("split off tree"));

    newTree.validate();

    System.err.println(tree.printTree("tree after split"));
    tree.validate();

    tree.join(newTree);
    System.err.println(tree.printTree("Joined Tree"));
    tree.validate();
  }

  /**
   * create a tree of 26 upper-case characters randomly split and join the tree at different
   * locations validate the split and re-joined tree
   */
  @Test
  public void testSplits() {

    tree = InsertionOrderSplayTreeWithSize.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());

    for (int splitPoint : shuffledInts(0, tree.size())) {
      InsertionOrderSplayTreeWithSize<String> splitter = tree.split(splitPoint);
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

    tree = InsertionOrderSplayTreeWithSize.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());
    tree.validate();

    for (int splitPoint : shuffledInts(0, tree.size())) {
      tree.validate();
      Pair<InsertionOrderSplayTreeWithSize<String>> pair =
          InsertionOrderSplayTreeWithSize.split(tree, splitPoint);
      System.err.println("split at " + splitPoint);

      InsertionOrderSplayTreeWithSize<String> left = pair.first;
      InsertionOrderSplayTreeWithSize<String> right = pair.second;
      System.err.println("left size: " + left.size());
      System.err.println("right size: " + right.size());
      System.err.println(left.printTree("split off tree"));
      System.err.println(right.printTree("split off tree"));

      left.validate();
      right.validate();
      System.err.println(
          "__________________________________________________________________________");

      InsertionOrderSplayTreeWithSize<String> joined = InsertionOrderSplayTreeWithSize.join(pair);
      System.err.println(joined.printTree("Joined Tree"));
      joined.validate();
      tree = joined;
    }
  }

  @Test
  public void testStaticSplitAtZero() {

    tree = InsertionOrderSplayTreeWithSize.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());

    Pair<InsertionOrderSplayTreeWithSize<String>> pair =
        InsertionOrderSplayTreeWithSize.split(tree, 0);
    System.err.println("split at " + 0);

    InsertionOrderSplayTreeWithSize<String> left = pair.first;
    InsertionOrderSplayTreeWithSize<String> right = pair.second;
    System.err.println("left size: " + left.size());
    System.err.println("right size: " + right.size());
    System.err.println(left.printTree("split off tree"));
    System.err.println(right.printTree("split off tree"));

    left.validate();
    right.validate();
    System.err.println(
        "__________________________________________________________________________");

    InsertionOrderSplayTreeWithSize<String> joined = InsertionOrderSplayTreeWithSize.join(pair);
    System.err.println(joined.printTree("Joined Tree"));
    joined.validate();
  }

  @Test
  public void testStaticSplitAtTop() {

    tree = InsertionOrderSplayTreeWithSize.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());

    Pair<InsertionOrderSplayTreeWithSize<String>> pair =
        InsertionOrderSplayTreeWithSize.split(tree, tree.size());
    System.err.println("split at " + tree.size());

    InsertionOrderSplayTreeWithSize<String> left = pair.first;
    InsertionOrderSplayTreeWithSize<String> right = pair.second;
    System.err.println("left size: " + left.size());
    System.err.println("right size: " + right.size());
    System.err.println(left.printTree("split off tree"));
    System.err.println(right.printTree("split off tree"));

    left.validate();
    right.validate();
    System.err.println(
        "__________________________________________________________________________");

    InsertionOrderSplayTreeWithSize<String> joined = InsertionOrderSplayTreeWithSize.join(pair);
    System.err.println(joined.printTree("Joined Tree"));
    joined.validate();
  }

  @Test
  public void testStaticSplitAtOverTop() {

    tree = InsertionOrderSplayTreeWithSize.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());

    Pair<InsertionOrderSplayTreeWithSize<String>> pair =
        InsertionOrderSplayTreeWithSize.split(tree, tree.size() + 10);
    System.err.println("split at " + tree.size() + 10);

    InsertionOrderSplayTreeWithSize<String> left = pair.first;
    InsertionOrderSplayTreeWithSize<String> right = pair.second;
    System.err.println("left size: " + left.size());
    System.err.println("right size: " + right.size());
    System.err.println(left.printTree("split off tree"));
    System.err.println(right.printTree("split off tree"));

    left.validate();
    right.validate();
    System.err.println(
        "__________________________________________________________________________");

    InsertionOrderSplayTreeWithSize<String> joined = InsertionOrderSplayTreeWithSize.join(pair);
    System.err.println(joined.printTree("Joined Tree"));
    joined.validate();
  }

  @Test
  public void testStaticSplitAtNegative() {

    tree = InsertionOrderSplayTreeWithSize.create();
    for (char c = 'A'; c <= 'Z'; c++) {
      tree.append("" + c);
    }
    System.err.println(tree.printTree());

    Pair<InsertionOrderSplayTreeWithSize<String>> pair =
        InsertionOrderSplayTreeWithSize.split(tree, -10);
    System.err.println("split at " + -10);

    InsertionOrderSplayTreeWithSize<String> left = pair.first;
    InsertionOrderSplayTreeWithSize<String> right = pair.second;
    System.err.println("left size: " + left.size());
    System.err.println("right size: " + right.size());
    System.err.println(left.printTree("split off tree"));
    System.err.println(right.printTree("split off tree"));

    left.validate();
    right.validate();
    System.err.println(
        "__________________________________________________________________________");

    InsertionOrderSplayTreeWithSize<String> joined = InsertionOrderSplayTreeWithSize.join(pair);
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

      tree = InsertionOrderSplayTreeWithSize.create();
      chars.forEach(tree::append);
      System.err.println(tree.printTree("starting tree"));

      Pair<InsertionOrderSplayTreeWithSize<String>> pair =
          InsertionOrderSplayTreeWithSize.split(tree, key);

      System.err.println(pair.first.printTree("first split off tree"));
      System.err.println(pair.second.printTree("second split off tree"));
      pair.first.validate();
      pair.second.validate();
    }
  }

  @Test
  public void joinWithEmptyTree() {
    tree = InsertionOrderSplayTreeWithSize.create();

    InsertionOrderSplayTreeWithSize<String> joiner =
        InsertionOrderSplayTreeWithSize.create(new InsertionOrderSplayTreeWithSize.Node("Z"));

    tree.join(joiner);

    System.err.println(joiner.printTree());

    InsertionOrderSplayTreeWithSize<String> next =
        InsertionOrderSplayTreeWithSize.create(new InsertionOrderSplayTreeWithSize.Node<>("A"));
    InsertionOrderSplayTreeWithSize.join(Pair.of(tree, next));
    System.err.println(tree.printTree());
    tree.splay("A");
    System.err.println(tree.printTree());

    TreePrinter.print(tree);
  }

  // Implementing Fisher–Yates shuffle
  static void shuffleArray(int[] ar) {
    Random rnd = ThreadLocalRandom.current();
    for (int i = ar.length - 1; i > 0; i--) {
      int index = rnd.nextInt(i + 1);
      // Simple swap
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
