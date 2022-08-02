package org.jungrapht.samples.util;

import java.util.HashMap;
import java.util.Map;
import org.jungrapht.visualization.layout.algorithms.*;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFA2Repulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutSpringRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardFA2Repulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardSpringRepulsion;

public class LayoutHelper {

  public enum Layouts {
    KK("Kamada Kawai", KKLayoutAlgorithm.builder()),
    CIRCLE("Circle", CircleLayoutAlgorithm.builder().reduceEdgeCrossing(false).threaded(false)),
    REDUCE_XING_CIRCLE(
        "Reduce Xing Circle", CircleLayoutAlgorithm.builder().reduceEdgeCrossing(true)),
    SELF_ORGANIZING_MAP("Self Organizing Map", ISOMLayoutAlgorithm.builder()),
    FR(
        "Fruchterman Reingold (Not Optimized)",
        FRLayoutAlgorithm.builder().repulsionContractBuilder(StandardFRRepulsion.builder())),
    FR_BH_VISITOR(
        "Fruchterman Reingold (Barnes Hut Optimized)",
        FRLayoutAlgorithm.builder().repulsionContractBuilder(BarnesHutFRRepulsion.builder())),
    FA2(
        "ForceAtlas2 (Not Optimized)",
        ForceAtlas2LayoutAlgorithm.builder()
            .repulsionContractBuilder(StandardFA2Repulsion.builder())),
    FA2_BH_VISITOR(
        "ForceAtlas2 (Barnes Hut Optimized)",
        ForceAtlas2LayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutFA2Repulsion.builder().repulsionK(100))),
    SPRING(
        "Spring (Not Optimized)",
        SpringLayoutAlgorithm.builder()
            .repulsionContractBuilder(StandardSpringRepulsion.builder())),
    SPRING_BH_VISITOR(
        "Spring (Barnes Hut Optimized)",
        SpringLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutSpringRepulsion.builder())),
    GEM("GEM", GEMLayoutAlgorithm.edgeAwareBuilder()),
    TREE("Tree", TreeLayoutAlgorithm.builder()),
    MULTI_ROW_TREE("Multirow Tree", MultiRowTreeLayoutAlgorithm.builder()),
    TIDY_TREE("Tidy Tree", TidierTreeLayoutAlgorithm.edgeAwareBuilder()),
    TIDY_RADIAL_TREE("Tidy Radial Tree", TidierRadialTreeLayoutAlgorithm.edgeAwareBuilder()),
    BALLOON("Balloon", BalloonLayoutAlgorithm.builder()),
    SUGIYAMA("Sugiyama", EiglspergerLayoutAlgorithm.edgeAwareBuilder().threaded(false)),
    RADIAL("Radial", RadialTreeLayoutAlgorithm.builder());

    private static final Map<String, Layouts> BY_NAME = new HashMap<>();

    static {
      for (Layouts layout : values()) {
        BY_NAME.put(layout.name, layout);
      }
    }

    Layouts(String name, LayoutAlgorithm.Builder layoutAlgorithmBuilder) {
      this.name = name;
      this.layoutAlgorithmBuilder = layoutAlgorithmBuilder;
    }

    private final String name;

    private final LayoutAlgorithm.Builder layoutAlgorithmBuilder;

    public LayoutAlgorithm.Builder getLayoutAlgorithmBuilder() {
      return layoutAlgorithmBuilder;
    }

    @Override
    public String toString() {
      return name;
    }

    public static Layouts valueOfName(String name) {
      return BY_NAME.get(name);
    }

    public LayoutAlgorithm getLayoutAlgorithm() {
      return layoutAlgorithmBuilder.build();
    }
  }

  public static Layouts[] getCombos() {
    return Layouts.values();
  }
}
