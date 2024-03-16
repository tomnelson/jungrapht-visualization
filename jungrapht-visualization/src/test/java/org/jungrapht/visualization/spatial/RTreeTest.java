package org.jungrapht.visualization.spatial;

import static org.junit.Assert.*;

import java.awt.geom.Rectangle2D;
import org.jungrapht.visualization.spatial.rtree.LeafNode;
import org.jungrapht.visualization.spatial.rtree.Node;
import org.jungrapht.visualization.spatial.rtree.RStarLeafSplitter;
import org.jungrapht.visualization.spatial.rtree.RStarSplitter;
import org.jungrapht.visualization.spatial.rtree.RTree;
import org.jungrapht.visualization.spatial.rtree.SplitterContext;
import org.jungrapht.visualization.spatial.rtree.TreeNode;
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
    assertEquals(0, rTree.count());
  }

  @Test
  public void testAddTwoRemoveOne() {
    addRandomShape();
    addRandomShape();
    log.info("rtree: {}", rTree);
    rTree = RTree.remove(rTree, "N0");
    log.info("rtree: {}", rTree);
    assertEquals(rTree.count(), 1);
  }

  @Test
  public void testAddHundredRemoveOne() {
    for (int i = 0; i < 100; i++) {
      addRandomShape();
    }
    assertEquals(100, rTree.count());
    rTree = RTree.remove(rTree, "N0");
    log.info("rtree: {}", rTree);
    assertEquals(99, rTree.count());
  }

  @Test
  public void testAddRemoveAll() {
    log.info("RTREE: " + rTree);
    count = 0;
    for (int i = 0; i < 1000; i++) {
      addRandomShape();
    }
    assertTrue(rTree.getRoot().isPresent());
    assertEquals(1000, rTree.count());
    Node<String> root = rTree.getRoot().get();
    log.info("Root kid size is {}", root.getChildren().size());
    log.info("RTree count: {}", rTree.count());
    assertHasChildren(root);
    //    System.err.println("RTREE: " + rTree);

    for (int i = 0; i < 1000; i++) {
      rTree = RTree.remove(rTree, "N" + i);
      log.info("count now {}", rTree.count());
    }
    assertFalse(rTree.getRoot().isPresent());
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
      assertTrue(((LeafNode) parent).size() > 0);
    } else {
      assertTrue(parent.getChildren().size() > 0);
      parent.getChildren().forEach(this::assertHasChildren);
    }
  }
}
