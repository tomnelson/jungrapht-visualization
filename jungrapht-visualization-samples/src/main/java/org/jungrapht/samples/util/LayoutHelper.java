package org.jungrapht.samples.util;

import java.util.HashMap;
import java.util.Map;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.ISOMLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.MultiRowTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutSpringRepulsion;

public class LayoutHelper {

  public enum Layouts {
    KK("Kamada Kawai", KKLayoutAlgorithm.builder().build()),
    CIRCLE("Circle", CircleLayoutAlgorithm.builder().build()),
    SELF_ORGANIZING_MAP("Self Organizing Map", ISOMLayoutAlgorithm.builder().build()),
    FR("Fruchterman Reingold", FRLayoutAlgorithm.builder().build()),
    FR_BH_VISITOR(
        "Fruchterman Reingold (BH Optimized)",
        FRLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutFRRepulsion.barnesHutBuilder())
            .build()),
    SPRING("Spring", SpringLayoutAlgorithm.builder().build()),
    SPRING_BH_VISITOR(
        "Spring (BH Optimized)",
        SpringLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutSpringRepulsion.barnesHutBuilder())
            .build()),
    TREE("Tree", TreeLayoutAlgorithm.builder().build()),
    MULTI_ROW_TREE("Multirow Tree", MultiRowTreeLayoutAlgorithm.builder().build()),
    BALLOON("Balloon", BalloonLayoutAlgorithm.builder().build()),
    RADIAL("Radial", RadialTreeLayoutAlgorithm.builder().build());

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
