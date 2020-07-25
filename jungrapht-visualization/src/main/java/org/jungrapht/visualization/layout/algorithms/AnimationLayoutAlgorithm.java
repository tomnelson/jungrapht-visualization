package org.jungrapht.visualization.layout.algorithms;

import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.util.AfterRunnable;
import org.jungrapht.visualization.layout.algorithms.util.IterativeContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
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
    protected LayoutModel<V> layoutModel;
    protected LayoutAlgorithm<V> endLayoutAlgorithm;
    protected Runnable after = () -> {};

    public B layoutModel(LayoutModel<V> layoutModel) {
      this.layoutModel = layoutModel;
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
  protected Runnable after = () -> {};

  LayoutModel<V> transitionLayoutModel;
  LayoutAlgorithm<V> endLayoutAlgorithm;
  LayoutModel<V> layoutModel;

  public AnimationLayoutAlgorithm() {
    this(AnimationLayoutAlgorithm.builder());
  }

  protected AnimationLayoutAlgorithm(Builder<V, ?, ?> builder) {
    super(builder);
    this.layoutModel = builder.layoutModel;
    this.endLayoutAlgorithm = builder.endLayoutAlgorithm;
    if (endLayoutAlgorithm instanceof AfterRunnable) {
      ((AfterRunnable) this.endLayoutAlgorithm).setAfter(builder.after);
    } else {
      this.after = builder.after;
    }
  }

  public void visit(LayoutModel<V> layoutModel) {
    Graph<V, ?> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    // save off the existing layoutModel
    this.layoutModel = layoutModel;
    // create a LayoutModel to hold points for the transition
    this.transitionLayoutModel =
        LayoutModel.<V>builder()
            .graph(layoutModel.getGraph())
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
      this.layoutModel.accept(endLayoutAlgorithm);
    }
  }

  public boolean done() {
    if (done) after.run();
    return done;
  }
}
