package org.jungrapht.visualization.layout.algorithms.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSplayTree {

  private static final Logger log = LoggerFactory.getLogger(TestSplayTree.class);

  SplayTree<Integer> tree;

  @Before
  public void setup() {
    tree = SplayTree.create();
    tree.insert(50);
    tree.insert(30);
    tree.insert(60);
    tree.insert(10);
    tree.insert(40);
    tree.insert(90);

    tree.insert(20);
    tree.insert(70);
    tree.insert(100);

    tree.insert(15);
  }

  @Test
  public void testRemoveLeftLeaf() {

    Assert.assertEquals(10, tree.size());

    tree.splay(tree.find(50));
    TreePrinter.print(tree);
    System.err.println("height: " + tree.height());
    tree.insert(80);
    Assert.assertEquals(11, tree.size());
    TreePrinter.print(tree);
    System.err.println("height: " + tree.height());
    tree.erase(15);
    TreePrinter.print(tree);
    System.err.println("height: " + tree.height());
    Assert.assertEquals(10, tree.size());

    Assert.assertEquals(100, tree.max().key.intValue());
    Assert.assertEquals(10, tree.min().key.intValue());

    tree.splay(tree.max());
    TreePrinter.print(tree);
    System.err.println("height: " + tree.height());

    tree.splay(tree.min());
    TreePrinter.print(tree);
    System.err.println("height: " + tree.height());
  }

  @Test
  public void testSplitAndJoin() {
    tree.splay(tree.find(50));
    TreePrinter.print(tree);
    SplayTree<Integer> greater = tree.split(50);
    TreePrinter.print(tree);
    TreePrinter.print(greater);

    tree.join(greater);
    TreePrinter.print(tree);
  }

  @Test
  public void testSplayWithElementNotThere() {
    tree.splay(999);
    TreePrinter.print(tree);
    tree.findSplay(70);
    TreePrinter.print(tree);
  }
}
