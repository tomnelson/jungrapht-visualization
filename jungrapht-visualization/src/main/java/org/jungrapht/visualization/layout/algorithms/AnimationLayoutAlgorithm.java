package org.jungrapht.visualization.layout.algorithms;

import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.algorithms.util.IterativeContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.LoadingCacheLayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class AnimationLayoutAlgorithm<V> extends AbstractIterativeLayoutAlgorithm<V>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(AnimationLayoutAlgorithm.class);

  public static class Builder<V, T extends AnimationLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      extends AbstractIterativeLayoutAlgorithm.Builder<V, T, B>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected VisualizationServer<V, ?> visualizationServer;
    protected LayoutAlgorithm<V> endLayoutAlgorithm;
    protected Runnable after = null;

    public B visualizationServer(VisualizationServer<V, ?> visualizationServer) {
      this.visualizationServer = visualizationServer;
      return self();
    }

    public B endLayoutAlgorithm(LayoutAlgorithm<V> endLayoutAlgorithm) {
      this.endLayoutAlgorithm = endLayoutAlgorithm;
      return self();
    }

    public B after(Runnable after) {
      this.after = after;
      return self();
    }

    public T build() {
      return (T) new AnimationLayoutAlgorithm<>(this);
    }
  }

  public static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  protected boolean done = false;
  protected int count = 20;
  protected int counter = 0;
  protected Runnable after;

  LayoutModel<V> transitionLayoutModel;
  VisualizationServer<V, ?> visualizationServer;
  LayoutAlgorithm<V> endLayoutAlgorithm;
  LayoutModel<V> layoutModel;

  protected AnimationLayoutAlgorithm(Builder<V, ?, ?> builder) {
    super(builder);
    this.visualizationServer = builder.visualizationServer;
    this.endLayoutAlgorithm = builder.endLayoutAlgorithm;
    this.after = builder.after;
  }

  public void visit(LayoutModel<V> layoutModel) {
    // save off the existing layoutModel
    this.layoutModel = layoutModel;
    // create a LayoutModel to hold points for the transition
    this.transitionLayoutModel =
        LoadingCacheLayoutModel.<V>builder()
            .graph(visualizationServer.getVisualizationModel().getGraph())
            .layoutModel(layoutModel)
            .initializer(layoutModel)
            .build();
    // start off the transitionLayoutModel with the endLayoutAlgorithm
    transitionLayoutModel.accept(endLayoutAlgorithm);
  }

  /**
   * each step of the animation moves every pouit 1/count of the distance from its old location to
   * its new location
   */
  public void step() {
    for (V v : layoutModel.getGraph().vertexSet()) {
      Point tp = layoutModel.apply(v);
      Point fp = transitionLayoutModel.apply(v);
      double dx = (fp.x - tp.x) / (count - counter);
      double dy = (fp.y - tp.y) / (count - counter);
      log.trace("dx:{},dy:{}", dx, dy);
      layoutModel.set(v, tp.x + dx, tp.y + dy);
    }
    counter++;
    if (counter >= count) {
      done = true;
      this.transitionLayoutModel.stopRelaxer();
      this.visualizationServer.getVisualizationModel().setLayoutAlgorithm(endLayoutAlgorithm);
    }
  }

  public boolean done() {
    after.run();
    return done;
  }
}
