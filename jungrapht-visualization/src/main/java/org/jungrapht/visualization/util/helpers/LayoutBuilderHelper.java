package org.jungrapht.visualization.util.helpers;

import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.ForceAtlas2LayoutAlgorithm;
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

import java.util.HashMap;
import java.util.Map;

public class LayoutBuilderHelper {

  public enum Builders {
    KK("Kamada Kawai", KKLayoutAlgorithm.builder()),
    CIRCLE(
        "Circle",
        CircleLayoutAlgorithm.builder().reduceEdgeCrossing(false).threaded(false)),
    REDUCE_XING_CIRCLE(
        "Reduce Xing Circle",
        CircleLayoutAlgorithm.builder().reduceEdgeCrossing(true).threaded(false)),
    SELF_ORGANIZING_MAP("Self Organizing Map", ISOMLayoutAlgorithm.builder()),
    FR("Fruchterman Reingold", FRLayoutAlgorithm.builder()),
    FR_BH_VISITOR(
        "Fruchterman Reingold (BH Optimized)",
        FRLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutFRRepulsion.barnesHutBuilder())
          ),
    FORCE_ATLAS2(
        "Force Atlas2",
        ForceAtlas2LayoutAlgorithm.builder()
            .repulsionContractBuilder(StandardFA2Repulsion.builder())
            ),
    FORCE_ATLAS2_BH_VISITOR(
        "Force Atlas2 (BH Optimized)",
        ForceAtlas2LayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutFA2Repulsion.builder())
            ),
    SPRING("Spring",  SpringLayoutAlgorithm.builder()),
    SPRING_BH_VISITOR(
        "Spring (BH Optimized)",
        SpringLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutSpringRepulsion.barnesHutBuilder())
            ),
    TREE("Tree", TreeLayoutAlgorithm.builder()),
    MULTI_ROW_TREE("Multirow Tree", MultiRowTreeLayoutAlgorithm.builder()),
    BALLOON("Balloon",  BalloonLayoutAlgorithm.builder()),
    RADIAL("Radial",  RadialTreeLayoutAlgorithm.builder());

    private static final Map<String, Builders> BY_NAME = new HashMap<>();

    static {
      for (Builders layout : values()) {
        BY_NAME.put(layout.name, layout);
      }
    }

    Builders(String name, LayoutAlgorithm.Builder builder) {
      this.name = name;
      this.builder = builder;
    }

    private final String name;

    private final LayoutAlgorithm.Builder builder;

    public LayoutAlgorithm.Builder getLayoutAlgorithmBuilder() {
      return builder;
    }

    @Override
    public String toString() {
      return name;
    }

    public static Builders valueOfName(String name) {
      return BY_NAME.get(name);
    }
  }

  public static Builders[] getCombos() {
    return Builders.values();
  }
}
