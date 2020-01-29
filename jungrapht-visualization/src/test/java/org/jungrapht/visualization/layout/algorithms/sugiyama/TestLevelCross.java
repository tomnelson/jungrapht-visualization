package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLevelCross {

  private static final Logger log = LoggerFactory.getLogger(TestLevelCross.class);

  // vertices for any tests
  LV<String> a = LV.of("a", 0, 0);
  LV<String> b = LV.of("b", 0, 1);
  LV<String> c = LV.of("c", 0, 2);
  LV<String> d = LV.of("d", 0, 3);
  LV<String> e = LV.of("e", 0, 4);

  LV<String> z = LV.of("z", 1, 0);
  LV<String> x = LV.of("x", 1, 1);
  LV<String> y = LV.of("y", 1, 2);
  LV<String> v = LV.of("v", 1, 3);
  LV<String> u = LV.of("u", 1, 4);
  List<LV<String>> topLevel = new ArrayList<>();
  List<LV<String>> botLevel = new ArrayList<>();
  List<LE<String, Integer>> edgeList = new ArrayList<>();

  @Test
  public void testLevelCross() {
    topLevel.add(a);
    topLevel.add(b);
    topLevel.add(c);
    topLevel.add(d);
    topLevel.add(e);

    botLevel.add(z);
    botLevel.add(x);
    botLevel.add(y);
    botLevel.add(v);
    botLevel.add(u);

    // all edges
    int edge = 0;
    edgeList.add(LE.of(edge++, a, z));
    edgeList.add(LE.of(edge++, a, x));
    edgeList.add(LE.of(edge++, a, y));
    edgeList.add(LE.of(edge++, a, v));
    edgeList.add(LE.of(edge++, a, u));

    edgeList.add(LE.of(edge++, b, z));
    edgeList.add(LE.of(edge++, b, x));
    edgeList.add(LE.of(edge++, b, y));
    edgeList.add(LE.of(edge++, b, v));
    edgeList.add(LE.of(edge++, b, u));

    edgeList.add(LE.of(edge++, c, z));
    edgeList.add(LE.of(edge++, c, x));
    edgeList.add(LE.of(edge++, c, y));
    edgeList.add(LE.of(edge++, c, v));
    edgeList.add(LE.of(edge++, c, u));

    edgeList.add(LE.of(edge++, d, z));
    edgeList.add(LE.of(edge++, d, x));
    edgeList.add(LE.of(edge++, d, y));
    edgeList.add(LE.of(edge++, d, v));
    edgeList.add(LE.of(edge++, d, u));

    edgeList.add(LE.of(edge++, e, z));
    edgeList.add(LE.of(edge++, e, x));
    edgeList.add(LE.of(edge++, e, y));
    edgeList.add(LE.of(edge++, e, v));
    edgeList.add(LE.of(edge++, e, u));
    go();
  }

  @Test
  public void testLevelCross5() { // should be 1
    // az, bx, cy, ay.. bx and ay cross
    topLevel.add(a);
    topLevel.add(b);
    topLevel.add(c);

    botLevel.add(z);
    botLevel.add(x);
    botLevel.add(y);

    int e = 0;
    // pairs
    edgeList.add(LE.of(e++, a, z));
    edgeList.add(LE.of(e++, b, x));
    edgeList.add(LE.of(e++, c, y));
    // trt
    edgeList.add(LE.of(e++, a, y));
    Assert.assertEquals(1, go());
  }

  @Test
  public void testLevelCross6() { //1
    // az, ax, bx, cy, cz, ay.. bx and ay cross, cz and bx cross, cz and ay cross
    topLevel.add(a);
    topLevel.add(b);
    topLevel.add(c);

    botLevel.add(z);
    botLevel.add(x);
    botLevel.add(y);

    int e = 0;
    edgeList.add(LE.of(e++, a, z));
    edgeList.add(LE.of(e++, a, x));
    edgeList.add(LE.of(e++, b, x));
    edgeList.add(LE.of(e++, c, y));
    edgeList.add(LE.of(e++, a, y));
    assert 1 == go();
  }

  @Test
  public void testLevelCross7() { // 3
    topLevel.add(a);
    topLevel.add(b);
    topLevel.add(c);

    botLevel.add(z);
    botLevel.add(x);
    botLevel.add(y);

    int e = 0;
    //pairs
    edgeList.add(LE.of(e++, a, z));
    edgeList.add(LE.of(e++, b, x));
    edgeList.add(LE.of(e++, c, y));
    // trt
    edgeList.add(LE.of(e++, a, y));
    // trb
    edgeList.add(LE.of(e++, c, z));
    Assert.assertEquals(3, go());
  }

  @Test
  public void testLevelCross8() { // 0
    topLevel.add(a);
    topLevel.add(b);
    topLevel.add(c);

    botLevel.add(z);
    botLevel.add(x);
    botLevel.add(y);

    int e = 0;
    edgeList.add(LE.of(e++, a, z));
    edgeList.add(LE.of(e++, c, y));
    edgeList.add(LE.of(e++, a, y));
    Assert.assertEquals(0, go());
  }

  @Test
  public void testLevelCross9() { // 1
    topLevel.add(a);
    topLevel.add(b);
    topLevel.add(c);

    botLevel.add(z);
    botLevel.add(x);
    botLevel.add(y);

    int e = 0;
    edgeList.add(LE.of(e++, a, z));
    edgeList.add(LE.of(e++, c, y));
    edgeList.add(LE.of(e++, a, y));
    edgeList.add(LE.of(e++, c, z));
    Assert.assertEquals(1, go());
  }

  @Test
  public void testLevelCross10() { // 1
    topLevel.add(a);
    topLevel.add(b);

    botLevel.add(z);
    botLevel.add(x);

    int e = 0;
    edgeList.add(LE.of(e++, a, x));
    edgeList.add(LE.of(e++, b, z));

    Assert.assertEquals(1, go());
  }

  @Test
  public void testLevelCross11() { // should be 1
    // az, bx, cy, ay.. bx and ay cross
    topLevel.add(a);
    topLevel.add(b);
    topLevel.add(c);

    botLevel.add(z);
    botLevel.add(x);
    botLevel.add(y);

    int e = 0;
    edgeList.add(LE.of(e++, a, z));
    edgeList.add(LE.of(e++, b, x));
    edgeList.add(LE.of(e++, c, y));
    edgeList.add(LE.of(e++, c, z));
    Assert.assertEquals(1, go());
  }

  private int go() {
    //        SVTransformedGraphSupplier<String,Integer> transformedGraphSupplier = new SVTransformedGraphSupplier<>(graph);
    //        this.svGraph = transformedGraphSupplier.get();
    //
    //        RemoveCycles<SV<String>,SE<String,Integer>> removeCycles = new RemoveCycles<>(svGraph);
    //        svGraph = removeCycles.removeCycles();
    //
    //        AssignLayers<String,Integer> assignLayers = new AssignLayers<>(svGraph);
    List<List<LV<String>>> layers = List.of(topLevel, botLevel);
    log.info("layer 0: {}", layers.get(0));
    log.info("layer 1: {}", layers.get(1));

    LevelCross<String, Integer> levelCross =
        new LevelCross<>(edgeList, layers.get(0).size(), 0, layers.get(1).size(), 1);
    log.info("levelCross: {}", levelCross);

    int crossCount = levelCross.levelCross();
    log.info("levelCross: {}", levelCross);

    log.info("got crossCount: {}", crossCount);
    return crossCount;
  }
}
