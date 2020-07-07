package org.jungrapht.samples.util;

import java.util.HashMap;
import java.util.Map;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.ForceAtlas2LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.GEMLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.ISOMLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.MultiRowTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFA2Repulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutSpringRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardFA2Repulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardSpringRepulsion;

public class LayoutHelper {

  public enum Layouts {
    KK("Kamada Kawai", new KKLayoutAlgorithm<>()),
    CIRCLE(
        "Circle",
        CircleLayoutAlgorithm.builder().reduceEdgeCrossing(false).threaded(false).build()),
    REDUCE_XING_CIRCLE(
        "Reduce Xing Circle", CircleLayoutAlgorithm.builder().reduceEdgeCrossing(true).build()),
    SELF_ORGANIZING_MAP("Self Organizing Map", new ISOMLayoutAlgorithm<>()),
    FR(
        "Fruchterman Reingold (Not Optimized)",
        FRLayoutAlgorithm.builder()
            .repulsionContractBuilder(StandardFRRepulsion.builder())
            .build()),
    FR_BH_VISITOR(
        "Fruchterman Reingold (Barnes Hut Optimized)",
        FRLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutFRRepulsion.builder())
            .build()),
    FA2(
        "ForceAtlas2 (Not Optimized)",
        ForceAtlas2LayoutAlgorithm.builder()
            .repulsionContractBuilder(StandardFA2Repulsion.builder())
            .build()),
    FA2_BH_VISITOR(
        "ForceAtlas2 (Barnes Hut Optimized)",
        ForceAtlas2LayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutFA2Repulsion.builder()) // the default
            .build()),
    SPRING(
        "Spring (Not Optimized)",
        SpringLayoutAlgorithm.builder()
            .repulsionContractBuilder(StandardSpringRepulsion.builder())
            .build()),
    SPRING_BH_VISITOR(
        "Spring (Barnes Hut Optimized)",
        SpringLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutSpringRepulsion.builder()) // the default
            .build()),
    GEM("GEM", GEMLayoutAlgorithm.edgeAwareBuilder().build()),
    TREE("Tree", new TreeLayoutAlgorithm<>()),
    MULTI_ROW_TREE("Multirow Tree", new MultiRowTreeLayoutAlgorithm<>()),
    BALLOON("Balloon", new BalloonLayoutAlgorithm<>()),
    RADIAL("Radial", new RadialTreeLayoutAlgorithm<>());

    private static final Map<String, Layouts> BY_NAME = new HashMap<>();

    static {
      for (Layouts layout : values()) {
        BY_NAME.put(layout.name, layout);
      }
    }

    Layouts(String name, LayoutAlgorithm layoutAlgorithm) {
      this.name = name;
      this.layoutAlgorithm = layoutAlgorithm;
    }

    private final String name;

    private final LayoutAlgorithm layoutAlgorithm;

    public LayoutAlgorithm getLayoutAlgorithm() {
      return layoutAlgorithm;
    }

    @Override
    public String toString() {
      return name;
    }

    public static Layouts valueOfName(String name) {
      return BY_NAME.get(name);
    }
  }

  public static Layouts[] getCombos() {
    return Layouts.values();
  }
}
