package org.jungrapht.visualization.layout.algorithms.barthmutzel;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.util.InsertionSortCounter;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestAccumulatorTree {

  private static Logger log = LoggerFactory.getLogger(TestAccumulatorTree.class);

  LV<String> n0 = LV.of("n0", 0, 0);
  LV<String> n1 = LV.of("n1", 0, 1);
  LV<String> n2 = LV.of("n2", 0, 2);
  LV<String> n3 = LV.of("n3", 0, 3);
  LV<String> n4 = LV.of("n4", 0, 4);
  LV<String> n5 = LV.of("n5", 0, 5);

  LV<String> s0 = LV.of("s0", 0, 0);
  LV<String> s1 = LV.of("s1", 0, 1);
  LV<String> s2 = LV.of("s2", 0, 2);
  LV<String> s3 = LV.of("s3", 0, 3);
  LV<String> s4 = LV.of("s4", 0, 4);

  LE<String, String> e0 = LE.of("e0", n0, s0);
  LE<String, String> e1 = LE.of("e1", n1, s1);
  LE<String, String> e2 = LE.of("e2", n1, s2);
  LE<String, String> e3 = LE.of("e3", n2, s0);
  LE<String, String> e4 = LE.of("e4", n2, s3);
  LE<String, String> e5 = LE.of("e5", n2, s4);
  LE<String, String> e6 = LE.of("e6", n3, s0);
  LE<String, String> e7 = LE.of("e7", n3, s2);
  LE<String, String> e8 = LE.of("e8", n4, s3);
  LE<String, String> e9 = LE.of("e9", n5, s2);
  LE<String, String> e10 = LE.of("e10", n5, s4);

  LV<String>[] layerN = new LV[] {n0, n1, n2, n3, n4, n5};
  LV<String>[] layerS = new LV[] {s0, s1, s2, s3, s4};
  List<LE<String, String>> edges =
      Arrays.asList(new LE[] {e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10});

  Comparator<LE<String, String>> sourceIndexComparator =
      Comparator.comparingInt(e -> e.getSource().getIndex());
  Comparator<LE<String, String>> targetIndexComparator =
      Comparator.comparingInt(e -> e.getTarget().getIndex());
  Comparator<LE<String, String>> biLevelEdgeComparator =
      sourceIndexComparator.thenComparing(targetIndexComparator);

  AccumulatorTree tree;

  @Before
  public void setup() {
    log.info("edges: {}", edges);
    Collections.shuffle(edges);
    log.info("shuffled edges: {}", edges);

    tree = new AccumulatorTree(layerS.length);
    log.info(" initial tree: {}", tree);
    edges.sort(biLevelEdgeComparator);
    log.info("edges: {}", edges);
  }

  @Test
  public void testZeroBased() {

    int[] targetIndices = new int[edges.size()];
    for (int i = 0; i < targetIndices.length; i++) {
      LE<String, String> edge = edges.get(i);
      targetIndices[i] = edge.getTarget().getIndex();
    }
    int count = tree.crossCount(targetIndices, targetIndices.length);
    log.info("count is :{}", count);

    int countIS = InsertionSortCounter.insertionSortCounter(targetIndices);
    log.info("countIS is :{}", countIS);
  }

  @Test
  public void testSwapping() {
    //    for (int j = 0; j <= layerN.length - 2; j++) {
    //      int vw = crossingCount(edges);//reducedEdgeMap.getOrDefault(i, Collections.emptyList()));
    //      // swap v and w and count again
    //      swap(layerN, j, j+1);
    //      int wv = crossingCount(edges);//reducedEdgeMap.getOrDefault(i, Collections.emptyList()));
    //      if (vw <= wv) {
    //        // put them back
    //        swap(layerN, j, j + 1);
    //      }
    //    }
    //    log.info("layerN: {}", layerN);

  }
}
