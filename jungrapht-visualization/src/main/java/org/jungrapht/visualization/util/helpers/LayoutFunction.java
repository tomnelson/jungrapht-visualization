package org.jungrapht.visualization.util.helpers;

import java.util.*;
import java.util.function.Function;
import org.jungrapht.visualization.layout.algorithms.*;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutSpringRepulsion;

public class LayoutFunction<V> implements Function<String, LayoutAlgorithm.Builder<V, ?, ?>> {

  Map<String, LayoutAlgorithm.Builder<V, ?, ?>> map = new LinkedHashMap<>();

  public static class Layout<V> {
    public final String name;
    public final LayoutAlgorithm.Builder<V, ?, ?> builder;

    public static <V> Layout of(String name, LayoutAlgorithm.Builder<V, ?, ?> builder) {
      return new Layout(name, builder);
    }

    private Layout(String name, LayoutAlgorithm.Builder<V, ?, ?> builder) {
      this.name = name;
      this.builder = builder;
    }
  }

  public Collection<String> getNames() {
    return map.keySet();
  }

  public LayoutFunction(Layout<V>... layouts) {
    Arrays.stream(layouts).forEach(e -> map.put(e.name, e.builder));
  }

  @Override
  public LayoutAlgorithm.Builder<V, ?, ?> apply(String s) {
    return map.get(s);
  }

  public static class FullLayoutFunction<V, E> extends LayoutFunction<V> {
    public FullLayoutFunction() {
      super(
          Layout.of("Kamada Kawai", KKLayoutAlgorithm.<V>builder()),
          Layout.of("Circle", CircleLayoutAlgorithm.<V>builder()),
          Layout.of("Self Organizing Map", ISOMLayoutAlgorithm.<V>builder()),
          Layout.of("Fruchterman Reingold", FRLayoutAlgorithm.<V>builder()),
          Layout.of(
              "Fruchterman Reingold (BH Optimized)",
              FRLayoutAlgorithm.builder()
                  .repulsionContractBuilder(BarnesHutFRRepulsion.barnesHutBuilder())),
          Layout.of("Spring", SpringLayoutAlgorithm.<V>builder()),
          Layout.of(
              "Spring (BH Optimized)",
              SpringLayoutAlgorithm.<V>builder()
                  .repulsionContractBuilder(BarnesHutSpringRepulsion.barnesHutBuilder())),
          Layout.of("Tree", TreeLayoutAlgorithm.<V>builder()),
          Layout.of("EdgeAwareTree", EdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()),
          Layout.of("Multirow Tree", MultiRowTreeLayoutAlgorithm.<V>builder()),
          Layout.of(
              "EdgeAwareMultirow Tree",
              MultiRowEdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()),
          Layout.of("Balloon", BalloonLayoutAlgorithm.<V>builder()),
          Layout.of("Radial", RadialTreeLayoutAlgorithm.<V>builder()),
          Layout.of(
              "EdgeAwareRadial", RadialEdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()));
    }
  }
}
