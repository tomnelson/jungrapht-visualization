package org.jungrapht.visualization.layout.algorithms;

import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.algorithms.util.IterativeContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.LoadingCacheLayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class AnimationLayoutAlgorithm<N> extends AbstractIterativeLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(AnimationLayoutAlgorithm.class);

  public static class Builder<N> extends AbstractIterativeLayoutAlgorithm.Builder {
    protected VisualizationServer<N, ?> visualizationServer;
    protected LayoutAlgorithm<N> endLayoutAlgorithm;

    public Builder visualizationServer(VisualizationServer<N, ?> visualizationServer) {
      this.visualizationServer = visualizationServer;
      return this;
    }

    public Builder endLayoutAlgorithm(LayoutAlgorithm<N> endLayoutAlgorithm) {
      this.endLayoutAlgorithm = endLayoutAlgorithm;
      return this;
    }

    public AnimationLayoutAlgorithm<N> build() {
      return new AnimationLayoutAlgorithm<>(this);
    }
  }

  public static <N> Builder<N> builder() {
    return new Builder();
  }

  protected boolean done = false;
  protected int count = 20;
  protected int counter = 0;

  LayoutModel<N> transitionLayoutModel;
  VisualizationServer<N, ?> visualizationServer;
  LayoutAlgorithm<N> endLayoutAlgorithm;
  LayoutModel<N> layoutModel;

  protected AnimationLayoutAlgorithm(Builder<N> builder) {
    super(builder);
    this.visualizationServer = builder.visualizationServer;
    this.endLayoutAlgorithm = builder.endLayoutAlgorithm;
  }

  public void visit(LayoutModel<N> layoutModel) {
    // save off the existing layoutModel
    this.layoutModel = layoutModel;
    // create a LayoutModel to hold points for the transition
    this.transitionLayoutModel =
        LoadingCacheLayoutModel.<N>builder()
            .graph(visualizationServer.getModel().getNetwork())
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
    for (N v : layoutModel.getGraph().vertexSet()) {
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
      this.visualizationServer.getModel().setLayoutAlgorithm(endLayoutAlgorithm);
    }
  }

  public boolean done() {
    return done;
  }
}
