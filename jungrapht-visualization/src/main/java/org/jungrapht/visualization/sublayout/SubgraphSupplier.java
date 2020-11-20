package org.jungrapht.visualization.sublayout;

import java.util.function.Supplier;
import org.jgrapht.Graph;

public interface SubgraphSupplier<V, E> extends Supplier<Graph<V, E>> {}
