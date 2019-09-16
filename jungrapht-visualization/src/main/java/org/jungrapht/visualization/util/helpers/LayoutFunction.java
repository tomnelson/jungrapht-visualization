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

    public Layout(String name, LayoutAlgorithm.Builder<V, ?, ?> builder) {
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
          new Layout("Kamada Kawai", KKLayoutAlgorithm.<V>builder()),
          new Layout("Circle", CircleLayoutAlgorithm.<V>builder()),
          new Layout("Self Organizing Map", ISOMLayoutAlgorithm.<V>builder()),
          new Layout("Fruchterman Reingold", FRLayoutAlgorithm.<V>builder()),
          new Layout(
              "Fruchterman Reingold (BH Optimized)",
              FRLayoutAlgorithm.builder()
                  .repulsionContractBuilder(BarnesHutFRRepulsion.barnesHutBuilder())),
          new Layout("Spring", SpringLayoutAlgorithm.<V>builder()),
          new Layout(
              "Spring (BH Optimized)",
              SpringLayoutAlgorithm.<V>builder()
                  .repulsionContractBuilder(BarnesHutSpringRepulsion.barnesHutBuilder())),
          new Layout("Tree", TreeLayoutAlgorithm.<V>builder()),
          new Layout("EdgeAwareTree", EdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()),
          new Layout("Multirow Tree", MultiRowTreeLayoutAlgorithm.<V>builder()),
          new Layout(
              "EdgeAwareMultirow Tree",
              MultiRowEdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()),
          new Layout("Balloon", BalloonLayoutAlgorithm.<V>builder()),
          new Layout("Radial", RadialTreeLayoutAlgorithm.<V>builder()),
          new Layout(
              "EdgeAwareRadial", RadialEdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()));
    }
  }
}
