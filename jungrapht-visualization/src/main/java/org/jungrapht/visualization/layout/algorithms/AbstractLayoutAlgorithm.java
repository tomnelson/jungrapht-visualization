package org.jungrapht.visualization.layout.algorithms;

/**
 * For Layout algorithms that can run an 'after' function
 *
 * @author Tom Nelson
 */
public abstract class AbstractLayoutAlgorithm<V> implements LayoutAlgorithm<V> {

  public abstract static class Builder<
          V, T extends AbstractLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected Runnable after = () -> {};

    public B after(Runnable after) {
      this.after = after;
      return self();
    }

    protected B self() {
      return (B) this;
    }

    public abstract T build();
  }

  protected AbstractLayoutAlgorithm(Builder builder) {
    this.after = builder.after;
  }

  public void setAfter(Runnable after) {
    this.after = after;
  }

  public void runAfter() {
    if (after != null) after.run();
  }

  protected Runnable after;
}
