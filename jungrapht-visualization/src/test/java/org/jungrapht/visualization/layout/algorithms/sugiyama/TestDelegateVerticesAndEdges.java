package org.jungrapht.visualization.layout.algorithms.sugiyama;

import static org.junit.jupiter.api.Assertions.*;

import org.jungrapht.visualization.layout.util.synthetics.SVI;
import org.jungrapht.visualization.layout.util.synthetics.Synthetic;
import org.junit.jupiter.api.Test;

public class TestDelegateVerticesAndEdges {

  @Test
  public void testVertices() {
    String a = "a";
    String b = "b";

    LV<String> sa = LV.of(a);
    LV<String> sb = LV.of(b);

    assertInstanceOf(SVI.class, sa);
    assertNotEquals(sa, sb);
    assertEquals(sa, LV.of("a"));
    LV<String> syntheticA = new SyntheticLV<>();
    assertNotEquals(syntheticA, sa);
    assertInstanceOf(Synthetic.class, syntheticA);
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
    assertInstanceOf(LEI.class, se1);
    assertInstanceOf(LEI.class, se2);
    assertNotEquals(se1, se2);
    assertEquals(se2, se3);
    assertInstanceOf(Synthetic.class, new SyntheticLE<>(se1, sa, sb));
  }
}
