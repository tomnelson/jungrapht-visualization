package org.jungrapht.visualization.layout.quadtree;

import static org.junit.jupiter.api.Assertions.*;

import org.jungrapht.visualization.layout.model.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test of basic construction of the BarnesHutOctTree, including an edge case with forceObjects at
 * the same location
 *
 * @author Tom Nelson
 */
public class BarnesHutQuadTreeTests {

  private static final Logger log = LoggerFactory.getLogger(BarnesHutQuadTreeTests.class);
  private BarnesHutQuadTree<String> tree;

  @BeforeEach
  public void setup() {
    tree = BarnesHutQuadTree.builder().bounds(500, 500).build();
  }

  /**
   * test that edge case where all force objects are at the same location results in the correct
   * tree
   */
  @Test
  public void testOne() {
    ForceObject<String> forceObjectOne = new ForceObject("A", 10, 10);
    ForceObject<String> forceObjectTwo = new ForceObject("B", 10, 10);
    ForceObject<String> forceObjectThree = new ForceObject("C", 10, 10);
    tree.insert(forceObjectOne);
    tree.insert(forceObjectTwo);
    tree.insert(forceObjectThree);

    log.info("tree: {}", tree);
    ForceObject<String> expectedForceObject = new ForceObject("force", 10, 10, 3);
    assertEquals(tree.getRoot().getForceObject(), expectedForceObject);
  }

  /** test a simple construction */
  @Test
  public void testTwo() {
    ForceObject<String> forceObjectA = new ForceObject<>("A", Point.of(200, 100));
    ForceObject<String> forceObjectB = new ForceObject<>("B", Point.of(100, 200));
    ForceObject<String> forceObjectC = new ForceObject<>("C", Point.of(100, 100));
    ForceObject<String> forceObjectD = new ForceObject<>("D", Point.of(500, 100));
    tree.insert(forceObjectA);
    tree.insert(forceObjectB);
    tree.insert(forceObjectC);
    tree.insert(forceObjectD);

    log.info("tree: {}", tree);
    assertNotNull(tree.getRoot());
    Node<String> root = tree.getRoot();
    assertFalse(root.isLeaf());
    Node<String> NW = root.NW;
    assertEquals(NW.forceObject, forceObjectA.add(forceObjectB).add(forceObjectC));
    assertFalse(NW.isLeaf());
    assertEquals(NW.NW.forceObject, forceObjectC);
    assertEquals(NW.NE.forceObject, forceObjectA);
    assertEquals(NW.SW.forceObject, forceObjectB);
    assertNull(NW.SE.forceObject);
    assertEquals(root.NE.forceObject, forceObjectD);
  }
}
