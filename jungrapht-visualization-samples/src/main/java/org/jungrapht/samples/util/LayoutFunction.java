package org.jungrapht.samples.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.DAGLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.EdgeAwareTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.ForceAtlas2LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.GEMLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.ISOMLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.MultiRowEdgeAwareTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.MultiRowTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialEdgeAwareTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TidierRadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TidierTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.orthogonal.OrthogonalLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFA2Repulsion;

public class LayoutFunction<V>
    implements Function<String, LayoutAlgorithm.Builder<V, LayoutAlgorithm<V>, ?>> {

  Map<String, LayoutAlgorithm.Builder<V, LayoutAlgorithm<V>, ?>> map = new LinkedHashMap<>();

  public static class Layout<V> {
    public final String name;
    public final LayoutAlgorithm.Builder<V, LayoutAlgorithm<V>, ?> builder;

    public static <V> Layout of(String name, LayoutAlgorithm.Builder<V, ?, ?> builder) {
      return new Layout(name, builder);
    }

    private Layout(String name, LayoutAlgorithm.Builder<V, LayoutAlgorithm<V>, ?> builder) {
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
  public LayoutAlgorithm.Builder<V, LayoutAlgorithm<V>, ?> apply(String s) {
    return map.get(s);
  }

  public static class FullLayoutFunction<V, E> extends LayoutFunction<V> {
    public FullLayoutFunction() {
      super(
          Layout.of("Kamada Kawai", KKLayoutAlgorithm.<V>builder()),
          Layout.of("Circle", CircleLayoutAlgorithm.<V>builder().reduceEdgeCrossing(false)),
          Layout.of(
              "Reduced Xing Circle", CircleLayoutAlgorithm.<V>builder().reduceEdgeCrossing(true)),
          Layout.of("Self Organizing Map", ISOMLayoutAlgorithm.<V>builder()),
          Layout.of("Fruchterman Reingold", FRLayoutAlgorithm.<V>builder()),
          Layout.of(
              "ForceAtlas2",
              ForceAtlas2LayoutAlgorithm.builder()
                  .repulsionContractBuilder(BarnesHutFA2Repulsion.builder().repulsionK(50))),
          Layout.of("Spring", SpringLayoutAlgorithm.<V, E>builder()),
          Layout.of("GEM", GEMLayoutAlgorithm.<V, E>edgeAwareBuilder()),
          Layout.of("DAG", DAGLayoutAlgorithm.<V, E>builder()),
          Layout.of("Tree", TreeLayoutAlgorithm.<V>builder()),
          Layout.of("Tidier Tree", TidierTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()),
          Layout.of("Tidier Radial Tree", TidierRadialTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()),
          Layout.of("EdgeAware Tree", EdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()),
          Layout.of("Multirow Tree", MultiRowTreeLayoutAlgorithm.<V>builder()),
          Layout.of("Orthogonal", OrthogonalLayoutAlgorithm.<V,E>builder()),
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
