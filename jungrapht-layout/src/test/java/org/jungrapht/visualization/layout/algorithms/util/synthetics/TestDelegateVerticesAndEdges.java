package org.jungrapht.visualization.layout.algorithms.util.synthetics;

import static org.junit.jupiter.api.Assertions.*;

import org.jungrapht.visualization.layout.util.synthetics.SE;
import org.jungrapht.visualization.layout.util.synthetics.SEI;
import org.jungrapht.visualization.layout.util.synthetics.SV;
import org.jungrapht.visualization.layout.util.synthetics.SVI;
import org.jungrapht.visualization.layout.util.synthetics.Synthetic;
import org.jungrapht.visualization.layout.util.synthetics.SyntheticSE;
import org.jungrapht.visualization.layout.util.synthetics.SyntheticSV;
import org.junit.jupiter.api.Test;

public class TestDelegateVerticesAndEdges {

  @Test
  public void testVertices() {
    String a = "a";
    String b = "b";

    SV<String> sa = SV.of(a);
    SV<String> sb = SV.of(b);

    assertInstanceOf(SVI.class, sa);
    assertNotEquals(sa, sb);
    assertEquals(sa, SV.of("a"));
    SV<String> syntheticA = new SyntheticSV();
    assertNotEquals(syntheticA, sa);
    assertInstanceOf(Synthetic.class, syntheticA);
  }

  @Test
  public void testEdges() {

    Integer edge1 = 1;
    int edge2 = 2;

    SE<Integer> se1 = SE.of(edge1);
    SE<Integer> se2 = SE.of(edge2);
    SE<Integer> se3 = SE.of(2);
    assertInstanceOf(SEI.class, se1);
    assertInstanceOf(SEI.class, se2);
    assertNotEquals(se1, se2);
    assertEquals(se2, se3);
    assertInstanceOf(Synthetic.class, new SyntheticSE<>());
  }
}
