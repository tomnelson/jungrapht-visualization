package org.jungrapht.samples.experimental;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.util.Attributed;

public class LayoutHelperEiglsperger {

  static final Predicate<Attributed<Integer>> favoredEdgePredicate =
      e -> "Fall-Through".equals(e.get("EdgeType"));
  //              || "Unconditional-Jump".equals(e.get("EdgeType"));

  static final Comparator<Attributed<Integer>> edgeComparator = new EdgeComparator();

  public enum Layouts {
    EIGLSPERGERTD(
        "Eiglsperger TopDown",
        EiglspergerLayoutAlgorithm.<Attributed<String>, Attributed<Integer>>builder()
            .threaded(false)
            .edgeComparator(edgeComparator)
            .layering(Layering.TOP_DOWN)),
    EIGLSPERGERLP(
        "Eiglsperger LongestPath",
        EiglspergerLayoutAlgorithm.<Attributed<String>, Attributed<Integer>>builder()
            .threaded(false)
            .edgeComparator(edgeComparator)
            .layering(Layering.LONGEST_PATH)),
    EIGLSPERGERNS(
        "Eiglsperger NetworkSimplex",
        EiglspergerLayoutAlgorithm.<Attributed<String>, Attributed<Integer>>builder()
            .threaded(false)
            .edgeComparator(edgeComparator)
            .layering(Layering.NETWORK_SIMPLEX)),
    EIGLSPERGERCG(
        "EiglspergerCoffmanGraham",
        EiglspergerLayoutAlgorithm.<Attributed<String>, Attributed<Integer>>builder()
            .threaded(false)
            .edgeComparator(edgeComparator)
            .layering(Layering.COFFMAN_GRAHAM)),
    EXPEIGLSPERGERTD(
        "Exp Eiglsperger TopDown",
        EiglspergerLayoutAlgorithm.<Attributed<String>, Attributed<Integer>>builder()
            .favoredEdgePredicate(favoredEdgePredicate)
            .threaded(false)
            .edgeComparator(edgeComparator)
            .layering(Layering.TOP_DOWN)),
    EXPEIGLSPERGERLP(
        "Exp Eiglsperger LongestPath",
        EiglspergerLayoutAlgorithm.<Attributed<String>, Attributed<Integer>>builder()
            .favoredEdgePredicate(favoredEdgePredicate)
            .threaded(false)
            .edgeComparator(edgeComparator)
            .layering(Layering.LONGEST_PATH)),
    EXPEIGLSPERGERNS(
        "Exp Eiglsperger NetworkSimplex",
        EiglspergerLayoutAlgorithm.<Attributed<String>, Attributed<Integer>>builder()
            .favoredEdgePredicate(favoredEdgePredicate)
            .threaded(false)
            .edgeComparator(edgeComparator)
            .layering(Layering.NETWORK_SIMPLEX)),
    EXPEIGLSPERGERCG(
        "Exp EiglspergerCoffmanGraham",
        EiglspergerLayoutAlgorithm.<Attributed<String>, Attributed<Integer>>builder()
            .favoredEdgePredicate(favoredEdgePredicate)
            .threaded(false)
            .edgeComparator(edgeComparator)
            .layering(Layering.COFFMAN_GRAHAM));

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
