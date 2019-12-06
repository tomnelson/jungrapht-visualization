package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.ArrayList;
import java.util.List;
import org.jungrapht.visualization.layout.algorithms.util.sugiyama.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLevelCross {

  private static final Logger log = LoggerFactory.getLogger(TestLevelCross.class);

  // vertices for any tests
  SugiyamaVertex<String> a = SugiyamaVertex.of("a", 0, 0);
  SugiyamaVertex<String> b = SugiyamaVertex.of("b", 0, 1);
  SugiyamaVertex<String> c = SugiyamaVertex.of("c", 0, 2);
  SugiyamaVertex<String> d = SugiyamaVertex.of("d", 0, 3);
  SugiyamaVertex<String> e = SugiyamaVertex.of("e", 0, 4);

  SugiyamaVertex<String> z = SugiyamaVertex.of("z", 1, 0);
  SugiyamaVertex<String> x = SugiyamaVertex.of("x", 1, 1);
  SugiyamaVertex<String> y = SugiyamaVertex.of("y", 1, 2);
  SugiyamaVertex<String> v = SugiyamaVertex.of("v", 1, 3);
  SugiyamaVertex<String> u = SugiyamaVertex.of("u", 1, 4);
  List<SugiyamaVertex<String>> topLevel = new ArrayList<>();
  List<SugiyamaVertex<String>> botLevel = new ArrayList<>();
  List<SugiyamaEdge<String, Integer>> edgeList = new ArrayList<>();

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
    edgeList.add(SugiyamaEdge.of(edge++, a, z));
    edgeList.add(SugiyamaEdge.of(edge++, a, x));
    edgeList.add(SugiyamaEdge.of(edge++, a, y));
    edgeList.add(SugiyamaEdge.of(edge++, a, v));
    edgeList.add(SugiyamaEdge.of(edge++, a, u));

    edgeList.add(SugiyamaEdge.of(edge++, b, z));
    edgeList.add(SugiyamaEdge.of(edge++, b, x));
    edgeList.add(SugiyamaEdge.of(edge++, b, y));
    edgeList.add(SugiyamaEdge.of(edge++, b, v));
    edgeList.add(SugiyamaEdge.of(edge++, b, u));

    edgeList.add(SugiyamaEdge.of(edge++, c, z));
    edgeList.add(SugiyamaEdge.of(edge++, c, x));
    edgeList.add(SugiyamaEdge.of(edge++, c, y));
    edgeList.add(SugiyamaEdge.of(edge++, c, v));
    edgeList.add(SugiyamaEdge.of(edge++, c, u));

    edgeList.add(SugiyamaEdge.of(edge++, d, z));
    edgeList.add(SugiyamaEdge.of(edge++, d, x));
    edgeList.add(SugiyamaEdge.of(edge++, d, y));
    edgeList.add(SugiyamaEdge.of(edge++, d, v));
    edgeList.add(SugiyamaEdge.of(edge++, d, u));

    edgeList.add(SugiyamaEdge.of(edge++, e, z));
    edgeList.add(SugiyamaEdge.of(edge++, e, x));
    edgeList.add(SugiyamaEdge.of(edge++, e, y));
    edgeList.add(SugiyamaEdge.of(edge++, e, v));
    edgeList.add(SugiyamaEdge.of(edge++, e, u));
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
    edgeList.add(SugiyamaEdge.of(e++, a, z));
    edgeList.add(SugiyamaEdge.of(e++, b, x));
    edgeList.add(SugiyamaEdge.of(e++, c, y));
    // trt
    edgeList.add(SugiyamaEdge.of(e++, a, y));
    assert 1 == go();
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
    edgeList.add(SugiyamaEdge.of(e++, a, z));
    edgeList.add(SugiyamaEdge.of(e++, a, x));
    edgeList.add(SugiyamaEdge.of(e++, b, x));
    edgeList.add(SugiyamaEdge.of(e++, c, y));
    edgeList.add(SugiyamaEdge.of(e++, a, y));
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
    edgeList.add(SugiyamaEdge.of(e++, a, z));
    edgeList.add(SugiyamaEdge.of(e++, b, x));
    edgeList.add(SugiyamaEdge.of(e++, c, y));
    // trt
    edgeList.add(SugiyamaEdge.of(e++, a, y));
    // trb
    edgeList.add(SugiyamaEdge.of(e++, c, z));
    assert 3 == go();
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
    edgeList.add(SugiyamaEdge.of(e++, a, z));
    edgeList.add(SugiyamaEdge.of(e++, c, y));
    edgeList.add(SugiyamaEdge.of(e++, a, y));
    assert 0 == go();
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
    edgeList.add(SugiyamaEdge.of(e++, a, z));
    edgeList.add(SugiyamaEdge.of(e++, c, y));
    edgeList.add(SugiyamaEdge.of(e++, a, y));
    edgeList.add(SugiyamaEdge.of(e++, c, z));
    assert 1 == go();
  }

  @Test
  public void testLevelCross10() { // 1
    topLevel.add(a);
    topLevel.add(b);

    botLevel.add(z);
    botLevel.add(x);

    int e = 0;
    edgeList.add(SugiyamaEdge.of(e++, a, x));
    edgeList.add(SugiyamaEdge.of(e++, b, z));

    assert 1 == go();
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
    edgeList.add(SugiyamaEdge.of(e++, a, z));
    edgeList.add(SugiyamaEdge.of(e++, b, x));
    edgeList.add(SugiyamaEdge.of(e++, c, y));
    edgeList.add(SugiyamaEdge.of(e++, c, z));
    assert 1 == go();
  }

  private int go() {
    //        SVTransformedGraphSupplier<String,Integer> transformedGraphSupplier = new SVTransformedGraphSupplier<>(graph);
    //        this.svGraph = transformedGraphSupplier.get();
    //
    //        RemoveCycles<SV<String>,SE<String,Integer>> removeCycles = new RemoveCycles<>(svGraph);
    //        svGraph = removeCycles.removeCycles();
    //
    //        AssignLayers<String,Integer> assignLayers = new AssignLayers<>(svGraph);
    List<List<SugiyamaVertex<String>>> layers = List.of(topLevel, botLevel);
    log.info("layer 0: {}", layers.get(0));
    log.info("layer 1: {}", layers.get(1));

    LevelCross<String, Integer> levelCross =
        new LevelCross<>(edgeList, layers.get(0), layers.get(1));
    log.info("levelCross: {}", levelCross);

    int crossCount = levelCross.levelCross();
    log.info("levelCross: {}", levelCross);

    log.info("got crossCount: {}", crossCount);
    return crossCount;
  }
}