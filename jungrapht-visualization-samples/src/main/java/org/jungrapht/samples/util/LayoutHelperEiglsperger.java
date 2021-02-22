package org.jungrapht.samples.util;

import java.util.HashMap;
import java.util.Map;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.eiglsperger.experimental.ExpEiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;

public class LayoutHelperEiglsperger {

  public enum Layouts {
    EIGLSPERGERTD(
        "Eiglsperger TopDown",
        EiglspergerLayoutAlgorithm.builder().threaded(false).layering(Layering.TOP_DOWN)),
    EIGLSPERGERLP(
        "Eiglsperger LongestPath",
        EiglspergerLayoutAlgorithm.builder().threaded(false).layering(Layering.LONGEST_PATH)),
    EIGLSPERGERNS(
        "Eiglsperger NetworkSimplex",
        EiglspergerLayoutAlgorithm.builder().threaded(false).layering(Layering.NETWORK_SIMPLEX)),
    EIGLSPERGERCG(
        "EiglspergerCoffmanGraham",
        EiglspergerLayoutAlgorithm.builder().threaded(false).layering(Layering.COFFMAN_GRAHAM)),
    EXPEIGLSPERGERTD(
        "Exp Eiglsperger TopDown",
        ExpEiglspergerLayoutAlgorithm.builder().threaded(false).layering(Layering.TOP_DOWN)),
    EXPEIGLSPERGERLP(
        "Exp Eiglsperger LongestPath",
        ExpEiglspergerLayoutAlgorithm.builder().threaded(false).layering(Layering.LONGEST_PATH)),
    EXPEIGLSPERGERNS(
        "Exp Eiglsperger NetworkSimplex",
        ExpEiglspergerLayoutAlgorithm.builder().threaded(false).layering(Layering.NETWORK_SIMPLEX)),
    EXPEIGLSPERGERCG(
        "Exp EiglspergerCoffmanGraham",
        ExpEiglspergerLayoutAlgorithm.builder().threaded(false).layering(Layering.COFFMAN_GRAHAM));

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
