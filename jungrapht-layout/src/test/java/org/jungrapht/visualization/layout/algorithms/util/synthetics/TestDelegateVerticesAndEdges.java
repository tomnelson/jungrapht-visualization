package org.jungrapht.visualization.layout.algorithms.util.synthetics;

import org.jungrapht.visualization.layout.util.synthetics.SE;
import org.jungrapht.visualization.layout.util.synthetics.SEI;
import org.jungrapht.visualization.layout.util.synthetics.SV;
import org.jungrapht.visualization.layout.util.synthetics.SVI;
import org.jungrapht.visualization.layout.util.synthetics.Synthetic;
import org.jungrapht.visualization.layout.util.synthetics.SyntheticSE;
import org.jungrapht.visualization.layout.util.synthetics.SyntheticSV;
import org.junit.Assert;
import org.junit.Test;

public class TestDelegateVerticesAndEdges {

  @Test
  public void testVertices() {
    String a = "a";
    String b = "b";

    SV<String> sa = SV.of(a);
    SV<String> sb = SV.of(b);

    Assert.assertTrue(sa instanceof SVI);
    Assert.assertNotEquals(sa, sb);
    Assert.assertEquals(sa, SV.of("a"));
    SV<String> syntheticA = new SyntheticSV();
    Assert.assertNotEquals(syntheticA, sa);
    Assert.assertTrue(syntheticA instanceof Synthetic);
  }

  @Test
  public void testEdges() {

    Integer edge1 = 1;
    int edge2 = 2;

    SE<Integer> se1 = SE.of(edge1);
    SE<Integer> se2 = SE.of(edge2);
    SE<Integer> se3 = SE.of(2);
    Assert.assertTrue(se1 instanceof SEI);
    Assert.assertTrue(se2 instanceof SEI);
    Assert.assertNotEquals(se1, se2);
    Assert.assertEquals(se2, se3);
    Assert.assertTrue(new SyntheticSE<>() instanceof Synthetic);
  }
}
