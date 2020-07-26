package org.jungrapht.visualization.util;

import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.AfterRunnable;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the transition to a new LayoutAlgorithm. The transition can me animated or immediate. The
 * view side has a reference to the VisualizationServer so that it can manage activity of the
 * Spatial structures during the transition. Typically, they are turned off until the transition is
 * complete to minimize unnecessary work.
 */
public class LayoutAlgorithmTransition {

  private static Logger log = LoggerFactory.getLogger(LayoutAlgorithmTransition.class);

  public static <V, E> void animate(
      VisualizationServer<V, E> visualizationServer, LayoutAlgorithm<V> endLayoutAlgorithm) {
    animate(visualizationServer, endLayoutAlgorithm, () -> {});
  }

  public static <V, E> void animate(
      VisualizationServer<V, E> visualizationServer,
      LayoutAlgorithm<V> endLayoutAlgorithm,
      Runnable after) {
    fireLayoutStateChanged(visualizationServer.getVisualizationModel().getLayoutModel(), true);

    if (endLayoutAlgorithm instanceof AfterRunnable) {
      ((AfterRunnable) endLayoutAlgorithm).setAfter(after);
    }

    LayoutAlgorithm<V> transitionLayoutAlgorithm =
        AnimationLayoutAlgorithm.<V>builder()
            .after(after)
            .visualizationServer(visualizationServer)
            .endLayoutAlgorithm(endLayoutAlgorithm)
            .prerelax(false)
            .build();
    visualizationServer.getVisualizationModel().setLayoutAlgorithm(transitionLayoutAlgorithm);
  }

  public static <V, E> void apply(
      VisualizationServer<V, E> visualizationServer,
      LayoutAlgorithm<V> endLayoutAlgorithm,
      Runnable after) {

    if (endLayoutAlgorithm instanceof AfterRunnable) {
      ((AfterRunnable) endLayoutAlgorithm).setAfter(after);
    }
    visualizationServer.getVisualizationModel().setLayoutAlgorithm(endLayoutAlgorithm);
  }

  public static <V, E> void apply(
      VisualizationServer<V, E> visualizationServer, LayoutAlgorithm<V> endLayoutAlgorithm) {
    apply(visualizationServer, endLayoutAlgorithm, () -> {});
  }

  private static void fireLayoutStateChanged(LayoutModel layoutModel, boolean state) {
    log.trace("fireLayoutStateChanged to {}", state);
    layoutModel.getLayoutStateChangeSupport().fireLayoutStateChanged(layoutModel, state);
  }
}
