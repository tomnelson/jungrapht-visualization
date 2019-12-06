package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.jungrapht.visualization.RenderContext;

public interface RenderContextAware<V, E> {

  void setRenderContext(RenderContext<V, E> renderContext);
}
