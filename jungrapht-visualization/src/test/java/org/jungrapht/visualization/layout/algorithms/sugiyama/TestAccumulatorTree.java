package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestAccumulatorTree {

  private static Logger log = LoggerFactory.getLogger(TestAccumulatorTree.class);

  AccumulatorTree tree;

  @Test
  public void testZeroBased() {
    tree = new AccumulatorTree(4);
    log.info(" initial tree: {}", tree);
    tree.addEdge(0);
    log.info("add 0 tree: {}", tree);
    tree.addEdge(1);
    log.info("add 1 tree: {}", tree);
    tree.addEdge(2);
    log.info("add 2 tree: {}", tree);
    tree.addEdge(3);
    log.info("add 3 tree: {}", tree);

    for (int i = 0; i < 4; i++) {
      log.info("countEdges {}", tree.countEdges(i, 3));
    }
  }

  @Test
  public void testOneBased() {
    AccumulatorTreeOneBased tree = new AccumulatorTreeOneBased(4);
    log.info(" initial tree: {}", tree);
    tree.addEdge(1);
    log.info("add 1 tree: {}", tree);
    tree.addEdge(2);
    log.info("add 2 tree: {}", tree);
    tree.addEdge(3);
    log.info("add 3 tree: {}", tree);
    tree.addEdge(4);
    log.info("add 4 tree: {}", tree);

    for (int i = 1; i < 5; i++) {
      log.info("countEdges {}", tree.countEdges(i));
    }
  }
}
