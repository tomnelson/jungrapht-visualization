package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.jungrapht.visualization.layout.util.synthetics.SVI;
import org.jungrapht.visualization.layout.util.synthetics.Synthetic;
import org.junit.Assert;
import org.junit.Test;

public class TestDelegateVerticesAndEdges {

  @Test
  public void testVertices() {
    String a = "a";
    String b = "b";

    LV<String> sa = LV.of(a);
    LV<String> sb = LV.of(b);

    Assert.assertTrue(sa instanceof SVI);
    Assert.assertNotEquals(sa, sb);
    Assert.assertEquals(sa, LV.of("a"));
    LV<String> syntheticA = new SyntheticLV<>();
    Assert.assertNotEquals(syntheticA, sa);
    Assert.assertTrue(syntheticA instanceof Synthetic);
  }

  @Test
  public void testEdges() {

    LV<String> sa = LV.of("a");
    LV<String> sb = LV.of("b");
    LV<String> sc = LV.of("c");
    Integer edge1 = 1;
    int edge2 = 2;

    LE<String, Integer> se1 = LE.of(edge1, sa, sb);
    LE<String, Integer> se2 = LE.of(edge2, sa, sc);
    LE<String, Integer> se3 = LE.of(2, sa, sc);
    Assert.assertTrue(se1 instanceof LEI);
    Assert.assertTrue(se2 instanceof LEI);
    Assert.assertNotEquals(se1, se2);
    Assert.assertEquals(se2, se3);
    Assert.assertTrue(new SyntheticLE<>(se1, sa, sb) instanceof Synthetic);
  }
}
