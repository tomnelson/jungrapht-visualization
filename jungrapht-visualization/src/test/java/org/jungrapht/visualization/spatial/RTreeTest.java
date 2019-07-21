package org.jungrapht.visualization.spatial.rtree;

import java.awt.geom.Rectangle2D;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTreeTest {

  private static final Logger log = LoggerFactory.getLogger(RTreeTest.class);

  SplitterContext<String> splitterContext =
      SplitterContext.of(new RStarLeafSplitter<>(), new RStarSplitter<>());
  RTree<String> rTree = RTree.create();
  int count;
  int width = 1000;
  int height = 1000;

  private void addRandomShape() {
    double w = 10;
    double h = 10;
    double x = Math.random() * width - w;
    double y = Math.random() * height - h;
    Rectangle2D r = new Rectangle2D.Double(x, y, w, h);
    rTree = RTree.add(rTree, splitterContext, "N" + count++, r);
  }

  @Test
  public void testAddRemoveOne() {
    addRandomShape();
    log.info("rtree: {}", rTree);
    rTree = RTree.remove(rTree, "N0");
    log.info("rtree: {}", rTree);
    Assert.assertTrue(rTree.count() == 0);
  }

  @Test
  public void testAddTwoRemoveOne() {
    addRandomShape();
    addRandomShape();
    log.info("rtree: {}", rTree);
    rTree = RTree.remove(rTree, "N0");
    log.info("rtree: {}", rTree);
    Assert.assertTrue(rTree.count() == 1);
  }

  @Test
  public void testAddHundredRemoveOne() {
    for (int i = 0; i < 100; i++) {
      addRandomShape();
    }
    Assert.assertTrue(rTree.count() == 100);
    rTree = RTree.remove(rTree, "N0");
    log.info("rtree: {}", rTree);
    Assert.assertTrue(rTree.count() == 99);
  }

  @Test
  public void testAddRemoveAll() {
    log.info("RTREE: " + rTree);
    count = 0;
    for (int i = 0; i < 1000; i++) {
      addRandomShape();
    }
    Assert.assertTrue(rTree.getRoot().isPresent());
    Assert.assertTrue(rTree.count() == 1000);
    Node<String> root = rTree.getRoot().get();
    log.info("Root kid size is {}", root.getChildren().size());
    log.info("RTree count: {}", rTree.count());
    assertHasChildren(root);
    //    System.err.println("RTREE: " + rTree);

    for (int i = 0; i < 1000; i++) {
      rTree = RTree.remove(rTree, "N" + i);
      log.info("count now {}", rTree.count());
    }
    Assert.assertFalse(rTree.getRoot().isPresent());
    count = 0;
    for (int i = 0; i < 10; i++) {
      addRandomShape();
    }
    log.info("RTREE: " + rTree);
    assertHasChildren(rTree.getRoot().get());
  }

  /**
   * all nodes have children (none are empty)
   *
   * @param parent
   */
  private void assertHasChildren(TreeNode parent) {
    if (parent instanceof LeafNode) {
      Assert.assertTrue(((LeafNode) parent).size() > 0);
    } else {
      Assert.assertTrue(parent.getChildren().size() > 0);
      parent.getChildren().forEach(this::assertHasChildren);
    }
  }
}
