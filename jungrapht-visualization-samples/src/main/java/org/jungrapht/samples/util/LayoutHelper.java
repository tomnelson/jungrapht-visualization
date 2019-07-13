package org.jungrapht.samples.util;

import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.ISOMLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithmWithDijkstra;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutSpringRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardSpringRepulsion;

public class LayoutHelper {

  public enum Layouts {
    KK("Kamada Kawai"),
    KK2("Kamada Kawai2"),
    CIRCLE("Circle"),
    SELF_ORGANIZING_MAP("Self Organizing Map"),
    FR("Fruchterman Reingold (FR)"),
    FR_BH_VISITOR("Fruchterman Reingold (BH Optimized)"),
    SPRING("Spring"),
    SPRING_BH_VISITOR("Spring (BH Optimized)"),
    TREE("Tree"),
    BALLOON("Balloon"),
    RADIAL("Radial");

    Layouts(String name) {
      this.name = name;
    }

    private final String name;

    @Override
    public String toString() {
      return name;
    }
  }

  public static Layouts[] getCombos() {
    return Layouts.values();
  }

  public static LayoutAlgorithm createLayout(Layouts layoutType) {
    switch (layoutType) {
      case CIRCLE:
        return CircleLayoutAlgorithm.builder().build();
      case FR:
        return FRLayoutAlgorithm.builder().build();
      case KK:
        return KKLayoutAlgorithm.builder().build();
      case KK2:
        return KKLayoutAlgorithmWithDijkstra.builder().build();
      case SELF_ORGANIZING_MAP:
        return ISOMLayoutAlgorithm.builder().build();
      case FR_BH_VISITOR:
        return FRLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutFRRepulsion.barnesHutBuilder())
            .build();
      case SPRING:
        return SpringLayoutAlgorithm.builder()
            .repulsionContractBuilder(StandardSpringRepulsion.standardBuilder())
            .build();
      case SPRING_BH_VISITOR:
        return SpringLayoutAlgorithm.builder()
            .repulsionContractBuilder(BarnesHutSpringRepulsion.barnesHutBuilder())
            .build();
      case TREE:
        return TreeLayoutAlgorithm.builder().build();
      case BALLOON:
        return BalloonLayoutAlgorithm.builder().build();
      case RADIAL:
        return RadialTreeLayoutAlgorithm.builder().build();
      default:
        throw new IllegalArgumentException("Unrecognized layout type");
    }
  }
}
