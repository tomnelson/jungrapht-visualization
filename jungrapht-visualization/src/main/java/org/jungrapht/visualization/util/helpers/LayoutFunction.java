package org.jungrapht.visualization.util.helpers;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jungrapht.visualization.layout.algorithms.*;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutSpringRepulsion;

public class LayoutFunction<V, E> implements Function<String, LayoutAlgorithm.Builder<V, ?, ?>> {

  Map<String, LayoutAlgorithm.Builder<V, ?, ?>> map;

  public static class Layout<V> {
    public final String name;
    public final LayoutAlgorithm.Builder<V, ?, ?> builder;

    public Layout(String name, LayoutAlgorithm.Builder<V, ?, ?> builder) {
      this.name = name;
      this.builder = builder;
    }
  }

  public Collection<String> getNames() {
    return map.keySet();
  }

  public LayoutFunction(Layout<V>... layouts) {
    this.map = Arrays.stream(layouts).collect(Collectors.toMap(e -> e.name, e -> e.builder, (v1,v2) ->{ throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));}, LinkedHashMap::new));
  }

  @Override
  public LayoutAlgorithm.Builder<V, ?, ?> apply(String s) {
    return map.get(s);
  }

  public static class FullLayoutFunction<V, E> extends LayoutFunction<V, E> {
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
