package org.jungrapht.visualization.util.helpers;

import java.util.HashMap;
import java.util.Map;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.ISOMLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardSpringRepulsion;

public class SubLayoutHelper {

  public enum Layouts {
    KK("Kamada Kawai", new KKLayoutAlgorithm<>()),
    CIRCLE("Circle", new CircleLayoutAlgorithm<>()),
    SELF_ORGANIZING_MAP("Self Organizing Map", new ISOMLayoutAlgorithm<>()),
    FR("Fruchterman Reingold (FR)", new FRLayoutAlgorithm<>()),
    FR_BH_VISITOR(
        "Fruchterman Reingold",
        FRLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutFRRepulsion.barnesHutBuilder())
            .build()),
    SPRING("Spring", new SpringLayoutAlgorithm<>()),
    SPRING_BH_VISITOR(
        "Spring (BH Optimized)",
        SpringLayoutAlgorithm.builder()
            .repulsionContractBuilder(StandardSpringRepulsion.standardBuilder())
            .build());

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
